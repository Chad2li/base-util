package io.github.chad2li.baseutil.redis.redisson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chad2li.baseutil.redis.redisson.codec.CustomJsonJacksonCodec;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.BaseCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * redisson 单机模式配置
 *
 * @author chad
 * @since 1 by chad at 2022/6/5
 */
@ConditionalOnResource(resources = {RedissonSingleConfig.PROPERTY_FILE})
@ImportAutoConfiguration(classes = {RedissonBaseConfig.class})
@PropertySource(value = {
        RedissonBaseConfig.BASE_PROPERTY_FILE,
        RedissonSingleConfig.PROPERTY_FILE
})
public class RedissonSingleConfig extends Config {
    /**
     * redis副本配置资源文件名
     */
    public static final String PROPERTY_FILE =
            "classpath:redisson/single-${spring.profiles.active}.properties";

    @Bean
    @ConfigurationProperties(prefix = RedissonBaseConfig.PROPERTY_PREFIX)
    public SingleServerConfig singleServerConfig() {
        return this.useSingleServer();
    }

    @Bean
    public RedissonClient redissonClient(SingleServerConfig singleServerConfig,
                                         ObjectMapper objectMapper) {
        BaseCodec codec = new CustomJsonJacksonCodec(objectMapper);

        this.setCodec(codec);

        return Redisson.create(this);
    }

    @Bean
    public RedissonOps redissonOps(RedissonClient redissonClient) {
        return new RedissonOps(redissonClient);
    }
}
