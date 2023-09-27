package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @Description: 手术信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface IMedicalOperationService extends IService<MedicalOperation> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalOperation> getQueryWrapper(MedicalOperation medicalOperation,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalOperation> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalOperation(MedicalOperation bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalOperation(MedicalOperation bean);

	/**
	 * 编码是否重复
	 * @param code
	 * @param id
	 * @return
	 */
	boolean isExistName(String code,String id);

	/**
	 * 修改
	 * @param bean
	 */
	public void updateMedicalOperation(MedicalOperation bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalOperation(MedicalOperation bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalOperation(QueryWrapper<MedicalOperation> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalOperation(QueryWrapper<MedicalOperation> queryWrapper, MedicalOperation bean) throws Exception;

	/**
	 * 批量新增修改导入
	 * @param file
	 * @param user
	 * @return
	 */
	public Result<?> importExcel(MultipartFile file,LoginUser user) throws Exception;

	/**
	 * 导出excel
	 * @param queryWrapper
	 * @param os
	 * @return
	 */
	public boolean exportExcel(QueryWrapper<MedicalOperation> queryWrapper, OutputStream os, String suffix);

	/**
	 * 获取章节目录
	 * @param typeCode
	 * @return
	 */
	public List<Map<String, Object>> getTypeList(String typeCode);


}
