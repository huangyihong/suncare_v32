package com.ai.modules.task.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.utils.IdUtils;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.entity.TaskProjectClient;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.service.ITaskProjectClientService;
import com.ai.modules.task.vo.TaskProjectVO;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.service.ITaskProjectService;
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
 * @Description: 任务项目
 * @Author: jeecg-boot
 * @Date:   2020-01-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="任务项目")
@RestController
@RequestMapping("/task/taskProject")
public class TaskProjectController extends JeecgController<TaskProject, ITaskProjectService> {
	@Autowired
	private ITaskProjectService taskProjectService;

	@Autowired
	private ITaskProjectBatchService taskProjectBatchService;

	@Autowired
	private ITaskProjectClientService taskProjectClientService;

	/**
	 * 分页列表查询
	 *
	 * @param taskProject
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "任务项目-分页列表查询")
	@ApiOperation(value="任务项目-分页列表查询", notes="任务项目-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TaskProject taskProject,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//		log.info("数据源：" + user.getDataSource());
		QueryWrapper<TaskProject> queryWrapper = QueryGenerator.initQueryWrapper(taskProject, req.getParameterMap());
		queryWrapper.eq("DATA_SOURCE",user.getDataSource());
		Page<TaskProject> page = new Page<>(pageNo, pageSize);
//		IPage<TaskProject> pageList = taskProjectService.page(page, queryWrapper);
		IPage<TaskProjectVO> pageList = taskProjectService.pageVO(page, queryWrapper);
		return Result.ok(pageList);
	}

	 @AutoLog(value = "任务项目-无分页列表条件查询")
	 @ApiOperation(value="任务项目-无分页列表条件查询", notes="任务项目-无分页列表条件查询")
	 @GetMapping(value = "/queryList")
	 public Result<?> queryList(TaskProject taskProject, HttpServletRequest req) {
		 QueryWrapper<TaskProject> queryWrapper = QueryGenerator.initQueryWrapper(taskProject, req.getParameterMap());
		 List<TaskProject> list = taskProjectService.list(queryWrapper);
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param taskProject
	 * @return
	 */
	@AutoLog(value = "任务项目-添加")
	@ApiOperation(value="任务项目-添加", notes="任务项目-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody TaskProjectVO taskProject) throws Exception {

		taskProjectService.saveProject(taskProject);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param taskProject
	 * @return
	 */
	@AutoLog(value = "任务项目-编辑")
	@ApiOperation(value="任务项目-编辑", notes="任务项目-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody TaskProjectVO taskProject) throws Exception {

		taskProjectService.updateProjectById(taskProject);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务项目-通过id删除")
	@ApiOperation(value="任务项目-通过id删除", notes="任务项目-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id") String id) {
		int batchCount = taskProjectBatchService.count(
				new QueryWrapper<TaskProjectBatch>().eq("PROJECT_ID",id));
		if(batchCount > 0){
			return Result.error("请先删除项目下的批次!");
		}
		// 删除关联客户
		taskProjectClientService.remove(
				new QueryWrapper<TaskProjectClient>().eq("PROJECT_ID",id));
		taskProjectService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "任务项目-批量删除")
	@ApiOperation(value="任务项目-批量删除", notes="任务项目-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskProjectService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务项目-通过id查询")
	@ApiOperation(value="任务项目-通过id查询", notes="任务项目-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		TaskProject taskProject = taskProjectService.getById(id);
		return Result.ok(taskProject);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param taskProject
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, TaskProject taskProject) {
      return super.exportXls(request, taskProject, TaskProject.class, "任务项目");
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
      return super.importExcel(request, response, TaskProject.class);
  }

	 /**
	  * 通过excel导入医疗机构数据
	  *
	  * @param request
	  * @param response
	  * @return
	  */
	 @RequestMapping(value = "/importOrgExcel", method = RequestMethod.POST)
	 public Result<?> importOrgExcel(HttpServletRequest request, HttpServletResponse response) throws Exception{
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		 Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		 for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			 MultipartFile file = entity.getValue();// 获取上传文件对象
			 // 判断文件名是否为空
			 if (file == null) {
				 return Result.error("上传文件为空");
			 }
			 // 获取文件名
			 String name = file.getOriginalFilename();
			 // 判断文件大小、即名称
			 long size = file.getSize();
			 if (name == null || ("").equals(name) && size == 0) {
				 return Result.error("上传文件内容为空");
			 }
			 return this.taskProjectService.importOrgExcel(file, user);
		 }
		 return Result.error("上传文件为空");
	 }

}
