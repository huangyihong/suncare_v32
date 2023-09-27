package com.ai.modules.task.service.impl;

import com.ai.modules.task.entity.TaskActionFieldCol;
import com.ai.modules.task.mapper.TaskActionFieldColMapper;
import com.ai.modules.task.service.ITaskActionFieldColService;
import com.ai.modules.task.vo.TaskActionFieldColVO;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 不合规行为表字段信息配置
 * @Author: jeecg-boot
 * @Date:   2021-02-22
 * @Version: V1.0
 */
@Service
public class TaskActionFieldColServiceImpl extends ServiceImpl<TaskActionFieldColMapper, TaskActionFieldCol> implements ITaskActionFieldColService {

    @Override
    public List<TaskActionFieldColVO> queryDefCol() {
        return this.baseMapper.queryDefCol();
    }

  /*  @Override
    public List<TaskActionFieldCol> queryDefColSimple(String platform) {
        return this.baseMapper.queryDefColSimple(platform);
    }*/

    @Override
    public List<TaskActionFieldColVO> queryColByAction(String platform, String actionId,  String actionName) {
        return this.baseMapper.queryColByAction(platform, actionId, actionName);
    }

    @Override
    public List<TaskActionFieldColVO> getDefSerCol() {
        return this.baseMapper.queryDefSerCol();
    }

    @Override
    public List<TaskActionFieldColVO> querySerColByAction(String platform, String actionId, String actionName) {
        return this.baseMapper.querySerColByAction(platform, actionId, actionName);
    }

    @Override
    public List<TaskActionFieldColVO> querySerByConfigId(String configId) {
        return this.baseMapper.querySerByConfigId(configId);
    }

    @Override
    public List<TaskActionFieldColVO> queryDelByConfigId(String configId) {
        return this.baseMapper.queryDelColByConfigId(configId);
    }
}
