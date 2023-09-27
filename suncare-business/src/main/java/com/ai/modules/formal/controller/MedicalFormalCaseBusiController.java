package com.ai.modules.formal.controller;

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
import com.ai.modules.formal.entity.MedicalFormalCaseBusi;
import com.ai.modules.formal.service.IMedicalFormalCaseBusiService;
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
 * @Description: 业务组关联表
 * @Author: jeecg-boot
 * @Date:   2019-11-28
 * @Version: V1.0
 */
@Slf4j
@Api(tags="业务组关联表")
@RestController
@RequestMapping("/formal/medicalFormalCaseBusi")
public class MedicalFormalCaseBusiController extends JeecgController<MedicalFormalCaseBusi, IMedicalFormalCaseBusiService> {
	@Autowired
	private IMedicalFormalCaseBusiService medicalFormalCaseBusiService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalFormalCaseBusi
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "业务组关联表-分页列表查询")
	@ApiOperation(value="业务组关联表-分页列表查询", notes="业务组关联表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalFormalCaseBusi medicalFormalCaseBusi,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalFormalCaseBusi> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCaseBusi, req.getParameterMap());
		Page<MedicalFormalCaseBusi> page = new Page<MedicalFormalCaseBusi>(pageNo, pageSize);
		IPage<MedicalFormalCaseBusi> pageList = medicalFormalCaseBusiService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param medicalFormalCaseBusi
	 * @return
	 */
	@AutoLog(value = "业务组关联表-添加")
	@ApiOperation(value="业务组关联表-添加", notes="业务组关联表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalFormalCaseBusi medicalFormalCaseBusi) {
		medicalFormalCaseBusiService.save(medicalFormalCaseBusi);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param medicalFormalCaseBusi
	 * @return
	 */
	@AutoLog(value = "业务组关联表-编辑")
	@ApiOperation(value="业务组关联表-编辑", notes="业务组关联表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalFormalCaseBusi medicalFormalCaseBusi) {
		medicalFormalCaseBusiService.updateById(medicalFormalCaseBusi);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务组关联表-通过id删除")
	@ApiOperation(value="业务组关联表-通过id删除", notes="业务组关联表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalFormalCaseBusiService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 通过caseId和busiId删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务组关联表-通过caseId和busiId删除")
	@ApiOperation(value="业务组关联表-通过caseId和busiId删除", notes="业务组关联表-通过caseId和busiId删除")
	@DeleteMapping(value = "/deleteByCaseId")
	public Result<?> deleteByCaseId(@RequestParam(name="caseId",required=true) String caseId,@RequestParam(name="busiId",required=true) String busiId) {
		medicalFormalCaseBusiService.remove(new QueryWrapper<MedicalFormalCaseBusi>().eq("CASE_ID",caseId).eq("BUSI_ID", busiId));
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "业务组关联表-批量删除")
	@ApiOperation(value="业务组关联表-批量删除", notes="业务组关联表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalFormalCaseBusiService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务组关联表-通过id查询")
	@ApiOperation(value="业务组关联表-通过id查询", notes="业务组关联表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalFormalCaseBusi medicalFormalCaseBusi = medicalFormalCaseBusiService.getById(id);
		return Result.ok(medicalFormalCaseBusi);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalFormalCaseBusi
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalFormalCaseBusi medicalFormalCaseBusi) {
      return super.exportXls(request, medicalFormalCaseBusi, MedicalFormalCaseBusi.class, "业务组关联表");
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
      return super.importExcel(request, response, MedicalFormalCaseBusi.class);
  }

}
