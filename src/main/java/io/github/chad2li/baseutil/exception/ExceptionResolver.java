package io.github.chad2li.baseutil.exception;


import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.chad2li.baseutil.exception.impl.AppException;
import io.github.chad2li.baseutil.exception.impl.BaseCode;
import io.github.chad2li.baseutil.http.vo.res.BaseRes;
import io.github.chad2li.baseutil.util.SpringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.MessageInterpolator;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Created by ly on 2015/1/11.
 *
 * @author chad
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
        writeLog(e);

        // 重置response
        resetResponse();

        // 封闭异常信息
        BaseRes resp = res(e);

        return resp;
    }

    /**
     * 后台 ajax 异常
     *
     * @param e
     * @return
     */
    public BaseRes<Void> res(Exception e) {
        BaseRes<Void> base = new BaseRes<>();

        String msg = null;
        if (e instanceof AppException) {
            // 应用自定义异常
            AppException infoE = (AppException) e;
            base.setCode(infoE.getCode().fullCode());
            msg = infoE.getMsg();
        } else if (isParamErr(e)) {
            base.setCode(BaseCode.PARAM_INVALID.fullCode());
            msg = parseParamErrDebugMsg(e);
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            // 不支持的请求方法
            base.setCode(BaseCode.REQ_METHOD_UNSUPPORTED.fullCode());
            msg = BaseCode.REQ_METHOD_UNSUPPORTED.msg();
        } else if (e instanceof NoHandlerFoundException) {
            // 404
            base.setCode(BaseCode.PATH_NOT_FOUND.fullCode());
            msg = BaseCode.PATH_NOT_FOUND.msg();
        } else {
            base.setCode(BaseCode.ERROR.fullCode());
            msg = BaseCode.ERROR.msg();
        }

        // 资源国际化
        if (null != messageSource && CharSequenceUtil.isNotEmpty(msg) && msg.matches("^[-\\w]+$")) {
            // spring默认使用 AcceptHeaderLocaleResolver
            Locale locale = null != localeResolver ? localeResolver.resolveLocale(SpringUtils.getRequest()) : Locale.getDefault();
            // 尝试从国际化资源文件中取值，取不到则用原值
            msg = messageSource.getMessage(msg, null, msg, locale);
        }

        base.setMsg(msg);

        return base;
    }

    /**
     * 重置 response
     *
     * @author chad
     * @since 1 by chad at 2023/9/5
     */
    private void resetResponse() {
        HttpServletResponse res = SpringUtils.getResponse();
        if (null == res) {
            return;
        }
        try {
            res.reset();
            res.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        } catch (Exception e) {
            log.error("reset response error", e);
        }
    }

    /**
     * 打印错误信息
     *
     * @param e
     */
    private void writeLog(Exception e) {
        if (null == e) {
            log.warn("exception without info");
            return;
        }

        if (e instanceof AppException) {
            writeLogByLevel((AppException) e);
        } else if (isParamErr(e)) {
            // 参数异常
            log.warn("WARN: {} {}", SpringUtils.getRequest().getRequestURI(), e.getMessage());
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            // 不支付的请求方法
            log.warn("WARN: [{}] not supported [{}] method", SpringUtils.getRequest().getRequestURI(), SpringUtils.getRequest().getMethod());
        } else if (e instanceof NoHandlerFoundException) {
            log.warn("WARN: [{}] not found", SpringUtils.getRequest().getRequestURI());
        } else {
            log.error("Unknown error", e);
        }
    }

    /**
     * 使用对应日志级别输出日志
     *
     * @param e app exception
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    private void writeLogByLevel(AppException e) {
        String logs = ExceptionUtil.stacktraceToOneLineString(e);
        switch (e.getCode().level()) {
            case TRACE:
                log.trace(logs);
                break;
            case DEBUG:
                log.debug(logs);
                break;
            case INFO:
                log.info(logs);
                break;
            case WARN:
                log.warn(logs);
                break;
            case ERROR:
                log.error(logs);
                break;
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
