package cn.lyjuan.base.util;

import java.lang.reflect.Field;

/**
 * Created by chad on 2016/8/11.
 */
public class MockSet
{
    /**
     * 给对象设置相应类型的属性
     *
     * @param obj     类的实例，在该实例上设置属性
     * @param memeber 属性
     */
    public static void set(Object obj, Object memeber)
    {
        set(null, obj, memeber);
    }

    /**
     * 给对象设置相应类型的属性
     *
     * @param cls     属性所在的类
     * @param obj     类的实例，在该实例上设置属性
     * @param memeber 属性
     */
    public static void set(Class<?> cls, Object obj, Object memeber)
    {
        if (null == obj)
            throw new RuntimeException("object not null");
        if (null == memeber)
            throw new RuntimeException("member not null");
        cls = null == cls ? obj.getClass() : cls;

        Field[] fs = cls.getDeclaredFields();

        if (null == fs)
            throw new RuntimeException(obj.getClass().getSimpleName() + "have no any field");

        boolean setSucc = false;
        for (Field f : fs)
        {
            try
            {
                f.setAccessible(true);
                f.set(obj, memeber);
                setSucc = true;// 不抛异常就是成功
                break;
            } catch (Exception e)
            {
                // 压制
            }
        }

        if (!setSucc)
            throw new RuntimeException(obj.getClass().getSimpleName() + " have no field of "
                + memeber.getClass().getSimpleName() + " type");
    }
}
