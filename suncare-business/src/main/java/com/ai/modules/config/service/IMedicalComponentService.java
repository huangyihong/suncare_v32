package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalComponent;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 成分表
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface IMedicalComponentService extends IService<MedicalComponent> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalComponent> getQueryWrapper(MedicalComponent medicalComponent,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalComponent> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalComponent(MedicalComponent bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalComponent(MedicalComponent bean);

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
	public void updateMedicalComponent(MedicalComponent bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalComponent(MedicalComponent bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalComponent(QueryWrapper<MedicalComponent> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalComponent(QueryWrapper<MedicalComponent> queryWrapper, MedicalComponent bean) throws Exception;

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
	public boolean exportExcel(QueryWrapper<MedicalComponent> queryWrapper, OutputStream os, String suffix);
}
