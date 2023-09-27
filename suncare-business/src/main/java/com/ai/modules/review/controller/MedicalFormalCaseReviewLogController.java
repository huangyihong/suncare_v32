package com.ai.modules.review.controller;

import java.util.ArrayList;
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

import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.review.entity.MedicalFormalCaseReviewLog;
import com.ai.modules.review.service.IMedicalFormalCaseReviewLogService;
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
 * @Description: 不合理行为就诊记录审查日志表
 * @Author: jeecg-boot
 * @Date:   2020-02-07
 * @Version: V1.0
 */
@Slf4j
@Api(tags="不合理行为就诊记录审查日志表")
@RestController
@RequestMapping("/review/medicalFormalCaseReviewLog")
public class MedicalFormalCaseReviewLogController extends JeecgController<MedicalFormalCaseReviewLog, IMedicalFormalCaseReviewLogService> {
	@Autowired
	private IMedicalFormalCaseReviewLogService medicalFormalCaseReviewLogService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalFormalCaseReviewLog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查日志表-分页列表查询")
	@ApiOperation(value="不合理行为就诊记录审查日志表-分页列表查询", notes="不合理行为就诊记录审查日志表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalFormalCaseReviewLog medicalFormalCaseReviewLog,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalFormalCaseReviewLog> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCaseReviewLog, req.getParameterMap());
		Page<MedicalFormalCaseReviewLog> page = new Page<MedicalFormalCaseReviewLog>(pageNo, pageSize);
		IPage<MedicalFormalCaseReviewLog> pageList = medicalFormalCaseReviewLogService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	@AutoLog(value = "审查日志列表查询")
	@ApiOperation(value="审查日志列表查询", notes="审查日志列表查询")
	@GetMapping(value = "/logList")
	public Result<?> logList(MedicalFormalCaseReviewLog medicalFormalCaseReviewLog,HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalFormalCaseReviewLog> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCaseReviewLog, req.getParameterMap());
		queryWrapper.orderByDesc("CREATE_TIME");
		List<MedicalFormalCaseReviewLog> list = medicalFormalCaseReviewLogService.list(queryWrapper);
		return Result.ok(list);
	}
	
	/**
	 * 添加
	 *
	 * @param medicalFormalCaseReviewLog
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查日志表-添加")
	@ApiOperation(value="不合理行为就诊记录审查日志表-添加", notes="不合理行为就诊记录审查日志表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalFormalCaseReviewLog medicalFormalCaseReviewLog) {
		medicalFormalCaseReviewLogService.save(medicalFormalCaseReviewLog);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param medicalFormalCaseReviewLog
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查日志表-编辑")
	@ApiOperation(value="不合理行为就诊记录审查日志表-编辑", notes="不合理行为就诊记录审查日志表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalFormalCaseReviewLog medicalFormalCaseReviewLog) {
		medicalFormalCaseReviewLogService.updateById(medicalFormalCaseReviewLog);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查日志表-通过id删除")
	@ApiOperation(value="不合理行为就诊记录审查日志表-通过id删除", notes="不合理行为就诊记录审查日志表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalFormalCaseReviewLogService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查日志表-批量删除")
	@ApiOperation(value="不合理行为就诊记录审查日志表-批量删除", notes="不合理行为就诊记录审查日志表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalFormalCaseReviewLogService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查日志表-通过id查询")
	@ApiOperation(value="不合理行为就诊记录审查日志表-通过id查询", notes="不合理行为就诊记录审查日志表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalFormalCaseReviewLog medicalFormalCaseReviewLog = medicalFormalCaseReviewLogService.getById(id);
		return Result.ok(medicalFormalCaseReviewLog);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalFormalCaseReviewLog
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalFormalCaseReviewLog medicalFormalCaseReviewLog) {
      return super.exportXls(request, medicalFormalCaseReviewLog, MedicalFormalCaseReviewLog.class, "不合理行为就诊记录审查日志表");
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
      return super.importExcel(request, response, MedicalFormalCaseReviewLog.class);
  }

}
