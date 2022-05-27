package io.github.chad2li.baseutil.exception;

/**
 * 状态码接口
 */
public interface IAppCode {
    /**
     * 状态码所属模块
     *
     * @return
     */
    IAppModuleEnum module();

    /**
     * 获取状态码
     * todo code转string，增加模块标识
     *
     * @return
     */
    String code();

    /**
     * 获取国际化消息 key
     *
     * @return
     */
    String msg();

    static String fullCode(IAppCode code) {
        return code.module() + "_" + code.code();
    }
}
