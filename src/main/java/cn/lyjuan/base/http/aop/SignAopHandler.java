package cn.lyjuan.base.http.aop;

import cn.lyjuan.base.exception.impl.BaseCode;
import cn.lyjuan.base.exception.util.ErrUtils;
import cn.lyjuan.base.http.aop.service.IHeaderService;
import cn.lyjuan.base.http.aop.service.ISignService;
import cn.lyjuan.base.http.filter.FilterProperties;
import cn.lyjuan.base.util.HttpSignUtil;
import cn.lyjuan.base.util.SpringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Aspect
@Data
@Order(SignAopHandler.ORDER)
public class SignAopHandler<H extends IHeaderService.AHeaderParam> {
    public static final int ORDER = SecureAopHandler.ORDER + 1;

    protected boolean isDebug = false;

    protected IHeaderService<H> headerService;

    protected ISignService<H> signService;

    private FilterProperties filterProperties;

    public SignAopHandler(IHeaderService<H> headerService, ISignService signService) {
        this.headerService = headerService;
        this.signService = signService;
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
        // 不可在此处通过PropertiesConfig.IS_DEBUG跳过，下面有用到 body cache

        HttpServletRequest req = SpringUtils.getRequest();
        // 获取header参数
        H header = headerService.cache();
        // 获取app信息
        ISignService.App app = signService.app(header);
        if (null == app || !app.isValid())
            ErrUtils.appThrow(BaseCode.APP_ID_INVALID);
        // 获取url参数
        Map<String, String> get = SpringUtils.getParam(req);
        // 获取body参数，get方法不获取
        String body = null;
        if (!"GET".equalsIgnoreCase(req.getMethod()))
            body = new String(((ContentCachingRequestWrapper) req).getContentAsByteArray());
        // 拼接签名字符串
        String signStr = HttpSignUtil.appendSign(req.getRequestURI(), req.getMethod(), header, get, body, app.getMd5key());

        // md5签名
        String sign = HttpSignUtil.md5sign(signStr);

        // 自行处理
        signService.signParam(header, get, body, app.getMd5key(), signStr, sign);

        if (sign.equalsIgnoreCase(header.getSign()))
            return;

        if (log.isDebugEnabled()) {
            log.debug("signStr: {}", signStr);
            log.debug("sign need {} but {}", sign, header.getSign());
        }
        if (isDebug)// 测试环境跳过签名不匹配
            return;
        ErrUtils.appThrow(BaseCode.SIGN_INVALID);
    }
}