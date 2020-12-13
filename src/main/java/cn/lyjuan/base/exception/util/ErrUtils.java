package cn.lyjuan.base.exception.util;

import cn.lyjuan.base.exception.IAppCode;
import cn.lyjuan.base.exception.impl.AppException;
import cn.lyjuan.base.exception.impl.BaseCode;

/**
 * 抛异常工具
 */
public class ErrUtils {
//    public static void appThrow() {
//        appThrow(null, null, null, null);
//    }

    public static void appThrow(IAppCode code, Throwable t) {
        appThrow(code, null, t);
    }

    public static void appThrow(IAppCode code, String log) {
        appThrow(code, log, null);
    }

    public static void appThrow(IAppCode code) {
        appThrow(code, null, null);
    }

    public static void appThrow(IAppCode code, String log, Throwable t) {
        // todo 未实现国际化
        appThrow(code, code.msg(), log, t);
    }


    /**
     * 业务异常
     *
     * @param code
     * @param msg
     * @param log
     */
    public static void appThrow(IAppCode code, String msg, String log, Throwable throwable) {
        throw new AppException(IAppCode.fullCode(code), msg, log, throwable);
    }


    public static void appThrow(String msg, String log, Throwable throwable) {
        appThrow(BaseCode.ERROR, msg, log, throwable);
    }

    public static void appThrow(String msg, Throwable throwable) {
        appThrow(BaseCode.ERROR, msg, null, throwable);
    }

    public static void appThrow(Throwable throwable) {
        appThrow(BaseCode.ERROR, null, null, throwable);
    }

}
