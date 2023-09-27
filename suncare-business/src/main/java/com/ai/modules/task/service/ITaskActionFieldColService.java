package com.ai.modules.task.service;

import com.ai.modules.task.entity.TaskActionFieldCol;
import com.ai.modules.task.vo.TaskActionFieldColVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 不合规行为表字段信息配置
 * @Author: jeecg-boot
 * @Date:   2021-02-22
 * @Version: V1.0
 */
public interface ITaskActionFieldColService extends IService<TaskActionFieldCol> {

    List<TaskActionFieldColVO> queryDefCol();

//    List<TaskActionFieldCol> queryDefColSimple(String platform);

    List<TaskActionFieldColVO> queryColByAction(String platform, String actionId, String actionName);

    List<TaskActionFieldColVO> getDefSerCol();

    List<TaskActionFieldColVO> querySerColByAction(String platform, String actionId, String actionName);

    List<TaskActionFieldColVO> querySerByConfigId(String configId);

    List<TaskActionFieldColVO> queryDelByConfigId(String configId);
}
