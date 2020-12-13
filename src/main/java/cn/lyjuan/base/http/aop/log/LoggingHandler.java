package cn.lyjuan.base.http.aop.log;

import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.SpringUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 该类提供Spring请求的日志打印功能
 */
@Slf4j
@Aspect
@Order(LoggingHandler.ORDER_LOGGING)
public class LoggingHandler {
    public static final int ORDER_LOGGING = 100;
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

    /**
     * 拦截带{@link org.springframework.web.bind.annotation.RestController}
     * 或{@link org.springframework.web.bind.annotation.RequestMapping}
     * 且为{@code public}的方法
     */
    @Pointcut("(@within(org.springframework.web.bind.annotation.RestController)" +
            " || @within(org.springframework.stereotype.Controller))" +
            " && execution(public * *(..))")
    public void pointcut() {

    }

    /**
     * 打印日志
     *
     * @param point
     */
    @Around("pointcut()")
    public Object logging(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest req = SpringUtils.getRequest();

        // 打印请求信息
        logReq(req);

        // 记录处理时间
        long begin = System.currentTimeMillis();
        // 执行
        Object result = point.proceed();
        // 处理时间
        long divide = System.currentTimeMillis() - begin;

        // 打印响应信息
        logRes(result, divide);

        // 返回响应
        return result;
    }

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

    private void logReq(HttpServletRequest req) {
        // 注意隐藏用户的pwd、token等信息
        // 忽略文件上传内容（易内存溢出）

        // 获取请求：
        // 路径信息
        String url = req.getRequestURI();
//        String path = req.getContextPath();
        String method = req.getMethod();
        // 头部信息：
        Map<String, String> header = pkgHeader(req);
        // 客户端信息：IP
        String clientHost = req.getRemoteHost();
        if (!log.isDebugEnabled()) {
            log.info("REQ-{}: {}:{}", method, clientHost, url);
            return;
        }
        // header
        StringBuilder sb = new StringBuilder();
        Map.Entry<String, String> entry = null;

        log.debug("=========== REQ-{} ===========", method);
        log.debug("--info {} {}:{}", req.getMethod(), clientHost, url);
        for (Iterator<Map.Entry<String, String>> it = header.entrySet().iterator(); it.hasNext(); ) {
            entry = it.next();
            log.debug("--head {}: {}", entry.getKey(), entry.getValue());
        }


        Map<String, String> params = SpringUtils.getParam(req);
        String body = null;
        if (!"GET".equalsIgnoreCase(method))
            body = SpringUtils.reqBody(req);
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

    public int getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(int maxLen) {
        this.maxLen = maxLen;
    }

    public int getPreLen() {
        return preLen;
    }

    public void setPreLen(int preLen) {
        this.preLen = preLen;
    }

    public int getSufLe() {
        return sufLe;
    }

    public void setSufLe(int sufLe) {
        this.sufLe = sufLe;
    }
}
