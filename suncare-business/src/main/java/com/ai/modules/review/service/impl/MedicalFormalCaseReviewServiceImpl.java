package com.ai.modules.review.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ai.modules.review.entity.MedicalFormalCaseReview;
import com.ai.modules.review.mapper.MedicalFormalCaseReviewMapper;
import com.ai.modules.review.service.IMedicalFormalCaseReviewService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 不合理行为就诊记录审查表
 * @Author: jeecg-boot
 * @Date:   2019-12-26
 * @Version: V1.0
 */
@Service
public class MedicalFormalCaseReviewServiceImpl extends ServiceImpl<MedicalFormalCaseReviewMapper, MedicalFormalCaseReview> implements IMedicalFormalCaseReviewService {

	@Override
	public MedicalFormalCaseReview getByVisitId(String visitId) {
		QueryWrapper<MedicalFormalCaseReview> queryWrapper =new QueryWrapper<MedicalFormalCaseReview>();
		queryWrapper.eq("VISIT_ID", visitId);
		List<MedicalFormalCaseReview> list = this.baseMapper.selectList(queryWrapper);
		if(list.size()>0) {
			return list.get(0);
		}
		return null;
	}

}
