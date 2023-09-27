package com.ai.modules.task.service;

import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.vo.TaskProjectVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description: 任务项目
 * @Author: jeecg-boot
 * @Date:   2020-01-03
 * @Version: V1.0
 */
public interface ITaskProjectService extends IService<TaskProject> {
    IPage<TaskProjectVO> pageVO(Page<TaskProject> page, Wrapper<TaskProject> wrapper);

    void saveProject(TaskProjectVO taskProject) throws Exception;

    void updateProjectById(TaskProjectVO taskProject) throws Exception;

    Result<?> importOrgExcel(MultipartFile file, LoginUser user) throws Exception;
}
