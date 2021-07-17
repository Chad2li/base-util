package cn.lyjuan.base.redis.redisson;

import cn.lyjuan.base.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * redisson通用配置
 */
public class RedissonBaseConfig {
    public static final String OBJECT_MAPPER_NAME = "redissonBaseConfigObjectMapper";

    @Bean(OBJECT_MAPPER_NAME)
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 为null的数据不序列化
//        objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
//            @Override
//            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//                //设置返回null转为 空字符串""
//                gen.writeString("");
//            }
//        });
//        mapper.setDateFormat(new SimpleDateFormat(DateUtils.FMT_DATE_TIME));
        // 如果json中有新增的字段并且是实体类类中不存在的，不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 下面配置解决LocalDateTime序列化的问题
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        //日期序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE_TIME)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_TIME)));

        //日期反序列化
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE_TIME)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_TIME)));

        mapper.registerModule(javaTimeModule);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }
}
