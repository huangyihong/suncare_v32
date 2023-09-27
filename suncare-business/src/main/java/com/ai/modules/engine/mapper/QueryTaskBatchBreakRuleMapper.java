/**
 * QueryTaskBatchBreakRuleMapper.java	  V1.0   2020年1月8日 下午8:08:58
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.entity.dto.QryMedicalDrugRuleDTO;
import com.ai.modules.medical.entity.dto.QryMedicalRuleConfigDTO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;

public interface QueryTaskBatchBreakRuleMapper {
	
	List<HisMedicalFormalCase> findHisMedicalFormalCase(String batchId);
	/**
	 * 
	 * 功能描述：按任务批次号查询模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月7日 上午10:58:14</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalFormalCase> queryMedicalFormalFlowCaseByBatchid(String batchId);
	
	List<MedicalFormalCase> queryMedicalFormalFlowCaseByBusiid(String busiId);
	
	/**
	 * 
	 * 功能描述：按批次号查询药品规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月19日 上午11:12:22</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDrugRule> queryMedicalDrugRuleByBatchid(String batchId);
	
	/**
	 * 
	 * 功能描述：按批次号查询收费项目规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月19日 上午11:12:35</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDrugRule> queryMedicalChargeRuleByBatchid(String batchId);
	
	/**
	 * 
	 * 功能描述：按批次号查询临床路径规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月19日 上午11:12:35</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDrugRule> queryMedicalClinicalRuleByBatchid(String batchId);
	
	List<MedicalDrugRule> queryMedicalDrugRuleByRuleid(String ruleId);
	
	List<MedicalDrugRule> queryMedicalDrugRule(QryMedicalDrugRuleDTO dto);
	
	IPage<MedicalDrugRule> queryMedicalDrugRuleByPager(IPage<MedicalDrugRule> page, @Param(Constants.WRAPPER) Wrapper<QryMedicalDrugRuleDTO> wapper);
	
	List<MedicalDrugRule> queryMedicalDrugRuleByItem(QryMedicalDrugRuleDTO dto);
	
	/**
	 * 
	 * 功能描述：查找临床路径必需药品组
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月10日 上午9:45:42</p>
	 *
	 * @param clinicalId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalClinicalRangeGroup> queryClinicalRequireDrugGroup(String clinicalId);
	/**
	 * 
	 * 功能描述：查找临床路径必需项目组
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月10日 上午9:45:42</p>
	 *
	 * @param clinicalId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalClinicalRangeGroup> queryClinicalRequireTreatGroup(String clinicalId);
	
	/**
	 * 
	 * 功能描述：按批次号查找合理用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月12日 下午9:14:55</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDruguse> queryMedicalDruguseByBatchid(String batchId);
	
	/**
	 * 
	 * 功能描述：查找失败部分的规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月1日 上午9:55:45</p>
	 *
	 * @param dto
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDrugRule> queryMedicalDrugRuleFail(QryMedicalDrugRuleDTO dto);
	
	/**
	 * 
	 * 功能描述：查找合理用药失败部分的规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月1日 上午10:22:45</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDruguse> queryMedicalDruguseFail(String batchId);
	
	List<MedicalRuleConfig> queryMedicalRuleConfigByBatchid(QryMedicalRuleConfigDTO dto);
	List<MedicalRuleConfig> queryMedicalRuleConfigFail(QryMedicalRuleConfigDTO dto);
	
	/**
	 * 
	 * 功能描述：查找重复用药失败部分的规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月11日 下午5:49:00</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalYbDrug> queryMedicalDrugrepeatFail(@Param("batchId") String batchId);
	
	List<String> queryTaskBatchBreakRuleIds(@Param("batchId") String batchId, @Param("ruleType") String ruleType);
	List<String> queryTaskBatchBreakRuleFailIds(@Param("batchId") String batchId, @Param("ruleType") String ruleType);
}
