package com.ai.modules.medical.controller;

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
import com.ai.modules.medical.entity.MedicalDictMergeLog;
import com.ai.modules.medical.service.IMedicalDictMergeLogService;
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
 * @Description: 基础字典合并日志表
 * @Author: jeecg-boot
 * @Date:   2021-07-20
 * @Version: V1.0
 */
@Slf4j
@Api(tags="基础字典合并日志表")
@RestController
@RequestMapping("/medical/medicalDictMergeLog")
public class MedicalDictMergeLogController extends JeecgController<MedicalDictMergeLog, IMedicalDictMergeLogService> {
	@Autowired
	private IMedicalDictMergeLogService medicalDictMergeLogService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalDictMergeLog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "基础字典合并日志表-分页列表查询")
	@ApiOperation(value="基础字典合并日志表-分页列表查询", notes="基础字典合并日志表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDictMergeLog medicalDictMergeLog,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDictMergeLog> queryWrapper = QueryGenerator.initQueryWrapper(medicalDictMergeLog, req.getParameterMap());
		Page<MedicalDictMergeLog> page = new Page<MedicalDictMergeLog>(pageNo, pageSize);
		IPage<MedicalDictMergeLog> pageList = medicalDictMergeLogService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param medicalDictMergeLog
	 * @return
	 */
	@AutoLog(value = "基础字典合并日志表-添加")
	@ApiOperation(value="基础字典合并日志表-添加", notes="基础字典合并日志表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalDictMergeLog medicalDictMergeLog) {
		medicalDictMergeLogService.save(medicalDictMergeLog);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param medicalDictMergeLog
	 * @return
	 */
	@AutoLog(value = "基础字典合并日志表-编辑")
	@ApiOperation(value="基础字典合并日志表-编辑", notes="基础字典合并日志表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalDictMergeLog medicalDictMergeLog) {
		medicalDictMergeLogService.updateById(medicalDictMergeLog);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "基础字典合并日志表-通过id删除")
	@ApiOperation(value="基础字典合并日志表-通过id删除", notes="基础字典合并日志表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDictMergeLogService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "基础字典合并日志表-批量删除")
	@ApiOperation(value="基础字典合并日志表-批量删除", notes="基础字典合并日志表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDictMergeLogService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "基础字典合并日志表-通过id查询")
	@ApiOperation(value="基础字典合并日志表-通过id查询", notes="基础字典合并日志表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDictMergeLog medicalDictMergeLog = medicalDictMergeLogService.getById(id);
		return Result.ok(medicalDictMergeLog);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDictMergeLog
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDictMergeLog medicalDictMergeLog) {
      return super.exportXls(request, medicalDictMergeLog, MedicalDictMergeLog.class, "基础字典合并日志表");
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
      return super.importExcel(request, response, MedicalDictMergeLog.class);
  }

}
