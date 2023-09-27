package com.ai.modules.probe.controller;

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
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.ai.modules.probe.service.IMedicalProbeFlowRuleService;
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
 * @Description: 流程图树
 * @Author: jeecg-boot
 * @Date:   2019-11-21
 * @Version: V1.0
 */
@Slf4j
@Api(tags="流程图树")
@RestController
@RequestMapping("/probe/medicalProbeFlowRule")
public class MedicalProbeFlowRuleController extends JeecgController<MedicalProbeFlowRule, IMedicalProbeFlowRuleService> {
	@Autowired
	private IMedicalProbeFlowRuleService medicalProbeFlowRuleService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalProbeFlowRule
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "流程图树-分页列表查询")
	@ApiOperation(value="流程图树-分页列表查询", notes="流程图树-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalProbeFlowRule medicalProbeFlowRule,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalProbeFlowRule> queryWrapper = QueryGenerator.initQueryWrapper(medicalProbeFlowRule, req.getParameterMap());
		Page<MedicalProbeFlowRule> page = new Page<MedicalProbeFlowRule>(pageNo, pageSize);
		IPage<MedicalProbeFlowRule> pageList = medicalProbeFlowRuleService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalProbeFlowRule
	 * @return
	 */
	@AutoLog(value = "流程图树-添加")
	@ApiOperation(value="流程图树-添加", notes="流程图树-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalProbeFlowRule medicalProbeFlowRule) {
		medicalProbeFlowRuleService.save(medicalProbeFlowRule);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalProbeFlowRule
	 * @return
	 */
	@AutoLog(value = "流程图树-编辑")
	@ApiOperation(value="流程图树-编辑", notes="流程图树-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalProbeFlowRule medicalProbeFlowRule) {
		medicalProbeFlowRuleService.updateById(medicalProbeFlowRule);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "流程图树-通过id删除")
	@ApiOperation(value="流程图树-通过id删除", notes="流程图树-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalProbeFlowRuleService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "流程图树-批量删除")
	@ApiOperation(value="流程图树-批量删除", notes="流程图树-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalProbeFlowRuleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "流程图树-通过id查询")
	@ApiOperation(value="流程图树-通过id查询", notes="流程图树-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalProbeFlowRule medicalProbeFlowRule = medicalProbeFlowRuleService.getById(id);
		return Result.ok(medicalProbeFlowRule);
	}

	 /**
	  * 通过caseId查询所有规则信息
	  *
	  * @param caseId
	  * @return
	  */
	 @AutoLog(value = "流程图-通过id查询所有探查信息")
	 @ApiOperation(value="流程图-通过id查询所有探查信息", notes="流程图-通过id查询所有rule")
	 @GetMapping(value = "/getRulesByCaseId")
	 public Result<?> getRulesByCaseId(@RequestParam(name="caseId") String caseId) {

		 return Result.ok(medicalProbeFlowRuleService.queryByCaseId(caseId));
	 }

}
