package io.github.chad2li.baseutil.exception.util;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.chad2li.baseutil.annot.nulls.Nullable;
import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.impl.AppException;
import io.github.chad2li.baseutil.exception.impl.BaseCode;

/**
 * 抛异常工具
 *
 * @author chad
 * @since 1 by chad at 2018/2/27 <br/>
 * 2 by chad at 2023/8/18: 1).废弃log参数;2).调用方throw更符合逻辑
 */
public class ErrUtils {
    /**
     * 封装异常信息
     *
     * @param code      状态码
     * @param msg       自定义消息，未指定使用 {@link IAppCode#msg()}
     * @param throwable 程序运行时的异常信息
     * @return app exception
     * @author chad
     * @since 1 by chad at 2023/8/18
     */
    public static <T> AppException appThrow(IAppCode code, String msg, Throwable throwable,
                                            @Nullable T data) {
        code = null != code ? code : BaseCode.ERROR;
        msg = CharSequenceUtil.isNotEmpty(msg) ? msg : code.msg();
        return new AppException(code, msg, throwable, data);
    }

    /**
     * 封装异常信息
     *
     * @return app exception
     * @author chad
     * @see ErrUtils#appThrow(IAppCode, String, Throwable, Object)
     * @since 1 by chad at 2023/8/18
     */
    public static AppException appThrow() {
        return appThrow(null, null, null, null);
    }

    /**
     * 封装异常信息
     *
     * @return app exception
     * @author chad
     * @see ErrUtils#appThrow(IAppCode, String, Throwable, Object)
     * @since 1 by chad at 2023/8/18
     */
    public static AppException appThrow(String msg) {
        return appThrow(null, msg, null, null);
    }

    /**
     * 封装异常信息
     *
     * @return app exception
     * @author chad
     * @see ErrUtils#appThrow(IAppCode, String, Throwable, Object)
     * @since 1 by chad at 2023/8/18
     */
    public static AppException appThrow(IAppCode code, Throwable t) {
        return appThrow(code, null, t, null);
    }

    /**
     * 封装异常信息
     *
     * @return app exception
     * @author chad
     * @see ErrUtils#appThrow(IAppCode, String, Throwable, Object)
     * @since 1 by chad at 2023/8/18
     */
    public static AppException appThrow(IAppCode code, String msg) {
        return appThrow(code, msg, null, null);
    }

    /**
     * 封装异常信息
     *
     * @return app exception
     * @author chad
     * @see ErrUtils#appThrow(IAppCode, String, Throwable, Object)
     * @since 1 by chad at 2023/8/18
     */
    public static AppException appThrow(IAppCode code) {
        return appThrow(code, null, null, null);
    }

    /**
     * 封装异常信息
     *
     * @return app exception
     * @author chad
     * @see ErrUtils#appThrow(IAppCode, String, Throwable, Object)
     * @since 1 by chad at 2023/8/18
     */
    public static AppException appThrow(String msg, Throwable throwable) {
        return appThrow(null, msg, throwable, null);
    }

    /**
     * 封装异常信息
     *
     * @return app exception
     * @author chad
     * @see ErrUtils#appThrow(IAppCode, String, Throwable, Object)
     * @since 1 by chad at 2023/8/18
     */
    public static AppException appThrow(Throwable throwable) {
        if (throwable instanceof AppException) {
            return (AppException) throwable;
        }
        return appThrow(null, null, throwable, null);
    }

    /**
     * 异常时响应携带数据
     *
     * @param code 异常码
     * @param data 数据
     * @return AppException
     * @author chad
     * @since 1 by chad at 2024/6/30
     */
    public static <T> AppException appThrowData(IAppCode code, T data) {
        return appThrow(code, null, null, data);
    }
}
