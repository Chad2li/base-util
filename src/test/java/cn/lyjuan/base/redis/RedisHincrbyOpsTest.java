package cn.lyjuan.base.redis;

import cn.lyjuan.base.redis.redisson.RedissonOps;
import cn.lyjuan.base.redis.redisson.RedissonOpsConnTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author chad
 * @date 2022/3/22 15:14
 * @since
 */
public class RedisHincrbyOpsTest {
    private RedissonOps redissonOps = RedissonOpsConnTest.redissonOps();

    private RedisHincrbyOps hincrbyOps;

    private final static String redisKey1 = "test:for:hincrby1";
    private final static String redisKey2 = "test:for:hincrby2";

    private final static String hashKey = "hashKey";

    @Before
    public void before() {
        hincrbyOps = new RedisHincrbyOps(redissonOps);
    }

    @Test
    public void hincrby() {
        RedisHincrbyOps.Result result = hincrbyOps.hincrby(hashKey, 2, 9L, 0L, null, redisKey2, redisKey1);
        System.out.println(result);
    }

    @Test
    public void hincrby_hash_succ() {
        // 1. success
        // 库存

        // 批量扣减库存
        RedisHincrbyOps.Result result = hincrbyOps.hincrby(hashKey, -1, null, 0L, RedisIncrbyOps.Exists.XX, redisKey1, redisKey2);
        Assert.assertTrue(result.isSucc());
        Assert.assertEquals(4L, result.getValues().get(0).longValue());
        Assert.assertEquals(0L, result.getValues().get(1).longValue());
    }

    @Test
    public void hincrby_fail() {
        // 批量扣减库存
        RedisHincrbyOps.Result result = hincrbyOps.hincrby(hashKey, -1, null, 0L, RedisIncrbyOps.Exists.XX, redisKey1, redisKey2);
        Assert.assertFalse(result.isSucc());
        Assert.assertEquals(5L, result.getValues().get(0).longValue());
        Assert.assertEquals(0L, result.getValues().get(1).longValue());
    }

    @Test
    public void hincrby_normal_succ() {
        // 5, 1
        // 批量扣减库存
        RedisHincrbyOps.Result result = hincrbyOps.hincrby(null, -1, null, 0L, RedisIncrbyOps.Exists.XX, redisKey1, redisKey2);
        Assert.assertTrue(result.isSucc());
        Assert.assertEquals(4L, result.getValues().get(0).longValue());
        Assert.assertEquals(0L, result.getValues().get(1).longValue());
    }

    @Test
    public void hincrby_normal_fail() {
        // 5, 1
        // 批量扣减库存
        RedisHincrbyOps.Result result = hincrbyOps.hincrby(null, -1, null, 0L, RedisIncrbyOps.Exists.XX, redisKey1, redisKey2);
        Assert.assertFalse(result.isSucc());
        Assert.assertEquals(4L, result.getValues().get(0).longValue());
        Assert.assertEquals(0L, result.getValues().get(1).longValue());
    }
}
