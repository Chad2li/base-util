package cn.lyjuan.base.exception.impl;

import cn.lyjuan.base.exception.IAppException;
import cn.lyjuan.base.util.StringUtils;

/**
 * 业务逻辑异常，无法处理，返回状态码及信息
 */
public class AppException extends RuntimeException implements IAppException
{
    /**
     * 未知错误代码
     */
    public static final Integer UNKNOWN_ERR_CODE = 1;

    private String code;

    private String msg;

    private String log;

    private Throwable throwable;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public String getLog()
    {
        return log;
    }

    public void setLog(String log)
    {
        this.log = log;
    }

    public Throwable getThrowable()
    {
        return throwable;
    }

    public void setThrowable(Throwable throwable)
    {
        this.throwable = throwable;
    }

    @Override
    public String toString()
    {
        return "AppException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", log='" + log + '\'' +
                ", throwable=" + throwable +
                '}';
    }

    public AppException(String code, String msg, String log, Throwable throwable)
    {
        super(msg, throwable);
        this.setCode(null == code ? BaseCode.ERROR.code() : code);
        this.setMsg(StringUtils.isNull(msg) ? BaseCode.ERROR.msg() : msg);
        this.setLog(StringUtils.isNull(log) ? msg : log);
        this.setThrowable(throwable);
    }
}
