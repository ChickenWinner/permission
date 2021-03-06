package com.imp.service;

import com.google.common.base.Preconditions;
import com.imp.common.RequestHolder;
import com.imp.dao.SysDeptMapper;
import com.imp.dao.SysUserMapper;
import com.imp.exception.ParamException;
import com.imp.model.SysDept;
import com.imp.param.DeptParam;
import com.imp.util.BeanValidator;
import com.imp.util.IpUtil;
import com.imp.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 部门业务类
 *
 * @author Imp
 * email: 1318944013@qq.com
 * date: 2019/2/1 20:28
 */

@Service
public class SysDeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    // 新增部门
    public void save(DeptParam deptParam) {
        // 检验参数
        BeanValidator.check(deptParam);
        // 检验同级下是否存在同名部门
        if(checkExist(deptParam.getParentId(), deptParam.getName(), deptParam.getId())) {
            throw new ParamException("同级下存在同名部门！");
        }
        // 构造新增对象
        SysDept sysDept = SysDept.builder().name(deptParam.getName()).parentId(deptParam.getParentId())
                .seq(deptParam.getSeq()).remark(deptParam.getRemark()).build();
        sysDept.setLevel
                (LevelUtil.calculateLevel(getLevel(deptParam.getParentId()),deptParam.getParentId()));
        sysDept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysDept.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysDept.setOperateTime(new Date());
        sysDeptMapper.insertSelective(sysDept);
    }

    // 更新部门
    public void update(DeptParam param) {
        // 检查参数
        BeanValidator.check(param);
        // 待更新部门是否存在
        SysDept before = sysDeptMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新的部门不存在");
        // 检查同一级下名称是否重复
        if(checkExist(param.getParentId(), param.getName(), param.getId())) {
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        // 构造更新部门的信息
        SysDept after = SysDept.builder().id(param.getId()).name(param.getName())
                .parentId(param.getParentId()).seq(param.getSeq()).remark(param.getRemark()).build();
        after.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()), param.getParentId()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());

        updateWithChild(before, after);
        //sysLogService.saveDeptLog(before, after);
    }

    // 更新子部门level
    @Transactional
    public void updateWithChild(SysDept before, SysDept after) {
        // 新level
        String newLevelPrefix = after.getLevel();
        // 旧level
        String oldLevelPrefix = before.getLevel();
        // 如果level更新了，那么它下面的部门的level一起更新
        if (!after.getLevel().equals(before.getLevel())) {
            // 获得下面的部门
            List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(before.getLevel());
            if (CollectionUtils.isNotEmpty(deptList)) {
                for (SysDept dept : deptList) {
                    String level = dept.getLevel();
                    if (level.indexOf(oldLevelPrefix) == 0) {
                        level = newLevelPrefix + level.substring(oldLevelPrefix.length());
                        dept.setLevel(level);
                    }
                }
                // 批量更新level
                sysDeptMapper.batchUpdateLevel(deptList);
            }
        }
        sysDeptMapper.updateByPrimaryKey(after);
    }

    // 同级下是否存在同名部门
    private boolean checkExist(Integer parentId, String deptName, Integer deptId) {
        return sysDeptMapper.countByNameAndParentId(parentId, deptName, deptId) > 0;
    }

    // 根据ID获得层级
    private String getLevel(Integer deptId) {
        SysDept sysDept = sysDeptMapper.selectByPrimaryKey(deptId);
        if(sysDept == null) {
            return null;
        }
        return sysDept.getLevel();
    }

    // 删除部门
    public void delete(int deptId) {
        // 判断部门是否存在
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        Preconditions.checkNotNull(dept, "待删除的部门不存在，无法删除");
        // 删除的部门下是否有子部门
        if (sysDeptMapper.countByParentId(dept.getId()) > 0) {
            throw new ParamException("当前部门下面有子部门，无法删除");
        }
        // 删除的部门下是否有用户
        if(sysUserMapper.countByDeptId(dept.getId()) > 0) {
            throw new ParamException("当前部门下面有用户，无法删除");
        }
        sysDeptMapper.deleteByPrimaryKey(deptId);
    }
}
