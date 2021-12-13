package cn.beinet.codegenerate.repository;

import cn.beinet.codegenerate.model.RedisResultDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RedisRepository {

    private final String ip;
    private final int port;
    private final int dbIndex;
    private final String pwd;

    private StringRedisTemplate redisTemplate;
    private ObjectMapper objectMapper = createObjectMapper();

    public RedisRepository(String ip, int port, int dbIndex, String pwd) {
        this.ip = ip;
        this.port = port;
        this.pwd = pwd;
        this.dbIndex = dbIndex;
    }

    /**
     * 获取指定Key的值
     *
     * @param key key
     * @return 值
     */
    public RedisResultDto get(String key) {
        RedisResultDto ret = new RedisResultDto();
        if (key.indexOf('*') > 0) {
            ret.setResult(getKeysByLike(key));
            return ret;
        }

        DataType type = getType(key);
        ret.setType(type.toString());
        if (type.equals(DataType.NONE)) {
            return ret;
        }

        String result;
        switch (getType(key)) {
            case HASH:
                result = HGetAll(key);
            case SET:
                result = SMembers(key);
            case LIST:
                result = LRange(key);
            case ZSET:
                result = ZRange(key);
                // case STRING:
            default:
                result = getSimple(key);
        }
// 	NONE("none"), STRING("string"), LIST("list"), SET("set"), ZSET("zset"), HASH("hash"),
        ret.setResult(result);
        ret.setTtl(getTTL(key));
        return ret;
    }

    public String getKeysByLike(String key) {
        int idx = key.indexOf('*');
        if (idx < 2) {
            return "keys至少要输入前2个字符";
        }

        Set list = getRedisTemplate().keys(key);
        return serialObj(list);
    }

    public String getSimple(String key) {
        String val = getRedisTemplate().opsForValue().get(key);
        return serialObj(val);
    }

    // 取hash的所有值
    public String HGetAll(String key) {
        Map map = getRedisTemplate().opsForHash().entries(key);
        return serialObj(map);
    }

    // 取set的所有值
    public String SMembers(String key) {
        Set list = getRedisTemplate().opsForSet().members(key);
        return serialObj(list);
    }

    // 取list的所有值
    public String LRange(String key) {
        List list = getRedisTemplate().opsForList().range(key, 0, -1);
        return serialObj(list);
    }

    // 取zset的所有值
    public String ZRange(String key) {
        Set list = getRedisTemplate().opsForZSet().range(key, 0, -1);
        return serialObj(list);
    }

    /**
     * 返回指定key的类型
     *
     * @param key key
     * @return 类型
     */
    public DataType getType(String key) {
        return getRedisTemplate().type(key);
    }

    /**
     * 获取剩余超时时间（秒）
     *
     * @param key key
     * @return 剩余值
     */
    public Long getTTL(String key) {
        Long ret = getRedisTemplate().getExpire(key);
        if (ret == null)
            return 0L;
        return ret;
    }

    private String serialObj(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "序列化错误:" + e.getMessage();
        }
    }

    private StringRedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            RedisConnectionFactory factory = initConnection(ip, port, dbIndex, pwd);

            StringRedisTemplate redis = new StringRedisTemplate();
            redis.setConnectionFactory(factory);
            redis.afterPropertiesSet();

            redisTemplate = redis;
        }
        return redisTemplate;
    }

    private RedisConnectionFactory initConnection(String host, int port, int db, String pwd) {
        // 单实例redis
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        // 哨兵redis 用 new RedisSentinelConfiguration();
        // 集群redis 用 new RedisClusterConfiguration();

        if (StringUtils.isEmpty(host)) {
            host = "127.0.0.1";
        }

        int timeout = 1000;
        boolean useSsl = false;

        redisConfig.setHostName(host);
        redisConfig.setPassword(RedisPassword.of(pwd));
        redisConfig.setPort(port);
        redisConfig.setDatabase(db);

//        int maxActive = env.getProperty("spring.redis.jedis.pool.max-active", Integer.class, 8);
//        int maxWait = env.getProperty("spring.redis.jedis.pool.max-wait", Integer.class, -1);
//        int maxIdel = env.getProperty("spring.redis.jedis.pool.max-idle", Integer.class, 8);
//        int minIdel = env.getProperty("spring.redis.jedis.pool.min-idle", Integer.class, 0);
//        org.apache.commons.pool2.impl.GenericObjectPoolConfig poolConfig = new org.apache.commons.pool2.impl.GenericObjectPoolConfig();
//        poolConfig.setMaxTotal(maxActive);
//        poolConfig.setMaxIdle(maxIdel);
//        poolConfig.setMaxWaitMillis(maxWait);
//        poolConfig.setMinIdle(minIdel);

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration
                .builder()
                // .poolConfig(poolConfig)
                .commandTimeout(Duration.ofMillis(timeout));
        if (useSsl)
            builder.useSsl();
        LettuceClientConfiguration config = builder.build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, config);
        factory.afterPropertiesSet();
        log.info("initedRedisConnect: " + host + ':' + port + " db:" + db);
        return factory;
    }

    private ObjectMapper createObjectMapper() {
        JavaTimeModule module = new JavaTimeModule();
        // module.addDeserializer(LocalDateTime.class, new LocalDateTimeSerializerExt());

        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
                .modules(module, new SimpleModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)  // 禁用写时间戳，Could not read JSON: Cannot construct instance of `java.time.LocalDateTime`
                .featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES) // 反序列化时，忽略大小写
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)// 忽略未知属性
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)   // 忽略未知的类型，比如本项目里不存在的class
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                // .featuresToDisable(MapperFeature.USE_ANNOTATIONS)
                .featuresToEnable()
                .build();

        //if (enableTypeing) { // 序列化结果里的类型不需要，占用空间
        // 没有这一句，会抛异常：java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to xxx
        // mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        //}
        return mapper;
    }
}
