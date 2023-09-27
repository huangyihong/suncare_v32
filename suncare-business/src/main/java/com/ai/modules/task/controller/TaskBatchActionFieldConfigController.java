package com.ai.modules.task.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.task.entity.TaskBatchActionFieldConfig;
import com.ai.modules.task.service.ITaskBatchActionFieldConfigService;
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
 * @Description: 任务批次不合规行为汇总字段
 * @Author: jeecg-boot
 * @Date:   2020-12-08
 * @Version: V1.0
 */
@Slf4j
@Api(tags="任务批次不合规行为汇总字段")
@RestController
@RequestMapping("/task/taskBatchActionFieldConfig")
public class TaskBatchActionFieldConfigController extends JeecgController<TaskBatchActionFieldConfig, ITaskBatchActionFieldConfigService> {
	@Autowired
	private ITaskBatchActionFieldConfigService taskBatchActionFieldConfigService;
	
	/**
	 * 分页列表查询
	 *
	 * @param taskBatchActionFieldConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "任务批次不合规行为汇总字段-分页列表查询")
	@ApiOperation(value="任务批次不合规行为汇总字段-分页列表查询", notes="任务批次不合规行为汇总字段-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskBatchActionFieldConfig taskBatchActionFieldConfig,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TaskBatchActionFieldConfig> queryWrapper = QueryGenerator.initQueryWrapper(taskBatchActionFieldConfig, req.getParameterMap());
		Page<TaskBatchActionFieldConfig> page = new Page<TaskBatchActionFieldConfig>(pageNo, pageSize);
		IPage<TaskBatchActionFieldConfig> pageList = taskBatchActionFieldConfigService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param taskBatchActionFieldConfig
	 * @return
	 */
	@AutoLog(value = "任务批次不合规行为汇总字段-添加")
	@ApiOperation(value="任务批次不合规行为汇总字段-添加", notes="任务批次不合规行为汇总字段-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskBatchActionFieldConfig taskBatchActionFieldConfig) {
		taskBatchActionFieldConfigService.save(taskBatchActionFieldConfig);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param taskBatchActionFieldConfig
	 * @return
	 */
	@AutoLog(value = "任务批次不合规行为汇总字段-编辑")
	@ApiOperation(value="任务批次不合规行为汇总字段-编辑", notes="任务批次不合规行为汇总字段-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskBatchActionFieldConfig taskBatchActionFieldConfig) {
		taskBatchActionFieldConfigService.updateById(taskBatchActionFieldConfig);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务批次不合规行为汇总字段-通过id删除")
	@ApiOperation(value="任务批次不合规行为汇总字段-通过id删除", notes="任务批次不合规行为汇总字段-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskBatchActionFieldConfigService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "任务批次不合规行为汇总字段-批量删除")
	@ApiOperation(value="任务批次不合规行为汇总字段-批量删除", notes="任务批次不合规行为汇总字段-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskBatchActionFieldConfigService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务批次不合规行为汇总字段-通过id查询")
	@ApiOperation(value="任务批次不合规行为汇总字段-通过id查询", notes="任务批次不合规行为汇总字段-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskBatchActionFieldConfig taskBatchActionFieldConfig = taskBatchActionFieldConfigService.getById(id);
		return Result.ok(taskBatchActionFieldConfig);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskBatchActionFieldConfig
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskBatchActionFieldConfig taskBatchActionFieldConfig) {
      return super.exportXls(request, taskBatchActionFieldConfig, TaskBatchActionFieldConfig.class, "任务批次不合规行为汇总字段");
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
      return super.importExcel(request, response, TaskBatchActionFieldConfig.class);
  }

}
