package com.ai.modules.config.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.*;
import com.ai.modules.config.mapper.*;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: 基础数据维护操作日志
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
@Service
public class MedicalAuditLogServiceImpl extends ServiceImpl<MedicalAuditLogMapper, MedicalAuditLog> implements IMedicalAuditLogService {

	@Autowired
	private MedicalOtherDictMapper otherDictMapper;

	@Autowired
	private MedicalChineseDrugMapper chineseDrugMapper;

	@Autowired
	private MedicalDiseaseDiagMapper diseaseDiagMapper;

	@Autowired
	private MedicalOperationMapper operationMapper;

	@Autowired
	private MedicalTreatProjectMapper treatProjectMapper;

	@Autowired
	private MedicalPathologyMapper pathologyMapper;

	@Autowired
	private MedicalEquipmentMapper equipmentMapper;

	@Autowired
	private MedicalStdAtcMapper stdAtcMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<MedicalAuditLog> getMedicalAuditLogListByKey(String recordKey, String tableName,String actionType) {
		QueryWrapper<MedicalAuditLog> queryWrapper = new QueryWrapper<MedicalAuditLog>();
		queryWrapper.eq("RECORD_KEY", recordKey);
		queryWrapper.eq("TABLE_NAME", tableName);
		if(StringUtils.isNotBlank(actionType)){
			queryWrapper.eq("ACTION_TYPE", actionType);
		}else{
			List<String> actionTypeList = new ArrayList<String>();
			actionTypeList.add(MedicalAuditLogConstants.ACTIONTYPE_ADD);
			actionTypeList.add(MedicalAuditLogConstants.ACTIONTYPE_UPDATE);
			actionTypeList.add(MedicalAuditLogConstants.ACTIONTYPE_DELETE);
			queryWrapper.in("ACTION_TYPE", actionTypeList);
		}
		queryWrapper.orderByDesc("ACTION_TIME");
		return this.baseMapper.selectList(queryWrapper);
	}

	@Override
	public MedicalAuditLog getMedicalAuditLog(String id) {
		return this.baseMapper.selectById(id);
	}

	@Override
	public void updateMedicalAuditLog(MedicalAuditLog bean) {
		this.baseMapper.updateById(bean);
	}

	@Override
	public void insertMedicalAuditLog(MedicalAuditLog log) {
		log.setId(IdUtils.uuid());
		this.baseMapper.insert(log);
	}

	@Override
	public void insertMedicalAuditLog(String tableName, String recordKey, String auditResult, String actionType,
			String actionReason, String actionStaff, String actionStaffName, Date actionTime, String updateJson,
			String actionContent) {
		MedicalAuditLog log = new MedicalAuditLog();
		log.setId(IdUtils.uuid());
		log.setTableName(tableName);
		log.setRecordKey(recordKey);
		log.setAuditResult(auditResult);
		log.setActionType(actionType);
		log.setActionReason(actionReason);
		log.setActionStaff(actionStaff);
		log.setActionStaffName(actionStaffName);
		log.setActionTime(actionTime);
		if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(actionType)){
			log.setUpdateJson(updateJson);
			log.setActionContent(actionContent);
		}
		this.baseMapper.insert(log);
	}

	@Override
	public MedicalAuditLog setMedicalAuditLog(String tableName, String recordKey, String auditResult, String actionType,
			String actionReason, String actionStaff, String actionStaffName, Date actionTime, String updateJson,
			String actionContent) {
		MedicalAuditLog log = new MedicalAuditLog();
		log.setId(IdUtils.uuid());
		log.setTableName(tableName);
		log.setRecordKey(recordKey);
		log.setAuditResult(auditResult);
		log.setActionType(actionType);
		log.setActionReason(actionReason);
		log.setActionStaff(actionStaff);
		log.setActionStaffName(actionStaffName);
		log.setActionTime(actionTime);
		if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(actionType)){
			log.setUpdateJson(updateJson);
			log.setActionContent(actionContent);
		}
		return log;
	}


	@Override
	public void insertExportLog(String tableName, int count, LoginUser user) {
		MedicalAuditLog bean = new MedicalAuditLog();
		bean.setTableName(tableName);
		bean.setActionStaff(user.getId());
		bean.setActionStaffName(user.getRealname());
    	bean.setActionTime(new Date());
    	bean.setActionType(MedicalAuditLogConstants.ACTIONTYPE_EXPORT);
		bean.setActionContent("Excel导出记录数"+count+"条");
		this.insertMedicalAuditLog(bean);
	}


	@Override
	@CacheEvict(value=CacheConstant.MEDICAL_OTHER_DICT_CACHE,key = "#dictEname+':'+#code")
	public void clearCacheByCode(String dictEname, String code) {

	}

	@Override
	@CacheEvict(value=CacheConstant.MEDICAL_OTHER_DICT_CACHE,key = "#dictEname+':'+#value")
	public void clearCacheByValue(String dictEname, String value) {

	}

	@Override
	public void updateRelationTable(String tableName,Object bean) {
		if("MEDICAL_DISEASE_DIAG".equals(tableName)){//ICD国际疾病
			MedicalDiseaseDiag oldBean = this.diseaseDiagMapper.selectById(((MedicalDiseaseDiag) bean).getId());
			if(!oldBean.getName().equals(((MedicalDiseaseDiag) bean).getName())&&StringUtils.isNotBlank(((MedicalDiseaseDiag) bean).getName())){
				this.updateGroupItem("MEDICAL_DISEASE_GROUP_ITEM",((MedicalDiseaseDiag) bean).getName(),oldBean.getCode(),"STD_ICD");
			}
		}
		if("MEDICAL_CHINESE_DRUG".equals(tableName)){//中草药
			MedicalChineseDrug oldBean = this.chineseDrugMapper.selectById(((MedicalChineseDrug) bean).getId());
			if(!oldBean.getName().equals(((MedicalChineseDrug) bean).getName())&&StringUtils.isNotBlank(((MedicalChineseDrug) bean).getName())){
				this.updateGroupItem("MEDICAL_DRUG_GROUP_ITEM",((MedicalChineseDrug) bean).getName(),oldBean.getCode(),"STD_HERB");
			}
		}
		if("MEDICAL_OTHER_DICT".equals(tableName)){//其他字典
			if("TCM_DIAG".equals(((MedicalOtherDict) bean).getDictEname())){
				MedicalOtherDict oldBean = this.otherDictMapper.selectById(((MedicalOtherDict) bean).getId());
				if(!oldBean.getValue().equals(((MedicalOtherDict) bean).getValue())&&StringUtils.isNotBlank(((MedicalOtherDict) bean).getValue())){
					this.updateGroupItem("MEDICAL_DISEASE_GROUP_ITEM",((MedicalOtherDict) bean).getValue(),oldBean.getCode(),"TCM_DIAG");
				}
			}
		}
		if("MEDICAL_OPERATION".equals(tableName)){//手术信息
			MedicalOperation oldBean = this.operationMapper.selectById(((MedicalOperation) bean).getId());
			if(!oldBean.getName().equals(((MedicalOperation) bean).getName())&&StringUtils.isNotBlank(((MedicalOperation) bean).getName())){
				this.updateGroupItem("MEDICAL_DISEASE_GROUP_ITEM",((MedicalOperation) bean).getName(),oldBean.getCode(),"STD_ICD_SURGERY");
			}
		}
		if("MEDICAL_OFFICE".equals(tableName)){//科室信息

		}
		if("MEDICAL_TREAT_PROJECT".equals(tableName)){//医疗服务项目
			MedicalTreatProject oldBean = this.treatProjectMapper.selectById(((MedicalTreatProject) bean).getId());
			if(!oldBean.getName().equals(((MedicalTreatProject) bean).getName())&&StringUtils.isNotBlank(((MedicalTreatProject) bean).getName())){
				this.updateGroupItem("MEDICAL_PROJECT_GROUP_ITEM",((MedicalTreatProject) bean).getName(),oldBean.getCode(),"STD_TREATMENT");
			}
		}
		if("MEDICAL_DRUG".equals(tableName)){//药品信息

		}
		if("MEDICAL_ORGAN".equals(tableName)){//医疗机构信息

		}
		if("MEDICAL_COMPONENT".equals(tableName)){//成分表

		}
		if("MEDICAL_PATHOLOGY".equals(tableName)){//形态学编码表
			MedicalPathology oldBean = this.pathologyMapper.selectById(((MedicalPathology) bean).getId());
			if(!oldBean.getName().equals(((MedicalPathology) bean).getName())&&StringUtils.isNotBlank(((MedicalPathology) bean).getName())){
				this.updateGroupItem("MEDICAL_DISEASE_GROUP_ITEM",((MedicalPathology) bean).getName(),oldBean.getCode(),"STD_PATHOLOGY");
			}
		}
		if("MEDICAL_DRUG_PROPERTY".equals(tableName)){//药品属性表

		}
		if("MEDICAL_EQUIPMENT".equals(tableName)){//医疗器械信息表
			MedicalEquipment oldBean = this.equipmentMapper.selectById(((MedicalEquipment) bean).getId());
			if(!oldBean.getProductname().equals(((MedicalEquipment) bean).getProductname())&&StringUtils.isNotBlank(((MedicalEquipment) bean).getProductname())){
				this.updateGroupItem("MEDICAL_PROJECT_GROUP_ITEM",((MedicalEquipment) bean).getProductname(),oldBean.getProductcode(),"STD_MEDICAL_EQUIPMENT");
			}
		}
		if("MEDICAL_STD_ATC".equals(tableName)){//ATC药品级别信息表
			MedicalStdAtc oldBean = this.stdAtcMapper.selectById(((MedicalStdAtc) bean).getId());
			if(!oldBean.getName().equals(((MedicalStdAtc) bean).getName())&&StringUtils.isNotBlank(((MedicalStdAtc) bean).getName())){
				this.updateGroupItem("MEDICAL_DRUG_GROUP_ITEM",((MedicalStdAtc) bean).getName(),oldBean.getCode(),"STD_ATC");
			}
		}
	}

	@Override
	public <T> QueryWrapper<T> initQueryWrapperTime(QueryWrapper<T> queryWrapper, HttpServletRequest request) throws Exception{
		//操作时间
		String createTime1 = request.getParameter("actionTime1");
		String createTime2 = request.getParameter("actionTime2");
		if(StringUtils.isNotEmpty(createTime1)&& StringUtils.isNotEmpty(createTime2)) {
			Date time1 = DateUtils.parseDate(createTime1, "yyyy-MM-dd");
			Date time2 = DateUtils.parseDate(createTime2+" 24:00:00", "yyyy-MM-dd HH:mm:ss");
			queryWrapper.and(wrapper ->{
				wrapper.ge("CREATE_TIME", time1).le("CREATE_TIME", time2);
				wrapper.or();
				wrapper.ge("UPDATE_TIME", time1).le("UPDATE_TIME", time2);
				wrapper.or();
				wrapper.ge("DELETE_TIME", time1).le("DELETE_TIME", time2);
				return wrapper;
			});
		}else if(StringUtils.isNotEmpty(createTime1)) {
			Date time1 = DateUtils.parseDate(createTime1, "yyyy-MM-dd");
			queryWrapper.and(wrapper ->wrapper.ge("CREATE_TIME", time1).or().ge("UPDATE_TIME", time1).or().ge("DELETE_TIME", time1));
		}else if(StringUtils.isNotEmpty(createTime2)) {
			Date time2 = DateUtils.parseDate(createTime2+" 24:00:00", "yyyy-MM-dd HH:mm:ss");
			queryWrapper.and(wrapper ->wrapper.le("CREATE_TIME", time2).or().le("UPDATE_TIME", time2).le("DELETE_TIME", time2));
		}
		return queryWrapper;
	}

	private void updateGroupItem(String tableName,String value,String code,String tableType){
		String sql = "update "+tableName+" set VALUE=? where CODE=? and TABLE_TYPE=?";
		jdbcTemplate.update(sql,value,code,tableType);
	}

}
