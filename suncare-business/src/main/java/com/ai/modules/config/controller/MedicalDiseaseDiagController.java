package com.ai.modules.config.controller;

import com.ai.common.utils.BeanUtil;
import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.*;
import com.ai.modules.config.service.*;
import com.ai.modules.config.vo.MedicalDiseaseDiagToGroup;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.AbsDictMergeHandleFactory;
import com.ai.modules.medical.handle.DiseaseDictMergeHandleFactory;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

 /**
 * @Description: ICD国际疾病维护
 * @Author: jeecg-boot
 * @Date:   2019-12-27
 * @Version: V1.0
 */
@Slf4j
@Api(tags="ICD国际疾病维护")
@RestController
@RequestMapping("/config/medicalDiseaseDiag")
public class MedicalDiseaseDiagController extends JeecgController<MedicalDiseaseDiag, IMedicalDiseaseDiagService> {
	@Autowired
	private IMedicalDiseaseDiagService service;

	@Autowired
	private IMedicalAuditLogService serviceLog;

	 @Autowired
	 @Qualifier("diseaseDiagAuditService")
	 ICommonAuditService auditService;

	 @Autowired
	 private IMedicalDiseaseGroupService medicalDiseaseGroupService;
	 @Autowired
	 private IMedicalDiseaseGroupItemService medicalDiseaseGroupItemService;

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath;

	 @Autowired
	 IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalDiseaseDiag
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "疾病诊断-分页列表查询")
	@ApiOperation(value="疾病诊断-分页列表查询", notes="疾病诊断-分页列表查询")
	@RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
	public Result<?> queryPageList(MedicalDiseaseDiag medicalDiseaseDiag,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = QueryGenerator.initQueryWrapper(medicalDiseaseDiag, req.getParameterMap());
		String typeCode_1 = req.getParameter("typeCode_1");
		if(StringUtils.isNotBlank(typeCode_1)&&!"0".equals(typeCode_1)){
			queryWrapper.and(wrapper -> wrapper.eq("TYPE1_CODE", typeCode_1).or().eq("TYPE2_CODE", typeCode_1).or().
												eq("TYPE3_CODE", typeCode_1).or().eq("TYPE4_CODE", typeCode_1).or().eq("TYPE_CODE", typeCode_1));
		}
		Page<MedicalDiseaseDiag> page = new Page<MedicalDiseaseDiag>(pageNo, pageSize);
		IPage<MedicalDiseaseDiag> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 管理维护分页列表查询
	 *
	 * @param medicalDiseaseDiag
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "ICD国际疾病-管理维护分页列表查询")
	@ApiOperation(value="ICD国际疾病-管理维护分页列表查询", notes="ICD国际疾病-管理维护分页列表查询")
	@GetMapping(value = "/manageList")
	public Result<?> queryPageManageList(MedicalDiseaseDiag medicalDiseaseDiag,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = service.getQueryWrapper(medicalDiseaseDiag, req);
		Page<MedicalDiseaseDiag> page = new Page<MedicalDiseaseDiag>(pageNo, pageSize);
		IPage<MedicalDiseaseDiag> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 全部数据查询
	  * @param medicalDiseaseDiag
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "ICD国际疾病-列表全选")
	 @ApiOperation(value="ICD国际疾病-列表全选", notes="ICD国际疾病-列表全选")
	 @RequestMapping(value = "/selectAll")
	 public Result<?> selectAll(MedicalDiseaseDiag medicalDiseaseDiag, HttpServletRequest req) {
		 QueryWrapper<MedicalDiseaseDiag> queryWrapper = QueryGenerator.initQueryWrapper(medicalDiseaseDiag, req.getParameterMap());
		 queryWrapper.select("ID", "CODE", "NAME");
		 List<MedicalDiseaseDiag> list = service.list(queryWrapper);
		 List<Map<String, Object>> mapList = BeanUtil.objectsToMaps(list);
		 list.clear();
		 return Result.ok(mapList);
	 }

	 /**
	  * 通过codes查询
	  *
	  * @param codes
	  * @return
	  */
	 @AutoLog(value = "ICD国际疾病-通过code查询")
	 @ApiOperation(value="ICD国际疾病-通过code查询", notes="ICD国际疾病-通过code查询")
	 @GetMapping(value = "/queryByCodes")
	 public Result<?> queryByCodes(@RequestParam(name="codes") String codes) {
		 List<String> codeList = Arrays.asList(codes.split(","));
		 List<MedicalDiseaseDiag> list = service.list(new QueryWrapper<MedicalDiseaseDiag>().in("CODE",codeList));
		 return Result.ok(list);
	 }
	/**
	 * 添加
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "ICD国际疾病-添加")
	@ApiOperation(value="ICD国际疾病-添加", notes="ICD国际疾病-添加")
	@PostMapping(value = "/add")
	@RequiresPermissions("basicDataManage:add")
	public Result<?> add(@RequestBody MedicalDiseaseDiag bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String id = bean.getId();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
		if(StringUtils.isNotBlank(id)){//修改(新增待审核状态)
			service.onlyUpdateMedicalDiseaseDiag(bean);
			return Result.ok("修改成功！");
		}else{//第一次新增
			String medicalDiseaseDiagId = IdUtils.uuid();
        	bean.setId(medicalDiseaseDiagId);
        	bean.setCreateTime(new Date());
        	bean.setState(MedicalAuditLogConstants.STATE_DSX);//待生效
        	bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);
        	bean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
			service.saveMedicalDiseaseDiag(bean);
		}
		return Result.ok("添加成功！");
	}

	/**
	 * 修改
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "ICD国际疾病-修改")
	@ApiOperation(value="ICD国际疾病-修改", notes="ICD国际疾病-修改")
	@PutMapping(value = "/edit")
	@RequiresPermissions("basicDataManage:edit")
	public Result<?> edit(@RequestBody MedicalDiseaseDiag bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setUpdateTime(new Date());
		service.updateMedicalDiseaseDiag(bean);
		return Result.ok("修改成功!");
	}

	/**
	 * 批量导入新的疾病组
	 *
	 * @return
	 */
	@AutoLog(value = "ICD国际疾病-批量导入新的疾病组")
	@ApiOperation(value="ICD国际疾病-批量导入新的疾病组", notes="ICD国际疾病-批量导入新的疾病组")
	@PutMapping(value = "/addToNewGroup")
	public Result<?> addToNewGroup(@RequestBody MedicalDiseaseDiagToGroup groupDto) {
		MedicalDiseaseGroup group = groupDto.getGroup();
		MedicalDiseaseGroup diseaseGroup = medicalDiseaseGroupService.getOne(new QueryWrapper<MedicalDiseaseGroup>().eq("GROUP_CODE",group.getGroupCode()));
		if(diseaseGroup != null){
			return Result.error("疾病组编码已存在");
		}

		MedicalDiseaseDiag medicalDiseaseDiag = groupDto.getParams();
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = QueryGenerator.initQueryWrapper(medicalDiseaseDiag, new HashMap<>());
		String groupId = IdUtils.uuid();

		List<MedicalDiseaseDiag> list = service.list(queryWrapper);

		if(list.size() == 0){
			return Result.error("过滤后的疾病项目不能为空");
		}
		AtomicLong i = new AtomicLong();
		List<MedicalDiseaseGroupItem> groupItemList = list.stream().map(item -> {
			MedicalDiseaseGroupItem diseaseGroupItem = new MedicalDiseaseGroupItem();
			diseaseGroupItem.setGroupId(groupId);
			diseaseGroupItem.setIsOrder(i.getAndIncrement());
			diseaseGroupItem.setCode(item.getCode());
			diseaseGroupItem.setValue(item.getName());
			return diseaseGroupItem;
		}).collect(Collectors.toList());

		group.setGroupId(groupId);
		medicalDiseaseGroupService.save(group);
		medicalDiseaseGroupItemService.saveBatch(groupItemList);
		return Result.ok("导入成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "ICD国际疾病-通过id查询")
	@ApiOperation(value="ICD国际疾病-通过id查询", notes="ICD国际疾病-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDiseaseDiag medicalDiseaseDiag = service.getById(id);
		return Result.ok(medicalDiseaseDiag);
	}

	/**
	 * 判断code是否重复
	 * @param request
	 * @param code
	 * @param id
	 * @return
	 */
   	@AutoLog(value = "ICD国际疾病-判断code是否重复 ")
	@ApiOperation(value="ICD国际疾病-判断code是否重复 ", notes="ICD国际疾病-判断code是否重复 ")
	@GetMapping(value = "/isExistName")
   	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="code",required=true)String code,String id){
    	boolean flag = service.isExistName(code,id);
   		return Result.ok(flag);
   	}

	@AutoLog(value = "删除操作")
	@ApiOperation(value="删除操作", notes="删除操作")
	@PutMapping(value = "/delMedicalDiseaseDiag")
	@RequiresPermissions("basicDataManage:del")
	public Result<?> delMedicalDiseaseDiag(@RequestBody MedicalDiseaseDiag bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setDeleteTime(new Date());
		service.delMedicalDiseaseDiag(bean);
		return Result.ok("删除操作成功");
	}

	@AutoLog(value = "全部删除操作")
	@ApiOperation(value="全部删除操作", notes="全部删除操作")
	@GetMapping(value = "/delAllMedicalDiseaseDiag")
	@RequiresPermissions("basicDataManage:delAll")
   	public Result<?> delAllMedicalDiseaseDiag(HttpServletRequest req,MedicalDiseaseDiag medicalDiseaseDiag) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalDiseaseDiag> queryWrapper = service.getQueryWrapper(medicalDiseaseDiag, req);
			MedicalDiseaseDiag bean = new MedicalDiseaseDiag();
			bean.setDeleteReason(req.getParameter("deleteReason1"));
			bean.setDeleteStaffName(user.getRealname());
			bean.setDeleteStaff(user.getRealname());
			bean.setDeleteTime(new Date());
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			//service.delAllMedicalDiseaseDiag(queryWrapper,medicalDiseaseDiag);
			return importTaskService.saveBatchTask("MEDICAL_DISEASE_DIAG","ICD国际疾病信息全部删除",bean, queryWrapper,
					(b,q)->{
						try {
							return this.service.delAllMedicalDiseaseDiag(q, (MedicalDiseaseDiag) b);
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
	 * @param medicalDiseaseDiag
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "查询数据条数")
	@ApiOperation(value="查询数据条数", notes="查询数据条数")
	@GetMapping(value = "/getDataCount")
	public Result<?> getDataCount(MedicalDiseaseDiag medicalDiseaseDiag,HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = service.getQueryWrapper(medicalDiseaseDiag, req);
		int count = service.count(queryWrapper);
		return Result.ok(count);
	}

	@AutoLog(value = "一键清理")
   	@ApiOperation(value="一键清理", notes="一键清理")
    @GetMapping(value = "/saveCleanMedicalDiseaseDiag")
	@RequiresPermissions("basicDataManage:clean")
   	public Result<?> saveCleanMedicalDiseaseDiag(HttpServletRequest req,MedicalDiseaseDiag medicalDiseaseDiag) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalDiseaseDiag> queryWrapper = service.getQueryWrapper(medicalDiseaseDiag, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
//			service.saveCleanMedicalDiseaseDiag(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_DISEASE_DIAG","ICD国际疾病信息一键清理",bean, queryWrapper,
					(b,q)->{
						return this.service.saveCleanMedicalDiseaseDiag(q, (MedicalAuditLog) b);
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
   	public Result<?> saveUndoAllMedicalAuditLog(HttpServletRequest req,MedicalDiseaseDiag medicalDiseaseDiag) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalDiseaseDiag> queryWrapper = service.getQueryWrapper(medicalDiseaseDiag, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			List list = service.list(queryWrapper);
			//auditService.saveUndoAllMedicalAuditLog(bean,list);
			return importTaskService.saveBatchTask("MEDICAL_DISEASE_DIAG","ICD国际疾病信息全部撤销",bean, list,
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
	public Result<?> saveAuditAllMedicalAuditLog(HttpServletRequest req, MedicalDiseaseDiag medicalDiseaseDiag)
			throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalDiseaseDiag> queryWrapper = service.getQueryWrapper(medicalDiseaseDiag, req);
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
			return importTaskService.saveBatchTask("MEDICAL_DISEASE_DIAG","ICD国际疾病信息批量审核",bean, list,
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
	 * @param medicalDiseaseDiag
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, MedicalDiseaseDiag medicalDiseaseDiag) {
		return super.exportXls(request, medicalDiseaseDiag, MedicalDiseaseDiag.class, "ICD国际疾病信息表");
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
			return importTaskService.saveImportTask("MEDICAL_DISEASE_DIAG","ICD国际疾病信息导入",file,user,
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
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalDiseaseDiag bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "ICD国际疾病_导出";
		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		String tableName = req.getParameter("tableName");
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = service.getQueryWrapper(bean, req);
		int count = service.count(queryWrapper);
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_DISEASE_DIAG";
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
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalDiseaseDiag bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "ICD国际疾病_导出";
		}
		String tableName = req.getParameter("tableName");
		if(StringUtils.isBlank(tableName)) {
			tableName  ="MEDICAL_DISEASE_DIAG";
 		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		// 选中数据
		String selections = req.getParameter("selections");
		if (StringUtils.isNotEmpty(selections)) {
			bean.setId(selections);
		}
		QueryWrapper<MedicalDiseaseDiag> queryWrapper = service.getQueryWrapper(bean, req);
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
	  * 替换疾病
	  * @param request
	  * @param code
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "ICD国际疾病-替换疾病")
	 @ApiOperation(value="ICD国际疾病-替换疾病 ", notes="ICD国际疾病-替换疾病")
	 @GetMapping(value = "/replaceData")
	 public Result<?> replaceData(HttpServletRequest request,@RequestParam(name="code",required=true)String code,@RequestParam(name="id",required=true)String id)throws Exception{
	 	LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
	 	MedicalDiseaseDiag oldBean = service.getById(id);//需要替换的数据
		 if(oldBean==null){
			 return Result.error("参数异常");
		 }
		 //设置需要替换的设置失效
		 oldBean.setState(MedicalAuditLogConstants.STATE_WX);
		 service.updateById(oldBean);
		 //调用替换逻辑
		 AbsDictMergeHandleFactory factory = new DiseaseDictMergeHandleFactory(code,oldBean.getCode());
		 List<DictMergeVO> result  = factory.merge();
		 return Result.ok("替换成功");
	 }

}
