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

import java.util.List;

import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.service.IMedicalDrugService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.drug.CaseDrugDictMergeHandle;
import com.ai.modules.medical.handle.drug.DrugDictMergerHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：药品合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DrugDictMergeHandleFactory extends AbsDictMergeHandleFactory {

	public DrugDictMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalDrugService service = context.getBean(IMedicalDrugService.class);
		List<MedicalDrug> drugList = service.list(new QueryWrapper<MedicalDrug>().eq("code", main));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到药品编码"+main);
		}
		MedicalDrug mainDrug = drugList.get(0);
		drugList = service.list(new QueryWrapper<MedicalDrug>().eq("code", repeat));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到药品编码"+repeat);
		}
		MedicalDrug repeatDrug = drugList.get(0);
		//药品字典
		AbsDictMergeHandle handle = context.getBean(DrugDictMergerHandle.class);
		handle.merge(main, repeat);
		//模型
		AbsCaseDictMergeHandle caseHandle = new CaseDrugDictMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
		
		this.addMedicalDictMergeLog(mainDrug.getName(), repeatDrug.getName(), "drug", result);
		return result;
	}
}
