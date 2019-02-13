package com.imp.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.imp.common.RequestHolder;
import com.imp.dao.SysRoleUserMapper;
import com.imp.dao.SysUserMapper;
import com.imp.model.SysRoleUser;
import com.imp.model.SysUser;
import com.imp.util.IpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class SysRoleUserService {

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysUserMapper sysUserMapper;
//    @Resource
//    private SysLogMapper sysLogMapper;

    // 根据角色ID 获得是该角色的用户集合
    public List<SysUser> getListByRoleId(int roleId) {
        // 用户ID集合
        List<Integer> userIdList = sysRoleUserMapper.getUserIdListByRoleId(roleId);
        if (CollectionUtils.isEmpty(userIdList)) {
            return Lists.newArrayList();
        }
        //  根据用户ID集合得用户集合
        return sysUserMapper.getByIdList(userIdList);
    }


    public void changeRoleUsers(int roleId, List<Integer> userIdList) {
        List<Integer> originUserIdList = sysRoleUserMapper.getUserIdListByRoleId(roleId);
        // 检查是否修改了
        if (originUserIdList.size() == userIdList.size()) {
            Set<Integer> originUserIdSet = Sets.newHashSet(originUserIdList);
            Set<Integer> userIdSet = Sets.newHashSet(userIdList);
            originUserIdSet.removeAll(userIdSet);
            if (CollectionUtils.isEmpty(originUserIdSet)) {
                return;
            }
        }
        updateRoleUsers(roleId, userIdList);
        //saveRoleUserLog(roleId, originUserIdList, userIdList);
    }

    @Transactional
    public void updateRoleUsers(int roleId, List<Integer> userIdList) {
        // 删除旧数据
        sysRoleUserMapper.deleteByRoleId(roleId);

        if (CollectionUtils.isEmpty(userIdList)) {
            return;
        }
        // 批量插入新的数据
        List<SysRoleUser> roleUserList = Lists.newArrayList();
        for (Integer userId : userIdList) {
            SysRoleUser roleUser = SysRoleUser.builder().roleId(roleId).userId(userId).operator(RequestHolder.getCurrentUser().getUsername())
                    .operateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest())).operateTime(new Date()).build();
            roleUserList.add(roleUser);
        }
        sysRoleUserMapper.batchInsert(roleUserList);
    }

//    private void saveRoleUserLog(int roleId, List<Integer> before, List<Integer> after) {
//        SysLogWithBLOBs sysLog = new SysLogWithBLOBs();
//        sysLog.setType(LogType.TYPE_ROLE_USER);
//        sysLog.setTargetId(roleId);
//        sysLog.setOldValue(before == null ? "" : JsonMapper.obj2String(before));
//        sysLog.setNewValue(after == null ? "" : JsonMapper.obj2String(after));
//        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
//        sysLog.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
//        sysLog.setOperateTime(new Date());
//        sysLog.setStatus(1);
//        sysLogMapper.insertSelective(sysLog);
//    }
}
