package cn.lyjuan.base.redis;

import cn.lyjuan.base.test.BaseSpringTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

public class RedisIncrbyOpsTest extends BaseSpringTest {

    @Resource
    private RedisIncrbyOps redisIncrbyOps;
    @Resource
    private RedisOps redisOps;

    private static final String redisKey = "test:for:incrby";
    private static final int hashKey = 1;

    @Before
    public void before() {
        redisOps.del(redisKey);
    }

    @After
    public void after() {
        redisOps.del(redisKey);
    }

    @Test
    public void incrby() {
        // 强制存在，不存在则失败
        RedisIncrbyOps.Result result = redisIncrbyOps.incrby(redisKey, null, null, null, null, RedisIncrbyOps.Exists.XX);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isXXFail());

        result = redisIncrbyOps.incrby(redisKey, null, null, null, null, null);
        Assert.assertTrue(result.isSucc());
        Assert.assertEquals(1L, result.value());

        // 强制不存在，存在则失败
        result = redisIncrbyOps.incrby(redisKey, null, 1, null, null, RedisIncrbyOps.Exists.NX);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isNXFail());

        // 上限
        redisOps.del(redisKey);
        result = redisIncrbyOps.incrby(redisKey, null, 11, 10L, null, null);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isMaxFail());
        redisOps.del(redisKey);
        result = redisIncrbyOps.incrby(redisKey, null, 10, null, null, null);
        Assert.assertTrue(result.isSucc());
        result = redisIncrbyOps.incrby(redisKey, null, null, 10L, null, null);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isMaxFail());

        // 下限
        redisOps.del(redisKey);
        result = redisIncrbyOps.incrby(redisKey, null, -1, null, 0L, RedisIncrbyOps.Exists.NX);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isMinFail());
    }

    @Test
    public void hashIncrby() {
        // 强制存在，不存在则失败
        RedisIncrbyOps.Result result = redisIncrbyOps.incrby(redisKey, hashKey, null, null, null, RedisIncrbyOps.Exists.XX);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isXXFail());

        result = redisIncrbyOps.incrby(redisKey, hashKey, null, null, null, null);
        Assert.assertTrue(result.isSucc());
        Assert.assertEquals(1L, result.value());

        // 强制不存在，存在则失败
        result = redisIncrbyOps.incrby(redisKey, hashKey, 1, null, null, RedisIncrbyOps.Exists.NX);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isNXFail());

        // 上限
        redisOps.del(redisKey);
        result = redisIncrbyOps.incrby(redisKey, hashKey, 11, 10L, null, null);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isMaxFail());
        redisOps.del(redisKey);
        result = redisIncrbyOps.incrby(redisKey, hashKey, 10, null, null, null);
        Assert.assertTrue(result.isSucc());
        result = redisIncrbyOps.incrby(redisKey, hashKey, null, 10L, null, null);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isMaxFail());

        // 下限
        redisOps.del(redisKey);
        result = redisIncrbyOps.incrby(redisKey, hashKey, -1, null, 0L, RedisIncrbyOps.Exists.NX);
        Assert.assertFalse(result.isSucc());
        Assert.assertTrue(result.isMinFail());
    }
}





















