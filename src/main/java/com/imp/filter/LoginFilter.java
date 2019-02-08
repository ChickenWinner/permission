package com.imp.filter;

import com.imp.common.RequestHolder;
import com.imp.model.SysUser;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Imp
 * email: 1318944013@qq.com
 * date: 2019/2/8 14:34
 */

// 登录过滤器
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        // 从session中查看是否登录
        SysUser sysUser = (SysUser)req.getSession().getAttribute("user");
        // 如果没有登录 跳转到登录假面
        if(sysUser == null) {
            String path = "/signin.jsp";
            resp.sendRedirect(path);
            return;
        }
        // 已经登录 将用户信息和request信息放入 threadLocal
        RequestHolder.add(sysUser);
        RequestHolder.add(req);
        // 放行
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
