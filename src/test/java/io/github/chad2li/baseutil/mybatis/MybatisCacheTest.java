package io.github.chad2li.baseutil.mybatis;

import io.github.chad2li.baseutil.mybatis.dao.DemoDao;
import io.github.chad2li.baseutil.mybatis.mapper.IDemoParentMapper;
import io.github.chad2li.baseutil.test.BaseSpringTest;
import io.github.chad2li.baseutil.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import tk.mybatis.spring.annotation.MapperScan;

import javax.annotation.Resource;

/**
 * @author chad
 * @date 2022/1/11 18:10
 * @since
 */
@Slf4j
@MapperScan(basePackages = {"cn.lyjuan.base.mybatis.mapper"})
public class MybatisCacheTest extends BaseSpringTest {
    //    @Resource
//    private IDemoMapper demoMapper;
    @Resource
    private IDemoParentMapper parentMapper;

    public static final String name_zs = "Zhangsan";

    @Test
    public void cache() throws InterruptedException {
        DemoDao demo = null;

        log.info("================================== 1 ==================================");
        // 基础缓存查询： 只有一次 SQL，缓存启作用
        demo = parentMapper.selectByName(name_zs);
        demo = parentMapper.selectByName(name_zs);

        log.info("================================== 2 ==================================");
        // 缓存刷新，flushInternal不起作用，只有在 update 后重新访问数据库
        log.info("2-1-等待 namespace flushInterval 刷新缓存");
        Thread.sleep(10 * 1000);
        log.info("2-2-实际并未刷新");
        demo = parentMapper.selectByName(name_zs);
        log.info("2-3-更新数据");
        parentMapper.updateAgeByName(demo.getName(), demo.getAge() + 1);
        log.info("2-4-更新导致 namespace 被清空");
        demo = parentMapper.selectByName(name_zs);

        log.info("================================== 3 ==================================");
        // 不存在的值-是否会穿透
        String rdmName = RandomUtils.uuid();
        log.info("3-1-多次查询不存在的值");
        demo = parentMapper.selectByName(rdmName);
        demo = parentMapper.selectByName(rdmName);

        // update/insert/delete 会清空整个 cache
        parentMapper.updateAgeByName(name_zs, 21);
        log.info("================================== 4 ==================================");
        log.info("4-1-初始查询");
        demo = parentMapper.selectByName("user2");// user2其实不存在，但会缓存 null 值
        demo = parentMapper.selectByName(name_zs);
        log.info("4-2-重复查询");
        demo = parentMapper.selectByName("user2");// 再次查询，证实缓存有效
        demo = parentMapper.selectByName(name_zs);
        log.info("4-3-更新导致 namespace 被清空");
        parentMapper.updateAgeByName(name_zs, demo.getAge());// 该操作会导致整个 namespace 缓存被清空
        log.info("4-4-所有关于该 namespace 的访问落地到数据库");
        demo = parentMapper.selectByName("user2");// 缓存全部清空，访问到数据库
        demo = parentMapper.selectByName(name_zs);
    }
}
