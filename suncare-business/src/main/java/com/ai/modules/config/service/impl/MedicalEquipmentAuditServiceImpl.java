package com.ai.modules.config.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.config.entity.MedicalEquipment;
import com.ai.modules.config.entity.MedicalEquipment;
import com.ai.modules.config.service.ICommonAuditService;
import com.ai.modules.config.service.IMedicalAuditLogService;
import com.ai.modules.config.service.IMedicalEquipmentService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


@Service("equipmentAuditService")
@Transactional
public class MedicalEquipmentAuditServiceImpl  implements ICommonAuditService {
	@Autowired
	private IMedicalEquipmentService service;

	@Autowired
	private IMedicalAuditLogService serviceLog;

	@Override
	public void saveAuditMedicalAuditLog(MedicalAuditLog bean) {
		List<MedicalEquipment> updateList = new ArrayList<>();
		List<MedicalAuditLog> logUpdateList = new ArrayList<>();
		String tableName = bean.getTableName();
		List<MedicalEquipment> list =  this.service.getListByIds(bean.getRecordKey());
		for(MedicalEquipment recordBean:list){
			commonAuditMedicalAuditLog(bean, tableName,recordBean,updateList,logUpdateList);
		}
		if(updateList.size()>0){
			service.updateBatchById(updateList,MedicalAuditLogConstants.BATCH_SIZE);
		}
		if(logUpdateList.size()>0){
			serviceLog.updateBatchById(logUpdateList,MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
		}
	}

	//审核单条操作
	private void commonAuditMedicalAuditLog(MedicalAuditLog bean, String tableName,MedicalEquipment recordBean,List<MedicalEquipment> updateList,List<MedicalAuditLog> logUpdateList) {
		List<MedicalAuditLog> logList = serviceLog.getMedicalAuditLogListByKey(recordBean.getId(),tableName,null);
		MedicalAuditLog oldBean = new MedicalAuditLog();
		if(logList.size()>0){
			oldBean = logList.get(0);
			oldBean.setAuditResult(bean.getAuditResult());
			oldBean.setAuditOpinion(bean.getAuditOpinion());
			oldBean.setAuditStaff(bean.getAuditStaff());
			oldBean.setAuditStaffName(bean.getAuditStaffName());
			oldBean.setAuditTime(new Date());
			logUpdateList.add(oldBean);
		}
		updateList.add(this.auditBean(oldBean, bean, recordBean));
	}

	//MedicalEquipment审核数据修改
	private MedicalEquipment auditBean(MedicalAuditLog oldBean, MedicalAuditLog bean, MedicalEquipment recordBean) {
		if(MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(oldBean.getActionType())){//新增
			if(MedicalAuditLogConstants.AUDITRESULT_SHTG.equals(bean.getAuditResult())){//审核通过
				recordBean.setState(MedicalAuditLogConstants.STATE_YX);//有效
			}else if(MedicalAuditLogConstants.AUDITRESULT_SHBTG.equals(bean.getAuditResult())){//审核不通过
				recordBean.setState(MedicalAuditLogConstants.STATE_WX);//无效
			}
		}else if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(oldBean.getActionType())){//修改
			if(MedicalAuditLogConstants.AUDITRESULT_SHTG.equals(bean.getAuditResult())){//审核通过
				if(StringUtils.isNotBlank(oldBean.getUpdateJson())){
					recordBean = JSON.parseObject(oldBean.getUpdateJson(), MedicalEquipment.class);//修改的对象
					recordBean.setId(oldBean.getRecordKey());
				}
				recordBean.setState(MedicalAuditLogConstants.STATE_YX);//有效
				//更新修改时间原因等
				recordBean.setUpdateStaff(oldBean.getActionStaff());
				recordBean.setUpdateStaffName(oldBean.getActionStaffName());
				recordBean.setUpdateTime(oldBean.getActionTime());
				recordBean.setUpdateReason(oldBean.getActionReason());
				//更新关联表信息
				this.serviceLog.updateRelationTable(bean.getTableName(),recordBean);
			}
		}else if(MedicalAuditLogConstants.ACTIONTYPE_DELETE.equals(oldBean.getActionType())){//删除
			if(MedicalAuditLogConstants.AUDITRESULT_SHTG.equals(bean.getAuditResult())){//审核通过
				recordBean.setState(MedicalAuditLogConstants.STATE_WX);//无效
				//更新删除时间原因等
				recordBean.setDeleteStaff(oldBean.getActionStaff());
				recordBean.setDeleteStaffName(oldBean.getActionStaffName());
				recordBean.setDeleteTime(oldBean.getActionTime());
				recordBean.setDeleteReason(oldBean.getActionReason());
			}else if(MedicalAuditLogConstants.AUDITRESULT_SHBTG.equals(bean.getAuditResult())){//审核不通过
				recordBean.setState(MedicalAuditLogConstants.STATE_YX);//有效
			}
		}
		//更新审核信息
		recordBean.setAuditResult(bean.getAuditResult());
		recordBean.setAuditOpinion(bean.getAuditOpinion());
		recordBean.setAuditStaff(bean.getAuditStaff());
		recordBean.setAuditStaffName(bean.getAuditStaffName());
		recordBean.setAuditTime(bean.getAuditTime());
		return recordBean;
	}

	@Override
	public void saveUndoMedicalAuditLog(MedicalAuditLog bean) {
		List list =  this.service.getListByIds(bean.getRecordKey());
		saveUndoMedicalAuditLog(bean, list);
	}

	private void saveUndoMedicalAuditLog(MedicalAuditLog bean, List<Object> list) {
		List<MedicalEquipment> updateList = new ArrayList<>();
		List<String> deleteList = new ArrayList<>();
		List<MedicalAuditLog> logAddList = new ArrayList<>();
		List<String> logDeleteList = new ArrayList<>();
		String tableName = bean.getTableName();
		for(Object obj:list){
			MedicalEquipment recordBean = (MedicalEquipment)obj;
			commonUndoMedicalAuditLog(bean, tableName, recordBean,updateList,deleteList,logAddList,logDeleteList);
		}
		if(deleteList.size()>0){
			List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(deleteList,MedicalAuditLogConstants.BATCH_SIZE);
			for (HashSet<String> idsSet : idSetList) {
				service.removeByIds(idsSet);
			}
		}
		if(updateList.size()>0){
			service.updateBatchById(updateList,MedicalAuditLogConstants.BATCH_SIZE);
		}
		if(logDeleteList.size()>0){
			List<HashSet<String>> idSetList = MedicalAuditLogConstants.getIdSetList(logDeleteList,MedicalAuditLogConstants.BATCH_SIZE);
			for (HashSet<String> idsSet : idSetList) {
				serviceLog.removeByIds(idsSet);
			}
		}
		if(logAddList.size()>0){
			serviceLog.saveBatch(logAddList,MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
		}
	}

	//撤回单条操作
	private void commonUndoMedicalAuditLog(MedicalAuditLog bean, String tableName,MedicalEquipment  recordBean,List<MedicalEquipment> updateList,List<String> deleteList,List<MedicalAuditLog> logAddList,List<String> logDeleteList) {
		List<MedicalAuditLog> logList = serviceLog.getMedicalAuditLogListByKey(recordBean.getId(),tableName,null);
		MedicalAuditLog oldBean = new MedicalAuditLog();
		if(logList.size()>0){
			oldBean = logList.get(0);
			logDeleteList.add(oldBean.getId());//删除上次的日志记录
			MedicalAuditLog temp = new MedicalAuditLog();
			BeanUtils.copyProperties(bean,temp);
			temp.setId(IdUtils.uuid());
			temp.setRecordKey(oldBean.getRecordKey());
			temp.setTableName(tableName);
			temp.setActionType(MedicalAuditLogConstants.ACTIONTYPE_UNDO);//撤销
			logAddList.add(temp);
		}
		this.undoMedicalEquipment(recordBean,oldBean,updateList,deleteList);
	}

	//撤销操作
	private void undoMedicalEquipment(MedicalEquipment recordBean, MedicalAuditLog oldBean, List<MedicalEquipment> updateList, List<String> deleteList) {
		if(MedicalAuditLogConstants.ACTIONTYPE_ADD.equals(oldBean.getActionType())){//撤销新增
			deleteList.add(recordBean.getId());
		}else if(MedicalAuditLogConstants.ACTIONTYPE_UPDATE.equals(oldBean.getActionType())||MedicalAuditLogConstants.ACTIONTYPE_DELETE.equals(oldBean.getActionType())){//修改、删除
			recordBean.setState(MedicalAuditLogConstants.STATE_YX);//有效
			//查找上上一次log记录 ，更新actionType、审核状态auditResult
			List<MedicalAuditLog> logList = serviceLog.getMedicalAuditLogListByKey(oldBean.getRecordKey(), oldBean.getTableName(),null);
			if(logList.size()>1){
				recordBean.setActionType(logList.get(1).getActionType());
				recordBean.setAuditResult(logList.get(1).getAuditResult());
			}else if(MedicalAuditLogConstants.AUDITRESULT_DSH.equals(recordBean.getAuditResult())){//无历史日志记录 待审核撤销为审核通过
				recordBean.setAuditResult(MedicalAuditLogConstants.AUDITRESULT_SHTG);
			}
			updateList.add(recordBean);
		}
	}

	@Override
	public void saveAuditAllMedicalAuditLog(MedicalAuditLog bean, List<Object> list) throws Exception {
		List<MedicalEquipment> updateList = new ArrayList<>();
		List<MedicalAuditLog> logUpdateList = new ArrayList<>();
		String tableName = bean.getTableName();
		for(Object obj:list){
			MedicalEquipment recordBean = (MedicalEquipment)obj;
			commonAuditMedicalAuditLog(bean, tableName,recordBean,updateList,logUpdateList);
		}
		if(updateList.size()>0){
			service.updateBatchById(updateList,MedicalAuditLogConstants.BATCH_SIZE);
		}
		if(logUpdateList.size()>0){
			serviceLog.updateBatchById(logUpdateList,MedicalAuditLogConstants.BATCH_SIZE);//生成日志信息
		}
	}

	@Override
	public void saveUndoAllMedicalAuditLog(MedicalAuditLog bean, List<Object> list) {
		this.saveUndoMedicalAuditLog(bean,list);
	}

}
