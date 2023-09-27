package com.ai.modules.formal.service;

import java.util.List;

import org.jeecg.common.system.vo.LoginUser;

import com.ai.modules.formal.entity.MedicalFormalBehavior;
import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 不合规行为配置
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
public interface IMedicalFormalBehaviorService extends IService<MedicalFormalBehavior> {
	/**
	 * 保存不合规行为及不合规行为模型关联信息
	 * @param medicalFormalBehaviorVO
	 */
	void saveBehaviorAndCaseBehavior(MedicalFormalBehaviorVO medicalFormalBehaviorVO);

	/**
	 * 修改不合规行为及不合规行为模型关联信息
	 * @param medicalFormalBehaviorVO
	 */
	void updateBehaviorAndCaseBehavior(MedicalFormalBehaviorVO medicalFormalBehaviorVO);

	/**
	 * 删除不合规行为及不合规行为模型关联信息
	 * @param id
	 */
	void removeBehaviorAndCaseBehaviorById(String id);

	/**
	 * 批量删除不合规行为及不合规行为模型关联信息
	 * @param idList
	 */
	void removeBehaviorAndCaseBehaviorByIds(List<String> idList);

	/**
	 * 根据历史批次导入不合规行为记录
	 * @param obj
	 * @param user
	 */
	void importByBatchId(JSONObject obj, LoginUser user);

	/**
	 * 按照特定排序写入solr记录及显示
	 * @param batchId
	 * @param type
	 * @return
	 */
    List<MedicalFormalBehavior> listByOrder(String batchId, String type);

	List<MedicalFormalBehavior> listByOrder(String batchId);

	List<MedicalFormalBehavior> selectByBatchCase( String batchId, String[] caseIds);

	List<MedicalFormalBehaviorVO> selectBehaviorCaseByBatch( String batchId);

}
