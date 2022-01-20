package cn.lyjuan.base.annot.nulls;

import java.lang.annotation.*;

/**
 * 标识一个参数或返回值可能为 null
 *
 * @author chad
 * @date 2022/1/19 17:07
 * @since 1 by chad create
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Nullable {
}