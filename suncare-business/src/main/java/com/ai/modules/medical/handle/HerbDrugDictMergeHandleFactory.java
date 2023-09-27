/**
 * DiseaseDictMergeHandleFactory.java	  V1.0   2021年7月12日 下午5:00:30
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.handle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.config.entity.MedicalChineseDrug;
import com.ai.modules.config.service.IMedicalChineseDrugService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.drug.CaseDrugDictMergeHandle;
import com.ai.modules.medical.handle.drug.HerbDrugDictMergerHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：中草药合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HerbDrugDictMergeHandleFactory extends AbsDictMergeHandleFactory {

	public HerbDrugDictMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalChineseDrugService service = context.getBean(IMedicalChineseDrugService.class);
		List<MedicalChineseDrug> drugList = service.list(new QueryWrapper<MedicalChineseDrug>().eq("code", main));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到中草药药品编码"+main);
		}
		MedicalChineseDrug mainDrug = drugList.get(0);
		drugList = service.list(new QueryWrapper<MedicalChineseDrug>().eq("code", repeat));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到中草药药品编码"+repeat);
		}
		MedicalChineseDrug repeatDrug = drugList.get(0);
		//中草药字典
		AbsDictMergeHandle handle = context.getBean(HerbDrugDictMergerHandle.class);
		handle.merge(main, repeat);
		//模型
		AbsCaseDictMergeHandle caseHandle = new CaseDrugDictMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
		
		this.addMedicalDictMergeLog(mainDrug.getName(), repeatDrug.getName(), "herb", result);
		return result;
	}
}
