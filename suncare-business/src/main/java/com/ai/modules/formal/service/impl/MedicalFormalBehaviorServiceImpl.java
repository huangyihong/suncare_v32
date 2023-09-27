package com.ai.modules.formal.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.utils.IdUtils;
import com.ai.modules.formal.entity.MedicalFormalBehavior;
import com.ai.modules.formal.entity.MedicalFormalCaseBehavior;
import com.ai.modules.formal.mapper.MedicalFormalBehaviorMapper;
import com.ai.modules.formal.mapper.MedicalFormalCaseBehaviorMapper;
import com.ai.modules.formal.service.IMedicalFormalBehaviorService;
import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 不合规行为配置
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@Service
public class MedicalFormalBehaviorServiceImpl extends ServiceImpl<MedicalFormalBehaviorMapper, MedicalFormalBehavior> implements IMedicalFormalBehaviorService {
	@Autowired
	MedicalFormalBehaviorMapper medicalFormalBehaviorMapper;

	@Autowired
	MedicalFormalCaseBehaviorMapper medicalFormalCaseBehaviorMapper;

	/**
	 * 保存不合规行为及不合规行为模型关联信息
	 */
	@Override
	public void saveBehaviorAndCaseBehavior(MedicalFormalBehaviorVO bean) {
		medicalFormalBehaviorMapper.insert(bean);
		//保存不合规行为模型关联信息
		saveBatchCaseBehavior(bean);

	}

	@Override
	public void updateBehaviorAndCaseBehavior(MedicalFormalBehaviorVO bean) {
		medicalFormalBehaviorMapper.updateById(bean);
		//删除不合规行为模型关联信息
		medicalFormalCaseBehaviorMapper.delete(new QueryWrapper<MedicalFormalCaseBehavior>().eq("BEHAVIOR_ID", bean.getId()));
		//保存不合规行为模型关联信息
		saveBatchCaseBehavior(bean);
	}

	private void saveBatchCaseBehavior(MedicalFormalBehaviorVO bean) {
		if(StringUtils.isNotBlank(bean.getCaseIds())) {
			String[] caseIds_arr = bean.getCaseIds().split(",");
			for(String caseId:caseIds_arr) {
				MedicalFormalCaseBehavior caseBehaviorBean = new MedicalFormalCaseBehavior();
				caseBehaviorBean.setRelaId(IdUtils.uuid());
				caseBehaviorBean.setCaseId(caseId);
				caseBehaviorBean.setBehaviorId(bean.getId());
				medicalFormalCaseBehaviorMapper.insert(caseBehaviorBean);
			}
		}
	}

	@Override
	public void removeBehaviorAndCaseBehaviorById(String id) {
		medicalFormalBehaviorMapper.deleteById(id);
		//删除不合规行为模型关联信息
		medicalFormalCaseBehaviorMapper.delete(new QueryWrapper<MedicalFormalCaseBehavior>().eq("BEHAVIOR_ID", id));
	}

	@Override
	public void removeBehaviorAndCaseBehaviorByIds(List<String> idList) {
		medicalFormalBehaviorMapper.deleteBatchIds(idList);
		//删除不合规行为模型关联信息
		medicalFormalCaseBehaviorMapper.delete(new QueryWrapper<MedicalFormalCaseBehavior>().in("BEHAVIOR_ID",idList));
	}

	@Override
	public void importByBatchId(JSONObject obj, LoginUser user) {
		String selectBatchId = (String)obj.get("selectBatchId");

		QueryWrapper<MedicalFormalBehavior> queryWrapper = new QueryWrapper<MedicalFormalBehavior>();
		queryWrapper.eq("BATCH_ID", selectBatchId);
		List<MedicalFormalBehavior> list = this.list(queryWrapper);
		for(MedicalFormalBehavior bean:list) {
			MedicalFormalBehavior newBean = new MedicalFormalBehavior();
			BeanUtils.copyProperties(bean,newBean);
			newBean.setId(IdUtils.uuid());
			newBean.setBatchId((String)obj.get("batchId"));
			newBean.setCustName((String)obj.get("custName"));
			newBean.setCreateTime(new Date());
			newBean.setCreateUserid(user.getId());
			newBean.setCreateUsername(user.getRealname());
			newBean.setUpdateTime(null);
			newBean.setUpdateUserid("");
			newBean.setUpdateUsername("");
			medicalFormalBehaviorMapper.insert(newBean);
			List<MedicalFormalCaseBehavior> caseList = medicalFormalCaseBehaviorMapper.selectList(new QueryWrapper<MedicalFormalCaseBehavior>().eq("BEHAVIOR_ID", bean.getId()));
			for(MedicalFormalCaseBehavior caseBean:caseList) {
				MedicalFormalCaseBehavior caseBehaviorBean = new MedicalFormalCaseBehavior();
				caseBehaviorBean.setRelaId(IdUtils.uuid());
				caseBehaviorBean.setCaseId(caseBean.getCaseId());
				caseBehaviorBean.setBehaviorId(newBean.getId());
				medicalFormalCaseBehaviorMapper.insert(caseBehaviorBean);
			}
		}

	}

	@Override
	public List<MedicalFormalBehavior> listByOrder(String batchId, String type) {
		return this.list(
				new QueryWrapper<MedicalFormalBehavior>()
						.eq("BATCH_ID", batchId)
						.eq("STATUS","2")
						.eq("ACTION_TYPE",type)
						.orderByAsc("ID"));
	}

	@Override
	public List<MedicalFormalBehavior> listByOrder(String batchId) {
		return this.list(
				new QueryWrapper<MedicalFormalBehavior>()
						.eq("BATCH_ID", batchId)
						.eq("STATUS","2")
						.orderByAsc("ACTION_TYPE","ID"));
	}

	@Override
	public List<MedicalFormalBehavior> selectByBatchCase(String batchId, String[] caseIds) {
		return this.baseMapper.selectByBatchCase(batchId, caseIds);
	}

	@Override
	public List<MedicalFormalBehaviorVO> selectBehaviorCaseByBatch(String batchId) {
		return this.baseMapper.selectBehaviorCaseByBatch(batchId);
	}

}
