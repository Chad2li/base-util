package cn.lyjuan.base.redis.redisson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.redisson.config.ReadMode;
import org.redisson.config.TransportMode;

/**
 * 测试本地连接redisson
 * @author chad
 * @date 2021/9/8 15:44
 * @since
 */
public class RedissonOpsConnTest {
    private RedissonBaseConfig baseConfig = new RedissonBaseConfig();
    private RedissonReplicaConfig config = new RedissonReplicaConfig();

    @Before
    public void before() {
        config.setMaster("redis://redis.test.hehewang.com:7101");
        config.setSlavers(new String[]{"redis://redis.test.hehewang.com:7102"});
        config.setDatabase(0);
        config.setPassword("");

        config.setThreads(4);
        config.setNettyThreads(4);
        config.setTransportMode(TransportMode.NIO.name());
        config.setLockWatchdogTimeout(30 * 1000);
        config.setReadMode(ReadMode.MASTER_SLAVE.name());
        config.setSlaveConnectionMinimumIdleSize(8);
        config.setSlaveConnectionPoolSize(8);
        config.setMasterConnectionMinimumIdleSize(4);
        config.setMasterConnectionPoolSize(4);
        config.setIdleConnectionTime(4);
        config.setConnectTimeout(10 * 1000);
        config.setTimeout(3000);
        config.setRetryAttempts(3);
        config.setRetryInterval(1500);
        config.setClientName("SyncClientFromLocal");
    }

    @Test
    public void conn() {
        RedissonOps redissonOps = config.redissonOps(baseConfig.createObjectMapper());
        String key = "test:for:conn";
        redissonOps.set(key, 1);
        int val = redissonOps.get(key);
        Assert.assertEquals(1, val);
    }
}
