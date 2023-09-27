package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalEquipment;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 医疗器械信息表
 * @Author: jeecg-boot
 * @Date:   2020-05-09
 * @Version: V1.0
 */
public interface IMedicalEquipmentService extends IService<MedicalEquipment> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalEquipment> getQueryWrapper(MedicalEquipment medicalEquipment,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalEquipment> getListByIds(String ids);
	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalEquipment(MedicalEquipment bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalEquipment(MedicalEquipment bean);

	/**
	 * 编码是否重复
	 * @param productcode
	 * @param id
	 * @return
	 */
	boolean isExistName(String productcode,String id);

	/**
	 * 修改
	 * @param bean
	 */
	public void updateMedicalEquipment(MedicalEquipment bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalEquipment(MedicalEquipment bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalEquipment(QueryWrapper<MedicalEquipment> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalEquipment(QueryWrapper<MedicalEquipment> queryWrapper, MedicalEquipment bean) throws Exception;

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
	public boolean exportExcel(QueryWrapper<MedicalEquipment> queryWrapper, OutputStream os, String suffix);

	public MedicalEquipment getBeanByCode(String code);
}
