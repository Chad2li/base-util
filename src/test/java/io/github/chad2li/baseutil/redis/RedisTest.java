package io.github.chad2li.baseutil.redis;

import io.github.chad2li.baseutil.test.BaseSpringTest;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;

import javax.annotation.Resource;
import java.util.List;

public class RedisTest extends BaseSpringTest {
    @Resource
    private RedisOps redisOps;

    @Test
    public void test() {
        byte[] redisKey = "test.for.pipl".getBytes();

        String value = "4";
        redisOps.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection conn) throws DataAccessException {
                conn.get(redisKey);
                conn.set(redisKey, value.getBytes());
                List<Object> list = conn.closePipeline();
                System.out.println(new String((byte[]) list.get(0)));
                return null;
            }
        });
    }
}
