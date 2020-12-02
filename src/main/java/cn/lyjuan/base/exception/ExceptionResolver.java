package cn.lyjuan.base.exception;


import cn.lyjuan.base.exception.impl.AppException;
import cn.lyjuan.base.exception.impl.BaseCode;
import cn.lyjuan.base.http.vo.res.BaseRes;
import cn.lyjuan.base.util.SpringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ly on 2015/1/11.
 */
@ControllerAdvice
@ResponseBody
public class ExceptionResolver
{
    private static Logger log = LogManager.getLogger(ExceptionResolver.class.getName());

    @ExceptionHandler(Throwable.class)
    public Object doResolveException(Exception e)
    {
        logExce(e);// 打印日志

        BaseRes resp = ajaxExce(e);// 封闭异常信息

        return resp;
    }

    /**
     * 后台 ajax 异常
     *
     * @param e
     * @return
     */
    public BaseRes ajaxExce(Exception e)
    {
        BaseRes base = new BaseRes();

        if (e instanceof AppException)
        {
            AppException infoE = (AppException) e;

            base.setCode(infoE.getCode());
            base.setMsg(infoE.getMsg());
        } else if (isParamErr(e))
        {
            base.setCode(IAppCode.fullCode(BaseCode.PARAM_INVALID));
            base.setMsg(BaseCode.PARAM_INVALID.msg());
        } else if (e instanceof HttpRequestMethodNotSupportedException)// 不支付的请求方法
        {
            base.setCode(IAppCode.fullCode(BaseCode.REQ_METHOD_UNSUPPORTED));
            base.setMsg(BaseCode.REQ_METHOD_UNSUPPORTED.msg());
        } else if (e instanceof NoHandlerFoundException)// 404
        {
            base.setCode(IAppCode.fullCode(BaseCode.PATH_NOT_FOUND));
            base.setMsg(BaseCode.PATH_NOT_FOUND.msg());
        } else
        {
            base.setCode(IAppCode.fullCode(BaseCode.ERROR));
            base.setMsg(BaseCode.ERROR.msg());
        }

        return base;
    }

    /**
     * 打印错误信息
     *
     * @param e
     */
    private void logExce(Exception e)
    {
        if (null == e)
            return;

        if (e instanceof AppException)
        {
            AppException info = (AppException) e;

            log.error("WARN: " + info.getCode() + "-" + info.getLog(), info.getThrowable());
        } else if (isParamErr(e))// 缺少参数
        {
            log.error("WARN: " + SpringUtils.getRequest().getRequestURI() + ": " + e.getMessage());
        } else if (e instanceof HttpRequestMethodNotSupportedException)// 不支付的请求方法
        {
            log.warn("WARN: [{}] not supported [{}] method", SpringUtils.getRequest().getRequestURI(), SpringUtils.getRequest().getMethod());
        } else if (e instanceof NoHandlerFoundException)
        {
            log.warn("WARN: [[]] not found", SpringUtils.getRequest().getRequestURI());
        } else
        {
            log.error("Error: " + e.getMessage(), e);
        }
    }

    /**
     * 是否为参数错误
     *
     * @param e
     * @return true参数错误
     */
    private boolean isParamErr(Exception e)
    {
        return e instanceof MissingServletRequestParameterException
                || e instanceof ServletRequestBindingException
                || e instanceof BindException
                || e instanceof MethodArgumentTypeMismatchException

                ;
    }
}
