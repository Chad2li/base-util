package io.github.chad2li.baseutil.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ly on 2015/1/11.
 */
public class SessUtils
{
    /**
     * 保存到信息 session
     */
    public static void save(HttpServletRequest req, String key, Object obj)
    {
        req.getSession().setAttribute(key, obj);
    }

    /**
     * 从 session 中获取信息
     * @param req
     * @param clazz     对象类
     * @param key
     * @param isThrow   true 表示如果信息不存在，抛出异常
     * @param <T>
     * @return
     */
    public static <T> T get(HttpServletRequest req, Class<T> clazz, String key, boolean isThrow)
    {
        Object obj = req.getSession().getAttribute(key);

        if (null == obj || obj.getClass() != clazz)
        {
            if (isThrow)
                throw  new RuntimeException("系统繁忙");
            return null;
        }

        return (T) obj;
    }

    /**
     * 从 session 中获取信息，直接调用 {@code get(req, clazz, key, false)}，默认不抛出异常
     * @param req
     * @param clazz
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T get(HttpServletRequest req, Class<T> clazz, String key)
    {
        return get(req, clazz, key, false);
    }

    /**
     * 从 session 中移除信息
     * @param req
     * @param key
     */
    public static void remove(HttpServletRequest req, String key)
    {
        req.getSession().removeAttribute(key);
    }
}
