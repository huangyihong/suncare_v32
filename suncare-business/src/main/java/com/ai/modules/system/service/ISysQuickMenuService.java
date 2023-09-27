package com.ai.modules.system.service;

import com.ai.modules.system.entity.SysQuickMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 用户快捷菜单
 * @Author: jeecg-boot
 * @Date:   2019-12-17
 * @Version: V1.0
 */
public interface ISysQuickMenuService extends IService<SysQuickMenu> {

    List<SysQuickMenu> queryByUser();
    void saveUserMenu(String[] menuIds);

}
