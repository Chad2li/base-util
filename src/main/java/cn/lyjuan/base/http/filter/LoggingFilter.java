package cn.lyjuan.base.http.filter;

import cn.lyjuan.base.http.filter.log.BufferedRequestWrapper;
import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.SpringUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 1. 将req封装成可重复读取<br/>
 * 2. 打印日志信息
 */
@Slf4j
@Data
@WebFilter(urlPatterns = {"*"}, filterName = LoggingFilter.NAME)
@Order(LoggingFilter.ORDER)
public class LoggingFilter implements Filter {
    public static final String NAME = "baseLoggingFilterName";

    public static final int ORDER = 100;
    /**
     * 打印最大长度
     */
    private int maxLen = 255;
    /**
     * 超出长度后截取前多少个
     */
    private int preLen = 100;
    /**
     * 超出长度后截后多少个
     */
    private int sufLe = 10;

    private FilterProperties filterProperties;

    /**
     * 打印响应信息
     *
     * @param result 响应数据
     * @param divide 处理时间，毫秒
     */
    private void logRes(Object result, long divide) {
        // 转化响应
        String resultJson = jsonResp(result);

        if (log.isDebugEnabled()) {
            log.debug("--resp {} [{}]", resultJson, divide);
            log.debug("=========== REQ-END ===========");
        } else
            log.info("RES: {} [{}]", resultJson, divide);
    }

    private void logReq(ContentCachingRequestWrapper req) throws UnsupportedEncodingException {
        // 注意隐藏用户的pwd、token等信息
        // 忽略文件上传内容（易内存溢出）

        // 获取请求：
        // 路径信息
        String url = req.getRequestURI();
//        url = req.getServletPath();
//        url = req.getContextPath();
        String method = req.getMethod();
        // 头部信息：
        Map<String, String> header = pkgHeader(req);
        // 客户端信息：IP
        String clientIp = SpringUtils.getRemoteIp(req);
        if (!log.isDebugEnabled()) {
            log.info("REQ-{}: {}:{}", method, clientIp, url);
            return;
        }
        // header
        Map.Entry<String, String> entry = null;

        log.debug("=========== REQ-{} ===========", method);
        log.debug("--info {} {}:{}", req.getMethod(), clientIp, url);
        for (Iterator<Map.Entry<String, String>> it = header.entrySet().iterator(); it.hasNext(); ) {
            entry = it.next();
            log.debug("--head {}: {}", entry.getKey(), entry.getValue());
        }

        Map<String, String> params = SpringUtils.getParam(req);
        String body = null;
        if (!"GET".equalsIgnoreCase(method)) {
            body = SpringUtils.reqBody(req);
        }
        if (null != params && params.size() > 0) {
            for (Iterator<Map.Entry<String, String>> it = params.entrySet().iterator(); it.hasNext(); ) {
                entry = it.next();
                log.debug("--para {}: {}", entry.getKey(), entry.getValue());
            }
        }

        if (!StringUtils.isNull(body)) {
            log.debug("--body {}", body);
        }

    }

    private String jsonResp(Object result) {
        // 将结果保存为JSON
        if (null == result) return "";

        String resultJson = null;
        try {
            resultJson = JsonUtils.to(result);
            int len = resultJson.length();
            // 限制打印长度
            if (len > maxLen) {
                // prefix
                String pre = resultJson.substring(0, preLen);
                // suffix
                String suf = resultJson.substring(len - sufLe - 1);
                resultJson = pre + "...(IGNORE " + (len - preLen - sufLe) + ")..." + suf;
            }

        } catch (Throwable t) {
            resultJson = "parse response error";
            log.warn(resultJson, t);
        }

        return resultJson;
    }


    /**
     * 封闭请求头部信息
     *
     * @param request
     * @return
     */
    private Map<String, String> pkgHeader(HttpServletRequest request) {
        Map<String, String> header = new HashMap<>();

        Enumeration<String> enums = request.getHeaderNames();

        String key = null;
        String val = null;
        while (enums.hasMoreElements()) {
            key = enums.nextElement();
            val = request.getHeader(key);
            header.put(key, val);
        }

        return header;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // 记录处理时间
        long begin = System.currentTimeMillis();
        //可重复读取 封装
        BufferedRequestWrapper req = new BufferedRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper((HttpServletResponse) response);

        res.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        res.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
        res.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        res.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");

        boolean isSkip = FilterProperties.isSkip(this.filterProperties, req.getRequestURI());
        if (!isSkip) {
            // 打印请求信息
            logReq(req);
        }

        //将request 传到下一个Filter
        filterChain.doFilter(req, res);

        if (!isSkip) {
            // response
            String result = new String(res.getContentAsByteArray());

            // 处理时间
            long divide = System.currentTimeMillis() - begin;

            // 打印响应信息
            logRes(result, divide);
        }

        res.copyBodyToResponse();
    }
}
