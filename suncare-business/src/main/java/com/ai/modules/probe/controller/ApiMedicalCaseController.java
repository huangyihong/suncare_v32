package com.ai.modules.probe.controller;

import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.engine.service.IEngineTrialService;
import com.ai.modules.probe.entity.MedicalProbeCase;
import com.ai.modules.probe.service.IMedicalProbeCaseService;
import com.ai.modules.probe.service.IMedicalProbeFlowRuleService;
import com.ai.modules.probe.vo.MedicalProbeCaseVO;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;

/**
* @Description: 流程图
* @Author: jeecg-boot
* @Date:   2019-11-21
* @Version: V1.0
*/
@Slf4j
@Api(tags="流程图")
@RestController
@RequestMapping("/apiCase/")
public class ApiMedicalCaseController extends JeecgController<MedicalProbeCase, IMedicalProbeCaseService> {


    @Autowired
    private IEngineTrialService engineTrialService;

    /**
     * 更新探查流程计数信息
     *
     * @param caseId
     * @return
     */
    @AutoLog(value = "流程图-更新探查流程计数信息")
    @ApiOperation(value="流程图-更新探查流程计数信息", notes="流程图-更新探查流程计数信息")
    @PostMapping(value = "/trialProbeFlowCnt")
    public Result<?> trialProbeFlowCnt(@RequestParam("caseId") String caseId) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            ThreadUtils.THREAD_SOLR_REQUEST_POOL.add(new EngineFunctionRunnable(sysUser.getDataSource(), sysUser.getToken(), () -> {
                engineTrialService.trialProbeFlowCnt(caseId);
            }));
        } catch (Exception e){
            return Result.error(e.getMessage());
        }
        return Result.ok("修改成功！");
    }

    /**
     * 更新归纳流程计数信息
     *
     * @param caseId
     * @return
     */
    @AutoLog(value = "流程图-更新归纳流程计数信息")
    @ApiOperation(value="流程图-更新归纳流程计数信息", notes="流程图-更新归纳流程计数信息")
    @PostMapping(value = "/trialFormalFlowCnt")
    public Result<?> trialFormalFlowCnt(@RequestParam("caseId") String caseId) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            ThreadUtils.THREAD_SOLR_REQUEST_POOL.add(new EngineFunctionRunnable(sysUser.getDataSource(), sysUser.getToken(), () -> {
                engineTrialService.trialFormalFlowCnt(caseId);
            }));
        } catch (Exception e){
            return Result.error(e.getMessage());
        }
        return Result.ok("修改成功！");
    }



}
