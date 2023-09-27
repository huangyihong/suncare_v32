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
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.service.ITaskBatchStepItemService;
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
 * @Description: 批次步骤子页子项关联
 * @Author: jeecg-boot
 * @Date:   2020-02-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="批次步骤子页子项关联")
@RestController
@RequestMapping("/task/taskBatchStepItem")
public class TaskBatchStepItemController extends JeecgController<TaskBatchStepItem, ITaskBatchStepItemService> {
	@Autowired
	private ITaskBatchStepItemService taskBatchStepItemService;

	/**
	 * 分页列表查询
	 *
	 * @param taskBatchStepItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "批次步骤子页子项关联-分页列表查询")
	@ApiOperation(value="批次步骤子页子项关联-分页列表查询", notes="批次步骤子页子项关联-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskBatchStepItem taskBatchStepItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TaskBatchStepItem> queryWrapper = QueryGenerator.initQueryWrapper(taskBatchStepItem, req.getParameterMap());
		Page<TaskBatchStepItem> page = new Page<TaskBatchStepItem>(pageNo, pageSize);
		IPage<TaskBatchStepItem> pageList = taskBatchStepItemService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 通过批次及步骤获取子项状态
	  * @param batchId
	  * @param step
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "批次步骤子页子项关联-通过批次及步骤获取子项状态")
	 @ApiOperation(value="批次步骤子页子项关联-通过批次及步骤获取子项状态", notes="批次步骤子页子项关联-通过批次及步骤获取子项状态")
	 @GetMapping(value = "/queryByBatchStep")
	 public Result<?> queryByBatchStep(@RequestParam(name="batchId") String batchId, @RequestParam(name="step") Integer step,
									HttpServletRequest req) {
		 List<TaskBatchStepItem> list = taskBatchStepItemService.queryByBatchStep(batchId, step);
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param taskBatchStepItem
	 * @return
	 */
	@AutoLog(value = "批次步骤子页子项关联-添加")
	@ApiOperation(value="批次步骤子页子项关联-添加", notes="批次步骤子页子项关联-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskBatchStepItem taskBatchStepItem) {
		taskBatchStepItemService.save(taskBatchStepItem);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param taskBatchStepItem
	 * @return
	 */
	@AutoLog(value = "批次步骤子页子项关联-编辑")
	@ApiOperation(value="批次步骤子页子项关联-编辑", notes="批次步骤子页子项关联-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskBatchStepItem taskBatchStepItem) {
		taskBatchStepItemService.updateById(taskBatchStepItem);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "批次步骤子页子项关联-通过id删除")
	@ApiOperation(value="批次步骤子页子项关联-通过id删除", notes="批次步骤子页子项关联-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskBatchStepItemService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "批次步骤子页子项关联-批量删除")
	@ApiOperation(value="批次步骤子页子项关联-批量删除", notes="批次步骤子页子项关联-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskBatchStepItemService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "批次步骤子页子项关联-通过id查询")
	@ApiOperation(value="批次步骤子页子项关联-通过id查询", notes="批次步骤子页子项关联-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskBatchStepItem taskBatchStepItem = taskBatchStepItemService.getById(id);
		return Result.ok(taskBatchStepItem);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskBatchStepItem
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskBatchStepItem taskBatchStepItem) {
      return super.exportXls(request, taskBatchStepItem, TaskBatchStepItem.class, "批次步骤子页子项关联");
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
      return super.importExcel(request, response, TaskBatchStepItem.class);
  }

}
