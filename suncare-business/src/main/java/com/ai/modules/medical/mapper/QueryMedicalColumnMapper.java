/**
 * QueryMedicalFormalFlowRuleGradeMapper.java	  V1.0   2020年5月9日 下午4:23:57
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.mapper;

import java.util.List;

import com.ai.modules.medical.entity.MedicalRuleRelyDtl;
import com.ai.modules.medical.vo.MedicalCaseColumnVO;
import com.ai.modules.medical.vo.MedicalDruguseColumnVO;
import com.ai.modules.medical.vo.MedicalRuleConfigColumnVO;
import com.ai.modules.task.entity.TaskProjectBatch;

public interface QueryMedicalColumnMapper {
	/**
	 * 
	 * 功能描述：查找模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月15日 上午11:19:00</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalCaseColumnVO> queryMedicalCaseColumn();
	
	/**
	 * 
	 * 功能描述：新版收费合规、药品合规、合理诊疗规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月16日 下午2:13:05</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalRuleConfigColumnVO> queryMedicalRuleConfigColumn();
	
	/**
	 * 
	 * 功能描述：查找合理用药规则
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月16日 上午10:27:06</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDruguseColumnVO> queryMedicalDruguseColumn();
	
	List<MedicalRuleRelyDtl> queryMedicalRuleRelyDtlByProject(TaskProjectBatch batch);
	List<MedicalRuleRelyDtl> queryMedicalRuleRelyDtlByBatch(TaskProjectBatch batch);
}
