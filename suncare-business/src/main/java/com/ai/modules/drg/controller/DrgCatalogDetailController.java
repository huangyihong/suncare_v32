package com.ai.modules.drg.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExportXUtils;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgCatalogDetail;
import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.drg.service.IDrgCatalogDetailService;
import com.ai.modules.drg.service.IDrgRuleLimitesService;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Description: DRG分组目录数据详细表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
@Slf4j
@Api(tags="DRG分组目录数据详细表")
@RestController
@RequestMapping("/drg/drgCatalogDetail")
public class DrgCatalogDetailController extends JeecgController<DrgCatalogDetail, IDrgCatalogDetailService> {
	@Autowired
	private IDrgCatalogDetailService drgCatalogDetailService;
	@Autowired
    private IMedicalDictService medicalDictService;


	/**
	 * 分页列表查询
	 *
	 * @param drgCatalogDetail
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "DRG分组目录数据详细表-分页列表查询")
	@ApiOperation(value="DRG分组目录数据详细表-分页列表查询", notes="DRG分组目录数据详细表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(DrgCatalogDetail drgCatalogDetail,String versionCode,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<DrgCatalogDetail> queryWrapper = QueryGenerator.initQueryWrapper(drgCatalogDetail, req.getParameterMap());
		String catalogType = drgCatalogDetail.getCatalogType();

		if(StringUtils.isNotBlank(catalogType)&&StringUtils.isNotBlank(versionCode)){
			//所属目录版本code
			queryWrapper.inSql("CATALOG_ID","select id from drg_catalog where catalog_type='"+catalogType+"' and version_code='"+versionCode+"'");
		}
		Page<DrgCatalogDetail> page = new Page<DrgCatalogDetail>(pageNo, pageSize);
		IPage<DrgCatalogDetail> pageList = drgCatalogDetailService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param drgCatalogDetail
	 * @return
	 */
	@AutoLog(value = "DRG分组目录数据详细表-添加")
	@ApiOperation(value="DRG分组目录数据详细表-添加", notes="DRG分组目录数据详细表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody DrgCatalogDetail drgCatalogDetail) {
		//drg不限制编码重复
		if(!drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.DRG_V)){
			if(drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.CONDITION_V)&&StringUtils.isNotBlank(drgCatalogDetail.getName())&&this.isExistName(drgCatalogDetail)){
				return Result.error("分组条件名称重复！");
			}else if(StringUtils.isNotBlank(drgCatalogDetail.getCode())&&this.isExistName(drgCatalogDetail)){
				if(drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.MDC_INFO_V)){
					return Result.error("目录下相同MDC目录“"+drgCatalogDetail.getMdcCatalogCode()+"”下编码重复！");
				}
				return Result.error("目录下编码重复！");
			}
		}
		drgCatalogDetailService.saveBean(drgCatalogDetail);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param drgCatalogDetail
	 * @return
	 */
	@AutoLog(value = "DRG分组目录数据详细表-编辑")
	@ApiOperation(value="DRG分组目录数据详细表-编辑", notes="DRG分组目录数据详细表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody DrgCatalogDetail drgCatalogDetail) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		//drg不限制编码重复
		if(!drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.DRG_V)) {
			if (drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.CONDITION_V) && StringUtils.isNotBlank(drgCatalogDetail.getName()) && this.isExistName(drgCatalogDetail)) {
				return Result.error("分组条件名称重复！");
			} else if (StringUtils.isNotBlank(drgCatalogDetail.getCode()) && this.isExistName(drgCatalogDetail)) {
				if (drgCatalogDetail.getCatalogType().equals(DrgCatalogConstants.MDC_INFO_V)) {
					return Result.error("目录下相同MDC目录“" + drgCatalogDetail.getMdcCatalogCode() + "”下编码重复！");
				}
				return Result.error("目录下编码重复！");
			}
		}
		drgCatalogDetail.setUpdateUser(user.getUsername());
		drgCatalogDetail.setUpdateUsername(user.getRealname());
		drgCatalogDetail.setUpdateTime(new Date());
		drgCatalogDetailService.updateBean(drgCatalogDetail);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过catalogId删除
	 *
	 * @param catalogId
	 * @return
	 */
	@AutoLog(value = "DRG分组目录数据详细表-通过catalogId删除")
	@ApiOperation(value="DRG分组目录数据详细表-通过catalogId删除", notes="DRG分组目录数据详细表-通过catalogId删除")
	@DeleteMapping(value = "/deleteByCatalogId")
	public Result<?> deleteByCatalogId(@RequestParam(name="catalogId",required=true) String catalogId) {
		return drgCatalogDetailService.deleteByCatalogId(catalogId);
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DRG分组目录数据详细表-通过id删除")
	@ApiOperation(value="DRG分组目录数据详细表-通过id删除", notes="DRG分组目录数据详细表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		return drgCatalogDetailService.delete(id);
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DRG分组目录数据详细表-批量删除")
	@ApiOperation(value="DRG分组目录数据详细表-批量删除", notes="DRG分组目录数据详细表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		return drgCatalogDetailService.deleteBatch(ids);
	}

	private boolean isExistName(DrgCatalogDetail drgCatalogDetail){
		String code = drgCatalogDetail.getCode();
		String name = drgCatalogDetail.getName();
		String catalogType = drgCatalogDetail.getCatalogType();
		String catalogId = drgCatalogDetail.getCatalogId();
		String id = drgCatalogDetail.getId();
		String mdcCatalogCode = drgCatalogDetail.getMdcCatalogCode();
		QueryWrapper<DrgCatalogDetail> queryWrapper = new QueryWrapper<DrgCatalogDetail>();
		if(catalogType.equals(DrgCatalogConstants.CONDITION_V)){
			queryWrapper.eq("NAME", name);
		}else{
			queryWrapper.eq("CODE", code);
		}
		queryWrapper.eq("CATALOG_TYPE", catalogType);
		if(StringUtils.isNotBlank(catalogId)){
			queryWrapper.eq("CATALOG_ID", catalogId);
		}
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		if(catalogType.equals(DrgCatalogConstants.MDC_INFO_V)){
			//code+mdcCatalogCode
			if(StringUtils.isNotBlank(mdcCatalogCode)){
				queryWrapper.eq("MDC_CATALOG_CODE", mdcCatalogCode);
			}
		}
		List<DrgCatalogDetail> list = this.drgCatalogDetailService.list(queryWrapper);
		if(list.size()>0){
			return true;
		}
		return false;
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DRG分组目录数据详细表-通过id查询")
	@ApiOperation(value="DRG分组目录数据详细表-通过id查询", notes="DRG分组目录数据详细表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		DrgCatalogDetail drgCatalogDetail = drgCatalogDetailService.getById(id);
		return Result.ok(drgCatalogDetail);
	}

	/**
	 * 根据mdcCatalogV查询数据
	 *
	 * @param mdcCatalogV
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "查询树形数据")
	@ApiOperation(value="查询树形数据", notes="查询树形数据")
	@GetMapping(value = "/getTreeList")
	public Result<?> getTreeList(String mdcCatalogV,String adrgCatalogV,String mdcCatalogCode) throws Exception {
		if(StringUtils.isBlank(mdcCatalogV)&&(StringUtils.isBlank(adrgCatalogV)||StringUtils.isBlank(mdcCatalogCode))) {
			return Result.error("参数异常");
		}
		if(StringUtils.isBlank(adrgCatalogV)){
			List<DrgCatalogDetail> list = this.drgCatalogDetailService.list(new QueryWrapper<DrgCatalogDetail>()
					.inSql("CATALOG_ID","select id from drg_catalog where catalog_type='MDC_V' and version_code='"+mdcCatalogV+"'")
					.eq("EXAMINE_STATUS","1").orderByAsc("code")
			);
			return Result.ok(list);
		}else{
			List<DrgCatalogDetail> list = this.drgCatalogDetailService.list(new QueryWrapper<DrgCatalogDetail>().eq("MDC_CATALOG_CODE",mdcCatalogCode)
					.inSql("CATALOG_ID","select id from drg_catalog where catalog_type='ADRG_V' and version_code='"+adrgCatalogV+"'")
					.eq("EXAMINE_STATUS","1").orderByAsc("code")
			);
			return Result.ok(list);
		}

	}

  /**
   * 导出excel
   *
   * @param req
   * @param drgCatalogDetail
   */
  @RequestMapping(value = "/exportXls")
  public void exportXls(HttpServletRequest req, HttpServletResponse response, DrgCatalogDetail drgCatalogDetail)throws Exception{
	  // 选中数据
	  String selections = req.getParameter("selections");
	  if (StringUtils.isNotEmpty(selections)) {
		  drgCatalogDetail.setId(selections);
	  }
	  QueryWrapper<DrgCatalogDetail> queryWrapper = QueryGenerator.initQueryWrapper(drgCatalogDetail, req.getParameterMap());
	  String catalogType = req.getParameter("catalogType");
	  DrgCatalogConstants.CatalogTypeInfo typeInfo = DrgCatalogConstants.CATALOG_TYPE_MAP.get(catalogType);
	  if(typeInfo==null){
		  throw new Exception("参数异常！");
	  }
	  String title = typeInfo.getFileName() + System.currentTimeMillis();
	  String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
	  //response.reset();
	  response.setContentType("application/octet-stream; charset=utf-8");
	  response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "." + suffix).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

	  OutputStream os = response.getOutputStream();
	  SXSSFWorkbook workbook = new SXSSFWorkbook();
	  List<DrgCatalogDetail> exportList = this.drgCatalogDetailService.list(queryWrapper);
	  ExportXUtils.exportExl(exportList, DrgCatalogDetail.class, typeInfo.getTitleArr(), typeInfo.getFieldArr(), workbook, typeInfo.getSheefName());
	  workbook.write(os);
	  workbook.dispose();
  }

	/**
	 * 动态获取导入模板
	 *
	 * @param req
	 * @param drgCatalogDetail
	 */
	@RequestMapping(value = "/importTemplate")
	public void importTemplate(HttpServletRequest req, HttpServletResponse response, DrgCatalogDetail drgCatalogDetail)throws Exception{
		QueryWrapper<DrgCatalogDetail> queryWrapper = QueryGenerator.initQueryWrapper(drgCatalogDetail, req.getParameterMap());
		String catalogType = req.getParameter("catalogType");
		DrgCatalogConstants.CatalogTypeInfo typeInfo = DrgCatalogConstants.CATALOG_TYPE_MAP.get(catalogType);
		if(typeInfo==null){
			throw new Exception("参数异常！");
		}
		String title = typeInfo.getFileName() + System.currentTimeMillis();
		String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		//response.reset();
		response.setContentType("application/octet-stream; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "." + suffix).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

		OutputStream os = response.getOutputStream();
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		queryWrapper.last("limit 10");
		List<DrgCatalogDetail> exportList = this.drgCatalogDetailService.list(queryWrapper);
		ExportXUtils.exportExl(exportList, DrgCatalogDetail.class, Arrays.copyOf(typeInfo.getTitleArr(), typeInfo.getTitleArr().length - 1),Arrays.copyOf(typeInfo.getFieldArr(), typeInfo.getFieldArr().length - 1), workbook, typeInfo.getSheefName());
		workbook.write(os);
		workbook.dispose();
	}

  /**
   * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
		  String catalogType = request.getParameter("catalogType");
		  String catalogId = request.getParameter("catalogId");
		  Map<String, String> yesnoDict = medicalDictService.queryNameMapByType("YESNO");
		  Map<String, String> havingornoDict = medicalDictService.queryNameMapByType("HAVINGORNO");
		  Map<String,Map<String,String>> dictMap = new HashMap<>();
		  dictMap.put("YESNO",yesnoDict);
		  dictMap.put("HAVINGORNO",havingornoDict);
		 return this.drgCatalogDetailService.importExcel(file,user,catalogType,catalogId,dictMap);
	  }
	  return Result.error("上传文件为空");
  }

	/**
	 * 批量审核
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DRG分组目录数据详细表-批量审核")
	@ApiOperation(value = "DRG分组目录数据详细表-批量审核", notes = "DRG分组目录数据详细表-批量审核")
	@GetMapping(value = "/batchExamine")
	public Result<?> batchExamine(String ids,
								  @RequestParam(name = "status", required = true) String status,
								  DrgCatalogDetail drgCatalogDetail,
								  HttpServletRequest req) {
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		List<DrgCatalogDetail> list = new ArrayList<>();
		if(StringUtils.isNotBlank(ids)){
			//选择指定数据审核
			List<String> idsArr = Arrays.asList(ids.split(","));
			for(String id:idsArr){
				DrgCatalogDetail bean = new DrgCatalogDetail();
				bean.setId(id);
				list.add(bean);
			}
		}else{
			QueryWrapper<DrgCatalogDetail> queryWrapper = QueryGenerator.initQueryWrapper(drgCatalogDetail, req.getParameterMap());
			queryWrapper.ne("EXAMINE_STATUS",status);
			list = this.drgCatalogDetailService.list(queryWrapper);
		}
		if(list.size()==0){
			return Result.error("没有需要审核的数据");
		}
		list.forEach(bean->{
			bean.setExamineStatus(status);
			bean.setExamineUser(loginUser.getUsername());
			bean.setExamineUsername(loginUser.getRealname());
			bean.setExamineTime(new Date());
		});

		drgCatalogDetailService.updateBatchById(list);

		return Result.ok("批量审核成功！");
	}



}
