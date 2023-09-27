package com.ai.modules.formal.mapper;

import java.util.List;

import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 模型归纳规则列表
 * @Author: jeecg-boot
 * @Date:   2019-11-29
 * @Version: V1.0
 */
public interface MedicalFormalFlowRuleMapper extends BaseMapper<MedicalFormalFlowRule> {
	/**
	 * 
	 * 功能描述：按模型ID查询流程节点的所有查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月9日 上午10:23:44</p>
	 *
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNodeRule> queryMedicalFormalFlowRuleByCaseid(String caseId);
	
	List<EngineNodeRule> queryMedicalProbeFlowRuleByCaseid(String caseId);
}
