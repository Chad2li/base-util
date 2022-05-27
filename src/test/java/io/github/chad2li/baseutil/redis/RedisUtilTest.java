package io.github.chad2li.baseutil.redis;


import org.junit.Assert;
import org.junit.Test;

public class RedisUtilTest {
    @Test
    public void key() {
        String key = RedisUtil.key(TestRedisKey.TEST, 1, 2);
        Assert.assertEquals("test:for:key:1:2", key);
        key = RedisUtil.key(TestRedisKey.TEST, 1);
        Assert.assertEquals("test:for:key:1:1", key);
    }

    public enum TestRedisKey implements IRedisKey {
        TEST("test:for:key:" + IRedisKey.ID_SPAN + ":" + IRedisKey.ID_SPAN + "");
        private String key;

        TestRedisKey(String key) {
            this.key = key;
        }

        @Override
        public String key() {
            return this.key;
        }
    }

}