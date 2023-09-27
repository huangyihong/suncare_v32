package com.ai.modules.config.service;

import java.util.List;

import org.apache.poi.ss.formula.functions.T;

import com.ai.modules.config.entity.MedicalAuditLog;

public interface  ICommonAuditService{

	/**
	 * 保存审核信息，进行审核业务
	 * @param bean
	 */
	public void saveAuditMedicalAuditLog(MedicalAuditLog bean);

	/**
	 * 保存撤回，进行撤回业务
	 * @param bean
	 */
	public void saveUndoMedicalAuditLog(MedicalAuditLog bean);

	/**
	 * 保存全部审核信息，进行全部审核业务
	 * @param bean
	 */
	public void saveAuditAllMedicalAuditLog(MedicalAuditLog bean,List<Object> list) throws Exception;

	/**
	 * 保存全部撤回，进行全部撤回业务
	 * @param bean
	 */
	public void saveUndoAllMedicalAuditLog(MedicalAuditLog bean,List<Object> ids);
}
