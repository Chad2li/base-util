package cn.lyjuan.base.redis;

import cn.lyjuan.base.util.JsonUtils;
import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 布隆过滤
 * 不支持删除操作，不支持精确操作
 *
 * @param
 */
@Data
@Service(RedisBloomOps.REDIS_BLOOM_OPS_NAME)
public class RedisBloomOps {
    public static final String REDIS_BLOOM_OPS_NAME = "appApiRedisBloomOps";
    /**
     * 缓存所有funnels
     */
    private static final Map<String, BloomFunnel> funnels = new HashMap<>();
    private static DefaultRedisScript<Long> addScript = new DefaultRedisScript<>();
    private static DefaultRedisScript<Long> existsScript = new DefaultRedisScript<>();

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisBloomOps(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    static {
        addScript.setResultType(Long.class);
        addScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/bloom/BloomAdd.lua")));

        existsScript.setResultType(Long.class);
        existsScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/bloom/BloomExists.lua")));
    }

    /**
     * 添加数据
     *
     * @param key
     * @param value
     * @param spanIds
     * @return true已存在
     */
    public boolean add(IRedisBloomKey key, String value, int... spanIds) {
        String fullKey = RedisUtil.key(key.key(), spanIds);
        BloomFunnel fun = create(fullKey, key.getExpectSize(), key.getFaultTolerant());

        return add(fullKey, value, fun);
    }

    /**
     * 添加数据
     *
     * @return true已存在
     */
    public boolean add(IRedisBloomKey key, String value, String... spanIds) {
        String fullKey = RedisUtil.key(key.key(), spanIds);
        BloomFunnel fun = create(fullKey, key.getExpectSize(), key.getFaultTolerant());

        return add(fullKey, value, fun);
    }

    /**
     * 添加数据
     *
     * @return true已存在
     */
    public boolean add(String key, String value, BloomFunnel fun) {
        if (null == fun)
            throw new IllegalStateException("must create funnel before");

        long[] offset = hashOffset(value, fun);
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(key);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("offset", offset);

        long count = redisTemplate.execute(addScript, keyList, JsonUtils.to(argvMap));

        return count > 0;
    }

    /**
     * 判断数据是否存在
     *
     * @param key
     * @param value
     * @param spanIds
     * @return
     */
    public boolean exists(IRedisBloomKey key, String value, int... spanIds) {
        String fullKey = RedisUtil.key(key.key(), spanIds);
        BloomFunnel fun = create(fullKey, key.getExpectSize(), key.getFaultTolerant());

        return exists(fullKey, value, fun);
    }

    /**
     * 判断数据是否存在
     *
     * @return
     */
    public boolean exists(IRedisBloomKey key, String value, String... spanIds) {
        String fullKey = RedisUtil.key(key.key(), spanIds);
        BloomFunnel fun = create(fullKey, key.getExpectSize(), key.getFaultTolerant());

        return exists(fullKey, value, fun);
    }

    /**
     * 判断数据是否存在
     *
     * @return
     */
    public boolean exists(String key, String value, BloomFunnel fun) {

        if (null == fun)
            throw new IllegalStateException("must create funnel before");

        long[] offset = hashOffset(value, fun);
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(key);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("offset", offset);

        Long result = redisTemplate.execute(existsScript, keyList, JsonUtils.to(argvMap));

        return result > 0;
    }


    /**
     * 创建用于计算的hash funnel
     *
     * @param key
     * @param expectedInsertions 期望存放的数据大小
     * @param fpp                容错率
     * @return
     */
    private BloomFunnel create(String key, long expectedInsertions, double fpp) {
        BloomFunnel fun = funnels.get(key);
        if (null != fun)
            return fun;

        synchronized (key) {
            fun = funnels.get(key);
            if (null != fun) return fun;

            fun = new BloomFunnel(expectedInsertions, fpp);
            funnels.put(key, fun);
        }

        return fun;
    }

    /**
     * 计算bit数组的长度，
     * m = -n * Math.log(p)/Math.pow(ln2,2)
     *
     * @param n 插入条数
     * @param p 误判概率
     */
    private long numOfBits(long n, double p) {
        if (p == 0)
            p = Double.MIN_VALUE;
        long sizeOfBitArray = (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
        return sizeOfBitArray;
    }

    /**
     * 计算hash方法执行次数
     * k = m/n*ln2
     *
     * @param n 插入的数据条数
     * @param m 数据位数
     */
    private int numberOfHashFunctions(long n, long m) {
        int countOfHash = Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
        return countOfHash;
    }

    /**
     * 使用 fun 方法 计算 value 在hash中的多个位置
     *
     * @param value
     * @param fun
     * @return
     */
    private long[] hashOffset(String value, BloomFunnel fun) {
        long[] offset = new long[fun.numHashFunctions];

        long hash64 = Hashing.murmur3_128().hashObject(value, fun.funnel).asLong();
        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);
        for (int i = 1; i <= fun.numHashFunctions; i++) {
            int nextHash = hash1 + i * hash2;
            if (nextHash < 0) {
                nextHash = ~nextHash;
            }
            offset[i - 1] = nextHash % fun.bitSize;
        }

        return offset;
    }

    /**
     * google hash值计算器
     */
    public class BloomFunnel {
        /**
         * 一次处理需要计算的数量
         */
        private int numHashFunctions;
        /**
         * bit大小
         */
        private long bitSize;

        private Funnel<CharSequence> funnel;

        public BloomFunnel(long expectedInsertions, double fpp) {
            this.funnel = Funnels.stringFunnel(Charsets.UTF_8);
            bitSize = numOfBits(expectedInsertions, fpp);
            numHashFunctions = numberOfHashFunctions(expectedInsertions, bitSize);
        }
    }
}
