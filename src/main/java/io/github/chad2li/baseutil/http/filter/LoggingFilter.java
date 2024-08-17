package io.github.chad2li.baseutil.http.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.github.chad2li.baseutil.consts.DefaultConstant;
import io.github.chad2li.baseutil.http.filter.log.BufferedRequestWrapper;
import io.github.chad2li.baseutil.util.HttpUtils;
import io.github.chad2li.baseutil.util.SpringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 1. 将req封装成可重复读取<br/>
 * 2. 打印日志信息
 *
 * @author chad
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
        log.info("RES: {} [{}]", resultJson, divide);
    }

    /**
     * 打印请求日志
     *
     * @param req http servlet request
     * @author chad
     * @since 1 by chad at 2024/3/15
     */
    private void logReq(ContentCachingRequestWrapper req) {
        // 注意隐藏用户的pwd、token等信息
        // 忽略文件上传内容（易内存溢出）

        // 获取请求：
        // 路径信息
        String url = req.getRequestURI();
        String method = req.getMethod();
        // 头部信息：
        Map<String, String> header = pkgHeader(req);
        // 客户端信息：IP
        String clientIp = SpringUtils.getRemoteIp(req);

        // headers
        StringJoiner headerJoiner = new StringJoiner(DefaultConstant.Norm.COMMA);
        Map.Entry<String, String> entry;
        for (Iterator<Map.Entry<String, String>> it = header.entrySet().iterator(); it.hasNext(); ) {
            entry = it.next();
            headerJoiner.add(entry.getKey() + DefaultConstant.Norm.COLON + entry.getValue());
        }

        // params
        Map<String, String> params = SpringUtils.getParam(req);
        StringJoiner paramsJoiner = new StringJoiner(DefaultConstant.Norm.COMMA);
        if (CollUtil.isNotEmpty(params)) {
            for (Iterator<Map.Entry<String, String>> it = params.entrySet().iterator(); it.hasNext(); ) {
                entry = it.next();
                paramsJoiner.add(entry.getKey() + DefaultConstant.Norm.COLON + entry.getValue());
            }
        }
        // body
        String body;
        if (!HttpUtils.HTTP_METHOD_GET.equalsIgnoreCase(method)) {
            body = SpringUtils.reqBody(req);
        } else {
            body = DefaultConstant.Norm.EMPTY;
        }

        log.info("REQ-{} {}:{}, headers:[{}] params:[{}], body:[{}]", method, clientIp, url,
                headerJoiner, paramsJoiner, body);
    }

    /**
     * 返回值转 json
     *
     * @param result 返回值
     * @return json
     * @author chad
     * @since 1 by chad at 2024/3/15
     */
    private String jsonResp(@Nullable Object result) {
        // 将结果转为JSON
        if (ObjectUtil.isEmpty(result)) {
            return "";
        }

        String resultJson = null;
        try {
            resultJson = JSONUtil.toJsonStr(result);
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
            log.warn("parse response error, result:{}", result, t);
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

//        res.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//        res.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
//        res.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
//        res.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");

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
