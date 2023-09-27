package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.poi.ss.formula.functions.T;
import org.jeecg.common.system.vo.LoginUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @Description: 基础数据维护操作日志
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
public interface IMedicalAuditLogService extends IService<MedicalAuditLog> {

	/**
	 * 获取审核记录信息，根据表名tableName和关联记录的主键recordKey和操作类型actionType
	 * @param recordKey
	 * @param tableName
	 * @param actionType
	 * @return
	 */
	public List<MedicalAuditLog> getMedicalAuditLogListByKey(String recordKey, String tableName, String actionType);

	/**
	 * 获取日志信息，根据主键id
	 * @param id
	 * @return
	 */
	public MedicalAuditLog getMedicalAuditLog(String id);

	/**
	 * 新增bean
	 * @param bean
	 */
	public void insertMedicalAuditLog(MedicalAuditLog bean);

	/**
	 * 更新bean
	 * @param bean
	 */
	public void updateMedicalAuditLog(MedicalAuditLog bean);

	/**
	 * 新增、修改、删除操作的时候插入日志记录
	 * @param tableName
	 * @param recordKey
	 * @param auditResult
	 * @param actionType
	 * @param actionReason
	 * @param actionStaff
	 * @param actionStaffName
	 * @param actionTime
	 * @param updateJson
	 * @param actionContent
	 */
	public void insertMedicalAuditLog(String tableName, String recordKey, String auditResult, String actionType, String actionReason, String actionStaff,
                                      String actionStaffName, Date actionTime, String updateJson, String actionContent);

	/**
	 * 新增、修改、删除操作的时候生成日志记录
	 * @param tableName
	 * @param recordKey
	 * @param auditResult
	 * @param actionType
	 * @param actionReason
	 * @param actionStaff
	 * @param actionStaffName
	 * @param actionTime
	 * @param updateJson
	 * @param actionContent
	 */
	public MedicalAuditLog setMedicalAuditLog(String tableName, String recordKey, String auditResult, String actionType, String actionReason, String actionStaff,
                                              String actionStaffName, Date actionTime, String updateJson, String actionContent);

	/**
	 * 插入导出日志
	 * @param tableName
	 * @param count
	 * @param user
	 */
	public void insertExportLog(String tableName, int count, LoginUser user);

	/**
	 * 清理其他字典缓存
	 * @param dictEname
	 * @param code
	 */
	public void clearCacheByCode(String dictEname, String code);

	/**
	 * 清理其他字典缓存
	 * @param dictEname
	 * @param value
	 */
	public void clearCacheByValue(String dictEname, String value);

	/**
	 * 根据表名和记录更新关联表记录
	 * @param tableName
	 * @param bean
	 * @return
	 */
	public void updateRelationTable(String tableName, Object bean);

	/**
	 * 操作时间
	 * @param queryWrapper
	 * @param request
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public <T> QueryWrapper<T> initQueryWrapperTime(QueryWrapper<T> queryWrapper, HttpServletRequest request) throws Exception;

}
