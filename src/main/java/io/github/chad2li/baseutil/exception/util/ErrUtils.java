package io.github.chad2li.baseutil.exception.util;

import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.impl.AppException;
import io.github.chad2li.baseutil.exception.impl.BaseCode;
import io.github.chad2li.baseutil.util.StringUtils;

/**
 * 抛异常工具
 */
public class ErrUtils {
    public static void appThrow() {
        appThrow(null, null, null, null);
    }

    public static void appThrow(String log) {
        appThrow(null, null, log, null);
    }

    public static void appThrow(IAppCode code, Throwable t) {
        appThrow(code, null, null, t);
    }

    public static void appThrow(IAppCode code, String log) {
        appThrow(code, null, log, null);
    }

    public static void appThrow(IAppCode code) {
        appThrow(code, null, null, null);
    }

    public static void appThrow(IAppCode code, String log, Throwable t) {
        appThrow(code, null, log, t);
    }


    /**
     * 抛异常
     *
     * @param code
     * @param msg
     * @param log
     */
    public static void appThrow(IAppCode code, String msg, String log, Throwable throwable) {
        String codeStr = IAppCode.fullCode(null == code ? BaseCode.ERROR : code);
        if (StringUtils.isNull(msg)) {
            msg = null == code ? BaseCode.ERROR.msg() : code.msg();
        }
        log = StringUtils.isNull(log) ? msg : log;
        throw new AppException(codeStr, msg, log, throwable);
    }

    public static void appThrow(String msg, String log, Throwable throwable) {
        appThrow(null, msg, log, throwable);
    }

    public static void appThrow(String msg, Throwable throwable) {
        appThrow(null, msg, null, throwable);
    }

    public static void appThrow(Throwable throwable) {
        appThrow(null, null, null, throwable);
    }
}
