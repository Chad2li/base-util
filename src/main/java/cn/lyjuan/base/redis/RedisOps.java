package cn.lyjuan.base.redis;

import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.util.Assert;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * RedisUtils
 */
@Setter
public class RedisOps {
    public static final String BEAN_NAME = "baseRedisOps";

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
        if (StringUtils.isNullArray(keys))
            throw new RuntimeException("Redis del keys cannot be null");
        if (1 == keys.length)
            redisTemplate.delete(keys[0]);
        else
            redisTemplate.delete(Arrays.asList(keys));
    }

    /**
     * redis键重命名
     *
     * @param oldKey
     * @param newKey
     */
    public void rename(final String oldKey, final String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * redis值自增
     *
     * @param redisKey
     */
    public long incr(final String redisKey) {
        return redisTemplate.opsForValue().increment(redisKey);
    }

    /**
     * redis值自增指定的数
     *
     * @param redisKey
     * @param delta    增加的数，可为负
     */
    public long incrby(final String redisKey, int delta) {
        return redisTemplate.opsForValue().increment(redisKey, delta);
    }

    /**
     * 一次设置多个键值
     *
     * @param values
     */
    public void mset(final Object... values) {
        if (StringUtils.isNullArray(values)) return;
        Map<String, String> map = new HashMap<>(values.length / 2);
        for (int i = 0; i < values.length; i = i + 2) {
            map.put(StringUtils.toStr(values[i]), JsonUtils.to(values[i + 1]));
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
     * @param xx            true强制存在才设置；false强制不存在才设置；null不作要求
     * @param expireSeconds 过期秒数；null 不设置过期时间或保持原有ttl
     * @return true设置成功
     */
    public boolean set(final String key, final Object value, Boolean xx, Long expireSeconds) {
        ValueOperations<String, String> oper = redisTemplate.opsForValue();
        String valStr = JsonUtils.to(value);
        Boolean result;
        if (null == xx) {
            // 不作要求
            if (null == expireSeconds) {
                oper.set(key, valStr);
            } else {
                oper.set(key, valStr, expireSeconds, TimeUnit.SECONDS);
            }
            return true;
        } else if (xx) {
            // 强制存在才改变值
            if (null == expireSeconds) {
                result = oper.setIfPresent(key, valStr);
            } else {
                result = oper.setIfPresent(key, valStr, expireSeconds, TimeUnit.SECONDS);
            }
        } else {
            // 强制不存在才改变值
            if (null == expireSeconds) {
                result = oper.setIfAbsent(key, valStr);
            } else {
                result = oper.setIfAbsent(key, valStr, expireSeconds, TimeUnit.SECONDS);
            }
        }
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
        if (StringUtils.isNullArray(keys))
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

    public List<String> gets(final String... keys) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        return operations.multiGet(Arrays.asList(keys));
    }

    public <T> List<T> gest(Class<T> cls, final T def, final String... keys) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        List<String> list = operations.multiGet(Arrays.asList(keys));
        int size = keys.length;
        List result = new ArrayList<>(keys.length);
        if (StringUtils.isNull(list)) {
            for (int i = 0; i < size; i++) {
                result.add(def);
            }
            return result;
        }

        for (String s : list) {
            result.add(JsonUtils.from(cls, s));
        }

        return result;
    }

    /**
     * 获取值，如果键不存在，则设置为默认值，并返回
     *
     * @param k          键
     * @param defaultVal 默认值
     * @param type       值类型
     * @return T
     * @date 2021/8/1 20:33
     * @author chad
     * @since 2.2.11
     */
    public <T> T get(final String k, T defaultVal, Type type) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String val = operations.get(k);
        if (!StringUtils.isNull(val)) {
            // 大部分情况下这里就返回了
            return JsonUtils.from(type, val);
        }
        Boolean isSetOk = operations.setIfAbsent(k, JsonUtils.to(defaultVal));
        if (isSetOk) {
            return defaultVal;
        }

        // 递归调用
        return get(k, defaultVal, type);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public <T> T get(final String key, Type cls) {
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
        String[] hashKeys = new String[hashKey.length];
        int i = 0;
        for (Object o : hashKey) {
            hashKeys[i++] = JsonUtils.to(o);
        }
        return redisTemplate.opsForHash().delete(key, hashKeys);
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
    public <T> T hgetAll(final String key, Type cls) {
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
    public <T> T hmGet(String key, Object hashKey, Type cls) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        String json = hash.get(key, JsonUtils.to(hashKey));
        if (StringUtils.isNull(json)) return null;
        return JsonUtils.from(cls, json);
    }

    public <T> List<T> hmGetMulti(final String key, Type cls, Object... hashKeys) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        int size = hashKeys.length;
        List<String> keys = new ArrayList<>(size);
        for (Object k : hashKeys) {
            keys.add(JsonUtils.to(k));
        }
        List<String> list = hash.multiGet(key, keys);
        List<T> result = new ArrayList<>(size);
        for (String s : list) {
            result.add(JsonUtils.from(cls, s));
        }

        return result;
    }

    /**
     * list.rightPush
     *
     * @param k
     * @param values
     * @param <T>
     */
    public <T> void lPush(String k, T... values) {
        lPush(true, k, values);
    }

//    public <T> void lPush(String k, Collection<T> vs) {
//        lPush(true, k, rawValues(vs));
//    }

    /**
     * 列表添加
     *
     * @param isRight true使用 rightPush；false使用leftPush
     * @param k
     * @param values
     */
    public <T> void lPush(boolean isRight, String k, T... values) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        if (StringUtils.isNullArray(values)) return;

        if (1 == values.length) {
            if (StringUtils.isNull(values[0])) return;
            String vJson = JsonUtils.to(values[0]);
            if (isRight)
                list.rightPush(k, vJson);
            else
                list.leftPush(k, vJson);
        } else {
            String[] vs = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                if (StringUtils.isNull(values[i])) continue;

                vs[i] = JsonUtils.to(values[i]);
            }

            if (isRight)
                list.rightPushAll(k, vs);
            else
                list.leftPushAll(k, vs);
        }
    }

    /**
     * list.leftPop
     *
     * @param k
     * @param cls
     * @param <T>
     * @return
     */
    public <T> T lPop(String k, Type cls) {
        return lPop(k, true, cls);
    }

    /**
     * 获取列表元素
     *
     * @param k
     * @param isLeft true leftPop；false rightPop
     * @param cls
     * @param <T>
     * @return
     */
    public <T> T lPop(String k, boolean isLeft, Type cls) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        String json = null;
        if (isLeft)
            json = list.leftPop(k);
        else
            json = list.rightPop(k);

        return StringUtils.isNull(json) ? null : JsonUtils.from(cls, json);
    }

    /**
     * 列表获取
     *
     * @param k
     * @param start
     * @param end
     * @return
     */
    public <T> List<T> lRange(String k, long start, long end, Type cls) {
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
     * 添加set元素
     *
     * @param k
     * @param values
     * @param <T>
     */
    public <T> long sAdd(String k, T... values) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        if (StringUtils.isNullArray(values)) return 0;
        String[] vs = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            if (StringUtils.isNull(values[i])) continue;
            vs[i] = JsonUtils.to(values[i]);
        }
        return setOper.add(k, vs);
    }

    /**
     * 移除set元素
     *
     * @param k
     * @param values
     * @param <T>
     * @return
     */
    public <T> long sRem(String k, T... values) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        if (StringUtils.isNullArray(values)) return 0;
        List<String> list = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            if (StringUtils.isNull(values[i])) continue;
            list.add(JsonUtils.to(values[i]));
        }


        return setOper.remove(k, list.toArray());
    }

    /**
     * 以source为基础，对比ks，找出差集，存入dest
     *
     * @param dest   目标SET
     * @param source 源SET
     * @param ks     差集比对的SET
     * @return void
     * @date 2021/8/1 20:18
     * @author chad
     * @since 2.2.11
     */
    public void sDiffStore(final String dest, final String source, final String... ks) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        setOper.differenceAndStore(source, Arrays.asList(ks), dest);
    }


    /**
     * Set元素大小
     *
     * @param key
     * @return
     */
    public long sSize(final String key) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        Long size = setOper.size(key);
        return null == size ? 0 : size;
    }

    /**
     * set中是否存在该值
     *
     * @param k
     * @param v
     * @param <T>
     * @return
     */
    public <T> boolean sIsMember(String k, T v) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        return StringUtils.isNull(v) ? false : setOper.isMember(k, JsonUtils.to(v));
    }

    /**
     * 随机删除set中一个元素，并返回该值
     *
     * @param k
     * @param type
     * @param <T>
     * @return
     */
    public <T> T sPop(String k, Type type) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        String json = setOper.pop(k);
        return JsonUtils.from(type, json);
    }

    /**
     * 集合获取
     *
     * @param key
     * @return
     */
    public <T> Set<T> sMembers(String key, Type cls) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        Set<String> origins = setOper.members(key);
        if (null == origins || origins.isEmpty()) return Collections.emptySet();

        Set<T> targets = new HashSet<>(origins.size());
        origins.forEach(item -> {
            if (StringUtils.isNull(item)) return;
            targets.add(JsonUtils.from(cls, item));
        });
        return targets;
    }

    /**
     * 获取Set中指定数量的元素，默认非重复元素
     *
     * @param k
     * @param count
     * @param cls
     * @return java.util.Set<T>
     * @date 2021/8/20 15:27
     * @author chad
     * @since
     */
    public <T> Set<T> sRandom(final String k, int count, Type cls) {
        return sRandom(k, count, cls, true);
    }

    /**
     * 随机获取Set中指定数量的元素
     *
     * @param k          redis键
     * @param isDistinct true返回结果不重复
     * @param count      随机获取的数量
     * @return java.util.Set<T>
     * @date 2021/8/1 20:27
     * @author chad
     * @since 1 新增
     * @since 2 by chad at 2021/08/20 增加参数获取非重复元素
     */
    public <T> Set<T> sRandom(final String k, int count, Type cls, boolean isDistinct) {
        SetOperations<String, String> setOper = redisTemplate.opsForSet();
        Collection<String> list = null;
        if (isDistinct) {
            list = setOper.distinctRandomMembers(k, count);
        } else {
            list = setOper.randomMembers(k, count);
        }
        if (StringUtils.isNull(list)) {
            return Collections.EMPTY_SET;
        }
        Set<T> set = new HashSet<>(list.size());
        for (String s : list) {
            set.add(JsonUtils.from(cls, s));
        }

        return set;
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
    public <T> Set<T> zRangeByScore(String key, double minScore, double maxScore, Type cls) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<String> origins = zset.rangeByScore(key, minScore, maxScore);
        if (null == origins || origins.isEmpty()) return Collections.emptySet();

        Set<T> targets = new HashSet<>(origins.size());
        origins.forEach(item -> {
            targets.add(JsonUtils.from(cls, item));
        });
        return targets;
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
     * ZSet获取指定排名的成员
     *
     * @param key   redis键
     * @param start 开始位置索引，从0开始，包含
     * @param end   结束位置索引，尾部为-1，包含
     * @return
     */
    public <T> Set<T> zRangeByRank(final String key, long start, long end, Type type) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<String> set = zset.range(key, start, end);
        if (StringUtils.isNull(set)) {
            return Collections.EMPTY_SET;
        }

        Set<T> setVal = new HashSet<>(set.size());
        set.forEach(it -> {
            setVal.add(JsonUtils.from(type, it));
        });

        return setVal;
    }

    /**
     * ZSet删除指定的成员
     *
     * @param key    Redis键
     * @param values ZSet要删除的成员，多个
     * @return long 删除的成员数量
     * @date 2021/8/13 14:32
     * @author chad
     * @since 1 by chad at 2021/08/13: 新增该方法
     */
    public long zRemove(final String key, Object... values) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        return zset.remove(key, values);
    }

    /**
     * ZSet集合成员数量
     *
     * @param key redis键
     * @return long
     * @date 2021/8/13 14:38
     * @author chad
     * @since 1 by chad 2021/8/13: 新增该方法
     */
    public long zSize(final String key) {
        ZSetOperations<String, String> zs = redisTemplate.opsForZSet();
        return zs.zCard(key);
    }


    /**
     * 有序集合获取排名
     *
     * @param key
     * @return 无数据返回 NULL
     */
    public <T> Set<ZSetOperations.TypedTuple<T>> zRankWithScore(String key, long start, long end, Type cls) {
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

    public <T> String[] rawValues(T... values) {
        Assert.notEmpty(values, "Values must not be 'null' or empty.");
        Assert.noNullElements(values, "Values must not contain 'null' value.");
        String[] strArr = new String[values.length];
        int i = 0;
        for (T t : values) {
            strArr[i++] = JsonUtils.to(t);
        }
        return strArr;
    }

    public <T> String[] rawValues(Collection<T> values) {
        Assert.notEmpty(values, "Values must not be 'null' or empty.");
        Assert.noNullElements(values.toArray(), "Values must not contain 'null' value.");

        String[] strArr = new String[values.size()];
        int i = 0;
        for (T t : values) {
            strArr[i++] = JsonUtils.to(t);
        }
        return strArr;
    }


    /**
     * 有序集合获取排名
     *
     * @param key
     */
    public <T> Set<ZSetOperations.TypedTuple<T>> reverseZRankWithScore(String key, long start, long end, Type cls) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> ret = zset.reverseRangeByScoreWithScores(key, start, end);

        return conver(ret, cls);
    }

    /**
     * 有序集合获取排名
     *
     * @param key
     */
    public <T> Set<ZSetOperations.TypedTuple<T>> reverseZRankWithRank(String key, long start, long end, Type cls) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> ret = zset.reverseRangeWithScores(key, start, end);

        return conver(ret, cls);
    }

    private <T> Set<ZSetOperations.TypedTuple<T>> conver(Set<ZSetOperations.TypedTuple<String>> ret, Type cls) {
        if (null == ret || ret.isEmpty()) return new LinkedHashSet<>(0);
        Set<ZSetOperations.TypedTuple<T>> targets = new LinkedHashSet<>(ret.size());
        ret.forEach(item -> {
            targets.add(new DefaultTypedTuple<T>(JsonUtils.from(cls, item.getValue()), item.getScore()));
        });
        return targets;
    }
}
