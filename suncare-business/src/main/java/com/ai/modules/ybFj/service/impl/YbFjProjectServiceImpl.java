package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.ai.common.utils.BeanUtil;
import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.YbFjProjectDto;
import com.ai.modules.ybFj.entity.*;
import com.ai.modules.ybFj.mapper.YbFjProjectClueMapper;
import com.ai.modules.ybFj.mapper.YbFjProjectMapper;
import com.ai.modules.ybFj.service.*;
import com.ai.modules.ybFj.vo.ProjectOrgClientVo;
import com.ai.modules.ybFj.vo.YbFjProjectOrgVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 飞检项目信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class YbFjProjectServiceImpl extends ServiceImpl<YbFjProjectMapper, YbFjProject> implements IYbFjProjectService {

    @Autowired
    private IYbFjProjectOrgService ybFjProjectOrgService;
    @Autowired
    private IYbFjOrgService ybFjOrgService;
    @Resource
    private YbFjProjectClueMapper projectClueMapper;
    @Autowired
    private IYbFjProjectClueDtlService projectClueDtlService;
    @Autowired
    private IYbFjProjectTaskService projectTaskService;

    @Override
    public void saveProject(YbFjProjectDto dto) {
        YbFjProject project = BeanUtil.toBean(dto, YbFjProject.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String projectId = IdUtils.uuid();
        project.setProjectId(projectId);
        project.setCreateTime(DateUtils.getDate());
        project.setCreateUser(user.getUsername());
        project.setCreateUsername(user.getRealname());
        project.setProjectState(DcFjConstants.PROJECT_STATE_INIT);
        this.save(project);

        if(StringUtils.isNotBlank(dto.getOrgIds())) {
            //保存关联医院信息
            List<YbFjProjectOrg> projectOrgList = new ArrayList<YbFjProjectOrg>();
            String[] orgs = dto.getOrgIds().split(",");
            Map<String, String> orgMap = this.getOrgMap(orgs);
            for(String orgid : orgs) {
                YbFjProjectOrg projectOrg = new YbFjProjectOrg();
                projectOrg.setProjectOrgId(IdUtils.uuid());
                projectOrg.setProjectId(projectId);
                projectOrg.setOrgId(orgid);
                if(orgMap!=null && orgMap.containsKey(orgid)) {
                    projectOrg.setOrgName(orgMap.get(orgid));
                }
                projectOrg.setState(DcFjConstants.PROJECT_STATE_INIT);
                projectOrg.setCreateTime(DateUtils.getDate());
                projectOrg.setCreateUser(user.getUsername());
                projectOrg.setCreateUsername(user.getRealname());
                projectOrgList.add(projectOrg);
            }
            ybFjProjectOrgService.saveBatch(projectOrgList);
        }
    }

    private Map<String, String> getOrgMap(String[] orgIds) {
        QueryWrapper<YbFjOrg> wrapper = new QueryWrapper<YbFjOrg>();
        wrapper.in("org_id", orgIds);
        List<YbFjOrg> orgList = ybFjOrgService.list(wrapper);
        if(orgList!=null && orgList.size()>0) {
            Map<String, String> result = new HashMap<>();
            for(YbFjOrg org : orgList) {
                result.put(org.getOrgId(), org.getOrgName());
            }
            return result;
        }
        return null;
    }

    @Override
    public void updateProject(YbFjProjectDto dto) {
        YbFjProject old = this.getById(dto.getProjectId());
        String oldOrg = baseMapper.queryYbFjProjectOrgIds(dto.getProjectId());
        BeanUtil.copyProperties(dto, old, CopyOptions.create().ignoreNullValue());
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        old.setUpdateTime(DateUtils.getDate());
        old.setUpdateUser(user.getUsername());
        old.setUpdateUsername(user.getRealname());

        Set<String> addOrgs = new HashSet<String>();
        Set<String> delOrgs = new HashSet<String>();
        if(StringUtils.isBlank(dto.getOrgIds())) {
            //删除之前关联的医院
            if(StringUtils.isNotBlank(oldOrg)) {
                String[] orgs = oldOrg.split(",");
                delOrgs = Arrays.stream(orgs).collect(Collectors.toSet());
            }
        } else {
            if(StringUtils.isNotBlank(oldOrg)) {
                //之前不为空，则判断哪些新增、哪些删除
                String[] oldOrgs = oldOrg.split(",");
                String[] newOrgs = dto.getOrgIds().split(",");
                Set<String> oldSet = Arrays.stream(oldOrgs).collect(Collectors.toSet());
                Set<String> newSet = Arrays.stream(newOrgs).collect(Collectors.toSet());
                //计算新增的医院
                addOrgs.addAll(newSet);
                addOrgs.removeAll(oldSet);
                //计算删除的医院
                delOrgs.addAll(oldSet);
                delOrgs.removeAll(newSet);
            } else {
                //之前的为空，则新增所有
                String[] orgs = dto.getOrgIds().split(",");
                addOrgs = Arrays.stream(orgs).collect(Collectors.toSet());
            }
        }

        if(delOrgs.size()>0) {
            //移除关联的医院
            this.removeOrgs(dto.getProjectId(), StringUtils.join(delOrgs, ","));
        }
        if(addOrgs.size()>0) {
            //保存关联医院信息
            Map<String, String> orgMap = this.getOrgMap(addOrgs.toArray(new String[]{}));
            List<YbFjProjectOrg> projectOrgList = new ArrayList<YbFjProjectOrg>();
            for(String orgid : addOrgs) {
                YbFjProjectOrg projectOrg = new YbFjProjectOrg();
                projectOrg.setProjectOrgId(IdUtils.uuid());
                projectOrg.setProjectId(old.getProjectId());
                projectOrg.setOrgId(orgid);
                if(orgMap!=null && orgMap.containsKey(orgid)) {
                    projectOrg.setOrgName(orgMap.get(orgid));
                }
                projectOrg.setState(DcFjConstants.PROJECT_STATE_INIT);
                projectOrg.setCreateTime(DateUtils.getDate());
                projectOrg.setCreateUser(user.getUsername());
                projectOrg.setCreateUsername(user.getRealname());
                projectOrgList.add(projectOrg);
            }
            ybFjProjectOrgService.saveBatch(projectOrgList);
        }
        this.updateById(old);
    }

    @Override
    public IPage<YbFjProjectOrgVo> queryYbFjProjectOrgVo(IPage<YbFjProjectOrgVo> page, String projectId) {
        QueryWrapper<YbFjProjectOrgVo> wrapper = new QueryWrapper<>();
        wrapper.eq("a.project_id", projectId);
        return baseMapper.queryYbFjProjectOrgVo(page, wrapper);
    }

    @Override
    public void removeProject(String projectId) {
        //先删除关联的医院
        QueryWrapper<YbFjProjectOrg> queryWrapper = new QueryWrapper<YbFjProjectOrg>();
        queryWrapper.eq("project_id", projectId);
        ybFjProjectOrgService.remove(queryWrapper);
        //删除线索
        QueryWrapper<YbFjProjectClue> clueQueryWrapper = new QueryWrapper<>();
        clueQueryWrapper.eq("project_id", projectId);
        projectClueMapper.delete(clueQueryWrapper);
        //删除线索明细
        QueryWrapper<YbFjProjectClueDtl> clueDtlQueryWrapper = new QueryWrapper<>();
        clueDtlQueryWrapper.eq("project_id", projectId);
        projectClueDtlService.remove(clueDtlQueryWrapper);
        //删除任务
        QueryWrapper<YbFjProjectTask> taskQueryWrapper = new QueryWrapper<>();
        taskQueryWrapper.eq("project_id", projectId);
        projectTaskService.remove(taskQueryWrapper);
        //再删除项目
        this.removeById(projectId);
    }

    @Override
    public void batchRemoveProject(String projectIds) {
        //先删除关联的医院
        String[] ids = projectIds.split(",");
        QueryWrapper<YbFjProjectOrg> queryWrapper = new QueryWrapper<YbFjProjectOrg>();
        queryWrapper.in("project_id", ids);
        ybFjProjectOrgService.remove(queryWrapper);
        //删除线索
        QueryWrapper<YbFjProjectClue> clueQueryWrapper = new QueryWrapper<>();
        clueQueryWrapper.in("project_id", ids);
        projectClueMapper.delete(clueQueryWrapper);
        //删除线索明细
        QueryWrapper<YbFjProjectClueDtl> clueDtlQueryWrapper = new QueryWrapper<>();
        clueDtlQueryWrapper.in("project_id", ids);
        projectClueDtlService.remove(clueDtlQueryWrapper);
        //删除任务
        QueryWrapper<YbFjProjectTask> taskQueryWrapper = new QueryWrapper<>();
        taskQueryWrapper.in("project_id", ids);
        projectTaskService.remove(taskQueryWrapper);
        //再删除项目
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public void settingProjectState(String projectIds, String state) {
        String[] ids = projectIds.split(",");
        QueryWrapper<YbFjProject> wrapper = new QueryWrapper<YbFjProject>();
        wrapper.in("project_id", ids);
        YbFjProject project = new YbFjProject();
        project.setProjectState(state);
        this.update(project, wrapper);
    }

    @Override
    public void settingOrgState(String projectOrgIds, String state) {
        String[] ids = projectOrgIds.split(",");
        QueryWrapper<YbFjProjectOrg> wrapper = new QueryWrapper<YbFjProjectOrg>();
        wrapper.in("project_org_id", ids);
        YbFjProjectOrg projectOrg = new YbFjProjectOrg();
        projectOrg.setState(state);
        ybFjProjectOrgService.update(projectOrg, wrapper);
    }

    @Override
    public void removeOrgs(String projectId, String orgIds) {
        String[] array = orgIds.split(",");
        String value = "('" + StringUtils.join(array, "','") + "')";
        String sql = "select project_org_id from yb_fj_project_org where org_id in"+value;
        //删除线索
        QueryWrapper<YbFjProjectClue> clueQueryWrapper = new QueryWrapper<>();
        clueQueryWrapper.eq("project_id", projectId);
        clueQueryWrapper.inSql("project_org_id", sql);
        projectClueMapper.delete(clueQueryWrapper);
        //删除线索明细
        QueryWrapper<YbFjProjectClueDtl> clueDtlQueryWrapper = new QueryWrapper<>();
        clueDtlQueryWrapper.eq("project_id", projectId);
        clueDtlQueryWrapper.inSql("project_org_id", sql);
        projectClueDtlService.remove(clueDtlQueryWrapper);
        //删除任务
        QueryWrapper<YbFjProjectTask> taskQueryWrapper = new QueryWrapper<>();
        taskQueryWrapper.eq("project_id", projectId);
        taskQueryWrapper.inSql("project_org_id", sql);
        projectTaskService.remove(taskQueryWrapper);
        QueryWrapper<YbFjProjectOrg> wrapper = new QueryWrapper<YbFjProjectOrg>();
        wrapper.eq("project_id", projectId);
        wrapper.in("org_id", array);
        ybFjProjectOrgService.remove(wrapper);
    }

    @Override
    public IPage<ProjectOrgClientVo> queryYbFjProjectByOrg(IPage<ProjectOrgClientVo> page, String orgId, Wrapper<YbFjProject> wrapper) {
        return baseMapper.queryYbFjProjectByOrg(page, orgId, wrapper);
    }
}
