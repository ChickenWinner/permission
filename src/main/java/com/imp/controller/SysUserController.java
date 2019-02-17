package com.imp.controller;

import com.google.common.collect.Maps;
import com.imp.beans.PageQuery;
import com.imp.beans.PageResult;
import com.imp.common.JsonData;
import com.imp.dto.DeptLevelDto;
import com.imp.param.DeptParam;
import com.imp.param.UserParam;
import com.imp.service.SysRoleService;
import com.imp.service.SysTreeService;
import com.imp.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * @author Imp
 * email: 1318944013@qq.com
 * date: 2019/2/6 19:27
 */

@RequestMapping("sys/user")
@Controller
public class SysUserController {


    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysTreeService sysTreeService;
    @Autowired
    private SysRoleService sysRoleService;

    @RequestMapping("save.json")
    @ResponseBody
    public JsonData saveUser(UserParam param) {
        sysUserService.save(param);
        return JsonData.success("新增用户成功");
    }

    @RequestMapping("update.json")
    @ResponseBody
    public JsonData updateUser(UserParam param) {
        // 得到部门树
        sysUserService.update(param);
        return JsonData.success("更新用户成功");
    }

    @RequestMapping("page.json")
    @ResponseBody
    public JsonData listUsers(int deptId, PageQuery pageQuery) {
        PageResult pageResult = sysUserService.getPageByDeptId(deptId, pageQuery);
        return JsonData.success(pageResult);
    }

    @RequestMapping("/acls.json")
    @ResponseBody
    public JsonData acls(int userId) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("acls", sysTreeService.userAclTree(userId));
        map.put("roles", sysRoleService.getRoleListByUserId(userId));
        return JsonData.success(map);
    }

    @RequestMapping("/noAuth.page")
    public ModelAndView noAuth() {
        return new ModelAndView("noAuth");
    }
}
