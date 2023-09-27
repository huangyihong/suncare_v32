/**
 * OrgMergeHandle.java	  V1.0   2021年7月16日 下午4:09:40
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.handle.org;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.modules.config.entity.MedicalOrgan;
import com.ai.modules.config.service.IMedicalOrganService;
import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 *
 * 功能描述：医疗机构合并
 *
 * @author  zhangly
 * Date: 2021年7月16日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
@Transactional
public class OrgMergeHandle extends AbsDictMergeHandle {
	@Autowired
	IMedicalOrganService service;
	@Autowired
	ITaskProjectBatchService batchSV;

	@Override
	public void merge(String main, String repeat) throws Exception {
		List<MedicalOrgan> orgList = service.list(new QueryWrapper<MedicalOrgan>().eq("code", main));
		if(orgList==null || orgList.size()==0) {
			throw new Exception("未找到医疗机构编码"+main);
		}
		//修改批次筛选的医疗机构
		QueryWrapper<TaskProjectBatch> wrapper = new QueryWrapper<TaskProjectBatch>();
//		wrapper.inSql("batch_id", "select batch_id from task_project_batch where instr(concat(concat(',', custom_filter), ','), ',"+repeat+",')>0");
		wrapper.inSql("batch_id", "select BATCH_ID from task_project_batch where locate(',"+repeat+",' , concat(concat(',', custom_filter), ','))>0");
		List<TaskProjectBatch> dataList = batchSV.list(wrapper);
		if(dataList.size()>0) {
			for(TaskProjectBatch batch : dataList) {
				String value = ","+batch.getCustomFilter()+",";
				if(value.contains(","+main+",")) {
					value = StringUtils.replace(value, ","+repeat+",", ",");
				} else {
					value = StringUtils.replace(value, ","+repeat+",", ","+main+",");
				}
				value = value.substring(1, value.length()-1);
				batch.setCustomFilter(value);
			}
			batchSV.updateBatchById(dataList);
		}
	}
}
