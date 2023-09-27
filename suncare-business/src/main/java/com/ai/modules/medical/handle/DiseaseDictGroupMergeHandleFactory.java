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

import com.ai.modules.config.entity.MedicalDiseaseGroup;
import com.ai.modules.config.service.IMedicalDiseaseGroupService;
import com.ai.modules.medical.handle.disease.CaseDiseaseDictGroupMergeHandle;
import com.ai.modules.medical.handle.disease.RuleDiseaseDictGroupMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：疾病组合并
 *
 * @author  zhangly
 * Date: 2022年4月13日
 * Copyright (c) 2022 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DiseaseDictGroupMergeHandleFactory extends AbsDictGroupMergeHandleFactory {

	public DiseaseDictGroupMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public void merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalDiseaseGroupService service = context.getBean(IMedicalDiseaseGroupService.class);
		List<MedicalDiseaseGroup> groupList = service.list(new QueryWrapper<MedicalDiseaseGroup>().eq("group_code", main));
		if(groupList==null || groupList.size()==0) {
			throw new Exception("未找到疾病组编码"+main);
		}
		MedicalDiseaseGroup mainGroup = groupList.get(0);
		groupList = service.list(new QueryWrapper<MedicalDiseaseGroup>().eq("group_code", repeat));
		if(groupList==null || groupList.size()==0) {
			throw new Exception("未找到疾病组编码"+repeat);
		}
		MedicalDiseaseGroup repeatGroup = groupList.get(0);
				
		//规则
		AbsDictMergeHandle handle = context.getBean(RuleDiseaseDictGroupMergeHandle.class);
		handle.merge(main, repeat);
		//模型
		AbsDictMergeHandle caseHandle = context.getBean(CaseDiseaseDictGroupMergeHandle.class);
		caseHandle.merge(main, repeat);
		//写日志		
		this.addMedicalDictMergeLog(mainGroup.getGroupName(), repeatGroup.getGroupName(), "diseaseGroup");
	}
}
