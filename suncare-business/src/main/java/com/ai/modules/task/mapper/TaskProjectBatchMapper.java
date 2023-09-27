package com.ai.modules.task.mapper;

import java.util.List;
import java.util.Map;

import com.ai.modules.task.dto.TaskBatchExecInfo;
import com.ai.modules.task.vo.TaskBatchStepItemVO;
import com.ai.modules.task.vo.TaskProjectBatchVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 任务项目批次
 * @Author: jeecg-boot
 * @Date:   2020-01-03
 * @Version: V1.0
 */
public interface TaskProjectBatchMapper extends BaseMapper<TaskProjectBatch> {
    IPage<TaskProjectBatchVO> selectPageVO(Page<TaskProjectBatchVO> page, @Param(Constants.WRAPPER) Wrapper<TaskProjectBatch> wrapper);


    List<TaskBatchStepItemVO> selectTopBatchItems(@Param("topNum") Integer topNum,@Param("dataSource")  String dataSource);
    Map<String, Object> queryExecTimeById(@Param("batchId") String batchId);
    Map<String, Object> queryExecNumById(@Param("batchId") String batchId);

    List<TaskProjectBatch> queryBatchByProjectOrDs(@Param("dsArray") String[] dsArray, @Param("pjArray") String[] pjArray);
}
