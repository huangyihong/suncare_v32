package com.ai.modules.review.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 不合理行为就诊记录审查表
 * @Author: jeecg-boot
 * @Date:   2019-12-26
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_FORMAL_CASE_REVIEW")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_FORMAL_CASE_REVIEW对象", description="不合理行为就诊记录审查表")
public class MedicalFormalCaseReview {
    
	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	private java.lang.String reviewId;
	/**就诊ID*/
	@Excel(name = "就诊ID", width = 15)
    @ApiModelProperty(value = "就诊ID")
	private java.lang.String visitId;
	/**审查名称*/
	@Excel(name = "审查名称", width = 15)
    @ApiModelProperty(value = "审查名称")
	private java.lang.String reviewName;
	/**第一次审查人ID*/
	@Excel(name = "第一次审查人ID", width = 15)
    @ApiModelProperty(value = "第一次审查人ID")
	private java.lang.String firReviewUserid;
	/**第一次审查人姓名*/
	@Excel(name = "第一次审查人姓名", width = 15)
    @ApiModelProperty(value = "第一次审查人姓名")
	private java.lang.String firReviewUsername;
	/**第一次审查时间*/
	@Excel(name = "第一次审查时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "第一次审查时间")
	private java.util.Date firReviewTime;
	/**第一次审查状态{wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}*/
	@Excel(name = "第一次审查状态{wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}", width = 15)
    @ApiModelProperty(value = "第一次审查状态{wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}")
	private java.lang.String firReviewStatus;
	/**第一次审查备注*/
	@Excel(name = "第一次审查备注", width = 15)
    @ApiModelProperty(value = "第一次审查备注")
	private java.lang.String firReviewRemark;
	/**是否推送{1:是,0:否}*/
	@Excel(name = "是否推送{1:是,0:否}", width = 15)
    @ApiModelProperty(value = "是否推送{1:是,0:否}")
	private java.lang.String pushStatus;
	/**推送人*/
	@Excel(name = "推送人", width = 15)
    @ApiModelProperty(value = "推送人")
	private java.lang.String pushUserid;
	/**第二次审查人ID*/
	@Excel(name = "第二次审查人ID", width = 15)
    @ApiModelProperty(value = "第二次审查人ID")
	private java.lang.String secReviewUserid;
	/**第二次审查人姓名*/
	@Excel(name = "第二次审查人姓名", width = 15)
    @ApiModelProperty(value = "第二次审查人姓名")
	private java.lang.String secReviewUsername;
	/**第二次审查时间*/
	@Excel(name = "第二次审查时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "第二次审查时间")
	private java.util.Date secReviewTime;
	/**第二次审查状态{wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}*/
	@Excel(name = "第二次审查状态{wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}", width = 15)
    @ApiModelProperty(value = "第二次审查状态{wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}")
	private java.lang.String secReviewStatus;
	/**第二次审查备注*/
	@Excel(name = "第二次审查备注", width = 15)
    @ApiModelProperty(value = "第二次审查备注")
	private java.lang.String secReviewRemark;
}
