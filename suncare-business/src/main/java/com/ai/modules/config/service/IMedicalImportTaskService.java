package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalImportTask;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @Description: 导入操作任务日志
 * @Author: jeecg-boot
 * @Date:   2022-04-20
 * @Version: V1.0
 */
public interface IMedicalImportTaskService extends IService<MedicalImportTask> {
    public MedicalImportTask saveImportTask(String tableName, String actionType);

    public Result saveImportTask(String tableName, String actionType, MultipartFile file, LoginUser user,
                          BiFunction<MultipartFile,LoginUser, Result> function);

    public Result saveBatchTask(String tableName, String actionType, MedicalAuditLog bean, List list,
                                 BiFunction<MedicalAuditLog,List, Result> function);

    public Result saveBatchTask(String tableName, String actionType, Object bean, QueryWrapper queryWrapper,
                                BiFunction<Object,QueryWrapper, Integer> function);
}
