package io.github.chad2li.baseutil.redis.redisson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chad2li.baseutil.redis.redisson.codec.CustomJsonJacksonCodec;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.BaseCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * Redisson 集群配置
 *
 * @author chad
 * @since 1 create by chad at 2022/5/27 12:55
 */
//@ConditionalOnResource(resources = {RedissonClusterConfig.CLUSTER_PROPERTY_FILE})
@ImportAutoConfiguration(classes = {RedissonBaseConfig.class})
@PropertySource(value = {
        RedissonBaseConfig.BASE_PROPERTY_FILE,
        RedissonClusterConfig.CLUSTER_PROPERTY_FILE
})
public class RedissonClusterConfig extends Config {
    /**
     * 资源文件名
     */
    public static final String CLUSTER_PROPERTY_FILE = "classpath:/redisson/cluster.properties";

    @Bean
    @ConfigurationProperties(prefix = RedissonBaseConfig.PROPERTY_PREFIX)
    public ClusterServersConfig clusterServersConfig() {
        return new ClusterServersConfig();
    }

    @Bean
    public RedissonClient redissonClient(ClusterServersConfig clusterServersConfig,
                                         ObjectMapper objectMapper) {
        BaseCodec codec = new CustomJsonJacksonCodec(objectMapper);

        this.setCodec(codec);
        this.setClusterServersConfig(clusterServersConfig);

        return Redisson.create(this);
    }
}
