package com.ai.modules.task.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.task.entity.TaskActionBatchExtmap;
import com.ai.modules.task.service.ITaskActionBatchExtmapService;
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
 * @Description: 批次扩展字段映射表
 * @Author: jeecg-boot
 * @Date:   2021-06-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="批次扩展字段映射表")
@RestController
@RequestMapping("/task/taskActionBatchExtmap")
public class TaskActionBatchExtmapController extends JeecgController<TaskActionBatchExtmap, ITaskActionBatchExtmapService> {
	@Autowired
	private ITaskActionBatchExtmapService taskActionBatchExtmapService;

	/**
	 * 分页列表查询
	 *
	 * @param taskActionBatchExtmap
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "批次扩展字段映射表-分页列表查询")
	@ApiOperation(value="批次扩展字段映射表-分页列表查询", notes="批次扩展字段映射表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskActionBatchExtmap taskActionBatchExtmap,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TaskActionBatchExtmap> queryWrapper = QueryGenerator.initQueryWrapper(taskActionBatchExtmap, req.getParameterMap());
		Page<TaskActionBatchExtmap> page = new Page<TaskActionBatchExtmap>(pageNo, pageSize);
		IPage<TaskActionBatchExtmap> pageList = taskActionBatchExtmapService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param taskActionBatchExtmap
	 * @return
	 */
	@AutoLog(value = "批次扩展字段映射表-添加")
	@ApiOperation(value="批次扩展字段映射表-添加", notes="批次扩展字段映射表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskActionBatchExtmap taskActionBatchExtmap) {
		taskActionBatchExtmapService.save(taskActionBatchExtmap);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param taskActionBatchExtmaps
	  * @return
	  */
	 @AutoLog(value = "批次扩展字段映射表-添加多个")
	 @ApiOperation(value="批次扩展字段映射表-添加多个", notes="批次扩展字段映射表-添加多个")
	 @PostMapping(value = "/addBatch")
	 public Result<?> addBatch(@RequestBody List<TaskActionBatchExtmap> taskActionBatchExtmaps) {
	 	 Map<String, List<String>> actionBatchIdsMap = new HashMap<>();

		 taskActionBatchExtmaps.forEach(r -> {
			 List<String> list = actionBatchIdsMap.computeIfAbsent(r.getActionId(), k -> new ArrayList<>());
			 list.add(r.getBatchId());
		 });

		 QueryWrapper<TaskActionBatchExtmap> queryWrapper = new QueryWrapper<>();
		 actionBatchIdsMap.forEach((key, value) -> queryWrapper.or(q -> q
				 .eq("ACTION_ID", key)
				 .in("BATCH_ID", value)
		 ));

		 taskActionBatchExtmapService.remove(queryWrapper);
		 taskActionBatchExtmapService.saveBatch(taskActionBatchExtmaps);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param taskActionBatchExtmap
	 * @return
	 */
	@AutoLog(value = "批次扩展字段映射表-编辑")
	@ApiOperation(value="批次扩展字段映射表-编辑", notes="批次扩展字段映射表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskActionBatchExtmap taskActionBatchExtmap) {
		taskActionBatchExtmapService.updateById(taskActionBatchExtmap);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "批次扩展字段映射表-通过id删除")
	@ApiOperation(value="批次扩展字段映射表-通过id删除", notes="批次扩展字段映射表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskActionBatchExtmapService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "批次扩展字段映射表-批量删除")
	@ApiOperation(value="批次扩展字段映射表-批量删除", notes="批次扩展字段映射表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskActionBatchExtmapService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "批次扩展字段映射表-通过id查询")
	@ApiOperation(value="批次扩展字段映射表-通过id查询", notes="批次扩展字段映射表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskActionBatchExtmap taskActionBatchExtmap = taskActionBatchExtmapService.getById(id);
		return Result.ok(taskActionBatchExtmap);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskActionBatchExtmap
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskActionBatchExtmap taskActionBatchExtmap) {
      return super.exportXls(request, taskActionBatchExtmap, TaskActionBatchExtmap.class, "批次扩展字段映射表");
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
      return super.importExcel(request, response, TaskActionBatchExtmap.class);
  }

}
