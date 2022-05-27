package io.github.chad2li.baseutil.http.filter;

import io.github.chad2li.baseutil.util.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 过滤器配置属性
 */
@Data
@NoArgsConstructor
public class FilterProperties {
    /**
     * 跳过的Url模式
     */
    private String exclusionUrlPattern;

    /**
     * 是否跳过path
     * @param path
     * @return
     */
    public boolean isSkip(String path) {
        if (StringUtils.isNull(path)) return false;
        if (StringUtils.isNull(exclusionUrlPattern)) return false;

        String[] urls = exclusionUrlPattern.split(",");

        for (String url : urls) {
            if (StringUtils.isNull(url)) continue;

            boolean isMatch = Pattern.matches(url, path);

            if (isMatch) return true;
        }

        return false;
    }

    public static boolean isSkip(FilterProperties properties, String path) {
        if (null == properties) return false;

        return properties.isSkip(path);
    }
}
