package com.ai.modules.review.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;

import com.ai.common.utils.IdUtils;
import com.ai.modules.review.entity.MedicalFormalCaseReview;
import com.ai.modules.review.service.IMedicalFormalCaseReviewService;
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
 * @Description: 不合理行为就诊记录审查表
 * @Author: jeecg-boot
 * @Date:   2019-12-26
 * @Version: V1.0
 */
@Slf4j
@Api(tags="不合理行为就诊记录审查表")
@RestController
@RequestMapping("/review/medicalFormalCaseReview")
public class MedicalFormalCaseReviewController extends JeecgController<MedicalFormalCaseReview, IMedicalFormalCaseReviewService> {
	@Autowired
	private IMedicalFormalCaseReviewService medicalFormalCaseReviewService;
	
	/**
	 * 分页列表查询
	 *
	 * @param medicalFormalCaseReview
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查表-分页列表查询")
	@ApiOperation(value="不合理行为就诊记录审查表-分页列表查询", notes="不合理行为就诊记录审查表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalFormalCaseReview medicalFormalCaseReview,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalFormalCaseReview> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCaseReview, req.getParameterMap());
		Page<MedicalFormalCaseReview> page = new Page<MedicalFormalCaseReview>(pageNo, pageSize);
		IPage<MedicalFormalCaseReview> pageList = medicalFormalCaseReviewService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param medicalFormalCaseReview
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查表-添加")
	@ApiOperation(value="不合理行为就诊记录审查表-添加", notes="不合理行为就诊记录审查表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalFormalCaseReview bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setReviewId(IdUtils.uuid());
		bean.setFirReviewUserid(user.getId());
		bean.setFirReviewUsername(user.getUsername());
		bean.setFirReviewTime(new Date());
		bean.setPushUserid(user.getId());
		medicalFormalCaseReviewService.save(bean);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param medicalFormalCaseReview
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查表-编辑")
	@ApiOperation(value="不合理行为就诊记录审查表-编辑", notes="不合理行为就诊记录审查表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalFormalCaseReview medicalFormalCaseReview) {
		medicalFormalCaseReviewService.updateById(medicalFormalCaseReview);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查表-通过id删除")
	@ApiOperation(value="不合理行为就诊记录审查表-通过id删除", notes="不合理行为就诊记录审查表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalFormalCaseReviewService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查表-批量删除")
	@ApiOperation(value="不合理行为就诊记录审查表-批量删除", notes="不合理行为就诊记录审查表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalFormalCaseReviewService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查表-通过id查询")
	@ApiOperation(value="不合理行为就诊记录审查表-通过id查询", notes="不合理行为就诊记录审查表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalFormalCaseReview medicalFormalCaseReview = medicalFormalCaseReviewService.getById(id);
		return Result.ok(medicalFormalCaseReview);
	}
	
	/**
	 * 通过visitId查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合理行为就诊记录审查表-通过visitId查询")
	@ApiOperation(value="不合理行为就诊记录审查表-通过visitId查询", notes="不合理行为就诊记录审查表-通过visitId查询")
	@GetMapping(value = "/queryByVisitId")
	public Result<?> queryByVisitId(@RequestParam(name="visitId",required=true) String visitId) {
		MedicalFormalCaseReview medicalFormalCaseReview = medicalFormalCaseReviewService.getByVisitId(visitId);
		return Result.ok(medicalFormalCaseReview);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalFormalCaseReview
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalFormalCaseReview medicalFormalCaseReview) {
      return super.exportXls(request, medicalFormalCaseReview, MedicalFormalCaseReview.class, "不合理行为就诊记录审查表");
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
      return super.importExcel(request, response, MedicalFormalCaseReview.class);
  }

}
