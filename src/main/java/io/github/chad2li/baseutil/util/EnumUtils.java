package io.github.chad2li.baseutil.util;

/**
 * 枚举工具
 * Created by chad on 2016/11/4.
 */
public class EnumUtils
{
    /**
     * 判断枚举是否相同
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean equals(Object obj1, Object obj2)
    {
        if (null == obj1 && null == obj2) return true;// 都为null，返回true

        if (null == obj1 || null == obj2) return false;// 只有一个为null，返回false

        if (obj1 == obj2) return true;

        if (valueOf(obj1).equalsIgnoreCase(valueOf(obj2))) return true;

        if (obj1.toString().equalsIgnoreCase(obj2.toString())) return true;// 字符串内容相同

        return false;
    }

    /**
     * 将枚举类型转为字符串表示
     * @param enums
     * @return
     */
    public static String valueOf(Object enums)
    {
        if (null == enums) return "";

        return enums.toString();
    }
}
