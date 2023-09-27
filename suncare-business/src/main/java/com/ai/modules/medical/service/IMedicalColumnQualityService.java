/**
 * IMedicalColumnQualityService.java	  V1.0   2021年3月15日 上午11:02:25
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.medical.entity.MedicalColumnQuality;
import com.ai.modules.medical.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 规则依赖字段质量表
 * @Author: jeecg-boot
 * @Date:   2021-03-16
 * @Version: V1.0
 */
public interface IMedicalColumnQualityService extends IService<MedicalColumnQuality> {
	void computeMedicalColumnQualityVO() throws Exception;
	
	void computeRuleRelyDtl(Map<String, MedicalActionDict> actionDictMap) throws Exception;

	/**
	 *
	 * 功能描述：查找模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月15日 下午2:14:47</p>
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
	 * <p>创建日期 ：2021年3月16日 上午10:30:33</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalDruguseColumnVO> queryMedicalDruguseColumn();

	boolean exportExcel(List<MedicalColumnQualityExportVO> listVO, OutputStream os, String suffix) throws Exception;

	boolean exportExcelSolr(List<DwbDataqualitySolrVO> listVO, OutputStream os, String suffix) throws Exception;
}
