package cn.lyjuan.base.http.aop.service;

import cn.lyjuan.base.exception.IAppCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public interface IUserService<T extends IUserService.UserToken, E extends IHeaderService.AHeaderParam> {
    /**
     * 根据请求头部参数 获取用户信息
     *
     * @param header
     * @return
     */
    T user(E header);

    /**
     * 将用户信息存入cache
     *
     * @param user
     */
    void setCache(T user);

    /**
     * 将用户信息存入缓存
     *
     * @return
     */
    T getCache();

    /**
     * 验证token是否过期
     *
     * @param user
     * @return true未过期
     */
    boolean isAccessValid(T user);

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
     *
     * @return
     */
    IAppCode errIllegalPermission();

    @Data
    @NoArgsConstructor
    abstract class UserToken {
        /**
         * 用户ID
         */
        protected int userId;
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
