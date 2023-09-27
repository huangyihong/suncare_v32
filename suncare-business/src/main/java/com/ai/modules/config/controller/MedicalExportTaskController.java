package com.ai.modules.config.controller;

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
import com.ai.modules.config.entity.MedicalExportTask;
import com.ai.modules.config.service.IMedicalExportTaskService;
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
 * @Description: 导出文件任务
 * @Author: jeecg-boot
 * @Date:   2020-01-07
 * @Version: V1.0
 */
@Slf4j
@Api(tags="导出文件任务")
@RestController
@RequestMapping("/config/medicalExportTask")
public class MedicalExportTaskController extends JeecgController<MedicalExportTask, IMedicalExportTaskService> {
	@Autowired
	private IMedicalExportTaskService medicalExportTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalExportTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "导出文件任务-分页列表查询")
	@ApiOperation(value="导出文件任务-分页列表查询", notes="导出文件任务-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalExportTask medicalExportTask,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalExportTask> queryWrapper = QueryGenerator.initQueryWrapper(medicalExportTask, req.getParameterMap());
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		queryWrapper.and(q -> q.eq("DATA_SOURCE", sysUser.getDataSource()).or().isNull("DATA_SOURCE"));
		Page<MedicalExportTask> page = new Page<MedicalExportTask>(pageNo, pageSize);
		IPage<MedicalExportTask> pageList = medicalExportTaskService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalExportTask
	 * @return
	 */
	@AutoLog(value = "导出文件任务-添加")
	@ApiOperation(value="导出文件任务-添加", notes="导出文件任务-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalExportTask medicalExportTask) {
		medicalExportTaskService.save(medicalExportTask);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalExportTask
	 * @return
	 */
	@AutoLog(value = "导出文件任务-编辑")
	@ApiOperation(value="导出文件任务-编辑", notes="导出文件任务-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalExportTask medicalExportTask) {
		medicalExportTaskService.updateById(medicalExportTask);
		return Result.ok("编辑成功!");
	}

	@AutoLog(value = "导出文件任务-编辑多个")
	@ApiOperation(value="导出文件任务-编辑多个", notes="导出文件任务-编辑多个")
	@PutMapping(value = "/editBatch")
	public Result<?> editBatch(@RequestBody List<MedicalExportTask> list) {
		medicalExportTaskService.updateBatchById(list);
		return Result.ok("编辑成功!");
	}



	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "导出文件任务-通过id查询")
	@ApiOperation(value="导出文件任务-通过id查询", notes="导出文件任务-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalExportTask medicalExportTask = medicalExportTaskService.getById(id);
		return Result.ok(medicalExportTask);
	}


}
