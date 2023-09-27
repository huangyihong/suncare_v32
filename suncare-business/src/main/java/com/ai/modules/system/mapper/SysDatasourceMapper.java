package com.ai.modules.system.mapper;

import com.ai.modules.system.entity.SysDatasource;
import com.ai.modules.system.vo.RoleVo;
import com.ai.modules.system.vo.SysDatasourceVo;
import com.ai.modules.ybFj.vo.OrgUserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 项目地配置
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
public interface SysDatasourceMapper extends BaseMapper<SysDatasource> {

    IPage<RoleVo> getRoleList(Page<RoleVo> page, @Param("query") SysDatasourceVo sysDatasource);

    void addRoleBatch(String code, String ids);

    void delRoleBatch(String code, String ids);

    IPage<SysDatasource> getPage(Page<SysDatasource> page, @Param(Constants.WRAPPER)QueryWrapper<SysDatasource> queryWrapper);
}
