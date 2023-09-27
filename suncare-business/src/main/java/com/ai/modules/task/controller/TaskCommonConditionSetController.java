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
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.service.ITaskCommonConditionSetService;
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
 * @Description: 通用条件设置
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
@Slf4j
@Api(tags="通用条件设置")
@RestController
@RequestMapping("/task/taskCommonConditionSet")
public class TaskCommonConditionSetController extends JeecgController<TaskCommonConditionSet, ITaskCommonConditionSetService> {
	@Autowired
	private ITaskCommonConditionSetService taskCommonConditionSetService;

	 @AutoLog(value = "通用规则条件集-通过ruleId查询")
	 @ApiOperation(value="通用规则条件集-通过ruleId查询", notes="通用规则条件集-通过ruleId查询")
	 @GetMapping(value = "/queryByRuleId")
	 public Result<?> queryByRuleId(@RequestParam(name="ruleId",required=true) String ruleId) {
		 List<TaskCommonConditionSet> list = taskCommonConditionSetService.list(
				 new QueryWrapper<TaskCommonConditionSet>().eq("RULE_ID", ruleId).orderByAsc("TYPE", "GROUP_NO", "ORDER_NO")
		 );
		 return Result.ok(list);
	 }

	/**
	 * 分页列表查询
	 *
	 * @param taskCommonConditionSet
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "通用条件设置-分页列表查询")
	@ApiOperation(value="通用条件设置-分页列表查询", notes="通用条件设置-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskCommonConditionSet taskCommonConditionSet,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TaskCommonConditionSet> queryWrapper = QueryGenerator.initQueryWrapper(taskCommonConditionSet, req.getParameterMap());
		Page<TaskCommonConditionSet> page = new Page<TaskCommonConditionSet>(pageNo, pageSize);
		IPage<TaskCommonConditionSet> pageList = taskCommonConditionSetService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param taskCommonConditionSet
	 * @return
	 */
	@AutoLog(value = "通用条件设置-添加")
	@ApiOperation(value="通用条件设置-添加", notes="通用条件设置-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskCommonConditionSet taskCommonConditionSet) {
		taskCommonConditionSetService.save(taskCommonConditionSet);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param taskCommonConditionSet
	 * @return
	 */
	@AutoLog(value = "通用条件设置-编辑")
	@ApiOperation(value="通用条件设置-编辑", notes="通用条件设置-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskCommonConditionSet taskCommonConditionSet) {
		taskCommonConditionSetService.updateById(taskCommonConditionSet);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "通用条件设置-通过id删除")
	@ApiOperation(value="通用条件设置-通过id删除", notes="通用条件设置-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskCommonConditionSetService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "通用条件设置-批量删除")
	@ApiOperation(value="通用条件设置-批量删除", notes="通用条件设置-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskCommonConditionSetService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "通用条件设置-通过id查询")
	@ApiOperation(value="通用条件设置-通过id查询", notes="通用条件设置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskCommonConditionSet taskCommonConditionSet = taskCommonConditionSetService.getById(id);
		return Result.ok(taskCommonConditionSet);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskCommonConditionSet
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskCommonConditionSet taskCommonConditionSet) {
      return super.exportXls(request, taskCommonConditionSet, TaskCommonConditionSet.class, "通用条件设置");
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
      return super.importExcel(request, response, TaskCommonConditionSet.class);
  }

}
