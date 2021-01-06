package cn.lyjuan.base.http.aop;

import cn.lyjuan.base.exception.impl.BaseCode;
import cn.lyjuan.base.exception.util.ErrUtils;
import cn.lyjuan.base.http.aop.service.IHeaderService;
import cn.lyjuan.base.http.aop.service.ISecureService;
import cn.lyjuan.base.http.filter.FilterProperties;
import cn.lyjuan.base.http.filter.HeaderFilter;
import cn.lyjuan.base.util.DateUtils;
import cn.lyjuan.base.util.SpringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;

/**
 * 接口安全验证
 */
@Slf4j
@Data
@Aspect
@Order(SecureAopHandler.ORDER)
public class SecureAopHandler {
    public static final int ORDER = 10000;

    /**
     * 测试环境标识
     */
    protected boolean isDebug = false;

    protected IHeaderService headerService;

    private ISecureService secureService;

    private FilterProperties filterProperties;
    /**
     * 时间戳超时的秒数，默认5分钟
     */
    protected int timestampTimeoutSeconds = 5 * 60;

    public SecureAopHandler(IHeaderService headerService, ISecureService secureService, int timestampTimeoutSeconds) {
        this.headerService = headerService;
        this.secureService = secureService;
        this.timestampTimeoutSeconds = timestampTimeoutSeconds;
    }

    /**
     * 对所有的 controller二级包下面的所有类所有方法进行拦截
     */
    @Pointcut("(@within(org.springframework.web.bind.annotation.RestController)" +
            " || @within(org.springframework.stereotype.Controller))" +
            " && execution(public * *(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void handler(JoinPoint jp) {
        if (FilterProperties.isSkip(this.filterProperties, SpringUtils.getRequest().getRequestURI()))
            return;

        if (isDebug) {// 测试环境跳过安全认证
            log.warn("Skip secure handler for debug");
            return;
        }
        IHeaderService.AHeaderParam header = headerService.cache();
        // timestamp丢弃超时
        long duration = DateUtils.time2long(LocalDateTime.now()) - header.getTimestamp();
        duration /= 1000;
        if (duration > timestampTimeoutSeconds) {
            ErrUtils.appThrow(BaseCode.TIMESTAMP_TIMEOUT);
        }
        // requestId防重
        long ttl = secureService.ttl(header.getRequestId());
        if (-2 != ttl)
            ErrUtils.appThrow(BaseCode.REQUESTID_DUPLICATE);
        // 缓存
        secureService.cache(header.getRequestId(), timestampTimeoutSeconds + 10);
        if (log.isDebugEnabled())
            log.debug("cache requestId: {}", header.getRequestId());
    }
}
