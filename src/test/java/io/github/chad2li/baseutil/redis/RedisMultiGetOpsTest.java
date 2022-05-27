package io.github.chad2li.baseutil.redis;

import io.github.chad2li.baseutil.redis.redis.RedisOpsConnTest;
import io.github.chad2li.baseutil.util.DateUtils;
import io.github.chad2li.baseutil.util.RandomUtils;
import io.github.chad2li.baseutil.util.ReflectUtilsTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisMultiGetOpsTest extends RedisOpsConnTest {
    @Resource
    private RedisMultiGetOps redisMultiGetOps;

    private String redisKey = "{test.for.multi.get}";

    private String redisHashKey = "test.for.multi.get.hash";

    @Before
    @Override
    public void before(){
        super.before();
        redisMultiGetOps = new RedisMultiGetOps(rt);
    }


    /**
     * lua脚本批量获取值
     */
    @Test
    public void getMultiKey() {
        int all = 100 * 10000;
        int single = 100;
        int count = all / single;

        Map<Integer, Integer> records = new HashMap<>();
        LocalDateTime start = LocalDateTime.now();
        boolean isFirst = true;
        for (int k = 0; k < count; k++) {
            List<Integer> keys = new ArrayList<>(single);
            for (int i = 0; i < single; i++) {
                keys.add(k + i);
            }

            LocalDateTime singleStart = LocalDateTime.now();
            List values = redisMultiGetOps.getMultiKey(redisKey, ":", keys.toArray());
            Assert.assertEquals(single, values.size());

            long dura = DateUtils.duration(singleStart, LocalDateTime.now());
            if (isFirst) {
                isFirst = false;
            } else {
                records.put(k, (int) dura);
            }

            System.out.println("[" + k + "] multi in " + dura);
        }

        int avg = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;

        for (int i = 1; i < count; i++) {// 第一次未放入
            int dura = records.get(i);
            max = Math.max(max, dura);
            min = Math.min(min, dura);
            avg += dura;
        }
        avg = avg / count;

        System.out.println(String.format("max:[%s] min:[%s] avg:[%s]", max, min, avg));
        System.out.println("all in " + DateUtils.duration(start, LocalDateTime.now()));

        /**
         * 测试报告
         * max:[63] min:[3] avg:[6]
         * all in 68270
         */
    }

    /**
     * 管道执行批量获取值
     */
    @Test
    public void getMultiKeyByPipe() {
        int all = 100 * 10000;
        int single = 100;
        int count = all / single;

        Map<Integer, Integer> records = new HashMap<>();
        LocalDateTime start = LocalDateTime.now();
        boolean isFirst = true;
        for (int k = 0; k < count; k++) {
            final int tmp = k;
            LocalDateTime singleStart = LocalDateTime.now();
            List values = new ArrayList(single);
            redisOps.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    for (int i = 0; i < single; i++) {
                        byte[] keys = (redisKey + ":" + (tmp + i)).getBytes();
                        Object v = connection.get(keys);
                        values.add(v);
                    }

                    Assert.assertEquals(single, values.size());
                    return null;
                }
            });

            long dura = DateUtils.duration(singleStart, LocalDateTime.now());
            if (isFirst) {
                isFirst = false;
            } else {
                records.put(k, (int) dura);
            }

            System.out.println("[" + k + "] multi in " + dura);
        }

        int avg = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;

        for (int i = 1; i < count; i++) {// 第一次未放入
            int dura = records.get(i);
            max = Math.max(max, dura);
            min = Math.min(min, dura);
            avg += dura;
        }
        avg = avg / count;

        System.out.println(String.format("max:[%s] min:[%s] avg:[%s]", max, min, avg));
        System.out.println("all in " + DateUtils.duration(start, LocalDateTime.now()));
        /**
         * 测试报告
         * max:[469] min:[6] avg:[14]
         * all in 149025
         */
    }

    /**
     * 每次发请求批量获取
     */
    @Test
    public void getMultiKeyOld() {
        int all = 1 * 10000;
        int single = 100;
        int count = all / single;

        Map<Integer, Integer> records = new HashMap<>();
        LocalDateTime start = LocalDateTime.now();
        boolean isFirst = true;
        for (int k = 0; k < count; k++) {
            final int tmp = k;
            LocalDateTime singleStart = LocalDateTime.now();
            List values = new ArrayList(single);
            for (int i = 0; i < single; i++) {
                Object v = redisOps.get(redisKey + ":" + (k + i));
                values.add(v);
            }

            long dura = DateUtils.duration(singleStart, LocalDateTime.now());
            if (isFirst) {
                isFirst = false;
            } else {
                records.put(k, (int) dura);
            }

            System.out.println("[" + k + "] multi in " + dura);
        }

        int avg = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;

        for (int i = 1; i < count; i++) {// 第一次未放入
            int dura = records.get(i);
            max = Math.max(max, dura);
            min = Math.min(min, dura);
            avg += dura;
        }
        avg = avg / count;

        System.out.println(String.format("max:[%s] min:[%s] avg:[%s]", max, min, avg));
        System.out.println("all in " + DateUtils.duration(start, LocalDateTime.now()));
        /**
         * 测试报告
         * max:[554] min:[333] avg:[409]
         * all in 42222
         */
    }

    /**
     * 生成数据
     */
    @Test
    public void genSdsDate() {
        int i = 10000;

        LocalDateTime start = LocalDateTime.now();

        redisOps.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                LocalDateTime single = LocalDateTime.now();
                for (int k = 0; k < i; k++) {
                    String redis = redisKey + ":" + k;
                    byte[] key = redis.getBytes();
                    connection.set(key, RandomUtils.uuid().getBytes());
                    if (k % 100 == 0) {
                        System.out.println("[" + k + "] set " + redis + " " + DateUtils.duration(single, LocalDateTime.now()));
                        single = LocalDateTime.now();
                    }
                }
                return null;
            }
        });

        System.out.println("Done in " + (DateUtils.duration(start, LocalDateTime.now()) / 1000));
    }

    /**
     * 生成hash数据
     */
    @Test
    public void genHashData() {
        int i = 100 * 10000;
        LocalDateTime start = LocalDateTime.now();
        byte[] redisKeys = redisHashKey.getBytes();
        redisOps.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                LocalDateTime single = LocalDateTime.now();
                for (int k = 0; k < i; k++) {
                    connection.hSet(redisKeys, String.valueOf(k).getBytes(), RandomUtils.uuid().getBytes());
                    if (k % 100 == 0) {
                        System.out.println("[" + k + "] set " + k + " " + DateUtils.duration(single, LocalDateTime.now()));
                        single = LocalDateTime.now();
                    }
                }
                return null;
            }
        });
        System.out.println("Done in " + (DateUtils.duration(start, LocalDateTime.now()) / 1000));
    }

    /**
     * lua脚本批量获取hash值
     */
    @Test
    public void getMultKeyWithHash() {
        int all = 100 * 10000;
        int single = 100;
        int count = all / single;

        Map<Integer, Integer> records = new HashMap<>();
        LocalDateTime start = LocalDateTime.now();
        boolean isFirst = true;
        for (int k = 0; k < count; k++) {
            List<Integer> keys = new ArrayList<>(single);
            for (int i = 0; i < single; i++) {
                keys.add(k + i);
            }

            LocalDateTime singleStart = LocalDateTime.now();
            List values = redisMultiGetOps.getHashMultiKey(redisKey, keys.toArray());
            Assert.assertEquals(single, values.size());

            long dura = DateUtils.duration(singleStart, LocalDateTime.now());
            if (isFirst) {
                isFirst = false;
            } else {
                records.put(k, (int) dura);
            }

            System.out.println("[" + k + "] multi in " + dura);
        }

        int avg = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;

        for (int i = 1; i < count; i++) {// 第一次未放入
            int dura = records.get(i);
            max = Math.max(max, dura);
            min = Math.min(min, dura);
            avg += dura;
        }
        avg = avg / count;

        System.out.println(String.format("max:[%s] min:[%s] avg:[%s]", max, min, avg));
        System.out.println("all in " + DateUtils.duration(start, LocalDateTime.now()));

        /**
         * 测试报告
         * max:[89] min:[2] avg:[5]
         * all in 56527
         */
    }

    @Test
    public void getByType() {
        ReflectUtilsTest.User user = new ReflectUtilsTest.User();
        user.setAge(18);
        user.setHeight(175);
        user.setName("Zhangsan");

        // sds
        String sdsKey = "{sds:test:for:get.multi.type:1}";
        redisOps.set(sdsKey, user);
        List<ReflectUtilsTest.User> list = redisMultiGetOps.getMultiKey(ReflectUtilsTest.User.class, sdsKey);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(user, list.get(0));

        // hash
        String hashKey = "hash:test:for:get.multi.type";
        redisOps.hmSet(hashKey, 1, user);
        list = redisMultiGetOps.getHashMultiKey(hashKey, ReflectUtilsTest.User.class, 1);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(user, list.get(0));

        // not exists
        redisOps.del(sdsKey);
        list = redisMultiGetOps.getMultiKey(ReflectUtilsTest.User.class, sdsKey);
        Assert.assertEquals(1, list.size());
    }
}