package com.ai.modules.task.service.impl;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.ExcelXUtils;
import com.ai.modules.config.entity.DwbMasterInfoOrg;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalOrgan;
import com.ai.modules.config.entity.MedicalSysDict;
import com.ai.modules.config.mapper.DwbMasterInfoOrgMapper;
import com.ai.modules.config.mapper.MedicalOrganMapper;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectClient;
import com.ai.modules.task.mapper.TaskProjectMapper;
import com.ai.modules.task.service.ITaskCommonConditionSetService;
import com.ai.modules.task.service.ITaskProjectClientService;
import com.ai.modules.task.service.ITaskProjectService;
import com.ai.modules.task.vo.TaskProjectVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 任务项目
 * @Author: jeecg-boot
 * @Date:   2020-01-03
 * @Version: V1.0
 */
@Service
public class TaskProjectServiceImpl extends ServiceImpl<TaskProjectMapper, TaskProject> implements ITaskProjectService {

    @Autowired
    ITaskCommonConditionSetService taskCommonConditionSetService;

    @Autowired
    ITaskProjectClientService taskProjectClientService;

    @Autowired
    MedicalOrganMapper organMapper;

    @Autowired
    DwbMasterInfoOrgMapper dwbOrgMapper;


    @Override
    public IPage<TaskProjectVO> pageVO(Page<TaskProject> page, Wrapper<TaskProject> wrapper) {
        return this.baseMapper.selectPageVO(page, wrapper);
    }

    @Override
    @Transactional
    public void saveProject(TaskProjectVO taskProject) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        taskProject.setDataSource(user.getDataSource());
        taskProject.setCreateUser(user.getId());
        taskProject.setCreateUserName(user.getRealname());
        this.save(taskProject);

        String[] ids = taskProject.getClientIds().split(",");
        String projectId = taskProject.getProjectId();

        if(ids.length > 0){
            List<TaskProjectClient> taskProjectClients = new ArrayList<>();
            for(String id: ids){
                TaskProjectClient taskProjectClient = new TaskProjectClient();
                taskProjectClient.setProjectId(projectId);
                taskProjectClient.setUserId(id);
                taskProjectClients.add(taskProjectClient);
            }
            if(!taskProjectClientService.saveBatch(taskProjectClients)){
                throw new Exception("客户关联失败！");
            }
        }

        List<TaskCommonConditionSet> conditionSets = taskProject.getConditionSets();
        if(conditionSets.size() > 0){
            for(TaskCommonConditionSet bean: conditionSets){
                bean.setRuleId(projectId);
                bean.setType("exclude");
            }
            if(!taskCommonConditionSetService.saveBatch(conditionSets)){
                throw new Exception("保存过滤条件失败！");
            }
        }
    }

    @Override
    public void updateProjectById(TaskProjectVO taskProject) throws Exception {
        String[] ids = taskProject.getClientIds().split(",");
        String projectId = taskProject.getProjectId();
        List<TaskProjectClient> taskProjectClients = new ArrayList<>();
        if(ids.length > 0){
            for(String id: ids){
                TaskProjectClient taskProjectClient = new TaskProjectClient();
                taskProjectClient.setProjectId(projectId);
                taskProjectClient.setUserId(id);
                taskProjectClients.add(taskProjectClient);
            }
            taskProjectClientService.remove(new QueryWrapper<TaskProjectClient>().eq("PROJECT_ID", projectId));
            if(!taskProjectClientService.saveBatch(taskProjectClients)){
                throw new Exception("客户关联失败！");
            }
        }

        List<TaskCommonConditionSet> conditionSets = taskProject.getConditionSets();
        if(conditionSets.size() > 0){
            for(TaskCommonConditionSet bean: conditionSets){
                bean.setRuleId(projectId);
                bean.setType("exclude");
            }
            taskCommonConditionSetService.removeByRuleId(projectId);
            if(!taskCommonConditionSetService.saveBatch(conditionSets)){
                throw new Exception("保存过滤条件失败！");
            }
        }


        this.updateById(taskProject);
    }

    @Override
    public Result<?> importOrgExcel(MultipartFile file, LoginUser user)throws Exception {
        String mappingFieldStr = "code,name";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");
        return importOrgExcel(file, user,mappingFields);
    }

    private Result<?> importOrgExcel(MultipartFile file, LoginUser user, String[] mappingFields) throws Exception, IOException {
        System.out.println("开始导入时间：" + DateUtils.now());

        List<MedicalOrgan> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalOrgan.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(MedicalOrgan.class, mappingFields, 0, 1, file.getInputStream());
        }
        if (list.size() == 0) {
            return Result.error("上传文件内容为空");
        }
        String message = "";
        Set<String> codeSet = new HashSet<String>();

        System.out.println("校验开始："+DateUtils.now() );
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalOrgan bean = list.get(i);
            if (StringUtils.isBlank(bean.getCode())) {
                message += "导入的数据中“医疗机构编码”不能为空，如：第" + (i + 2) + "行数据“医疗机构编码”为空\n";
                flag = false;
            }
            if (StringUtils.isBlank(bean.getName())) {
                message += "导入的数据中“医疗机构名称”不能为空，如：第" + (i + 2) + "行数据“医疗机构名称”为空\n";
                flag = false;
            }
            //判断code在excel中是否重复
            if(codeSet.contains(bean.getCode())){
                message += "导入的数据中“医疗机构编码”不能重复，如：第" + (i + 2) + "行数据医疗机构编码为“"+bean.getCode()+"”在excel中重复\n";
                flag = false;
            }
            if(!flag) {
                continue;
            }
            codeSet.add(bean.getCode());
        }
        //判断医疗机构编码在基础数据机构或者病例机构中是否存在
        List<String> codeList = list.stream().map(MedicalOrgan::getCode).distinct().collect(Collectors.toList());
        List<String> existOrg = organMapper.selectList(
                new QueryWrapper<MedicalOrgan>().eq("STATE", MedicalAuditLogConstants.STATE_YX).in("CODE",codeList)).
                stream().map(item->item.getCode()).collect(Collectors.toList());
        List<String> existDwbOrg = dwbOrgMapper.selectList(
                new QueryWrapper<DwbMasterInfoOrg>().eq("DATA_SOURCE", user.getDataSource()).in("CODE",codeList)).
                stream().map(item->item.getCode()).collect(Collectors.toList());
        List<String> notExistCode = codeList.stream().filter(item->!existOrg.contains(item)&&!existDwbOrg.contains(item)).collect(Collectors.toList());
        if(notExistCode.size()>0){
            message += "导入的数据中“医疗机构编码”在系统医疗机构字典或者病例机构不存在，如：[" +
                    StringUtils.join(notExistCode, ",") + "]";
        }


        if(StringUtils.isNotBlank(message)){
            message +="请核对数据后进行导入。";
            return Result.error(message);
        }else{
            return Result.ok(message,list);
        }
    }
}
