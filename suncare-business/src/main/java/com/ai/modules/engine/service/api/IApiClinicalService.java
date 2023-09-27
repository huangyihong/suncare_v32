/**
 * IApiClinicalService.java	  V1.0   2020年12月28日 上午9:25:20
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api;

import java.util.List;

import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;

public interface IApiClinicalService {
	/**
	 * 
	 * 功能描述：查找临床路径信息
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午9:28:24</p>
	 *
	 * @param clinicalId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	MedicalClinical findMedicalClinical(String clinicalId);
	
	MedicalClinicalInfo findMedicalClinicalInfo(String clinicalId);
	
	/**
	 * 
	 * 功能描述：临床路径准入条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午9:42:48</p>
	 *
	 * @param clinicalId
	 * @param groupType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalClinicalAccessGroup> findMedicalClinicalAccessGroup(String clinicalId, String groupType);
	
	/**
	 * 
	 * 功能描述：查找临床路径药品、项目范围
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午9:48:20</p>
	 *
	 * @param clinicalId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalClinicalRangeGroup> findMedicalClinicalRangeGroup(String clinicalId);
	
	/**
	 * 
	 * 功能描述：查找临床路径必需包含的药品范围
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午10:24:55</p>
	 *
	 * @param clinicalId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalClinicalRangeGroup> findClinicalRequireDrugGroup(String clinicalId);
	
	/**
	 * 
	 * 功能描述：查找临床路径必需包含的项目范围
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月28日 上午10:24:55</p>
	 *
	 * @param clinicalId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<MedicalClinicalRangeGroup> findClinicalRequireTreatGroup(String clinicalId);
}
