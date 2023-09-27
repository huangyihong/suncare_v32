/**
 * IQueryTaskBatchRuleService.java	  V1.0   2020年12月22日 上午9:17:58
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api;

import java.util.List;

import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalDruguseRuleGroup;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

public interface IApiQueryTaskBatchRuleService {
	/**
	 * 
	 * 功能描述：查找规则（新版）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 上午9:25:09</p>
	 *
	 * @param batchId
	 * @param stepType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalRuleConfig> queryMedicalRuleConfig(String batchId, String stepType);
	
	/**
	 * 
	 * 功能描述：查找批次中跑失败的规则（新版）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 上午9:25:09</p>
	 *
	 * @param batchId
	 * @param stepType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalRuleConfig> queryMedicalRuleConfigFail(String batchId, String stepType);
	
	/**
	 * 
	 * 功能描述：查找规则筛查条件（新版）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 上午10:12:54</p>
	 *
	 * @param ruleId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalRuleConditionSet> queryMedicalRuleConditionSet(String ruleId);
	
	/**
	 * 
	 * 功能描述：查找规则（新版）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 上午10:19:24</p>
	 *
	 * @param ruleId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	MedicalRuleConfig queryMedicalRuleConfig(String ruleId);
	
	/**
	 * 
	 * 功能描述：查找药品、收费、诊疗等规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 下午5:09:44</p>
	 *
	 * @param batchId
	 * @param stepType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDrugRule> queryMedicalDrugRule(String batchId, String stepType);
	
	/**
	 * 
	 * 功能描述：查找批次中跑失败的药品、收费、诊疗等规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 下午5:09:44</p>
	 *
	 * @param batchId
	 * @param stepType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDrugRule> queryMedicalDrugRuleFail(String batchId, String stepType);
	
	/**
	 * 
	 * 功能描述：查找规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 下午5:30:46</p>
	 *
	 * @param ruleId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDrugRule> queryMedicalDrugRuleByRuleid(String ruleId);
	
	/**
	 * 
	 * 功能描述：根据编码获取药品名称
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 下午5:42:04</p>
	 *
	 * @param itemcode
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	String getDrugname(String itemcode);
	
	/**
	 * 
	 * 功能描述：查找批次中的某个药品、收费项目、诊疗项目规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 下午5:49:56</p>
	 *
	 * @param batchId
	 * @param stepType
	 * @param itemCode
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDrugRule> queryMedicalDrugRuleByItem(String batchId, String stepType, String itemCode);
	
	/**
	 * 
	 * 功能描述：根据编码获取收费项目、诊疗项目名称
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月22日 下午5:42:04</p>
	 *
	 * @param itemcode
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	String getTreatname(String itemcode);
	
	/**
	 * 
	 * 功能描述：查找批次中用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月24日 下午3:37:21</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDruguse> queryMedicalDruguseByBatchid(String batchId);
	
	/**
	 * 
	 * 功能描述：查找批次中跑失败的用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月24日 下午3:37:21</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDruguse> queryMedicalDruguseFail(String batchId);
	
	/**
	 * 
	 * 功能描述：查找用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月24日 下午4:02:36</p>
	 *
	 * @param ruleId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	MedicalDruguse queryMedicalDruguse(String ruleId);
	
	/**
	 * 
	 * 功能描述：查找用药规则筛查条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月24日 下午4:09:12</p>
	 *
	 * @param ruleId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDruguseRuleGroup> queryMedicalDruguseRuleGroup(String ruleId);
	
	/**
	 * 
	 * 功能描述：是否启用hive计算引擎
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月24日 下午4:24:03</p>
	 *
	 * @param ruleId
	 * @param ruleType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	boolean existsMedicalRuleEngine(String ruleId, String ruleType);
	
	/**
	 * 
	 * 功能描述：查找重复用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月11日 下午5:56:22</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalYbDrug> queryMedicalDrugrepeat();
	
	/**
	 * 
	 * 功能描述：查找批次中跑失败的重复用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月11日 下午5:57:16</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalYbDrug> queryMedicalDrugrepeatFail(String batchId);
	
	/**
	 * 
	 * 功能描述：查找重复用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月11日 下午5:57:16</p>
	 *
	 * @param ruleId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalYbDrug> queryMedicalDrugrepeat(String ruleId);
}
