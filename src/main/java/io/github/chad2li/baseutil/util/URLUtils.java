package io.github.chad2li.baseutil.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import io.github.chad2li.baseutil.consts.DefaultConstant;
import org.springframework.lang.Nullable;

import java.beans.IntrospectionException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Logger;

/**
 * URL编码、解码工具
 * Created by ly on 2014/12/1.
 */
public class URLUtils {
    private static Logger log = Logger.getLogger(URLUtils.class.getName());

    /**
     * URL 编码、解码 List
     *
     * @param list
     * @param charset
     * @param isEnc
     * @param <T>
     * @return
     * @throws java.io.UnsupportedEncodingException
     * @throws IllegalAccessException
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     */
    private static <T extends List> T urlEncodeList(T list, String charset, boolean isEnc) throws UnsupportedEncodingException,
            IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (null == list || list.isEmpty())
            return list;

        for (int i = 0; i < list.size(); i++)
            list.set(i, urlCode(list.get(i), charset, isEnc));

        return list;
    }

    /**
     * URL 编码、解码 Array
     *
     * @param array
     * @param charset
     * @param isEnc
     * @param <T>
     * @return
     * @throws java.io.UnsupportedEncodingException
     * @throws IllegalAccessException
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     */
    private static <T> T[] urlEncodeArray(T[] array, String charset, boolean isEnc) throws UnsupportedEncodingException,
            IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (null == array || array.length < 1)
            return array;

        for (int i = 0; i < array.length; i++)
            array[i] = urlCode(array[i], charset, isEnc);

        return array;
    }

    /**
     * URL编码、解码 Map
     *
     * @param map
     * @param charset
     * @param isEnc
     * @param <T>
     * @return
     * @throws java.io.UnsupportedEncodingException
     * @throws IllegalAccessException
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     */
    private static <T extends Map> T urlEncodeMap(T map, String charset, boolean isEnc) throws UnsupportedEncodingException,
            IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (null == map || map.size() < 1)
            return map;

        Object obj = null;
        Object val = null;
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            obj = it.next();
            val = map.get(obj);
            val = urlCode(val, charset, isEnc);
            map.put(obj, val);
        }

        return map;
    }

    /**
     * URL编码、解码 Set
     *
     * @param set
     * @param charset
     * @param isEnc
     * @param <T>
     * @return
     * @throws java.io.UnsupportedEncodingException
     * @throws IllegalAccessException
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     */
    private static <T extends Set> T urlEncodeSet(T set, String charset, boolean isEnc) throws UnsupportedEncodingException,
            IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (null == set || set.size() < 1)
            return set;

        Object[] array = set.toArray();
        set.clear();
        for (Object obj : array)
            set.add(urlCode(obj, charset, isEnc));

        return set;
    }

    /**
     * URL编码、解码 Bean
     *
     * @param obj
     * @param charset
     * @param isEnc
     * @param <T>
     * @return
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     * @throws java.io.UnsupportedEncodingException
     */
    private static <T> T urlEncodeBean(T obj, Class<?> clazz, String charset, boolean isEnc) throws IntrospectionException, InvocationTargetException,
            IllegalAccessException, UnsupportedEncodingException {
        if (clazz == Object.class) return obj;

        Set<String> members = ReflectUtils.parseMember(clazz);

        Object val = null;
        Class<?> valType = null;
        for (String m : members) {
            log.fine("parseMember >> " + m);
            val = ReflectUtils.getValue(obj, m);
            valType = ReflectUtils.field(clazz, m).getType();
            val = urlCode(val, charset, isEnc);
            ReflectUtils.setValue(obj, m, val);
            log.fine("parseMember >> " + m + " val >> " + val);
        }

        if (clazz == Object.class) return obj;

        return urlEncodeBean(obj, clazz.getSuperclass(), charset, isEnc);
    }

    /**
     * URL编码、解码
     *
     * @param obj     编码、解码对象
     * @param charset 编码字符集
     * @param isEnc   为 true 表示编码；为 false 表示解码
     * @param <T>     泛型
     * @return
     * @throws java.io.UnsupportedEncodingException
     * @throws IllegalAccessException
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     */
    private static <T> T urlCode(T obj, String charset, boolean isEnc) throws UnsupportedEncodingException, IllegalAccessException,
            IntrospectionException, InvocationTargetException {
        if (obj == null) return obj;

        // 原始类型
        if (obj instanceof Integer
                || obj instanceof Float || obj instanceof Boolean
                || obj instanceof Short || obj instanceof Double
                || obj instanceof Long || obj instanceof BigDecimal
                || obj instanceof BigInteger || obj instanceof Byte)
            return obj;

        if (obj instanceof String) {
            return (T) (isEnc ? URLEncoder.encode(obj.toString(), charset)
                    : URLDecoder.decode(obj.toString(), charset));
        }

        if (obj instanceof Object[])
            return (T) urlEncodeArray((Object[]) obj, charset, isEnc);

        if (obj instanceof List)
            return (T) urlEncodeList((List) obj, charset, isEnc);

        if (obj instanceof Map)
            return (T) urlEncodeMap((Map) obj, charset, isEnc);

        if (obj instanceof Set)
            return (T) urlEncodeSet((Set) obj, charset, isEnc);

        // 当obj 为 java 内置其它对象时，不解析
        if (obj.getClass().getName().startsWith("java.")
                || obj.getClass().getName().startsWith("javax."))
            return obj;

        return urlEncodeBean(obj, obj.getClass(), charset, isEnc);
    }

    /**
     * URL编码、解码
     *
     * @param obj     编码、解码对象
     * @param charset 编码字符集
     * @param isEnc   为 true 表示编码；为 false 表示解码
     * @param <T>     泛型
     * @return
     */
    public static <T> T code(T obj, String charset, boolean isEnc) {
        try {
            return urlCode(obj, charset, isEnc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 以 / 拼接路径
     *
     * @param url     前缀，不可为空
     * @param isHttps 是否使用https开头
     * @param paths   所有的路径
     * @return full url
     * @author chad
     * @see URLUtils#appendUrl(String, Boolean)
     * @since 1 by chad at 2023/9/7
     */
    public static String appendUrlPath(String url, @Nullable Boolean isHttps, String... paths) {
        url = appendUrl(url, isHttps);
        if (ArrayUtil.isEmpty(paths)) {
            return url;
        }

        return appendUrl(url, paths);
    }

    /**
     * 给url加前缀协议 http[s]://
     *
     * @param url     url
     * @param isHttps 是否使用https开头
     *                <ul>
     *                <li>true: 前缀会拼接https://</li>
     *                <li>false: 前缀会拼接http://</li>
     *                <li>null: 不拼接</li>
     *                <li>url如果已有http[s]://，则不拼接</li>
     *                </ul>
     * @return http[s]://{url}
     * @author chad
     * @since 1 by chad at 2023/9/7
     */
    public static String appendUrl(String url, @Nullable Boolean isHttps) {
        url = CharSequenceUtil.nullToEmpty(url).trim();
        String protocol = DefaultConstant.Norm.EMPTY;
        if (!url.startsWith(DefaultConstant.Http.HTTP_PROTOCOL)
                && !url.startsWith(DefaultConstant.Http.HTTPS_PROTOCOL)
                && !url.startsWith(DefaultConstant.Http.ROOT_PROTOCOL)) {
            // 不以http://, https://, //开头，null不拼接http[s]://
            if (Boolean.TRUE.equals(isHttps)) {
                protocol = DefaultConstant.Http.HTTPS_PROTOCOL;
            } else if (Boolean.FALSE.equals(isHttps)) {
                protocol = DefaultConstant.Http.HTTP_PROTOCOL;
            }
            url = protocol + url;
        }
        return url;
    }

    /**
     * 拼接url
     *
     * @param url   url
     * @param paths 路径，按顺序拼接在url后
     * @return url[?&]paths
     * @author chad
     * @since 1 by chad at 2023/9/7
     */
    public static String appendUrl(String url, String... paths) {
        StringBuilder sb = new StringBuilder(url);
        int len = paths.length;
        if (len > 0 && !url.endsWith(DefaultConstant.Norm.FILE_SPLIT)) {
            sb.append(DefaultConstant.Norm.FILE_SPLIT);
        }
        for (int i = 0; i < len; i++) {
            String path = paths[i];
            if (CharSequenceUtil.isEmpty(path)) {
                continue;
            }
            if (path.matches("^" + DefaultConstant.Norm.FILE_SPLIT + "$")) {
                // 仅包含 /
                continue;
            }
            // todo 非法字符
            if (path.endsWith(DefaultConstant.Norm.FILE_SPLIT)) {
                // 去掉开头的 / todo 开头可能有多个 /
                path = path.substring(1);
            }
            sb.append(path);
            if (i + 1 < len && !path.endsWith(DefaultConstant.Norm.FILE_SPLIT)) {
                sb.append(DefaultConstant.Norm.FILE_SPLIT);
            }
        }
        // todo 将 // 替换为 /，中间过程替换，最终全局替换，谁性能高
        return sb.toString();
    }

    public static String appendQueryUtf8(String url, String... queries) {
        return appendQuery(url, StandardCharsets.UTF_8.name(), queries);
    }

    /**
     * 拼接查询参数，将对参数值进行URLEncode
     *
     * @param url     url
     * @param queries query，1个名称，1个值，依次
     * @return url[?&]queries
     * @author chad
     * @see URLUtils#appendQuery(String, Map, String)
     * @since 1 by chad at 2023/9/7
     */
    public static String appendQuery(String url, String charset, String... queries) {
        Assert.notBlank(url);
        if (ArrayUtil.isEmpty(queries)) {
            return url;
        }
        int len = queries.length;
        Map<String, Object> queryMap = new HashMap<>(len / 2);
        String name;
        String value;
        for (int i = 0; i < len; i++) {
            name = queries[i];
            if (CharSequenceUtil.isEmpty(name)) {
                continue;
            }
            value = DefaultConstant.Norm.EMPTY;
            if (i + 1 < len) {
                // 有参数值，不需要判空，拼接map的方法有判空处理
                value = queries[i + 1];
            }
            queryMap.put(name, value);
        }
        return appendQuery(url, queryMap, charset);
    }

    public static String appendQuery(String url, Map<String, ?> queryMap) {
        return appendQuery(url, queryMap, StandardCharsets.UTF_8.name());
    }

    /**
     * 拼接query参数
     *
     * @param url      url
     * @param queryMap key: 参数名，value：参数值
     * @param charset  编码
     * @return k1=v1&k2=v2...
     * @author chad
     * @see URLUtils#appendQuery(Map, String)
     * @since 1 by chad at 2023/9/7
     */
    public static String appendQuery(String url, Map<String, ?> queryMap, String charset) {
        Assert.notBlank(url);
        if (CollUtil.isEmpty(queryMap)) {
            return url;
        }
        String query = appendQuery(queryMap, charset);
        if (CharSequenceUtil.isEmpty(query)) {
            return url;
        }
        if (url.indexOf(DefaultConstant.Norm.QUESTION_MARK) > 0) {
            return url + DefaultConstant.Norm.AMPERSAND + query;
        } else {
            return url + DefaultConstant.Norm.QUESTION_MARK + query;
        }
    }

    /**
     * 以&拼接参数key=value
     * <p>
     * 如果参数值为空，也会拼接参数名，如：name=
     * </p>
     *
     * @param queryMap key：参数名，value：参数值（可以为空）
     * @return k1=v1&k2=v2...
     * @author chad
     * @since 1 by chad at 2023/9/7
     */
    public static String appendQuery(Map<String, ?> queryMap, String charset) {
        if (CollUtil.isEmpty(queryMap)) {
            return DefaultConstant.Norm.EMPTY;
        }
        StringJoiner sj = new StringJoiner(DefaultConstant.Norm.AMPERSAND);
        String name;
        Object value;
        String encodeValue;
        Charset charsetVal = Charset.forName(charset);
        for (Map.Entry<String, ?> entry : queryMap.entrySet()) {
            name = entry.getKey();
            value = entry.getValue();
            if (CharSequenceUtil.isEmpty(name)) {
                continue;
            }
            if (ObjectUtil.isEmpty(value)) {
                value = DefaultConstant.Norm.EMPTY;
            }
            encodeValue = URLUtil.encode(String.valueOf(value), charsetVal);
            sj.add(name + DefaultConstant.Norm.EQUALS + encodeValue);
        }

        return sj.toString();
    }
}
