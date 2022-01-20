package cn.lyjuan.base.redis.redisson;

import cn.lyjuan.base.util.ReflectUtils;
import cn.lyjuan.base.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redisson封装工具
 */
@Slf4j
public class RedissonOps {

    @Getter
    private RedissonClient client;
    @Getter
    private ObjectMapper objectMapper;

    @Autowired
    public RedissonOps(@Autowired RedissonClient client,
                       @Autowired @Qualifier(RedissonBaseConfig.OBJECT_MAPPER_NAME) ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     */
    public void set(final String key, Object value) {
        RBucket<Object> bucket = client.getBucket(key);
        bucket.set(value);
    }

    /**
     * 设置键值对
     *
     * @param key
     * @param value
     * @param xx            true强制存在才设置；false强制不存在才设置；null不作要求，不存在则创建，存在则覆盖
     * @param expireSeconds 过期秒数，小于等于0不设置过期时间
     * @return true设置成功
     */
    public boolean set(final String key, Object value, Boolean xx, Long expireSeconds) {
        RBucket<Object> rb = client.getBucket(key);
        boolean result = false;
        if (null == xx) {
            if (expireSeconds > 0)
                rb.set(value, expireSeconds, TimeUnit.SECONDS);
            else
                rb.set(value);
        } else if (xx) {// 强制存在
            if (expireSeconds > 0)
                result = rb.setIfExists(value, expireSeconds, TimeUnit.SECONDS);
            else
                result = rb.setIfExists(value);
        } else {// 强制不存在
            if (expireSeconds > 0)
                result = rb.trySet(value, expireSeconds, TimeUnit.SECONDS);
            else
                result = rb.trySet(value);
        }
        return result;
    }

    /**
     * 获取指定结果类型
     *
     * @param key 键值
     * @param <T>
     * @return
     */
    public <T> T get(final String key) {
        RBucket<T> bucket = client.getBucket(key);
        T t = bucket.get();
        return t;
    }

    /**
     * 获取值，如果键不存在，则设置为默认值并返回
     *
     * @param key        键
     * @param defaultVal 默认值，不存在会将远程redis该键设置为默认值
     * @return T
     * @date 2021/8/1 17:13
     * @author chad
     * @since 1.0.0
     */
    public <T> T get(final String key, T defaultVal) {
        RBucket<T> rb = client.getBucket(key);
        T val = rb.get();
        if (!StringUtils.isNull(val)) {
            // 大部分情况下这里就返回了
            return val;
        }
        boolean isSetOk = rb.trySet(defaultVal);
        if (isSetOk) {
            return defaultVal;
        }

        // 递归调用
        return get(key, defaultVal);
    }

    /**
     * 批量获取键值
     *
     * @param keys 键值
     * @param <V>
     * @return
     */
    public <V> Map<String, V> gets(final String... keys) {
        RBuckets rbs = client.getBuckets();
        return rbs.get(keys);
    }

    /**
     * 强制返回结果为long型
     *
     * @param key 键
     * @return
     */
    public Long getLong(final String key) {
        RBucket<Object> rb = client.getBucket(key);
        Object t = rb.get();
        if (null == t)
            return null;
        if (Integer.class == t.getClass())
            return ((Integer) t).longValue();
        return (Long) t;
    }

    /**
     * 增1
     * Notes: 分布式情况下建议改为 {@link RLongAdder}
     *
     * @param key
     * @return 返回新值
     */
    public long incr(final String key) {
        RAtomicLong rtl = client.getAtomicLong(key);
        return rtl.incrementAndGet();
    }

    /**
     * 减1
     *
     * @param key
     * @return 返回新值
     */
    public long decr(final String key) {
        RAtomicLong rtl = client.getAtomicLong(key);
        return rtl.decrementAndGet();
    }

    /**
     * 增加 delta
     *
     * @param key
     * @param delta 可为负，即减
     * @return 返回新值
     */
    public long incr(final String key, long delta) {
        RAtomicLong rtl = client.getAtomicLong(key);
        return rtl.addAndGet(delta);
    }

    /**
     * 如果为期望的值，则更新
     *
     * @param key    键
     * @param expect 期望的值
     * @param update 更新的值
     * @param <T>
     * @return
     */
    public <T> boolean compareAndSet(final String key, T expect, T update) {
        RBucket<Object> bucket = client.getBucket(key);
        return bucket.compareAndSet(expect, update);
    }

    /**
     * 获取通用操作桶
     *
     * @param key 键值
     * @param <T> 类型
     * @return
     */
    private <T> RBucket<T> getBucket(final String key) {
        return client.getBucket(key);
    }

    /**
     * 删除Redis键
     *
     * @param keys 键，支持数组
     * @return 返回删除的键的数量
     * @since 2 by chad at 2021/12/24 增加返回值
     */
    public long del(final String... keys) {
        if (StringUtils.isNullArray(keys)) {
            return 0;
        }
        RKeys rkeys = client.getKeys();
        long count = rkeys.delete(keys);
        return count;
    }

    /**
     * 获取键值并删
     *
     * @param key redis键
     * @return T null键不存在，请用小写接收
     * @date 2022/1/18 19:56
     * @author chad
     * @since 1 by chad create
     */
    public <T> T getAndDel(final String key) {
        if (StringUtils.isNull(key)) {
            return null;
        }
        RBucket<T> rb = client.getBucket(key);
        T val = rb.getAndDelete();
        return val;
    }

    /**
     * 设置key的过期时间
     *
     * @param key           键值
     * @param expireSeconds 过期时间，秒
     */
    public void expire(final String key, long expireSeconds) {
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
        RBucket bucket = client.getBucket(key);
        return bucket.isExists();
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

    /**
     * 保存Map为redis hash结构
     *
     * @param key   键
     * @param value hash结构值
     */
    public void hSetMap(final String key, Map value) {
        RMap rmap = client.getMap(key);
        rmap.putAll(value);
    }

    /**
     * 保存对象为redis hash结构
     *
     * @param key redis键
     * @param obj 对象，将对象转为hash结构存在
     */
    public void hSetBean(final String key, Object obj) {
        if (ReflectUtils.isBaseClass(obj.getClass()))
            throw new IllegalArgumentException("Java base class cannot save by hash: " + obj.getClass().getName());
        Map map = ReflectUtils.membersToMap(obj);
        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry e = it.next();
            if (null == e.getValue())
                it.remove();
        }
        RMap rmap = client.getMap(key);
        rmap.putAll(map);
    }

    /**
     * 获取hash中指定的键值
     *
     * @param redisKey redis键
     * @param hashKey  hash键
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> V hGet(final String redisKey, Object hashKey) {
        RMap<K, V> rm = client.getMap(redisKey);
        return rm.get(hashKey);
    }

    /**
     * {@link RedissonOps#hGets(String, Set)}
     *
     * @param redisKey redis键
     * @param hashKeys hash键
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> Map<K, V> hGets(final String redisKey, K... hashKeys) {
        Set<K> set = new HashSet<>(hashKeys.length);
        for (K o : hashKeys) {
            set.add(o);
        }
        return hGets(redisKey, set);
    }

    /**
     * 一次性获取多个指定的hash键值
     *
     * @param redisKey redis键
     * @param hashKeys hash键，多个
     * @param <K>
     * @param <V>
     * @return 当hashKeys中有不存在的映射值时，结果中并不包含不存在的键值
     */
    public <K, V> Map<K, V> hGets(final String redisKey, Set<K> hashKeys) {
        RMap<K, V> rm = client.getMap(redisKey);
        return rm.getAll(hashKeys);
    }

    /**
     * 获取hash结构所有值
     *
     * @param key redis键
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> Map<K, V> hGetMap(final String key) {
        return client.getMap(key);
    }

    /**
     * 获取hash值并解析bean
     *
     * @param redisKey 键值
     * @param cls
     * @param <T>
     * @return
     */
    public <T> T hGetBean(final String redisKey, Class<T> cls) {
//        Set<String> names = ReflectUtils.parseMember(cls);
        Map<String, Object> map = hGetMap(redisKey);

        if (null == map || map.isEmpty()) return null;

//        T t = objectMapper.convertValue(map, type);
        T t = ReflectUtils.genBean(cls, map);
        return t;
    }

    /**
     * 设置hash键值
     *
     * @param redisKey redis键
     * @param hashKey  hash键
     * @param value    值
     * @return true如果hashKey不存在；false如果hashKey存在并更新成功
     */
    public boolean hmSet(final String redisKey, Object hashKey, Object value) {
        RMap rmap = client.getMap(redisKey);
        return rmap.fastPut(hashKey, value);
    }

    /**
     * 设置hash值，并对原hash键存在性作较检验
     *
     * @param redisKey redis键
     * @param hashKey  hash键
     * @param value    需要设置的值
     * @param xx       true原值必须存在；false原值必须不存在；null不作要求
     * @return boolean true设置成功，值被改变
     * @date 2021/9/23 20:44
     * @author chad
     * @since 1 by chad at 2021/9/23 新增
     */
    public boolean hmSet(final String redisKey, Object hashKey, Object value, Boolean xx) {
        RMap rmap = client.getMap(redisKey);

        if (null == xx) {
            // 存在性不作要求
            return rmap.fastPut(hashKey, value);
        } else if (xx) {
            // 强制存在才设置
            Object oldVal = rmap.putIfExists(hashKey, value);
            return null != oldVal;
        } else {
            // 强制不存在才设置
            Object oldVal = rmap.putIfAbsent(hashKey, value);
            return null == oldVal;
        }
    }

    /**
     * hash是否存在
     *
     * @param redisKey redis键
     * @param hashKey  hash键
     * @return boolean true hash值存在
     * @date 2021/12/30 21:30
     * @author chad
     * @since 1 by chad create
     */
    public <T> boolean hmExists(final String redisKey, Object hashKey) {
        RMap<Object, T> rm = client.getMap(redisKey);
        boolean exists = rm.containsKey(hashKey);
        return exists;
    }

    /**
     * 执行Hash快速删除
     *
     * @param redisKey redis键
     * @param hashKeys hash键
     * @return long 删除hash值的数量
     * @date 2021/12/30 21:05
     * @author chad
     * @since 1 by chad create
     */
    public <T> long hmDel(final String redisKey, Object... hashKeys) {
        RMap<Object, T> rm = client.getMap(redisKey);
        long count = rm.fastRemove(hashKeys);
        return count;
    }

    /**
     * 删除并取回被删除的 hash 值
     *
     * @param redisKey redis键
     * @param hashKey  hash键
     * @return T 删除的 hash 值，如果 redisKey 或 hashKey 不存在返回 null
     * @date 2021/12/30 21:26
     * @author chad
     * @since 1 by chad create
     */
    public <T> T hmDelAndGet(final String redisKey, Object hashKey) {
        RMap<Object, T> rm = client.getMap(redisKey);
        T t = rm.remove(hashKey);
        return t;
    }

    /**
     * 获取redis hash结构指定键的值
     *
     * @param redisKey redis键
     * @param hashKey  hash键
     * @param <T>
     * @return 值，如果不存在返回null
     */
    public <T> T hmGet(final String redisKey, Object hashKey) {
        RMap<Object, T> rmap = client.getMap(redisKey);
        return rmap.get(hashKey);
    }

    /**
     * 往redis set中增加值
     *
     * @param key   键
     * @param value 值
     * @return true 增加成功（原set中不包含该值）；false 增加失败（原set中包含该值）
     */
    public boolean sAdd(final String key, Object value) {
        if (StringUtils.isNull(value))
            throw new NullPointerException("Redis cannot set null to set");
        RSet<Object> rs = client.getSet(key);
        return rs.add(value);
    }

    /**
     * 批量给指定Set增加元素，如果该Set已存在，则执行union（并集）操作
     *
     * @param key    redis键
     * @param values 新增元素
     * @return true如果该Set元素内容发生变化
     */
    public boolean sAdds(final String key, Collection values) {
        if (StringUtils.isNull(values))
            return false;

        RSet rs = client.getSet(key);
        return rs.addAll(values);
    }

    /**
     * 批量给指定Set增加元素，如果该Set已存在，则执行union（并集）操作
     *
     * @param key    redis键
     * @param values 新增元素
     * @return true如果该Set元素内容发生变化
     */
    public boolean sAdds(final String key, Object... values) {
        if (StringUtils.isNullArray(values))
            return false;

        return sAdds(key, Arrays.asList(values));
    }

    /**
     * set中是否存在指定值
     *
     * @param key   键
     * @param value 是否存在该值
     * @return true存在
     */
    public boolean sContains(final String key, Object value) {
        RSet<Object> rset = client.getSet(key);
        return rset.contains(value);
    }

    /**
     * 删除set中指定的值
     *
     * @param key   键
     * @param value 值
     * @return true删除成功；false值不存在
     */
    public boolean sDel(final String key, Object value) {
        if (StringUtils.isNull(value))
            return false;
        RSet<Object> rset = client.getSet(key);
        return rset.remove(value);
    }

    /**
     * 删除set中指定的多个值
     *
     * @param key    键
     * @param values 值，支持集合
     * @return true set元素发生改变（values全部删除或部分删除）；false未发生改变
     */
    public boolean sDels(final String key, Collection values) {
        if (StringUtils.isNull(values))
            return false;
        RSet<Object> rset = client.getSet(key);
        return rset.removeAll(values);
    }

    /**
     * 获取Set元素个数
     *
     * @param key 键
     * @return 最大值 {@link Integer#MAX_VALUE}
     */
    public int sSize(final String key) {
        RSet rset = client.getSet(key);
        return rset.size();
    }

    /**
     * 随机取一值
     *
     * @param key 键
     * @param <T>
     * @return null 当set没有任何元素时
     */
    public <T> T sRandom(final String key) {
        RSet<T> rset = client.getSet(key);
        return rset.random();
    }

    /**
     * 随机取 count 个元素
     *
     * @param key   redis键
     * @param count 随机取出的数量
     * @param <T>
     * @return
     */
    public <T> Set<T> sRandom(final String key, int count) {
        RSet<T> rs = client.getSet(key);
        return rs.random(count);
    }

    /**
     * 取 {@code diffKeys} 的差集，并存入 {@code dest}中，如果 dest存在则覆盖<br/>
     * 以 diffKeys 第1个key指定的Set为主，对比其后的Set，找出1中所有不存在于其他set的元素，放入dest
     *
     * @param dest
     * @param diffKeys
     */
    public void sDiffStore(final String dest, final String... diffKeys) {
        client.getSet(dest).diff(diffKeys);
    }

    /**
     * 往redis zset中新增 member
     *
     * @param redisKey redis键
     * @param member   成员
     * @param score    排序分数
     * @return true增加成功；false 如果member已存在，则增加失败
     */
    public boolean zAdd(final String redisKey, Object member, double score) {
        RScoredSortedSet rs = client.getScoredSortedSet(redisKey);
        return rs.add(score, member);
    }

    /**
     * 批量增加zset成员
     *
     * @param redisKey 键
     * @param members  成员分数映射，Key为成员，Value为score
     * @param <M>
     * @return 成功新增的数量，不计数已经存在的（即使其score在本次被覆盖更新）
     */
    public <M> int zAdd(final String redisKey, Map<M, Double> members) {
        RScoredSortedSet rs = client.getScoredSortedSet(redisKey);
        return rs.addAll(members);
    }

    /**
     * 删除redis zset中存在的member
     *
     * @param redisKey redis键
     * @param member   成员
     * @return true 删除成功；false member不存在
     */
    public boolean zDel(final String redisKey, Object member) {
        RScoredSortedSet rs = client.getScoredSortedSet(redisKey);
        return rs.remove(member);
    }

    /**
     * 获取zset成员个数
     *
     * @param redisKey 键
     * @return
     */
    public int zCount(final String redisKey) {
        RScoredSortedSet rs = client.getScoredSortedSet(redisKey);
        return rs.size();
    }

    /**
     * 获取指定score范围内成员个数
     *
     * @param key        键
     * @param startScore 起点score，包含
     * @param endScore   结束score，不包含
     * @return
     */
    public int zCount(final String key, double startScore, double endScore) {
        RScoredSortedSet rs = client.getScoredSortedSet(key);
        return rs.count(startScore, true, endScore, false);
    }

    /**
     * 获取成员当前分数
     *
     * @param redisKey redis键
     * @param member   成员
     * @return
     */
    public Double zScore(final String redisKey, Object member) {
        RScoredSortedSet rs = client.getScoredSortedSet(redisKey);
        return rs.getScore(member);
    }

    /**
     * 获取指定score范围内成员
     *
     * @param key        键
     * @param startScore 开始score，包含
     * @param endScore   结束score，不包含
     * @param <T>
     * @return
     */
    public <T> Collection<T> zRange(final String key, double startScore, double endScore) {
        RScoredSortedSet rs = client.getScoredSortedSet(key);
        return rs.valueRange(startScore, true, endScore, false);
    }

    /**
     * 取Zset中score最小者
     *
     * @param key redis键
     * @param <T>
     * @return 最小成员，zset无成员返回 null
     */
    public <T> T zMin(final String key) {
        RScoredSortedSet<T> rs = client.getScoredSortedSet(key);
        Collection<T> cs = rs.valueRange(0, 0);
        if (null == cs || cs.isEmpty())
            return null;
        return cs.iterator().next();
    }

    /**
     * 取Zset中score最大者
     *
     * @param key redis键
     * @param <T>
     * @return 最大成员，zset无成员返回 null
     */
    public <T> T zMax(final String key) {
        RScoredSortedSet<T> rs = client.getScoredSortedSet(key);
        Collection<T> cs = rs.valueRange(-1, -1);
        if (null == cs || cs.isEmpty())
            return null;
        return cs.iterator().next();
    }

    /**
     * zset成员score自增
     *
     * @param key    键
     * @param member 成员
     * @return 返回score值改变后的排名，从0开始升序，如果score相同，后加入的排序后
     */
    public double zIncr(final String key, Object member) {
        RScoredSortedSet rs = client.getScoredSortedSet(key);
        return rs.addScore(member, 1);
    }

    /**
     * zset成员score自减
     *
     * @param key    键
     * @param member 成员
     * @return 返回score改变后的排名，从0开始升序
     */
    public double zDecrAndRank(final String key, Object member) {
        RScoredSortedSet rs = client.getScoredSortedSet(key);
        return rs.addScore(member, -1);
    }

    /**
     * 根据score获取redis zset成员及分数
     *
     * @param redisKey   键
     * @param startScore 开始分数（包含）
     * @param endScore   结束分数（不包含）
     * @param <T>
     * @return
     */
    public <T> Collection<ScoredEntry<T>> zRangeWithScore(final String redisKey, double startScore, double endScore) {
        RScoredSortedSet rs = client.getScoredSortedSet(redisKey);
        return rs.entryRange(startScore, true, endScore, false);
    }


    /**
     * 获取指定起始位置的成员
     * 0, -1表示全部成员
     *
     * @param redisKey 键
     * @param start    开始位置，第1个元素为0；负数表示从尾部开始，-1表示从尾部开始第1个元素
     * @param end      结束位置，包含
     */
    public <T> Collection<T> zRange(final String redisKey, int start, int end) {
        RScoredSortedSet rs = client.getScoredSortedSet(redisKey);
        return rs.valueRange(start, end);
    }

    /**
     * 将srcKey指定范围内的成员移除到desKey内<br />
     * Notes: redis 6.2.0+
     *
     * @param srcKey     源
     * @param desKey     目标
     * @param startScore 开始score，包含
     * @param endScore   结束score，不包含
     * @return 返回移动数据
     */
    public int zRangeStore(final String srcKey, final String desKey, double startScore, double endScore) {
        RScoredSortedSet rs = client.getScoredSortedSet(srcKey);
        return rs.rangeTo(desKey, startScore, true, endScore, false);
    }

    /**
     * 获取脚本执行器
     *
     * @return
     */
    public RScript getScript() {
        return client.getScript();
    }

    /**
     * 获取阻塞队列
     *
     * @param queueName 队列名称，全局唯一
     * @param <T>
     * @return
     */
    public <T> RBlockingQueue<T> getBlockingQueue(final String queueName) {
        return client.getBlockingQueue(queueName);
    }

    /**
     * 获取指定阻塞队列的延迟队列
     *
     * @param queue 阻塞队列
     * @param <T>
     * @return
     */
    public <T> RDelayedQueue<T> getDelayQueue(RBlockingQueue<T> queue) {
        return client.getDelayedQueue(queue);
    }

    /**
     * 获取Redis分布式锁
     * <p>
     * 为保证在主备切换中的可靠性，会同步等待所有的slaver服务器响应
     * </p>
     *
     * @param lockName 锁名称
     * @return org.redisson.api.RLock
     * @date 2021/9/22 22:25
     * @author chad
     * @since 1 by chad at 2021/9/22 新增
     */
    public RLock getLock(String lockName) {
        RLock rl = client.getLock(lockName);
        return rl;
    }

    /**
     * redisson批处理，参考redis pipeline
     *
     * @return org.redisson.api.RBatch
     * @date 2021/9/23 17:11
     * @author chad
     * @since 1 by chad at 2021/9/23 新增
     */
    public RBatch getBatch() {
        return client.createBatch();
    }
}
