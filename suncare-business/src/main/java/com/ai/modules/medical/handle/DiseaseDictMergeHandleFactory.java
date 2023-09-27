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

import com.ai.modules.config.entity.MedicalDiseaseDiag;
import com.ai.modules.config.service.IMedicalDiseaseDiagService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.disease.CaseDiseaseDictMergeHandle;
import com.ai.modules.medical.handle.disease.DiseaseDictMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：疾病合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DiseaseDictMergeHandleFactory extends AbsDictMergeHandleFactory {

	public DiseaseDictMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalDiseaseDiagService service = context.getBean(IMedicalDiseaseDiagService.class);
		List<MedicalDiseaseDiag> diagList = service.list(new QueryWrapper<MedicalDiseaseDiag>().eq("code", main));
		if(diagList==null || diagList.size()==0) {
			throw new Exception("未找到疾病编码"+main);
		}
		MedicalDiseaseDiag mainDiag = diagList.get(0);
		diagList = service.list(new QueryWrapper<MedicalDiseaseDiag>().eq("code", repeat));
		if(diagList==null || diagList.size()==0) {
			throw new Exception("未找到疾病编码"+repeat);
		}
		MedicalDiseaseDiag repeatDiag = diagList.get(0);
				
		//疾病字典
		AbsDictMergeHandle handle = context.getBean(DiseaseDictMergeHandle.class);
		handle.merge(main, repeat);
		//模型
		AbsCaseDictMergeHandle caseHandle = new CaseDiseaseDictMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
				
		this.addMedicalDictMergeLog(mainDiag.getName(), repeatDiag.getName(), "disease", result);
		return result;
	}
}
