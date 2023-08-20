package io.github.chad2li.baseutil.mybatis.enums;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import org.springframework.lang.Nullable;

/**
 * 数字与枚举值转换接口
 * 在保存入库时，使用数字值{@code code}代替字符
 * 在取出时，使用枚举值代替数字
 */
public interface ICodeEnum {
    /**
     * 值
     *
     * @return
     */
    String code();

    /**
     * 显示名称
     *
     * @return
     */
    String display();

    /**
     * 判断是否是指定的类型
     *
     * @param codeEnum
     * @return
     */
    default boolean is(ICodeEnum codeEnum) {
        return this == codeEnum;
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
    static <T extends ICodeEnum> T parse(Class<T> cls, String code) {
        T[] enums = cls.getEnumConstants();
        if (ArrayUtil.isEmpty(enums))
            return null;

        for (T e : enums) {
            if (CharSequenceUtil.equalsIgnoreCase(e.code(), code)) {
                return e;
            }
        }
        return null;
    }
}
