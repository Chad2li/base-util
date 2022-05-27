package io.github.chad2li.baseutil.redis.redis;

import io.github.chad2li.baseutil.redis.RedisOps;
import io.github.chad2li.baseutil.redis.RedisReplicaConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author chad
 * @date 2021/9/15 16:17
 * @since
 */
public class RedisOpsConnTest {

    protected RedisTemplate<String, String> rt;

    protected RedisOps redisOps;

    @Before
    public void before() {
        RedisReplicaConfig config = new RedisReplicaConfig();
        config.setCommandTimeout(10);
        config.setHostname("192.168.1.201");
        config.setPort(7101);
        config.setDatabase(0);
        config.setAuth("XXWck7QQQghPbittPNQErNyzxtOhcikVP0KifN3VsKjw8oht4gxN6RgSh3FGbVsPOskBF9AVQMXmjtCIDCrkUx8h10ifWSBcecd");
        config.setMaxActive(5);
        config.setMaxIdle(2);
        config.setMinIdle(1);
        config.setMaxWaitMillis(30 * 1000);

        // connect factory
        LettuceConnectionFactory factory = config.lettuceConnectionFactory();
        factory.afterPropertiesSet();

        // redis template
        rt = new StringRedisTemplate();
        rt.setConnectionFactory(factory);
        rt.setDefaultSerializer(new StringRedisSerializer());

        rt.afterPropertiesSet();

        redisOps = new RedisOps(rt);
    }

    @Test
    public void conn() {
        String key = "test:for;redisops:conn";
        redisOps.set(key, 1);
        int val = redisOps.get(key, Integer.class);

        Assert.assertEquals(1, val);
    }
}
