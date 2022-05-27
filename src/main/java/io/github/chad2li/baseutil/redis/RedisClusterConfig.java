package io.github.chad2li.baseutil.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis配置
 * https://blog.csdn.net/qq_32447321/article/details/53143795
 */
@Slf4j
@PropertySource({"classpath:redis.properties"})
public class RedisClusterConfig {
    public static final String REDIS_TEMPLATE_BEAN_NAME = "baseRedisTemplateName";

    @Value("${redis.timeout.command}")
    private int commandTimeout;
    /**
     * 链接池中最大连接数,默认为8
     */
    @Value("${redis.lettuce.pool.max-active}")
    private int maxActive;
    /**
     * 链接池中最大空闲的连接数,默认为8
     */
    @Value("${redis.lettuce.pool.max-idle}")
    private int maxIdle;
    /**
     * 连接池中最少空闲的连接数,默认为0
     */
    @Value("${redis.lettuce.pool.min-idle}")
    private int minIdle;
    /**
     * 当连接池资源耗尽时，调用者最大阻塞的时间，超时将跑出异常。单位，毫秒数;默认为-1.表示永不超时
     */
    @Value("${redis.lettuce.pool.max-wait-millis}")
    private int maxWaitMillis;
    @Value("${redis.cluster.max-redirects}")
    private int maxRedirects;
    @Value("${redis.topology.trigger.timeout}")
    private int triggerTimeout;
    @Value("${redis.topology.periodic.refresh}")
    private int periodicRefresh;
    @Value("${redis.cluster.nodes}")
    private String nodes;
    @Value("${redis.cluster.auth}")
    private String auth;

    public GenericObjectPoolConfig<?> genericObjectPoolConfig() {
        GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(maxActive);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setMaxWaitMillis(maxWaitMillis);

        return config;
    }

    /**
     * lettuce连接器工厂
     *
     * @return
     */
    @Bean(destroyMethod = "destroy")
    public LettuceConnectionFactory lettuceConnectionFactory() {
        //开启 自适应集群拓扑刷新和周期拓扑刷新
        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                // 开启全部自适应刷新
                .enableAllAdaptiveRefreshTriggers() // 开启自适应刷新,自适应刷新不开启,Redis集群变更时将会导致连接异常
                // 自适应刷新超时时间(默认30秒)
                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(triggerTimeout)) //默认关闭,开启后时间为30秒
                // 开周期刷新，同时开启浪费性能
//                .enablePeriodicRefresh(Duration.ofSeconds(periodicRefresh))  // 默认关闭,开启后时间为60秒
                .build();
        ClientOptions clientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(genericObjectPoolConfig())
//                .readFrom(ReadFrom.MASTER_PREFERRED)
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(commandTimeout)) //默认RedisURI.DEFAULT_TIMEOUT 60
                .build();

        String[] nodesArr = nodes.split(",");
        Set<RedisNode> nodes = new HashSet<RedisNode>(nodesArr.length);
        String[] nodeArr = null;
        for (String node : nodesArr) {
            nodeArr = node.split(":");
            nodes.add(new RedisNode(nodeArr[0], Integer.valueOf(nodeArr[1])));
        }

        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.setClusterNodes(nodes);
        clusterConfiguration.setPassword(auth);
        clusterConfiguration.setMaxRedirects(maxRedirects);

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(clusterConfiguration, clientConfig);
        // lettuceConnectionFactory.setShareNativeConnection(false); //是否允许多个线程操作共用同一个缓存连接，默认true，false时每个操作都将开辟新的连接
        // lettuceConnectionFactory.resetConnection(); // 重置底层共享连接, 在接下来的访问时初始化
        return lettuceConnectionFactory;
    }

    //    @Bean(REDIS_TEMPLATE_BEAN_NAME)
    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {

        // 配置redisTemplate
        RedisTemplate<String, String> redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        RedisSerializer<?> stringSerializer = new StringRedisSerializer();

        redisTemplate.setKeySerializer(stringSerializer);// key序列化
        redisTemplate.setValueSerializer(stringSerializer);// value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);// Hash key序列化
        redisTemplate.setHashValueSerializer(stringSerializer);// Hash value序列化

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


}
