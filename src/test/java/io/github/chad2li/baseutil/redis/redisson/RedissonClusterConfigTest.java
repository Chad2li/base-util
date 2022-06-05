package io.github.chad2li.baseutil.redis.redisson;

import io.github.chad2li.baseutil.test.BaseSpringTest;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

/**
 * redisson集群配置
 *
 * @author chad
 * @since 1 create by chad at 2022/6/5 13:22
 */
@ImportAutoConfiguration(value = {RedissonClusterConfig.class})
public class RedissonClusterConfigTest extends BaseSpringTest {

//    @Resource
//    private RedissonClient client;

    @Test
    public void test() {
        String value = null;
//        value = (String) client.getBucket("test:for:conn").get();
        System.out.println("successful ==> " + value);
    }
}