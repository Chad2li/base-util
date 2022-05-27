package io.github.chad2li.baseutil.http.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Login {
    /**
     * 匹配的登录身份
     *
     * @return
     */
    String[] value() default TYPE_USER;

    /**
     * 无须登录
     */
    String TYPE_UNLOGIN = "TYPE_UNLOGIN";

    /**
     * 普通用户，默认登录类型
     */
    String TYPE_USER = "TYPE_USER";
}