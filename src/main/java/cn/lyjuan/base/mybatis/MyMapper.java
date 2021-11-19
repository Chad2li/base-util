package cn.lyjuan.base.mybatis;


import org.apache.ibatis.annotations.SelectProvider;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * 用于以后扩展
 *
 * @param <T>
 */
public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T> {
    /**
     * 根据Example条件进行查询，并仅返回一条记录，如果有多条记录则抛异常
     *
     * @param example
     * @return
     */
    @SelectProvider(type = MyMapperProvider.class, method = "dynamicSQL")
    T selectOneByExample(Object example);
}
