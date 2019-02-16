package com.imp.controller;

import com.imp.common.JsonData;
import com.imp.dto.DeptLevelDto;
import com.imp.param.DeptParam;
import com.imp.service.SysDeptService;
import com.imp.service.SysTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Imp
 * email: 1318944013@qq.com
 * date: 2019/2/1 20:46
 */

@Controller
@Slf4j
@RequestMapping("sys/dept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysTreeService sysTreeService;


    // 跳转到部门首页
    @RequestMapping("dept.page")
    public ModelAndView page(){
        return new ModelAndView("dept");
    }

    @RequestMapping("save.json")
    @ResponseBody
    public JsonData saveDept(DeptParam deptParam) {
        sysDeptService.save(deptParam);
        return JsonData.success("新增部门成功");
    }

    @RequestMapping("tree.json")
    @ResponseBody
    public JsonData tree() {
        // 得到部门树
        List<DeptLevelDto> deptLevelList = sysTreeService.deptTree();
        return JsonData.success(deptLevelList);
    }

    @RequestMapping("update.json")
    @ResponseBody
    public JsonData updateDept(DeptParam deptParam) {
        // 得到部门树
       sysDeptService.update(deptParam);
       return JsonData.success("更新部门成功");
    }

    @RequestMapping("/delete.json")
    @ResponseBody
    public JsonData delete(int id) {
        sysDeptService.delete(id);
        return JsonData.success();
    }

}
