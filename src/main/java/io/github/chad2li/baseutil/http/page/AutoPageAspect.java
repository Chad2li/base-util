package io.github.chad2li.baseutil.http.page;

import cn.hutool.core.util.ArrayUtil;
import io.github.chad2li.baseutil.http.vo.res.PagerReq;
import io.github.chad2li.baseutil.http.vo.res.PagerRes;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.lang.Nullable;

/**
 * 自动分页
 * <pre>
 *     1. 仅对controller层有效
 *     2. 请求的入参必须继承 {@link PagerReq}
 *     3. 请求的响应必须为 {@link PagerRes}
 * </pre>
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/19 14:56
 */
@Slf4j
@Aspect
public class AutoPageAspect {
    /**
     * 拦截所有 RestController类下的public方法
     *
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    @Pointcut("(" +
            "@within(org.springframework.web.bind.annotation.RestController) " +
            "|| @within(org.springframework.stereotype.Controller)" +
            ") && execution(public * *(..))")
    public void pointcut() {
        // do nothing
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        Object[] args = jp.getArgs();

        if (ArrayUtil.isEmpty(args)) {
            // 无入参
            return jp.proceed(args);
        }

        // 解析并设置入参分页信息
        setPageReq(args);

        Object result = null;
        try {
            // 执行业务逻辑
            result = jp.proceed(args);
            // 设置响应结果分页信息
            setPageRes(result);
        } finally {
            // 清除分页缓存
            clean();
        }
        return result;
    }

    public void setPageReq(Object[] args) {
        // 解析并设置分页信息
        for (Object arg : args) {
            if (!(arg instanceof PagerReq)) {
                // 非 PagerReq
                continue;
            }
            // 解析参数
            PagerReq pagerReq = (PagerReq) arg;
            // todo set page
            if (log.isDebugEnabled()) {
                log.debug("Page set, pn:{}, ps:{}", pagerReq.getPn(), pagerReq.getPs());
            }
            break;
        }
    }

    public void setPageRes(@Nullable Object result) {
        if (!(result instanceof PagerRes)) {
            // 非 PagerRes
            return;
        }


    }

    public void clean() {

    }
}
