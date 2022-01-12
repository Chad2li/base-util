//package cn.lyjuan.base.mybatis.mapper;
//
//import cn.lyjuan.base.mybatis.dao.DemoDao;
//import org.apache.ibatis.annotations.CacheNamespaceRef;
//import org.apache.ibatis.annotations.Param;
//
///**
// * 继承另一个 mapper 的缓存命名空间
// *
// * @author chad
// * @date 2022/1/11 18:00
// * @since 1 by chad create
// */
//@CacheNamespaceRef(value = IDemoParentMapper.class)
//public interface IDemoMapper {
//
//    DemoDao selectByNameAge(@Param("name") String name, @Param("age") int age);
//}
