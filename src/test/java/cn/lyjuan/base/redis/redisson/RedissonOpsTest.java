package cn.lyjuan.base.redis.redisson;

import cn.lyjuan.base.util.DateUtils;
import cn.lyjuan.base.util.JsonUtils;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RedissonOpsTest {
    private RedissonOps redissonOps;

    private static final String MASTER = "redis://192.168.1.201:7101";
    private static final String SLAVER = "redis://192.168.1.202:7102";
    private static final String PWD = "XXWck7QQQghPbittPNQErNyzxtOhcikVP0KifN3VsKjw8oht4gxN6RgSh3FGbVsPOskBF9AVQMXmjtCIDCrkUx8h10ifWSBcecd";


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

    @Before
    public void before() {
        Config config = new Config();
        ObjectMapper mapper = createObjectMapper();
        JsonJacksonCodec codec = new JsonJacksonCodec(mapper);
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
        redissonOps = new RedissonOps(client);
    }

    @Test
    public void setAndGet() {
        // map -> string
        String key = "test:for:redisson:set";
        Map map = new HashMap<>(2);
        map.put("name", "zhangsan");
        map.put("age", 1);
        map.put("mail", null);
        map.put("birthday", LocalDateTime.now());
        redissonOps.set(key, map);
        Map val = redissonOps.get(key);
        System.out.println("result map ==> " + JsonUtils.to(val));
        Assert.assertEquals(map.get("name"), val.get("name"));

        // string
        redissonOps.set(key, "abc");
        String valStr = redissonOps.get(key);
        System.out.println("result str ==> " + valStr);
        Assert.assertEquals("abc", valStr);

        // int
        redissonOps.set(key, 1);
        Integer valInt = redissonOps.get(key);
        System.out.println("result int ==> " + valInt);
        Assert.assertTrue(valInt.equals(1));

        // set with xx=true and exists
        boolean booleanVal = redissonOps.set(key, 2, true, 10);
        Assert.assertTrue(booleanVal);
        long ttl = redissonOps.ttl(key);
        Assert.assertTrue(ttl <= 10 * 1000);
        int intVal = redissonOps.get(key);
        Assert.assertTrue(2 == intVal);

        // set with xx=true and non exists
        redissonOps.del(key);
        booleanVal = redissonOps.set(key, 3, true, 10);
        Assert.assertFalse(booleanVal);

        // set with xx=false and non exists
        booleanVal = redissonOps.set(key, 4, false, 10);
        Assert.assertTrue(booleanVal);
        intVal = redissonOps.get(key);
        Assert.assertTrue(4 == intVal);

        // set with xx=false and exists
        booleanVal = redissonOps.set(key, 5, false, 10);
        Assert.assertFalse(booleanVal);

        // expire -1
        redissonOps.del(key);
        booleanVal = redissonOps.set(key, 6, false, -1);
        Assert.assertTrue(booleanVal);
        ttl = redissonOps.ttl(key);
        Assert.assertTrue(-1 == ttl);

        // array
        String[] arr = new String[]{"a", "b", "c"};
        redissonOps.set(key, arr);

    }

    @Test
    public void incr() {
        String key = "test:for:redisson:incr";
        redissonOps.del(key);
        redissonOps.set(key, 0);
        // incr by nil
        Long longVal = redissonOps.incr(key);
        Assert.assertTrue(1 == longVal);

        /// incr by exists
        longVal = redissonOps.incr(key);
        Assert.assertTrue(2 == longVal);

        // decr by nil
        redissonOps.del(key);
        longVal = redissonOps.decr(key);
        Assert.assertTrue(-1 == longVal);
        // decr by exists
        longVal = redissonOps.decr(key);
        Assert.assertTrue(-2 == longVal);

        // incr by step
        redissonOps.del(key);
        longVal = redissonOps.incr(key, 2);
        Assert.assertTrue(2 == longVal);

        // incr by -
        longVal = redissonOps.incr(key, -1);
        Assert.assertTrue(1 == longVal);

        redissonOps.set(key, longVal);
        longVal = redissonOps.getLong(key);// get int by long
        Assert.assertTrue(1 == longVal);

        // incr long
//        longVal = redissonOps.incr(key, Integer.MAX_VALUE);
//        Assert.assertEquals(1L + Integer.MAX_VALUE, longVal);
//        longVal = redissonOps.get(key);
//        Assert.assertEquals(1L + Integer.MAX_VALUE, longVal);
    }

    @Test
    public void compareAndSet() {
        String key = "test:for:redisson:compareAndSet";
        redissonOps.del(key);
        // string ok
        String valStr = "value";
        boolean isUpdate = redissonOps.compareAndSet(key, null, valStr);
        Assert.assertTrue(isUpdate);
        String valStr2 = redissonOps.get(key);
        Assert.assertEquals(valStr, valStr2);

        // string no
        valStr2 = "value2";
        isUpdate = redissonOps.compareAndSet(key, valStr2, "abc");
        Assert.assertFalse(isUpdate);

        // int ok
        // - 之前为string后再设置为integer，integer会转为string
        redissonOps.del(key);
        redissonOps.set(key, 1);
        isUpdate = redissonOps.compareAndSet(key, 1, 2);
        Assert.assertTrue(isUpdate);
        Integer valInt2 = redissonOps.get(key);
        Assert.assertTrue(valInt2.equals(2));
    }

    @Test
    public void hash() {
        String key = "test:for:redisson:hash";
        redissonOps.del(key);
        String stringK = "string";
        String intK = "int";

        Map<String, Object> map = new HashMap<>(4);
        map.put(intK, 1);
        map.put(stringK, "str");
        map.put("double", 1.0);
        map.put("boolean", false);
        Map subMap = new HashMap();
        subMap.put("string2", "str2");
        map.put("map", subMap);

        redissonOps.hSetMap(key, map);

        Map result = redissonOps.hGetAll(key);
        System.out.println("result ==> " + JsonUtils.to(result));
        Assert.assertEquals("str", result.get(stringK));

        // set & get
        String newKey = "newInt";
        boolean isNew = redissonOps.hmSet(key, newKey, 2);
        Assert.assertTrue(isNew);
        int intVal = redissonOps.hmGet(key, newKey);
        Assert.assertTrue(2 == intVal);
        isNew = redissonOps.hmSet(key, newKey, 3);
        Assert.assertFalse(isNew);

        // gets
        result = redissonOps.hGets(key, intK, stringK);
        Assert.assertTrue(2 == result.size());
        Assert.assertEquals("str", result.get(stringK));
        // gets by set
        Set setV = new HashSet(2);
        setV.add(stringK);
        setV.add(intK);
        result = redissonOps.hGets(key, setV);
        Assert.assertTrue(2 == result.size());
        Assert.assertEquals("str", result.get(stringK));

        // gets with non exists hask key
        result = redissonOps.hGets(key, intK, stringK, "nonExistsKey");
        Assert.assertTrue(2 == result.size());
    }

    @Test
    public void zset() {
        String key = "test:for:redisson:zset";
        redissonOps.del(key);

        String member1 = "member1";
        String member2 = "member2";

        // add
        boolean booleanVal = redissonOps.zAdd(key, member1, 1);
        Assert.assertTrue(booleanVal);
        double doubleVal = redissonOps.zScore(key, member1);
        Assert.assertTrue(1 == doubleVal);

        // set all
        Map<String, Double> values = new HashMap<>(2);
        values.put(member1, 2D);
        values.put(member2, 3D);
        int intVal = redissonOps.zAdd(key, values);
        Assert.assertEquals(1, intVal);
        doubleVal = redissonOps.zScore(key, member1);
        Assert.assertTrue(2 == doubleVal);
        doubleVal = redissonOps.zScore(key, member2);
        Assert.assertTrue(3 == doubleVal);

        // zcount
        intVal = redissonOps.zCount(key);
        Assert.assertTrue(2 == intVal);
        intVal = redissonOps.zCount(key, 1, 3);
        Assert.assertTrue(1 == intVal);
        intVal = redissonOps.zCount(key, 2, 3);
        Assert.assertTrue(1 == intVal);
        intVal = redissonOps.zCount(key, 2, 4);
        Assert.assertTrue(2 == intVal);

        // zRange by score
        Collection<Integer> collectionVal = redissonOps.zRange(key, 1D, 2D);
        Assert.assertTrue(0 == collectionVal.size());
        collectionVal = redissonOps.zRange(key, 1D, 3D);
        Assert.assertTrue(1 == collectionVal.size());
        collectionVal = redissonOps.zRange(key, 2D, 3D);
        Assert.assertTrue(1 == collectionVal.size());
        collectionVal = redissonOps.zRange(key, 1D, 4D);
        Assert.assertTrue(2 == collectionVal.size());

        // zRange by index
        collectionVal = redissonOps.zRange(key, 0, -1);
        Assert.assertTrue(2 == collectionVal.size());

        // zrange by score with score
        Collection<ScoredEntry<Integer>> scoreVal = redissonOps.zRangeWithScore(key, 1D, 3D);
        Assert.assertTrue(1 == scoreVal.size());
        scoreVal = redissonOps.zRangeWithScore(key, 2D, 4D);
        Assert.assertTrue(2 == scoreVal.size());
    }

}