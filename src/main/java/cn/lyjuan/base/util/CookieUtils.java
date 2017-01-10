package cn.lyjuan.base.util;


import cn.lyjuan.base.cst.ProjectCst;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * cookie工具类
 *
 * @author hanchao
 *         <p/>
 *         2013-04-23
 */

public class CookieUtils
{

    /**
     * 根据Cookie名称获取Cookie值
     *
     * @param request
     * @param cName        Cookie名称
     * @param defaultValue 默认值
     * @return
     */

    public static String getValue(HttpServletRequest request, String cName, String defaultValue)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies == null)
            return defaultValue;

        Cookie cookie = null;
        for (int i = 0; i < cookies.length; i++)
        {
            cookie = cookies[i];
            if (cName.equals(cookie.getName()))
                return cookie.getValue();
        }

        return defaultValue;
    }


    /**
     * 根据名称获取Cookie的值
     *
     * @param request
     * @param cName   Cookie名称
     * @return
     */

    public static Cookie getCookie(HttpServletRequest request, String cName)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies == null)
            return null;

        Cookie cookie = null;
        for (int i = 0; i < cookies.length; i++)
        {
            cookie = cookies[i];
            if (cName.equals(cookie.getName()))
                return cookie;
        }

        return null;
    }


    /**
     * 添加cookie
     *
     * @param response
     * @param name     cookie的key值
     * @param value    cookie的value值
     * @param path     cookie的路径
     * @param domain   cookie的域
     * @param timeout  cookie的过期时间 2013-6-18
     * @author： 韩超
     */

    public static void addCookie(HttpServletResponse response, String name, String value,
                                 String path, String domain, int timeout)
    {

        Cookie cookie = new Cookie(name, value);

        if (domain == null)
        {
            URL url = null;
            try
            {
                url = new URL(ProjectCst.$_LOCAL_URL);
            } catch (MalformedURLException e)
            {
            }
            domain = url.getHost();
        }

        if (path == null)
            path = "/";

        cookie.setDomain(domain);

        cookie.setPath(path);

        cookie.setMaxAge(timeout);

        response.addCookie(cookie);
    }


    /**
     * 删除cookie
     *
     * @param request
     * @param response
     * @param name     要删除的 cookie 的名称
     */

    public static void delCookie(HttpServletRequest request, HttpServletResponse response,
                                 String name)
    {

        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies)
        {

            if (cookies != null && (name).equals(cookie.getName()))
            {

                addCookie(response, name, null, null, null, 0);

                return;

            }

        }

    }


    /**
     * 修改cookie的value值
     *
     * @param request
     * @param response
     * @param name     Cookie名称
     * @param value    Cookie要修改成的值
     */

    public static void updateCookie(HttpServletRequest request, HttpServletResponse response,
                                    String name, String value)
    {

        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies)
        {

            if (cookies != null && (name).equals(cookie.getName()))
            {

                addCookie(response, name, value, cookie.getPath(), cookie.getDomain(), cookie
                        .getMaxAge());

                return;

            }

        }

    }
}
















