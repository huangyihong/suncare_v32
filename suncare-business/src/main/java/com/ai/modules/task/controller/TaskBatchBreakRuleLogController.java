package com.ai.modules.task.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.ai.common.MedicalConstant;
import com.ai.modules.task.dto.TaskBatchBreakRuleLogDTO;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskBatchBreakRuleLogService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.service.ITaskProjectService;
import com.ai.modules.task.vo.TaskBatchBreakRuleLogVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

 /**
 * @Description: 批次任务运行日志
 * @Author: jeecg-boot
 * @Date:   2020-10-12
 * @Version: V1.0
 */
@Slf4j
@Api(tags="批次任务运行日志")
@RestController
@RequestMapping("/task/taskBatchBreakRuleLog")
public class TaskBatchBreakRuleLogController extends JeecgController<TaskBatchBreakRuleLog, ITaskBatchBreakRuleLogService> {
	@Autowired
	private ITaskBatchBreakRuleLogService taskBatchBreakRuleLogService;
	@Autowired
    private ITaskProjectBatchService taskProjectBatchService;
    @Autowired
    private ITaskProjectService taskProjectService;
	
	/**
	 * 分页列表查询
	 *
	 * @param taskBatchBreakRuleLog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "批次任务运行日志-分页列表查询")
	@ApiOperation(value="批次任务运行日志-分页列表查询", notes="批次任务运行日志-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskBatchBreakRuleLog taskBatchBreakRuleLog,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TaskBatchBreakRuleLog> queryWrapper = QueryGenerator.initQueryWrapper(taskBatchBreakRuleLog, req.getParameterMap());
		Page<TaskBatchBreakRuleLog> page = new Page<TaskBatchBreakRuleLog>(pageNo, pageSize);
		IPage<TaskBatchBreakRuleLog> pageList = taskBatchBreakRuleLogService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param taskBatchBreakRuleLog
	 * @return
	 */
	@AutoLog(value = "批次任务运行日志-添加")
	@ApiOperation(value="批次任务运行日志-添加", notes="批次任务运行日志-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskBatchBreakRuleLog taskBatchBreakRuleLog) {
		taskBatchBreakRuleLogService.save(taskBatchBreakRuleLog);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param taskBatchBreakRuleLog
	 * @return
	 */
	@AutoLog(value = "批次任务运行日志-编辑")
	@ApiOperation(value="批次任务运行日志-编辑", notes="批次任务运行日志-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskBatchBreakRuleLog taskBatchBreakRuleLog) {
		taskBatchBreakRuleLogService.updateById(taskBatchBreakRuleLog);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "批次任务运行日志-通过id删除")
	@ApiOperation(value="批次任务运行日志-通过id删除", notes="批次任务运行日志-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskBatchBreakRuleLogService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "批次任务运行日志-批量删除")
	@ApiOperation(value="批次任务运行日志-批量删除", notes="批次任务运行日志-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskBatchBreakRuleLogService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "批次任务运行日志-通过id查询")
	@ApiOperation(value="批次任务运行日志-通过id查询", notes="批次任务运行日志-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskBatchBreakRuleLog taskBatchBreakRuleLog = taskBatchBreakRuleLogService.getById(id);
		return Result.ok(taskBatchBreakRuleLog);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskBatchBreakRuleLog
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskBatchBreakRuleLog taskBatchBreakRuleLog) {
      return super.exportXls(request, taskBatchBreakRuleLog, TaskBatchBreakRuleLog.class, "批次任务运行日志");
  }

  /**
   * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      return super.importExcel(request, response, TaskBatchBreakRuleLog.class);
  }
  
  	@AutoLog(value = "查询某批次的任务运行日志")
	@ApiOperation(value="查询某批次的任务运行日志", notes="查询某批次的任务运行日志")
	@GetMapping(value = "/logList")
	public Result<?> logList(TaskBatchBreakRuleLogDTO dto,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
  		
  		if(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE.equals(dto.getItemType())) {
  			//用药合规
  	  		Page<TaskBatchBreakRuleLogVO> page = new Page<TaskBatchBreakRuleLogVO>(pageNo, pageSize);
  	  		IPage<TaskBatchBreakRuleLogVO> pageList = taskBatchBreakRuleLogService.queryDruguseLog(page, dto);
  	  		return Result.ok(pageList);
  		} else if(MedicalConstant.ENGINE_BUSI_TYPE_DRUGREPEAT.equals(dto.getItemType())) {
  			//重复用药
  			Page<TaskBatchBreakRuleLog> page = new Page<TaskBatchBreakRuleLog>(pageNo, pageSize);
  	  		IPage<TaskBatchBreakRuleLog> pageList = taskBatchBreakRuleLogService.queryDrugLog(page, dto);
  	  		return Result.ok(pageList);
  		} else if("charge".equals(dto.getItemType())
  				|| "drug".equals(dto.getItemType())
  				|| "treat".equals(dto.getItemType())) {
  			//老版本收费、药品、诊疗
  			Page<TaskBatchBreakRuleLog> page = new Page<TaskBatchBreakRuleLog>(pageNo, pageSize);
  	  		IPage<TaskBatchBreakRuleLog> pageList = taskBatchBreakRuleLogService.queryDrugLog(page, dto);
  	  		return Result.ok(pageList);
  		} else {
  			//新版本收费、药品、诊疗
  			//QueryWrapper<TaskBatchBreakRuleLogDTO> wrapper = QueryGenerator.initQueryWrapper(dto, req.getParameterMap());
  	  		Page<TaskBatchBreakRuleLogVO> page = new Page<TaskBatchBreakRuleLogVO>(pageNo, pageSize);
  	  		IPage<TaskBatchBreakRuleLogVO> pageList = taskBatchBreakRuleLogService.queryTaskBatchBreakRuleLog(page, dto);
  	  		return Result.ok(pageList);
  		}  		
	}
  	
  	@ApiOperation(value="批次任务运行日志查询条件", notes="批次任务运行日志查询条件")
	@PostMapping(value = "/logInit")
	public Result<?> logCondition(TaskBatchBreakRuleLogDTO dto) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("engineList", taskBatchBreakRuleLogService.queryTaskBatchBreakRuleEngine(dto.getBatchId()));
		TaskProjectBatch batch = taskProjectBatchService.getById(dto.getBatchId());
        result.put("batch", batch);
        if (batch != null) {
        	TaskProject project = taskProjectService.getById(batch.getProjectId());
        	result.put("project", project);
        }
  		return Result.ok(result);
	}
  	
  	@ApiOperation(value="批次任务运行日志查询条件", notes="批次任务运行日志查询条件")
	@PostMapping(value = "/logInit/action")
	public Result<?> batchEngine(TaskBatchBreakRuleLogDTO dto) {
  		List<Map<String, Object>> list = taskBatchBreakRuleLogService.queryTaskBatchBreakRuleLimit(dto);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("ruleLimitList", list);
		list = taskBatchBreakRuleLogService.queryTaskBatchBreakRuleAction(dto);
		result.put("actionList", list);		
  		return Result.ok(result);
	}
}
