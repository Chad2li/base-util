package io.github.chad2li.baseutil.redis.redisson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.redisson.config.ReadMode;
import org.redisson.config.ReplicatedServersConfig;
import org.redisson.config.TransportMode;

import java.util.Arrays;

/**
 * 测试本地连接redisson
 *
 * @author chad
 * @date 2021/9/8 15:44
 * @since
 */
public class RedissonOpsConnTest {
    private RedissonBaseConfig baseConfig = new RedissonBaseConfig();
    private RedissonReplicatedConfig config = new RedissonReplicatedConfig();
    private RedissonClient redissonClient;
    private RedissonOps redissonOps;
    private ObjectMapper objectMapper;

    @Before
    public void before() {
        config.setThreads(4);
        config.setNettyThreads(4);
        config.setTransportMode(TransportMode.NIO);
        config.setLockWatchdogTimeout(30 * 1000);


        ReplicatedServersConfig replicatedServersConfig = new ReplicatedServersConfig();
        replicatedServersConfig.setDatabase(0);
        replicatedServersConfig.setNodeAddresses(Arrays.asList(new String[]{"redis://localhost:6379"}));
        replicatedServersConfig.setReadMode(ReadMode.MASTER_SLAVE);
        replicatedServersConfig.setSlaveConnectionMinimumIdleSize(8);
        replicatedServersConfig.setSlaveConnectionPoolSize(8);
        replicatedServersConfig.setMasterConnectionMinimumIdleSize(4);
        replicatedServersConfig.setMasterConnectionPoolSize(4);
        replicatedServersConfig.setIdleConnectionTimeout(1500);
        replicatedServersConfig.setConnectTimeout(10 * 1000);
        replicatedServersConfig.setTimeout(3000);
        replicatedServersConfig.setRetryAttempts(3);
        replicatedServersConfig.setRetryInterval(1500);
        replicatedServersConfig.setClientName("SyncClientFromLocal");

        objectMapper = baseConfig.createObjectMapper();
        redissonClient = config.redissonClient(replicatedServersConfig, objectMapper);
    }

    @Test
    public void conn() {
        redissonOps = new RedissonOps(redissonClient);
        String key = "test:for:conn";
        redissonOps.set(key, 1);
        int val = redissonOps.get(key);
        Assert.assertEquals(1, val);
    }


    public static RedissonOps redissonOps() {
        RedissonOpsConnTest conn = new RedissonOpsConnTest();
        conn.before();
        conn.conn();

        return conn.redissonOps;
    }
}
