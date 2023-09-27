package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.YbFjProjectDto;
import com.ai.modules.ybFj.entity.YbFjProject;
import com.ai.modules.ybFj.vo.ProjectOrgClientVo;
import com.ai.modules.ybFj.vo.YbFjProjectOrgVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 飞检项目信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
public interface IYbFjProjectService extends IService<YbFjProject> {

    void saveProject(YbFjProjectDto dto);

    void updateProject(YbFjProjectDto dto);

    IPage<YbFjProjectOrgVo> queryYbFjProjectOrgVo(IPage<YbFjProjectOrgVo> page, String projectId);

    void removeProject(String projectId);

    void batchRemoveProject(String projectIds);

    /**
     *
     * 功能描述：变更项目状态
     * @author zhangly
     * @date 2023-03-03 12:06:17
     *
     * @param projectIds
     * @param state
     *
     * @return void
     *
     */
    void settingProjectState(String projectIds, String state);

    /**
     *
     * 功能描述：变更关联的医疗机构的状态
     * @author zhangly
     * @date 2023-03-03 09:46:30
     *
     * @param projectOrgIds
     * @param state
     *
     * @return void
     *
     */
    void settingOrgState(String projectOrgIds, String state);

    /**
     *
     * 功能描述：删除关联的医疗机构
     * @author zhangly
     * @date 2023-03-03 12:05:38
     *
     * @param projectId
     * @param orgIds
     *
     * @return void
     *
     */
    void removeOrgs(String projectId, String orgIds);

    IPage<ProjectOrgClientVo> queryYbFjProjectByOrg(IPage<ProjectOrgClientVo> page, String orgId, Wrapper<YbFjProject> wrapper);
}
