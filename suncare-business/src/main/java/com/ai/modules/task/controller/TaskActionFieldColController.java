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
import com.ai.common.utils.ReflectHelper;
import com.ai.modules.task.entity.TaskActionFieldRela;
import com.ai.modules.task.service.ITaskActionFieldRelaService;
import com.ai.modules.task.vo.TaskActionFieldColVO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.ai.modules.task.service.ITaskActionFieldColService;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 不合规行为表字段信息配置
 * @Author: jeecg-boot
 * @Date:   2021-02-22
 * @Version: V1.0
 */
@Slf4j
@Api(tags="不合规行为表字段信息配置")
@RestController
@RequestMapping("/task/taskActionFieldCol")
public class TaskActionFieldColController extends JeecgController<TaskActionFieldCol, ITaskActionFieldColService> {
	@Autowired
	private ITaskActionFieldColService taskActionFieldColService;

	@Autowired
	private JdbcTemplate jdbcTemplate;


	@Autowired
	private ITaskActionFieldRelaService taskActionFieldRelaService;



	 @AutoLog(value = "不合规行为表字段信息配置-获取默认字段")
	 @ApiOperation(value = "不合规行为表字段信息配置-获取默认字段", notes = "不合规行为表字段信息配置-获取默认字段")
	 @GetMapping(value = "/getDefSelectCol")
	 public Result<?> getDefSelectCol(String platform ,HttpServletRequest req) {
		 List<TaskActionFieldColVO> list = taskActionFieldColService.queryDefCol();
		 return Result.ok(list);


	 }


	 @AutoLog(value = "不合规行为表字段信息配置-获取默认字段")
	 @ApiOperation(value = "不合规行为表字段信息配置-获取默认字段", notes = "不合规行为表字段信息配置-获取默认字段")
	 @GetMapping(value = "/getDefSerCol")
	 public Result<?> getDefSearchCol(String platform ,HttpServletRequest req) {
		 List<TaskActionFieldColVO> list = taskActionFieldColService.getDefSerCol();
		 return Result.ok(list);


	 }

	 @AutoLog(value = "不合规行为表字段信息配置-根据不合规行为名称或编码获取配置")
	 @ApiOperation(value = "不合规行为表字段信息配置-根据不合规行为名称或编码获取配置", notes = "不合规行为表字段信息配置-根据不合规行为名称或编码获取配置")
	 @GetMapping(value = "/getColByAction")
	 public Result<?> getColByAction(String actionId, String actionName ,@RequestParam(name="platform") String platform, HttpServletRequest req) {
		 List<TaskActionFieldColVO> list = taskActionFieldColService.queryColByAction(platform, actionId, actionName);
		 return Result.ok(list);


	 }

	/* @AutoLog(value = "不合规行为表字段信息配置-获取默认字段")
	 @ApiOperation(value = "不合规行为表字段信息配置-获取默认字段", notes = "不合规行为表字段信息配置-获取默认字段")
	 @GetMapping(value = "/getSerColByAction")
	 public Result<?> getSerColByAction(String actionId, String actionName ,@RequestParam(name="platform") String platform, HttpServletRequest req) {
		 List<TaskActionFieldColVO> list = taskActionFieldColService.getSerColByAction(platform, actionId, actionName);
		 return Result.ok(list);


	 }*/

	/* @AutoLog(value = "不合规行为表字段信息配置-获取默认字段")
	 @ApiOperation(value = "不合规行为表字段信息配置-获取默认字段", notes = "不合规行为表字段信息配置-获取默认字段")
	 @GetMapping(value = "/getDefColSimple")
	 public Result<?> getDefColSimple(@RequestParam(name="platform") String platform ,HttpServletRequest req) {
		 List<TaskActionFieldCol> list = taskActionFieldColService.queryDefColSimple(MedicalConstant.PLATFORM_SERVICE);
		 return Result.ok(list);


	 }*/
/*
	 @AutoLog(value = "不合规行为表字段信息配置-获取全部字段")
	 @ApiOperation(value = "不合规行为表字段信息配置-获取全部字段", notes = "不合规行为表字段信息配置-获取全部字段")
	 @GetMapping(value = "/listAll")
	 public Result<?> listAll(HttpServletRequest req) {
		 *//*List<TaskActionFieldCol> list = taskActionFieldColService.list(new QueryWrapper<TaskActionFieldCol>()
				 .eq("status", "normal")
		 );*//*
		 List<TaskActionFieldCol> list = taskActionFieldColService.list();
		 return Result.ok(list);
	 }*/

	 @AutoLog(value = "不合规行为表字段信息配置-获取符合条件的列表")
	 @ApiOperation(value = "不合规行为表字段信息配置-获取符合条件的列表", notes = "不合规行为表字段信息配置-获取符合条件的列表")
	 @GetMapping(value = "/listAll")
	 public Result<?> listAll(TaskActionFieldCol taskActionFieldCol, HttpServletRequest req) {
		 QueryWrapper<TaskActionFieldCol> queryWrapper = QueryGenerator.initQueryWrapper(taskActionFieldCol, req.getParameterMap());

		 List<TaskActionFieldCol> list = taskActionFieldColService.list(queryWrapper);
		 return Result.ok(list);
	 }

	 @AutoLog(value = "不合规行为表字段信息配置-更新默认字段或搜索条件排序")
	 @ApiOperation(value = "不合规行为表字段信息配置-更新默认字段或搜索条件排序", notes = "不合规行为表字段信息配置-更新默认字段或搜索条件排序")
	 @PutMapping(value = "/updateOrder")
	 public Result<?> updateOrder(
	 		/*@RequestParam(name="colIds") String colIds,
	 		@RequestParam(name="type") String type,*/
			 @RequestBody TaskActionFieldCol[] list,
			HttpServletRequest req) {
		 /*List<String> idList = Arrays.asList(colIds.split(","));
		 List<TaskActionFieldCol> list = null;
		 AtomicInteger index = new AtomicInteger(1);
		 if("defSelect".equals(type)){
			 list = idList.stream().map(r -> {
				 TaskActionFieldCol bean = new TaskActionFieldCol();
				 bean.setColId(r);
				 bean.setOrderNo(String.valueOf(index.getAndIncrement()));
				 return bean;
			 }).collect(Collectors.toList());
		 } else if("defSearch".equals(type)){
			 list = idList.stream().map(r -> {
				 TaskActionFieldCol bean = new TaskActionFieldCol();
				 bean.setColId(r);
				 bean.setSerOrderNo(String.valueOf(index.getAndIncrement()));
				 return bean;
			 }).collect(Collectors.toList());
		 }*/

		 taskActionFieldColService.updateBatchById(Arrays.asList(list));
		 return Result.ok();
	 }

	 @AutoLog(value = "不同不合规行为显示字段配置-列表查询")
	 @ApiOperation(value = "不同不合规行为显示字段配置-列表查询", notes = "不同不合规行为显示字段配置-列表查询")
	 @GetMapping(value = "/queryIdsByConfigId")
	 public Result<?> queryIdsByConfigId(@RequestParam(name="configId") String configId, HttpServletRequest req) {
		 /*List<TaskActionFieldCol> list = taskActionFieldColService.list(new QueryWrapper<TaskActionFieldCol>()
				 .eq("STATUS", "normal")
				 .in("COL_ID",
						 "SELECT COL_ID FROM TASK_ACTION_FIELD_RELA WHERE CONFIG_ID = '" + configId + "'")
		 );*/

//		 List<String> list = jdbcTemplate.queryForList("SELECT COL_ID FROM TASK_ACTION_FIELD_RELA WHERE CONFIG_ID = '" + configId + "'", String.class);
		 List<String> list = taskActionFieldRelaService.listObjs(new QueryWrapper<TaskActionFieldRela>()
				 .select("COL_ID").eq("CONFIG_ID", configId).orderByAsc("ORDER_NO"), Object::toString);
		 return Result.ok(list);
	 }

	 @AutoLog(value = "不同不合规行为显示字段配置-列表查询")
	 @ApiOperation(value = "不同不合规行为显示字段配置-列表查询", notes = "不同不合规行为显示字段配置-列表查询")
	 @GetMapping(value = "/queryByConfigId")
	 public Result<?> queryByConfigId(@RequestParam(name="configId") String configId, HttpServletRequest req) {
		 /*List<TaskActionFieldCol> list = taskActionFieldColService.list(new QueryWrapper<TaskActionFieldCol>()
				 .eq("STATUS", "normal")
				 .in("COL_ID",
						 "SELECT COL_ID FROM TASK_ACTION_FIELD_RELA WHERE CONFIG_ID = '" + configId + "'")
		 );*/

//		 List<String> list = jdbcTemplate.queryForList("SELECT COL_ID FROM TASK_ACTION_FIELD_RELA WHERE CONFIG_ID = '" + configId + "'", String.class);
		 List<TaskActionFieldRela> list = taskActionFieldRelaService.list(new QueryWrapper<TaskActionFieldRela>()
				 .select("COL_ID", "COL_CNNAME", "ORDER_NO")
				 .eq("CONFIG_ID", configId)
				 .orderByAsc("ORDER_NO")
		 );
		 return Result.ok(list);
	 }

	 @AutoLog(value = "不同不合规行为显示字段配置-列表查询")
	 @ApiOperation(value = "不同不合规行为显示字段配置-列表查询", notes = "不同不合规行为显示字段配置-列表查询")
	 @GetMapping(value = "/queryDelByConfigId")
	 public Result<?> queryDelByConfigId(@RequestParam(name="configId") String configId,String platform, HttpServletRequest req) {
		 /*List<TaskActionFieldCol> list = taskActionFieldColService.list(new QueryWrapper<TaskActionFieldCol>()
				 .eq("STATUS", "normal")
				 .in("COL_ID",
						 "SELECT COL_ID FROM TASK_ACTION_FIELD_RELA WHERE CONFIG_ID = '" + configId + "'")
		 );*/

//		 List<String> list = jdbcTemplate.queryForList("SELECT COL_ID FROM TASK_ACTION_FIELD_RELA WHERE CONFIG_ID = '" + configId + "'", String.class);
		 List<TaskActionFieldColVO> list = taskActionFieldColService.queryDelByConfigId(configId);
		 return Result.ok(list);
	 }


	 @AutoLog(value = "不同不合规行为显示字段配置-列表查询")
	 @ApiOperation(value = "不同不合规行为显示字段配置-列表查询", notes = "不同不合规行为显示字段配置-列表查询")
	 @GetMapping(value = "/querySerByConfigId")
	 public Result<?> querySerByConfigId(@RequestParam(name="configId") String configId,
										 String platform,
										 HttpServletRequest req) {

		 List<TaskActionFieldColVO> list = taskActionFieldColService.querySerByConfigId(configId);
		 return Result.ok(list);
	 }

	/**
	 * 分页列表查询
	 *
	 * @param taskActionFieldCol
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "不合规行为表字段信息配置-分页列表查询")
	@ApiOperation(value="不合规行为表字段信息配置-分页列表查询", notes="不合规行为表字段信息配置-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskActionFieldCol taskActionFieldCol,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TaskActionFieldCol> queryWrapper = QueryGenerator.initQueryWrapper(taskActionFieldCol, req.getParameterMap());
		Page<TaskActionFieldCol> page = new Page<TaskActionFieldCol>(pageNo, pageSize);
		IPage<TaskActionFieldCol> pageList = taskActionFieldColService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param taskActionFieldCol
	 * @return
	 */
	@AutoLog(value = "不合规行为表字段信息配置-添加")
	@ApiOperation(value="不合规行为表字段信息配置-添加", notes="不合规行为表字段信息配置-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskActionFieldCol taskActionFieldCol) {
		taskActionFieldColService.save(taskActionFieldCol);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param taskActionFieldCol
	 * @return
	 */
	@AutoLog(value = "不合规行为表字段信息配置-编辑")
	@ApiOperation(value="不合规行为表字段信息配置-编辑", notes="不合规行为表字段信息配置-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskActionFieldCol taskActionFieldCol) {
		taskActionFieldColService.updateById(taskActionFieldCol);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合规行为表字段信息配置-通过id删除")
	@ApiOperation(value="不合规行为表字段信息配置-通过id删除", notes="不合规行为表字段信息配置-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskActionFieldColService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "不合规行为表字段信息配置-批量删除")
	@ApiOperation(value="不合规行为表字段信息配置-批量删除", notes="不合规行为表字段信息配置-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskActionFieldColService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合规行为表字段信息配置-通过id查询")
	@ApiOperation(value="不合规行为表字段信息配置-通过id查询", notes="不合规行为表字段信息配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskActionFieldCol taskActionFieldCol = taskActionFieldColService.getById(id);
		return Result.ok(taskActionFieldCol);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskActionFieldCol
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskActionFieldCol taskActionFieldCol) {
      return super.exportXls(request, taskActionFieldCol, TaskActionFieldCol.class, "不合规行为表字段信息配置");
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
      return super.importExcel(request, response, TaskActionFieldCol.class);
  }

}
