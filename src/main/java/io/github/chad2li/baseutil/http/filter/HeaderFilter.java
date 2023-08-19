package io.github.chad2li.baseutil.http.filter;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.chad2li.baseutil.exception.impl.BaseCode;
import io.github.chad2li.baseutil.http.aop.service.IHeaderService;
import io.github.chad2li.baseutil.http.vo.res.BaseRes;
import io.github.chad2li.baseutil.util.JsonUtils;
import io.github.chad2li.baseutil.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 处理请求头部header信息
 */
@Data
@Slf4j
@Order(HeaderFilter.ORDER)
@WebFilter(urlPatterns = {"*"}, filterName = HeaderFilter.NAME)
public class HeaderFilter<T extends IHeaderService.AHeaderParam> implements Filter {

    public static final String NAME = "baseHeaderFilterName";

    public static final int ORDER = LoggingFilter.ORDER + 1;

    /**
     * 是否是测试环境，测试环境会返回更多的信息便于调试。<br/>
     * 生产环境开启debug易导致安全问题
     */
    private boolean isDebug = false;
    /**
     * 存入header信息的实例化类<br/>
     * 必须有一个空构造器
     */
    private Class<T> headerCls;

    private IHeaderService<T> headerService;

    private FilterProperties filterProperties;

    public HeaderFilter(Class<T> headerCls, IHeaderService<T> headerService) {
        this.headerCls = headerCls;
        this.headerService = headerService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        ContentCachingRequestWrapper req = (ContentCachingRequestWrapper) request;

        if (!FilterProperties.isSkip(this.filterProperties, req.getRequestURI())) {
            // 解析头部信息
            T header = IHeaderService.AHeaderParam.parse((HttpServletRequest) request, headerCls);
            // 校验头部信息
            String errMsg = header.check();
            // 校验头部信息未通过
            if (CharSequenceUtil.isNotEmpty(errMsg)) {
                // 此处直接返回信息
                // 或者使用 BasicErrorController 处理请求信息
                String msg = isDebug ? errMsg : BaseCode.PARAM_INVALID.msg();
                if (log.isDebugEnabled())
                    log.debug("header check invalid: {}", msg);
                BaseRes<Void> res = BaseRes.resp(BaseCode.PARAM_INVALID, msg, null);
                String json = JsonUtils.to(res);
                response.getWriter().print(json);
                response.flushBuffer();
                return;
            }

            // 缓存头部信息
            headerService.cache(header);
        }

        chain.doFilter(request, response);
    }
}
