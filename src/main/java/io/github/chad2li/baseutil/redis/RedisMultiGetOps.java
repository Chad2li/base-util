package io.github.chad2li.baseutil.redis;

import io.github.chad2li.baseutil.redis.lua.ARedisLua;
import io.github.chad2li.baseutil.redis.redisson.RedissonOps;
import io.github.chad2li.baseutil.util.JsonUtils;
import io.github.chad2li.baseutil.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * 集群环境下，所有的redisKey需要定位到同一个slot<br/>
 * 可使用 {slotKey}:yourIdentify，redis只使用 {} 中的值来计算 slot值<br/>
 * 不支持redisson，获取多值请使用{@link RedissonOps#hGets(String, Set)}或{@link RedissonOps#hGets(String, Object[])}或{@link RedissonOps#gets(String...)}
 */
public class RedisMultiGetOps extends ARedisLua {
    public static final String BEAN_NAME = "appApiRedisMultiGetOps";

    /**
     * 脚本资源
     */
    private static final String LUA_SCRIPT_FILE = "lua/other/MultiGet.lua";

    /**
     * 仅支持RedisTemplate方式
     *
     * @param redisTemplate
     */
    public RedisMultiGetOps(@Autowired(required = false) RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate, null, LUA_SCRIPT_FILE);
        this.redisTemplate = redisTemplate;
    }


    /**
     * 返回所有 {keys} 的值，并解析成指定的 {type}类
     *
     * @param type
     * @param keys
     * @param <T>
     * @return
     */
    public <T> List<T> getMultiKey(Type type, String... keys) {
        List<String> list = getMultiKey(keys);
        return str2type(list, type);
    }

    /**
     * 返回所有 {keys} 的值
     *
     * @param keys
     * @return
     */
    public List<String> getMultiKey(String... keys) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(keys[0]);// 集群下决定slot的点

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("isHash", false);
        argvMap.put("keys", keys);

        String json = JsonUtils.to(argvMap);

        /**
         * 调用脚本并执行
         */
        Object obj = execute(keyList, json);

        return (List<String>) obj;
    }

    /**
     * 返回所有 {redisKey + separator + keys} 的值，并解析为指定的{@code type}类型
     *
     * @param redisKey
     * @param type
     * @param separator
     * @param keys
     * @param <T>
     * @return
     */
    public <T> List<T> getMultiKey(String redisKey, Type type, String separator, Object... keys) {
        List<String> list = getMultiKey(redisKey, separator, keys);
        return str2type(list, type);
    }

    /**
     * 返回所有 {redisKey + separator + keys} 的值
     *
     * @param redisKey  redis键
     * @param separator 完整Key的分隔符
     * @param keys      hash键，空表示sds结构
     * @return
     */
    public List getMultiKey(String redisKey, String separator, Object... keys) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(redisKey);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("isHash", false);
        argvMap.put("keys", keys);
        separator = StringUtils.isNull(separator) ? "" : separator;
        argvMap.put("sep", separator);

        String json = JsonUtils.to(argvMap);

        /**
         * 调用脚本并执行
         */
        Object obj = execute(keyList, json);
        return (List) obj;
    }

    /**
     * 返回 redisKey的hash结构下所有 {hashKey + separator + keys}的值，并解析成指定的{@code type}类型
     *
     * @param redisKey
     * @param type
     * @param keys
     * @param <T>
     * @return
     */
    public <T> List<T> getHashMultiKey(String redisKey, Type type, Object... keys) {
        List<String> list = getHashMultiKey(redisKey, keys);
        return str2type(list, type);
    }

    /**
     * 返回 redisKey的hash结构下所有keys的值
     *
     * @param redisKey
     * @param keys
     * @return
     */
    public List<String> getHashMultiKey(String redisKey, Object... keys) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(redisKey);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("isHash", true);
        argvMap.put("keys", keys);

        String json = JsonUtils.to(argvMap);

        /**
         * 调用脚本并执行
         */
        Object obj = execute(keyList, json);

        return (List<String>) obj;
    }

    /**
     * 将结果返回的String集合转为指定的{@code type}类型
     *
     * @param source
     * @param type
     * @param <T>
     * @return
     */
    private <T> List<T> str2type(List<String> source, Type type) {
        if (CollectionUtils.isEmpty(source))
            return Collections.EMPTY_LIST;

        List<T> result = new ArrayList<>(source.size());
        for (Object o : source) {
            if (StringUtils.isNull(o))
                continue;

            result.add(JsonUtils.from(type, String.valueOf(o)));
        }

        return result;
    }
}
