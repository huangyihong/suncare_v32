package com.ai.modules.system.service;

import com.ai.modules.system.entity.SysDatasource;
import com.ai.modules.system.vo.RoleVo;
import com.ai.modules.system.vo.SysDatasourceVo;
import com.ai.modules.ybFj.vo.OrgUserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 项目地配置
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
public interface ISysDatasourceService extends IService<SysDatasource> {

    void addByTransactional(SysDatasource sysDatasource) throws Exception;

    void updateByTransactional(SysDatasource sysDatasource);

    void removeByTransactional(String id);

    void removeByTransactionals(String ids);

    SysDatasource getByCode(String code);

    IPage<RoleVo> getRoleList(Page<RoleVo> page, SysDatasourceVo sysDatasource);

    void addRoleBatch(String code, String ids);

    void delRoleBatch(String code, String ids);

    IPage<SysDatasource> getPage(Page<SysDatasource> page, QueryWrapper<SysDatasource> queryWrapper);
}
