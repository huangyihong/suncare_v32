package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalDrugProperty;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 药品属性表
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface IMedicalDrugPropertyService extends IService<MedicalDrugProperty> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalDrugProperty> getQueryWrapper(MedicalDrugProperty medicalDrugProperty,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalDrugProperty> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalDrugProperty(MedicalDrugProperty bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalDrugProperty(MedicalDrugProperty bean);

	/**
	 * 序号是否重复
	 * @param orderNum
	 * @param id
	 * @return
	 */
	boolean isExistOrderNum(int orderNum,String id);

	/**
	 * 修改
	 * @param bean
	 */
	public void updateMedicalDrugProperty(MedicalDrugProperty bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalDrugProperty(MedicalDrugProperty bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalDrugProperty(QueryWrapper<MedicalDrugProperty> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalDrugProperty(QueryWrapper<MedicalDrugProperty> queryWrapper, MedicalDrugProperty bean) throws Exception;

	/**
	 * 批量新增修改导入
	 * @param file
	 * @param user
	 * @return
	 */
	public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception;

	/**
	 * 导出excel
	 * @param queryWrapper
	 * @param os
	 * @return
	 */
	public boolean exportExcel(QueryWrapper<MedicalDrugProperty> queryWrapper, OutputStream os, String suffix);

	/**
	 * 获取最大序号orderNum
	 * @return
	 */
	public int getMaxOrderNum();

}
