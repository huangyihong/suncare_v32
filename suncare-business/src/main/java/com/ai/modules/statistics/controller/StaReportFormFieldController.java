package com.ai.modules.statistics.controller;

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
import com.ai.modules.statistics.entity.StaReportFormField;
import com.ai.modules.statistics.service.IStaReportFormFieldService;
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
 * @Description: 报表查询条件定义表
 * @Author: jeecg-boot
 * @Date:   2020-08-21
 * @Version: V1.0
 */
@Slf4j
@Api(tags="报表查询条件定义表")
@RestController
@RequestMapping("/statistics/staReportFormField")
public class StaReportFormFieldController extends JeecgController<StaReportFormField, IStaReportFormFieldService> {
	@Autowired
	private IStaReportFormFieldService staReportFormFieldService;
	
	/**
	 * 分页列表查询
	 *
	 * @param staReportFormField
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "报表查询条件定义表-分页列表查询")
	@ApiOperation(value="报表查询条件定义表-分页列表查询", notes="报表查询条件定义表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(StaReportFormField staReportFormField,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<StaReportFormField> queryWrapper = QueryGenerator.initQueryWrapper(staReportFormField, req.getParameterMap());
		Page<StaReportFormField> page = new Page<StaReportFormField>(pageNo, pageSize);
		IPage<StaReportFormField> pageList = staReportFormFieldService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param staReportFormField
	 * @return
	 */
	@AutoLog(value = "报表查询条件定义表-添加")
	@ApiOperation(value="报表查询条件定义表-添加", notes="报表查询条件定义表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody StaReportFormField staReportFormField) {
		staReportFormFieldService.save(staReportFormField);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param staReportFormField
	 * @return
	 */
	@AutoLog(value = "报表查询条件定义表-编辑")
	@ApiOperation(value="报表查询条件定义表-编辑", notes="报表查询条件定义表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody StaReportFormField staReportFormField) {
		staReportFormFieldService.updateById(staReportFormField);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "报表查询条件定义表-通过id删除")
	@ApiOperation(value="报表查询条件定义表-通过id删除", notes="报表查询条件定义表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		staReportFormFieldService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "报表查询条件定义表-批量删除")
	@ApiOperation(value="报表查询条件定义表-批量删除", notes="报表查询条件定义表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.staReportFormFieldService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "报表查询条件定义表-通过id查询")
	@ApiOperation(value="报表查询条件定义表-通过id查询", notes="报表查询条件定义表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		StaReportFormField staReportFormField = staReportFormFieldService.getById(id);
		return Result.ok(staReportFormField);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param staReportFormField
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, StaReportFormField staReportFormField) {
      return super.exportXls(request, staReportFormField, StaReportFormField.class, "报表查询条件定义表");
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
      return super.importExcel(request, response, StaReportFormField.class);
  }

}
