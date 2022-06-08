package cn.beinet.codegenerate.configs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * Description:
 *
 * @author : youbl
 * @create: 2022/6/6 17:31
 */
@Configuration
public class DefaultConfigurations {
    @Bean
    public ObjectMapper createMapper() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(Timestamp.class, new TimestampJsonSerializer());
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

    public static class TimestampJsonSerializer extends JsonSerializer<Timestamp> {

        @Override
        public void serialize(Timestamp timestamp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(timestamp.toString());
        }
    }
}
