package com.ai.modules.dcmapping.service;

import com.ai.modules.dcmapping.entity.DcMappingResultTable;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 采集映射表名称映射结果表
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
public interface IDcMappingResultTableService extends IService<DcMappingResultTable> {

    void relationTable(String taskId, String destTableName, String sourceTableName);
}
