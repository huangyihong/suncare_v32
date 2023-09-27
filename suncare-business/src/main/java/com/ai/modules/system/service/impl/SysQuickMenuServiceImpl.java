package com.ai.modules.system.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.system.entity.SysQuickMenu;
import com.ai.modules.system.mapper.SysQuickMenuMapper;
import com.ai.modules.system.service.ISysQuickMenuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 用户快捷菜单
 * @Author: jeecg-boot
 * @Date:   2019-12-17
 * @Version: V1.0
 */
@Service
public class SysQuickMenuServiceImpl extends ServiceImpl<SysQuickMenuMapper, SysQuickMenu> implements ISysQuickMenuService {

    @Override
    public List<SysQuickMenu> queryByUser() {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getId();
        return this.baseMapper.selectList(new QueryWrapper<SysQuickMenu>().eq("USER_ID",userId).orderByAsc("ORDER_NO"));
    }

    @Override
    public void saveUserMenu(String[] menuIds) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getId();
        // 删除旧数据
        this.baseMapper.delete(new QueryWrapper<SysQuickMenu>().eq("USER_ID",userId));
        // 插入新数据
        SysQuickMenu quickMenu = new SysQuickMenu();
        quickMenu.setUserId(userId);
        int i = 1;
        for(String menuId: menuIds){
            quickMenu.setId(IdUtils.uuid());
            quickMenu.setMenuId(menuId);
            quickMenu.setOrderNo(i++);
            this.baseMapper.insert(quickMenu);
        }
    }
}
