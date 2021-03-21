package cn.lyjuan.base.redis;

import cn.lyjuan.base.test.BaseSpringTest;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

public class RedisReplicaTest extends BaseSpringTest {
    @Resource
    private RedisOps redisOps;

    @Test
    public void getAndSet() {
        redisOps.set("name", "Lisi");
        String name = redisOps.get("name");
        Assert.assertEquals("Lisi", name);
    }
}
