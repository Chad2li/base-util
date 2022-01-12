package cn.lyjuan.base.redis.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring cacheable + redisson
 *
 * @author chad
 * @date 2022/1/11 10:11
 * @since 1 by chad create
 */
@Slf4j
public class RedissonCacheConfig extends CachingConfigurerSupport {

    /**
     * 缓存键生成器
     *
     * @return org.springframework.cache.interceptor.KeyGenerator
     * @date 2022/1/11 12:11
     * @author chad
     * @since 1 by chad create
     */
    @Override
    public KeyGenerator keyGenerator() {
        log.info("Init KeyGenerator");
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(target.getClass().getSimpleName());
                stringBuilder.append(".");
                stringBuilder.append(method.getName());
                stringBuilder.append("[");
                for (Object obj : params) {
                    stringBuilder.append(obj.toString());
                }
                stringBuilder.append("]");

                return stringBuilder.toString();
            }
        };
    }

    @Bean
    public CacheManager cacheManager(RedissonOps redissonOps) {
        RedissonSpringCacheManager rcm = new RedissonSpringCacheManager(redissonOps.getClient());
        Map<String, CacheConfig> configs = new HashMap<>(1);
        // ttl: 键值存活的时间，毫秒
        // max idle time: 在 ttl 为 0 的情况下，键值在该时间内未被使用将被移除
        // max size: 缓存 map 最大的条目数，超出部分使用 LRU 算法移除
//        CacheConfig config = new CacheConfig(5 * 1000, 10 * 1000);
//        config.setMaxSize(2);
//        configs.put("spring:cache", config);
//        rcm.setConfig(configs);
        return rcm;
    }
}
