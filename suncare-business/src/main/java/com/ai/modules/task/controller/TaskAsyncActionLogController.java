package com.ai.modules.task.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.task.entity.TaskAsyncActionLog;
import com.ai.modules.task.service.ITaskAsyncActionLogService;

import java.util.stream.Collectors;

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
 * @Description: 异步操作日志
 * @Author: jeecg-boot
 * @Date:   2020-12-07
 * @Version: V1.0
 */
@Slf4j
@Api(tags="异步操作日志")
@RestController
@RequestMapping("/task/taskAsyncActionLog")
public class TaskAsyncActionLogController extends JeecgController<TaskAsyncActionLog, ITaskAsyncActionLogService> {
	@Autowired
	private ITaskAsyncActionLogService taskAsyncActionLogService;

	/**
	 * 分页列表查询
	 *
	 * @param taskAsyncActionLog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "异步操作日志-分页列表查询")
	@ApiOperation(value="异步操作日志-分页列表查询", notes="异步操作日志-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskAsyncActionLog taskAsyncActionLog,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<TaskAsyncActionLog> queryWrapper = QueryGenerator.initQueryWrapper(taskAsyncActionLog, req.getParameterMap());
		Page<TaskAsyncActionLog> page = new Page<>(pageNo, pageSize);
		IPage<TaskAsyncActionLog> pageList = taskAsyncActionLogService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param taskAsyncActionLog
	 * @return
	 */
	@AutoLog(value = "异步操作日志-添加")
	@ApiOperation(value="异步操作日志-添加", notes="异步操作日志-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskAsyncActionLog taskAsyncActionLog) {
		taskAsyncActionLogService.save(taskAsyncActionLog);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param taskAsyncActionLog
	 * @return
	 */
	@AutoLog(value = "异步操作日志-编辑")
	@ApiOperation(value="异步操作日志-编辑", notes="异步操作日志-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskAsyncActionLog taskAsyncActionLog) {
		taskAsyncActionLogService.updateById(taskAsyncActionLog);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "异步操作日志-通过id删除")
	@ApiOperation(value="异步操作日志-通过id删除", notes="异步操作日志-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskAsyncActionLogService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "异步操作日志-批量删除")
	@ApiOperation(value="异步操作日志-批量删除", notes="异步操作日志-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskAsyncActionLogService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "异步操作日志-通过id查询")
	@ApiOperation(value="异步操作日志-通过id查询", notes="异步操作日志-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskAsyncActionLog taskAsyncActionLog = taskAsyncActionLogService.getById(id);
		return Result.ok(taskAsyncActionLog);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskAsyncActionLog
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskAsyncActionLog taskAsyncActionLog) {
      return super.exportXls(request, taskAsyncActionLog, TaskAsyncActionLog.class, "异步操作日志");
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
      return super.importExcel(request, response, TaskAsyncActionLog.class);
  }

}
