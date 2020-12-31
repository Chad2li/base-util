package cn.lyjuan.base.http.aop.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 签名服务
 */
public interface ISignService {

    /**
     * 获取指定appId的缓存信息
     *
     * @return
     */
    App app(Integer appId);

    /**
     * 自行处理签名的数据
     *
     * @param header
     * @param get
     * @param body
     * @param key
     * @param signStr
     * @param sign
     */
    void signParam(IHeaderService.AHeaderParam header, Map<String, String> get, String body, String key, String signStr, String sign);

    @Data
    @NoArgsConstructor
    public static class App {
        /**
         * 平台分配的appId
         */
        protected Integer appId;
        /**
         * appId对应的md5key
         */
        protected String md5key;

        /**
         * app是否有效
         */
        protected boolean isValid;
    }
}
