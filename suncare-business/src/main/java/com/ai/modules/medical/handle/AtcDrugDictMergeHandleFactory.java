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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.config.entity.MedicalStdAtc;
import com.ai.modules.config.service.IMedicalStdAtcService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.drug.AtcDrugDictMergerHandle;
import com.ai.modules.medical.handle.drug.CaseDrugDictMergeHandle;
import com.ai.modules.medical.handle.drug.RuleDrugDictMergerHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：atc药品合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class AtcDrugDictMergeHandleFactory extends AbsDictMergeHandleFactory {

	public AtcDrugDictMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalStdAtcService service = context.getBean(IMedicalStdAtcService.class);
		List<MedicalStdAtc> drugList = service.list(new QueryWrapper<MedicalStdAtc>().eq("code", main));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到ATC药品编码"+main);
		}
		MedicalStdAtc mainDrug = drugList.get(0);
		drugList = service.list(new QueryWrapper<MedicalStdAtc>().eq("code", repeat));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到ATC药品编码"+repeat);
		}
		MedicalStdAtc repeatDrug = drugList.get(0);
		
		Set<Class<? extends AbsDictMergeHandle>> clazzSet = new HashSet<Class<? extends AbsDictMergeHandle>>();
		//atc药品字典
		clazzSet.add(AtcDrugDictMergerHandle.class);
		//规则
		clazzSet.add(RuleDrugDictMergerHandle.class);
		for(Class<? extends AbsDictMergeHandle> clazz : clazzSet) {
			AbsDictMergeHandle handle = context.getBean(clazz);
			handle.merge(main, repeat);
		}
		//模型
		AbsCaseDictMergeHandle caseHandle = new CaseDrugDictMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
		
		this.addMedicalDictMergeLog(mainDrug.getName(), repeatDrug.getName(), "atc", result);
		return result;
	}
}
