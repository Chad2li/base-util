package cn.lyjuan.base.exception.util;

import cn.lyjuan.base.exception.IAppCode;
import cn.lyjuan.base.exception.impl.AppException;

/**
 * 抛异常工具
 *
 */
public class ErrUtils
{
    public static void appThrow()
    {
        appThrow(null, null, null, null);
    }

    public static void appThrow(IAppCode code, Throwable t)
    {
        appThrow(code, null, t);
    }

    public static void appThrow(IAppCode code, String log)
    {
        appThrow(code, log, null);
    }

    public static void appThrow(IAppCode code)
    {
        appThrow(code, null, null);
    }

    public static void appThrow(IAppCode code, String log, Throwable t)
    {
        // todo 未实现国际化
        appThrow(IAppCode.fullCode(code), code.msg(), log, t);
    }


    /**
     * 业务异常
     *
     * @param code
     * @param msg
     * @param log
     */
    private static void appThrow(String code, String msg, String log, Throwable throwable)
    {
        throw new AppException(code, msg, log, throwable);
    }


    public static void appThrow(String msg, String log, Throwable throwable)
    {
        appThrow(null, msg, log, throwable);
    }

    public static void appThrow(String msg, Throwable throwable)
    {
        appThrow(null, msg, null, throwable);
    }

    public static void appThrow(Throwable throwable)
    {
        appThrow(null, null, null, throwable);
    }
}
