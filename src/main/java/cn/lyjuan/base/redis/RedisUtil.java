package cn.lyjuan.base.redis;

import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.ReflectUtils;
import cn.lyjuan.base.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RedisUtil {
    /**
     * 将任意值类型的map转为string值类型的map
     *
     * @param map
     * @return
     */
    public static Map<String, String> map2map(Map<?, ?> map) {
        if (null == map || map.isEmpty()) return Collections.EMPTY_MAP;

        Map<String, String> strMap = new HashMap<>(map.size());
        map.entrySet().forEach(entry ->
                strMap.put(String.valueOf(entry.getKey()), JsonUtils.to(entry.getValue()))
        );
        return strMap;
    }

    /**
     * 将bean转为map，方便存入hash
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> Map<String, String> bean2map(T obj) {
        if (null == obj) return Collections.EMPTY_MAP;
        Map<String, Object> map = ReflectUtils.membersToMap(obj);
        Map<String, String> result = new HashMap<>(map.size());

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (StringUtils.isNull(entry.getKey())
                    || StringUtils.isNull(entry.getValue())) continue;
            result.put(entry.getKey(), JsonUtils.to(entry.getValue()));
        }

        return result;
    }

    /**
     * 转换redis hash存储map
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> Map<byte[], byte[]> bean2byteMap(T obj) {
        Map<String, Object> map = ReflectUtils.membersToMap(obj);
        Map<byte[], byte[]> result = new HashMap<>(map.size());

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (StringUtils.isNull(entry.getKey())
                    || StringUtils.isNull(entry.getValue())) continue;
            result.put(entry.getKey().getBytes(), JsonUtils.to(entry.getValue()).getBytes());
        }


        return result;
    }

    /**
     * 将ID按顺序拼接，每个ID固定12位长度，不足12位左补零
     *
     * @param ids
     * @return
     */
    public static <T> String idKeys(T... ids) {
        if (ArrayUtils.isEmpty(ids))
            throw new NullPointerException("IdKeys cannot be empty");

        if (ids.length > 6)
            throw new IllegalStateException("IdKeys max length: 6, but <" + ids.length + ">");

        StringBuilder sb = new StringBuilder();
        for (T id : ids) {
            sb.append(id).append(",");
//            sb.append(String.format("%012d", id));
        }

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * 从idKeys中取出索引为index的id
     *
     * @param idKeys
     * @param index  第index+1位表示的ID
     * @return
     */
    public static String fromIdKeys(String idKeys, Integer index) {
        if (StringUtils.isNull(idKeys))
            throw new IllegalArgumentException("IdKeys cannot be empty");
        if (index > 5 || index < 0)
            throw new IllegalArgumentException("IdKeys index must be {0,5}, but<" + index + ">");

//        return idKeys.substring(index * 12, (index + 1) * 12);
        return idKeys.split(",")[index];
    }

    /**
     * 生成完整的redis key
     *
     * @param key
     * @param spanIds
     * @return
     */
    public static String key(IRedisKey key, int... spanIds) {
        return key(key.key(), spanIds);
    }

    public static String key(IRedisKey key, String... spanValues) {
        return key(key.key(), spanValues);
    }


    /**
     * 生成完整的redis key
     *
     * @param spanIds
     * @return
     */
    public static String key(String key, int... spanIds) {
        if (1 == spanIds.length)
            return RedisUtil.key(key, String.valueOf(spanIds[0]));

        String[] arr = new String[spanIds.length];
        for (int i = 0; i < spanIds.length; i++) {
            arr[i] = String.valueOf(spanIds[i]);
        }
        return RedisUtil.key(key, arr);
    }

    /**
     * 使用spanValues替换key中{@code RedisSpan.ID_SPAN}占位符
     *
     * @param key
     * @param spanValues
     * @return
     */
    public static String key(String key, String... spanValues) {
        if (null == spanValues || spanValues.length < 1)
            return key;

        if (1 == spanValues.length)
            return key.replaceAll(IRedisKey.ID_SPAN, spanValues[0]);

        String strKey = key;
        for (String id : spanValues) {
            strKey = strKey.replaceFirst(IRedisKey.ID_SPAN, id);
        }

        return strKey;
    }
}
