package com.ai.modules.review.service;

import com.ai.modules.review.entity.MedicalFormalCaseReview;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 不合理行为就诊记录审查表
 * @Author: jeecg-boot
 * @Date:   2019-12-26
 * @Version: V1.0
 */
public interface IMedicalFormalCaseReviewService extends IService<MedicalFormalCaseReview> {

	/**
	 * 根据就诊id获取审查记录
	 * @param visitId
	 * @return
	 */
	public MedicalFormalCaseReview getByVisitId(String visitId);

}
