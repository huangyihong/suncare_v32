package com.ai.modules.ybChargeSearch.controller;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrug;
import com.ai.modules.ybChargeSearch.handle.rule.DcRuleHandleUtil;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleRegexResult;
import com.ai.modules.ybChargeSearch.vo.YbChargeDrugRuleVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.ai.modules.ybChargeSearch.service.IYbChargeDrugRuleService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static com.ai.modules.ybChargeSearch.controller.YbChargeSearchTaskController.initTitleStyle;

/**
 * @Description: 药品规则库
 * @Author: jeecg-boot
 * @Date:   2023-02-14
 * @Version: V1.0
 */
@Slf4j
@Api(tags="药品规则库")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeDrugRule")
public class YbChargeDrugRuleController extends JeecgController<YbChargeDrugRule, IYbChargeDrugRuleService> {
	@Autowired
	private IYbChargeDrugRuleService ybChargeDrugRuleService;
	 @Autowired
	 private ISysBaseAPI sysBaseAPI;
	@Autowired
	private IMedicalDictService medicalDictService;
	@Value(value = "${jeecg.path.upload}")
	private String uploadPath;


	/**
	 * 分页列表查询
	 *
	 * @param ybChargeDrugRule
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品规则库-分页列表查询")
	@ApiOperation(value="药品规则库-分页列表查询", notes="药品规则库-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeDrugRule ybChargeDrugRule,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		String drugName = ybChargeDrugRule.getDrugName();
		QueryWrapper<YbChargeDrugRule> queryWrapper = new QueryWrapper<>();
		if(StrUtil.isNotEmpty(drugName) && drugName.contains("#")){
			drugName=drugName.trim();
			ybChargeDrugRule.setDrugName("");
			queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDrugRule, req.getParameterMap());
			List<String> itemNames = Arrays.asList(drugName.split("#"));
			queryWrapper.in("drug_name",itemNames);
		}else if(StrUtil.isNotEmpty(drugName)){
			ybChargeDrugRule.setDrugName("");
			queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDrugRule, req.getParameterMap());
			queryWrapper.like("drug_name",drugName);
		}else{
			queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDrugRule, req.getParameterMap());
		}


		Page<YbChargeDrugRule> page = new Page<YbChargeDrugRule>(pageNo, pageSize);
		IPage<YbChargeDrugRule> pageList = ybChargeDrugRuleService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 药品规则库-相关查询
	 *
	 * @param ybChargeDrugRuleVo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品规则库-相关查询列表")
	@ApiOperation(value = "药品规则库-相关查询列表", notes = "药品规则库-相关查询列表")
	@GetMapping(value = "/drugRuleImportList")
	public Result<?> drugRuleImportList(YbChargeDrugRuleVo ybChargeDrugRuleVo,
									@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
									@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
									HttpServletRequest req) throws Exception {
		Page<YbChargeDrugRuleVo> page = new Page<YbChargeDrugRuleVo>(pageNo, pageSize);
		return Result.ok(ybChargeDrugRuleService.drugRuleImportList(ybChargeDrugRuleVo,page,req));
	}


	/**
	 * 添加
	 *
	 * @param ybChargeDrugRule
	 * @return
	 */
	@AutoLog(value = "药品规则库-添加")
	@ApiOperation(value="药品规则库-添加", notes="药品规则库-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeDrugRule ybChargeDrugRule) throws Exception {
		String limitType = ybChargeDrugRule.getLimitType();
		String limitContent = ybChargeDrugRule.getLimitContent();
		String drugName = ybChargeDrugRule.getDrugName();
		if(StrUtil.isEmpty(limitType) || StrUtil.isEmpty(limitContent) || StrUtil.isEmpty(drugName)){
			throw new Exception("限制规则或限制内容或药品名称不能为空!");
		}
		//验证规则限制内容是否满足要求
		if(StrUtil.isNotEmpty(limitType) && StrUtil.isNotEmpty(limitContent)){
			RuleRegexResult ruleRegexResult = DcRuleHandleUtil.validRuleRegex(limitType, limitContent);
			if(!ruleRegexResult.isSuccess()){
				throw new Exception(ruleRegexResult.getMessage());
			}
		}

		ybChargeDrugRuleService.save(ybChargeDrugRule);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param ybChargeDrugRule
	 * @return
	 */
	@AutoLog(value = "药品规则库-编辑")
	@ApiOperation(value="药品规则库-编辑", notes="药品规则库-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeDrugRule ybChargeDrugRule) throws Exception {
		sysBaseAPI.addLog("药品规则库修改，id： " +ybChargeDrugRule.getId(), CommonConstant.LOG_TYPE_2, 2);
		String limitType = ybChargeDrugRule.getLimitType();
		String limitContent = ybChargeDrugRule.getLimitContent();
		String drugName = ybChargeDrugRule.getDrugName();
		if(StrUtil.isEmpty(limitType) || StrUtil.isEmpty(limitContent) || StrUtil.isEmpty(drugName)){
			throw new Exception("限制规则或限制内容或药品名称不能为空!");
		}
		//验证规则限制内容是否满足要求
		if(StrUtil.isNotEmpty(limitType) && StrUtil.isNotEmpty(limitContent)){
			RuleRegexResult ruleRegexResult = DcRuleHandleUtil.validRuleRegex(limitType, limitContent);
			if(!ruleRegexResult.isSuccess()){
				throw new Exception(ruleRegexResult.getMessage());
			}
		}
		ybChargeDrugRuleService.updateById(ybChargeDrugRule);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品规则库-通过id删除")
	@ApiOperation(value="药品规则库-通过id删除", notes="药品规则库-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysBaseAPI.addLog("药品规则库删除，id： " +id, CommonConstant.LOG_TYPE_2, 3);
		ybChargeDrugRuleService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "药品规则库-批量删除")
	@ApiOperation(value="药品规则库-批量删除", notes="药品规则库-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		sysBaseAPI.addLog("药品规则库批量删除，ids： " +ids, CommonConstant.LOG_TYPE_2, 3);
		this.ybChargeDrugRuleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品规则库-通过id查询")
	@ApiOperation(value="药品规则库-通过id查询", notes="药品规则库-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeDrugRule ybChargeDrugRule = ybChargeDrugRuleService.getById(id);
		return Result.ok(ybChargeDrugRule);
	}

	 /**
	  * 批量审核
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "收费明细风控检查内容-批量审核")
	 @ApiOperation(value = "收费明细风控检查内容-批量审核", notes = "收费明细风控检查内容-批量审核")
	 @GetMapping(value = "/batchExamine")
	 public Result<?> batchExamine(@RequestParam(name = "ids", required = true) String ids,
								   @RequestParam(name = "examineStatus", required = true) String examineStatus) {
		 sysBaseAPI.addLog("药品案例库批量审核，ids： " +ids, CommonConstant.LOG_TYPE_2, 2);
		 List<String> strings = Arrays.asList(ids.split(","));
		 ArrayList<YbChargeDrugRule> list = new ArrayList<>();
		 strings.stream().forEach(t -> {
			 YbChargeDrugRule drug = new YbChargeDrugRule();
			 drug.setId(t);
			 drug.setExamineStatus(examineStatus);
			 list.add(drug);
		 });
		 ybChargeDrugRuleService.updateBatchById(list);

		 return Result.ok("批量审核成功！");
	 }




	/**
	 * 导出excel
	 *
	 * @param ybChargeDrugRule
	 */
	@RequestMapping(value = "/exportXls")
	public void exportXls(HttpServletRequest req, HttpServletResponse response, YbChargeDrugRule ybChargeDrugRule) throws Exception {
		OutputStream os = response.getOutputStream();
		String title = "药品规则库" + System.currentTimeMillis();
		response.setContentType("application/octet-stream; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

		String[] titleArr = {"药品分类","药品分类(小)", "医保类别", "药品名称", "剂型","限制类型","限制内容", "备注", "整理人","审核状态"};
		String[] fieldArr = {"drugType", "drugTypeSmall", "funType", "drugName", "dosageType","limitType","limitContent", "remark", "sorter","examineStatus"};


		//导出数据
		QueryWrapper<YbChargeDrugRule> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDrugRule, req.getParameterMap());
		List<YbChargeDrugRule> list = ybChargeDrugRuleService.list(queryWrapper);


		// 生成一个表格
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet("药品规则库");

		int startHang = 0;

		// 设置标题样式
		CellStyle titleStyle = initTitleStyle(workbook);

		Row rowTitle = sheet.createRow(startHang);
		rowTitle.setHeight((short) 500);
		//填充表头
		for (int i = 0, len = titleArr.length; i < len; i++) {
			String t = titleArr[i];
			Cell cell = rowTitle.createCell(i);
			cell.setCellValue(t);
			cell.setCellStyle(titleStyle);
			sheet.setColumnWidth(i, 15 * 256);
		}

		startHang++;

		//填充值
		if (list.size() > 0) {
			int celNum = 0;
			for (YbChargeDrugRule word : list) {
				Class<? extends YbChargeDrugRule> aClass = word.getClass();
				Row row = sheet.createRow(startHang++);
				for (String field : fieldArr) {
					Cell cell = row.createCell(celNum++);
					Field declaredField = aClass.getDeclaredField(field);
					declaredField.setAccessible(true);
					MedicalDict annotation = declaredField.getAnnotation(MedicalDict.class);
					Object o = declaredField.get(word);
					if(annotation !=null && ObjectUtil.isNotEmpty(o)){
						String code = declaredField.getAnnotation(MedicalDict.class).dicCode();
						o = medicalDictService.queryDictTextByKey(code, o.toString().trim());
					}

					if (ObjectUtil.isEmpty(o)) {
						o = "";
					}
					cell.setCellValue(String.valueOf(o));

				}
				celNum = 0;
			}
		}


		workbook.write(os);
		workbook.dispose();


	}





	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/importExcel", method = RequestMethod.GET)
	public Result<?> importExcel(@RequestParam(name = "filePath", required = true) String filePath,
								 HttpServletRequest request) throws Exception {
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		File importFile = new File(uploadPath + "/" + filePath);
		if (!importFile.exists()) {
			throw new Exception("文件不存在");
		}
		sysBaseAPI.addLog("药品规则库导入，url： " +importFile, CommonConstant.LOG_TYPE_2, 1);
		// 获取上传文件对象
		FileInputStream fileInputStream = new FileInputStream(importFile);
		//字典数据
		Map<String, String> limitTypeDict = medicalDictService.queryNameMapByType("DC_DRUG_LIMIT_TYPE");

		//导入校验
		ImportParams params = new ImportParams();
		params.setHeadRows(1);
		params.setNeedSave(true);
		try {
			//解析数据
			List<YbChargeDrugRule> importList = ExcelImportUtil.importExcel(fileInputStream, YbChargeDrugRule.class, params);
			int errNum = 0;
			String errMsg = "";
			for (int i = 0; i < importList.size(); i++) {
				YbChargeDrugRule bean = importList.get(i);
				bean.setCreatedBy(loginUser.getUsername());
				bean.setCreatedByName(loginUser.getRealname());
				bean.setCreatedTime(new Date());

				//限制类型
				String limitType = limitTypeDict.get(bean.getLimitType());
				if(StrUtil.isEmpty(limitType)){
					errNum++;
					errMsg += "第" + (i + 2) + "行:限制类型不存在!";
					if (errNum >= 5) {
						return Result.error("文件导入失败:" + errMsg);
					}
				}else{
					bean.setLimitType(limitType);
				}

				// 限制内容
				String limitContent = bean.getLimitContent();
				if(StrUtil.isEmpty(limitContent)){
					errNum++;
					errMsg += "第" + (i + 2) + "行:限制内容不能为空!";
					if (errNum >= 5) {
						return Result.error("文件导入失败:" + errMsg);
					}
				}

				//验证规则限制内容是否满足要求
				if(StrUtil.isNotEmpty(limitType) && StrUtil.isNotEmpty(limitContent)){
					RuleRegexResult ruleRegexResult = DcRuleHandleUtil.validRuleRegex(limitType, limitContent);
					if(!ruleRegexResult.isSuccess()){
						errNum++;
						errMsg += "第" + (i + 2) + "行:"+ruleRegexResult.getMessage();
						if (errNum >= 5) {
							return Result.error("文件导入失败:" + errMsg);
						}
					}
				}


				// 药品名称
				String drugName = bean.getDrugName();
				if(StrUtil.isEmpty(drugName)){
					errNum++;
					errMsg += "第" + (i + 2) + "行:药品名称不能为空!";
					if (errNum >= 5) {
						return Result.error("文件导入失败:" + errMsg);
					}
				}

//				//整理人
//				String sorter = bean.getSorter();
//				if(StrUtil.hasBlank(sorter)){
//					errNum++;
//					errMsg += "第" + (i + 1) + "行:整理人必填!";
//					if (errNum >= 5) {
//						return Result.error("文件导入失败:" + errMsg);
//					}
//				}


			}
			if (errNum > 0) {
				return Result.error("文件导入失败:" + errMsg);
			}

			ybChargeDrugRuleService.saveBatch(importList);
			return Result.ok("文件导入成功！数据行数:" + importList.size());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error("文件导入失败:" + e.getMessage());
		} finally {
			try {
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
