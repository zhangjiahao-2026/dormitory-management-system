package com.example.springboot.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.common.PasswordUtil;
import com.example.springboot.entity.DormManager;
import com.example.springboot.mapper.DormManagerMapper;
import com.example.springboot.service.DormManagerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class DormManagerServiceImpl extends ServiceImpl<DormManagerMapper, DormManager> implements DormManagerService {

    /**
     * 注入DAO层对象
     */
    @Resource
    private DormManagerMapper dormManagerMapper;

    /**
     * 宿管登录
     */
    @Override
    public DormManager dormManagerLogin(String username, String password) {
        DormManager dormManager = dormManagerMapper.selectById(username);
        if (dormManager == null || !PasswordUtil.matches(password, dormManager.getPassword())) {
            return null;
        }
        if (PasswordUtil.needsUpgrade(dormManager.getPassword())) {
            dormManager.setPassword(PasswordUtil.encode(password));
            dormManagerMapper.updateById(dormManager);
        }
        return dormManager;
    }

    /**
     * 宿管新增
     */
    @Override
    public int addNewDormManager(DormManager dormManager) {
        dormManager.setPassword(PasswordUtil.encode(dormManager.getPassword()));
        int insert = dormManagerMapper.insert(dormManager);
        return insert;
    }

    /**
     * 宿管查找
     */
    @Override
    public Page find(Integer pageNum, Integer pageSize, String search) {
        Page page = new Page<>(pageNum, pageSize);
        QueryWrapper<DormManager> qw = new QueryWrapper<>();
        qw.like("name", search);
        Page dormManagerPage = dormManagerMapper.selectPage(page, qw);
        return dormManagerPage;
    }

    /**
     * 宿管信息更新
     */
    @Override
    public int updateNewDormManager(DormManager dormManager) {
        DormManager stored = dormManagerMapper.selectById(dormManager.getUsername());
        if (stored == null) {
            return 0;
        }
        if (dormManager.getPassword() == null || dormManager.getPassword().isBlank()) {
            dormManager.setPassword(stored.getPassword());
        } else if (!PasswordUtil.isBcrypt(dormManager.getPassword())) {
            dormManager.setPassword(PasswordUtil.encode(dormManager.getPassword()));
        }
        int i = dormManagerMapper.updateById(dormManager);
        return i;
    }

    /**
     * 宿管删除
     */
    @Override
    public int deleteDormManager(String username) {
        int i = dormManagerMapper.deleteById(username);
        return i;
    }


}
