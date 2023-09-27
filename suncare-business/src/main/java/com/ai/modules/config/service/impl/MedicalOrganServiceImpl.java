package com.ai.modules.config.service.impl;

import com.ai.common.utils.*;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalOrgan;
import com.ai.modules.config.mapper.MedicalOrganMapper;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalOrganService;
import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.ai.modules.config.vo.MedicalOrganResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 医疗机构
 * @Author: jeecg-boot
 * @Date:   2019-12-31
 * @Version: V1.0
 */
@Service
public class MedicalOrganServiceImpl extends ServiceImpl<MedicalOrganMapper, MedicalOrgan> implements IMedicalOrganService {
	@Autowired
	IMedicalAuditLogService logService;
	@Autowired
    private RedisUtil redisUtil;

	private static final String TABLE_NAME="MEDICAL_ORGAN";//表名

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public IPage<MedicalOrgan> pageByMasterInfoJoin(Page<MedicalOrgan> page, QueryWrapper<MedicalOrgan> queryWrapper, String dataSource){
		return this.baseMapper.listByMasterInfoJoin(page, queryWrapper, dataSource);
	}

	@Override
	public QueryWrapper<MedicalOrgan> getQueryWrapper(MedicalOrgan medicalOrgan,HttpServletRequest request) throws Exception {
		medicalOrgan.setState("");
		medicalOrgan.setAuditResult("");
		medicalOrgan.setActionType("");
		QueryWrapper<MedicalOrgan> queryWrapper = QueryGenerator.initQueryWrapper(medicalOrgan, request.getParameterMap());
		String state = request.getParameter("state");
		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		String auditResult = request.getParameter("auditResult");
		if(StringUtils.isNotBlank(auditResult)){
			queryWrapper.in("AUDIT_RESULT", Arrays.asList(auditResult.split(",")));
		}
		String actionType = request.getParameter("actionType");
		if(StringUtils.isNotBlank(actionType)){
			queryWrapper.in("ACTION_TYPE", Arrays.asList(actionType.split(",")));
		}
		//操作时间
		queryWrapper = logService.initQueryWrapperTime(queryWrapper,request);
		//省市县乡镇
		String typeCode = request.getParameter("typeCode");
		if(StringUtils.isNotBlank(typeCode)) {
			queryWrapper.and(wrapper -> wrapper
					.eq("PROVINCE_CODE", typeCode)
					.or().eq("CITY_CODE", typeCode)
					.or().eq("COUNTY_CODE", typeCode)
					.or().eq("TOWN_CODE", typeCode)
					.or().eq("VILLAGE_CODE", typeCode));
		}
		return queryWrapper;
	}

	@Override
	public List<MedicalOrgan> getListByIds(String ids){
		String[] ids_arr = ids.split(",");
		List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(Arrays.asList(ids_arr),MedicalAuditLogConstants.BATCH_SIZE);
		List<MedicalOrgan> list = new ArrayList();
		for (HashSet<String> idsSet : idSetList) {
			list.addAll(this.baseMapper.selectBatchIds(idsSet));
		}
		return list;
	}

	@Override
	@Transactional
	public void saveMedicalOrgan(MedicalOrgan bean) {
		this.baseMapper.insert(bean);
		//插入日志记录
		logService.insertMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
				bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null);
	}

	@Override
	@Transactional
	public void onlyUpdateMedicalOrgan(MedicalOrgan bean) {
		this.baseMapper.updateById(bean);
		//直接修改日志记录
		List<MedicalAuditLog> list =  logService.getMedicalAuditLogListByKey(bean.getId(), TABLE_NAME,null);
		if(list.size()>0){
			MedicalAuditLog log = list.get(0);
			log.setActionReason(bean.getCreateReason());
			logService.updateMedicalAuditLog(log);
		}
	}

	@Override
	public boolean isExistName(String code, String id) {
		QueryWrapper<MedicalOrgan> queryWrapper = new QueryWrapper<MedicalOrgan>();
		queryWrapper.eq("CODE", code);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalOrgan> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}


	/**
	 *
	 * 功能描述：重复检查
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年10月9日 上午10:52:09</p>
	 *
	 * @param name
	 * @param code
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	@Override
	public boolean isExistOrg(MedicalOrgan bean, String code) {
		List<String> params = new ArrayList<String>();
		StringBuilder sql = new StringBuilder();
		sql.append("select count(1) from medical_organ where ("+DbDataEncryptUtil.decryptFunc("name")+"=?");
		params.add(bean.getName());
		if(StringUtils.isNotBlank(bean.getOrgUsedName())) {
			sql.append(" or org_used_name=?");
			params.add(bean.getOrgUsedName());
		}
		if(StringUtils.isNotBlank(bean.getAddress()) && !bean.getName().contains("卫生院")) {
			sql.append(" or address=?");
			params.add(bean.getAddress());
		}
		sql.append(")");
		if(StringUtils.isNotBlank(code)){
			sql.append(" and code<>?");
			params.add(bean.getCode());
		}
		sql.append(" and state in('0', '1')");
		int count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(new String[0]), Integer.class);
		return count>0;
	}

	/**
	 *
	 * 功能描述：是否存在同名机构名称
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年10月9日 上午10:52:09</p>
	 *
	 * @param name
	 * @param code
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public boolean isExistOrgName(String name, String code) {
		QueryWrapper<MedicalOrgan> queryWrapper = new QueryWrapper<MedicalOrgan>();
		queryWrapper.eq("NAME", name);
		if(StringUtils.isNotBlank(code)){
			queryWrapper.eq("CODE", code);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalOrgan> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	/**
	 *
	 * 功能描述：是否存在同名机构曾用名称
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年10月9日 上午10:52:09</p>
	 *
	 * @param name
	 * @param code
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public boolean isExistOrgUsedName(String orgUsedName, String code) {
		QueryWrapper<MedicalOrgan> queryWrapper = new QueryWrapper<MedicalOrgan>();
		queryWrapper.eq("ORG_USED_NAME", orgUsedName);
		if(StringUtils.isNotBlank(code)){
			queryWrapper.eq("CODE", code);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalOrgan> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	/**
	 *
	 * 功能描述：是否存在同名机构地址
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年10月9日 上午10:52:09</p>
	 *
	 * @param name
	 * @param code
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public boolean isExistAddress(String address, String code) {
		QueryWrapper<MedicalOrgan> queryWrapper = new QueryWrapper<MedicalOrgan>();
		queryWrapper.eq("ADDRESS", address);
		if(StringUtils.isNotBlank(code)){
			queryWrapper.eq("CODE", code);
		}
		List<String> stateList = new ArrayList<String>();
		stateList.add(MedicalAuditLogConstants.STATE_DSX);
		stateList.add(MedicalAuditLogConstants.STATE_YX);
		queryWrapper.in("STATE", stateList);//待生效、有效
		List<MedicalOrgan> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public void updateMedicalOrgan(MedicalOrgan bean) {
		MedicalOrgan oldBean = this.baseMapper.selectById(bean.getId());
		Map<String,String> map = MedicalAuditLogConstants.contrastObj(oldBean,bean,null);
		if(map!=null){
			oldBean.setActionStaff(bean.getActionStaff());
  			oldBean.setActionStaffName(bean.getActionStaffName());
          	oldBean.setActionTime(bean.getActionTime());
			if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
				String updateBeanStr = map.get("updateBeanStr");
				String updateContentStr = map.get("updateContentStr");
				this.baseMapper.updateById(oldBean);
				//直接修改日志记录
				List<MedicalAuditLog> list =  logService.getMedicalAuditLogListByKey(bean.getId(), TABLE_NAME,null);
				if(list.size()>0){
					MedicalAuditLog log = list.get(0);
					log.setUpdateJson(updateBeanStr);
					log.setActionContent(updateContentStr);
					log.setActionReason(bean.getUpdateReason());
					log.setActionTime(bean.getUpdateTime());
					logService.updateMedicalAuditLog(log);
				}
			}else{
				String updateBeanStr = map.get("updateBeanStr");
				String updateContentStr = map.get("updateContentStr");
				oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
				oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
				this.baseMapper.updateById(oldBean);
				//插入日志记录
				logService.insertMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
						bean.getUpdateReason(),bean.getUpdateStaff(),bean.getUpdateStaffName(),bean.getUpdateTime(), updateBeanStr,updateContentStr);
			}
		}
	}

	public void updateBeanBatch(List<MedicalOrgan> list, List<MedicalOrgan> oldlist) {
		Field[] fields = MedicalOrgan.class.getDeclaredFields();
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			MedicalOrgan bean = list.get(i);
			MedicalOrgan oldBean = oldlist.get(i);
			Map<String,String> map = MedicalAuditLogConstants.contrastObj(oldBean,bean,fields);
			if(map!=null){
				oldBean.setActionStaff(bean.getActionStaff());
				oldBean.setActionStaffName(bean.getActionStaffName());
				oldBean.setActionTime(bean.getActionTime());
				String updateBeanStr = map.get("updateBeanStr");
				String updateContentStr = map.get("updateContentStr");
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					//直接修改日志记录
					List<MedicalAuditLog> loglist =  logService.getMedicalAuditLogListByKey(bean.getId(), TABLE_NAME,null);
					if(loglist.size()>0){
						MedicalAuditLog log = loglist.get(0);
						log.setUpdateJson(updateBeanStr);
						log.setActionContent(updateContentStr);
						log.setActionReason(bean.getUpdateReason());
						log.setActionTime(bean.getUpdateTime());
						logAddList.add(log);
					}
				}else{
					oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
					oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
					logAddList.add(logService.setMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
							bean.getUpdateReason(),bean.getUpdateStaff(),bean.getUpdateStaffName(),bean.getUpdateTime(), updateBeanStr,updateContentStr));
				}
			}
		}
		this.updateBatchById(oldlist,MedicalAuditLogConstants.BATCH_SIZE);
		if(logAddList.size()>0){
			logService.saveOrUpdateBatch(logAddList,MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
		}
	}

	@Override
	@Transactional
	public void delMedicalOrgan(MedicalOrgan bean) {
		List<MedicalOrgan> list =getListByIds(bean.getId());
		commonDelMedicalOrganBatch(bean, list);
	}

	//删除单条操作
	private void commonDelMedicalOrganBatch(MedicalOrgan bean, List<MedicalOrgan> oldlist) {
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		for (MedicalOrgan oldBean : oldlist) {
			oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_DELETE);
			oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
			logAddList.add(logService.setMedicalAuditLog(TABLE_NAME, oldBean.getId(), oldBean.getAuditResult(), oldBean.getActionType(),
					bean.getDeleteReason(), bean.getDeleteStaff(), bean.getDeleteStaffName(), bean.getDeleteTime(), null, null));
		}
		this.updateBatchById(oldlist,MedicalAuditLogConstants.BATCH_SIZE);
		if (logAddList.size() > 0) {
			logService.saveOrUpdateBatch(logAddList, MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
		}
	}

	@Override
	@Transactional
	public int saveCleanMedicalOrgan(QueryWrapper<MedicalOrgan> queryWrapper, MedicalAuditLog bean) {
		int count = this.baseMapper.selectCount(queryWrapper);
		if(count>0){
			this.baseMapper.delete(queryWrapper);
			bean.setId(IdUtils.uuid());
			bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_CLEAN);//清理
			bean.setActionContent("影响记录数"+count+"条");
			this.logService.save(bean);
		}
		return count;
	}

	@Override
	@Transactional
	public int delAllMedicalOrgan(QueryWrapper<MedicalOrgan> queryWrapper, MedicalOrgan bean)
			throws Exception {
		List<MedicalOrgan> list = this.baseMapper.selectList(queryWrapper);
		commonDelMedicalOrganBatch(bean, list);
		return list.size();
	}

	@Override
	@Transactional
	public Result<?> importExcel(MultipartFile file,LoginUser user) throws Exception {
		String mappingFieldStr = "code,name,parentId,orgCode,ybDdbh,orgUsedName,orgLevelCode,orgLevel,orgClassCode,orgClass,"
				+ "provinceCode,provinceName,cityCode,cityName,countyCode,countyName,townCode,townName,villageCode,villageName,"
				+ "address,latLon,administrativeLevelCode,administrativeLevel,orgTypeCode,orgType,healthTypeCode,healthType,"
				+ "businessNatureCode,businessNature,ownershipCode,ownership,priceLevelCode,priceLevel,membershipCode,membership,"
				+ "organiserTypeCode,organiserType,competentUnitCode,competentUnit,postcode,telephone,xnhFlagCode,xnhFlagName,ybFlagCode,ybFlagName,"
				+ "gsFlagCode,gsFlagName,zybFlagCode,zybFlagName,approveBedNum,openBedNum,legalName,legalIdType,legalIdNo,legalAddress,legalpersonPhone,insuranceOrg,insuranceOrgname,createReason,actionType";//导入的字段
		String[] mappingFields = mappingFieldStr.split(",");
		return allImportExcel(file, user,mappingFields);
	}


	private Result<?> allImportExcel(MultipartFile file, LoginUser user,String[] mappingFields) throws Exception, IOException {
		System.out.println("开始导入时间："+DateUtils.now() );
		String serialNum = UUIDGenerator.generate();
		List<MedicalOrgan> list = new ArrayList<MedicalOrgan>();
		String name = file.getOriginalFilename();
		if (name.endsWith(ExcelTool.POINT+ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
			list = ExcelXUtils.readSheet(MedicalOrgan.class, mappingFields, 0, 1, file.getInputStream());
		}else {
			list = ExcelUtils.readSheet(MedicalOrgan.class, mappingFields, 0, 1, file.getInputStream());
		}
		if(list.size() == 0) {
			Result<MedicalOrganResult> result = new Result<MedicalOrganResult>();
			result.setCode(CommonConstant.SC_INTERNAL_SERVER_ERROR_500);
			MedicalOrganResult t = new MedicalOrganResult(serialNum, "上传文件内容为空");
			result.setResult(t);
		    return result;
		}
		String message = "";
		//机构编码
		Set<String> codeSet = new HashSet<String>();
		//机构名称
		Set<String> nameSet = new HashSet<String>();
		//机构曾用名
		Set<String> usedNameSet = new HashSet<String>();
		List<MedicalOrgan> addList = new ArrayList<MedicalOrgan>();
		List<MedicalOrgan> olnyUpdateList = new ArrayList<MedicalOrgan>();
		List<MedicalOrgan> updateList = new ArrayList<MedicalOrgan>();
		List<MedicalOrgan> oldUpdateList = new ArrayList<MedicalOrgan>();
		List<MedicalOrgan> deleteList = new ArrayList<MedicalOrgan>();
		List<MedicalAuditLog> logList = new ArrayList<MedicalAuditLog>();
		//Set<String> codeExistSet = getAllCodeSet();//库中已存在的code
		//重复机构明细
		List<MedicalOrgan> excludeList = new ArrayList<MedicalOrgan>();
		System.out.println("校验开始："+DateUtils.now() );
		List<String> codesAdd = new ArrayList<>();//新增编码
		List<String> codesUpdate = new ArrayList<>();//修改编码
		List<String> codesDelete = new ArrayList<>();//删除编码
		Map<String,MedicalOrgan> addMap = new HashMap<>();
		Map<String,MedicalOrgan> updateMap = new HashMap<>();
		Map<String,MedicalOrgan> deleteMap = new HashMap<>();
		List<String> errorMsg =  new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			MedicalOrgan bean = list.get(i);
			if (StringUtils.isBlank(bean.getCode())) {
				errorMsg.add("导入的数据中“医疗机构编码”不能为空，如：第" + (i + 2) + "行数据“医疗机构编码”为空");
		    }
		    if (StringUtils.isBlank(bean.getName())) {
				errorMsg.add("导入的数据中“医疗机构名称”不能为空，如：第" + (i + 2) + "行数据“医疗机构名称”为空");
		    }
		    //判断code在excel中是否重复
		    if(codeSet.contains(bean.getCode())){
				errorMsg.add("导入的数据中“医疗机构编码”不能重复，如：第" + (i + 2) + "行数据“"+bean.getCode()+"”在excel中重复");
		    }
		    //判断name在excel中是否存在重复
		    if("0".equals(bean.getActionType()) || "1".equals(bean.getActionType())) {
		    	if(nameSet.contains(bean.getName())) {
					errorMsg.add("导入的数据中“医疗机构名称”不能重复，如：第" + (i + 2) + "行数据“"+bean.getName()+"”在excel中重复");
		    	}
		    	if(usedNameSet.contains(bean.getOrgUsedName())) {
					errorMsg.add("导入的数据中“医疗机构曾用名”不能重复，如：第" + (i + 2) + "行数据“"+bean.getOrgUsedName()+"”在excel中重复");
		    	}
		    }
			if (StringUtils.isBlank(bean.getActionType())) {
				errorMsg.add("导入的数据中“更新标志”不能为空，如：第" + (i + 2) + "行数据“更新标志”为空");
		    }
			if (!Arrays.asList(MedicalAuditLogConstants.importActionTypeArr).contains(bean.getActionType())) {
				errorMsg.add("导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据");
			}
			if(StringUtils.isBlank(bean.getCode())){
				continue;
			}
			if("1".equals(bean.getActionType())) {//新增
				codesAdd.add(bean.getCode());
				addMap.put(bean.getCode(),bean);
			}else if("0".equals(bean.getActionType())) {//修改
				codesUpdate.add(bean.getCode());
				updateMap.put(bean.getCode(),bean);
			}else if("2".equals(bean.getActionType())) {//删除
				codesDelete.add(bean.getCode());
				deleteMap.put(bean.getCode(),bean);
			}
			if(StringUtils.isNotBlank(bean.getCode())) {
				codeSet.add(bean.getCode());
			}
			if(StringUtils.isNotBlank(bean.getName())) {
				nameSet.add(bean.getName());
			}
			if(StringUtils.isNotBlank(bean.getOrgUsedName())) {
				usedNameSet.add(bean.getOrgUsedName());
			}
		}
		if(codesAdd.size()>0){
			List<String> stateList = new ArrayList<String>();
			stateList.add(MedicalAuditLogConstants.STATE_DSX);
			stateList.add(MedicalAuditLogConstants.STATE_YX);
			List<MedicalOrgan> existList = getBeanByCode(codesAdd,stateList,"CODE".split(","));
			List<String> existCode = existList.stream().map(MedicalOrgan::getCode).collect(Collectors.toList());
			if(existCode.size()>0){
				errorMsg.add("导入的数据中，新增数据中包含系统中待生效或者有效的数据，如：[" +
				StringUtils.join(existCode, ",") + "]");
			}
			if(errorMsg.size()==0){
				addMap.forEach((k, bean) -> {
					if(this.isExistOrg(bean, null)) {
						//新增存在重复机构
						excludeList.add(bean);
					} else {
						bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);

						//设置导入的新增人员
						bean.setId(IdUtils.uuid());
						bean.setState(MedicalAuditLogConstants.STATE_DSX);//待生效
						bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_ADD);
						bean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
						bean.setCreateStaff(user.getId());
						bean.setCreateStaffName(user.getRealname());
						bean.setCreateTime(new Date());
						bean.setActionStaff(user.getId());
						bean.setActionStaffName(user.getRealname());
						bean.setActionTime(new Date());

						logList.add(logService.setMedicalAuditLog(TABLE_NAME,bean.getId(),bean.getAuditResult(),bean.getActionType(),
								bean.getCreateReason(),bean.getCreateStaff(),bean.getCreateStaffName(),bean.getCreateTime(), null,null));

						addList.add(bean);
					}
				});
			}
		}

		if(codesUpdate.size()>0){
			//不存在或者无效的记录
			List<String> stateList = new ArrayList<String>();
			stateList.add(MedicalAuditLogConstants.STATE_DSX);
			stateList.add(MedicalAuditLogConstants.STATE_YX);
			List<MedicalOrgan> existList = getBeanByCode(codesUpdate,stateList,null);
			List<String> existCode = existList.stream().map(MedicalOrgan::getCode).collect(Collectors.toList());
			List<String> notExistCode = codesUpdate.stream().filter(item -> !existCode.contains(item)).collect(Collectors.toList());
			if(notExistCode.size()>0){
				errorMsg.add("导入的数据中，修改数据中包含系统中不存在或者无效的记录，如：[" +
						StringUtils.join(notExistCode, ",") + "]");
			}
			//待生效、有效 ->
			existList.forEach(oldBean ->{
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					if(MedicalAuditLogConstants.ACTIONTYPE_DELETE.equals(oldBean.getActionType())){
						errorMsg.add("导入的数据中，包含正在删除审核中的记录，如：“"+oldBean.getCode()+"”，不允许修改");
					}else if(MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(oldBean.getActionType())&&!user.getId().equals(oldBean.getActionStaff())){
						errorMsg.add("导入的数据中，包含其他用户新增的待审核的记录，如：“"+oldBean.getCode()+"”，不允许修改");
					}else if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(oldBean.getActionType())&&!user.getId().equals(oldBean.getActionStaff())){
						errorMsg.add("导入的数据中，包含其他用户正在修改的待审核的记录，如：“"+oldBean.getCode()+"”，不允许修改");
					}
				}
			});
			if(errorMsg.size()==0) {
				existList.forEach(oldBean -> {
					MedicalOrgan bean = updateMap.get(oldBean.getCode());
					if (this.isExistOrg(bean, bean.getCode())) {
						//修改存在重复机构
						excludeList.add(bean);
					} else {
						bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
						bean.setUpdateReason(bean.getCreateReason());
						bean.setCreateReason(oldBean.getCreateReason());
						bean.setId(oldBean.getId());
						if (MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult()) && MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(oldBean.getActionType())) {////新增待审核状态，进行修改
							bean.setActionStaff(user.getId());
							bean.setActionStaffName(user.getRealname());
							bean.setActionTime(new Date());
							bean.setCreateReason(bean.getUpdateReason());
							bean.setUpdateReason("");
							olnyUpdateList.add(bean);
						} else {
							bean.setUpdateStaff(user.getId());
							bean.setUpdateStaffName(user.getRealname());
							bean.setUpdateTime(new Date());
							bean.setActionStaff(user.getId());
							bean.setActionStaffName(user.getRealname());
							bean.setActionTime(new Date());
							updateList.add(bean);
							oldUpdateList.add(oldBean);
						}
					}
				});
			}
		}
		if(codesDelete.size()>0){
			//不存在或者无效的记录
			List<String> stateList = new ArrayList<String>();
			stateList.add(MedicalAuditLogConstants.STATE_DSX);
			stateList.add(MedicalAuditLogConstants.STATE_YX);
			List<MedicalOrgan> existList = getBeanByCode(codesDelete,stateList,null);
			List<String> existCode = existList.stream().map(MedicalOrgan::getCode).collect(Collectors.toList());
			List<String> notExistCode = codesDelete.stream().filter(item -> !existCode.contains(item)).collect(Collectors.toList());
			if(notExistCode.size()>0){
				errorMsg.add("导入的数据中，删除数据中包含系统中不存在或者无效的记录，如：[" +
						StringUtils.join(notExistCode, ",") + "]");
			}
			//待生效、有效 ->
			existList.forEach(oldBean ->{
				if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(oldBean.getAuditResult())){//待审核
					errorMsg.add("导入的数据中，删除数据中包含正在审核中的数据的记录，如：“"+oldBean.getCode()+"”，不允许删除");
				}
			});
			if(errorMsg.size()==0) {
				existList.forEach(oldBean -> {
					MedicalOrgan bean = deleteMap.get(oldBean.getCode());
					bean.setId(oldBean.getId());
					bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_DELETE);
					bean.setDeleteReason(bean.getCreateReason());
					bean.setCreateReason(oldBean.getCreateReason());
					bean.setActionStaff(user.getId());
					bean.setActionStaffName(user.getRealname());
					bean.setActionTime(new Date());
					bean.setDeleteTime(new Date());
					oldBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_DELETE);
					oldBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_DSH);//待审核
					//插入日志记录
					logList.add(logService.setMedicalAuditLog(TABLE_NAME,oldBean.getId(),oldBean.getAuditResult(),oldBean.getActionType(),
							bean.getDeleteReason(),bean.getDeleteStaff(),bean.getDeleteStaffName(),bean.getDeleteTime(), null,null));
					deleteList.add(oldBean);
				});
			}
		}

		if(errorMsg.size()>0){
			message = StringUtils.join(errorMsg, "\n");
		}
		if(StringUtils.isNotBlank(message)){
			message +="\n请核对数据后进行批量导入。";
			Result<MedicalOrganResult> result = new Result<MedicalOrganResult>();
			result.setSuccess(false);
			result.setCode(CommonConstant.SC_INTERNAL_SERVER_ERROR_500);
			MedicalOrganResult t = new MedicalOrganResult(serialNum, message);
			result.setResult(t);
			result.setMessage(message);
		    return result;
		}else{
			System.out.println("开始插入时间："+DateUtils.now() );
			//批量删除
			if(deleteList.size()>0){
				this.updateBatchById(deleteList,MedicalAuditLogConstants.BATCH_SIZE);
			}
			//批量新增
			if(addList.size()>0) {
				this.saveBatch(addList,MedicalAuditLogConstants.BATCH_SIZE);//直接插入
			}
			//批量修改
			if(olnyUpdateList.size()>0){
				this.updateBatchById(olnyUpdateList,MedicalAuditLogConstants.BATCH_SIZE);
			}
			if(updateList.size()>0){
				this.updateBeanBatch(updateList,oldUpdateList);
			}
			//新增删除生成日志
			if(logList.size()>0){
				logService.saveBatch(logList,MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
			}
			message += "批量导入完成！";
			if(addList.size()>0) {
				message += "共新增"+addList.size()+"条数据。";
			}
			if((olnyUpdateList.size()+updateList.size())>0) {
				message += "共修改"+(olnyUpdateList.size()+updateList.size())+"条数据。";
			}
			if(deleteList.size()>0) {
				message += "共删除"+deleteList.size()+"条数据。";
			}
			if(excludeList.size()>0) {
				//重复明细写入缓存，缓存5分钟
				String key = serialNum.concat("@MedicalOrgan");
				redisUtil.set(key, excludeList, 60*5);
				List<String> excludeCode = excludeList.stream().map(MedicalOrgan::getCode).collect(Collectors.toList());
				message +="\n导入的数据中，存在重复机构，忽略导入"+excludeCode.size()+"条数据。如：[" +
						StringUtils.join(excludeCode, ",") + "]";
			}
			System.out.println("结束导入时间："+DateUtils.now() );
			Result<MedicalOrganResult> result = new Result<MedicalOrganResult>();
			MedicalOrganResult t = new MedicalOrganResult(serialNum, message);
			t.setCount(excludeList.size());
			result.setSuccess(true);
			result.setResult(t);
			result.setMessage(message);
			return result;
		}
	}

	private List<MedicalOrgan> getBeanByCode(List<String> codes,List<String> stateList,String[] fileds){
		List<MedicalOrgan> alllist = new ArrayList<>();
		List<HashSet<String>> setList = MedicalAuditLogConstants.getIdSetList(codes,1000);
		for(Set<String> strList:setList){
			QueryWrapper<MedicalOrgan> queryWrapper = new QueryWrapper<MedicalOrgan>();
			queryWrapper.in("CODE",strList);
			queryWrapper.in("STATE", stateList);
			if(fileds!=null&&fileds.length>0){
				queryWrapper.select(fileds);
			}
			alllist.addAll(this.baseMapper.selectList(queryWrapper));
		}
		return  alllist;
	}

	private MedicalOrgan findBeanByCode(String code,String state) {
		QueryWrapper<MedicalOrgan> queryWrapper = new QueryWrapper<MedicalOrgan>();
		queryWrapper.eq("CODE", code);

		if(StringUtils.isNotBlank(state)){
			queryWrapper.in("STATE", Arrays.asList(state.split(",")));
		}
		List<MedicalOrgan> list = this.baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public boolean exportExcel(QueryWrapper<MedicalOrgan> queryWrapper,OutputStream os, String suffix){
		boolean isSuc = true;
		try {
			List<MedicalOrgan> list = this.list(queryWrapper);
			List<MedicalOrgan> dataList = new ArrayList<MedicalOrgan>();
	    	for(MedicalOrgan exportBean:list){
	    		if(StringUtils.isNotBlank(exportBean.getActionType())){
	    			exportBean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_MAP.get(exportBean.getActionType()));
	    		}
	    		if(StringUtils.isNotBlank(exportBean.getAuditResult())){
	    			exportBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_MAP.get(exportBean.getAuditResult()));
	    		}
	    		if(StringUtils.isNotBlank(exportBean.getState())){
	    			exportBean.setState(MedicalAuditLogConstants.STATE_MAP.get(exportBean.getState()));
	    		}
	    		dataList.add(exportBean);
	    	}
	    	String titleStr = "医疗机构编码,医疗机构名称,上级机构ID,组织机构代码,医保定点编号,医疗机构曾用名,医疗机构级别,医疗机构级别名称,医疗机构等级,医疗机构等级名称,"
					+ "地址-省(自治区、直辖市）代码,地址-省(自治区、直辖市）名称,地址-市(地区、州)代码,地址-市(地区、州)名称,地址-县(区)代码,地址-县(区)名称,地址-乡(镇、街道办事处)代码,地址-乡(镇、街道办事处)名称,地址-村(街、路、弄等)代码,地址-村(街、路、弄等)名称,"
					+ "医疗机构地址,经纬度,医疗机构行政级别编码,医疗机构行政级别名称,医疗机构类型编码,医疗机构类型名称,卫生机构类别,卫生机构类别名称,"
					+ "医疗机构经营性质编码,医疗机构经营性质名称,所有制形式编码,所有制形式名称,物价级别编码,物价级别名称,隶属关系编码,隶属关系名称,"
					+ "设置/主办单位类别编码,设置/主办单位类别名称,主管单位编码,主管单位名称,医疗机构邮政编码,医疗机构联系电话,新农合定点医疗机构标志,新农合定点医疗机构标志名称,医保定点医疗机构标志,医保定点医疗机构标志名称,"
					+ "工伤医疗机构标志,工伤医疗机构标志名称,职业病鉴定机构标志,职业病鉴定机构标志名称,医疗机构批准床位数,医疗机构实际开放床位数,法人姓名,法人证件类型,法人身份证件号码码,法人联系地址,法人联系电话,所属医保机构编码,所属医保机构名称,"
					+ "最近一次操作类型,数据状态,审核状态,审核人,审核时间,审核意见,"
	    			+ "新增人,新增时间,新增原因,最新修改人,最新修改时间,修改原因,删除人,删除时间,删除原因";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr = "code,name,parentId,orgCode,ybDdbh,orgUsedName,orgLevelCode,orgLevel,orgClassCode,orgClass,"
					+ "provinceCode,provinceName,cityCode,cityName,countyCode,countyName,townCode,townName,villageCode,villageName,"
					+ "address,latLon,administrativeLevelCode,administrativeLevel,orgTypeCode,orgType,healthTypeCode,healthType,"
					+ "businessNatureCode,businessNature,ownershipCode,ownership,priceLevelCode,priceLevel,membershipCode,membership,"
					+ "organiserTypeCode,organiserType,competentUnitCode,competentUnit,postcode,telephone,xnhFlagCode,xnhFlagName,ybFlagCode,ybFlagName,"
					+ "gsFlagCode,gsFlagName,zybFlagCode,zybFlagName,approveBedNum,openBedNum,legalName,legalIdType,legalIdNo,legalAddress,legalpersonPhone,insuranceOrg,insuranceOrgname,"
					+ "actionType,state,auditResult,auditStaffName,auditTime,auditOpinion,"
					+ "createStaffName,createTime,createReason,updateStaffName,updateTime,updateReason,deleteStaffName,deleteTime,deleteReason";
			String[] fields=fieldStr.split(",");
			if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
				SXSSFWorkbook workbook = new SXSSFWorkbook();
			    ExportXUtils.exportExl(dataList,MedicalOrgan.class,titles,fields,workbook,"数据");
			    workbook.write(os);
		        workbook.dispose();
			}else {
				 // 创建文件输出流
		        WritableWorkbook wwb = Workbook.createWorkbook(os);
		        WritableSheet sheet = wwb.createSheet("数据", 0);
				ExportUtils.exportExl(dataList,MedicalOrgan.class,titles,fields,sheet, "");
				wwb.write();
		        wwb.close();
			}
    	} catch (Exception e) {
			e.printStackTrace();
			isSuc = false;
		}
    	return isSuc;
	}

	@Override
	public boolean exportExcel(String serialNum, OutputStream os, String suffix){
		boolean isSuc = true;
		try {
			String key = serialNum.concat("@MedicalOrgan");
			List<MedicalOrgan> dataList = new ArrayList<MedicalOrgan>();
			Object object = redisUtil.get(key);
			if(object!=null) {
				dataList = (List<MedicalOrgan>) object;
			}
			for(MedicalOrgan exportBean : dataList){
	    		if(StringUtils.isNotBlank(exportBean.getActionType())){
	    			if(MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(exportBean.getActionType())) {
	    				exportBean.setActionType("1");
	    			} else if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(exportBean.getActionType())) {
	    				exportBean.setActionType("0");
	    			} else if(MedicalAuditLogConstants.ACTIONTYPE_DELETE.equals(exportBean.getActionType())) {
	    				exportBean.setActionType("2");
	    			}
	    		}
	    	}
	    	String titleStr = "*医疗机构编码,*医疗机构名称,上级机构ID,组织机构代码,医保定点编号,医疗机构曾用名,医疗机构级别,医疗机构级别名称,医疗机构等级,医疗机构等级名称,"
					+ "地址-省(自治区、直辖市）代码,地址-省(自治区、直辖市）名称,地址-市(地区、州)代码,地址-市(地区、州)名称,地址-县(区)代码,地址-县(区)名称,地址-乡(镇、街道办事处)代码,地址-乡(镇、街道办事处)名称,地址-村(街、路、弄等)代码,地址-村(街、路、弄等)名称,"
					+ "医疗机构地址,经纬度,医疗机构行政级别编码,医疗机构行政级别名称,医疗机构类型编码,医疗机构类型名称,卫生机构类别,卫生机构类别名称,"
					+ "医疗机构经营性质编码,医疗机构经营性质名称,所有制形式编码,所有制形式名称,物价级别编码,物价级别名称,隶属关系编码,隶属关系名称,"
					+ "设置/主办单位类别编码,设置/主办单位类别名称,主管单位编码,主管单位名称,医疗机构邮政编码,医疗机构联系电话,新农合定点医疗机构标志,新农合定点医疗机构标志名称,医保定点医疗机构标志,医保定点医疗机构标志名称,"
	    			+ "工伤医疗机构标志,工伤医疗机构标志名称,职业病鉴定机构标志,职业病鉴定机构标志名称,医疗机构批准床位数,医疗机构实际开放床位数,法人姓名,法人证件类型,法人身份证件号码,法人联系地址,法人联系电话,所属医保机构编码,所属医保机构名称,操作原因,更新标志(1新增0修改2删除)";
	    	String[] titles= titleStr.split(",");
	    	String fieldStr = "code,name,parentId,orgCode,ybDdbh,orgUsedName,orgLevelCode,orgLevel,orgClassCode,orgClass,"
					+ "provinceCode,provinceName,cityCode,cityName,countyCode,countyName,townCode,townName,villageCode,villageName,"
					+ "address,latLon,administrativeLevelCode,administrativeLevel,orgTypeCode,orgType,healthTypeCode,healthType,"
					+ "businessNatureCode,businessNature,ownershipCode,ownership,priceLevelCode,priceLevel,membershipCode,membership,"
					+ "organiserTypeCode,organiserType,competentUnitCode,competentUnit,postcode,telephone,xnhFlagCode,xnhFlagName,ybFlagCode,ybFlagName,"
					+ "gsFlagCode,gsFlagName,zybFlagCode,zybFlagName,approveBedNum,openBedNum,legalName,legalIdType,legalIdNo,legalAddress,legalpersonPhone,insuranceOrg,insuranceOrgname,createReason,actionType";
			String[] fields=fieldStr.split(",");
			if(ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
				SXSSFWorkbook workbook = new SXSSFWorkbook();
			    ExportXUtils.exportExl(dataList,MedicalOrgan.class,titles,fields,workbook,"重复明细");
			    workbook.write(os);
		        workbook.dispose();
			}else {
				 // 创建文件输出流
		        WritableWorkbook wwb = Workbook.createWorkbook(os);
		        WritableSheet sheet = wwb.createSheet("重复明细", 0);
				ExportUtils.exportExl(dataList,MedicalOrgan.class,titles,fields,sheet, "");
				wwb.write();
		        wwb.close();
			}
    	} catch (Exception e) {
			e.printStackTrace();
			isSuc = false;
		}
    	return isSuc;
	}

	@Override
	public String getNameByCode(String code) {
		QueryWrapper<MedicalOrgan> queryWrapper = new QueryWrapper<MedicalOrgan>();
		queryWrapper.eq("CODE", code);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOrgan> list = this.baseMapper.selectList(queryWrapper);
		if(list.size()>0) {
			return list.get(0).getName();
		}
		return null;
	}

	@Override
	public Map<String, String> getMapByCode(String codes) {
		Map<String, String> map = new HashMap<>();
		List<String> valueList = Arrays.asList(codes.split(","));
		List<List<String>> setList = getBatchList(valueList,1000);
		for(List<String> strList:setList){
			queryListByCodes(strList, map);
		}
		return map;
	}

	private void queryListByCodes(List<String> codeList, Map<String, String> map) {
		QueryWrapper<MedicalOrgan> queryWrapper = new QueryWrapper<MedicalOrgan>();
		queryWrapper.in("CODE", codeList);
		queryWrapper.eq("STATE", MedicalAuditLogConstants.STATE_YX);
		List<MedicalOrgan> list = this.baseMapper.selectList(queryWrapper);
		for(MedicalOrgan bean:list){
			map.put(bean.getCode(),bean.getName());
		}
	}

	private List<List<String>> getBatchList(List<String> list,int batchSize) {
		List<List<String>> batchList = new ArrayList<List<String>>();
		List<String> strList = new ArrayList<String>();
		if(list.size()<batchSize){
			batchList.add(list);
			return batchList;
		}
		for (String str : list) {
			if (strList.size() > batchSize) {
				batchList.add(strList);
				strList = new ArrayList<String>();
			}
			strList.add(str);
		}
		if (strList.size() > 0) {
			batchList.add(strList);
		}
		return batchList;
	}

	@Override
	public List<Map<String, Object>> getRegionList(String paretnCode,String type) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String fieldName = "PROVINCE_NAME" ;
		String fieldCode = "PROVINCE_CODE" ;
		String paretnFieldCode = "";
		String showType="PROVINCE";
		if("PROVINCE".equals(type)){
			fieldName = "PROVINCE_NAME" ;
			fieldCode = "PROVINCE_CODE " ;
			paretnFieldCode = "";
			showType="CITY";
		}
		if("CITY".equals(type)){
			fieldName = "CITY_NAME" ;
			fieldCode = "CITY_CODE " ;
			paretnFieldCode = "PROVINCE_CODE";
			showType="COUNTY";
		}
		if("COUNTY".equals(type)){
			fieldName = "COUNTY_NAME" ;
			fieldCode = "COUNTY_CODE " ;
			paretnFieldCode = "CITY_CODE";
			showType="TOWN";
		}
		if("TOWN".equals(type)){
			fieldName = "TOWN_NAME" ;
			fieldCode = "TOWN_CODE " ;
			paretnFieldCode = "COUNTY_CODE";
			showType="VILLAGE";
		}
		if("VILLAGE".equals(type)){
			fieldName = "VILLAGE_NAME" ;
			fieldCode = "VILLAGE_CODE " ;
			paretnFieldCode = "TOWN_CODE";
			showType="VILLAGE";
		}
		String sql = "select distinct t."+fieldName+" as value,t."+fieldCode+" as code from" +
				" (select distinct code,name from DWB_MASTER_INFO_ORG where data_source='"+user.getDataSource()+"' )t1 " +
				"left join medical_organ t on t1.code = t.code where 1=1 ";
		if(StringUtils.isNotBlank(paretnCode)){
			sql += " and t."+paretnFieldCode+" ='"+paretnCode+"'" ;
		}
		sql += " order by t."+fieldCode+" asc";
//		return jdbcTemplate.queryForList("select  nvl(value,'空值') as value,nvl(code,'') as code,'"+showType+"' as type from ("+sql+")");
		return jdbcTemplate.queryForList("select  ifnull(value,'空值') as VALUE,ifnull(code,'') as CODE,'"+showType+"' as type from ("+sql+") t9");
	}

	@Override
	public List<MedicalCodeNameVO> listMasterInfoJoinSelectMaps(QueryWrapper<MedicalOrgan> queryWrapper, String dataSource) {
		return this.baseMapper.listMasterInfoJoinSelectMaps(queryWrapper, dataSource);
	}
}
