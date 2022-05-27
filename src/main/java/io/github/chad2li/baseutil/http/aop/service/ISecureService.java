package io.github.chad2li.baseutil.http.aop.service;

public interface ISecureService<H extends IHeaderService.AHeaderParam> {

    /**
     * 应用可以组装requestId
     *
     * @param header 请求头部信息
     * @return
     */
    String fullRequestId(H header);

    /**
     * 获取上一个requestId存活时间
     *
     * @param fullRequestId 请求唯一标识
     * @return requestId是否重复
     */
    boolean exists(String fullRequestId);

    /**
     * 缓存requestId，防止重复，并设置失效时间
     *
     * @param fullRequestId 请求唯一标识
     * @param expireSeconds 失效秒数
     */
    void cache(String fullRequestId, int expireSeconds);
}
