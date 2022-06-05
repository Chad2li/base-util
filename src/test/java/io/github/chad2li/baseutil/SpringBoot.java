package io.github.chad2li.baseutil;

import io.github.chad2li.baseutil.redis.RedisIncrbyOps;
import io.github.chad2li.baseutil.redis.RedisMultiGetOps;
import io.github.chad2li.baseutil.redis.RedisOps;
import io.github.chad2li.baseutil.redis.redisson.RedissonOps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootApplication(excludeName = {"*"})
@ImportAutoConfiguration(classes = {
        // RedisReplicaConfig.class
//        , RedissonReplicaConfig.class
})
@EnableConfigurationProperties
public class SpringBoot {

    @Bean
    @Resource
    public RedisOps redisOps(RedisTemplate<String, String> rt) {
        return new RedisOps(rt);
    }

    @Bean
    @Resource
    public RedisIncrbyOps redisIncrbyOps(
            @Autowired(required = false) RedisTemplate<String, String> redisTemplate
            , @Autowired(required = false) RedissonOps redissonOps) {
        return new RedisIncrbyOps(redisTemplate, redissonOps);
    }

    @Bean
    @Resource
    public RedisMultiGetOps redisMultiGetOps(RedisTemplate<String, String> redisTemplate) {
        return new RedisMultiGetOps(redisTemplate);
    }
}
