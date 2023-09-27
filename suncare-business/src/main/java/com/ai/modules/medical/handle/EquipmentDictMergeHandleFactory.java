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

import com.ai.modules.config.entity.MedicalEquipment;
import com.ai.modules.config.service.IMedicalEquipmentService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.equipment.CaseEquipmentDictMergeHandle;
import com.ai.modules.medical.handle.equipment.EquipmentDictMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：医疗器械合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EquipmentDictMergeHandleFactory extends AbsDictMergeHandleFactory {

	public EquipmentDictMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalEquipmentService service = context.getBean(IMedicalEquipmentService.class);
		List<MedicalEquipment> dataList = service.list(new QueryWrapper<MedicalEquipment>().eq("productcode", main));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到器材编码"+main);
		}
		MedicalEquipment mainEquipment = dataList.get(0);
		dataList = service.list(new QueryWrapper<MedicalEquipment>().eq("productcode", repeat));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到器材编码"+repeat);
		}
		MedicalEquipment repeatEquipment = dataList.get(0);
		
		//医疗器械字典
		AbsDictMergeHandle handle = context.getBean(EquipmentDictMergeHandle.class);
		handle.merge(main, repeat);
		//模型
		AbsCaseDictMergeHandle caseHandle = new CaseEquipmentDictMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
		
		this.addMedicalDictMergeLog(mainEquipment.getProductname(), repeatEquipment.getProductname(), "equipment", result);
		return result;
	}
}
