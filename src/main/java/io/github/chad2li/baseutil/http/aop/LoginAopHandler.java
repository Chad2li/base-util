package io.github.chad2li.baseutil.http.aop;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.util.ErrUtils;
import io.github.chad2li.baseutil.http.aop.annotation.Login;
import io.github.chad2li.baseutil.http.aop.service.IHeaderService;
import io.github.chad2li.baseutil.http.aop.service.IUserService;
import io.github.chad2li.baseutil.http.filter.FilterProperties;
import io.github.chad2li.baseutil.util.SpringUtils;
import lombok.AllArgsConstructor;
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

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 登录身份检查
 */
@Slf4j
@Data
@Aspect
@AllArgsConstructor
@Order(LoginAopHandler.ORDER)
public class LoginAopHandler<H extends IHeaderService.AHeaderParam> {
    public static final int ORDER = SignAopHandler.ORDER - 1;

    public static final String USER_SERVICE_NAME = "loginHandlerUserServiceImpl";

    private IUserService userService;

//    private IHeaderService<H> headerService;

    private FilterProperties filterProperties;
    /**
     * 请求中的token名
     */
    private String headerTokenName;

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
        HttpServletRequest req = SpringUtils.getRequest();
        if (FilterProperties.isSkip(this.filterProperties, req.getRequestURI())) {
            return;
        }

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
        String token = req.getHeader(headerTokenName);
        // 没有用户标识
        if (CharSequenceUtil.isEmpty(token)) {
            if (!mustLogin) {
                return;
            }
            // 必须登录
            IAppCode code = userService.needLogin();
            throw ErrUtils.appThrow(code);
        }

        IUserService.UserToken user = userService.user(token);
        // token无效
        if (null == user || !userService.isAccessValid(user)) {
            // 无须登录权限
            if (!mustLogin) return;
            // 必须登录
            throw ErrUtils.appThrow(userService.tokenInvalid());
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
            throw ErrUtils.appThrow(userService.illegalPermission());
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
        throw ErrUtils.appThrow(userService.illegalPermission());
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
