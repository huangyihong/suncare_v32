package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.entity.MedicalOffice;
import com.ai.modules.config.service.ICommonAuditService;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.service.IMedicalOfficeService;
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
 * @Description: 科室信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Slf4j
@Api(tags="科室信息")
@RestController
@RequestMapping("/config/medicalOffice")
public class MedicalOfficeController extends JeecgController<MedicalOffice, IMedicalOfficeService> {
	@Autowired
	private IMedicalOfficeService service;

	@Autowired
	private IMedicalAuditLogService serviceLog;

	 @Autowired
	 @Qualifier("officeAuditService")
	 ICommonAuditService auditService;

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath;

	 @Autowired
	 IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalOffice
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "科室信息-分页列表查询")
	@ApiOperation(value="科室信息-分页列表查询", notes="科室信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalOffice medicalOffice,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalOffice> queryWrapper = QueryGenerator.initQueryWrapper(medicalOffice, req.getParameterMap());
		Page<MedicalOffice> page = new Page<MedicalOffice>(pageNo, pageSize);
		IPage<MedicalOffice> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}


	/**
	 * 管理维护分页列表查询
	 *
	 * @param medicalOffice
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "科室信息-管理维护分页列表查询")
	@ApiOperation(value="科室信息-管理维护分页列表查询", notes="科室信息-管理维护分页列表查询")
	@GetMapping(value = "/manageList")
	public Result<?> queryPageManageList(MedicalOffice medicalOffice,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalOffice> queryWrapper = service.getQueryWrapper(medicalOffice, req);
		Page<MedicalOffice> page = new Page<MedicalOffice>(pageNo, pageSize);
		IPage<MedicalOffice> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "科室信息-添加")
	@ApiOperation(value="科室信息-添加", notes="科室信息-添加")
	@PostMapping(value = "/add")
	@RequiresPermissions("basicDataManage:add")
	public Result<?> add(@RequestBody MedicalOffice bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String id = bean.getId();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
		if(StringUtils.isNotBlank(id)){//修改(新增待审核状态)
			service.onlyUpdateMedicalOffice(bean);
			return Result.ok("修改成功！");
		}else{//第一次新增
			String medicalOfficeId = IdUtils.uuid();
        	bean.setId(medicalOfficeId);
        	bean.setCreateTime(new Date());
        	bean.setState(MedicalAuditLogConstants.STATE_DSX);//待生效
        	bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);
        	bean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
			service.saveMedicalOffice(bean);
		}
		return Result.ok("添加成功！");
	}

	/**
	 * 修改
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "科室信息-修改")
	@ApiOperation(value="科室信息-修改", notes="科室信息-修改")
	@PutMapping(value = "/edit")
	@RequiresPermissions("basicDataManage:edit")
	public Result<?> edit(@RequestBody MedicalOffice bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setUpdateTime(new Date());
		service.updateMedicalOffice(bean);
		return Result.ok("修改成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "科室信息-通过id查询")
	@ApiOperation(value="科室信息-通过id查询", notes="科室信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalOffice medicalOffice = service.getById(id);
		return Result.ok(medicalOffice);
	}

	/**
	 * 判断code是否重复
	 * @param request
	 * @param code
	 * @param id
	 * @return
	 */
   	@AutoLog(value = "科室信息-判断code是否重复 ")
	@ApiOperation(value="科室信息-判断code是否重复 ", notes="科室信息-判断code是否重复 ")
	@GetMapping(value = "/isExistName")
   	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="code",required=true)String code,String id){
    	boolean flag = service.isExistName(code,id);
   		return Result.ok(flag);
   	}

	@AutoLog(value = "删除操作")
	@ApiOperation(value="删除操作", notes="删除操作")
	@PutMapping(value = "/delMedicalOffice")
	@RequiresPermissions("basicDataManage:del")
	public Result<?> delMedicalOffice(@RequestBody MedicalOffice bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setDeleteTime(new Date());
		service.delMedicalOffice(bean);
		return Result.ok("删除操作成功");
	}

	@AutoLog(value = "全部删除操作")
	@ApiOperation(value="全部删除操作", notes="全部删除操作")
	@GetMapping(value = "/delAllMedicalOffice")
	@RequiresPermissions("basicDataManage:delAll")
   	public Result<?> delAllMedicalOffice(HttpServletRequest req,MedicalOffice medicalOffice) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOffice> queryWrapper = service.getQueryWrapper(medicalOffice, req);
			MedicalOffice bean = new MedicalOffice();
			bean.setDeleteReason(req.getParameter("deleteReason1"));
			bean.setDeleteStaffName(user.getRealname());
			bean.setDeleteStaff(user.getRealname());
			bean.setDeleteTime(new Date());
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			//service.delAllMedicalOffice(queryWrapper,medicalDrug);
			return importTaskService.saveBatchTask("MEDICAL_OFFICE","科室信息全部删除",bean, queryWrapper,
					(b,q)->{
						try {
							return this.service.delAllMedicalOffice(q, (MedicalOffice) b);
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
	 * @param medicalOffice
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "查询数据条数")
	@ApiOperation(value="查询数据条数", notes="查询数据条数")
	@GetMapping(value = "/getDataCount")
	public Result<?> getDataCount(MedicalOffice medicalOffice,HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalOffice> queryWrapper = service.getQueryWrapper(medicalOffice, req);
		int count = service.count(queryWrapper);
		return Result.ok(count);
	}

	@AutoLog(value = "一键清理")
   	@ApiOperation(value="一键清理", notes="一键清理")
    @GetMapping(value = "/saveCleanMedicalOffice")
	@RequiresPermissions("basicDataManage:clean")
   	public Result<?> saveCleanMedicalOffice(HttpServletRequest req,MedicalOffice medicalOffice) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOffice> queryWrapper = service.getQueryWrapper(medicalOffice, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
//			service.saveCleanMedicalOffice(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_OFFICE","科室信息一键清理",bean, queryWrapper,
					(b,q)->{
						return this.service.saveCleanMedicalOffice(q, (MedicalAuditLog) b);
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
   	public Result<?> saveUndoAllMedicalAuditLog(HttpServletRequest req,MedicalOffice medicalOffice) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOffice> queryWrapper = service.getQueryWrapper(medicalOffice, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			List list = service.list(queryWrapper);
			//auditService.saveUndoAllMedicalAuditLog(bean,list);
			return importTaskService.saveBatchTask("MEDICAL_OFFICE","科室信息全部撤销",bean, list,
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
	public Result<?> saveAuditAllMedicalAuditLog(HttpServletRequest req, MedicalOffice medicalOffice)
			throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOffice> queryWrapper = service.getQueryWrapper(medicalOffice, req);
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
			return importTaskService.saveBatchTask("MEDICAL_OFFICE","科室信息批量审核",bean, list,
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
	 * @param medicalOffice
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, MedicalOffice medicalOffice) {
		return super.exportXls(request, medicalOffice, MedicalOffice.class, "科室信息表");
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
			return importTaskService.saveImportTask("MEDICAL_OFFICE","科室信息导入",file,user,
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
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalOffice bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "科室信息_导出";
		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		String tableName = req.getParameter("tableName");
		QueryWrapper<MedicalOffice> queryWrapper = service.getQueryWrapper(bean, req);
		int count = service.count(queryWrapper);
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_OFFICE";
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
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalOffice bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "科室信息_导出";
		}
		String tableName = req.getParameter("tableName");
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_OFFICE";
 		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		// 选中数据
		String selections = req.getParameter("selections");
		if (StringUtils.isNotEmpty(selections)) {
			bean.setId(selections);
		}
		QueryWrapper<MedicalOffice> queryWrapper = service.getQueryWrapper(bean, req);
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

    /**
	 * 获取一级二级科目
	 * @param request
	 * @param typeCode
	 * @return
	 */
   	@AutoLog(value = "科室信息-获取一级二级科目 ")
	@ApiOperation(value="科室信息-获取一级二级科目 ", notes="科室信息-获取一级二级科目 ")
	@GetMapping(value = "/getTypeList")
   	public Result<?> getTypeList(HttpServletRequest request,@RequestParam(name="typeCode",required=true)String typeCode){
   		List<Map<String,Object>> typeList = service.getTypeList(typeCode);
   		return Result.ok(typeList);
   	}


}
