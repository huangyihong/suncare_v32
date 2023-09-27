package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalStdAtc;
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
 * @Description: ATC药品级别信息
 * @Author: jeecg-boot
 * @Date:   2019-12-20
 * @Version: V1.0
 */
public interface IMedicalStdAtcService extends IService<MedicalStdAtc> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalStdAtc> getQueryWrapper(MedicalStdAtc medicalStdAtc, HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalStdAtc> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalStdAtc(MedicalStdAtc bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalStdAtc(MedicalStdAtc bean);

	/**
	 * 编码是否重复
	 * @param code
	 * @param id
	 * @return
	 */
	boolean isExistName(String code, String id);

	/**
	 * 修改
	 * @param bean
	 */
	public void updateMedicalStdAtc(MedicalStdAtc bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalStdAtc(MedicalStdAtc bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalStdAtc(QueryWrapper<MedicalStdAtc> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalStdAtc(QueryWrapper<MedicalStdAtc> queryWrapper, MedicalStdAtc bean) throws Exception;

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
	public boolean exportExcel(QueryWrapper<MedicalStdAtc> queryWrapper, OutputStream os, String suffix);

	public List<Map<String, Object>> queryCascader(String parentCode,int levelNum,String state);

	public String getNameByCode(String code);

	public String getCodeByName(String value);

	public MedicalStdAtc getBeanByCode(String code);

	public Map<String, String> getMapByCode(String codes);

	public Map<String, String> getMapByName(String values);
}
