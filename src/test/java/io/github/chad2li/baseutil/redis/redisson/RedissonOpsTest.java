package io.github.chad2li.baseutil.redis.redisson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
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
import io.github.chad2li.baseutil.util.DateUtils;
import io.github.chad2li.baseutil.util.JsonUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RLock;
import org.redisson.api.RMultimapAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.Encoder;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import javax.xml.datatype.XMLGregorianCalendar;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedissonOpsTest {
    private RedissonOps redissonOps;

    private static final String MASTER = "redis://192.168.1.201:7101";
    private static final String SLAVER = "redis://192.168.1.202:7102";
    private static final String PWD = "XXWck7QQQghPbittPNQErNyzxtOhcikVP0KifN3VsKjw8oht4gxN6RgSh3FGbVsPOskBF9AVQMXmjtCIDCrkUx8h10ifWSBcecd";

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

        // localDateTime -> no
//        LocalDateTime now = LocalDateTime.now();
//        redissonOps.set(key, now);
//        LocalDateTime now2 = redissonOps.get(key);
//        Assert.assertEquals(now, now2);

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
        boolean booleanVal = redissonOps.set(key, 2, true, 10L);
        Assert.assertTrue(booleanVal);
        long ttl = redissonOps.ttl(key);
        Assert.assertTrue(ttl <= 10 * 1000);
        int intVal = redissonOps.get(key);
        Assert.assertTrue(2 == intVal);

        // set with xx=true and non exists
        redissonOps.del(key);
        booleanVal = redissonOps.set(key, 3, true, 10L);
        Assert.assertFalse(booleanVal);

        // set with xx=false and non exists
        booleanVal = redissonOps.set(key, 4, false, 10L);
        Assert.assertTrue(booleanVal);
        intVal = redissonOps.get(key);
        Assert.assertTrue(4 == intVal);

        // set with xx=false and exists
        booleanVal = redissonOps.set(key, 5, false, 10L);
        Assert.assertFalse(booleanVal);

        // expire -1
        redissonOps.del(key);
        booleanVal = redissonOps.set(key, 6, false, -1L);
        Assert.assertTrue(booleanVal);
        ttl = redissonOps.ttl(key);
        Assert.assertTrue(-1 == ttl);

        // array
        String[] arr = new String[]{"a", "b", "c"};
        redissonOps.set(key, arr);


        // bean
        User userVal = user();
        redissonOps.set(key, userVal);
        User userVal2 = redissonOps.get(key);
        Assert.assertEquals(userVal.getName(), userVal2.getName());
        Assert.assertEquals(DateUtils.format(userVal.getCreateTime(), DateUtils.FMT_DATE_TIME),
                DateUtils.format(userVal2.getCreateTime(), DateUtils.FMT_DATE_TIME));

        // get with default
        redissonOps.del(key);
        intVal = redissonOps.get(key, 1);
        Assert.assertTrue(1 == intVal);
        intVal = redissonOps.get(key, 2);
        Assert.assertTrue(1 == intVal);

        // int not exists
        redissonOps.del(key);
        // int会报空指针异常
        Integer intVal2 = redissonOps.get(key);
        Assert.assertNull(intVal2);


        redissonOps.del(key);
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
        longVal = redissonOps.incr(key, Integer.MAX_VALUE);
        Assert.assertTrue(1L + Integer.MAX_VALUE == longVal);
        longVal = redissonOps.get(key);
        Assert.assertTrue(1L + Integer.MAX_VALUE == longVal);
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
    public void set() {
        String key1 = "test:for:redisson:set1";
        String key2 = "test:for:redisson:set2";
        String key3 = "test:for:redisson:set3";
        String key4 = "test:for:redisson:set4";
        redissonOps.del(key1, key2, key3, key4);

        int m1 = 1;
        int m2 = 2;
        int m3 = 3;

        // add & random
        redissonOps.sAdd(key1, m1);
        int intVal = redissonOps.sRandom(key1);
        Assert.assertTrue(m1 == intVal);

        // diffStore
        redissonOps.sAdds(key1, m1, m2);
        redissonOps.sAdd(key2, m2);
        redissonOps.sDiffStore(key3, key1, key2);
        intVal = redissonOps.sSize(key3);
        Assert.assertTrue(1 == intVal);

        // adds
        redissonOps.del(key1);
        boolean booleanVal = redissonOps.sAdds(key1, m1, m2);
        Assert.assertTrue(booleanVal);
        booleanVal = redissonOps.sAdds(key1, m1, m2);
        Assert.assertFalse(booleanVal);
        booleanVal = redissonOps.sAdds(key2, m1, m2, 3, 4);
        Assert.assertTrue(booleanVal);
    }

    @Test
    public void hash() {
        String key = "test:for:redisson:hash";
        redissonOps.del(key);

        String stringK = "string";
        String intK = "int";

        // set map
        Map<String, Object> map = new HashMap<>(4);
        map.put(intK, 1);
        map.put(stringK, "str");
        map.put("double", 1.0);
        map.put("boolean", false);
        map.put("localDateTime", LocalDateTime.now());
        Map subMap = new HashMap();
        subMap.put("string2", "str2");
        map.put("map", subMap);
        redissonOps.hSetMap(key, map);

        // get all
        Map result = redissonOps.hGetMap(key);
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

        // set bean
        // set with null
        User<User.UserAttr> userVal = null;
        userVal = new User("CacheVal", null, 18, "test@mail.com", null, null, null);
        redissonOps.hSetBean(key, userVal);
        redissonOps.del(key);

        userVal = user();
        redissonOps.hSetBean(key, userVal);
        Map mapVal = redissonOps.hGetMap(key);
        Assert.assertEquals(6, mapVal.size());
        Assert.assertEquals("Zhangsan", mapVal.get("name"));
        Assert.assertNull(mapVal.get("cache"));
        Assert.assertNull(mapVal.get("staticTest"));

        // get bean
        User<User.UserAttr> userVal2 = redissonOps.hGetBean(key, User.class);
        Assert.assertNotNull(userVal2);
        Assert.assertEquals(userVal.name, userVal2.name);
        Assert.assertEquals(userVal.age, userVal2.age);
        Assert.assertEquals(userVal.email, userVal2.email);

        // xx -> null
        redissonOps.del(key);
        String hashKey = "name";
        boolean booleanVal = redissonOps.hmSet(key, hashKey, 1, null);
        Assert.assertTrue(booleanVal);
        // xx -> true exists
        booleanVal = redissonOps.hmSet(key, hashKey, 1, true);
        Assert.assertTrue(booleanVal);
        // xx -> true not exists
        redissonOps.del(key);
        booleanVal = redissonOps.hmSet(key, hashKey, 1, true);
        Assert.assertFalse(booleanVal);
        // xx -> false not exists
        booleanVal = redissonOps.hmSet(key, hashKey, 1, false);
        Assert.assertTrue(booleanVal);
        // xx -> false exists
        booleanVal = redissonOps.hmSet(key, hashKey, 1, false);
        Assert.assertFalse(booleanVal);

        // hmExists
        redissonOps.del(key);
        redissonOps.hmSet(key, hashKey, 1);
        booleanVal = redissonOps.hmExists(key, hashKey);
        Assert.assertTrue(booleanVal);

        // hmDel
        redissonOps.hmSet(key, hashKey, 1);
        Assert.assertTrue(redissonOps.hmExists(key, hashKey));
        long longVal = redissonOps.hmDel(key, hashKey);
        Assert.assertEquals(1L, longVal);
        Assert.assertFalse(redissonOps.hmExists(key, hashKey));

        // hmDelAndGet
        redissonOps.hmSet(key, hashKey, 1);
        intVal = redissonOps.hmDelAndGet(key, hashKey);
        Assert.assertEquals(1, intVal);
        Assert.assertFalse(redissonOps.hmExists(key, hashKey));

        redissonOps.del(key);
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


        // min & max - null
        redissonOps.del(key);
        Object obj = redissonOps.zMax(key);
        Assert.assertNull(obj);
        // min & max
        booleanVal = redissonOps.zAdd(key, member1, 1);
        Assert.assertTrue(booleanVal);
        booleanVal = redissonOps.zAdd(key, member2, 2);
        Assert.assertTrue(booleanVal);
        String strVal = redissonOps.zMax(key);// max
        Assert.assertEquals(member2, strVal);
        strVal = redissonOps.zMin(key);// min
        Assert.assertEquals(member1, strVal);

        // incr
        redissonOps.del(key);
        doubleVal = redissonOps.zIncr(key, member1);
        Assert.assertEquals(1, doubleVal, 0);
        doubleVal = redissonOps.zIncr(key, member2);
        Assert.assertEquals(1, doubleVal, 0);
        // 相同score顺序不变
        doubleVal = redissonOps.zIncr(key, member2);
        Assert.assertEquals(2, doubleVal, 0);
        doubleVal = redissonOps.zIncr(key, member1);
        Assert.assertEquals(2, doubleVal, 0);

        // decr
        redissonOps.del(key);
        doubleVal = redissonOps.zDecrAndRank(key, member1);
        Assert.assertEquals(-1, doubleVal, 0);
        doubleVal = redissonOps.zScore(key, member1);
        Assert.assertEquals(-1D, doubleVal, 0);
        redissonOps.zIncr(key, member1);

        redissonOps.del(key);
    }

    private User user() {
        User userVal = null;
        User.UserAttr attr = new User.UserAttr(175, 65);
        List<User.UserAttr> attrs = new ArrayList<>(2);
        User.UserAttr attr1 = new User.UserAttr(176, 66);
        User.UserAttr attr2 = new User.UserAttr(177, 67);
        attrs.add(attr1);
        attrs.add(attr2);
        userVal = new User("CacheVal", "Zhangsan", 18, "test@mail.com", attr, attrs, LocalDateTime.now());

        return userVal;
    }


    @Test
    public void json() throws Exception {
        Encoder encoder = codec.getValueEncoder();

        // int
        long a = 1;
        print("int", encoder.encode(a));

        // sample -> no ????
        LocalDateTime now = LocalDateTime.now();
        print("now", encoder.encode(now));

        // obj -> ok
        User user = user();
        print("user", encoder.encode(user));

        // map -> ok
        Map map = new HashMap(2);
        map.put("int", 1);
        map.put("time", now);
        print("map", encoder.encode(map));
    }

    @Test
    public void lock() throws InterruptedException {
        String key = "test:for:redisson:lock";
//        redissonOps.del(key);

        // 同一个线程下，可重入
        RLock r1 = redissonOps.getLock(key);
        long start = System.currentTimeMillis();
        boolean booleanVal = r1.tryLock(5, 300, TimeUnit.SECONDS);
        System.out.println("duration ==> " + (System.currentTimeMillis() - start));
        Assert.assertTrue(booleanVal);
        Assert.assertEquals(1, r1.getHoldCount());
        RLock r2 = redissonOps.getLock(key);
        r2.lock();
        Assert.assertEquals(2, r1.getHoldCount());
        Assert.assertEquals(2, r2.getHoldCount());
        // try lock
        r1.unlock();
        r2.unlock();
        redissonOps.del(key);
        r1 = redissonOps.getLock(key);
        r2 = redissonOps.getLock(key);
        r1.tryLock();
        Assert.assertEquals(1, r1.getHoldCount());
        r2.tryLock();
        Assert.assertEquals(2, r1.getHoldCount());
        Assert.assertEquals(2, r2.getHoldCount());
        System.out.println(r1.getName());

        // 永久锁
        redissonOps.del(key);
        r1 = redissonOps.getLock(key);
        booleanVal = r1.tryLock(5, -1, TimeUnit.SECONDS);
        Assert.assertTrue(booleanVal);

        long longVal = redissonOps.ttl(key);
        Assert.assertEquals(-1, longVal);

        // 手动上锁
        redissonOps.del(key);
        redissonOps.hmSet(key, "succ", 1);
        r1 = redissonOps.getLock(key);
        booleanVal = r1.tryLock(3, TimeUnit.SECONDS);
        Assert.assertFalse(booleanVal);

    }

    @Test
    public void batch() {
        String key = "test:for:redisson:batch";
        RBatch rb = redissonOps.getBatch();
        RMultimapAsync<String, Integer> mapOper = rb.getSetMultimap(key);
        mapOper.putAsync("a", 1);
        mapOper.putAsync("b", 2);
        mapOper.expireAsync(300, TimeUnit.SECONDS);

        BatchResult<?> results = rb.execute();
        for (Object o : results.getResponses()) {
            System.out.println(o.getClass().getSimpleName() + " ==> " + o);
        }
    }

    private void print(String title, ByteBuf b) {
        System.out.println(title + " ==> " + b.toString(StandardCharsets.UTF_8));
    }


    public class CustomJsonJacksonCodec extends JsonJacksonCodec {

        public CustomJsonJacksonCodec(ObjectMapper mapObjectMapper) {
            super(mapObjectMapper);
        }

        @Override
        protected void initTypeInclusion(ObjectMapper mapObjectMapper) {
            TypeResolverBuilder<?> mapTyper = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL) {
                public boolean useForType(JavaType t) {
                    Class cls = t.getRawClass();
                    switch (_appliesFor) {
                        case NON_CONCRETE_AND_ARRAYS:
                            while (t.isArrayType()) {
                                t = t.getContentType();
                            }
                            // fall through
                        case OBJECT_AND_NON_CONCRETE:
                            return (t.getRawClass() == Object.class) || !t.isConcrete();
                        case NON_FINAL:
                            while (t.isArrayType()) {
                                t = t.getContentType();
                            }
                            // to fix problem with wrong long to int conversion
                            if (cls == Long.class) {
                                return true;
                            }
                            if (cls == LocalDateTime.class ||
                                    cls == LocalDate.class ||
                                    cls == LocalTime.class)
                                return true;
                            if (cls == XMLGregorianCalendar.class) {
                                return false;
                            }
                            return !t.isFinal(); // includes Object.class
                        default:
                            // case JAVA_LANG_OBJECT:
                            return cls == Object.class;
                    }
                }
            };
            mapTyper.init(JsonTypeInfo.Id.CLASS, null);
            mapTyper.inclusion(JsonTypeInfo.As.PROPERTY);
            mapObjectMapper.setDefaultTyping(mapTyper);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class User<T> {
        /**
         * 不缓存
         */
        public static String staticTest = "staticTest";
        public transient String cache;
        public String name;
        public int age;
        public String email;
        public T attr;
        public List<T> attrs;
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public LocalDateTime createTime;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class UserAttr {
            public int tail;
            public int weight;
        }
    }

}