package com.imp.controller;

import com.imp.model.SysUser;
import com.imp.service.SysUserService;
import com.imp.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class UserController {

    @Resource
    private SysUserService sysUserService;

    @RequestMapping("/logout.page")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getSession().invalidate(); // 清空session
        // 跳转页面
        String path = "signin.jsp";
        response.sendRedirect(path);
    }

    @RequestMapping("/login.page")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 查询指定用户 username可能是手机号或者邮箱
        SysUser sysUser = sysUserService.findByKeyword(username);
        String errorMsg = "";
        // 待跳转的界面(可能有或无)
        String ret = request.getParameter("ret");

        // 判断各种参数
        if (StringUtils.isBlank(username)) {
            errorMsg = "用户名不可以为空";
        } else if (StringUtils.isBlank(password)) {
            errorMsg = "密码不可以为空";
        } else if (sysUser == null) {
            errorMsg = "查询不到指定的用户";
        } else if (!sysUser.getPassword().equals(MD5Util.encrypt(password))) {
            errorMsg = "用户名或密码错误";
        } else if (sysUser.getStatus() != 1) {
            errorMsg = "用户已被冻结，请联系管理员";
        } else {
            // 登录成功
            // 存储用户信息
            request.getSession().setAttribute("user", sysUser);
            if (StringUtils.isNotBlank(ret)) {
                // 跳转界面
                response.sendRedirect(ret);
                return;
            } else {
                // 跳转到管理员界面
                response.sendRedirect("/admin/index.page"); //TODO
                return;
            }
        }

        request.setAttribute("error", errorMsg);
        request.setAttribute("username", username);
        if (StringUtils.isNotBlank(ret)) {
            request.setAttribute("ret", ret);
        }
        String path = "signin.jsp";
        request.getRequestDispatcher(path).forward(request, response);
    }
}
