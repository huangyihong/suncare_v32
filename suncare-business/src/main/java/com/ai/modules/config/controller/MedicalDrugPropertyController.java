package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.entity.MedicalDrugProperty;
import com.ai.modules.config.service.ICommonAuditService;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalDrugPropertyService;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

 /**
 * @Description: 药品属性表
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="药品属性表")
@RestController
@RequestMapping("/config/medicalDrugProperty")
public class MedicalDrugPropertyController extends JeecgController<MedicalDrugProperty, IMedicalDrugPropertyService> {
	@Autowired
	private IMedicalDrugPropertyService service;

	@Autowired
	private IMedicalAuditLogService serviceLog;

	 @Autowired
	 @Qualifier("drugPropertyAuditService")
	 ICommonAuditService auditService;

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath;

	 @Autowired
	 IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalDrugProperty
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品属性-分页列表查询")
	@ApiOperation(value="药品属性-分页列表查询", notes="药品属性-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDrugProperty medicalDrugProperty,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDrugProperty> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugProperty, req.getParameterMap());
		Page<MedicalDrugProperty> page = new Page<MedicalDrugProperty>(pageNo, pageSize);
		IPage<MedicalDrugProperty> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 管理维护分页列表查询
	 *
	 * @param medicalDrugProperty
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "药品属性-管理维护分页列表查询")
	@ApiOperation(value="药品属性-管理维护分页列表查询", notes="药品属性-管理维护分页列表查询")
	@GetMapping(value = "/manageList")
	public Result<?> queryPageManageList(MedicalDrugProperty medicalDrugProperty,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalDrugProperty> queryWrapper = service.getQueryWrapper(medicalDrugProperty, req);
		Page<MedicalDrugProperty> page = new Page<MedicalDrugProperty>(pageNo, pageSize);
		IPage<MedicalDrugProperty> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "药品属性-添加")
	@ApiOperation(value="药品属性-添加", notes="药品属性-添加")
	@PostMapping(value = "/add")
	@RequiresPermissions("basicDataManage:add")
	public Result<?> add(@RequestBody MedicalDrugProperty bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String id = bean.getId();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
		if(StringUtils.isNotBlank(id)){//修改(新增待审核状态)
			service.onlyUpdateMedicalDrugProperty(bean);
			return Result.ok("修改成功！");
		}else{//第一次新增
			String medicalDrugPropertyId = IdUtils.uuid();
        	bean.setId(medicalDrugPropertyId);
        	bean.setCreateTime(new Date());
        	bean.setState(MedicalAuditLogConstants.STATE_DSX);//待生效
        	bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);
        	bean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
			service.saveMedicalDrugProperty(bean);
		}
		return Result.ok("添加成功！");
	}

	/**
	 * 修改
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "药品属性-修改")
	@ApiOperation(value="药品属性-修改", notes="药品属性-修改")
	@PutMapping(value = "/edit")
	@RequiresPermissions("basicDataManage:edit")
	public Result<?> edit(@RequestBody MedicalDrugProperty bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setUpdateTime(new Date());
		service.updateMedicalDrugProperty(bean);
		return Result.ok("修改成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品属性-通过id查询")
	@ApiOperation(value="药品属性-通过id查询", notes="药品属性-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDrugProperty medicalDrugProperty = service.getById(id);
		return Result.ok(medicalDrugProperty);
	}

	/**
	 * 判断序号orderNum是否重复
	 * @param request
	 * @param orderNum
	 * @param id
	 * @return
	 */
   	@AutoLog(value = "药品属性-判断序号orderNum是否重复 ")
	@ApiOperation(value="药品属性-判断序号orderNum是否重复 ", notes="药品属性-判断序号orderNum是否重复 ")
	@GetMapping(value = "/isExistOrderNum")
   	public Result<?> isExistOrderNum(HttpServletRequest request,@RequestParam(name="orderNum",required=true)int orderNum,String id){
    	boolean flag = service.isExistOrderNum(orderNum,id);
   		return Result.ok(flag);
   	}

   	/**
	 * 查询最大序号orderNum
	 *
	 * @param medicalDrugProperty
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "查询最大序号orderNum")
	@ApiOperation(value="查询最大序号orderNum", notes="查询最大序号orderNum")
	@GetMapping(value = "/getMaxOrderNum")
	public Result<?> getMaxOrderNum(MedicalDrugProperty medicalDrugProperty,HttpServletRequest req) throws Exception {
		int orderNum = service.getMaxOrderNum()+1;
		return Result.ok(orderNum);
	}

	@AutoLog(value = "删除操作")
	@ApiOperation(value="删除操作", notes="删除操作")
	@PutMapping(value = "/delMedicalDrugProperty")
	@RequiresPermissions("basicDataManage:del")
	public Result<?> delMedicalDrugProperty(@RequestBody MedicalDrugProperty bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setDeleteTime(new Date());
		service.delMedicalDrugProperty(bean);
		return Result.ok("删除操作成功");
	}

	@AutoLog(value = "全部删除操作")
	@ApiOperation(value="全部删除操作", notes="全部删除操作")
	@GetMapping(value = "/delAllMedicalDrugProperty")
	@RequiresPermissions("basicDataManage:delAll")
   	public Result<?> delAllMedicalDrugProperty(HttpServletRequest req,MedicalDrugProperty medicalDrugProperty) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalDrugProperty> queryWrapper = service.getQueryWrapper(medicalDrugProperty, req);
			MedicalDrugProperty bean = new MedicalDrugProperty();
			bean.setDeleteReason(req.getParameter("deleteReason1"));
			bean.setDeleteStaffName(user.getRealname());
			bean.setDeleteStaff(user.getRealname());
			bean.setDeleteTime(new Date());
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
//			service.delAllMedicalDrugProperty(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_DRUG_PROPERTY","药品属性全部删除",bean, queryWrapper,
					(b,q)->{
						try {
							return this.service.delAllMedicalDrugProperty(q, (MedicalDrugProperty) b);
						} catch (Exception e) {
							e.printStackTrace();
							return 0;
						}
					});
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
    }

	/**
	 * 查询数据条数
	 *
	 * @param medicalDrugProperty
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "查询数据条数")
	@ApiOperation(value="查询数据条数", notes="查询数据条数")
	@GetMapping(value = "/getDataCount")
	public Result<?> getDataCount(MedicalDrugProperty medicalDrugProperty,HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalDrugProperty> queryWrapper = service.getQueryWrapper(medicalDrugProperty, req);
		int count = service.count(queryWrapper);
		return Result.ok(count);
	}

	@AutoLog(value = "一键清理")
   	@ApiOperation(value="一键清理", notes="一键清理")
    @GetMapping(value = "/saveCleanMedicalDrugProperty")
	@RequiresPermissions("basicDataManage:clean")
   	public Result<?> saveCleanMedicalDrugProperty(HttpServletRequest req,MedicalDrugProperty medicalDrugProperty) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalDrugProperty> queryWrapper = service.getQueryWrapper(medicalDrugProperty, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
//			service.saveCleanMedicalDrugProperty(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_DRUG_PROPERTY","药品属性一键清理",bean, queryWrapper,
					(b,q)->{
						return this.service.saveCleanMedicalDrugProperty(q, (MedicalAuditLog) b);
					});
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
    }


	@AutoLog(value = "全部撤销")
   	@ApiOperation(value="全部撤销", notes="全部撤销")
    @GetMapping(value = "/saveUndoAllMedicalAuditLog")
	@RequiresPermissions("basicDataManage:undoAll")
   	public Result<?> saveUndoAllMedicalAuditLog(HttpServletRequest req,MedicalDrugProperty medicalDrugProperty) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalDrugProperty> queryWrapper = service.getQueryWrapper(medicalDrugProperty, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			List list = service.list(queryWrapper);
			//auditService.saveUndoAllMedicalAuditLog(bean,list);
			return importTaskService.saveBatchTask("MEDICAL_DRUG_PROPERTY","药品属性全部撤销",bean, list,
					(b,l)->{
						try {
							this.auditService.saveUndoAllMedicalAuditLog(b,l);
							return Result.ok("全部撤销操作成功");
						} catch (Exception e) {
							e.printStackTrace();
							return Result.error(e.getMessage());
						}
					});
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
    }

	@AutoLog(value = "批量审核操作")
	@ApiOperation(value = "批量审核操作", notes = "批量审核操作")
	@GetMapping(value = "/saveAuditAllMedicalAuditLog")
	@RequiresPermissions("basicDataManage:auditAll")
	public Result<?> saveAuditAllMedicalAuditLog(HttpServletRequest req, MedicalDrugProperty medicalDrugProperty)
			throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalDrugProperty> queryWrapper = service.getQueryWrapper(medicalDrugProperty, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setAuditResult(req.getParameter("auditResult1"));
			bean.setAuditOpinion(req.getParameter("auditOpinion1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			bean.setAuditStaff(user.getId());
			bean.setAuditStaffName(user.getRealname());
			List list = service.list(queryWrapper);
			//auditService.saveAuditAllMedicalAuditLog(bean, list);
			return importTaskService.saveBatchTask("MEDICAL_DRUG_PROPERTY","药品属性批量审核",bean, list,
					(b,l)->{
						try {
							this.auditService.saveAuditAllMedicalAuditLog(b,l);
							return Result.ok("批量审核操作成功");
						} catch (Exception e) {
							e.printStackTrace();
							return Result.error(e.getMessage());
						}
					});
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param medicalDrugProperty
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, MedicalDrugProperty medicalDrugProperty) {
		return super.exportXls(request, medicalDrugProperty, MedicalDrugProperty.class, "药品属性表");
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
			return importTaskService.saveImportTask("MEDICAL_DRUG_PROPERTY_PROPERTY","药品属性导入",file,user,
					(f,u)->{
						try {
							return this.service.importExcel(f,u);
						} catch (Exception e) {
							e.printStackTrace();
							return Result.error(e.getMessage());
						}
					});

		}
		return Result.error("上传文件为空");
	}

	/**
	 * 导出excel
	 *
	 * @param req
	 * @param bean
	 * @throws Exception
	 */
    @AutoLog(value = "线程导出excel")
	@ApiOperation(value="线程导出excel", notes="线程导出excel")
	@RequestMapping(value = "/exportExcelByThread")
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalDrugProperty bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "药品属性表_导出";
		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		String tableName = req.getParameter("tableName");
		QueryWrapper<MedicalDrugProperty> queryWrapper = service.getQueryWrapper(bean, req);
		int count = service.count(queryWrapper);
        if(StringUtils.isBlank(tableName)) {
        	tableName ="MEDICAL_DRUG_PROPERTY_PROPERTY";
		}
		String finalTableName = tableName;
		ThreadUtils.EXPORT_POOL.add(title,suffix, count, (os)->{
			Result exportResult = Result.ok();
			try {
				this.service.exportExcel(queryWrapper,os,suffix);
			} catch (Exception e) {
				e.printStackTrace();
				exportResult = Result.error(e.getMessage());
			} finally {
				serviceLog.insertExportLog(finalTableName,count,user);
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
	 * @param bean
	 * @throws Exception
	 */
    @RequestMapping(value = "/exportExcel")
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalDrugProperty bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "药品属性表_导出";
		}
		String tableName = req.getParameter("tableName");
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_DRUG_PROPERTY_PROPERTY";
 		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		// 选中数据
		String selections = req.getParameter("selections");
		if (StringUtils.isNotEmpty(selections)) {
			bean.setId(selections);
		}
		QueryWrapper<MedicalDrugProperty> queryWrapper = service.getQueryWrapper(bean, req);
		int count = service.count(queryWrapper);
		//response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename="+new String((title+"."+suffix).getBytes("UTF-8"),"iso-8859-1"));
        try {
        	OutputStream os =response.getOutputStream();
        	service.exportExcel(queryWrapper,os,suffix);
        } catch (Exception e) {
			throw e;
		} finally {
			//日志记录
			serviceLog.insertExportLog(tableName,count,user);
		}
	}

}
