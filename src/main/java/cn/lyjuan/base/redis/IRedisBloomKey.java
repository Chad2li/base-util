package cn.lyjuan.base.redis;

public interface IRedisBloomKey extends IRedisKey{
    /**
     * 期望存储数据大小
     * @return
     */
    Long getExpectSize();

    /**
     * 期望容错率0~1
     * @return
     */
    Double getFaultTolerant();
}
