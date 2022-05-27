package io.github.chad2li.baseutil.http.ctl;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基础控制层
 * <p>
 *     实现一些通用功能
 * </p>
 */
public abstract class BaseCtl
{
    /**
     * 得到request对象
     */
    public HttpServletRequest req()
    {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        return request;
    }

    public HttpServletResponse resp()
    {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        return response;
    }
}
