package cn.lyjuan.base.http.filter;

import cn.lyjuan.base.exception.impl.BaseCode;
import cn.lyjuan.base.http.aop.service.IHeaderService;
import cn.lyjuan.base.http.vo.res.BaseRes;
import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.StringUtils;
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
            if (!StringUtils.isNull(errMsg)) {
                // 此处直接返回信息
                // 或者使用 BasicErrorController 处理请求信息
                String msg = isDebug ? errMsg : BaseCode.PARAM_INVALID.msg();
                BaseRes res = BaseRes.res(BaseCode.PARAM_INVALID, msg);
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
