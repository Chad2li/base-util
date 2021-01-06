package cn.lyjuan.base.redis;

import cn.lyjuan.base.mybatis.enums.ICodeEnum;
import cn.lyjuan.base.mybatis.typehander.CodeTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.Set;

/**
 * 实现在 mybatis 加载mapper前 注入自定义的 typeHandler
 */
@Slf4j
public class CustomSqlSessionFactoryBean extends SqlSessionFactoryBean {

    @Override
    protected SqlSessionFactory buildSqlSessionFactory() throws IOException {
        SqlSessionFactory factory = super.buildSqlSessionFactory();
        TypeHandlerRegistry registry = factory.getConfiguration().getTypeHandlerRegistry();

        Reflections reflections = new Reflections(ICodeEnum.class.getPackage().getName());
        Set<Class<? extends ICodeEnum>> classes = reflections.getSubTypesOf(ICodeEnum.class);
        if (null == classes || classes.isEmpty()) {
            log.info("Mybatis type handler registered empty");
            return factory;
        }

        for (Class<? extends ICodeEnum> cls : classes) {
            registry.register(cls, CodeTypeHandler.class);
            log.info("Mybatis registered CustomEnumHandler for enum {}", cls.getName());
        }

        return factory;
    }
}
