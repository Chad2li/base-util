package io.github.chad2li.baseutil.util;

import java.beans.IntrospectionException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * URL编码、解码工具
 * Created by ly on 2014/12/1.
 */
public class URLUtils
{
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
            IllegalAccessException, IntrospectionException, InvocationTargetException
    {
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
            IllegalAccessException, IntrospectionException, InvocationTargetException
    {
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
            IllegalAccessException, IntrospectionException, InvocationTargetException
    {
        if (null == map || map.size() < 1)
            return map;

        Object obj = null;
        Object val = null;
        for (Iterator it = map.keySet().iterator(); it.hasNext(); )
        {
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
            IllegalAccessException, IntrospectionException, InvocationTargetException
    {
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
            IllegalAccessException, UnsupportedEncodingException
    {
        if (clazz == Object.class) return obj;

        Set<String> members = ReflectUtils.parseMember(clazz);

        Object val = null;
        Class<?> valType = null;
        for (String m : members)
        {
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
            IntrospectionException, InvocationTargetException
    {
        if (obj == null) return obj;

        // 原始类型
        if (obj instanceof Integer
                || obj instanceof Float || obj instanceof Boolean
                || obj instanceof Short || obj instanceof Double
                || obj instanceof Long || obj instanceof BigDecimal
                || obj instanceof BigInteger || obj instanceof Byte)
            return obj;

        if (obj instanceof String)
        {
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
    public static <T> T code(T obj, String charset, boolean isEnc)
    {
        try
        {
            return urlCode(obj, charset, isEnc);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
