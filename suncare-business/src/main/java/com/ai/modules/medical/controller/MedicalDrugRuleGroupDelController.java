package com.ai.modules.medical.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.medical.entity.MedicalDrugRuleGroup;
import com.ai.modules.medical.vo.MedicalDrugRuleGroupDelVO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.medical.entity.MedicalDrugRuleGroupDel;
import com.ai.modules.medical.service.IMedicalDrugRuleGroupDelService;
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
 * @Description: 医疗字典分组子项
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医疗字典分组子项")
@RestController
@RequestMapping("/medical/medicalDrugRuleGroupDel")
public class MedicalDrugRuleGroupDelController extends JeecgController<MedicalDrugRuleGroupDel, IMedicalDrugRuleGroupDelService> {
	@Autowired
	private IMedicalDrugRuleGroupDelService medicalDrugRuleGroupDelService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalDrugRuleGroupDel
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医疗字典分组子项-分页列表查询")
	@ApiOperation(value="医疗字典分组子项-分页列表查询", notes="医疗字典分组子项-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDrugRuleGroupDel medicalDrugRuleGroupDel,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDrugRuleGroupDel> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugRuleGroupDel, req.getParameterMap());
		Page<MedicalDrugRuleGroupDel> page = new Page<MedicalDrugRuleGroupDel>(pageNo, pageSize);
		IPage<MedicalDrugRuleGroupDel> pageList = medicalDrugRuleGroupDelService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 不分页列表查询
	  *
	  * @param medicalDrugRuleGroupDel
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "医疗字典分组子项-不分页列表查询")
	 @ApiOperation(value="医疗字典分组子项-不分页列表查询", notes="医疗字典分组子项-不分页列表查询")
	 @GetMapping(value = "/listByGroup")
	 public Result<?> listByGroup(MedicalDrugRuleGroupDel medicalDrugRuleGroupDel, MedicalDrugRuleGroup medicalDrugRuleGroup,
								  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								  @RequestParam(name="pageSize", defaultValue="9999999") Integer pageSize,
								  HttpServletRequest req) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		 Page<MedicalDrugRuleGroupDel> page = new Page<MedicalDrugRuleGroupDel>(pageNo, pageSize);
		 return Result.ok(medicalDrugRuleGroupDelService.list(page, medicalDrugRuleGroupDel, medicalDrugRuleGroup));
	 }

	 /**
	  * 通过id查询
	  *
	  * @param kinds
	  * @return
	  */
	 @AutoLog(value = "医疗字典分组子项-通过kinds查询")
	 @ApiOperation(value = "医疗字典分组子项-通过kinds查询", notes = "药品合规规则分组-通过kinds查询")
	 @GetMapping(value = "/queryMapByKinds")
	 public Result<?> queryMapByKinds(@RequestParam(name = "kinds", required = true) String kinds) {
		 Map<String, List<MedicalDrugRuleGroupDelVO>> map = medicalDrugRuleGroupDelService.getMapByKinds(kinds.trim().split(","));
		 return Result.ok(map);
	 }
	/**
	 * 添加
	 *
	 * @param medicalDrugRuleGroupDel
	 * @return
	 */
	@AutoLog(value = "医疗字典分组子项-添加")
	@ApiOperation(value="医疗字典分组子项-添加", notes="医疗字典分组子项-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalDrugRuleGroupDel medicalDrugRuleGroupDel) {
		medicalDrugRuleGroupDelService.save(medicalDrugRuleGroupDel);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalDrugRuleGroupDel
	 * @return
	 */
	@AutoLog(value = "医疗字典分组子项-编辑")
	@ApiOperation(value="医疗字典分组子项-编辑", notes="医疗字典分组子项-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalDrugRuleGroupDel medicalDrugRuleGroupDel) {
		medicalDrugRuleGroupDelService.updateById(medicalDrugRuleGroupDel);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗字典分组子项-通过id删除")
	@ApiOperation(value="医疗字典分组子项-通过id删除", notes="医疗字典分组子项-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDrugRuleGroupDelService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医疗字典分组子项-批量删除")
	@ApiOperation(value="医疗字典分组子项-批量删除", notes="医疗字典分组子项-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDrugRuleGroupDelService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗字典分组子项-通过id查询")
	@ApiOperation(value="医疗字典分组子项-通过id查询", notes="医疗字典分组子项-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDrugRuleGroupDel medicalDrugRuleGroupDel = medicalDrugRuleGroupDelService.getById(id);
		return Result.ok(medicalDrugRuleGroupDel);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDrugRuleGroupDel
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDrugRuleGroupDel medicalDrugRuleGroupDel) {
      return super.exportXls(request, medicalDrugRuleGroupDel, MedicalDrugRuleGroupDel.class, "医疗字典分组子项");
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
      return super.importExcel(request, response, MedicalDrugRuleGroupDel.class);
  }

}
