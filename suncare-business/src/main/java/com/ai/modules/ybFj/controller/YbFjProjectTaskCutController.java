package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.YbFjProjectTaskDto;
import com.ai.modules.ybFj.entity.YbFjProjectTask;
import com.ai.modules.ybFj.entity.YbFjUserOrg;
import com.ai.modules.ybFj.service.IYbFjProjectTaskService;
import com.ai.modules.ybFj.service.IYbFjUserOrgService;
import com.ai.modules.ybFj.vo.TaskClueVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @Description: 飞检项目线上核减任务
* @Author: jeecg-boot
* @Date:   2023-03-10
* @Version: V1.0
*/
@Slf4j
@Api(tags="飞检项目线上核减任务")
@RestController
@RequestMapping("/fj/project/task/cut")
public class YbFjProjectTaskCutController extends JeecgController<YbFjProjectTask, IYbFjProjectTaskService> {
   @Autowired
   private IYbFjProjectTaskService ybFjProjectTaskService;
    @Autowired
    private IYbFjUserOrgService userOrgService;

    @AutoLog(value = "飞检项目线上核减任务-查找选中线索总览的附件及汇总")
    @ApiOperation(value="飞检项目线上核减任务-查找选中线索总览的附件及汇总", notes="飞检项目线上核减任务-查找选中线索总览的附件及汇总")
    @GetMapping(value = "/createByClue")
    public Result<TaskClueVo> createByClue(String clueIds, HttpServletRequest req) {
        TaskClueVo vo = ybFjProjectTaskService.queryTaskClueVoInCut(clueIds);
        Result<TaskClueVo> result = new Result<TaskClueVo>();
        result.setResult(vo);
        return result;
    }

    @AutoLog(value = "飞检项目线上核减任务-已审核的任务")
    @ApiOperation(value="飞检项目线上核减任务-已审核的任务", notes="飞检项目线上核减任务-已审核的任务")
    @GetMapping(value = "/alreadyAuditList")
    public Result<IPage<YbFjProjectTask>> alreadyAuditList(@RequestParam(name="projectOrgId",required=true) String projectOrgId ,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        Page<YbFjProjectTask> page = new Page<YbFjProjectTask>(pageNo, pageSize);
        IPage<YbFjProjectTask> pageList = ybFjProjectTaskService.queryProjectTaskAlreadyAudit(page, projectOrgId, DcFjConstants.CLUE_STEP_CUT);
        Result<IPage<YbFjProjectTask>> result = new Result<>();
        result.setResult(pageList);
        return result;
    }

    @AutoLog(value = "飞检项目线上核减任务-我创建的任务")
    @ApiOperation(value="飞检项目线上核减任务-我创建的任务", notes="飞检项目线上核减任务-我创建的任务")
    @GetMapping(value = "/mineCreateList")
    public Result<IPage<YbFjProjectTask>> mineCreateList(@RequestParam(name="projectOrgId",required=true) String projectOrgId ,
                                                         @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                         @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                         HttpServletRequest req) {
        Page<YbFjProjectTask> page = new Page<YbFjProjectTask>(pageNo, pageSize);
        IPage<YbFjProjectTask> pageList = ybFjProjectTaskService.queryProjectTaskMineCreate(page, projectOrgId, DcFjConstants.CLUE_STEP_CUT);
        Result<IPage<YbFjProjectTask>> result = new Result<>();
        result.setResult(pageList);
        return result;
    }

    @AutoLog(value = "飞检项目线上核减任务-待审核的任务")
    @ApiOperation(value="飞检项目线上核减任务-待审核的任务", notes="飞检项目线上核减任务-待审核的任务")
    @GetMapping(value = "/waitAuditList")
    public Result<IPage<YbFjProjectTask>> waitAuditList(@RequestParam(name="projectOrgId",required=true) String projectOrgId ,
                                                        @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                        @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                        HttpServletRequest req) {
        Page<YbFjProjectTask> page = new Page<YbFjProjectTask>(pageNo, pageSize);
        IPage<YbFjProjectTask> pageList = ybFjProjectTaskService.queryProjectTaskWaitAudit(page, projectOrgId, DcFjConstants.CLUE_STEP_CUT);
        Result<IPage<YbFjProjectTask>> result = new Result<>();
        result.setResult(pageList);
        return result;
    }

   /**
    * 添加
    *
    * @param dto
    * @param files
    * @return
    */
   @AutoLog(value = "飞检项目线上核减任务-添加")
   @ApiOperation(value="飞检项目线上核减任务-添加", notes="飞检项目线上核减任务-添加")
   @PostMapping(value = "/add")
   public Result<?> add(YbFjProjectTaskDto dto, MultipartFile[] files) throws Exception {
       ybFjProjectTaskService.saveProjectTask(dto, DcFjConstants.CLUE_STEP_CUT, files);
       return Result.ok("添加成功！");
   }

   /**
    * 编辑
    *
    * @param dto
    * @param files
    * @return
    */
   @AutoLog(value = "飞检项目线上核减任务-编辑")
   @ApiOperation(value="飞检项目线上核减任务-编辑", notes="飞检项目线上核减任务-编辑")
   @PostMapping(value = "/edit")
   public Result<?> edit(YbFjProjectTaskDto dto, MultipartFile[] files, String fileIds) throws Exception {
       ybFjProjectTaskService.updateProjectTask(dto, files, fileIds);
       return Result.ok("编辑成功!");
   }

   /**
    * 通过id删除
    *
    * @param taskId
    * @return
    */
   @AutoLog(value = "飞检项目线上核减任务-通过id删除")
   @ApiOperation(value="飞检项目线上核减任务-通过id删除", notes="飞检项目线上核减任务-通过id删除")
   @DeleteMapping(value = "/delete")
   public Result<?> delete(@RequestParam(name="id",required=true) String taskId) {
       ybFjProjectTaskService.removeProjectTask(taskId);
       return Result.ok("删除成功!");
   }

   /**
    * 批量删除
    *
    * @param taskIds
    * @return
    */
   @AutoLog(value = "飞检项目线上核减任务-批量删除")
   @ApiOperation(value="飞检项目线上核减任务-批量删除", notes="飞检项目线上核减任务-批量删除")
   @DeleteMapping(value = "/deleteBatch")
   public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String taskIds) {
       this.ybFjProjectTaskService.removeProjectTasks(taskIds);
       return Result.ok("批量删除成功！");
   }

   /**
    * 通过id查询
    *
    * @param id
    * @return
    */
   @AutoLog(value = "飞检项目线上核减任务-通过id查询")
   @ApiOperation(value="飞检项目线上核减任务-通过id查询", notes="飞检项目线上核减任务-通过id查询")
   @GetMapping(value = "/queryById")
   public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
       YbFjProjectTask ybFjProjectTask = ybFjProjectTaskService.getById(id);
       return Result.ok(ybFjProjectTask);
   }

    @RequiresPermissions("fj:clue:cut:task:audit")
    @AutoLog(value = "飞检项目线上核减任务-任务审核")
    @ApiOperation(value = "飞检项目线上核减任务-任务审核", notes = "飞检项目线上核减任务-任务审核")
    @PostMapping(value = "/audit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "auditState", value = "审核状态", paramType = "query"),
            @ApiImplicitParam(name = "auditOpinion", value = "审核意见", paramType = "query")
    })
    public Result<?> audit(@RequestParam(name = "taskId", required = true) String taskId,
                           @RequestParam(name = "auditState", required = true) String auditState,
                           @RequestParam(name = "auditOpinion") String auditOpinion,
                           MultipartFile[] files) throws Exception {
        ybFjProjectTaskService.auditCutTask(taskId, auditState, auditOpinion, files);
        return Result.ok("任务审核成功！");
    }

    @AutoLog(value = "飞检项目线上核减任务（医院端）-核减进度")
    @ApiOperation(value="飞检项目线上核减任务（医院端）-核减进度", notes="飞检项目线上核减任务（医院端）-核减进度")
    @GetMapping(value = "/mineCreateListByOrg")
    public Result<IPage<YbFjProjectTask>> mineCreateListByOrg(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
                                                         @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                         @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                         HttpServletRequest req) throws Exception {
        Page<YbFjProjectTask> page = new Page<YbFjProjectTask>(pageNo, pageSize);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String orgId = this.getUserOrgId();
        IPage<YbFjProjectTask> pageList = ybFjProjectTaskService.queryCutTaskMineCreate(page, orgId, projectOrgId);
        Result<IPage<YbFjProjectTask>> result = new Result<>();
        result.setResult(pageList);
        return result;
    }

    private String getUserOrgId() throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        YbFjUserOrg userOrg = userOrgService.getById(user.getId());
        if(userOrg==null) {
            throw new Exception("非医院端用户，没权限操作");
        }
        return userOrg.getOrgId();
    }
}
