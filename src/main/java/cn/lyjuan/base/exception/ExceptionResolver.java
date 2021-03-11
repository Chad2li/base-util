package cn.lyjuan.base.exception;


import cn.lyjuan.base.exception.impl.AppException;
import cn.lyjuan.base.exception.impl.BaseCode;
import cn.lyjuan.base.http.vo.res.BaseRes;
import cn.lyjuan.base.util.SpringUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

/**
 * Created by ly on 2015/1/11.
 */
@Slf4j
@Data
@RestControllerAdvice
@ResponseBody
public class ExceptionResolver {
    /**
     * 是否为测试环境
     */
    private boolean isDebug = false;

    /**
     * 拦截所有 Exception
     * Note: 只能使用 Exception
     *
     * @param e
     * @return
     */
    @ExceptionHandler({Exception.class})
    public Object doResolveException(Exception e) {
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
    public BaseRes ajaxExce(Exception e) {
        BaseRes base = new BaseRes();

        if (e instanceof AppException) {
            AppException infoE = (AppException) e;

            base.setCode(infoE.getCode());
            base.setMsg(infoE.getMsg());
        } else if (isParamErr(e)) {
            base.setCode(IAppCode.fullCode(BaseCode.PARAM_INVALID));
            if (!isDebug) {
                base.setMsg(BaseCode.PARAM_INVALID.msg());
            } else {
                base.setMsg(parseParamErrDebugMsg(e));
            }
        } else if (e instanceof HttpRequestMethodNotSupportedException)// 不支持的请求方法
        {
            base.setCode(IAppCode.fullCode(BaseCode.REQ_METHOD_UNSUPPORTED));
            base.setMsg(BaseCode.REQ_METHOD_UNSUPPORTED.msg());
        } else if (e instanceof NoHandlerFoundException)// 404
        {
            base.setCode(IAppCode.fullCode(BaseCode.PATH_NOT_FOUND));
            base.setMsg(BaseCode.PATH_NOT_FOUND.msg());
        } else {
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
    private void logExce(Exception e) {
        if (null == e)
            return;

        if (e instanceof AppException) {
            AppException info = (AppException) e;

            log.error("WARN: " + info.getCode() + "-" + info.getLog(), info.getThrowable());
        } else if (isParamErr(e))// 缺少参数
        {
            log.error("WARN: " + SpringUtils.getRequest().getRequestURI() + ": " + e.getMessage());
        } else if (e instanceof HttpRequestMethodNotSupportedException)// 不支付的请求方法
        {
            log.warn("WARN: [{}] not supported [{}] method", SpringUtils.getRequest().getRequestURI(), SpringUtils.getRequest().getMethod());
        } else if (e instanceof NoHandlerFoundException) {
            log.warn("WARN: [[]] not found", SpringUtils.getRequest().getRequestURI());
        } else {
            log.error("Error: " + e.getMessage(), e);
        }
    }

    /**
     * 是否为参数错误
     *
     * @param e
     * @return true参数错误
     */
    private boolean isParamErr(Exception e) {
        return e instanceof MissingServletRequestParameterException// 少参数
                || e instanceof ServletRequestBindingException// 少参数
                || e instanceof BindException// 少参数
                || e instanceof MethodArgumentTypeMismatchException// 少参数
                || e instanceof ConstraintViolationException// validation检验不通过
                || e instanceof HttpMessageNotReadableException//没有 request body

                ;
    }

    /**
     * 解析参数错误调试信息
     *
     * @param e
     * @return
     */
    private String parseParamErrDebugMsg(Exception e) {
        if (e instanceof MethodArgumentNotValidException) {
            StringBuilder sb = new StringBuilder();
            ((MethodArgumentNotValidException) e).getAllErrors().forEach(item -> {
                Object[] args = item.getArguments();
                if (!StringUtils.isNull(args)) {
                    sb.append("[");
                    for (Object o : args) {
                        if (o instanceof DefaultMessageSourceResolvable) {
                            sb.append(((DefaultMessageSourceResolvable) o).getDefaultMessage()).append(",");
                        }
                    }
                    if (sb.length() > 1) {
                        sb.deleteCharAt(sb.length() - 1);
                        sb.append("] ");
                    } else
                        sb.deleteCharAt(0);
                }
                String objName = item.getObjectName();
                String defMsg = item.getDefaultMessage();
                log.warn("objName:{} args:{} defMsg:{}", objName, StringUtils.toStr(args), defMsg);
                sb.append(defMsg).append(",");
            });
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } else if (e instanceof ConstraintViolationException) {
            return e.getMessage();
        } else if (e instanceof HttpMessageNotReadableException) {
            return "Request body is missing";
        }

        return e.getMessage();
    }
}
