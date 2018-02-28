package cn.lyjuan.base.http.log;

import cn.lyjuan.base.util.HttpUtils;
import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.SpringUtils;
import cn.lyjuan.base.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 该类提供Spring请求的日志打印功能
 */
@Aspect
//@Component
public class LoggingAop
{
    private static Logger log;

    public LoggingAop()
    {
        this(null);
    }

    public LoggingAop(Logger customLog)
    {
        if (null == log)
        {
            synchronized (LoggingAop.class)
            {
                if (null == log)
                    log = null == customLog ? LogManager.getLogger(LoggingAop.class) : customLog;
            }
        }
    }

    /**
     * 拦截带{@link org.springframework.web.bind.annotation.RestController}
     * 或{@link org.springframework.web.bind.annotation.RequestMapping}
     * 且为{@code public}的方法
     */
    @Pointcut("(@within(org.springframework.web.bind.annotation.RestController)" +
            " || @within(org.springframework.stereotype.Controller))" +
            " && execution(public * *(..))")
    public void pointcut()
    {

    }

    /**
     * 打印日志
     *
     * @param point
     */
    @Around("pointcut()")
    public void logging(ProceedingJoinPoint point) throws Throwable
    {
        logging(log, point);
    }

    /**
     * 打印日志
     */
    public static Object logging(Logger log, ProceedingJoinPoint point) throws Throwable
    {
        HttpServletRequest req = SpringUtils.getRequest();
        // 注意隐藏用户的pwd、token等信息
        // 忽略文件上传内容（易内存溢出）

        // 获取请求：
        // 路径信息
        String url = req.getRequestURI();
        String path = req.getContextPath();
        String method = req.getMethod();
        // 头部信息：
        Map<String, String> header = pkgHeader(req);
        // 客户端信息：IP
        String clientHost = req.getRemoteHost();
        if (log.isDebugEnabled())
        {
            // header
            StringBuilder sb = new StringBuilder();
            Map.Entry<String, String> entry = null;

            log.debug("=========== REQ-{} ===========", method);
            log.debug("--info {} {}:{}", req.getMethod(), clientHost, url);
            for (Iterator<Map.Entry<String, String>> it = header.entrySet().iterator(); it.hasNext(); )
            {
                entry = it.next();
                log.debug("--head {}: {}", entry.getKey(), entry.getValue());
            }


            Map<String, String> params = SpringUtils.getParam(req);
            String body = null;
            if (!"GET".equalsIgnoreCase(method))
                body = pkgReqBody(req);
            if (null != params && params.size() > 0)
            {
                for (Iterator<Map.Entry<String, String>> it = params.entrySet().iterator(); it.hasNext(); )
                {
                    entry = it.next();
                    log.debug("--para {}: {}", entry.getKey(), entry.getValue());
                }
            }

            if (!StringUtils.isNull(body))
            {
                log.debug("--body {}", body);
            }
        } else
            log.info("REQ-{}: {}:{}", method, clientHost, url);
        // 记录处理时间
        long begin = System.currentTimeMillis();
        String resultJson = null;
        Object result = null;
        try
        {
            result = point.proceed();
            InputStream in = null;
            // 将结果保存为JSON
            resultJson = result2json(result);
        } catch (Throwable t)
        {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            Writer w = new B
//            t.printStackTrace(out);
            // 记录异常
            throw t;
        }

        // 处理时间
        long divide = System.currentTimeMillis() - begin;

        if (log.isDebugEnabled())
        {
            log.debug("--resp {} [{}]", result, divide);
            log.debug("=========== REQ-END ===========");
        } else
            log.info("RES: {} [{}]", result, divide);

        // 获取响应
        return result;
    }

    private static String pkgReqBody(HttpServletRequest req)
    {
        InputStream in = null;
        String str = null;
        try
        {
            in = req.getInputStream();
            if (!in.markSupported())
                return "[unsupported mark]";

            in.mark(req.getContentLength());

            str = HttpUtils.postStr(in);

            in.reset();
        } catch (IOException e)
        {
            return null;
        }


        return str;
    }

    /**
     * 封闭请求头部信息
     *
     * @param request
     * @return
     */
    private static Map<String, String> pkgHeader(HttpServletRequest request)
    {
        Map<String, String> header = new HashMap<>();

        Enumeration<String> enums = request.getHeaderNames();

        String key = null;
        String val = null;
        while (enums.hasMoreElements())
        {
            key = enums.nextElement();
            val = request.getHeader(key);
            header.put(key, val);
        }

        return header;
    }

    private static String result2json(Object result)
    {
        try
        {
            return JsonUtils.to(result);
        } catch (Throwable t)
        {
            return "{\"err\":\"" + t.getMessage() + "\"}";
        }
    }
}
