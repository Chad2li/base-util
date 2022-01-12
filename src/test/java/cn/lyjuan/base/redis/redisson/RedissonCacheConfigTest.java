package cn.lyjuan.base.redis.redisson;

import cn.lyjuan.base.test.BaseSpringTest;
import cn.lyjuan.base.util.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 测试 spring cacheable + redisson
 *
 * @author chad
 * @date 2022/1/11 10:40
 * @since 1 by chad create
 */
@EnableCaching
@ImportAutoConfiguration(classes = {RedissonReplicaConfig.class, RedissonCacheConfig.class, RedissonCacheConfigTest.RedissonRepositoryConfig.class})
public class RedissonCacheConfigTest extends BaseSpringTest {
    @Resource
    private RedissonRepository repo;

    @Test
    public void cache() throws InterruptedException {
        DemoDto user = repo.getDemoDto("ZhangSan");
        user = repo.getDemoDto("user0");
        Thread.sleep(1000);
        user = repo.getDemoDto("user1");
        Thread.sleep(1000);
        user = repo.getDemoDto("user2");
        Thread.sleep(1000);
        user = repo.getDemoDto("user3");
        Thread.sleep(1000);
        user = repo.getDemoDto("user4");

        // 等待缓存过期
        Thread.sleep(10 * 1000);

        // 无缓存的情况下取值
        user = repo.getDemoDto("user5");

        Thread.sleep(10 * 1000);
    }

    public static class RedissonRepositoryConfig {
        @Bean
        public RedissonRepository repo() {
            return new RedissonRepository();
        }
    }

    @Service
    public static class RedissonRepository {
        /**
         * 带 缓存的方法
         */
        @Cacheable(cacheNames = {"spring:cache"})
        public DemoDto getDemoDto(String name) {
            System.out.println("New Demo user: " + name);
            return new DemoDto(name, RandomUtils.randomInt(2));
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Data
    public static class DemoDto {
        private String name;
        private int age;
    }
}