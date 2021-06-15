package cn.lyjuan.base.redis;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisOpsTest {

    private static final String pwd = "XXWck7QQQghPbittPNQErNyzxtOhcikVP0KifN3VsKjw8oht4gxN6RgSh3FGbVsPOskBF9AVQMXmjtCIDCrkUx8h10ifWSBcecd";

    private RedisOps redisOps;

    private RedisTemplate<String, String> rt;

    @Before
    public void before() {
        rt = new StringRedisTemplate();


        RedisReplicaConfig config = new RedisReplicaConfig();
        config.setAuth(pwd);
        config.setCommandTimeout(10);
        config.setDatabase(1);
        config.setHostname("redis.test.hehewang.com");
        config.setPort(7101);
        config.setMaxActive(1);
        config.setMaxIdle(1);
        config.setMaxWaitMillis(3 * 1000);

        LettuceConnectionFactory lcf = config.lettuceConnectionFactory();
        rt.setConnectionFactory(lcf);


        RedisSerializer<?> stringSerializer = new StringRedisSerializer();

        rt.setKeySerializer(stringSerializer);// key序列化
        rt.setValueSerializer(stringSerializer);// value序列化
        rt.setHashKeySerializer(stringSerializer);// Hash key序列化
        rt.setHashValueSerializer(stringSerializer);// Hash value序列化

        rt.afterPropertiesSet();



        redisOps = new RedisOps(rt);


//        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
//        config.setHostName("redis.test.hehewang.com");
//        config.setPort(7001);
//        config.setPassword(pwd);
//        config.setDatabase(1);
//        RedisConnectionFactory rcf = new LettuceConnectionFactory(config);
//        rt.setConnectionFactory(lcf);
//        redisOps = new RedisOps(rt);
    }

    @Test
    public void hmGet() {
        rt.opsForHash().put("test.for.hmget", "1", "abc");


//        redisOps.hmSet("test.for.hmget", 1, "abc");
//        String val = redisOps.hmGet("test.for.hmget", 1, String.class);
//        Assert.assertEquals("abc", val);
    }
}