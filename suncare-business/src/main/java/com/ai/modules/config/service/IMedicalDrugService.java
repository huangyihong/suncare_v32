package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalDrug;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 药品信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface IMedicalDrugService extends IService<MedicalDrug> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalDrug> getQueryWrapper(MedicalDrug medicalDrug, HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalDrug> getListByIds(String ids);
	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalDrug(MedicalDrug bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalDrug(MedicalDrug bean);

	/**
	 * 编码是否重复
	 * @param code
	 * @param groupId
	 * @return
	 */
	boolean isExistName(String code, String groupId);

	/**
	 * 修改
	 * @param bean
	 */
	public void updateMedicalDrug(MedicalDrug bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalDrug(MedicalDrug bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalDrug(QueryWrapper<MedicalDrug> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalDrug(QueryWrapper<MedicalDrug> queryWrapper, MedicalDrug bean) throws Exception;

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
	public boolean exportExcel(QueryWrapper<MedicalDrug> queryWrapper, OutputStream os, String suffix);

}
