/**
 * CaseDiseaseDictMergeHandle.java	  V1.0   2021年7月6日 下午2:37:18
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.handle.org;

import java.util.List;

import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.AbsCaseDictMergeHandle;
import com.ai.modules.medical.handle.CaseDictMergeHandle;

/**
 * 
 * 功能描述：模型遇到医疗机构字典合并处理类
 *
 * @author  zhangly
 * Date: 2021年7月6日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class CaseOrgMergeHandle extends AbsCaseDictMergeHandle {

	@Override
	public List<DictMergeVO> merge(String main, String repeat) throws Exception {
		CaseDictMergeHandle handle = new CaseDictMergeHandle("ORGCODE");
		return handle.merge(main, repeat);
	}
}
