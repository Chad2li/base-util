package io.github.chad2li.baseutil.util;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chad on 2016/8/11.
 */
public class MockSet
{
    /**
     * 根据{@code memberValues}的类型，对{@code target}对象设置属性
     * NOTE: 如果有多个属性的Class相同，则会被{@code memberValues}中最后一个对应类型值覆盖
     *
     * @param target        类的实例，在该实例上设置属性
     * @param memeberValues 属性
     */
    public static void set(Object target, Object... memeberValues)
    {
        Map<String, Field> fs = ReflectUtils.fields(target.getClass());

        Map.Entry<String, Field> entry = null;
        for (Object m : memeberValues)
        {
            for (Iterator<Map.Entry<String, Field>> it = fs.entrySet().iterator(); it.hasNext(); )
            {
                entry = it.next();

                if (entry.getValue().getType().isInstance(m))
                    ReflectUtils.setValue(target, entry.getKey(), m);
            }
        }
    }

    public static void set(Object target, String memberName, Object memberVal)
    {
        if (null == target)
            throw new RuntimeException("target cannot null");
        if (StringUtils.isNull(memberName))
            throw new RuntimeException("member name cannot be null");
        if (null == memberVal)
            throw new RuntimeException("member value cannot null");

        ReflectUtils.setValue(target, memberName, memberVal);
    }
}
