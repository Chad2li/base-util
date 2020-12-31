package cn.lyjuan.base.http.aop.service;

public interface ISecureService {
    /**
     * 获取上一个requestId存活时间
     *
     * @param requestId
     * @return -2表示没有该reqeustId，其他值表示requestId已存在，参考redis tll命令
     */
    long ttl(String requestId);

    /**
     * 缓存requestId，防止重复，并设置失效时间
     *
     * @param requestId
     * @param expireSeconds
     */
    void cache(String requestId, int expireSeconds);
}
