package com.ai.modules.ybChargeSearch.controller;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.ybChargeSearch.vo.YbChargeDrugRuleVo;
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
import com.ai.modules.ybChargeSearch.entity.YbChargeDrug;
import com.ai.modules.ybChargeSearch.service.IYbChargeDrugService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static com.ai.modules.ybChargeSearch.controller.YbChargeSearchTaskController.initTitleStyle;

/**
 * @Description: 药品案例库
 * @Author: jeecg-boot
 * @Date:   2023-02-08
 * @Version: V1.0
 */
@Slf4j
@Api(tags="药品案例库")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeDrug")
public class YbChargeDrugController extends JeecgController<YbChargeDrug, IYbChargeDrugService> {
	@Autowired
	private IYbChargeDrugService ybChargeDrugService;

	@Value(value = "${jeecg.path.upload}")
	private String uploadPath;
	@Autowired
	private IMedicalDictService medicalDictService;
	@Autowired
	private ISysBaseAPI sysBaseAPI;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeDrug
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品案例库-分页列表查询")
	@ApiOperation(value="药品案例库-分页列表查询", notes="药品案例库-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeDrug ybChargeDrug,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		String drugName = ybChargeDrug.getDrugName();
		QueryWrapper<YbChargeDrug> queryWrapper = new QueryWrapper<>();
		if(StrUtil.isNotEmpty(drugName) && drugName.contains("#")){
			drugName=drugName.trim();
			ybChargeDrug.setDrugName("");
			queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDrug, req.getParameterMap());
			List<String> itemNames = Arrays.asList(drugName.split("#"));
			queryWrapper.in("drug_name",itemNames);
		}else if(StrUtil.isNotEmpty(drugName)){
			ybChargeDrug.setDrugName("");
			queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDrug, req.getParameterMap());
			queryWrapper.like("drug_name",drugName);
		}else{
			queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDrug, req.getParameterMap());
		}


		Page<YbChargeDrug> page = new Page<YbChargeDrug>(pageNo, pageSize);
		IPage<YbChargeDrug> pageList = ybChargeDrugService.page(page, queryWrapper);
		return Result.ok(pageList);
	}




	/**
	 * 添加
	 *
	 * @param ybChargeDrug
	 * @return
	 */
	@AutoLog(value = "药品案例库-添加")
	@ApiOperation(value="药品案例库-添加", notes="药品案例库-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeDrug ybChargeDrug) {
		ybChargeDrugService.save(ybChargeDrug);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param ybChargeDrug
	 * @return
	 */
	@AutoLog(value = "药品案例库-编辑")
	@ApiOperation(value="药品案例库-编辑", notes="药品案例库-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeDrug ybChargeDrug) {
		sysBaseAPI.addLog("药品案例库修改，id： " +ybChargeDrug.getId(), CommonConstant.LOG_TYPE_2, 2);
		ybChargeDrugService.updateById(ybChargeDrug);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品案例库-通过id删除")
	@ApiOperation(value="药品案例库-通过id删除", notes="药品案例库-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysBaseAPI.addLog("药品案例库删除，id： " +id, CommonConstant.LOG_TYPE_2, 3);
		ybChargeDrugService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "药品案例库-批量删除")
	@ApiOperation(value="药品案例库-批量删除", notes="药品案例库-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		sysBaseAPI.addLog("药品案例库批量删除，ids： " +ids, CommonConstant.LOG_TYPE_2, 3);
		this.ybChargeDrugService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品案例库-通过id查询")
	@ApiOperation(value="药品案例库-通过id查询", notes="药品案例库-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeDrug ybChargeDrug = ybChargeDrugService.getById(id);
		return Result.ok(ybChargeDrug);
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
		 ArrayList<YbChargeDrug> list = new ArrayList<>();
		 strings.stream().forEach(t -> {
			 YbChargeDrug drug = new YbChargeDrug();
			 drug.setId(t);
			 drug.setExamineStatus(examineStatus);
			 list.add(drug);
		 });
		 ybChargeDrugService.updateBatchById(list);

		 return Result.ok("批量审核成功！");
	 }


	/**
	 * 导出excel
	 *
	 * @param ybChargeDrug
	 */
	@RequestMapping(value = "/exportXls")
	public void exportXls(HttpServletRequest req, HttpServletResponse response, YbChargeDrug ybChargeDrug) throws Exception {
		OutputStream os = response.getOutputStream();
		String title = "药品案例库" + System.currentTimeMillis();
		response.setContentType("application/octet-stream; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

		String[] titleArr = {"药品分类","药品分类(小)", "医保类别", "药品名称", "剂型", "备注", "整理人","审核状态"};
		String[] fieldArr = {"drugType", "drugTypeSmall", "funType", "drugName", "dosageType", "remark", "sorter","examineStatus"};


		//导出数据
		QueryWrapper<YbChargeDrug> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDrug, req.getParameterMap());
		List<YbChargeDrug> list = ybChargeDrugService.list(queryWrapper);


		// 生成一个表格
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet("药品案例库");

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
			for (YbChargeDrug word : list) {
				Class<? extends YbChargeDrug> aClass = word.getClass();
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
		sysBaseAPI.addLog("药品案例库导入，url： " +importFile, CommonConstant.LOG_TYPE_2, 1);
		// 获取上传文件对象
		FileInputStream fileInputStream = new FileInputStream(importFile);

		//导入校验
		ImportParams params = new ImportParams();
		params.setHeadRows(1);
		params.setNeedSave(true);
		try {
			//解析数据
			List<YbChargeDrug> importList = ExcelImportUtil.importExcel(fileInputStream, YbChargeDrug.class, params);
			int errNum = 0;
			String errMsg = "";
			for (int i = 0; i < importList.size(); i++) {
				YbChargeDrug bean = importList.get(i);
				bean.setCreatedBy(loginUser.getUsername());
				bean.setCreatedByName(loginUser.getRealname());
				bean.setCreatedTime(new Date());

				//整理人
				String sorter = bean.getSorter();
				if(StrUtil.hasBlank(sorter)){
					errNum++;
					errMsg += "第" + (i + 1) + "行:整理人必填!";
					if (errNum >= 5) {
						return Result.error("文件导入失败:" + errMsg);
					}
				}


			}
			if (errNum > 0) {
				return Result.error("文件导入失败:" + errMsg);
			}

			ybChargeDrugService.saveBatch(importList);
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
