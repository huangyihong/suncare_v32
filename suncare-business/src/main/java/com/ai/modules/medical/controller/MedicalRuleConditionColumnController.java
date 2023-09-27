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
import com.ai.modules.medical.entity.MedicalRuleConditionColumn;
import com.ai.modules.medical.service.IMedicalRuleConditionColumnService;
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
 * @Description: 规则依赖字段表
 * @Author: jeecg-boot
 * @Date:   2021-03-16
 * @Version: V1.0
 */
@Slf4j
@Api(tags="规则依赖字段表")
@RestController
@RequestMapping("/medical/medicalRuleConditionColumn")
public class MedicalRuleConditionColumnController extends JeecgController<MedicalRuleConditionColumn, IMedicalRuleConditionColumnService> {
	@Autowired
	private IMedicalRuleConditionColumnService medicalRuleConditionColumnService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalRuleConditionColumn
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-分页列表查询")
	@ApiOperation(value="规则依赖字段表-分页列表查询", notes="规则依赖字段表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalRuleConditionColumn medicalRuleConditionColumn,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalRuleConditionColumn> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleConditionColumn, req.getParameterMap());
		Page<MedicalRuleConditionColumn> page = new Page<MedicalRuleConditionColumn>(pageNo, pageSize);
		IPage<MedicalRuleConditionColumn> pageList = medicalRuleConditionColumnService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param medicalRuleConditionColumn
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-添加")
	@ApiOperation(value="规则依赖字段表-添加", notes="规则依赖字段表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalRuleConditionColumn medicalRuleConditionColumn) {
		medicalRuleConditionColumnService.save(medicalRuleConditionColumn);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param medicalRuleConditionColumn
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-编辑")
	@ApiOperation(value="规则依赖字段表-编辑", notes="规则依赖字段表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalRuleConditionColumn medicalRuleConditionColumn) {
		medicalRuleConditionColumnService.updateById(medicalRuleConditionColumn);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-通过id删除")
	@ApiOperation(value="规则依赖字段表-通过id删除", notes="规则依赖字段表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalRuleConditionColumnService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-批量删除")
	@ApiOperation(value="规则依赖字段表-批量删除", notes="规则依赖字段表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalRuleConditionColumnService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-通过id查询")
	@ApiOperation(value="规则依赖字段表-通过id查询", notes="规则依赖字段表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalRuleConditionColumn medicalRuleConditionColumn = medicalRuleConditionColumnService.getById(id);
		return Result.ok(medicalRuleConditionColumn);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalRuleConditionColumn
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalRuleConditionColumn medicalRuleConditionColumn) {
      return super.exportXls(request, medicalRuleConditionColumn, MedicalRuleConditionColumn.class, "规则依赖字段表");
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
      return super.importExcel(request, response, MedicalRuleConditionColumn.class);
  }

}
