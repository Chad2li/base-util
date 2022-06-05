package io.github.chad2li.baseutil.redis.redisson;

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
import io.github.chad2li.baseutil.util.DateUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * redisson通用配置
 *
 * @author chad
 */
public class RedissonBaseConfig {
    public static final String OBJECT_MAPPER_NAME = "redissonBaseConfigObjectMapper";
    /**
     * redisson通用配置资源文件名
     */
    public static final String BASE_PROPERTY_FILE = "classpath:/redisson/redisson.properties";
    /**
     * 资源前缀
     */
    public static final String PROPERTY_PREFIX = "redisson";

    /**
     * 当名为 {@link RedissonBaseConfig#OBJECT_MAPPER_NAME} 的 ObjectMapper 不存在时，创建该 bean
     *
     * @return Redisson的对象映射工具
     * @date 2022/5/27 12:58
     * @author chad
     * @since 1 by chad at 2022/5/27
     */
    @ConditionalOnMissingBean(name = RedissonBaseConfig.OBJECT_MAPPER_NAME, value = ObjectMapper.class)
    @Bean(OBJECT_MAPPER_NAME)
    public ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 为null的数据不序列化
        mapper.setDateFormat(new SimpleDateFormat(DateUtils.FMT_DATE_TIME));
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
