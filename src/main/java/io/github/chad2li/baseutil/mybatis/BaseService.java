package io.github.chad2li.baseutil.mybatis;


import io.github.chad2li.baseutil.util.ReflectUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class BaseService<T> {
    @Autowired
    protected MyMapper<T> mapper;

    public void insert(T entity) {
        mapper.insert(entity);
    }

    public void insertSelective(T entity) {
        mapper.insertSelective(entity);
    }

    public int insertList(List<T> list) {
        return mapper.insertList(list);
    }

    public T queryById(Integer id) {
        return (T) mapper.selectByPrimaryKey(id);
    }

    public boolean exist(Integer id) {
        return mapper.existsWithPrimaryKey(id);
    }

    public void updateWithoutNull(T t) {
        mapper.updateByPrimaryKeySelective(t);
    }

    /**
     * 使用简单的 and key = val 拼接 where 查询
     *
     * @param map
     */
    public List<T> selectByExample(Map<String, Object> map) {
        if (null == map || map.isEmpty())
            return new ArrayList<>();

        Class currGenericity = ReflectUtils.getGenericityClass(this.getClass(), 0);

        Example.Criteria criteria = new Example(currGenericity)
                .createCriteria();

        for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            criteria.andEqualTo(entry.getKey(), entry.getValue());
        }

        return mapper.selectByExample(currGenericity);
    }


    /**
     * 使用简单的 and key = val 拼接 where 查询
     *
     * @return 如果找不到，返回null，如果有多个，抛出 impl.AppException异常
     */
    public T selectOneByExample(Example example) {
        T t = mapper.selectOneByExample(example);
        return t;
    }

    public T selectById(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    public List<T> selectAll() {
        return mapper.selectAll();
    }

    public List<T> selectAllByPage(Integer pn, Integer ps) {
        PageHelper.startPage(pn, ps);

        return mapper.selectAll();
    }
}
