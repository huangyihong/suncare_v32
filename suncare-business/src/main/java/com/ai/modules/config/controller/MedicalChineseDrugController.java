package com.ai.modules.config.controller;

import com.ai.common.utils.BeanUtil;
import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalChineseDrug;
import com.ai.modules.config.service.ICommonAuditService;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalChineseDrugService;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.AbsDictMergeHandleFactory;
import com.ai.modules.medical.handle.HerbDrugDictMergeHandleFactory;
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
 * @Description: 中草药维护
 * @Author: jeecg-boot
 * @Date:   2019-12-20
 * @Version: V1.0
 */
@Slf4j
@Api(tags="中草药维护")
@RestController
@RequestMapping("/config/medicalChineseDrug")
public class MedicalChineseDrugController extends JeecgController<MedicalChineseDrug, IMedicalChineseDrugService> {
	@Autowired
	private IMedicalChineseDrugService service;

	@Autowired
	private IMedicalAuditLogService serviceLog;

	 @Autowired
	 @Qualifier("chineseDrugAuditService")
	 ICommonAuditService auditService;

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath;

	 @Autowired
	 IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalChineseDrug
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "中草药维护-分页列表查询")
	@ApiOperation(value="中草药维护-分页列表查询", notes="中草药维护-分页列表查询")
	@RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
	public Result<?> queryPageList(MedicalChineseDrug medicalChineseDrug,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalChineseDrug> queryWrapper = QueryGenerator.initQueryWrapper(medicalChineseDrug, req.getParameterMap());
		Page<MedicalChineseDrug> page = new Page<MedicalChineseDrug>(pageNo, pageSize);
		IPage<MedicalChineseDrug> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 管理维护分页列表查询
	 *
	 * @param medicalChineseDrug
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "中草药-管理维护分页列表查询")
	@ApiOperation(value="中草药-管理维护分页列表查询", notes="中草药-管理维护分页列表查询")
	@GetMapping(value = "/manageList")
	public Result<?> queryPageManageList(MedicalChineseDrug medicalChineseDrug,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalChineseDrug> queryWrapper = service.getQueryWrapper(medicalChineseDrug, req);
		Page<MedicalChineseDrug> page = new Page<MedicalChineseDrug>(pageNo, pageSize);
		IPage<MedicalChineseDrug> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 全部数据查询
	  * @param medicalChineseDrug
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "中草药维护-列表全选")
	 @ApiOperation(value="中草药维护-列表全选", notes="中草药维护-列表全选")
	 @RequestMapping(value = "/selectAll")
	 public Result<?> selectAll(MedicalChineseDrug medicalChineseDrug,HttpServletRequest req) {
		 QueryWrapper<MedicalChineseDrug> queryWrapper = QueryGenerator.initQueryWrapper(medicalChineseDrug, req.getParameterMap());
		 queryWrapper.select("ID", "CODE", "NAME");
		 List<MedicalChineseDrug> list = service.list(queryWrapper);
		 List<Map<String, Object>> mapList = BeanUtil.objectsToMaps(list);
		 list.clear();
		 return Result.ok(mapList);
	 }

	/**
	 * 添加
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "中草药-添加")
	@ApiOperation(value="中草药-添加", notes="中草药-添加")
	@PostMapping(value = "/add")
	@RequiresPermissions("basicDataManage:add")
	public Result<?> add(@RequestBody MedicalChineseDrug bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String id = bean.getId();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
		if(StringUtils.isNotBlank(id)){//修改(新增待审核状态)
			service.onlyUpdateMedicalChineseDrug(bean);
			return Result.ok("修改成功！");
		}else{//第一次新增
			String medicalChineseDrugId = IdUtils.uuid();
        	bean.setId(medicalChineseDrugId);
        	bean.setCreateTime(new Date());
        	bean.setState(MedicalAuditLogConstants.STATE_DSX);//待生效
        	bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);
        	bean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
			service.saveMedicalChineseDrug(bean);
		}
		return Result.ok("添加成功！");
	}

	/**
	 * 修改
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "中草药-修改")
	@ApiOperation(value="中草药-修改", notes="中草药-修改")
	@PutMapping(value = "/edit")
	@RequiresPermissions("basicDataManage:edit")
	public Result<?> edit(@RequestBody MedicalChineseDrug bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setUpdateTime(new Date());
		service.updateMedicalChineseDrug(bean);
		return Result.ok("修改成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "中草药-通过id查询")
	@ApiOperation(value="中草药-通过id查询", notes="中草药-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalChineseDrug medicalChineseDrug = service.getById(id);
		return Result.ok(medicalChineseDrug);
	}

	/**
	 * 判断code是否重复
	 * @param request
	 * @param code
	 * @param id
	 * @return
	 */
   	@AutoLog(value = "中草药-判断code是否重复 ")
	@ApiOperation(value="中草药-判断code是否重复 ", notes="中草药-判断code是否重复 ")
	@GetMapping(value = "/isExistName")
   	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="code",required=true)String code,String id){
    	boolean flag = service.isExistName(code,id);
   		return Result.ok(flag);
   	}

	@AutoLog(value = "删除操作")
	@ApiOperation(value="删除操作", notes="删除操作")
	@PutMapping(value = "/delMedicalChineseDrug")
	@RequiresPermissions("basicDataManage:del")
	public Result<?> delMedicalChineseDrug(@RequestBody MedicalChineseDrug bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setDeleteTime(new Date());
		service.delMedicalChineseDrug(bean);
		return Result.ok("删除操作成功");
	}

	@AutoLog(value = "全部删除操作")
	@ApiOperation(value="全部删除操作", notes="全部删除操作")
	@GetMapping(value = "/delAllMedicalChineseDrug")
	@RequiresPermissions("basicDataManage:delAll")
   	public Result<?> delAllMedicalChineseDrug(HttpServletRequest req,MedicalChineseDrug medicalChineseDrug) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalChineseDrug> queryWrapper = service.getQueryWrapper(medicalChineseDrug, req);
			MedicalChineseDrug bean = new MedicalChineseDrug();
			bean.setDeleteReason(req.getParameter("deleteReason1"));
			bean.setDeleteStaffName(user.getRealname());
			bean.setDeleteStaff(user.getRealname());
			bean.setDeleteTime(new Date());
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			//service.delAllMedicalChineseDrug(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_CHINESE_DRUG","中草药全部删除",bean, queryWrapper,
					(b,q)->{
						try {
							return this.service.delAllMedicalChineseDrug(q, (MedicalChineseDrug) b);
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
	 * @param medicalChineseDrug
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "查询数据条数")
	@ApiOperation(value="查询数据条数", notes="查询数据条数")
	@GetMapping(value = "/getDataCount")
	public Result<?> getDataCount(MedicalChineseDrug medicalChineseDrug,HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalChineseDrug> queryWrapper = service.getQueryWrapper(medicalChineseDrug, req);
		int count = service.count(queryWrapper);
		return Result.ok(count);
	}

	@AutoLog(value = "一键清理")
   	@ApiOperation(value="一键清理", notes="一键清理")
    @GetMapping(value = "/saveCleanMedicalChineseDrug")
	@RequiresPermissions("basicDataManage:clean")
   	public Result<?> saveCleanMedicalChineseDrug(HttpServletRequest req,MedicalChineseDrug medicalChineseDrug) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalChineseDrug> queryWrapper = service.getQueryWrapper(medicalChineseDrug, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			//service.saveCleanMedicalChineseDrug(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_CHINESE_DRUG","中草药一键清理",bean, queryWrapper,
					(b,q)->{
						return this.service.saveCleanMedicalChineseDrug(q, (MedicalAuditLog) b);
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
   	public Result<?> saveUndoAllMedicalAuditLog(HttpServletRequest req,MedicalChineseDrug medicalChineseDrug) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalChineseDrug> queryWrapper = service.getQueryWrapper(medicalChineseDrug, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			List list = service.list(queryWrapper);
			//auditService.saveUndoAllMedicalAuditLog(bean,list);
			return importTaskService.saveBatchTask("MEDICAL_CHINESE_DRUG","中草药全部撤销",bean, list,
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
	public Result<?> saveAuditAllMedicalAuditLog(HttpServletRequest req, MedicalChineseDrug medicalChineseDrug)
			throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalChineseDrug> queryWrapper = service.getQueryWrapper(medicalChineseDrug, req);
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
			return importTaskService.saveBatchTask("MEDICAL_CHINESE_DRUG","中草药批量审核",bean, list,
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
	 * @param medicalChineseDrug
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, MedicalChineseDrug medicalChineseDrug) {
		return super.exportXls(request, medicalChineseDrug, MedicalChineseDrug.class, "中草药信息表");
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
			return importTaskService.saveImportTask("MEDICAL_CHINESE_DRUG","中草药导入",file,user,
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
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalChineseDrug bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "中草药维护_导出";
		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		String tableName = req.getParameter("tableName");
		QueryWrapper<MedicalChineseDrug> queryWrapper = service.getQueryWrapper(bean, req);
		int count = service.count(queryWrapper);
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_CHINESE_DRUG";
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
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalChineseDrug bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "中草药维护_导出";
		}
		String tableName = req.getParameter("tableName");
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_CHINESE_DRUG";
 		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		// 选中数据
		String selections = req.getParameter("selections");
		if (StringUtils.isNotEmpty(selections)) {
			bean.setId(selections);
		}
		QueryWrapper<MedicalChineseDrug> queryWrapper = service.getQueryWrapper(bean, req);
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
	  * 替换中草药
	  * @param request
	  * @param code
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "中草药-替换中草药")
	 @ApiOperation(value="中草药-替换中草药", notes="中草药-替换中草药")
	 @GetMapping(value = "/replaceData")
	 public Result<?> replaceData(HttpServletRequest request,@RequestParam(name="code",required=true)String code,@RequestParam(name="id",required=true)String id)throws Exception{
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 MedicalChineseDrug oldBean = service.getById(id);//需要替换的数据
		 if(oldBean==null){
			 return Result.error("参数异常");
		 }
		 //设置需要替换的设置失效
		 oldBean.setState(MedicalAuditLogConstants.STATE_WX);
		 service.updateById(oldBean);
		 //调用替换逻辑
		 AbsDictMergeHandleFactory factory = new HerbDrugDictMergeHandleFactory(code,oldBean.getCode());
		 List<DictMergeVO> result  = factory.merge();
		 return Result.ok("替换成功");
	 }

}
