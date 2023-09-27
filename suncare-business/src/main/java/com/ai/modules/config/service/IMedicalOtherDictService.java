package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalAuditLog;
import com.ai.modules.config.entity.MedicalOtherDict;
import com.alibaba.fastjson.JSONObject;
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
 * @Description: 其他字典
 * @Author: jeecg-boot
 * @Date:   2019-12-18
 * @Version: V1.0
 */
public interface IMedicalOtherDictService extends IService<MedicalOtherDict> {
	/**
	 * 根据request拼装QueryWrapper
	 * @param request
	 * @return
	 */
	public QueryWrapper<MedicalOtherDict> getQueryWrapper(MedicalOtherDict medicalOtherDict,HttpServletRequest request)throws Exception;

	/**
	 * 根据主键获取记录
	 * @param ids
	 * @return
	 */
	public List<MedicalOtherDict> getListByIds(String ids);

	/**
	 * 新增
	 * @param bean
	 */
	public void saveMedicalOtherDict(MedicalOtherDict bean);

	/**
	 * 待审批的数据修改
	 * @param bean
	 */
	public void onlyUpdateMedicalOtherDict(MedicalOtherDict bean);

	/**
	 * 编码是否重复
	 * @param code
	 * @param id
	 * @return
	 */
	boolean isExistName(String code,String dictEname,String id);

	/**
	 * 修改
	 * @param bean
	 */
	public void updateMedicalOtherDict(MedicalOtherDict bean);

	/**
	 * 删除
	 * @param bean
	 */
	public void delMedicalOtherDict(MedicalOtherDict bean);

	/**
	 * 一键清理
	 * @param queryWrapper
	 * @param bean
	 */
	public int saveCleanMedicalOtherDict(QueryWrapper<MedicalOtherDict> queryWrapper, MedicalAuditLog bean);

	/**
	 * 全部删除操作
	 * @param queryWrapper
	 * @param bean
	 */
	public int delAllMedicalOtherDict(QueryWrapper<MedicalOtherDict> queryWrapper, MedicalOtherDict bean) throws Exception;

	/**
	 * 批量新增修改导入
	 * @param file
	 * @param user
	 * @return
	 */
	public Result<?> importExcel(MultipartFile file,LoginUser user) throws Exception;

	/**
	 * 根据code获取value值
	 * @param dictCname
	 * @param dictEname
	 * @param parentCode
	 * @param parentValue
	 * @param code
	 * @return
	 */
	public String queryValueByCode(String dictCname,String dictEname, String parentCode, String parentValue, String code);

	/**
	 * 导出excel
	 * @param queryWrapper
	 * @param os
	 * @return
	 */
	public boolean exportExcel(QueryWrapper<MedicalOtherDict> queryWrapper,OutputStream os, String suffix);

	/**
	 * 上级级联
	 * @param dictEname
	 * @param typeCode
	 * @return
	 */
	public List<Map<String, Object>> getTypeList(String dictEname,String typeCode);

	/**
	 * 根据字典英文名称获取字典数据列表
	 * @param dictEname
	 * @return
	 */
	public List<MedicalOtherDict> getOtherDictListByDictEname(String dictEname);

	/**
	 * 根据dictEname和字典项编码code获取字典项名称
	 * @param dictEname
	 * @param code
	 * @return
	 */
	public String getValueByCode(String dictEname,String code);

	/**
	 * 根据dictEname和字典项名称value获取字典项编码
	 * @param dictEname
	 * @param value
	 * @return
	 */
	public String getCodeByValue(String dictEname,String value);

	Map<String, String> queryNameMapByType(String type);


	Map<String, String> queryMapByType(String dictEname);

	/**
	 * 清理缓存
	 * @param dictEname
	 * @param code
	 */
	public void clearCacheByCode(String dictEname, String code);

	/**
	 * 清理缓存
	 * @param dictEname
	 * @param value
	 */
	public void clearCacheByValue(String dictEname, String value);

	public Map<String, String> getMapByCode(String dictEname,String codes);

	public Map<String, String> getMapByValue(String dictEname,String values);

	List<JSONObject> getTreeAllList(String dictCname, String dictEname);
}
