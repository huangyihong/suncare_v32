package com.ai.modules.drg.service;

import com.ai.modules.drg.entity.DrgTask;

/**
 * @author : zhangly
 * @date : 2023/4/6 10:29
 */
public interface IDrgHandleService {

    void execute(String taskId) throws Exception;
}
