package com.ai.modules.dcmapping.service;

import com.ai.modules.dcmapping.entity.DcMappingTask;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 采集映射任务信息表
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
public interface IDcMappingTaskService extends IService<DcMappingTask> {

    void deleteByIds(String ids);
}
