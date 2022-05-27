package io.github.chad2li.baseutil.annot.nulls;

import java.lang.annotation.*;

/**
 * 标注一个参数或返回值不可能为空，可以不做空值检查
 *
 * @author chad
 * @date 2022/1/19 17:11
 * @since 1 by chad create
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Notnull {
}
