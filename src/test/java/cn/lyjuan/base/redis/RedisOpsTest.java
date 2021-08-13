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
import java.util.Map;
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
    public void setAndGet() {
        String key = "test:for:redisson";
        Map val = redisOps.get(key, Map.class);
        System.out.println("result ==> " + val);
    }

    @Test
    public void hmGet() {
        String key = KEY_PREFIX + "hash";
        rt.opsForHash().put(key, "1", "abc");


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
        Assert.assertTrue(setVal.contains(m1));
        Assert.assertTrue(setVal.contains(m2));

        longVal = redisOps.zRemove(key, m1, m2);
        Assert.assertEquals(2, longVal);
        setVal = redisOps.zRangeByRank(key, 0, -1, String.class);
        Assert.assertEquals(1, setVal.size());
        Assert.assertTrue(setVal.contains(m3));

    }
}