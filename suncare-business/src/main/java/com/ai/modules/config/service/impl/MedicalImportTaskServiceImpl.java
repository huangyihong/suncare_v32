package com.ai.modules.config.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.entity.MedicalImportTask;
import com.ai.modules.config.mapper.MedicalImportTaskMapper;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @Description: 导入操作任务日志
 * @Author: jeecg-boot
 * @Date:   2022-04-20
 * @Version: V1.0
 */
@Service
public class MedicalImportTaskServiceImpl extends ServiceImpl<MedicalImportTaskMapper, MedicalImportTask> implements IMedicalImportTaskService {

    @Override
    public MedicalImportTask saveImportTask(String tableName, String actionType) {
        MedicalImportTask importTask = new MedicalImportTask();
        importTask.setId(IdUtils.uuid());
        importTask.setStatus("00");
        importTask.setTableName(tableName);
        importTask.setActionType(actionType);
        importTask.setStartTime(new Date());
        this.save(importTask);
        return importTask;
    }

    @Override
    public Result saveImportTask(String tableName, String actionType, MultipartFile file, LoginUser user,
                                   BiFunction<MultipartFile, LoginUser, Result> function) {
        MedicalImportTask importTask = this.saveImportTask(tableName,actionType);
        try{
            Result result = function.apply(file, user);
            importTask.setMessage(result.getMessage());
            if(result.isSuccess()){
                importTask.setStatus("01");
                if(result.getResult() instanceof Integer){
                    importTask.setRecordCount((Integer) result.getResult());
                }
                return Result.ok(importTask.getMessage());
            }else{
                importTask.setStatus("02");
            }
            return result;
        } catch (Exception e) {
            log.error("", e);
            importTask.setStatus("02");
            importTask.setMessage(e.getMessage());
        } finally {
            if (importTask.getMessage()!=null&&importTask.getMessage().length() > 1000) {
                importTask.setMessage(importTask.getMessage().substring(0, 1000)+"...");
            }
            importTask.setEndTime(new Date());
            this.updateById(importTask);
        }
        return Result.error(importTask.getMessage());
    }

    @Override
    public Result saveBatchTask(String tableName, String actionType, MedicalAuditLog bean, List list,
                                BiFunction<MedicalAuditLog,List, Result> function) {
        //判断相同的表是否已存在相同的操作
        int count = this.baseMapper.selectCount(new QueryWrapper<MedicalImportTask>().
                eq("TABLE_NAME",tableName).eq("ACTION_TYPE",actionType).eq("STATUS","00"));
        if(count>0){
            return Result.error("后台有正在运行的相同操作，请稍后再进行操作");
        }
        MedicalImportTask importTask = this.saveImportTask(tableName,actionType);
        try{
            function.apply(bean, list);
            importTask.setRecordCount(list.size());
            importTask.setStatus("01");
            importTask.setMessage(actionType+"成功，数据量:"+list.size()+"条");
            return Result.ok(actionType+"成功!");
        } catch (Exception e) {
            log.error("", e);
            importTask.setStatus("02");
            importTask.setMessage(e.getMessage());
        } finally {
            if (importTask.getMessage()!=null&&importTask.getMessage().length() > 1000) {
                importTask.setMessage(importTask.getMessage().substring(0, 1000)+"...");
            }
            importTask.setEndTime(new Date());
            this.updateById(importTask);
        }
        return Result.error(importTask.getMessage());
    }

    @Override
    public Result saveBatchTask(String tableName, String actionType, Object bean, QueryWrapper queryWrapper,
                                BiFunction<Object,QueryWrapper, Integer> function) {
        //判断相同的表是否已存在相同的操作
        int count = this.baseMapper.selectCount(new QueryWrapper<MedicalImportTask>().
                eq("TABLE_NAME",tableName).eq("ACTION_TYPE",actionType).eq("STATUS","00"));
        if(count>0){
            return Result.error("后台有正在运行的相同操作，请稍后再进行操作");
        }
        MedicalImportTask importTask = this.saveImportTask(tableName,actionType);
        try{
            int recordCount = function.apply(bean, queryWrapper);
            importTask.setRecordCount(recordCount);
            importTask.setStatus("01");
            importTask.setMessage(actionType+"成功，数据量:"+recordCount+"条");
            return Result.ok(actionType+"成功!");
        } catch (Exception e) {
            log.error("", e);
            importTask.setStatus("02");
            importTask.setMessage(e.getMessage());
        } finally {
            if (importTask.getMessage()!=null&&importTask.getMessage().length() > 1000) {
                importTask.setMessage(importTask.getMessage().substring(0, 1000)+"...");
            }
            importTask.setEndTime(new Date());
            this.updateById(importTask);
        }
        return Result.error(importTask.getMessage());
    }
}
