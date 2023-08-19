package io.github.chad2li.baseutil.exception;

import org.slf4j.event.Level;

/**
 * 状态码接口
 * @author chad
 * @since 1 by chad 2018/2/17
 */
public interface IAppCode {
    /**
     * 状态码所属模块
     * @return module
     * @author chad
     * @since 1 by chad at 2018/2/27
     */
    IAppModuleEnum module();

    /**
     * 状态码
     * @return code
     * @author chad
     * @since 1 by chad at 2018/2/27
     */
    String code();

    /**
     * 获取国际化消息 key
     * @return code
     * @author chad
     * @since 1 by chad at 2018/2/27
     */
    String msg();

    /**
     * 日志级别，自定义异常日志的输出级别，默认为 {@link Level#INFO}
     *
     * @return Level
     * @author chad
     * @since 1 by chad at 2023/8/18
     */
    Level level();

    /**
     * 模块化的状态码
     *
     * @return module()-code()
     * @author chad
     * @since 1 by chad at 2023/8/18
     */
    default String fullCode() {
        return this.module() + "_" + this.code();
    }

    /**
     * @date 2023/8/18 23:02
     * @author chad
     * @see IAppCode#fullCode()
     * @since 1 by chad at 2023/8/18
     * @deprecated {@link IAppCode#fullCode()}
     */
    @Deprecated
    static String fullCode(IAppCode code) {
        return code.module() + "_" + code.code();
    }
}
