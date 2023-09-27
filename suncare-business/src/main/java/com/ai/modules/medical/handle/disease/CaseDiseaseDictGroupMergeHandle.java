/**
 * CaseDiseaseDictMergeHandle.java	  V1.0   2021年7月6日 下午2:37:18
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.handle.disease;

import org.springframework.stereotype.Service;

import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.ai.modules.medical.handle.CaseDictGroupMergeHandle;

/**
 * 
 * 功能描述：模型遇到疾病组字典合并处理类
 *
 * @author  zhangly
 * Date: 2022年4月13日
 * Copyright (c) 2022 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
public class CaseDiseaseDictGroupMergeHandle extends AbsDictMergeHandle {

	@Override
	public void merge(String main, String repeat) throws Exception {
		CaseDictGroupMergeHandle handle = new CaseDictGroupMergeHandle("DWB_DIAG", "DISEASECODEGROUP");
		handle.merge(main, repeat);
		handle = new CaseDictGroupMergeHandle("STD_DIAGGROUP", "DIAGGROUP_CODE");
		handle.merge(main, repeat);
	}
}
