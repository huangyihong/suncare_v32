package com.ai.modules.task.mapper;

import java.util.List;

import com.ai.modules.task.vo.TaskActionFieldColVO;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 不合规行为表字段信息配置
 * @Author: jeecg-boot
 * @Date:   2021-02-22
 * @Version: V1.0
 */
public interface TaskActionFieldColMapper extends BaseMapper<TaskActionFieldCol> {
    List<TaskActionFieldColVO> queryDefCol();

    List<TaskActionFieldColVO> queryColByAction(@Param("platform") String platform, @Param("actionId") String actionId, @Param("actionName") String actionName);

    List<TaskActionFieldColVO> queryDefSerCol();

    List<TaskActionFieldColVO> querySerColByAction(@Param("platform") String platform, @Param("actionId") String actionId, @Param("actionName") String actionName);

    List<TaskActionFieldColVO> querySerByConfigId(@Param("configId") String configId);

    List<TaskActionFieldColVO> queryDelColByConfigId(@Param("configId") String configId);
}
