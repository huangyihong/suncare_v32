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

import com.ai.modules.config.entity.MedicalOperation;
import com.ai.modules.config.service.IMedicalOperationService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.operation.CaseOperationDictMergeHandle;
import com.ai.modules.medical.handle.operation.ClinicalOperatioinDictMergeHandle;
import com.ai.modules.medical.handle.operation.OperationDicMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：手术合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class OperationDictMergeHandleFactory extends AbsDictMergeHandleFactory {

	public OperationDictMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalOperationService service = context.getBean(IMedicalOperationService.class);
		List<MedicalOperation> dataList = service.list(new QueryWrapper<MedicalOperation>().eq("code", main));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到手术编码"+main);
		}
		MedicalOperation mainOperation = dataList.get(0);
		dataList = service.list(new QueryWrapper<MedicalOperation>().eq("code", repeat));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到手术编码"+repeat);
		}
		MedicalOperation repeatOperation = dataList.get(0);
		
		Set<Class<? extends AbsDictMergeHandle>> clazzSet = new HashSet<Class<? extends AbsDictMergeHandle>>();
		//手术字典
		clazzSet.add(OperationDicMergeHandle.class);
		//临床路径
		clazzSet.add(ClinicalOperatioinDictMergeHandle.class);
		for(Class<? extends AbsDictMergeHandle> clazz : clazzSet) {
			AbsDictMergeHandle handle = context.getBean(clazz);
			handle.merge(main, repeat);
		}		
		//模型
		AbsCaseDictMergeHandle caseHandle = new CaseOperationDictMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
		
		this.addMedicalDictMergeLog(mainOperation.getName(), repeatOperation.getName(), "operation", result);
		return result;
	}
}
