package cn.lyjuan.base.exception;


import cn.lyjuan.base.exception.impl.AppException;
import cn.lyjuan.base.exception.impl.BaseCode;
import cn.lyjuan.base.http.vo.res.BaseRes;
import cn.lyjuan.base.util.SpringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.MessageInterpolator;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

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
     * 国际化-资源
     */
    private MessageSource messageSource;
    private MessageInterpolator messageInterpolator;
    /**
     * 国际化-语言环境解析器
     */
    private LocaleResolver localeResolver;

    public ExceptionResolver() {
    }

    public ExceptionResolver(MessageSource messageSource, LocaleResolver localeResolver) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
    }

    /**
     * 拦截所有 Exception
     * Note: 只能使用 Exception
     *
     * @param e
     * @return
     */
    @ExceptionHandler({Exception.class})
    public Object doResolveException(Exception e) {
        // 打印日志
        logExce(e);

        // 封闭异常信息
        BaseRes resp = ajaxExce(e);

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
            // 应用自定义异常
            AppException infoE = (AppException) e;
            base.setCode(infoE.getCode());
            if (null != messageSource) {
                // spring默认使用 AcceptHeaderLocaleResolver
                Locale locale = null != localeResolver ? localeResolver.resolveLocale(SpringUtils.getRequest()) : Locale.getDefault();
                String msg = infoE.getMsg();
                // 尝试从国际化资源文件中取值，取不到则用原值
                msg = messageSource.getMessage(msg, null, msg, locale);
                base.setMsg(msg);
            } else {
                base.setMsg(infoE.getMsg());
            }
        } else if (isParamErr(e)) {
            base.setCode(IAppCode.fullCode(BaseCode.PARAM_INVALID));
            base.setMsg(parseParamErrDebugMsg(e));
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            // 不支持的请求方法
            base.setCode(IAppCode.fullCode(BaseCode.REQ_METHOD_UNSUPPORTED));
            base.setMsg(BaseCode.REQ_METHOD_UNSUPPORTED.msg());
        } else if (e instanceof NoHandlerFoundException) {
            // 404
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
        if (null == e) {
            return;
        }

        if (e instanceof AppException) {
            AppException info = (AppException) e;

            log.warn("WARN: " + info.getCode() + "-" + info.getLog(), info.getThrowable());
        } else if (isParamErr(e))// 缺少参数
        {
            log.warn("WARN: " + SpringUtils.getRequest().getRequestURI() + ": " + e.getMessage());
        } else if (e instanceof HttpRequestMethodNotSupportedException)// 不支付的请求方法
        {
            log.warn("WARN: [{}] not supported [{}] method", SpringUtils.getRequest().getRequestURI(), SpringUtils.getRequest().getMethod());
        } else if (e instanceof NoHandlerFoundException) {
            log.warn("WARN: [{}] not found", SpringUtils.getRequest().getRequestURI());
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
                || e instanceof HttpMessageNotReadableException// 没有 request body

                ;
    }

    /**
     * 解析参数错误调试信息
     *
     * @param e
     * @return
     */
    private String parseParamErrDebugMsg(Exception e) {
        if (e instanceof BindException) {
            // 自行处理还是默认处理
            StringJoiner sj = new StringJoiner(",");
            for (ObjectError item : ((BindException) e).getAllErrors()) {
                String defMsg = item.getDefaultMessage();
                sj.add(defMsg);
            }
            return sj.toString();
        } else if (e instanceof ConstraintViolationException) {
            Set<ConstraintViolation<?>> set = ((ConstraintViolationException) e).getConstraintViolations();
            StringJoiner sj = new StringJoiner(",");
            for (ConstraintViolation s : set) {
                sj.add(s.getMessage());
            }
            return sj.toString();
        }
//        else {
//            if (isDebug) {
//                if (e instanceof HttpMessageNotReadableException) {
//                    return "Request body is missing";
//                }
//            }
//        }

        return BaseCode.PARAM_INVALID.msg();
    }
}
