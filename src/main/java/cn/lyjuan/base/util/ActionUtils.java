package cn.lyjuan.base.util;


import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@Slf4j
public class ActionUtils {
    /**
     * 是否ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if ("XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || request.getParameter("ajax") != null || request.getHeader("ajax") == "true") {
            return true;
        }

        if (request.getRequestURI().indexOf("/api/") > -1) return true;

        return false;
    }

    /**
     * 为请求和响应设置默认编码
     *
     * @param req
     */
    public static void setDefaultEncode(HttpServletRequest req, HttpServletResponse resp, String charset) {
        try {
            req.setCharacterEncoding(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        resp.setCharacterEncoding(charset);
    }
}