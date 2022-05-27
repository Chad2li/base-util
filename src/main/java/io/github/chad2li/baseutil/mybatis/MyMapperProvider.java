package io.github.chad2li.baseutil.mybatis;

import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.provider.ExampleProvider;

public class MyMapperProvider extends ExampleProvider {
    public MyMapperProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    /**
     * 根据Example条件进行查询，并仅返回一条记录，如果有多条记录则抛异常
     *
     * @param ms
     * @return
     */
    public String selectOneByExample(MappedStatement ms) {
        return selectByExample(ms);
    }
}
