package com.imp.service;

import com.google.common.collect.Lists;
import com.imp.beans.CacheKeyConstants;
import com.imp.common.RequestHolder;
import com.imp.dao.SysAclMapper;
import com.imp.dao.SysRoleAclMapper;
import com.imp.dao.SysRoleUserMapper;
import com.imp.model.SysAcl;
import com.imp.model.SysUser;
import com.imp.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysCoreService {

    @Resource
    private SysAclMapper sysAclMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleAclMapper sysRoleAclMapper;
    @Resource
    private SysCacheService sysCacheService;

    // 得到当前用户的权限集合
    public List<SysAcl> getCurrentUserAclList() {
        int userId = RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    // 得到角色的权限集合
    public List<SysAcl> getRoleAclList(int roleId) {
        // 根据角色ID得到权限ID集合
        List<Integer> aclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(Lists.<Integer>newArrayList(roleId));
        if (CollectionUtils.isEmpty(aclIdList)) {
            return Lists.newArrayList();
        }
        // 根据权限ID得到权限
        return sysAclMapper.getByIdList(aclIdList);
    }

    // 得到用户的权限集合
    // 用户->角色->权限
    public List<SysAcl> getUserAclList(int userId) {
        // 如果是超级管理员，返回所有权限
        if (isSuperAdmin()) {
            return sysAclMapper.getAll();
        }
        // 根据用户id得到相应的角色ID集合
        List<Integer> userRoleIdList = sysRoleUserMapper.getRoleIdListByUserId(userId);
        if (CollectionUtils.isEmpty(userRoleIdList)) {
            return Lists.newArrayList();
        }
        // 根据角色id得到相应的权限ID
        List<Integer> userAclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(userRoleIdList);
        if (CollectionUtils.isEmpty(userAclIdList)) {
            return Lists.newArrayList();
        }
        // 根据权限ID获取权限
        return sysAclMapper.getByIdList(userAclIdList);
    }

    private boolean isSuperAdmin() {
        // 这里是我自己定义了一个假的超级管理员规则，实际中要根据项目进行修改
        // 可以是配置文件获取，可以指定某个用户，也可以指定某个角色
        SysUser sysUser = RequestHolder.getCurrentUser();
        if (sysUser.getMail().contains("admin")) {
            return true;
        }
        return false;
    }


    public boolean hasUrlAcl(String url) {
        // 如果是超级管理员 拥有权限
        if (isSuperAdmin()) {
            return true;
        }
        // 通过url查询权限点
        List<SysAcl> aclList = sysAclMapper.getByUrl(url);
        // 如果权限点为空，说明不在意的url，放行
        if (CollectionUtils.isEmpty(aclList)) {
            return true;
        }

        List<SysAcl> userAclList = getCurrentUserAclListFromCache();
        // 获取当前用户的权限点
        //List<SysAcl> userAclList = getCurrentUserAclList();
        // 将权限点ID取出生成set
        Set<Integer> userAclIdSet = userAclList.stream().map(acl -> acl.getId()).collect(Collectors.toSet());

        // 防止权限点全无效，没有返回true
        boolean hasValidAcl = false;
        // 规则：只要有一个权限点有权限，那么我们就认为有访问权限

        for (SysAcl acl : aclList) {
            // 判断一个用户是否具有某个权限点的访问权限
            if (acl == null || acl.getStatus() != 1) { // 权限点无效
                continue;
            }
            hasValidAcl = true;
            if (userAclIdSet.contains(acl.getId())) {
                return true;
            }
        }
        if (!hasValidAcl) {
            return true;
        }
        return false;
    }

    public List<SysAcl> getCurrentUserAclListFromCache() {
        // 获取当前用户的ID
        int userId = RequestHolder.getCurrentUser().getId();
        // 从redis中取值
        String cacheValue = sysCacheService.getFromCache(CacheKeyConstants.USER_ACLS, String.valueOf(userId));
        // 如果是空
        if (StringUtils.isBlank(cacheValue)) {
            // 查询数据库
            List<SysAcl> aclList = getCurrentUserAclList();
            // 将值缓存到redis
            if (CollectionUtils.isNotEmpty(aclList)) {
                sysCacheService.saveCache(JsonMapper.obj2String(aclList), 600, CacheKeyConstants.USER_ACLS, String.valueOf(userId));
            }
            return aclList;
        }
        return JsonMapper.string2Obj(cacheValue, new TypeReference<List<SysAcl>>() {
        });
    }
}
