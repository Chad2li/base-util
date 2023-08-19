package io.github.chad2li.baseutil.http.page.config;

import io.github.chad2li.baseutil.http.page.AutoPageAspect;
import org.springframework.context.annotation.Bean;

/**
 * auto page 配置
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/19 16:56
 */
public class AutoPageConfiguration {
    @Bean
    public AutoPageAspect autoPageAspect() {
        return new AutoPageAspect();
    }
}
