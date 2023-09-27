package com.ai.modules.task.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.task.entity.TaskProjectClient;
import com.ai.modules.task.service.ITaskProjectClientService;
import java.util.Date;
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
 * @Description: 项目客户关联
 * @Author: jeecg-boot
 * @Date:   2020-02-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="项目客户关联")
@RestController
@RequestMapping("/task/taskProjectClient")
public class TaskProjectClientController extends JeecgController<TaskProjectClient, ITaskProjectClientService> {
	@Autowired
	private ITaskProjectClientService taskProjectClientService;

	/**
	 * 分页列表查询
	 *
	 * @param taskProjectClient
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "项目客户关联-分页列表查询")
	@ApiOperation(value="项目客户关联-分页列表查询", notes="项目客户关联-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskProjectClient taskProjectClient,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TaskProjectClient> queryWrapper = QueryGenerator.initQueryWrapper(taskProjectClient, req.getParameterMap());
		Page<TaskProjectClient> page = new Page<TaskProjectClient>(pageNo, pageSize);
		IPage<TaskProjectClient> pageList = taskProjectClientService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 @AutoLog(value = "项目客户关联-查询项目关联的用户")
	 @ApiOperation(value="项目客户关联-查询项目关联的用户", notes="项目客户关联-查询项目关联的用户")
	 @GetMapping(value = "/queryClientsByProject")
	 public Result<?> queryClientsByProject( @RequestParam(name="projectId", defaultValue="1") String projectId,HttpServletRequest req) {
		 return Result.ok(taskProjectClientService.selectUsersByProject(projectId));
	 }

	 @AutoLog(value = "项目客户关联-用户所关联的项目")
	 @ApiOperation(value="项目客户关联-用户所关联的项目", notes="项目客户关联-用户所关联的项目")
	 @GetMapping(value = "/queryProjectSelf")
	 public Result<?> queryProjectSelf(HttpServletRequest req) {
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 return Result.ok(taskProjectClientService.selectProjectByUser(user.getId()));
	 }

	/**
	 * 添加
	 *
	 * @param taskProjectClient
	 * @return
	 */
	@AutoLog(value = "项目客户关联-添加")
	@ApiOperation(value="项目客户关联-添加", notes="项目客户关联-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskProjectClient taskProjectClient) {
		taskProjectClientService.save(taskProjectClient);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param taskProjectClient
	 * @return
	 */
	@AutoLog(value = "项目客户关联-编辑")
	@ApiOperation(value="项目客户关联-编辑", notes="项目客户关联-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskProjectClient taskProjectClient) {
		taskProjectClientService.updateById(taskProjectClient);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目客户关联-通过id删除")
	@ApiOperation(value="项目客户关联-通过id删除", notes="项目客户关联-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		taskProjectClientService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目客户关联-批量删除")
	@ApiOperation(value="项目客户关联-批量删除", notes="项目客户关联-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskProjectClientService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目客户关联-通过id查询")
	@ApiOperation(value="项目客户关联-通过id查询", notes="项目客户关联-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskProjectClient taskProjectClient = taskProjectClientService.getById(id);
		return Result.ok(taskProjectClient);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskProjectClient
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskProjectClient taskProjectClient) {
      return super.exportXls(request, taskProjectClient, TaskProjectClient.class, "项目客户关联");
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
      return super.importExcel(request, response, TaskProjectClient.class);
  }

}
