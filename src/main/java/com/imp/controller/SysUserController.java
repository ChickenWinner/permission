package com.imp.controller;

import com.imp.common.JsonData;
import com.imp.dto.DeptLevelDto;
import com.imp.param.DeptParam;
import com.imp.param.UserParam;
import com.imp.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
}
