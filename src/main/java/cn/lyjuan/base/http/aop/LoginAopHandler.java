package cn.lyjuan.base.http.aop;

import cn.lyjuan.base.exception.IAppCode;
import cn.lyjuan.base.exception.util.ErrUtils;
import cn.lyjuan.base.http.aop.annotation.Login;
import cn.lyjuan.base.http.aop.service.IHeaderService;
import cn.lyjuan.base.http.aop.service.IUserService;
import cn.lyjuan.base.http.filter.FilterProperties;
import cn.lyjuan.base.util.SpringUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 登录身份检查
 */
@Slf4j
@Data
@Aspect
@Order(LoginAopHandler.ORDER)
public class LoginAopHandler {
    public static final int ORDER = SignAopHandler.ORDER - 1;

    public static final String USER_SERVICE_NAME = "loginHandlerUserServiceImpl";

    private IUserService userService;

    private IHeaderService<? extends IHeaderService.AHeaderParam> headerService;

    private FilterProperties filterProperties;

    public LoginAopHandler(IUserService userService) {
        this.userService = userService;
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
    public void handle(JoinPoint jp) {
        if (FilterProperties.isSkip(this.filterProperties, SpringUtils.getRequest().getRequestURI()))
            return;

        log.debug("login handler");
        // 获取类对象
        Object target = jp.getTarget();

        Login login = null;

        // 获取方法注解
        Signature sign = jp.getSignature();
        if (MethodSignature.class.isInstance(sign)) {
            MethodSignature ms = (MethodSignature) sign;
            login = ms.getMethod().getDeclaredAnnotation(Login.class);
        }
        // 获取类注解，方法上注解的优先级高
        if (null == login)
            login = target.getClass().getDeclaredAnnotation(Login.class);

        // 登录检查
//        boolean mustLogin = null != login && login.value();

        // 没有用户标识
        if (!headerService.hasUserId()) {
            if (null == login)
                return;
            // 标识无须登录
            for (String l : login.value()) {
                if (Login.TYPE_UNLOGIN.equalsIgnoreCase(l))
                    return;
            }
            // 必须登录
            IAppCode code = userService.errNeedLogin();
            ErrUtils.appThrow(code);
        }

        IUserService.UserToken user = userService.user(headerService.cache());
        // token无效
        if (null == user || !userService.isAccessValid(user)) {
            ErrUtils.appThrow(userService.errTokenInvalid());
        }
        // 接口无需权限
        if (null == login) {
            userService.setCache(user);
            return;
        }
        String[] types = login.value();
        // 仅登录即可
        if (types.length == 1 && Login.TYPE_USER.equalsIgnoreCase(types[0])) {
            userService.setCache(user);
            return;
        }
        // 权限判断
        List<String> loginTypes = user.getLoginTypes();
        if (CollectionUtils.isEmpty(loginTypes)) {// 无权访问
            ErrUtils.appThrow(userService.errIllegalPermission());
        }
        for (String type : types) {
            for (String userType : loginTypes) {
                if (type.equalsIgnoreCase(userType)) {
                    // 有权限
                    userService.setCache(user);
                    return;
                }
            }
        }
        // 无权访问
        ErrUtils.appThrow(userService.errIllegalPermission());
    }
}
