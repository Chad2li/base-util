package cn.lyjuan.base.redis.redisson;

import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.mockito.internal.creation.bytebuddy.MockMethodInterceptor;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Redisson封装工具
 */
@Slf4j
public class RedissonOps {

    private RedissonClient client;

    @Autowired
    public RedissonOps(RedissonClient client) {
        this.client = client;
    }

    /**
     * 获取指定结果类型
     *
     * @param key 键值
     * @param <T>
     * @return
     */
    public <T> T get(final String key, Type type) {
        RBucket<T> bucket = client.getBucket(key);
        return bucket.get();
    }

    /**
     * @param key
     * @param expect
     * @param update
     * @param <T>
     * @return
     */
    public <T> boolean compareAndSet(final String key, T expect, T update) {
        RBucket<String> bucket = client.getBucket(key);
        String expectVal = JsonUtils.to(expect);
        String updateVal = JsonUtils.to(update);
        return bucket.compareAndSet(expectVal, updateVal);
    }

    /**
     * 获取通用操作桶
     *
     * @param key 键值
     * @param <T> 类型
     * @return
     */
    public <T> RBucket<T> getBucket(final String key) {
        return client.getBucket(key);
    }

    /**
     * 删除Redis键
     *
     * @param keys 键，支持数组
     */
    public void del(final String... keys) {
        if (StringUtils.isNullArray(keys))
            return;
        RKeys rkeys = client.getKeys();
        rkeys.delete(keys);
    }

    /**
     * 设置key的过期时间
     *
     * @param key           键值
     * @param expireSeconds 过期时间，秒
     */
    public void expire(final String key, int expireSeconds) {
        RKeys rkeys = client.getKeys();
        rkeys.expire(key, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * Key是否存在
     *
     * @param key 键值
     * @return true存在
     */
    public boolean exist(final String key) {
        return -2 != ttl(key);
    }

    /**
     * 返回元素的生命倒计时，毫秒
     *
     * @param key
     * @return time in milliseconds
     * -2 key不存在
     * -1 key存在，但未设置过期时间
     */
    public long ttl(final String key) {
        return client.getBucket(key).remainTimeToLive();
    }

    public <T> T hgetAll(final String key, Type type) {
//        client.getMapCache(key).getAll();
        return null;
    }
}
