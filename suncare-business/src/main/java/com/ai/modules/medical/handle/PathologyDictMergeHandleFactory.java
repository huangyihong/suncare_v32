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

import com.ai.modules.config.entity.MedicalPathology;
import com.ai.modules.config.service.IMedicalPathologyService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.pathology.CasePathologyDictMergeHandle;
import com.ai.modules.medical.handle.pathology.ClinicalPathologyDictMergeHandle;
import com.ai.modules.medical.handle.pathology.PathologyDictMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：形态学合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class PathologyDictMergeHandleFactory extends AbsDictMergeHandleFactory {

	public PathologyDictMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalPathologyService service = context.getBean(IMedicalPathologyService.class);
		List<MedicalPathology> dataList = service.list(new QueryWrapper<MedicalPathology>().eq("code", main));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到形态学编码"+main);
		}
		MedicalPathology mainPathology = dataList.get(0);
		dataList = service.list(new QueryWrapper<MedicalPathology>().eq("code", repeat));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到形态学编码"+repeat);
		}
		MedicalPathology repeatPathology = dataList.get(0);
		
		Set<Class<? extends AbsDictMergeHandle>> clazzSet = new HashSet<Class<? extends AbsDictMergeHandle>>();
		//形态学字典
		clazzSet.add(PathologyDictMergeHandle.class);
		//临床路径
		clazzSet.add(ClinicalPathologyDictMergeHandle.class);
		for(Class<? extends AbsDictMergeHandle> clazz : clazzSet) {
			AbsDictMergeHandle handle = context.getBean(clazz);
			handle.merge(main, repeat);
		}
		//模型
		AbsCaseDictMergeHandle caseHandle = new CasePathologyDictMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
		
		this.addMedicalDictMergeLog(mainPathology.getName(), repeatPathology.getName(), "pathology", result);
		return result;
	}
}
