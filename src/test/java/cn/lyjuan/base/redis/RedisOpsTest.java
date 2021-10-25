package cn.lyjuan.base.redis;

import io.lettuce.core.ReadFrom;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class RedisOpsTest {

    private static final String pwd = "XXWck7QQQghPbittPNQErNyzxtOhcikVP0KifN3VsKjw8oht4gxN6RgSh3FGbVsPOskBF9AVQMXmjtCIDCrkUx8h10ifWSBcecd";

    private static final String KEY_PREFIX = "test:for:redisops:";

    private RedisOps redisOps;

    private RedisTemplate<String, String> rt;

    @Before
    public void before() {
        rt = new StringRedisTemplate();
        String host = "192.168.1.201";
        int port = 7101;

        GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(1);
        config.setMaxIdle(1);
        config.setMinIdle(1);
        config.setMaxWaitMillis(3 * 1000);

        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(config)
                .readFrom(ReadFrom.UPSTREAM_PREFERRED)
//                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(3)) //默认RedisURI.DEFAULT_TIMEOUT 60
                .build();

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);
        serverConfig.setPassword(pwd);
        serverConfig.setDatabase(0);

        LettuceConnectionFactory rcf = new LettuceConnectionFactory(serverConfig);
        rcf.afterPropertiesSet();
        rt.setConnectionFactory(rcf);
        rt.afterPropertiesSet();
        redisOps = new RedisOps(rt);
    }

    @Test
    public void setAndGet() throws InterruptedException {
        String key = "test:for:rediss:getAndset";
        redisOps.del(key);

        // xx
        Boolean booleanVal = redisOps.set(key, 1, null, 10L);
        Assert.assertTrue(booleanVal);
        long longVal = redisOps.ttl(key);
        Assert.assertTrue(longVal > 9);
        int intVal = redisOps.get(key, Integer.class);
        Assert.assertEquals(1, intVal);

        booleanVal = redisOps.set(key, 1, false, 10L);
        Assert.assertFalse(booleanVal);

        booleanVal = redisOps.set(key, 2, true, 10L);
        Assert.assertTrue(booleanVal);
        intVal = redisOps.get(key, Integer.class);
        Assert.assertEquals(2, intVal);

        // set - not exists - null expire
        redisOps.del(key);
        booleanVal = redisOps.set(key, 3, false, null);
        Assert.assertTrue(booleanVal);
        longVal = redisOps.ttl(key);
        Assert.assertEquals(-1, longVal);

        // set - not exists - expire
        redisOps.del(key);
        booleanVal = redisOps.set(key, 3, false, 10L);
        Assert.assertTrue(booleanVal);
        longVal = redisOps.ttl(key);
        Assert.assertTrue(longVal > 9);

        // set - exists - null expire
        booleanVal = redisOps.set(key, 3, true, null);
        Assert.assertTrue(booleanVal);
        longVal = redisOps.ttl(key);
        Assert.assertEquals(-1, longVal);

        // set - exists - expire
        Thread.sleep(1500);// 测试ttl被覆盖
        booleanVal = redisOps.set(key, 3, true, 10L);
        Assert.assertTrue(booleanVal);
        longVal = redisOps.ttl(key);
        Assert.assertTrue(longVal > 9);

        // multi by null
        String str = redisOps.get("abc");
        List<String> strs = redisOps.gets(key + "1", key + "2", key + "3");
        Assert.assertEquals(3, strs.size());
        System.out.println("multi ==> " + strs);
    }

    @Test
    public void hmGet() {
        String key = KEY_PREFIX + "hash";
        redisOps.hmSet(key, 1, 1);

        List<Integer> list = redisOps.hmGetMulti(key, Integer.class, "1", "2");
        Assert.assertEquals(2, list.size());
        System.out.println("multi ==> " + list);


//        redisOps.hmSet("test.for.hmget", 1, "abc");
//        String val = redisOps.hmGet("test.for.hmget", 1, String.class);
//        Assert.assertEquals("abc", val);
    }

    @Test
    public void zset() {
        String key = KEY_PREFIX + "zset";
        String m1 = "a";
        String m2 = "b";
        String m3 = "c";

        // add
        redisOps.zAdd(key, m1, 1);
        redisOps.zAdd(key, m2, 2);
        redisOps.zAdd(key, m3, 3);

        // size
        long longVal = redisOps.zSize(key);
        Assert.assertEquals(3, longVal);

        // rangeByRank
        Set<String> setVal = redisOps.zRangeByRank(key, 0, 1, String.class);
        Assert.assertEquals(2, setVal.size());
        Assert.assertTrue(setVal.contains(m2));
        Assert.assertTrue(setVal.contains(m1));

        setVal = redisOps.zRangeByRank(key, -2, -1, String.class);
        Assert.assertEquals(2, setVal.size());
        Assert.assertTrue(setVal.contains(m3));
        Assert.assertTrue(setVal.contains(m2));

        longVal = redisOps.zRemove(key, m1, m2);
        Assert.assertEquals(2, longVal);
        setVal = redisOps.zRangeByRank(key, 0, -1, String.class);
        Assert.assertEquals(1, setVal.size());
        Assert.assertTrue(setVal.contains(m3));

    }
}