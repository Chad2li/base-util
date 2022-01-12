package cn.lyjuan.base.mybatis.mapper;

import cn.lyjuan.base.mybatis.MyMapper;
import cn.lyjuan.base.mybatis.dao.DemoDao;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Param;

/**
 * @author chad
 * @date 2022/1/11 18:00
 * @since
 */
@CacheNamespace(flushInterval = 5000)
public interface IDemoParentMapper extends MyMapper<DemoDao> {
    DemoDao selectByName(@Param("name") String name);

    int updateAgeByName(@Param("name") String name, @Param("age") int age);
}
