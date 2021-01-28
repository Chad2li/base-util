package cn.lyjuan.base;

import cn.lyjuan.base.redis.RedisClusterConfig;
import cn.lyjuan.base.redis.RedisIncrbyOps;
import cn.lyjuan.base.redis.RedisOps;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootApplication
@ImportAutoConfiguration(classes = {RedisClusterConfig.class})
@EnableConfigurationProperties
public class SpringBoot {
    @Resource
    private RedisClusterConfig redisClusterConfig;

    @Bean
    @Resource
    public RedisOps redisOps(@Qualifier(RedisClusterConfig.REDIS_TEMPLATE_BEAN_NAME) RedisTemplate<String, String> rt) {
        return new RedisOps(rt);
    }

    @Bean
    @Resource
    public RedisIncrbyOps redisIncrbyOps(@Qualifier(RedisClusterConfig.REDIS_TEMPLATE_BEAN_NAME) RedisTemplate<String, String> rt) {
        return new RedisIncrbyOps(rt);
    }
}
