package com.ai.modules.task.service;

import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectClient;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.system.vo.LoginUser;

import java.util.List;

/**
 * @Description: 项目客户关联
 * @Author: jeecg-boot
 * @Date:   2020-02-18
 * @Version: V1.0
 */
public interface ITaskProjectClientService extends IService<TaskProjectClient> {

    List<LoginUser> selectUsersByProject(String projectId);
    List<TaskProject> selectProjectByUser(String userId);

}
