package com.ai.modules.medical.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.medical.entity.MedicalRuleRely;
import com.ai.modules.medical.service.IMedicalColumnQualityService;
import com.ai.modules.medical.service.IMedicalRuleRelyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Arrays;

 /**
 * @Description: 规则依赖字段表
 * @Author: jeecg-boot
 * @Date:   2022-01-20
 * @Version: V1.0
 */
@Slf4j
@Api(tags="规则依赖字段表")
@RestController
@RequestMapping("/medical/medicalRuleRely")
public class MedicalRuleRelyController extends JeecgController<MedicalRuleRely, IMedicalRuleRelyService> {
	@Autowired
	private IMedicalRuleRelyService medicalRuleRelyService;

	@Autowired
	private IMedicalColumnQualityService medicalColumnQualityService;
	public boolean computeFlag=false;//更新标志

	/**
	 * 分页列表查询
	 *
	 * @param medicalRuleRely
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-分页列表查询")
	@ApiOperation(value="规则依赖字段表-分页列表查询", notes="规则依赖字段表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalRuleRely medicalRuleRely,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalRuleRely> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleRely, req.getParameterMap());
		Page<MedicalRuleRely> page = new Page<MedicalRuleRely>(pageNo, pageSize);
		IPage<MedicalRuleRely> pageList = medicalRuleRelyService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalRuleRely
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-添加")
	@ApiOperation(value="规则依赖字段表-添加", notes="规则依赖字段表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalRuleRely medicalRuleRely) {
		medicalRuleRelyService.save(medicalRuleRely);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalRuleRely
	 * @return
	 */
	@AutoLog(value = "规则依赖字段表-编辑")
	@ApiOperation(value="规则依赖字段表-编辑", notes="规则依赖字段表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalRuleRely medicalRuleRely) {
		medicalRuleRelyService.updateById(medicalRuleRely);
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
		medicalRuleRelyService.removeById(id);
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
		this.medicalRuleRelyService.removeByIds(Arrays.asList(ids.split(",")));
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
		MedicalRuleRely medicalRuleRely = medicalRuleRelyService.getById(id);
		return Result.ok(medicalRuleRely);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalRuleRely
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalRuleRely medicalRuleRely) {
      return super.exportXls(request, medicalRuleRely, MedicalRuleRely.class, "规则依赖字段表");
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
      return super.importExcel(request, response, MedicalRuleRely.class);
  }

	 /**
	  * 直接导出excel
	  *
	  * @param req
	  * @param response
	  * @param medicalRuleRely
	  * @throws Exception
	  */
	 @RequestMapping(value = "/exportExcel")
	 public void exportExcel(HttpServletRequest req, HttpServletResponse response, MedicalRuleRely medicalRuleRely) throws Exception {
		 Result<?> result = new Result<>();
		 String title = req.getParameter("title");
		 if (StringUtils.isBlank(title)) {
			 title = "规则依赖字段信息_导出";
		 }
		 //response.reset();
		 response.setContentType("application/octet-stream; charset=utf-8");
		 response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
		 try {
			 OutputStream os = response.getOutputStream();
			 // 选中数据
			 String selections = req.getParameter("selections");
			 if (StringUtils.isNotEmpty(selections)) {
				 medicalRuleRely.setId(selections);
			 }
			 QueryWrapper<MedicalRuleRely> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleRely, req.getParameterMap());
			 String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
			 medicalRuleRelyService.exportExcel(queryWrapper, os, suffix);
		 } catch (Exception e) {
			 throw e;
		 }
	 }

	 /**
	  * 导出excel
	  *
	  * @param req
	  * @param medicalRuleRely
	  * @throws Exception
	  */
	 @AutoLog(value = "线程导出excel")
	 @ApiOperation(value="线程导出excel", notes="线程导出excel")
	 @RequestMapping(value = "/exportExcelByThread")
	 public Result<?> exportExcelByThread(HttpServletRequest req, MedicalRuleRely medicalRuleRely) throws Exception {
		 Result<?> result = new Result<>();
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 String title = req.getParameter("title");
		 if(org.apache.commons.lang3.StringUtils.isBlank(title)) {
			 title = "规则依赖字段信息_导出";
		 }
		 String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		 QueryWrapper<MedicalRuleRely> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleRely, req.getParameterMap());
		 int count = medicalRuleRelyService.count(queryWrapper);
		 ThreadUtils.EXPORT_POOL.add(title,suffix, count, (os)->{
			 Result exportResult = Result.ok();
			 try {
				 this.medicalRuleRelyService.exportExcel(queryWrapper,os,suffix);
			 } catch (Exception e) {
				 e.printStackTrace();
				 exportResult = Result.error(e.getMessage());
			 } finally {

			 }
			 return exportResult;
		 });

		 result.setMessage("等待导出，请在导出记录界面查看进度");
		 return result;
	 }


	 /**
	  * 更新计算
	  * @return
	  */
	 @AutoLog(value = "规则依赖字段表-更新计算")
	 @ApiOperation(value="规则依赖字段表-更新计算", notes="规则依赖字段表-更新计算")
	 @GetMapping(value = "/compute")
	 public Result<?> compute()throws Exception{
		 if(this.computeFlag){
			return Result.error("正在更新，请稍后进行操作...");
		 }
		 this.computeFlag = true;
		 medicalColumnQualityService.computeMedicalColumnQualityVO();
         this.computeFlag = false;
		 return Result.ok(" 更新成功");
	 }

}
