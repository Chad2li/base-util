package cn.lyjuan.base.http.aop.log;

import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * 将req封装成可重复读取
 */
@ServletComponentScan
@WebFilter(urlPatterns = "*", filterName = "logFilter")
public class LogFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //备份HttpServletRequest
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        httpRequest = new BufferedServletRequestWrapper(httpRequest);
        InputStream in = httpRequest.getInputStream();
        in.mark(Integer.MAX_VALUE);

        //将request 传到下一个Filter
        filterChain.doFilter(httpRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
