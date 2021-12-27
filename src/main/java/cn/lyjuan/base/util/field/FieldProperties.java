package cn.lyjuan.base.util.field;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性注解
 *
 * @author chad
 * @date 2021/12/22 12:00:00
 * @since 1 by chad at 2021/12/22 新增
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldProperties {
    /**
     * 字段名称，可以使用此字段更改生成json的字段名称
     *
     * @return
     */
    String name() default "";

    /**
     * 标题，字段的标题
     *
     * @return
     */
    String title() default "属性";

    /**
     * 类型 {@link ItemTypeEnum}，该字段的类型
     *
     * @return
     */
//    ItemTypeEnum type() default ItemTypeEnum.STRING;

    /**
     * 说明描述文字
     *
     * @return
     */
    String remark() default "";

    /**
     * 数字为最大值（包含），字符为最大长度（包含），-1表示不限制
     *
     * @return
     */
    int max() default -1;

    /**
     * 数字为最小值（包含），字符为最小长度（包含），-1表示不限制
     *
     * @return
     */
    int min() default -1;

    /**
     * 是否必填，true允许为空
     *
     * @return
     */
    boolean notNull() default false;

    /**
     * 升序排序，数值越大越靠后
     *
     * @return
     */
    int sort() default 0;
}
