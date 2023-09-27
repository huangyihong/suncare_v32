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
import com.ai.modules.medical.entity.MedicalFlowTrial;
import com.ai.modules.medical.service.IMedicalFlowTrialService;
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
 * @Description: 风控模型流程节点试算表
 * @Author: jeecg-boot
 * @Date:   2021-08-26
 * @Version: V1.0
 */
@Slf4j
@Api(tags="风控模型流程节点试算表")
@RestController
@RequestMapping("/medical/medicalFlowTrial")
public class MedicalFlowTrialController extends JeecgController<MedicalFlowTrial, IMedicalFlowTrialService> {
	@Autowired
	private IMedicalFlowTrialService medicalFlowTrialService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalFlowTrial
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "风控模型流程节点试算表-分页列表查询")
	@ApiOperation(value="风控模型流程节点试算表-分页列表查询", notes="风控模型流程节点试算表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalFlowTrial medicalFlowTrial,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalFlowTrial> queryWrapper = QueryGenerator.initQueryWrapper(medicalFlowTrial, req.getParameterMap());
		Page<MedicalFlowTrial> page = new Page<MedicalFlowTrial>(pageNo, pageSize);
		IPage<MedicalFlowTrial> pageList = medicalFlowTrialService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param medicalFlowTrial
	 * @return
	 */
	@AutoLog(value = "风控模型流程节点试算表-添加")
	@ApiOperation(value="风控模型流程节点试算表-添加", notes="风控模型流程节点试算表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalFlowTrial medicalFlowTrial) {
		medicalFlowTrialService.save(medicalFlowTrial);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param medicalFlowTrial
	 * @return
	 */
	@AutoLog(value = "风控模型流程节点试算表-编辑")
	@ApiOperation(value="风控模型流程节点试算表-编辑", notes="风控模型流程节点试算表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalFlowTrial medicalFlowTrial) {
		medicalFlowTrialService.updateById(medicalFlowTrial);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "风控模型流程节点试算表-通过id删除")
	@ApiOperation(value="风控模型流程节点试算表-通过id删除", notes="风控模型流程节点试算表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalFlowTrialService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "风控模型流程节点试算表-批量删除")
	@ApiOperation(value="风控模型流程节点试算表-批量删除", notes="风控模型流程节点试算表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalFlowTrialService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "风控模型流程节点试算表-通过id查询")
	@ApiOperation(value="风控模型流程节点试算表-通过id查询", notes="风控模型流程节点试算表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalFlowTrial medicalFlowTrial = medicalFlowTrialService.getById(id);
		return Result.ok(medicalFlowTrial);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalFlowTrial
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalFlowTrial medicalFlowTrial) {
      return super.exportXls(request, medicalFlowTrial, MedicalFlowTrial.class, "风控模型流程节点试算表");
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
      return super.importExcel(request, response, MedicalFlowTrial.class);
  }

}
