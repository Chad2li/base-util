package io.github.chad2li.baseutil.redis.prod;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.github.chad2li.baseutil.redis.redisson.codec.CustomJsonJacksonCodec;
import io.github.chad2li.baseutil.util.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RedissonOps {
    private io.github.chad2li.baseutil.redis.redisson.RedissonOps redissonOps;

    private static final String MASTER = "redis://redis.hehewang.com:7001";
    private static final String SLAVER = "redis://redis2.hehewang.com:7002";
    private static final String PWD = "dqzNKFUCki6piL7jwQeRz1lRv2PmGvkV1vAYglZJQvBCgSzhbgDWBUTBIDqLGUyMPxmieZeLMHuHdJlMpabE6LDroIZmXuKZ4b1";

    private JsonJacksonCodec codec;


    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 为null的数据不序列化
//        mapper.setDateFormat(new SimpleDateFormat(DateUtils.FMT_DATE_TIME));
        // 如果json中有新增的字段并且是实体类类中不存在的，不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 下面配置解决LocalDateTime序列化的问题
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 生成带有有类型的json
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
//        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.EVERYTHING);

        //日期序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE_TIME)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_TIME)));

        //日期反序列化
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE_TIME)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_TIME)));

        mapper.registerModule(new Jdk8Module())
                .registerModule(javaTimeModule)
                .registerModule(new ParameterNamesModule())
        //
        ;
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }

    @Before
    public void before() {
        Config config = new Config();
        ObjectMapper mapper = createObjectMapper();
        codec = new CustomJsonJacksonCodec(mapper);
        config.setCodec(codec)
                .useReplicatedServers()
                .setDatabase(0)
                .addNodeAddress(MASTER, SLAVER)
                .setPassword(PWD)
                .setTimeout(3000)
                .setMasterConnectionPoolSize(1)
                .setSlaveConnectionPoolSize(2)
                .setSubscriptionConnectionPoolSize(1)
                .setMasterConnectionMinimumIdleSize(1)
                .setSlaveConnectionMinimumIdleSize(1)
                .setSubscriptionConnectionPoolSize(1)
        //
        ;

        RedissonClient client = Redisson.create(config);
        redissonOps = new io.github.chad2li.baseutil.redis.redisson.RedissonOps(client);
    }


    @Test
    public void test() {
        Object obj = redissonOps.get("sds:cs:group:prologue:limit:time:second");
        System.out.println("result ==> " + obj);
    }
}
