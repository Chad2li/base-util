package cn.lyjuan.base.redis;

import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * RedisUtils
 */
@Data
@Service(RedisOps.NAME)
public class RedisOps {
    public static final String NAME = "baseRedisOps";

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisOps(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static double size = Math.pow(2, 32);

    /**
     * ttl的key不存在
     */
    public static final long TTL_KEY_NOT_EXISTS = -2;
    /**
     * ttl查询的key未设置过期时间
     */
    public static final long TTL_KEY_NO_SET_EXPIRE = -1;

    /**
     * 获取key的剩余生存时间
     *
     * @param key
     * @return -2 key不存在; -1未设置生存时间；返回秒级剩余生存时间
     */
    public long ttl(final String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param offset 位 8Bit=1Byte
     * @return
     */
    public boolean setBit(String key, long offset, boolean isShow) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.setBit(key, offset, isShow);
        return true;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param offset
     * @return
     */
    public boolean getBit(String key, long offset) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        return operations.getBit(key, offset);
    }


    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public <T> void set(final String key, T value) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(key, JsonUtils.to(value));
    }

    /**
     * 删除键
     *
     * @param keys
     */
    public void del(final String... keys) {
        if (null == keys || keys.length < 1) return;
        if (1 == keys.length)
            redisTemplate.delete(keys[0]);
        else
            redisTemplate.delete(Arrays.asList(keys));
    }

    /**
     * 一次设置多个键值
     *
     * @param vals
     */
    public void mset(final Object... vals) {
        if (null == vals || vals.length < 1) return;
        Map<String, String> map = new HashMap<>(vals.length / 2);
        for (int i = 0; i < vals.length; i = i + 2) {
            map.put(StringUtils.toStr(vals[i]), JsonUtils.to(vals[i + 1]));
        }
        redisTemplate.opsForValue().multiSet(map);
    }

    /**
     * 一次设置多个值
     *
     * @param map
     */
    public void mset(final Map<String, Object> map) {
        if (null == map || map.isEmpty()) return;
        Map<String, String> newMap = new HashMap<>(map.size());
        map.forEach((key, val) ->
                newMap.put(key, JsonUtils.to(val))
        );

        redisTemplate.opsForValue().multiSet(newMap);
    }

    /**
     * 设置键值对
     *
     * @param key
     * @param value
     * @param nx            true存在才设置；false不存在才设置
     * @param expireSeconds 过期秒数
     * @param <T>
     * @return true设置成功
     */
    public <T> boolean set(final String key, final T value, boolean nx, Integer expireSeconds) {
        ValueOperations<String, String> oper = redisTemplate.opsForValue();
        Boolean result;
        if (nx)
            result = oper.setIfPresent(key, JsonUtils.to(value), expireSeconds, TimeUnit.SECONDS);
        else
            result = oper.setIfAbsent(key, JsonUtils.to(value), expireSeconds, TimeUnit.SECONDS);

        return result;
    }

    /**
     * 管道执行
     *
     * @param callback
     * @param <T>
     * @return
     */
    public <T> List<Object> executePipelined(RedisCallback<T> callback) {
        return redisTemplate.executePipelined(callback);
    }


    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param value
     * @param expireSeconds 多少秒后过期
     * @return
     */
    public <T> boolean set(final String key, T value, Integer expireSeconds) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(key, JsonUtils.to(value), expireSeconds, TimeUnit.SECONDS);
        return true;
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        if (null == keys || keys.length < 1)
            throw new RuntimeException("Redis keys cannot be empty");
        if (1 == keys.length)
            redisTemplate.delete(keys[0]);
        else
            redisTemplate.delete(Arrays.asList(keys));
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    public String get(final String key) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        return operations.get(key);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public <T> T get(final String key, Class<T> cls) {
        String json = get(key);
        if (StringUtils.isNull(json)) return null;
        return JsonUtils.from(cls, json);
    }

    /**
     * 设置key的过期时间
     *
     * @param key
     * @param expireSeconds
     */
    public void expire(final String key, int expireSeconds) {
        redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * hash值增加 incrby
     *
     * @param redisKey
     * @param hashKey
     * @param incrby   增加的数，可为负
     * @return
     */
    public Long hmIncrby(final String redisKey, final Object hashKey, Integer incrby) {
        return redisTemplate.opsForHash().increment(redisKey, JsonUtils.to(hashKey), incrby);
    }

    /**
     * hash值增加 1
     *
     * @param redisKey
     * @param hashKey
     * @return
     */
    public Long hmIncr(final String redisKey, final Object hashKey) {
        return redisTemplate.opsForHash().increment(redisKey, JsonUtils.to(hashKey), 1);
    }

    /**
     * 哈希 添加
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public void hmSet(String key, Object hashKey, Object value) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(key, JsonUtils.to(hashKey), JsonUtils.to(value));
    }

    /**
     * 删除hash中多个域值
     *
     * @param key
     * @param hashKey
     */
    public Long hmDel(String key, Object... hashKey) {
        return redisTemplate.opsForHash().delete(key, hashKey);
    }

    /**
     * 将map存入redis hash格式，并将value转为json格式
     *
     * @param key
     * @param map
     * @param <K>
     * @param <V>
     */
    public <K, V> void hsetMap(final String key, Map<K, V> map) {
        Map<String, String> strMap = RedisUtil.map2map(map);
        redisTemplate.opsForHash().putAll(key, strMap);
    }

    /**
     * 对象所有属性以json存入hash
     *
     * @param key
     * @param value
     */
    public <T> void hsetBean(final String key, T value) {
        Map<String, String> map = RedisUtil.bean2map(value);
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 获取所有属性并转为对象
     *
     * @param key
     * @param cls
     * @param <T>
     * @return
     */
    public <T> T hgetAll(final String key, Class<T> cls) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (null == map || map.isEmpty()) return null;

        String json = JsonUtils.to(map);

        return JsonUtils.from(cls, json);
    }

    /**
     * 哈希获取数据
     *
     * @param key
     * @param hashKey
     * @return
     */
    public <T> T hmGet(String key, Object hashKey, Class<T> cls) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        String json = hash.get(key, JsonUtils.to(hashKey));
        if (StringUtils.isNull(json)) return null;
        return JsonUtils.from(cls, json);
    }

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
    public <T> void lPush(String k, T v) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.rightPush(k, JsonUtils.to(v));
    }

    /**
     * 列表获取
     *
     * @param k
     * @param start
     * @param end
     * @return
     */
    public <T> List<T> lRange(String k, long start, long end, Class<T> cls) {
        ListOperations<String, String> listOper = redisTemplate.opsForList();
        List<String> list = listOper.range(k, start, end);
        // 防止空指针
        if (null == list || list.isEmpty()) return new ArrayList<>(0);
        List<T> targets = new ArrayList<>(list.size());
        list.forEach(item -> {
            targets.add(JsonUtils.from(cls, item));
        });

        return targets;
    }

    /**
     * 集合添加
     *
     * @param k
     * @param v
     */
    public <T> void add(String k, T v) {
        SetOperations<String, String> set = redisTemplate.opsForSet();
        set.add(k, JsonUtils.to(v));
    }

    /**
     * 集合获取
     *
     * @param key
     * @return
     */
    public <T> Set<T> sMembers(String key, Class<T> cls) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        Set<String> origins = setOper.members(key);
        if (null == origins || origins.isEmpty()) return new HashSet<>(0);

        Set<T> targets = new HashSet<>(origins.size());
        origins.forEach(item -> {
            targets.add(JsonUtils.from(cls, item));
        });
        return targets;
    }

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     * @param score 排序浮点分数
     */
    public <T> void zAdd(String key, T value, double score) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        zset.add(key, JsonUtils.to(value), score);
    }

    /**
     * 有序集合获取
     *
     * @param key
     * @param minScore
     * @param maxScore
     * @return
     */
    public <T> Set<T> rangeByScore(String key, double minScore, double maxScore, Class<T> cls) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<String> origins = zset.rangeByScore(key, minScore, maxScore);
        if (null == origins || origins.isEmpty()) return new HashSet<>(0);

        Set<T> targets = new HashSet<>(origins.size());
        origins.forEach(item -> {
            targets.add(JsonUtils.from(cls, item));
        });
        return targets;
    }


    //第一次加载的时候将数据加载到redis中
    // TODO 理解
    public void saveDataToRedis(String name) {
        double index = Math.abs(name.hashCode() % size);
        long indexLong = new Double(index).longValue();
        boolean availableUsers = setBit("availableUsers", indexLong, true);
    }

    //第一次加载的时候将数据加载到redis中
    // TODO 理解
    public boolean getDataToRedis(String name) {

        double index = Math.abs(name.hashCode() % size);
        long indexLong = new Double(index).longValue();
        return getBit("availableUsers", indexLong);
    }

    /**
     * 有序集合获取排名
     *
     * @param key   集合名称
     * @param value 值
     */
    public <T> Long zRank(String key, T value) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        return zset.rank(key, JsonUtils.to(value));
    }


    /**
     * 有序集合获取排名
     *
     * @param key
     * @return 无数据返回 NULL
     */
    public <T> Set<ZSetOperations.TypedTuple<T>> zRankWithScore(String key, long start, long end, Class<T> cls) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();

        Set<ZSetOperations.TypedTuple<String>> ret = zset.rangeWithScores(key, start, end);
        return conver(ret, cls);
    }

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     */
    public <T> Double zSetScore(String key, T value) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        return zset.score(key, JsonUtils.to(value));
    }


    /**
     * 有序集合添加分数
     *
     * @param key
     * @param value
     * @param scoure
     */
    public <T> void incrementScore(String key, T value, double scoure) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();

        zset.incrementScore(key, JsonUtils.to(value), scoure);
    }


    /**
     * 有序集合获取排名
     *
     * @param key
     */
    public <T> Set<ZSetOperations.TypedTuple<T>> reverseZRankWithScore(String key, long start, long end, Class<T> cls) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> ret = zset.reverseRangeByScoreWithScores(key, start, end);

        return conver(ret, cls);
    }

    /**
     * 有序集合获取排名
     *
     * @param key
     */
    public <T> Set<ZSetOperations.TypedTuple<T>> reverseZRankWithRank(String key, long start, long end, Class<T> cls) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> ret = zset.reverseRangeWithScores(key, start, end);

        return conver(ret, cls);
    }

    private <T> Set<ZSetOperations.TypedTuple<T>> conver(Set<ZSetOperations.TypedTuple<String>> ret, Class<T> cls) {
        if (null == ret || ret.isEmpty()) return new LinkedHashSet<>(0);
        Set<ZSetOperations.TypedTuple<T>> targets = new LinkedHashSet<>(ret.size());
        ret.forEach(item -> {
            targets.add(new DefaultTypedTuple<T>(JsonUtils.from(cls, item.getValue()), item.getScore()));
        });
        return targets;
    }
}
