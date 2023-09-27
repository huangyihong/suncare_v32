package com.ai.modules.task.mapper;

import java.util.List;

import com.ai.modules.task.entity.TaskProject;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.task.entity.TaskProjectClient;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.common.system.vo.LoginUser;

/**
 * @Description: 项目客户关联
 * @Author: jeecg-boot
 * @Date:   2020-02-18
 * @Version: V1.0
 */
public interface TaskProjectClientMapper extends BaseMapper<TaskProjectClient> {
    List<LoginUser> selectUsersByProject(@Param("projectId") String projectId);
    List<TaskProject> selectProjectByUser(@Param("userId") String userId);
}
