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
import com.ai.modules.medical.entity.MedicalDrugRuleId;
import com.ai.modules.medical.service.IMedicalDrugRuleIdService;
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
 * @Description: RULE_ID
 * @Author: jeecg-boot
 * @Date:   2019-12-23
 * @Version: V1.0
 */
@Slf4j
@Api(tags="RULE_ID")
@RestController
@RequestMapping("/medical/medicalDrugRuleId")
public class MedicalDrugRuleIdController extends JeecgController<MedicalDrugRuleId, IMedicalDrugRuleIdService> {
	@Autowired
	private IMedicalDrugRuleIdService medicalDrugRuleIdService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalDrugRuleId
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "RULE_ID-分页列表查询")
	@ApiOperation(value="RULE_ID-分页列表查询", notes="RULE_ID-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDrugRuleId medicalDrugRuleId,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDrugRuleId> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugRuleId, req.getParameterMap());
		Page<MedicalDrugRuleId> page = new Page<MedicalDrugRuleId>(pageNo, pageSize);
		IPage<MedicalDrugRuleId> pageList = medicalDrugRuleIdService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param medicalDrugRuleId
	 * @return
	 */
	@AutoLog(value = "RULE_ID-添加")
	@ApiOperation(value="RULE_ID-添加", notes="RULE_ID-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalDrugRuleId medicalDrugRuleId) {
		medicalDrugRuleIdService.save(medicalDrugRuleId);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param medicalDrugRuleId
	 * @return
	 */
	@AutoLog(value = "RULE_ID-编辑")
	@ApiOperation(value="RULE_ID-编辑", notes="RULE_ID-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalDrugRuleId medicalDrugRuleId) {
		medicalDrugRuleIdService.updateById(medicalDrugRuleId);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "RULE_ID-通过id删除")
	@ApiOperation(value="RULE_ID-通过id删除", notes="RULE_ID-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDrugRuleIdService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "RULE_ID-批量删除")
	@ApiOperation(value="RULE_ID-批量删除", notes="RULE_ID-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDrugRuleIdService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "RULE_ID-通过id查询")
	@ApiOperation(value="RULE_ID-通过id查询", notes="RULE_ID-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDrugRuleId medicalDrugRuleId = medicalDrugRuleIdService.getById(id);
		return Result.ok(medicalDrugRuleId);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDrugRuleId
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDrugRuleId medicalDrugRuleId) {
      return super.exportXls(request, medicalDrugRuleId, MedicalDrugRuleId.class, "RULE_ID");
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
      return super.importExcel(request, response, MedicalDrugRuleId.class);
  }

}
