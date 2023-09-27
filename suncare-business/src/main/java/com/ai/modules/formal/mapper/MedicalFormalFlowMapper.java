package com.ai.modules.formal.mapper;

import java.util.List;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.formal.entity.MedicalFormalFlow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 模型归纳节点列表
 * @Author: jeecg-boot
 * @Date:   2019-11-29
 * @Version: V1.0
 */
public interface MedicalFormalFlowMapper extends BaseMapper<MedicalFormalFlow> {
	/**
	 * 
	 * 功能描述：按模型ID递归查询流程节点
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月6日 下午2:55:04</p>
	 *
	 * @param caseId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<EngineNode> recursionMedicalFormalFlowByCaseid(String caseId);
	
	List<EngineNode> findMedicalFormalFlowByCaseid(String caseId);
}
