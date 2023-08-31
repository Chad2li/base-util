package io.github.chad2li.baseutil.http.aop.service;

import io.github.chad2li.baseutil.exception.IAppCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public interface IUserService<T extends IUserService.UserToken> {
    /**
     * 根据请求头部参数 获取用户信息
     *
     * @param token 请求头中的身份凭证
     * @return user
     * @author chad
     * @since 1 by chad at 2020/12/14<br/>
     * 2 by chad at 2023/8/28: 入参改为token，与IHeaderService解耦
     */
    @Nullable
    T user(String token);

    /**
     * 将user存入缓存
     *
     * @param user user
     * @author chad
     * @since 1 by chad at 2020/12/14
     */
    void setCache(T user);

    /**
     * 从缓存中获取用户
     *
     * @return user
     * @author chad
     * @since 1 by chad at 2020/12/14
     */
    T getCache();

    /**
     * 验证token是否过期
     *
     * @return true登录信息有效
     * @author chad
     * @since 1 by chad at 2020/12/14
     */
    boolean isAccessValid(T user);

    /**
     * 需要登录的异常码
     *
     * @author chad
     * @since 1 by chad at 2020/12/14
     */
    IAppCode needLogin();

    /**
     * token无效异常码
     *
     * @author chad
     * @since 1 by chad at 2020/12/14
     */
    IAppCode tokenInvalid();

    /**
     * 无权访问异常码
     *
     * @author chad
     * @since 1 by chad at 2020/12/14
     */
    IAppCode illegalPermission();

    @Data
    @NoArgsConstructor
    abstract class UserToken {
        /**
         * 用户ID
         */
        protected Long id;
        /**
         * 登陆类型
         */
        protected List<String> loginTypes;
        /**
         * 缓存时间
         */
        protected LocalDateTime cacheTime = LocalDateTime.now();
    }
}
