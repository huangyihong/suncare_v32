package com.ai.modules.config.controller;

import com.ai.common.utils.BeanUtil;
import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.entity.MedicalOtherDict;
import com.ai.modules.config.service.*;
import com.alibaba.fastjson.JSONObject;
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
import java.util.stream.Collectors;

/**
 * @Description: 其他字典
 * @Author: jeecg-boot
 * @Date:   2019-12-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="其他字典")
@RestController
@RequestMapping("/config/medicalOtherDict")
public class MedicalOtherDictController extends JeecgController<MedicalOtherDict, IMedicalOtherDictService> {
	@Autowired
	private IMedicalOtherDictService service;

	@Autowired
	private IMedicalAuditLogService serviceLog;

	 @Autowired
	 private IMedicalDictClearService medicalDictClearService;

	 @Autowired
	 @Qualifier("otherDictAuditService")
	 ICommonAuditService auditService;

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath;

	 @Autowired
	 IMedicalImportTaskService importTaskService;


	 @AutoLog(value = "其他字典-清除数据源所有缓存")
	 @ApiOperation(value="其他字典-清除数据源所有缓存", notes="其他字典-清除数据源所有缓存")
	 @PostMapping(value = "/clearDsCache")
	 public Result<?> clearDsCache() {
		 medicalDictClearService.clearRemoteOdictCache(null);
		 return Result.ok();
	 }

	/**
	 * 分页列表查询
	 *
	 * @param medicalOtherDict
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "其他字典-分页列表查询")
	@ApiOperation(value="其他字典-分页列表查询", notes="其他字典-分页列表查询")
	@RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
	public Result<?> queryPageList(MedicalOtherDict medicalOtherDict,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalOtherDict> queryWrapper = QueryGenerator.initQueryWrapper(medicalOtherDict, req.getParameterMap());
		Page<MedicalOtherDict> page = new Page<MedicalOtherDict>(pageNo, pageSize);
		IPage<MedicalOtherDict> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 管理维护分页列表查询
	 *
	 * @param medicalOtherDict
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "其他字典-管理维护分页列表查询")
	@ApiOperation(value="其他字典-管理维护分页列表查询", notes="其他字典-管理维护分页列表查询")
	@GetMapping(value = "/manageList")
	public Result<?> queryPageManageList(MedicalOtherDict medicalOtherDict,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalOtherDict> queryWrapper = service.getQueryWrapper(medicalOtherDict, req);
		Page<MedicalOtherDict> page = new Page<MedicalOtherDict>(pageNo, pageSize);
		//queryWrapper.orderByAsc("DICT_ENAME,PARENT_CODE,IS_ORDER,CODE");
		IPage<MedicalOtherDict> pageList = service.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 全部数据查询
	  * @param medicalOtherDict
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "其他字典-列表全选")
	 @ApiOperation(value="其他字典-列表全选", notes="其他字典-列表全选")
	 @RequestMapping(value = "/selectAll")
	 public Result<?> selectAll(MedicalOtherDict medicalOtherDict, HttpServletRequest req) {
		 QueryWrapper<MedicalOtherDict> queryWrapper = QueryGenerator.initQueryWrapper(medicalOtherDict, req.getParameterMap());
		 queryWrapper.select("ID", "CODE", "VALUE");
		 List<MedicalOtherDict> list = service.list(queryWrapper);
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
	 @AutoLog(value = "其他字典-通过code查询")
	 @ApiOperation(value="其他字典-通过code查询", notes="其他字典-通过code查询")
	 @GetMapping(value = "/queryByCodes")
	 public Result<?> queryByCodes(@RequestParam(name="codes") String codes,@RequestParam(name="dictEname") String dictEname) {
		 List<String> codeList = Arrays.asList(codes.split(","));
		 List<MedicalOtherDict> list = service.list(new QueryWrapper<MedicalOtherDict>()
				 .eq("DICT_ENAME",dictEname)
				 .in("CODE",codeList));
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "其他字典-添加")
	@ApiOperation(value="其他字典-添加", notes="其他字典-添加")
	@PostMapping(value = "/add")
	@RequiresPermissions("basicDataManage:add")
	public Result<?> add(@RequestBody MedicalOtherDict bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String id = bean.getId();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
		if(StringUtils.isNotBlank(id)){//修改(新增待审核状态)
			service.onlyUpdateMedicalOtherDict(bean);
			return Result.ok("修改成功！");
		}else{//第一次新增
			String medicalOtherDictId = IdUtils.uuid();
        	bean.setId(medicalOtherDictId);
        	bean.setCreateTime(new Date());
        	bean.setState(MedicalAuditLogConstants.STATE_DSX);//待生效
        	bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);
        	bean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
			service.saveMedicalOtherDict(bean);
		}
		return Result.ok("添加成功！");
	}

	/**
	 * 修改
	 *
	 * @param bean
	 * @return
	 */
	@AutoLog(value = "其他字典-修改")
	@ApiOperation(value="其他字典-修改", notes="其他字典-修改")
	@PutMapping(value = "/edit")
	@RequiresPermissions("basicDataManage:edit")
	public Result<?> edit(@RequestBody MedicalOtherDict bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setUpdateTime(new Date());
		service.updateMedicalOtherDict(bean);
		return Result.ok("修改成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "其他字典-通过id查询")
	@ApiOperation(value="其他字典-通过id查询", notes="其他字典-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalOtherDict medicalOtherDict = service.getById(id);
		return Result.ok(medicalOtherDict);
	}

	/**
	 * 判断code是否重复
	 * @param request
	 * @param code
	 * @param id
	 * @return
	 */
   	@AutoLog(value = "其他字典-判断code是否重复 ")
	@ApiOperation(value="其他字典-判断code是否重复 ", notes="其他字典-判断code是否重复 ")
	@GetMapping(value = "/isExistName")
   	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="code",required=true)String code,@RequestParam(name="dictEname",required=true)String dictEname,String id){
    	boolean flag = service.isExistName(code,dictEname,id);
   		return Result.ok(flag);
   	}

	@AutoLog(value = "删除操作")
	@ApiOperation(value="删除操作", notes="删除操作")
	@PutMapping(value = "/delMedicalOtherDict")
	@RequiresPermissions("basicDataManage:del")
	public Result<?> delMedicalOtherDict(@RequestBody MedicalOtherDict bean) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
      	bean.setActionTime(new Date());
      	bean.setDeleteTime(new Date());
		service.delMedicalOtherDict(bean);
		return Result.ok("删除操作成功");
	}

	@AutoLog(value = "全部删除操作")
	@ApiOperation(value="全部删除操作", notes="全部删除操作")
	@GetMapping(value = "/delAllMedicalOtherDict")
	@RequiresPermissions("basicDataManage:delAll")
   	public Result<?> delAllMedicalOtherDict(HttpServletRequest req,MedicalOtherDict medicalOtherDict) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOtherDict> queryWrapper = service.getQueryWrapper(medicalOtherDict, req);
			MedicalOtherDict bean = new MedicalOtherDict();
			bean.setDeleteReason(req.getParameter("deleteReason1"));
			bean.setDeleteStaffName(user.getRealname());
			bean.setDeleteStaff(user.getRealname());
			bean.setDeleteTime(new Date());
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			//service.delAllMedicalOtherDict(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_OTHER_DICT","药品全部删除",bean, queryWrapper,
					(b,q)->{
						try {
							return this.service.delAllMedicalOtherDict(q, (MedicalOtherDict) b);
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
	 * @param medicalOtherDict
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "查询数据条数")
	@ApiOperation(value="查询数据条数", notes="查询数据条数")
	@GetMapping(value = "/getDataCount")
	public Result<?> getDataCount(MedicalOtherDict medicalOtherDict,HttpServletRequest req) throws Exception {
		QueryWrapper<MedicalOtherDict> queryWrapper = service.getQueryWrapper(medicalOtherDict, req);
		int count = service.count(queryWrapper);
		return Result.ok(count);
	}

	@AutoLog(value = "一键清理")
   	@ApiOperation(value="一键清理", notes="一键清理")
    @GetMapping(value = "/saveCleanMedicalOtherDict")
	@RequiresPermissions("basicDataManage:clean")
   	public Result<?> saveCleanMedicalOtherDict(HttpServletRequest req,MedicalOtherDict medicalOtherDict) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOtherDict> queryWrapper = service.getQueryWrapper(medicalOtherDict, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
//			service.saveCleanMedicalOtherDict(queryWrapper,bean);
			return importTaskService.saveBatchTask("MEDICAL_OTHER_DICT","药品一键清理",bean, queryWrapper,
					(b,q)->{
						return this.service.saveCleanMedicalOtherDict(q, (MedicalAuditLog) b);
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
   	public Result<?> saveUndoAllMedicalAuditLog(HttpServletRequest req,MedicalOtherDict medicalOtherDict) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOtherDict> queryWrapper = service.getQueryWrapper(medicalOtherDict, req);
			MedicalAuditLog bean = new MedicalAuditLog();
			bean.setActionReason(req.getParameter("actionReason1"));
			bean.setTableName(req.getParameter("tableName1"));
			bean.setActionStaff(user.getId());
			bean.setActionStaffName(user.getRealname());
			bean.setActionTime(new Date());
			List list = service.list(queryWrapper);
			//auditService.saveUndoAllMedicalAuditLog(bean,list);
			return importTaskService.saveBatchTask("MEDICAL_OTHER_DICT","药品全部撤销",bean, list,
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
	public Result<?> saveAuditAllMedicalAuditLog(HttpServletRequest req, MedicalOtherDict medicalOtherDict)
			throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			QueryWrapper<MedicalOtherDict> queryWrapper = service.getQueryWrapper(medicalOtherDict, req);
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
			return importTaskService.saveBatchTask("MEDICAL_OTHER_DICT","药品批量审核",bean, list,
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
	 * @param medicalOtherDict
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, MedicalOtherDict medicalOtherDict) {
		return super.exportXls(request, medicalOtherDict, MedicalOtherDict.class, "其他字典信息表");
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
			return importTaskService.saveImportTask("MEDICAL_OTHER_DICT","其他字典信息导入",file,user,
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
	 * 根据dictCname查询数据
	 *
	 * @param dictCname
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "根据dictCname或dictEname查询数据")
	@ApiOperation(value="根据dictCname或dictEname查询数据", notes="根据dictCname或dictEname查询数据")
	@GetMapping(value = "/getDictList")
	public Result<?> getDictList(String dictCname,String dictEname) throws Exception {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		if(StringUtils.isNotBlank(dictCname)) {
			queryWrapper.eq("DICT_CNAME", dictCname);
		}
		if(StringUtils.isNotBlank(dictEname)) {
			queryWrapper.eq("DICT_ENAME", dictEname);
		}
		if(StringUtils.isBlank(dictCname)&&StringUtils.isBlank(dictEname)) {
			return Result.error("参数异常");
		}
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		queryWrapper.select("ID", "CODE", "VALUE");
		List<MedicalOtherDict> list = service.list(queryWrapper);
		return Result.ok(list);
	}

	/**
	 * 根据dictCname和parentCode查询数据
	 *
	 * @param dictCname
	 * @param parentCode
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "根据dictCname或dictEname查询树形数据")
	@ApiOperation(value="根据dictCname或dictEname查询树形数据", notes="根据dictCname或dictEname查询树形数据")
	@GetMapping(value = "/getTreeList")
	public Result<?> getTreeList(String dictCname,String dictEname,String parentCode) throws Exception {
		if(StringUtils.isBlank(dictCname)&&StringUtils.isBlank(dictEname)) {
			return Result.error("参数异常");
		}
		List<MedicalOtherDict> list = getOtherDictList(dictCname, dictEname, parentCode);
		return Result.ok(list);

	}

	@AutoLog(value = "查询存在医疗机构的地区树形数据")
	@ApiOperation(value="查询存在医疗机构的地区树形数据", notes="查询存在医疗机构的地区树形数据")
	@GetMapping(value = "/getRegionInOrgTreeList")
	public Result<?> getRegionInOrgTreeList(String dictCname,String dictEname,String parentCode, String type) throws Exception {
		if(StringUtils.isBlank(dictCname)&&StringUtils.isBlank(dictEname)) {
			return Result.error("参数异常");
		}
		QueryWrapper<MedicalOtherDict> queryWrapper = getOtherDictListQuery(dictCname, dictEname, parentCode);

		List<String> typeFieldList = new ArrayList<>(Arrays.asList("","PROVINCE_CODE", "CITY_CODE", "COUNTY_CODE", "TOWN_CODE", "VILLAGE_CODE"));
		if(StringUtils.isBlank(type)){
			type = "";
		}
		String typeNew = typeFieldList.get(typeFieldList.indexOf(type) + 1);

		String inSql = "SELECT DISTINCT " + typeNew + " from medical_organ where 1 = 1";
		if(!"".equals(type)){
			inSql += " AND " + type + "='" + parentCode + "'";
		}
		queryWrapper.inSql("CODE", inSql);
		List<MedicalOtherDict> list = service.list(queryWrapper);
		for(MedicalOtherDict bean: list){
			bean.setActionType(typeNew);
		}
		return Result.ok(list);

	}

	private QueryWrapper<MedicalOtherDict> getOtherDictListQuery(String dictCname, String dictEname, String parentCode) {
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		if(StringUtils.isNotBlank(dictCname)) {
			queryWrapper.eq("DICT_CNAME", dictCname);
		}
		if(StringUtils.isNotBlank(dictEname)) {
			queryWrapper.eq("DICT_ENAME", dictEname);
		}
		if(StringUtils.isNotBlank(parentCode)) {
			queryWrapper.eq("PARENT_CODE", parentCode);
		}else {
			queryWrapper.and(wrapper -> wrapper.eq("PARENT_CODE", "0").or().eq("PARENT_CODE", "").or().isNull("PARENT_CODE"));
		}
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		queryWrapper.orderByAsc("CODE");
		return queryWrapper;
	}
	private List<MedicalOtherDict> getOtherDictList(String dictCname, String dictEname, String parentCode) {
		QueryWrapper<MedicalOtherDict> queryWrapper = getOtherDictListQuery(dictCname, dictEname, parentCode);
		List<MedicalOtherDict> list = service.list(queryWrapper);
		return list;
	}

	/**
	 * 根据dictCname和parentCode查询所有树形数据
	 *
	 * @param dictCname
	 * @param dictEname
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "根据dictCname或dictEname查询所有树形数据")
	@ApiOperation(value="根据dictCname或dictEname查询所有树形数据", notes="根据dictCname或dictEname查询所有树形数据")
	@GetMapping(value = "/getTreeAllList")
	public Result<?> getTreeAllList(String dictCname,String dictEname) throws Exception {
		if(StringUtils.isBlank(dictCname)&&StringUtils.isBlank(dictEname)) {
			return Result.error("参数异常");
		}
		List<JSONObject> list = service.getTreeAllList(dictCname,dictEname);
		return Result.ok(list);
	}


	private JSONObject getTreeNode(String dictCname,String dictEname, MedicalOtherDict bean) {
		JSONObject node = new JSONObject();
		node.put("label",bean.getValue());
		node.put("value",bean.getCode());
		List<MedicalOtherDict> childrenList = getOtherDictList(dictCname,dictEname,bean.getCode());
		if(childrenList.size()>0) {
			node.put("isLeaf",false);
			List<JSONObject> list2 = new ArrayList<JSONObject>();
			for(MedicalOtherDict bean2:childrenList) {
				JSONObject node2 = getTreeNode(dictCname,dictEname, bean2);
				list2.add(node2);
			}
			node.put("children", list2);
		}else {
			node.put("isLeaf",true);
		}
		return node;
	}

	/**
	 * 根据dictCname和code查询所有父级数据
	 *
	 * @param dictCname
	 * @param code
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "根据dictCname或dictEname和parentCode查询所有父级数据")
	@ApiOperation(value="根据dictCname或dictEname和parentCode查询所有父级数据", notes="根据dictCname或dictEname和parentCode查询所有父级数据")
	@GetMapping(value = "/getParentAllList")
	public Result<?> getParentAllList(String dictCname,String dictEname,@RequestParam(name="code",required=true)String code) throws Exception {
		if(StringUtils.isBlank(dictCname)&&StringUtils.isBlank(dictEname)) {
			return Result.error("参数异常");
		}
		List<MedicalOtherDict> parentList = new ArrayList<MedicalOtherDict>();
		getParentBean(dictCname,dictEname, code,parentList);
		return Result.ok(parentList);

	}

	private void getParentBean(String dictCname,String dictEname, String code, List<MedicalOtherDict> parentList) {
		MedicalOtherDict bean = getOtherDictBean(dictCname,dictEname, code);
		if(bean!=null) {
			parentList.add(bean);
			getParentBean(dictCname,dictEname, bean.getParentCode(),parentList);
		}
	}

	private MedicalOtherDict getOtherDictBean(String dictCname,String dictEname, String code) {
		MedicalOtherDict bean = null;
		QueryWrapper<MedicalOtherDict> queryWrapper = new QueryWrapper<MedicalOtherDict>();
		if(StringUtils.isNotBlank(dictCname)) {
			queryWrapper.eq("DICT_CNAME", dictCname);
		}
		if(StringUtils.isNotBlank(dictEname)) {
			queryWrapper.eq("DICT_ENAME", dictEname);
		}
		queryWrapper.eq("CODE", code);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOtherDict> list = service.list(queryWrapper);
		if(list.size()>0) {
			bean = list.get(0);
		}
		return bean;
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
	public Result<?> exportExcelByThread(HttpServletRequest req, MedicalOtherDict bean) throws Exception {
    	Result<?> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
    	String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "其他字典合集_导出";
		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;
		String tableName = req.getParameter("tableName");
		QueryWrapper<MedicalOtherDict> queryWrapper = service.getQueryWrapper(bean, req);
		int count = service.count(queryWrapper);
        if(StringUtils.isBlank(tableName)) {
        	tableName ="MEDICAL_OTHER_DICT";
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
	public void exportExcel(HttpServletRequest req,HttpServletResponse response, MedicalOtherDict bean) throws Exception {
		Result<?> result = new Result<>();
		LoginUser user = new LoginUser();
		user.setId(req.getParameter("loginUserId"));
		user.setRealname(req.getParameter("loginRealName"));
		String title = req.getParameter("title");
		if(StringUtils.isBlank(title)) {
			title = "其他字典合集_导出";
		}
		String tableName = req.getParameter("tableName");
		if(StringUtils.isBlank(tableName)) {
			tableName ="MEDICAL_OTHER_DICT";
 		}
		String suffix=ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
		// 选中数据
		String selections = req.getParameter("selections");
		if (StringUtils.isNotEmpty(selections)) {
			bean.setId(selections);
		}
		QueryWrapper<MedicalOtherDict> queryWrapper = service.getQueryWrapper(bean, req);
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
	 * 根据dictCname和parentCode和parentItemNo查询所有树形数据
	 *
	 * @param dictCname
	 * @param parentCode
	 * @return
	 * @throws Exception
	 */
	@AutoLog(value = "根据dictCname或dictEname和parentItemNo查询所有树形数据")
	@ApiOperation(value="根据dictCname或dictEname和parentItemNo查询所有树形数据", notes="根据dictCname或dictEname和parentItemNo查询所有树形数据")
	@GetMapping(value = "/getCascaderTreeList")
	 public Result<?> getCascaderTreeList(String dictCname,String dictEname,String parentCode) throws Exception {
		if(StringUtils.isBlank(dictCname)&&StringUtils.isBlank(dictEname)) {
			return Result.error("参数异常");
		}
		List<MedicalOtherDict> parentList = getOtherDictList(dictCname,dictEname,parentCode);
		List<JSONObject> list = new ArrayList<JSONObject>();
		for(MedicalOtherDict bean:parentList) {
			 JSONObject node = getCascaderTreeNode(dictCname,dictEname,bean);
			 list.add(node);
		}

		return Result.ok(list);

	 }

	 private JSONObject getCascaderTreeNode(String dictCname,String dictEname, MedicalOtherDict bean) {
			JSONObject node = new JSONObject();
			node.put("label",bean.getValue());
			node.put("value",bean.getCode());
			List<MedicalOtherDict> childrenList = getOtherDictList(dictCname,dictEname,bean.getCode());
			if(childrenList.size()>0) {
				node.put("isLeaf",false);
			}else {
				node.put("isLeaf",true);
			}
			return node;
		}


	 /**
	  * 根据选中的节点查询树形数据
	  *
	  * @param parentCodes
	  * @return
	  * @throws Exception
	  */
	 @AutoLog(value = "根据选中的节点查询树形数据")
	 @ApiOperation(value="根据选中的节点查询树形数据", notes="根据选中的节点查询树形数据")
	 @GetMapping(value = "/getCascaderSelectTreeList")
	 public Result<?> getCascaderSelectTreeList(String dictCname,String dictEname,@RequestParam(name="parentCodes",required=true) String parentCodes) throws Exception {
		 if(StringUtils.isBlank(dictCname)&&StringUtils.isBlank(dictEname)) {
				return Result.error("参数异常");
		 }
		 //1.获取所有一级数据
		 List<MedicalOtherDict> parentList = getOtherDictList(dictCname,dictEname,null);
		 List<JSONObject> list = new ArrayList<JSONObject>();
		 String[] parentCodes_arr = parentCodes.split(",");
		 for(MedicalOtherDict bean:parentList) {
			 JSONObject node= getCascaderSelectTreeNode(dictCname,dictEname,bean,parentCodes_arr,0);;
			 list.add(node);
		 }
		 return Result.ok(list);
	 }

	 private JSONObject getCascaderSelectTreeNode(String dictCname,String dictEname,MedicalOtherDict bean,String[] parentCodes_arr,int num) {
		 JSONObject node = new JSONObject();
		 node.put("label",bean.getValue());
		 node.put("value",bean.getCode());
		 List<MedicalOtherDict> childrenList = getOtherDictList(dictCname,dictEname,bean.getCode());
		 if(childrenList.size()>0) {
			 node.put("isLeaf",false);
			 if(parentCodes_arr.length>num&&parentCodes_arr[num].equals(bean.getCode())){
			 	 num++;
			 	 List<JSONObject> list2 = new ArrayList<JSONObject>();
				 for(MedicalOtherDict bean2:childrenList) {
					 JSONObject node2 = getCascaderSelectTreeNode(dictCname,dictEname,bean2,parentCodes_arr,num);
					 list2.add(node2);
				 }
				 node.put("children", list2);
			 }
		 }else {
			 node.put("isLeaf",true);
		 }
		 return node;
	 }

}
