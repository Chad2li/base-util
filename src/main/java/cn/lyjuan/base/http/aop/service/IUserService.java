package cn.lyjuan.base.http.aop.service;

import cn.lyjuan.base.exception.IAppCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public interface IUserService<T extends IUserService.UserToken> {
    /**
     * 根据access token 获取用户信息
     * @param access
     * @return
     */
    <T extends UserToken> T user(String access);

    /**
     * 将用户信息存入cache
     * @param user
     */
    void setCache(T user);

    /**
     * 将用户信息存入缓存
     * @return
     */
    <T extends UserToken> T getCache();

    /**
     * 验证token是否过期
     * @return  true未过期
     */
    boolean isAccessValid(LocalDateTime tokenCreatetime);

    /**
     * 需要登录
     */
    IAppCode errNeedLogin();

    /**
     * token无效
     */
    IAppCode errTokenInvalid();

    /**
     * 无权访问
     * @return
     */
    IAppCode errIllegalPermission();

    @Data
    @NoArgsConstructor
    abstract class UserToken{
        /**
         * 用户ID
         */
        protected int userId;
        /**
         * 登陆类型
         */
        protected String[] loginType;
        protected String access;
        protected String refresh;
        /**
         * token的创建时间，用于有效斯的判断
         */
        protected LocalDateTime tokenCreatetime;
    }
}
