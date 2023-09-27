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
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.service.IMedicalRuleConditionSetService;
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
 * @Description: 通用规则条件集
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
@Slf4j
@Api(tags="通用规则条件集")
@RestController
@RequestMapping("/medical/medicalRuleConditionSet")
public class MedicalRuleConditionSetController extends JeecgController<MedicalRuleConditionSet, IMedicalRuleConditionSetService> {
	@Autowired
	private IMedicalRuleConditionSetService medicalRuleConditionSetService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalRuleConditionSet
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "通用规则条件集-分页列表查询")
	@ApiOperation(value="通用规则条件集-分页列表查询", notes="通用规则条件集-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalRuleConditionSet medicalRuleConditionSet,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalRuleConditionSet> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleConditionSet, req.getParameterMap());
		Page<MedicalRuleConditionSet> page = new Page<MedicalRuleConditionSet>(pageNo, pageSize);
		IPage<MedicalRuleConditionSet> pageList = medicalRuleConditionSetService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalRuleConditionSet
	 * @return
	 */
	@AutoLog(value = "通用规则条件集-添加")
	@ApiOperation(value="通用规则条件集-添加", notes="通用规则条件集-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalRuleConditionSet medicalRuleConditionSet) {
		medicalRuleConditionSetService.save(medicalRuleConditionSet);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalRuleConditionSet
	 * @return
	 */
	@AutoLog(value = "通用规则条件集-编辑")
	@ApiOperation(value="通用规则条件集-编辑", notes="通用规则条件集-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalRuleConditionSet medicalRuleConditionSet) {
		medicalRuleConditionSetService.updateById(medicalRuleConditionSet);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "通用规则条件集-通过id删除")
	@ApiOperation(value="通用规则条件集-通过id删除", notes="通用规则条件集-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalRuleConditionSetService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "通用规则条件集-批量删除")
	@ApiOperation(value="通用规则条件集-批量删除", notes="通用规则条件集-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalRuleConditionSetService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "通用规则条件集-通过id查询")
	@ApiOperation(value="通用规则条件集-通过id查询", notes="通用规则条件集-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalRuleConditionSet medicalRuleConditionSet = medicalRuleConditionSetService.getById(id);
		return Result.ok(medicalRuleConditionSet);
	}

	 @AutoLog(value = "通用规则条件集-通过ruleId查询")
	 @ApiOperation(value="通用规则条件集-通过ruleId查询", notes="通用规则条件集-通过ruleId查询")
	 @GetMapping(value = "/queryByRuleId")
	 public Result<?> queryByRuleId(@RequestParam(name="ruleId",required=true) String ruleId) {
		 List<MedicalRuleConditionSet> list = medicalRuleConditionSetService.list(
		 		new QueryWrapper<MedicalRuleConditionSet>().eq("RULE_ID", ruleId).orderByAsc("TYPE", "GROUP_NO", "ORDER_NO")
		 );
		 return Result.ok(list);
	 }


	 /**
   * 导出excel
   *
   * @param request
   * @param medicalRuleConditionSet
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalRuleConditionSet medicalRuleConditionSet) {
      return super.exportXls(request, medicalRuleConditionSet, MedicalRuleConditionSet.class, "通用规则条件集");
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
      return super.importExcel(request, response, MedicalRuleConditionSet.class);
  }

}
