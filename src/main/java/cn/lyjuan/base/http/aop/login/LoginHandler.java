package cn.lyjuan.base.http.aop.login;

import cn.lyjuan.base.exception.util.ErrUtils;
import cn.lyjuan.base.http.aop.login.annotation.Login;
import cn.lyjuan.base.http.aop.login.service.IUserService;
import cn.lyjuan.base.util.SpringUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 登录身份检查
 */
@Slf4j
@Aspect
@Order(LoginHandler.ORDER_LOGIN)
public class LoginHandler {
    public static final int ORDER_LOGIN = 1000;

    private IUserService userService;

    public LoginHandler(IUserService userService) {
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

        String token = SpringUtils.getRequest().getHeader("token");
        // 无token参数
        if (StringUtils.isNull(token)) {
            if (null != login)// 必须登录
                userService.errNeedLogin();
            else return;
        }

        IUserService.UserToken user = userService.user(token);

        if (null == user || !userService.isAccessValid(user.getTokenCreatetime())) {// token无效
            ErrUtils.appThrow(userService.errTokenInvalid());
        }
        // 无权访问
        String[] types = login.value();
        for (String type : types) {
            if (type.equalsIgnoreCase(user.getLoginType())) {
                // 有权限
                userService.setCache(user);
                return;
            }
        }
        // 无权访问
        ErrUtils.appThrow(userService.errIllegalPermission());

    }
}
