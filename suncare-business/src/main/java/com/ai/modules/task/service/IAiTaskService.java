package com.ai.modules.task.service;

import com.ai.modules.task.entity.AiTask;
import com.ai.modules.task.vo.AiModelResultVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI任务表
 * @Author: jeecg-boot
 * @Date:   2022-02-28
 * @Version: V1.0
 */
public interface IAiTaskService extends IService<AiTask> {
    boolean exportExcelSolr(List<AiModelResultVO> listVO, List<Map<String, Object>> listMap, OutputStream os, String suffix) throws Exception;
}
