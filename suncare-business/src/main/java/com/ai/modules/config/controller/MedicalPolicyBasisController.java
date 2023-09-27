package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.modules.config.entity.MedicalPolicyBasis;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.service.IMedicalPolicyBasisService;
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
import org.jeecg.common.system.util.CommonUtil;
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
 * @Description: 政策法规
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
@Slf4j
@Api(tags="政策法规")
@RestController
@RequestMapping("/config/medicalPolicyBasis")
public class MedicalPolicyBasisController extends JeecgController<MedicalPolicyBasis, IMedicalPolicyBasisService> {
	@Autowired
	private IMedicalPolicyBasisService medicalPolicyBasisService;
	 @Autowired
	 IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalPolicyBasis
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "政策法规-分页列表查询")
	@ApiOperation(value="政策法规-分页列表查询", notes="政策法规-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalPolicyBasis medicalPolicyBasis,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalPolicyBasis> queryWrapper = QueryGenerator.initQueryWrapper(medicalPolicyBasis, req.getParameterMap());
		Page<MedicalPolicyBasis> page = new Page<MedicalPolicyBasis>(pageNo, pageSize);
		IPage<MedicalPolicyBasis> pageList = medicalPolicyBasisService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalPolicyBasis
	 * @return
	 */
	@AutoLog(value = "政策法规-添加")
	@ApiOperation(value="政策法规-添加", notes="政策法规-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalPolicyBasis medicalPolicyBasis) {
		boolean flag = this.medicalPolicyBasisService.isExistName(medicalPolicyBasis.getName(),medicalPolicyBasis.getId());
        if(flag){
            return Result.error("新增失败，该政策法规的名称已存在！");
        }
		medicalPolicyBasisService.save(medicalPolicyBasis);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalPolicyBasis
	 * @return
	 */
	@AutoLog(value = "政策法规-编辑")
	@ApiOperation(value="政策法规-编辑", notes="政策法规-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalPolicyBasis medicalPolicyBasis) {
		boolean flag = this.medicalPolicyBasisService.isExistName(medicalPolicyBasis.getName(),medicalPolicyBasis.getId());
        if(flag){
            return Result.error("修改失败，该政策法规的名称已存在！");
        }
		medicalPolicyBasisService.updateById(medicalPolicyBasis);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "政策法规-通过id删除")
	@ApiOperation(value="政策法规-通过id删除", notes="政策法规-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalPolicyBasisService.deleteById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "政策法规-批量删除")
	@ApiOperation(value="政策法规-批量删除", notes="政策法规-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalPolicyBasisService.deleteByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "政策法规-通过id查询")
	@ApiOperation(value="政策法规-通过id查询", notes="政策法规-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalPolicyBasis medicalPolicyBasis = medicalPolicyBasisService.getById(id);
		return Result.ok(medicalPolicyBasis);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalPolicyBasis
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalPolicyBasis medicalPolicyBasis) {
      return super.exportXls(request, medicalPolicyBasis, MedicalPolicyBasis.class, "政策法规");
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
			 return importTaskService.saveImportTask("MEDICAL_POLICY_BASIS","政策法规导入",file,user,
					 (f,u)->{
						 try {
							 return this.medicalPolicyBasisService.importExcel(f,u);
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
	  * @param medicalPolicyBasis
	  * @throws Exception
	  */
	 @RequestMapping(value = "/exportExcel")
	 public void exportExcel(HttpServletRequest req, HttpServletResponse response, MedicalPolicyBasis medicalPolicyBasis) throws Exception {
		 Result<?> result = new Result<>();
		 String title = req.getParameter("title");
		 if (StringUtils.isBlank(title)) {
			 title = "政策法规_导出";
		 }
		 //response.reset();
		 response.setContentType("application/octet-stream; charset=utf-8");
		 response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
		 try {
			 OutputStream os = response.getOutputStream();
			 // 选中数据
			 String selections = req.getParameter("selections");
			 if (StringUtils.isNotEmpty(selections)) {
				 medicalPolicyBasis.setId(selections);
			 }
			 QueryWrapper<MedicalPolicyBasis> queryWrapper = QueryGenerator.initQueryWrapper(medicalPolicyBasis, req.getParameterMap());
			 List<MedicalPolicyBasis> list = medicalPolicyBasisService.list(queryWrapper);
			 String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
			 medicalPolicyBasisService.exportExcel(list, os, suffix);
		 } catch (Exception e) {
			 throw e;
		 }
	 }

	 /**
	  * 批量导入附件
	  *
	  * @param file
	  * @param response
	  * @return
	  */
	 @RequestMapping(value = "/importFiles", method = RequestMethod.POST)
	 public Result<?> importFiles(@RequestParam("file") MultipartFile file,HttpServletRequest req, HttpServletResponse response) {

		 // 获取文件名
		 String fileName = file.getOriginalFilename();
		 try {
			 int index = fileName.indexOf("_");
			 if (index < 0) {
				 throw new Exception("文件名缺少下划线");
			 }
			 String name = fileName.substring(0,index);
			 MedicalPolicyBasis bean = this.medicalPolicyBasisService.getBeanByName(name);
			 if(bean == null){
				 throw new Exception("政策法规名称不存在：" + name);
			 }
			 // 校验文件名是否重复
			 if(org.apache.commons.lang3.StringUtils.isNotBlank(bean.getFilenames())){
				 String[] filePaths = bean.getFilenames().split(",");
				 for(String path: filePaths){
					 // 去掉时间戳
					 if(fileName.equals(CommonUtil.pathToFileName(path))){
						 throw new Exception("文件已存在");
					 }
				 }
			 }

			 String path = CommonUtil.upload(file,req.getParameter("bizPath"));
			 if(org.apache.commons.lang3.StringUtils.isBlank(bean.getFilenames())){
				 bean.setFilenames(path);
			 } else {
				 bean.setFilenames(bean.getFilenames() + ","+path);

			 }

			 this.medicalPolicyBasisService.updateById(bean);

			 return Result.ok(path);
		 } catch (Exception e) {
			 return Result.error(e.getMessage());
		 }
	 }

}
