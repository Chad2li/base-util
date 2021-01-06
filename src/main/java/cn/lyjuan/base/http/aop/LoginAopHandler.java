package cn.lyjuan.base.http.aop;

import cn.lyjuan.base.exception.util.ErrUtils;
import cn.lyjuan.base.http.aop.annotation.Login;
import cn.lyjuan.base.http.aop.service.IUserService;
import cn.lyjuan.base.http.filter.FilterProperties;
import cn.lyjuan.base.util.SpringUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;

/**
 * 登录身份检查
 */
@Slf4j
@Data
@Aspect
@Order(LoginAopHandler.ORDER)
public class LoginAopHandler {
    public static final int ORDER = SignAopHandler.ORDER;

    public static final String USER_SERVICE_NAME = "loginHandlerUserServiceImpl";

    private IUserService userService;

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

        String token = SpringUtils.getRequest().getHeader("token");
        // 无token参数
        if (StringUtils.isNull(token)) {
            if (null != login)// 必须登录
                userService.errNeedLogin();
            else return;
        }

        IUserService.UserToken user = userService.user(token);
        // token无效
        if (null == user || !userService.isAccessValid(user.getTokenCreatetime())) {
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
        String[] userTypes = user.getLoginType();
        if (null == userTypes || userTypes.length < 1) {
            // 无权访问
            ErrUtils.appThrow(userService.errIllegalPermission());
        }
        for (String type : types) {
            for (String userType : userTypes) {
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
