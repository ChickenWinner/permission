package com.imp.service;

import com.google.common.base.Preconditions;
import com.imp.beans.PageQuery;
import com.imp.beans.PageResult;
import com.imp.common.RequestHolder;
import com.imp.dao.SysUserMapper;
import com.imp.exception.ParamException;
import com.imp.model.SysUser;
import com.imp.param.UserParam;
import com.imp.util.BeanValidator;
import com.imp.util.IpUtil;
import com.imp.util.MD5Util;
import com.imp.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Imp
 * email: 1318944013@qq.com
 * date: 2019/2/6 19:33
 */


@Service
public class SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    // 新增用户
    public void save(UserParam param) {
        // 检验参数
        BeanValidator.check(param);
        // 检查邮箱是否存在
        if(checkEmailExist(param.getMail(), param.getId())) {
            throw new ParamException("email已经存在");
        }
        // 检查电话是否存在
        if(checkTelephoneExist(param.getTelephone(), param.getId())) {
            throw new ParamException("telephone已经存在");
        }

        String password = PasswordUtil.randomPassword();
        password = "112233";
        // 加密密码
        String encryptedPassword = MD5Util.encrypt(password);
        //TODO 发送密码到邮箱
        // 构造新增对象
        SysUser user = SysUser.builder().username(param.getUsername()).telephone(param.getTelephone()).
                mail(param.getMail()).password(encryptedPassword).deptId(param.getDeptId()).
                status(param.getStatus()).remark(param.getRemark()).build();
        user.setOperator(RequestHolder.getCurrentUser().getUsername());
        user.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        user.setOperateTime(new Date());

        sysUserMapper.insertSelective(user);

    }

    // 更新用户
    public void update(UserParam param) {
        BeanValidator.check(param);
        if(checkTelephoneExist(param.getTelephone(), param.getId())) {
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(param.getMail(), param.getId())) {
            throw new ParamException("邮箱已被占用");
        }
        // 检查待更新对象是否存在
        SysUser before = sysUserMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新的用户不存在");
        // 构造更新对象
        SysUser after = SysUser.builder().id(param.getId()).username(param.getUsername()).
                telephone(param.getTelephone()).mail(param.getMail()).deptId(param.getDeptId()).
                status(param.getStatus()).remark(param.getRemark()).build();
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());
        sysUserMapper.updateByPrimaryKeySelective(after);
//        sysLogService.saveUserLog(before, after);
    }

    // 分页查询部门下的用户
    public PageResult<SysUser> getPageByDeptId(int deptId, PageQuery page) {
        BeanValidator.check(page);
        // 查询人数
        int count = sysUserMapper.countByDeptId(deptId);
        // 人数是否大于0
        if (count > 0) {
            // 大于0 查询用户集合并返回
            List<SysUser> list = sysUserMapper.getPageByDeptId(deptId, page);
            return PageResult.<SysUser>builder().total(count).data(list).build();
        }
        // 否则返回空
        return PageResult.<SysUser>builder().build();
    }



    // 检查邮箱是否存在
    private boolean checkEmailExist(String email, Integer userId) {
        return sysUserMapper.countByMail(email,userId) > 0;
    }

    // 检查电话是否存在
    private boolean checkTelephoneExist(String telephone, Integer userId) {
        return sysUserMapper.countByTelephone(telephone,userId) > 0;
    }

    // 根据关键字查询指定用户
    public SysUser findByKeyword(String keyWord) {
        return sysUserMapper.findByKeyword(keyWord);
    }


}
