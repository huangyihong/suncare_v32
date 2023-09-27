package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalDiseaseDiag;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: ICD国际疾病维护
 * @Author: jeecg-boot
 * @Date:   2019-12-27
 * @Version: V1.0
 */
public interface IMedicalDiseaseDiagService extends IService<MedicalDiseaseDiag> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalDiseaseDiag> getQueryWrapper(MedicalDiseaseDiag medicalDiseaseDiag,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalDiseaseDiag> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalDiseaseDiag(MedicalDiseaseDiag bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalDiseaseDiag(MedicalDiseaseDiag bean);

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
	public void updateMedicalDiseaseDiag(MedicalDiseaseDiag bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalDiseaseDiag(MedicalDiseaseDiag bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalDiseaseDiag(QueryWrapper<MedicalDiseaseDiag> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalDiseaseDiag(QueryWrapper<MedicalDiseaseDiag> queryWrapper, MedicalDiseaseDiag bean) throws Exception;

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
	public boolean exportExcel(QueryWrapper<MedicalDiseaseDiag> queryWrapper, OutputStream os, String suffix);


}
