package io.github.chad2li.baseutil.redis;

import io.github.chad2li.baseutil.test.BaseSpringTest;
import org.junit.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;

public class RedisCustomCommand extends BaseSpringTest {
    @Resource
    private RedisOps redisOps;
    @Resource
    private LettuceConnectionFactory lcf;

    @Test
    public void customCommand() throws InvocationTargetException, IllegalAccessException {

    }

}
