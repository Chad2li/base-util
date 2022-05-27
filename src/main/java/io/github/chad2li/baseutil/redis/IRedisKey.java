package io.github.chad2li.baseutil.redis;

public interface IRedisKey {

    /**
     * 键占位符
     */
    String ID_SPAN = "_KEY_SPAN_";

    /**
     * 替换占位符
     *
     * @param ids
     * @return
     */
    default String key(Object... ids) {
        return RedisUtil.key(this, ids);
    }

    String key();
}
