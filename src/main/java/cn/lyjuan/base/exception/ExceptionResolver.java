package cn.lyjuan.base.exception;


import cn.lyjuan.base.exception.impl.AppException;
import cn.lyjuan.base.exception.impl.BaseCode;
import cn.lyjuan.base.http.vo.res.BaseRes;
import cn.lyjuan.base.util.SpringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

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

            log.error("Error: " + info.getCode() + "-" + info.getLog(), info.getThrowable());
        } else if (isParamErr(e))// 缺少参数
        {
            log.error("Error: " + SpringUtils.getRequest().getRequestURI() + ": " + e.getMessage());
        } else
        {
            log.error("Error: " + e.getMessage(), e);
        }
    }

    /**
     * 是否为参数错误
     * @param e
     * @return      true参数错误
     */
    private boolean isParamErr(Exception e)
    {
        return e instanceof MissingServletRequestParameterException
                || e instanceof ServletRequestBindingException
                || e instanceof BindException

                ;
    }
}
