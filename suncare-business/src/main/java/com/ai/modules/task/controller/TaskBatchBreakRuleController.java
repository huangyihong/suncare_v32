package com.ai.modules.task.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.MedicalConstant;
import com.ai.modules.task.vo.TaskBatchBreakRuleVO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.service.ITaskBatchBreakRuleService;
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
 * @Description: 批次规则关联
 * @Author: jeecg-boot
 * @Date:   2020-01-02
 * @Version: V1.0
 */
@Slf4j
@Api(tags="批次规则关联")
@RestController
@RequestMapping("/task/taskBatchBreakRule")
public class TaskBatchBreakRuleController extends JeecgController<TaskBatchBreakRule, ITaskBatchBreakRuleService> {

	@Autowired
	private ITaskBatchBreakRuleService taskBatchBreakRuleService;

	/**
	 * 分页列表查询
	 *
	 * @param taskBatchBreakRule
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "批次规则关联-分页列表查询")
	@ApiOperation(value="批次规则关联-分页列表查询", notes="批次规则关联-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskBatchBreakRule taskBatchBreakRule,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TaskBatchBreakRule> queryWrapper = QueryGenerator.initQueryWrapper(taskBatchBreakRule, req.getParameterMap());
		Page<TaskBatchBreakRule> page = new Page<TaskBatchBreakRule>(pageNo, pageSize);
		IPage<TaskBatchBreakRule> pageList = taskBatchBreakRuleService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 批次不同类型规则关联查询
	  * @param type
	  * @param batchId
	  * @param onStep
	  * @param req
	  * @return
	  */
	@AutoLog(value = "批次规则关联-批次不同类型规则关联查询")
	@ApiOperation(value="批次规则关联-批次不同类型规则关联查询", notes="批次规则关联-批次不同类型规则关联查询")
	@GetMapping(value = "/listByType")
	public Result<?> listByType(@RequestParam(name="type") String type,
								@RequestParam(name="batchId") String batchId,
								@RequestParam(name="notHis", defaultValue="true") Boolean notHis,
								HttpServletRequest req) {
		QueryWrapper<TaskBatchBreakRule> queryWrapper = new QueryWrapper<TaskBatchBreakRule>()
				.eq("t.BATCH_ID",batchId);
		List<TaskBatchBreakRuleVO> list = taskBatchBreakRuleService.listByType(queryWrapper, type, notHis);
		return Result.ok(list);
	}

	 @AutoLog(value = "批次规则关联-规则是否存在于现有配置里")
	 @ApiOperation(value="批次规则关联-规则是否存在于现有配置里", notes="批次规则关联-规则是否存在于现有配置里")
	 @PostMapping(value = "/listInFormalByType")
	 public Result<?> listInFormalByType(@RequestParam(name="type") String type,
								 @RequestParam(name="ruleIds") String ruleIds,
								 HttpServletRequest req) {
		 List<TaskBatchBreakRuleVO> list = taskBatchBreakRuleService.listInFormalByType(ruleIds.split(","),type);
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param taskBatchBreakRule
	 * @return
	 */
	@AutoLog(value = "批次规则关联-添加")
	@ApiOperation(value="批次规则关联-添加", notes="批次规则关联-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskBatchBreakRule taskBatchBreakRule) {
		taskBatchBreakRuleService.save(taskBatchBreakRule);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param taskBatchBreakRule
	 * @return
	 */
	@AutoLog(value = "批次规则关联-编辑")
	@ApiOperation(value="批次规则关联-编辑", notes="批次规则关联-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskBatchBreakRule taskBatchBreakRule) {
		taskBatchBreakRuleService.updateById(taskBatchBreakRule);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "批次规则关联-通过id删除")
	@ApiOperation(value="批次规则关联-通过id删除", notes="批次规则关联-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskBatchBreakRuleService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "批次规则关联-批量删除")
	@ApiOperation(value="批次规则关联-批量删除", notes="批次规则关联-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskBatchBreakRuleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "批次规则关联-通过id查询")
	@ApiOperation(value="批次规则关联-通过id查询", notes="批次规则关联-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskBatchBreakRule taskBatchBreakRule = taskBatchBreakRuleService.getById(id);
		return Result.ok(taskBatchBreakRule);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskBatchBreakRule
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskBatchBreakRule taskBatchBreakRule) {
      return super.exportXls(request, taskBatchBreakRule, TaskBatchBreakRule.class, "批次规则关联");
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
      return super.importExcel(request, response, TaskBatchBreakRule.class);
  }

}
