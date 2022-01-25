package cn.lyjuan.base.mybatis.util;

import cn.lyjuan.base.util.StringUtils;

import javax.persistence.Table;

/**
 * Mybatis工具
 *
 * @author chad
 * @date 2022/1/24 09:58
 * @since 1 by chad create
 */
public class MybatisUtil {
    /**
     * 获取类上 {@link Table} 注解的 {@code name} 或 {@code schema}标识的表名
     *
     * @param cls 数据库实例类
     * @return java.lang.String 表名
     * @date 2022/1/24 10:02
     * @author chad
     * @since 1 by chad create
     */
    public static String getTableName(Class cls) {
        Table table = (Table) cls.getAnnotation(Table.class);
        if (null == table) {
            throw new IllegalStateException(cls.getName() + " have not Table annotation");
        }

        String name = table.name();
        if (StringUtils.isNull(name)) {
            name = table.schema();
        }
        if (StringUtils.isNull(name)) {
            throw new NullPointerException(cls.getName() + " Table annotation have not name or schema attribute");
        }
        return name;
    }
}
