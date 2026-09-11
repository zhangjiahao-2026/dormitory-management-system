package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.common.PasswordUtil;
import com.example.springboot.entity.Admin;
import com.example.springboot.mapper.AdminMapper;
import com.example.springboot.service.AdminService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    /**
     * 注入DAO层对象
     */
    @Resource
    private AdminMapper adminMapper;

    /**
     * 管理员登录
     */
    @Override
    public Admin adminLogin(String username, String password) {
        Admin admin = adminMapper.selectById(username);
        if (admin == null || !PasswordUtil.matches(password, admin.getPassword())) {
            return null;
        }
        if (PasswordUtil.needsUpgrade(admin.getPassword())) {
            admin.setPassword(PasswordUtil.encode(password));
            adminMapper.updateById(admin);
        }
        return admin;
    }

    /**
     * 管理员信息更新
     */
    @Override
    public int updateAdmin(Admin admin) {
        Admin stored = adminMapper.selectById(admin.getUsername());
        if (stored == null) {
            return 0;
        }
        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            admin.setPassword(stored.getPassword());
        } else if (!PasswordUtil.isBcrypt(admin.getPassword())) {
            admin.setPassword(PasswordUtil.encode(admin.getPassword()));
        }
        int i = adminMapper.updateById(admin);
        return i;
    }

}
