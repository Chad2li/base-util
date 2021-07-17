package cn.lyjuan.base.redis.redisson;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.HashSet;
import java.util.Set;

@Data
@ImportAutoConfiguration(classes = {RedissonBaseConfig.class})
@PropertySource({"classpath:redisson-replica.properties"})
public class RedissonReplicaConfig {
    public static final String NAME = "baseRedissonReplicaConfig";

    @Value("${redisson.master}")
    private String master;
    @Value("${redisson.slaver}")
    private String[] slavers;
    @Value("${redisson.database}")
    private int database;
    /**
     * 用于节点身份验证的密码
     */
    @Value("${redisson.password}")
    private String password;

    /**
     * 默认值:当前处理核数量 *2
     */
    @Value("${redisson.threads}")
    private int threads;
    /**
     * 这个线程池数量是在一个Redisson实例内，被其创建的所有分布式数据类型和服务，以及底层客户端所一同共享的线程池里保存的线程数量。
     * 默认值:当前处理核数量 *2
     */
    @Value("${redisson.netty_threads}")
    private int nettyThreads;
    /**
     * 传输模式<br/>
     * 默认值：TransportMode.NIO<br/>
     * 可选参数：<br/>
     * TransportMode.NIO<br/>
     * TransportMode.EPOLL -需要依赖里有netty-transport-native-epoll包（Linux）TransportMode.KQUEUE -
     * 需要依赖里有 netty-transport-native-kqueue包（macOS）
     */
    @Value("${redisson.transport_mode}")
    private String transportMode;
    /**
     * 监控锁的看门狗超时时间单位为毫秒。该参数只适用于分布式锁的加锁请求中未明确使用leaseTimeout参数的情况。
     * 如果该看门口未使用lockWatchdogTimeout去重新调整一个分布式锁的lockWatchdogTimeout超时，那么这个锁将变为失效状态。
     * 这个参数可以用来避免由Redisson客户端节点宕机或其他原因造成死锁的情况。<br/>
     * 默认值：30000
     */
    @Value("${redisson.lock_watchdog_timeout}")
    private long lockWatchdogTimeout;
    /**
     * 设置读取操作选择节点的模式。可用值为：SLAVE -只在从服务节点里读取。MASTER -只在主服务节点里读取。MASTER_SLAVE -在主从服务节点里都可以读取。
     */
    @Value("${redisson.read-mode}")
    private String readMode;
    /**
     * 多从节点的环境里，每个 从服务节点里用于普通操作（非 发布和订阅）的最小保持连接数（长连接）。
     * 长期保持一定数量的连接有利于提高瞬时读取反映速度。<br/>
     * 默认值：32
     */
    @Value("${redisson.slave-connection-minimum-idle-size}")
    private int slaveConnectionMinimumIdleSize;
    /**
     * 多从节点的环境里，每个 从服务节点里用于普通操作（非 发布和订阅）连接的连接池最大容量。连接池的连接数量自动弹性伸缩。
     * 默认值：32
     */
    @Value("${redisson.slave-connection-pool-size}")
    private int slaveConnectionPoolSize;
    /**
     * 多从节点的环境里，每个 主节点的最小保持连接数（长连接）。长期保持一定数量的连接有利于提高瞬时写入反应速度。<br/>
     * 主节点的连接池最大容量。连接池的连接数量自动弹性伸缩。<br/>
     * 默认值：64
     */
    @Value("${redisson.master-connection-minimum-idle-size}")
    private int masterConnectionMinimumIdleSize;
    /**
     * # 主节点的连接池最大容量。连接池的连接数量自动弹性伸缩。<br/>
     * # 默认值：64
     */
    @Value("${redisson.master-connection-pool-size}")
    private int masterConnectionPoolSize;
    /**
     * 如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。
     * 时间单位是毫秒。<br/>
     * 默认值：10000
     */
    @Value("${redisson.idle-connection-timeout}")
    private int idleConnectionTime;
    /**
     * 同任何节点建立连接时的等待超时。时间单位是毫秒。<br/>
     * 默认值：10000
     */
    @Value("${redisson.connect-timeout}")
    private int connectTimeout;
    /**
     * 等待节点回复命令的时间。该时间从命令发送成功时开始计时，单位：毫秒<br/>
     * 默认值：3000
     */
    @Value("${redisson.timeout}")
    private int timeout;
    /**
     * 如果尝试达到 retryAttempts（命令失败重试次数）仍然不能将命令发送至某个指定的节点时，将抛出错误。如果尝试在此限制之内发送成功，
     * 则开始启用 timeout（命令等待超时）计时。<br/>
     * 默认值：3
     */
    @Value("${redisson.retry-attempts}")
    private int retryAttempts;
    /**
     * 在某个节点执行相同或不同命令时，连续 失败failedAttempts（执行失败最大次数）时，该节点将被从可用节点列表里清除，
     * 直到 reconnectionTimeout（重新连接时间间隔）超时以后再次尝试。<br/>
     * 默认值：1500
     */
    @Value("${redisson.retry-interval}")
    private int retryInterval;
    /**
     * 该连接在redis控制台显示的客户端名称
     */
    @Value("${redisson.client-name}")
    private String clientName;

    @Bean
    public RedissonOps redissonOps(@Autowired ObjectMapper objectMapper) {
        RedissonClient client = redissonClient(objectMapper);
        return new RedissonOps(client, objectMapper);
    }

    public RedissonClient redissonClient(ObjectMapper objectMapper) {
        JsonJacksonCodec codec = new JsonJacksonCodec(objectMapper);

        Set<String> slaverSet = new HashSet<>(this.slavers.length);
        for (String s : this.slavers)
            slaverSet.add(s);

        Config config = new Config();
        config.setCodec(codec)
                .setThreads(this.threads)
                .setNettyThreads(this.nettyThreads)
                .setTransportMode(TransportMode.valueOf(this.transportMode))
                .setLockWatchdogTimeout(this.lockWatchdogTimeout)
                // master slaver
                .useMasterSlaveServers()
                .setDatabase(this.database)
                .setMasterAddress(this.master)
                .setPassword(this.password)
                // other
                .setReadMode(ReadMode.valueOf(this.readMode))
                .setSlaveConnectionMinimumIdleSize(this.slaveConnectionMinimumIdleSize)
                .setSlaveConnectionPoolSize(this.slaveConnectionPoolSize)
                .setMasterConnectionMinimumIdleSize(this.masterConnectionMinimumIdleSize)
                .setMasterConnectionPoolSize(this.masterConnectionPoolSize)
                .setIdleConnectionTimeout(this.idleConnectionTime)
                .setConnectTimeout(this.connectTimeout)
                .setTimeout(this.timeout)
                .setRetryAttempts(this.retryAttempts)
                .setRetryInterval(this.retryInterval)
                .setClientName(this.clientName)
                // all slaver
                .setSlaveAddresses(slaverSet)
        //
        ;

        RedissonClient client = Redisson.create(config);
        return client;
    }
}
