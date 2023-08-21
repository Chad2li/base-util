package io.github.chad2li.baseutil.http.aop;

import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.util.ErrUtils;
import io.github.chad2li.baseutil.http.aop.annotation.Login;
import io.github.chad2li.baseutil.http.aop.service.IHeaderService;
import io.github.chad2li.baseutil.http.aop.service.IUserService;
import io.github.chad2li.baseutil.http.filter.FilterProperties;
import io.github.chad2li.baseutil.util.SpringUtils;
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
public class LoginAopHandler<H extends IHeaderService.AHeaderParam> {
    public static final int ORDER = SignAopHandler.ORDER - 1;

    public static final String USER_SERVICE_NAME = "loginHandlerUserServiceImpl";

    private IUserService userService;

    private IHeaderService<H> headerService;

    private FilterProperties filterProperties;

    public LoginAopHandler(IUserService userService, IHeaderService<H> headerService) {
        this.userService = userService;
        this.headerService = headerService;
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
        boolean mustLogin = mustLogin(login);

        H header = headerService.cache();
        // 没有用户标识
        if (!headerService.hasUserId()) {
            if (!mustLogin)
                return;
            // 必须登录
            IAppCode code = userService.errNeedLogin();
            throw ErrUtils.appThrow(code);
        }

        IUserService.UserToken user = userService.user(headerService.cache());
        // token无效
        if (null == user || !userService.isAccessValid(user)) {
            // 无须登录权限
            if (!mustLogin) return;
            // 必须登录
            throw ErrUtils.appThrow(userService.errTokenInvalid());
        }
        // token有效
        // 接口无需权限
        if (!mustLogin) {
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
            throw ErrUtils.appThrow(userService.errIllegalPermission());
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
        throw ErrUtils.appThrow(userService.errIllegalPermission());
    }

    /**
     * 是否必须登录权限
     *
     * @param login
     * @return true 必须登录权限
     */
    public static boolean mustLogin(Login login) {
        boolean mustLogin = null != login;
        if (mustLogin) {
            for (String l : login.value()) {
                if (Login.TYPE_UNLOGIN.equalsIgnoreCase(l))
                    mustLogin = false;
            }
        }
        return mustLogin;
    }
}
