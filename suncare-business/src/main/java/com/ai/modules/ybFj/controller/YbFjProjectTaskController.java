package com.ai.modules.ybFj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.YbFjProjectTaskDto;
import com.ai.modules.ybFj.vo.TaskClueVo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjProjectTask;
import com.ai.modules.ybFj.service.IYbFjProjectTaskService;
import java.util.Date;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 飞检项目审核任务
 * @Author: jeecg-boot
 * @Date:   2023-03-10
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目审核任务")
@RestController
@RequestMapping("/fj/project/task")
public class YbFjProjectTaskController extends JeecgController<YbFjProjectTask, IYbFjProjectTaskService> {
	@Autowired
	private IYbFjProjectTaskService ybFjProjectTaskService;

	 @AutoLog(value = "飞检项目审核任务-查找选中线索总览的附件及汇总")
	 @ApiOperation(value="飞检项目审核任务-查找选中线索总览的附件及汇总", notes="飞检项目审核任务-查找选中线索总览的附件及汇总")
	 @GetMapping(value = "/createByClue")
	 public Result<TaskClueVo> createByClue(String clueIds, HttpServletRequest req) {
		 TaskClueVo vo = ybFjProjectTaskService.queryTaskClueVo(clueIds);
		 Result<TaskClueVo> result = new Result<TaskClueVo>();
		 result.setResult(vo);
		 return result;
	 }

	/**
	 * 分页列表查询
	 *
	 * @param ybFjProjectTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目审核任务-分页列表查询")
	@ApiOperation(value="飞检项目审核任务-分页列表查询", notes="飞检项目审核任务-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YbFjProjectTask>> queryPageList(YbFjProjectTask ybFjProjectTask,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbFjProjectTask> queryWrapper = QueryGenerator.initQueryWrapper(ybFjProjectTask, req.getParameterMap());
		Page<YbFjProjectTask> page = new Page<YbFjProjectTask>(pageNo, pageSize);
		IPage<YbFjProjectTask> pageList = ybFjProjectTaskService.page(page, queryWrapper);
		Result<IPage<YbFjProjectTask>> result = new Result<>();
		result.setResult(pageList);
		return result;
	}

	 @AutoLog(value = "飞检项目审核任务-已审核的任务")
	 @ApiOperation(value="飞检项目审核任务-已审核的任务", notes="飞检项目审核任务-已审核的任务")
	 @GetMapping(value = "/alreadyAuditList")
	 public Result<IPage<YbFjProjectTask>> alreadyAuditList(@RequestParam(name="projectOrgId",required=true) String projectOrgId ,
									@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									HttpServletRequest req) {
		 Page<YbFjProjectTask> page = new Page<YbFjProjectTask>(pageNo, pageSize);
		 IPage<YbFjProjectTask> pageList = ybFjProjectTaskService.queryProjectTaskAlreadyAudit(page, projectOrgId, DcFjConstants.CLUE_STEP_SUBMIT);
		 Result<IPage<YbFjProjectTask>> result = new Result<>();
		 result.setResult(pageList);
		 return result;
	 }

	 @AutoLog(value = "飞检项目审核任务-我创建的任务")
	 @ApiOperation(value="飞检项目审核任务-我创建的任务", notes="飞检项目审核任务-我创建的任务")
	 @GetMapping(value = "/mineCreateList")
	 public Result<IPage<YbFjProjectTask>> mineCreateList(@RequestParam(name="projectOrgId",required=true) String projectOrgId ,
														  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														  HttpServletRequest req) {
		 Page<YbFjProjectTask> page = new Page<YbFjProjectTask>(pageNo, pageSize);
		 IPage<YbFjProjectTask> pageList = ybFjProjectTaskService.queryProjectTaskMineCreate(page, projectOrgId, DcFjConstants.CLUE_STEP_SUBMIT);
		 Result<IPage<YbFjProjectTask>> result = new Result<>();
		 result.setResult(pageList);
		 return result;
	 }

	 @AutoLog(value = "飞检项目审核任务-待我审核的任务")
	 @ApiOperation(value="飞检项目审核任务-待我审核的任务", notes="飞检项目审核任务-待我审核的任务")
	 @GetMapping(value = "/waitAuditList")
	 public Result<IPage<YbFjProjectTask>> waitAuditList(@RequestParam(name="projectOrgId",required=true) String projectOrgId ,
														 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														 HttpServletRequest req) {
		 Page<YbFjProjectTask> page = new Page<YbFjProjectTask>(pageNo, pageSize);
		 IPage<YbFjProjectTask> pageList = ybFjProjectTaskService.queryProjectTaskWaitAudit(page, projectOrgId, DcFjConstants.CLUE_STEP_SUBMIT);
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
	@RequiresPermissions("fj:clue:submit:task:save")
	@AutoLog(value = "飞检项目审核任务-添加")
	@ApiOperation(value="飞检项目审核任务-添加", notes="飞检项目审核任务-添加")
	@PostMapping(value = "/add")
	public Result<?> add(YbFjProjectTaskDto dto, MultipartFile[] files) throws Exception {
		ybFjProjectTaskService.saveProjectTask(dto, DcFjConstants.CLUE_STEP_SUBMIT, files);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param dto
	 * @param files
	 * @return
	 */
	@RequiresPermissions("fj:clue:submit:task:save")
	@AutoLog(value = "飞检项目审核任务-编辑")
	@ApiOperation(value="飞检项目审核任务-编辑", notes="飞检项目审核任务-编辑")
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
	@AutoLog(value = "飞检项目审核任务-通过id删除")
	@ApiOperation(value="飞检项目审核任务-通过id删除", notes="飞检项目审核任务-通过id删除")
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
	@AutoLog(value = "飞检项目审核任务-批量删除")
	@ApiOperation(value="飞检项目审核任务-批量删除", notes="飞检项目审核任务-批量删除")
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
	@AutoLog(value = "飞检项目审核任务-通过id查询")
	@ApiOperation(value="飞检项目审核任务-通过id查询", notes="飞检项目审核任务-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbFjProjectTask ybFjProjectTask = ybFjProjectTaskService.getById(id);
		return Result.ok(ybFjProjectTask);
	}

	 @RequiresPermissions("fj:clue:submit:task:audit")
     @AutoLog(value = "飞检项目审核任务-任务审核")
     @ApiOperation(value = "飞检项目审核任务-任务审核", notes = "飞检项目审核任务-任务审核")
     @PostMapping(value = "/audit")
     @ApiImplicitParams({
             @ApiImplicitParam(name = "auditState", value = "审核状态", paramType = "query"),
             @ApiImplicitParam(name = "auditOpinion", value = "审核意见", paramType = "query")
     })
     public Result<?> audit(@RequestParam(name = "taskId", required = true) String taskId,
                            @RequestParam(name = "auditState", required = true) String auditState,
                            @RequestParam(name = "auditOpinion") String auditOpinion,
                            MultipartFile[] files) throws Exception {
         ybFjProjectTaskService.auditProjectTask(taskId, auditState, auditOpinion, files);
         return Result.ok("任务审核成功！");
     }
}
