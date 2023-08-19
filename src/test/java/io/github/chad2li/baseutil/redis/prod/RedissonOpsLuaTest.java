package io.github.chad2li.baseutil.redis.prod;

import io.github.chad2li.baseutil.redis.redisson.RedissonOps;
import io.github.chad2li.baseutil.util.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * redissonOps执行 lua 脚本
 * @author chad
 * @since 1 create by chad at 2022/6/11 07:26
 */
public class RedissonOpsLuaTest {
    private RedissonOps redissonOps;

    @Before
    public void before() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setDatabase(0)
        ;
        RedissonClient client = Redisson.create(config);
        redissonOps = new RedissonOps(client);
    }

    @Test
    public void script() {
        String redisKey = "test:for:redissonOps.lua";
        String hashKey = "1";
        UserDemo user = new UserDemo(1L, "ZhangSan", 18, "zhangsan@mail.com");
        String luaScript = "local lent = redis.call('hlen', KEYS[1]);lent = lent + 1;redis.call('hset', KEYS[1], ARGV[1], ARGV[2]);return lent;";
//        String luaScript = "local lent = redis.call('hlen', KEYS[1]);lent = lent + 1;return lent";
        String value = JsonUtils.to(user);
        redissonOps.getScript().eval(redisKey, RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER, Arrays.asList(new String[]{redisKey}), hashKey, value);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class UserDemo {
        private Long id;
        private String name;
        private int age;
        private String email;

    }
}
