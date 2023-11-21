package io.github.chad2li.baseutil.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import io.github.chad2li.baseutil.mybatis.enums.ICodeEnum;
import org.springframework.lang.Nullable;

/**
 * 枚举工具
 * Created by chad on 2016/11/4.
 */
public class EnumUtils {
    /**
     * 判断枚举是否相同
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean equals(Object obj1, Object obj2) {
        if (null == obj1 && null == obj2) return true;// 都为null，返回true

        if (null == obj1 || null == obj2) return false;// 只有一个为null，返回false

        if (obj1 == obj2) return true;

        if (valueOf(obj1).equalsIgnoreCase(valueOf(obj2))) return true;

        if (obj1.toString().equalsIgnoreCase(obj2.toString())) return true;// 字符串内容相同

        return false;
    }

    /**
     * 将枚举类型转为字符串表示
     *
     * @param enums
     * @return
     */
    public static String valueOf(Object enums) {
        if (null == enums) return "";

        return enums.toString();
    }

    /**
     * 解析枚举值
     * <pre>
     *     如果枚举值不存在，会抛异常
     * </pre>
     *
     * @param cls  枚举类
     * @param code code
     * @return ICodeEnum instant
     * @author chad
     * @since 1 by chad at 2023/11/7
     */
    public static <T extends ICodeEnum> T parse(Class<T> cls, String code) {
        return parse(cls, code, true);
    }

    /**
     * 解析值
     *
     * @param cls  ICodeEnum子类
     * @param code 值
     * @return 值或null
     * @author chad
     * @since 1 by chad at 2023/8/20
     */
    @Nullable
    public static <T extends ICodeEnum> T parse(Class<T> cls, String code,
                                                boolean errorNotExists) {
        Assert.notNull(cls);
        Assert.notEmpty(code);
        T[] enums = cls.getEnumConstants();
        if (ArrayUtil.isEmpty(enums)) {
            throw new NullPointerException("Need confirm class is enum:" + cls.getName());
        }

        for (T e : enums) {
            if (CharSequenceUtil.equalsIgnoreCase(e.code(), code)) {
                return e;
            }
        }
        if (!errorNotExists) {
            return null;
        }

        throw new NullPointerException(code + " not instant of " + cls.getName());
    }

    /**
     * 判断code是否为指定枚举值
     *
     * @param codeEnum 枚举
     * @param code     枚举值
     * @return true 相等；否则 false
     * @author chad
     * @since 1 by chad at 2023/11/7
     */
    public static <T extends ICodeEnum> boolean is(T codeEnum, String code) {
        return codeEnum.code().equalsIgnoreCase(code);
    }

    private EnumUtils() {
        // do nothing
    }
}
