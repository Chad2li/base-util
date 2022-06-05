package io.github.chad2li.baseutil.redis.redisson;

import io.github.chad2li.baseutil.test.BaseSpringTest;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import javax.annotation.Resource;

/**
 * TODO-类描述
 *
 * @author chad
 * @since 1 create by chad at 2022/6/5 14:45
 */
@ImportAutoConfiguration(classes = {RedissonReplicatedConfig.class})
public class RedissonReplicatedConfigTest extends BaseSpringTest {

    @Resource
    private RedissonClient redisClient;

    @Test
    public void test() {
        Integer value = null;
        value = (Integer) redisClient.getBucket("test:for:conn").get();
        System.out.println("successful ==> " + value);
    }
}