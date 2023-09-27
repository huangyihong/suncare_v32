package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.modules.config.entity.StdOrgAgreement;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.service.IStdOrgAgreementService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

 /**
 * @Description: 医疗机构医保协议相关参数
 * @Author: jeecg-boot
 * @Date:   2020-12-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医疗机构医保协议相关参数")
@RestController
@RequestMapping("/config/stdOrgAgreement")
public class StdOrgAgreementController extends JeecgController<StdOrgAgreement, IStdOrgAgreementService> {
	@Autowired
	private IStdOrgAgreementService stdOrgAgreementService;
	 @Autowired
	 IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param stdOrgAgreement
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医疗机构医保协议相关参数-分页列表查询")
	@ApiOperation(value="医疗机构医保协议相关参数-分页列表查询", notes="医疗机构医保协议相关参数-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(StdOrgAgreement stdOrgAgreement,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<StdOrgAgreement> queryWrapper = QueryGenerator.initQueryWrapper(stdOrgAgreement, req.getParameterMap());
		Page<StdOrgAgreement> page = new Page<StdOrgAgreement>(pageNo, pageSize);
		IPage<StdOrgAgreement> pageList = stdOrgAgreementService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	  * 添加
	  *
	  * @param stdOrgAgreement
	  * @return
	  */
	 @AutoLog(value = "医疗机构医保协议相关参数-添加")
	 @ApiOperation(value="医疗机构医保协议相关参数-添加", notes="医疗机构医保协议相关参数-添加")
	 @PostMapping(value = "/add")
	 public Result<?> add(@RequestBody StdOrgAgreement stdOrgAgreement) {
		 boolean flag = this.stdOrgAgreementService.isExist(stdOrgAgreement.getOrgid(),stdOrgAgreement.getId(),stdOrgAgreement.getSurancetypecode(),stdOrgAgreement.getStartdate(),stdOrgAgreement.getEnddate());
		 if(flag){
			 return Result.error("新增失败，该医疗机构编码+医疗保险类别代码+适用时间数据已存在！");
		 }
		 stdOrgAgreementService.save(stdOrgAgreement);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param stdOrgAgreement
	 * @return
	 */
	@AutoLog(value = "医疗机构医保协议相关参数-编辑")
	@ApiOperation(value="医疗机构医保协议相关参数-编辑", notes="医疗机构医保协议相关参数-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody StdOrgAgreement stdOrgAgreement) {
		boolean flag = this.stdOrgAgreementService.isExist(stdOrgAgreement.getOrgid(),stdOrgAgreement.getId(),stdOrgAgreement.getSurancetypecode(),stdOrgAgreement.getStartdate(),stdOrgAgreement.getEnddate());
		if(flag){
			return Result.error("修改失败，该医疗机构编码+医疗保险类别代码+适用时间数据已存在！");
		}
		stdOrgAgreementService.updateById(stdOrgAgreement);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗机构医保协议相关参数-通过id删除")
	@ApiOperation(value="医疗机构医保协议相关参数-通过id删除", notes="医疗机构医保协议相关参数-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		stdOrgAgreementService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医疗机构医保协议相关参数-批量删除")
	@ApiOperation(value="医疗机构医保协议相关参数-批量删除", notes="医疗机构医保协议相关参数-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.stdOrgAgreementService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗机构医保协议相关参数-通过id查询")
	@ApiOperation(value="医疗机构医保协议相关参数-通过id查询", notes="医疗机构医保协议相关参数-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		StdOrgAgreement stdOrgAgreement = stdOrgAgreementService.getById(id);
		return Result.ok(stdOrgAgreement);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param stdOrgAgreement
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, StdOrgAgreement stdOrgAgreement) {
      return super.exportXls(request, stdOrgAgreement, StdOrgAgreement.class, "医疗机构医保协议相关参数");
  }

	 /**
	  * 通过excel导入数据
	  *
	  * @param request
	  * @param response
	  * @return
	  */
	 @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	 public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws Exception{
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		 Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		 for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			 MultipartFile file = entity.getValue();// 获取上传文件对象
			 // 判断文件名是否为空
			 if (file == null) {
				 return Result.error("上传文件为空");
			 }
			 // 获取文件名
			 String name = file.getOriginalFilename();
			 // 判断文件大小、即名称
			 long size = file.getSize();
			 if (name == null || ("").equals(name) && size == 0) {
				 return Result.error("上传文件内容为空");
			 }
			 return importTaskService.saveImportTask("STD_ORG_AGREEMENT","医疗机构医保协议相关参数导入",file,user,
					 (f,u)->{
						 try {
							 return this.stdOrgAgreementService.importExcel(f,u);
						 } catch (Exception e) {
							 e.printStackTrace();
							 return Result.error(e.getMessage());
						 }
					 });
		 }
		 return Result.error("上传文件为空");
	 }

	 /**
	  * 直接导出excel
	  *
	  * @param req
	  * @param response
	  * @param bean
	  * @throws Exception
	  */
	 @RequestMapping(value = "/exportExcel")
	 public void exportExcel(HttpServletRequest req, HttpServletResponse response, StdOrgAgreement bean) throws Exception {
		 Result<?> result = new Result<>();
		 String title = req.getParameter("title");
		 if (StringUtils.isBlank(title)) {
			 title = "医疗机构医保协议相关参数_导出";
		 }
		 //response.reset();
		 response.setContentType("application/octet-stream; charset=utf-8");
		 response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
		 try {
			 OutputStream os = response.getOutputStream();
			 // 选中数据
			 String selections = req.getParameter("selections");
			 if (StringUtils.isNotEmpty(selections)) {
				 bean.setId(selections);
			 }
			 QueryWrapper<StdOrgAgreement> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());
			 List<StdOrgAgreement> list = stdOrgAgreementService.list(queryWrapper);
			 String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
			 stdOrgAgreementService.exportExcel(list, os, suffix);
		 } catch (Exception e) {
			 throw e;
		 }
	 }

}
