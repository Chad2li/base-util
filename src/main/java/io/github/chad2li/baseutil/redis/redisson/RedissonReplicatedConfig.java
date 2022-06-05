package io.github.chad2li.baseutil.redis.redisson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chad2li.baseutil.redis.redisson.codec.CustomJsonJacksonCodec;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.BaseCodec;
import org.redisson.config.Config;
import org.redisson.config.ReplicatedServersConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * redisson 副本模式配置
 *
 * @author chad
 * @since 1 by chad at 2022/6/5
 */
@ConditionalOnResource(resources = {RedissonReplicatedConfig.REPLICATED_PROPERTY_FILE})
@ImportAutoConfiguration(classes = {RedissonBaseConfig.class})
@PropertySource(value = {
        RedissonBaseConfig.BASE_PROPERTY_FILE,
        RedissonReplicatedConfig.REPLICATED_PROPERTY_FILE
})
public class RedissonReplicatedConfig extends Config {
    /**
     * redis副本配置资源文件名
     */
    public static final String REPLICATED_PROPERTY_FILE = "classpath:redisson/replicated.properties";

    @Bean
    @ConfigurationProperties(prefix = RedissonBaseConfig.PROPERTY_PREFIX)
    public ReplicatedServersConfig replicatedServersConfig() {
        return new ReplicatedServersConfig();
    }

    @Bean
    public RedissonClient redissonClient(ReplicatedServersConfig replicatedServersConfig,
                                         ObjectMapper objectMapper) {
        BaseCodec codec = new CustomJsonJacksonCodec(objectMapper);

        this.setCodec(codec);
        this.setReplicatedServersConfig(replicatedServersConfig);

        return Redisson.create(this);
    }
}
