package com.ai.modules.config.controller;

import com.ai.common.utils.BeanUtil;
import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.MD5Util;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.entity.MedicalOrgan;
import com.ai.modules.config.service.ICommonAuditService;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.service.IMedicalOrganService;
import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.ai.modules.engine.util.ObjectCacheWithFile;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.AbsDictMergeHandleFactory;
import com.ai.modules.medical.handle.OrgMergeHandleFactory;
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
import org.jeecg.common.util.RedisUtil;
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
import java.util.stream.Collectors;

/**
 * @Description: 医疗机构
 * @Author: jeecg-boot
 * @Date:   2019-12-31
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医疗机构")
@RestController
@RequestMapping("/config/medicalOrgan")
public class MedicalOrganController extends JeecgController<MedicalOrgan, IMedicalOrganService> {
	@Autowired
	private IMedicalOrganService service;

	@Autowired
	private IMedicalAuditLogService serviceLog;

	 @Autowired
	 @Qualifier("organAuditService")
	 ICommonAuditService auditService;

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath;

	@Autowired
    private RedisUtil redisUtil;

	 @Autowired
	 IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalOrgan
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医疗机构信息-分页列表查询")
	@ApiOperation(value="医疗机构信息-分页列表查询", notes="医疗机构信息-分页列表查询")
	@RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
	public Result<?> queryPageList(MedicalOrgan medicalOrgan,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalOrgan> queryWrapper = QueryGenerator.initQueryWrapper(medicalOrgan, req.getParameterMap());
		//省市县乡镇
		String typeCode = req.getParameter("typeCode");
		if(StringUtils.isNotBlank(typeCode)) {
			queryWrapper.and(wrapper -> wrapper
					.eq("PROVINCE_CODE", typeCode)
					.or().eq("CITY_CODE", typeCode)
					.or().eq("COUNTY_CODE", typeCode)
					.or().eq("TOWN_CODE", typeCode)
					.or().eq("VILLAGE_CODE", typeCode));
		}
		Page<MedicalOrgan> page = new Page<MedicalOrgan>(pageNo, pageSize);
		IPage<MedicalOrgan> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 @AutoLog(value = "医疗机构信息-列表全选")
	 @ApiOperation(value = "医疗机构信息-列表全选", notes = "医疗机构信息-列表全选")
	 @GetMapping(value = "/selectAll")
	 public Result<?> selectAll(MedicalOrgan medicalOrgan,
								HttpServletRequest req) {
		 QueryWrapper<MedicalOrgan> queryWrapper = QueryGenerator.initQueryWrapper(medicalOrgan, req.getParameterMap());
		 queryWrapper.select("ID", "CODE", "NAME");
		 List<MedicalOrgan> list = service.list(queryWrapper);
		 List<Map<String, Object>> mapList = BeanUtil.objectsToMaps(list);
		 list.clear();
		 return Result.ok(mapList);
	 }

	 @AutoLog(value = "医疗机构信息-列表全选-关联数据")
	 @ApiOperation(value = "医疗机构信息-列表全选-关联数据", notes = "医疗机构信息-列表全选-关联数据")
	 @GetMapping(value = "/selectAllInData")
	 public Result<?> selectAllInData(MedicalOrgan medicalOrgan,
								HttpServletRequest req) {
		 QueryWrapper<MedicalOrgan> queryWrapper = QueryGenerator.initQueryWrapper(medicalOrgan, req.getParameterMap());
		 //省市县乡镇
		 String typeCode = req.getParameter("typeCode");
		 if(StringUtils.isNotBlank(typeCode)) {
			 queryWrapper.and(wrapper -> wrapper
					 .eq("PROVINCE_CODE", typeCode)
					 .or().eq("CITY_CODE", typeCode)
					 .or().eq("COUNTY_CODE", typeCode)
					 .or().eq("TOWN_CODE", typeCode)
					 .or().eq("VILLAGE_CODE", typeCode));
		 }
		 String nullField = req.getParameter("nullField");
		 if(StringUtils.isNotBlank(nullField)){
			 if(StringUtils.isBlank(typeCode)){//省空值或者00000
				 queryWrapper.and(wrapper -> wrapper
						 .eq("PROVINCE_CODE", "000000")
						 .or().isNull("PROVINCE_CODE"));
			 }else{
				 queryWrapper.isNull(nullField);
			 }
		 }
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 List<MedicalCodeNameVO> codeNamelist = service.listMasterInfoJoinSelectMaps(queryWrapper, user.getDataSource());
		 List<Map<String,Object>> list = codeNamelist.stream().map(item->{
		 	Map<String,Object> map = new HashMap<>();
		 	map.put("ID",item.getId());
		 	map.put("CODE",item.getCode());
		 	map.put("NAME",item.getName());
		 	return map;
		 }).collect(Collectors.toList());
		 return Result.ok(list);
	 }


	/**
	 * 管理维护分页列表查询
	 *
	 * @param medicalOrgan
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "医疗机构信息-管理维护分页列表查询")
	@ApiOperation(value="医疗机构信息-管理维护分页列表查询", notes="医疗机构信息-管理维护分页列表查询")
	@RequestMapping(value = "/manageList",method = { RequestMethod.GET,RequestMethod.POST })
//	@GetMapping(value = "/manageList")
	public Result<?> queryPageManageList(MedicalOrgan medicalOrgan,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalOrgan> queryWrapper = service.getQueryWrapper(medicalOrgan, req);
		Page<MedicalOrgan> page = new Page<MedicalOrgan>(pageNo, pageSize);
		IPage<MedicalOrgan> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	@AutoLog(value = "医疗机构信息-分页列表查询-关联数据")
	@ApiOperation(value="医疗机构信息-分页列表查询-关联数据", notes="医疗机构信息-分页列表查询-关联数据")
	@RequestMapping(value = "/listInData",method = { RequestMethod.GET,RequestMethod.POST })
	public Result<?> listInData(MedicalOrgan medicalOrgan,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalOrgan> queryWrapper = QueryGenerator.initQueryWrapper(medicalOrgan, req.getParameterMap());
		Page<MedicalOrgan> page = new Page<>(pageNo, pageSize);
		//省市县乡镇
		String typeCode = req.getParameter("typeCode");
		if(StringUtils.isNotBlank(typeCode)) {
			queryWrapper.and(wrapper -> wrapper
					.eq("PROVINCE_CODE", typeCode)
					.or().eq("CITY_CODE", typeCode)
					.or().eq("COUNTY_CODE", typeCode)
					.or().eq("TOWN_CODE", typeCode)
					.or().eq("VILLAGE_CODE", typeCode));
		}
		String nullField = req.getParameter("nullField");
		if(StringUtils.isNotBlank(nullField)){
			if(StringUtils.isBlank(typeCode)){//省空值或者00000
				queryWrapper.and(wrapper -> wrapper
						.eq("PROVINCE_CODE", "000000")
						.or().isNull("PROVINCE_CODE"));
			}else{
				queryWrapper.isNull(nullField);
			}
		}
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		IPage<MedicalOrgan> pageList = service.pageByMasterInfoJoin(page, queryWrapper, user.getDataSource());
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "医疗机构信息-添加")
	@ApiOperation(value="医疗机构信息-添加", notes="医疗机构信息-添加")
	@PostMapping(value = "/add")
	@RequiresPermissions("basicDataManage:add")
	public Result<?> add(@RequestBody MedicalOrgan bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String code = StringUtils.isNotBlank(bean.getId())?bean.getCode():"";
		boolean flag = service.isExistOrg(bean,code);
		if(flag){
			return Result.error("该医疗机构名称或医疗机构曾用名或医疗机构地址已存在，请重新输入");
		}
		String id = bean.getId();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
		if(StringUtils.isNotBlank(id)){//修改(新增待审核状态)
			service.onlyUpdateMedicalOrgan(bean);
			return Result.ok("修改成功！");
		}else{//第一次新增
			String medicalOrganId = IdUtils.uuid();
        	bean.setId(medicalOrganId);
        	bean.setCreateTime(new Date());
        	bean.setState(MedicalAuditLogConstants.STATE_DSX);//待生效
        	bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);
        	bean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
			service.saveMedicalOrgan(bean);
		}
		return Result.ok("添加成功！");
	}

	/**
	 * 修改
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "医疗机构信息-修改")
	@ApiOperation(value="医疗机构信息-修改", notes="医疗机构信息-修改")
	@PutMapping(value = "/edit")
	@RequiresPermissions("basicDataManage:edit")
	public Result<?> edit(@RequestBody MedicalOrgan bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		boolean flag = service.isExistOrg(bean,bean.getCode());
		if(flag){
			return Result.error("该医疗机构已存在，请重新输入");
		}
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setUpdateTime(new Date());
		service.updateMedicalOrgan(bean);
		return Result.ok("修改成功!");
	}

     /**
      * 通过id查询
      *
      * @param code
      * @return
      */
     @AutoLog(value = "医疗机构信息-通过code查询")
     @ApiOperation(value="医疗机构信息-通过code查询", notes="医疗机构信息-通过code查询")
     @GetMapping(value = "/queryByCode")
     public Result<?> queryByCode(@RequestParam(name="code",required=true) String code) {
         MedicalOrgan medicalOrgan = service.getOne(new QueryWrapper<MedicalOrgan>().eq("CODE",code));
         return Result.ok(medicalOrgan);
     }

	 /**
	  * 通过codes查询
	  *
	  * @param codes
	  * @return
	  */
	 @AutoLog(value = "医疗机构信息-通过codes查询")
	 @ApiOperation(value="医疗机构信息-通过codes查询", notes="医疗机构信息-通过codes查询")
	 @GetMapping(value = "/queryByCodes")
	 public Result<?> queryByCodes(@RequestParam(name="codes") String codes) {
		 //先尝试从缓存获取
		 String cacheType= "queryByCodes";
		 String cacheName = MD5Util.getMD5(codes);
		 int expireSecond = 10 *60; //10分钟
		 List<MedicalOrgan>  cacheObjectList =(List<MedicalOrgan> ) ObjectCacheWithFile.getObjectFromFile(cacheType, cacheName, expireSecond);

		 if(cacheObjectList != null) {
			 return Result.ok(cacheObjectList);
		 }

		 List<String> codeList = Arrays.asList(codes.split(","));
		 List<MedicalOrgan> list = service.list(new QueryWrapper<MedicalOrgan>().in("CODE",codeList));

		 //放入缓存
		 if(list != null) {
			 ObjectCacheWithFile.saveObjectToFile(cacheType, cacheName, list);
		 }

		 return Result.ok(list);
	 }

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗机构信息-通过id查询")
	@ApiOperation(value="医疗机构信息-通过id查询", notes="医疗机构信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalOrgan medicalOrgan = service.getById(id);
		return Result.ok(medicalOrgan);
	}

	/**
	 * 判断code是否重复
	 * @param request
	 * @param code
	 * @param id
	 * @return
	 */
   	@AutoLog(value = "医疗机构信息-判断code是否重复 ")
	@ApiOperation(value="医疗机构信息-判断code是否重复 ", notes="医疗机构信息-判断code是否重复 ")
	@GetMapping(value = "/isExistName")
   	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="code",required=true)String code,String id){
    	boolean flag = service.isExistName(code,id);
   		return Result.ok(flag);
   	}

	@AutoLog(value = "删除操作")
	@ApiOperation(value="删除操作", notes="删除操作")
	@PutMapping(value = "/delMedicalOrgan")
	@RequiresPermissions("basicDataManage:del")
	public Result<?> delMedicalOrgan(@RequestBody MedicalOrgan bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setDeleteTime(new Date());
		service.delMedicalOrgan(bean);
		return Result.ok("删除操作成功");
	}

	@AutoLog(value = "全部删除操作")
	@ApiOperation(value="全部删除操作", notes="全部删除操作")
	@GetMapping(value = "/delAllMedicalOrgan")
	@RequiresPermissions("basicDataManage:delAll")
   	public Result<?> delAllMedicalOrgan(HttpServletRequest req,MedicalOrgan medicalOrgan) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOrgan> queryWrapper = service.getQueryWrapper(medicalOrgan, req);
			MedicalOrgan bean = new MedicalOrgan();
			bean.setDeleteReason(req.getParameter("deleteReason1"));
			bean.setDeleteStaffName(user.getRealname());
			bean.setDeleteStaff(user.getRealname());
			bean.setDeleteTime(new Date());
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			//service.delAllMedicalOrgan(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_ORGAN","医疗机构信息全部删除",bean, queryWrapper,
					(b,q)->{
						try {
							return this.service.delAllMedicalOrgan(q, (MedicalOrgan) b);
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
	 * @param medicalOrgan
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "查询数据条数")
	@ApiOperation(value="查询数据条数", notes="查询数据条数")
	@GetMapping(value = "/getDataCount")
	public Result<?> getDataCount(MedicalOrgan medicalOrgan,HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalOrgan> queryWrapper = service.getQueryWrapper(medicalOrgan, req);
		int count = service.count(queryWrapper);
		return Result.ok(count);
	}

	@AutoLog(value = "一键清理")
   	@ApiOperation(value="一键清理", notes="一键清理")
    @GetMapping(value = "/saveCleanMedicalOrgan")
	@RequiresPermissions("basicDataManage:clean")
   	public Result<?> saveCleanMedicalOrgan(HttpServletRequest req,MedicalOrgan medicalOrgan) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOrgan> queryWrapper = service.getQueryWrapper(medicalOrgan, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
//			service.saveCleanMedicalOrgan(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_ORGAN","医疗机构信息一键清理",bean, queryWrapper,
					(b,q)->{
						return this.service.saveCleanMedicalOrgan(q, (MedicalAuditLog) b);
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
   	public Result<?> saveUndoAllMedicalAuditLog(HttpServletRequest req,MedicalOrgan medicalOrgan) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOrgan> queryWrapper = service.getQueryWrapper(medicalOrgan, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			List list = service.list(queryWrapper);
			//auditService.saveUndoAllMedicalAuditLog(bean,list);
			return importTaskService.saveBatchTask("MEDICAL_ORGAN","医疗机构信息全部撤销",bean, list,
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
	public Result<?> saveAuditAllMedicalAuditLog(HttpServletRequest req, MedicalOrgan medicalOrgan)
			throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOrgan> queryWrapper = service.getQueryWrapper(medicalOrgan, req);
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
			return importTaskService.saveBatchTask("MEDICAL_ORGAN","医疗机构信息批量审核",bean, list,
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
	 * @param medicalOrgan
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, MedicalOrgan medicalOrgan) {
		return super.exportXls(request, medicalOrgan, MedicalOrgan.class, "医疗机构信息表");
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
			return importTaskService.saveImportTask("MEDICAL_ORGAN","医疗机构导入",file,user,
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
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalOrgan bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "医疗机构信息_导出";
		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		String tableName = req.getParameter("tableName");
		QueryWrapper<MedicalOrgan> queryWrapper = service.getQueryWrapper(bean, req);
		int count = service.count(queryWrapper);
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_ORGAN";
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
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalOrgan bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "医疗机构信息_导出";
		}
		String tableName = req.getParameter("tableName");
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_ORGAN";
 		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		// 选中数据
		String selections = req.getParameter("selections");
		if (StringUtils.isNotEmpty(selections)) {
			bean.setId(selections);
		}
		QueryWrapper<MedicalOrgan> queryWrapper = service.getQueryWrapper(bean, req);
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
	  * 根据parentCode和type查询数据
	  *
	  * @param parentCode
	  * @param type
	  * @return
	  * @throws Exception
	  */
	 @AutoLog(value = "根据parentCode和type查询数据")
	 @ApiOperation(value="根据parentCode和type查询数据", notes="根据parentCode和type查询数据")
	 @GetMapping(value = "/getMasterOrgTreeList")
	 public Result<?> getMasterOrgTreeList(String parentCode,String type) throws Exception {
	 	 if(StringUtils.isBlank(type)){
			 type="PROVINCE";
		 }
		 List<Map<String, Object>> list = service.getRegionList(parentCode,type);
		 return Result.ok(list);

	 }

	 /**
	  * 替换医疗机构
	  * @param request
	  * @param code
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "医疗机构信息-替换医疗机构")
	 @ApiOperation(value="医疗机构信息-替换医疗机构", notes="医疗机构信息-替换医疗机构")
	 @GetMapping(value = "/replaceData")
	 public Result<?> replaceData(HttpServletRequest request,@RequestParam(name="code",required=true)String code,@RequestParam(name="id",required=true)String id)throws Exception{
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 MedicalOrgan oldBean = service.getById(id);//需要替换的数据
		 if(oldBean==null){
			 return Result.error("参数异常");
		 }
		 //设置需要替换的设置失效
		 oldBean.setState(MedicalAuditLogConstants.STATE_WX);
		 service.updateById(oldBean);
		 //调用替换逻辑
		 AbsDictMergeHandleFactory factory = new OrgMergeHandleFactory(code,oldBean.getCode());
		 List<DictMergeVO> result  = factory.merge();
		 return Result.ok("替换成功");
	 }

	 @RequestMapping(value = "/repeat/exportExcel")
	 public void repeatExport(HttpServletResponse response, HttpServletRequest request,
			 @RequestParam(name="serialNum",required=true) String serialNum)throws Exception{
		 String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		 response.setContentType("application/octet-stream; charset=utf-8");
		 response.setHeader("Content-Disposition", "attachment; filename="+new String(("机构重复明细."+suffix).getBytes("UTF-8"), "iso-8859-1"));
		 try {
			 OutputStream os = response.getOutputStream();
			 service.exportExcel(serialNum, os, suffix);
		 } catch (Exception e) {
			throw e;
		 }
	 }
}
