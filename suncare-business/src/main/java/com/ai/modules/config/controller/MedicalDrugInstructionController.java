package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrugInstruction;
import com.ai.modules.config.entity.MedicalDrugInstructionItem;
import com.ai.modules.config.service.IMedicalDrugInstructionService;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.*;

/**
 * @Description: 药品说明书
 * @Author: jeecg-boot
 * @Date:   2020-11-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="药品说明书")
@RestController
@RequestMapping("/config/medicalDrugInstruction")
public class MedicalDrugInstructionController extends JeecgController<MedicalDrugInstruction, IMedicalDrugInstructionService> {
	@Autowired
	private IMedicalDrugInstructionService medicalDrugInstructionService;

	@Autowired
	IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalDrugInstruction
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品说明书-分页列表查询")
	@ApiOperation(value="药品说明书-分页列表查询", notes="药品说明书-分页列表查询")
	@GetMapping(value = "/manageList")
	public Result<?> manageList(MedicalDrugInstruction medicalDrugInstruction, MedicalDrugInstructionItem medicalDrugInstructionItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception{
		QueryWrapper<MedicalDrugInstruction> queryWrapper = getMedicalDrugInstructionQueryWrapper(medicalDrugInstruction, medicalDrugInstructionItem, req);
		Page<MedicalDrugInstruction> page = new Page<MedicalDrugInstruction>(pageNo, pageSize);
		IPage<MedicalDrugInstruction> pageList = medicalDrugInstructionService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	private QueryWrapper<MedicalDrugInstruction> getMedicalDrugInstructionQueryWrapper(MedicalDrugInstruction medicalDrugInstruction, MedicalDrugInstructionItem medicalDrugInstructionItem, HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalDrugInstruction> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugInstruction, req.getParameterMap());
		String inStr = "";
		if (StringUtils.isNotEmpty(medicalDrugInstructionItem.getItemCode())) {
			inStr += "  and ITEM_CODE like '" + medicalDrugInstructionItem.getItemCode().replaceAll("\\*","%") + "'";
		}
		if (StringUtils.isNotEmpty(medicalDrugInstructionItem.getItemValue())) {
			inStr += "  and "+DbDataEncryptUtil.decryptFunc("ITEM_VALUE")+" like '" + medicalDrugInstructionItem.getItemValue().replaceAll("\\*","%") + "'";
		}
		if (inStr.length() > 0) {
			queryWrapper.exists("SELECT 1 FROM MEDICAL_DRUG_INSTRUCTION_ITEM a where MEDICAL_DRUG_INSTRUCTION.PARENT_ID=a.PARENT_ID" + inStr);
		}
		queryWrapper = MedicalAuditLogConstants.queryTime(queryWrapper,req);
		return queryWrapper;
	}

	/**
	 * 分页列表查询
	 *
	 * @param medicalDrugInstruction
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品说明书-分页列表查询")
	@ApiOperation(value="药品说明书-分页列表查询", notes="药品说明书-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDrugInstruction medicalDrugInstruction,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDrugInstruction> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugInstruction, req.getParameterMap());
		Page<MedicalDrugInstruction> page = new Page<MedicalDrugInstruction>(pageNo, pageSize);
		IPage<MedicalDrugInstruction> pageList = medicalDrugInstructionService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 通过code查询
	 *
	 * @param code
	 * @return
	 */
	@AutoLog(value = "药品说明书-通过code查询")
	@ApiOperation(value="药品说明书-通过code查询", notes="药品说明书-通过code查询")
	@GetMapping(value = "/queryByCode")
	public Result<?> queryByCode(@RequestParam(name="code") String code) {
		MedicalDrugInstruction MedicalDrugInstruction = medicalDrugInstructionService.getOne(new QueryWrapper<MedicalDrugInstruction>().eq("CODE",code));
		return Result.ok(MedicalDrugInstruction);
	}

	/**
	 * 通过codes查询
	 *
	 * @param codes
	 * @return
	 */
	@AutoLog(value = "药品说明书-通过code查询")
	@ApiOperation(value="药品说明书-通过code查询", notes="药品说明书-通过code查询")
	@GetMapping(value = "/queryByCodes")
	public Result<?> queryByCodes(@RequestParam(name="codes") String codes) {
		List<String> codeList = Arrays.asList(codes.split(","));
		List<MedicalDrugInstruction> list = medicalDrugInstructionService.list(new QueryWrapper<MedicalDrugInstruction>().in("CODE",codeList));
		return Result.ok(list);
	}

	/**
	 * 添加
	 *
	 * @param MedicalDrugInstruction
	 * @return
	 */
	@AutoLog(value = "药品说明书-添加")
	@ApiOperation(value="药品说明书-添加", notes="药品说明书-添加")
	@PostMapping(value = "/add")
	public Result<?> add(MedicalDrugInstruction MedicalDrugInstruction,String itemCodes, String itemNames, String tableTypes) {
		medicalDrugInstructionService.saveMedicalDrugInstruction(MedicalDrugInstruction, itemCodes,  itemNames, tableTypes);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param MedicalDrugInstruction
	 * @return
	 */
	@AutoLog(value = "药品说明书-编辑")
	@ApiOperation(value="药品说明书-编辑", notes="药品说明书-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(MedicalDrugInstruction MedicalDrugInstruction,String itemCodes, String itemNames, String tableTypes) {
		medicalDrugInstructionService.updateMedicalDrugInstruction(MedicalDrugInstruction, itemCodes,  itemNames, tableTypes);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品说明书-通过id删除")
	@ApiOperation(value="药品说明书-通过id删除", notes="药品说明书-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDrugInstructionService.deleteById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "药品说明书-批量删除")
	@ApiOperation(value="药品说明书-批量删除", notes="药品说明书-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDrugInstructionService.deleteByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品说明书-通过id查询")
	@ApiOperation(value="药品说明书-通过id查询", notes="药品说明书-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDrugInstruction MedicalDrugInstruction = medicalDrugInstructionService.getById(id);
		return Result.ok(MedicalDrugInstruction);
	}

	/**
	 * 判断code是否重复
	 * @param request
	 * @param code
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品说明书-判断groupCode是否重复 ")
	@ApiOperation(value="药品说明书-判断groupCode是否重复 ", notes="药品说明书-判断groupCode是否重复 ")
	@GetMapping(value = "/isExistName")
	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="code",required=true)String code,String id){
		boolean flag = medicalDrugInstructionService.isExistName(code,id);
		return Result.ok(flag);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDrugInstruction
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDrugInstruction medicalDrugInstruction) {
      return super.exportXls(request, medicalDrugInstruction, MedicalDrugInstruction.class, "药品说明书");
  }

	/**
	 * 导出excel
	 *
	 * @param req
	 * @param medicalDrugInstruction
	 * @param medicalDrugInstructionItem
	 * @throws Exception
	 */
	@AutoLog(value = "线程导出excel")
	@ApiOperation(value="线程导出excel", notes="线程导出excel")
	@RequestMapping(value = "/exportExcelByThread")
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalDrugInstruction medicalDrugInstruction, MedicalDrugInstructionItem medicalDrugInstructionItem) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "药品说明书_导出";
		}
		String suffix= ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		final String tableName = req.getParameter("tableName");
		QueryWrapper<MedicalDrugInstruction> queryWrapper = getMedicalDrugInstructionQueryWrapper(medicalDrugInstruction, medicalDrugInstructionItem, req);
		int count = medicalDrugInstructionService.count(queryWrapper);
		ThreadUtils.EXPORT_POOL.add(title,suffix, count, (os)->{
			Result exportResult = Result.ok();
			try {
				List<MedicalDrugInstruction> list = medicalDrugInstructionService.list(queryWrapper);
				medicalDrugInstructionService.exportExcel(list,os,suffix);
			} catch (Exception e) {
				e.printStackTrace();
				exportResult = Result.error(e.getMessage());
			}
			return exportResult;
		});

		result.setMessage("等待导出，请在导出记录界面查看进度");
		return result;
	}

	/**
	 * 直接导出excel
	 *
	 * @param req
	 * @param medicalDrugInstruction
	 * @param medicalDrugInstructionItem
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportExcel")
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalDrugInstruction medicalDrugInstruction, MedicalDrugInstructionItem medicalDrugInstructionItem) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "药品说明书_导出";
		}
		//response.reset();
		try {
			String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
			// 选中数据
			String selections = req.getParameter("selections");
			if (StringUtils.isNotEmpty(selections)) {
				medicalDrugInstruction.setId(selections);
			}
			QueryWrapper<MedicalDrugInstruction> queryWrapper = getMedicalDrugInstructionQueryWrapper(medicalDrugInstruction, medicalDrugInstructionItem, req);
			List<MedicalDrugInstruction> list = medicalDrugInstructionService.list(queryWrapper);
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename="+new String((title+"."+suffix).getBytes("UTF-8"),"iso-8859-1"));
			OutputStream os =response.getOutputStream();

			medicalDrugInstructionService.exportExcel(list,os,suffix);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
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

			return importTaskService.saveImportTask("MEDICAL_DRUG_INSTRUCTION","药品说明书导入",file,user,
					(f,u)-> {
						try {
							return this.medicalDrugInstructionService.importExcel(f, u);
						} catch (Exception e) {
							e.printStackTrace();
							return Result.error(e.getMessage());
						}
					});
		}
		return Result.error("上传文件为空");
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
			String code = fileName.substring(0,index);
			MedicalDrugInstruction bean = this.medicalDrugInstructionService.getBeanByCode(code);
			if(bean == null){
				throw new Exception("说明书编码不存在：" + code);
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

			this.medicalDrugInstructionService.updateById(bean);

			return Result.ok(path);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	/**
	 * 列表查询
	 *
	 * @param itemCodes
	 * @param req
	 * @return
	 */
	@AutoLog(value = "根据子项code查询说明书列表-列表查询")
	@ApiOperation(value="根据子项code查询说明书列表-列表查询", notes="根据子项code查询说明书列表")
	@GetMapping(value = "/listByItemCodes")
	public Result<?> listByItemCodes(@RequestParam("itemCodes") String itemCodes,
							   HttpServletRequest req) {
		QueryWrapper<MedicalDrugInstruction> queryWrapper = new QueryWrapper<MedicalDrugInstruction>();
		Map<String, List<MedicalDrugInstruction>>  itemMapList= new HashMap<>();
		String[] itemCodeArray = itemCodes.split(",");
		for(String itemCode:itemCodeArray){
			queryWrapper = new QueryWrapper<MedicalDrugInstruction>();
			queryWrapper.inSql("ID",
					"SELECT PARENT_ID FROM MEDICAL_DRUG_INSTRUCTION_ITEM where 1=1 and ITEM_CODE ='"+itemCode+"'");
			queryWrapper.orderByAsc("CODE");
			List<MedicalDrugInstruction> list = medicalDrugInstructionService.list(queryWrapper);
			itemMapList.put(itemCode,list);
		}
		return Result.ok(itemMapList);
	}


}
