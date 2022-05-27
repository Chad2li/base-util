package io.github.chad2li.baseutil.redis;

import io.github.chad2li.baseutil.test.BaseSpringTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

public class RedisReplicaTest extends BaseSpringTest {
    @Resource
    private RedisOps redisOps;

    @Test
    public void getAndSet() {
        redisOps.set("name", "Lisi");
        String name = redisOps.get("name");
        Assert.assertEquals("Lisi", name);
    }
}
