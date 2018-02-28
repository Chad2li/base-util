package cn.lyjuan.base.http.log;

import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

//@Component
@ServletComponentScan
@WebFilter(urlPatterns = "*",filterName = "logFilter")
public class LogFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        //备份HttpServletRequest
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        httpRequest = new BufferedServletRequestWrapper(httpRequest);

        //将request 传到下一个Filter
        filterChain.doFilter(httpRequest, servletResponse);
    }

    @Override
    public void destroy()
    {

    }
}
