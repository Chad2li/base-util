package cn.lyjuan.base.redis;

import io.lettuce.core.ReadFrom;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

/**
 * Redis读写分离
 */
@PropertySource({"classpath:redis-replica.properties"})
public class RedisReplicaConfig {
    /**
     * 连接超时：秒
     */
    @Value("${redis.timeout.command}")
    private int commandTimeout;
    /**
     * 连接主机
     */
    @Value("${redis.hostname}")
    private String hostname;
    /**
     * 连接端口
     */
    @Value("${redis.port}")
    private int port;
    /**
     * 连接的数据库
     */
    @Value("${redis.database}")
    private int database;
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
    @Value("${redis.auth}")
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
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(genericObjectPoolConfig())
                .readFrom(ReadFrom.REPLICA_PREFERRED)
//                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(commandTimeout)) //默认RedisURI.DEFAULT_TIMEOUT 60
                .build();


        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(hostname, port);
        serverConfig.setPassword(auth);
        serverConfig.setDatabase(database);
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
}
