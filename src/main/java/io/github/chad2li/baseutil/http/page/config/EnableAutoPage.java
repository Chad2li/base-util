package io.github.chad2li.baseutil.http.page.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启自动分页
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/19 16:55
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(AutoPageConfiguration.class)
public @interface EnableAutoPage {
}
