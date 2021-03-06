package io.github.chad2li.baseutil.mybatis.enums;

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
    int code();

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

    static <T extends ICodeEnum> T parse(Class<T> cls, int code) {
        T[] enums = cls.getEnumConstants();
        if (null == enums || enums.length < 1)
            return null;

        for (T e : enums) {
            if (e.code() == code) return e;
        }
        return null;
    }
}
