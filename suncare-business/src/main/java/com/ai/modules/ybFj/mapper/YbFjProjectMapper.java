package com.ai.modules.ybFj.mapper;

import java.util.List;

import com.ai.modules.ybFj.vo.ProjectOrgClientVo;
import com.ai.modules.ybFj.vo.YbFjProjectOrgVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.ybFj.entity.YbFjProject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 飞检项目信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
public interface YbFjProjectMapper extends BaseMapper<YbFjProject> {

    IPage<YbFjProjectOrgVo> queryYbFjProjectOrgVo(IPage<YbFjProjectOrgVo> page, @Param(Constants.WRAPPER) Wrapper<YbFjProjectOrgVo> wrapper);

    String queryYbFjProjectOrgIds(@Param("projectId") String projectId);

    IPage<ProjectOrgClientVo> queryYbFjProjectByOrg(IPage<ProjectOrgClientVo> page, @Param("orgId") String orgId, @Param(Constants.WRAPPER) Wrapper<YbFjProject> wrapper);
}
