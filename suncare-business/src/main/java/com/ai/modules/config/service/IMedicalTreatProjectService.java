package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalTreatProject;
import com.ai.modules.config.vo.MedicalTreatProjectEquipmentVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 医疗服务项目
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface IMedicalTreatProjectService extends IService<MedicalTreatProject> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalTreatProject> getQueryWrapper(MedicalTreatProject medicalTreatProject,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalTreatProject> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalTreatProject(MedicalTreatProject bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalTreatProject(MedicalTreatProject bean);

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
	public void updateMedicalTreatProject(MedicalTreatProject bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalTreatProject(MedicalTreatProject bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalTreatProject(QueryWrapper<MedicalTreatProject> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalTreatProject(QueryWrapper<MedicalTreatProject> queryWrapper, MedicalTreatProject bean) throws Exception;

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
	public boolean exportExcel(QueryWrapper<MedicalTreatProject> queryWrapper, OutputStream os, String suffix);

	/**
	 * 两张表关联结果列表
	 * @param page
	 * @param queryWrapper
	 * @return
	 */
	public IPage<MedicalTreatProjectEquipmentVO> selectTreatProjectEquipmentPageVO(
			Page<MedicalTreatProjectEquipmentVO> page, QueryWrapper<MedicalTreatProjectEquipmentVO> queryWrapper);

	List<MedicalTreatProjectEquipmentVO> selectTreatProjectEquipment(
			QueryWrapper<MedicalTreatProjectEquipmentVO> queryWrapper);

	public MedicalTreatProject getBeanByCode(String code);
}
