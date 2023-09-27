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
import com.ai.modules.probe.entity.MedicalProbeFlow;
import com.ai.modules.probe.service.IMedicalProbeFlowService;
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
@RequestMapping("/probe/medicalProbeFlow")
public class MedicalProbeFlowController extends JeecgController<MedicalProbeFlow, IMedicalProbeFlowService> {
	@Autowired
	private IMedicalProbeFlowService medicalProbeFlowService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalProbeFlow
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "流程图树-分页列表查询")
	@ApiOperation(value="流程图树-分页列表查询", notes="流程图树-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalProbeFlow medicalProbeFlow,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalProbeFlow> queryWrapper = QueryGenerator.initQueryWrapper(medicalProbeFlow, req.getParameterMap());
		Page<MedicalProbeFlow> page = new Page<MedicalProbeFlow>(pageNo, pageSize);
		IPage<MedicalProbeFlow> pageList = medicalProbeFlowService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param medicalProbeFlow
	 * @return
	 */
	@AutoLog(value = "流程图树-添加")
	@ApiOperation(value="流程图树-添加", notes="流程图树-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalProbeFlow medicalProbeFlow) {
		medicalProbeFlowService.save(medicalProbeFlow);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param medicalProbeFlow
	 * @return
	 */
	@AutoLog(value = "流程图树-编辑")
	@ApiOperation(value="流程图树-编辑", notes="流程图树-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalProbeFlow medicalProbeFlow) {
		medicalProbeFlowService.updateById(medicalProbeFlow);
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
		medicalProbeFlowService.removeById(id);
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
		this.medicalProbeFlowService.removeByIds(Arrays.asList(ids.split(",")));
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
		MedicalProbeFlow medicalProbeFlow = medicalProbeFlowService.getById(id);
		return Result.ok(medicalProbeFlow);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalProbeFlow
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalProbeFlow medicalProbeFlow) {
      return super.exportXls(request, medicalProbeFlow, MedicalProbeFlow.class, "流程图树");
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
      return super.importExcel(request, response, MedicalProbeFlow.class);
  }

}
