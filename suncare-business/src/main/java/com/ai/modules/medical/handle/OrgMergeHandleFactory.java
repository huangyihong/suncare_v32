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

import com.ai.modules.config.entity.MedicalOrgan;
import com.ai.modules.config.service.IMedicalOrganService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.handle.org.CaseOrgMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：医疗机构合并
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class OrgMergeHandleFactory extends AbsDictMergeHandleFactory {

	public OrgMergeHandleFactory(String main, String repeat) {
		super(main, repeat);
	}

	@Override
	public List<DictMergeVO> merge() throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IMedicalOrganService service = context.getBean(IMedicalOrganService.class);
		List<MedicalOrgan> orgList = service.list(new QueryWrapper<MedicalOrgan>().eq("code", main));
		if(orgList==null || orgList.size()==0) {
			throw new Exception("未找到医疗机构编码"+main);
		}
		MedicalOrgan mainOrg = orgList.get(0);
		orgList = service.list(new QueryWrapper<MedicalOrgan>().eq("code", repeat));
		if(orgList==null || orgList.size()==0) {
			throw new Exception("未找到医疗机构编码"+repeat);
		}
		MedicalOrgan repeatOrg = orgList.get(0);
		
		/*//医疗机构
		AbsDictMergeHandle handle = context.getBean(OrgMergeHandle.class);
		handle.merge(main, repeat);*/
		//模型
		AbsCaseDictMergeHandle caseHandle = new CaseOrgMergeHandle();
		List<DictMergeVO> result = caseHandle.merge(main, repeat);
		
		this.addMedicalDictMergeLog(mainOrg.getName(), repeatOrg.getName(), "org", result);
		return result;
	}
}
