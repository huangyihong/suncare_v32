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

import com.ai.modules.config.entity.MedicalTreatProject;
import com.ai.modules.config.service.IMedicalTreatProjectService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.treat.CaseTreatDictMergeHandle;
import com.ai.modules.medical.handle.treat.ClinicalTreatDictMergeHandle;
import com.ai.modules.medical.handle.treat.TreatDictMergerHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：诊疗项目合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class TreatDictMergeHandleFactory extends AbsDictMergeHandleFactory {

	public TreatDictMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalTreatProjectService service = context.getBean(IMedicalTreatProjectService.class);
		List<MedicalTreatProject> dataList = service.list(new QueryWrapper<MedicalTreatProject>().eq("code", main));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到项目编码"+main);
		}
		MedicalTreatProject mainTreat = dataList.get(0);
		dataList = service.list(new QueryWrapper<MedicalTreatProject>().eq("code", repeat));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到项目编码"+repeat);
		}
		MedicalTreatProject repeatTreat = dataList.get(0);
		
		Set<Class<? extends AbsDictMergeHandle>> clazzSet = new HashSet<Class<? extends AbsDictMergeHandle>>();
		//诊疗项目字典
		clazzSet.add(TreatDictMergerHandle.class);
		//临床路径
		clazzSet.add(ClinicalTreatDictMergeHandle.class);
		for(Class<? extends AbsDictMergeHandle> clazz : clazzSet) {
			AbsDictMergeHandle handle = context.getBean(clazz);
			handle.merge(main, repeat);
		}
		//模型
		AbsCaseDictMergeHandle caseHandle = new CaseTreatDictMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
		
		this.addMedicalDictMergeLog(mainTreat.getName(), repeatTreat.getName(), "treat", result);
		return result;
	}
}
