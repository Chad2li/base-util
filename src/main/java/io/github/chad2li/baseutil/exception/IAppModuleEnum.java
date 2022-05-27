package io.github.chad2li.baseutil.exception;

import io.github.chad2li.baseutil.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 用于确定模块类型
 *
 * @author chad
 */
public interface IAppModuleEnum {
    /**
     * 默认的国际化资源相对路径
     */
    String MESSAGE_RESOURCE_DIRECTORY = "message/";

    /**
     * 显示名称，用于显示资源国际化名称
     *
     * @return java.lang.String
     * @date 2022/1/6 13:06
     * @author chad
     * @since
     */
    String displayName();

    /**
     * 完整的模块国际化资源classpath路径，如：
     * <p>
     * classpath:message/other_common.properties
     * </p>
     *
     * @return java.lang.String
     * @date 2022/1/6 09:22
     * @author chad
     * @since 1 by chad create
     */
    default List<String> messageResourceFileNames(Locale... locales) {
        List<String> rs = new ArrayList<>();
        // 默认的
        rs.add(messageResourceFileName(null));

        if (!StringUtils.isNull(locales)) {
            for (Locale l : locales) {
                rs.add(messageResourceFileName(l));
            }
        }
        return rs;
    }

    /**
     * 生成对应国际化资源文件
     *
     * @param locale 语言环境
     * @return java.lang.String
     * @date 2022/1/6 11:19
     * @author chad
     * @since 1 by chad create
     */
    default String messageResourceFileName(Locale locale) {
        String fileName = "classpath:" + MESSAGE_RESOURCE_DIRECTORY + this.displayName().toLowerCase();
        if (null != locale) {
            // 默认
            fileName += "_" + locale.getLanguage();
        }

        return fileName;
    }
}
