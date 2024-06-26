package cn.beinet.codegenerate.repository;

import cn.beinet.codegenerate.model.RedisResultDto;
import cn.beinet.codegenerate.util.ParamRunnable;
import cn.beinet.codegenerate.util.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisRepository {

    private final String ip;
    private final int port;
    private final int dbIndex;
    private final String pwd;

    private StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

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
        if (!StringUtils.hasLength(key)) {
            ret.setResult("命令为空");
            return ret;
        }
        if (key.equalsIgnoreCase("info")) {
            ret.setResult(getInfo());
            return ret;
        }

        if (key.indexOf('*') > 0) {
            ret.setResult(getKeysByLike(key));
            return ret;
        }

        DataType type = getType(key);
        ret.setType(type.toString());
        if (type.equals(DataType.NONE)) {
            ret.setResult("指定的key在Redis中不存在");
            return ret;
        }

        String result;
        try {
            switch (type) {
                case HASH:
                    result = HGetAll(key);
                    break;
                case SET:
                    result = SMembers(key);
                    break;
                case LIST:
                    result = LRange(key);
                    break;
                case ZSET:
                    result = ZRange(key);
                    break;
                // case STRING:
                default:
                    result = getSimple(key);
                    break;
            }
        } catch (Exception exp) {
            result = "获取出错:" + exp.getMessage();
        }
// 	NONE("none"), STRING("string"), LIST("list"), SET("set"), ZSET("zset"), HASH("hash"),
        ret.setResult(result);
        ret.setTtl(getTTL(key));
        return ret;
    }

    public String getInfo() {
        Properties properties = getRedisTemplate().execute((RedisCallback<Properties>) connection -> connection.info());

        // 收集到list里，进行排序
        List<Map.Entry<Object, Object>> list = new ArrayList<Map.Entry<Object, Object>>(properties.entrySet());
        Collections.sort(list, (Map.Entry<Object, Object> o1, Map.Entry<Object, Object> o2) -> {
            String key1 = o1.getKey() + "";
            String key2 = o2.getKey() + "";
            return key1.compareTo(key2);
        });

        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Object, Object> item : list) {
            stringBuilder.append(item.getKey())
                    .append(" : ")
                    .append(item.getValue())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    public String getKeysByLike(String key) {
        int idx = key.indexOf('*');
        if (idx < 2) {
            return "keys至少要输入前2个字符";
        }

        Set<String> list = getRedisTemplate().keys(key);
        return serialObj(list);
    }

    public String getSimple(String key) {
        String val = getRedisTemplate().opsForValue().get(key);
        return serialObj(val);
    }

    // 取hash的所有值
    public String HGetAll(String key) {
        Map<Object, Object> map = getRedisTemplate().opsForHash().entries(key);
        return serialObj(map);
    }

    // 取set的所有值
    public String SMembers(String key) {
        Set<String> list = getRedisTemplate().opsForSet().members(key);
        return serialObj(list);
    }

    // 取list的所有值
    public String LRange(String key) {
        List<String> list = getRedisTemplate().opsForList().range(key, 0, -1);
        return serialObj(list);
    }

    // 取zset的所有值
    public String ZRange(String key) {
        Set<String> list = getRedisTemplate().opsForZSet().range(key, 0, -1);
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

    /**
     * 设置超时时间（秒）
     *
     * @param key    key
     * @param second 超时时间
     * @return true false
     */
    public Boolean setTTL(String key, int second) {
        return getRedisTemplate().expire(key, second, TimeUnit.SECONDS);
    }

    // 查询所有keys，直接写入输出流中
    @SneakyThrows
    public void saveAllKeys(OutputStream stream) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            getAllKeys("*", key -> {
                writer.write(key.toString());
                // 不能加，会严重拖慢速度
                //writer.write(" ttl:");
                //writer.write(getTTL(key).toString());
                writer.write("\r\n");
            });
            writer.flush();
        }
    }

    @SneakyThrows
    public List<String> getAllKeys(String match, ParamRunnable callable) {
        List<String> ret = new ArrayList<>();
        RedisConnection redisConnection = null;
        try {
            redisConnection = getRedisTemplate().getConnectionFactory().getConnection();
            ScanOptions options = ScanOptions.scanOptions().match(match).count(100).build();
            Cursor<byte[]> c = redisConnection.scan(options);
            while (c.hasNext()) {
                String key = new String(c.next());
                if (callable != null) {
                    callable.run(key);
                }
            }
        } finally {
            if (redisConnection != null)
                redisConnection.close(); //Ensure closing this connection.
        }
        return ret;
    }

    public Boolean removeKey(String key) {
        return getRedisTemplate().delete(key);
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
}
