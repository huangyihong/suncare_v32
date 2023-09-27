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
 * @Description: 不合理行为就诊记录审查日志表
 * @Author: jeecg-boot
 * @Date:   2020-02-07
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_FORMAL_CASE_REVIEW_LOG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_FORMAL_CASE_REVIEW_LOG对象", description="不合理行为就诊记录审查日志表")
public class MedicalFormalCaseReviewLog {
    
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**不合规行为结果记录主键id*/
	@Excel(name = "不合规行为结果记录主键id", width = 15)
    @ApiModelProperty(value = "不合规行为结果记录主键id")
	private java.lang.String reviewId;
	/**流程状态*/
	@Excel(name = "流程状态", width = 15)
    @ApiModelProperty(value = "流程状态")
	private java.lang.String secReviewStatus;
	/**操作类型*/
	@Excel(name = "操作类型", width = 15)
    @ApiModelProperty(value = "操作类型")
	private java.lang.String type;
	/**操作内容*/
	@Excel(name = "操作内容", width = 15)
    @ApiModelProperty(value = "操作内容")
	private java.lang.String content;
	/**操作结果*/
	@Excel(name = "操作结果", width = 15)
    @ApiModelProperty(value = "操作结果")
	private java.lang.String result;
	/**操作原因意见*/
	@Excel(name = "操作原因意见", width = 15)
    @ApiModelProperty(value = "操作原因意见")
	private java.lang.String remark;
	/**操作人ID*/
	@Excel(name = "操作人ID", width = 15)
    @ApiModelProperty(value = "操作人ID")
	private java.lang.String createUserid;
	/**操作人*/
	@Excel(name = "操作人", width = 15)
    @ApiModelProperty(value = "操作人")
	private java.lang.String createUsername;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "操作时间")
	private java.util.Date createTime;
	/**就诊ID*/
	@Excel(name = "就诊ID", width = 15)
    @ApiModelProperty(value = "就诊ID")
	private java.lang.String visitId;
}
