package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalOffice;
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
 * @Description: 科室信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface IMedicalOfficeService extends IService<MedicalOffice> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalOffice> getQueryWrapper(MedicalOffice medicalOffice,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalOffice> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalOffice(MedicalOffice bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalOffice(MedicalOffice bean);

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
	public void updateMedicalOffice(MedicalOffice bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalOffice(MedicalOffice bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalOffice(QueryWrapper<MedicalOffice> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalOffice(QueryWrapper<MedicalOffice> queryWrapper, MedicalOffice bean) throws Exception;

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
	public boolean exportExcel(QueryWrapper<MedicalOffice> queryWrapper, OutputStream os, String suffix);

	/**
	 * 获取一级二级科目
	 * @param typeCode
	 * @return
	 */
	public List<Map<String, Object>> getTypeList(String typeCode);


}
