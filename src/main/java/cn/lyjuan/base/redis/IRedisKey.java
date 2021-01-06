package cn.lyjuan.base.redis;

public interface IRedisKey {

    /**
     * 键占位符
     */
    String ID_SPAN = "_KEY_SPAN_";

    String key();
}
