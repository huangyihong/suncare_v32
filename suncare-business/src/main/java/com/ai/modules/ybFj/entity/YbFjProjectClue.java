package com.ai.modules.ybFj.entity;

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
 * @Description: 飞检项目线索
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_project_clue")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_project_clue对象", description="飞检项目线索")
public class YbFjProjectClue {
    
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String clueId;
	/**关联的飞检项目主键*/
	@Excel(name = "关联的飞检项目主键", width = 15)
    @ApiModelProperty(value = "关联的飞检项目主键")
	private java.lang.String projectId;
	/**关联yb_fj_project_org.project_org_id*/
	@Excel(name = "关联yb_fj_project_org.project_org_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_org.project_org_id")
	private java.lang.String projectOrgId;
	/**问题类别（一级）*/
	@Excel(name = "问题类别（一级）", width = 15)
    @ApiModelProperty(value = "问题类别（一级）")
	private java.lang.String issueType;
	/**问题类型（二级）*/
	@Excel(name = "问题类型（二级）", width = 15)
    @ApiModelProperty(value = "问题类型（二级）")
	private java.lang.String issueSubtype;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private java.lang.String clueName;
	/**项目类别*/
	@Excel(name = "项目类别", width = 15)
    @ApiModelProperty(value = "项目类别")
	private java.lang.String clueType;
	/**涉及数量*/
	@Excel(name = "涉及数量", width = 15)
    @ApiModelProperty(value = "涉及数量")
	private java.lang.Integer caseAmount;
	/**涉及人次*/
	@Excel(name = "涉及人次", width = 15)
    @ApiModelProperty(value = "涉及人次")
	private java.lang.Integer casePersonCnt;
	/**涉及金额（单位：元）*/
	@Excel(name = "涉及金额（单位：元）", width = 15)
    @ApiModelProperty(value = "涉及金额（单位：元）")
	private java.math.BigDecimal caseFee;
	/**涉及医保基金金额（单位：元）*/
	@Excel(name = "涉及医保基金金额（单位：元）", width = 15)
    @ApiModelProperty(value = "涉及医保基金金额（单位：元）")
	private java.math.BigDecimal caseFundFee;
	/**违规说明*/
	@Excel(name = "违规说明", width = 15)
    @ApiModelProperty(value = "违规说明")
	private java.lang.String caseRemark;
	/**审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)*/
	@Excel(name = "审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)", width = 15)
    @ApiModelProperty(value = "审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)，字典FJXMSHZT")
	private java.lang.String auditState;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
	private java.lang.String auditUser;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private java.lang.String auditUserName;
	/**审核时间*/
	@Excel(name = "审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
	private java.util.Date auditTime;
	/**审核意见*/
	@Excel(name = "审核意见", width = 15)
    @ApiModelProperty(value = "审核意见")
	private java.lang.String auditOpinion;
	/**医院复核环节推送人*/
	@Excel(name = "医院复核环节推送人", width = 15)
	@ApiModelProperty(value = "医院复核环节推送人")
	private java.lang.String hospStepUser;
	/**医院复核环节推送人姓名*/
	@Excel(name = "医院复核环节推送人姓名", width = 15)
	@ApiModelProperty(value = "医院复核环节推送人姓名")
	private java.lang.String hospStepUserName;
	/**医院复核环节推送时间*/
	@Excel(name = "医院复核环节推送时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "医院复核环节推送时间")
	private java.util.Date hospStepTime;
	/**医院复核环节状态(init:待反馈 accept:已认可 noaccept:不认可 cut 待核减)*/
	@Excel(name = "医院复核环节状态(init:待反馈 accept:已认可 noaccept:不认可 cut 待核减)", width = 15)
	@ApiModelProperty(value = "医院复核环节状态(init:待反馈 accept:已认可 noaccept:不认可 cut 待核减)")
	private java.lang.String hospAuditState;
	/**医院复核环节审核人*/
	@Excel(name = "医院复核环节审核人", width = 15)
	@ApiModelProperty(value = "医院复核环节审核人")
	private java.lang.String hospAuditUser;
	/**医院复核环节审核人姓名*/
	@Excel(name = "医院复核环节审核人姓名", width = 15)
	@ApiModelProperty(value = "医院复核环节审核人姓名")
	private java.lang.String hospAuditUserName;
	/**医院复核环节审核时间*/
	@Excel(name = "医院复核环节审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "医院复核环节审核时间")
	private java.util.Date hospAuditTime;
	/**医院复核环节审核意见*/
	@Excel(name = "医院复核环节审核意见", width = 15)
	@ApiModelProperty(value = "医院复核环节审核意见")
	private java.lang.String hospAuditOpinion;
	/**线上核查环节推送人*/
	@Excel(name = "线上核查环节推送人", width = 15)
	@ApiModelProperty(value = "线上核查环节推送人")
	private java.lang.String cutStepUser;
	/**线上核查环节推送人姓名*/
	@Excel(name = "线上核查环节推送人姓名", width = 15)
	@ApiModelProperty(value = "线上核查环节推送人姓名")
	private java.lang.String cutStepUserName;
	/**线上核查环节推送时间*/
	@Excel(name = "线上核查环节推送时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "线上核查环节推送时间")
	private java.util.Date cutStepTime;
	/**线上核减环节状态(init:待核减 finish 已核减)*/
	@Excel(name = "线上核减环节状态(init:待核减 finish 已核减)", width = 15)
	@ApiModelProperty(value = "线上核减环节状态(init:待核减 finish 已核减)")
	private java.lang.String cutAuditState;
	/**线上核减环节审核人*/
	@Excel(name = "线上核减环节审核人", width = 15)
	@ApiModelProperty(value = "线上核减环节审核人")
	private java.lang.String cutAuditUser;
	/**线上核减环节审核人姓名*/
	@Excel(name = "线上核减环节审核人姓名", width = 15)
	@ApiModelProperty(value = "线上核减环节审核人姓名")
	private java.lang.String cutAuditUserName;
	/**线上核减环节审核时间*/
	@Excel(name = "线上核减环节审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "线上核减环节审核时间")
	private java.util.Date cutAuditTime;
	/**线上核减环节审核意见*/
	@Excel(name = "线上核减环节审核意见", width = 15)
	@ApiModelProperty(value = "线上核减环节审核意见")
	private java.lang.String cutAuditOpinion;
	/**现场核查环节推送人*/
	@Excel(name = "现场核查环节推送人", width = 15)
	@ApiModelProperty(value = "现场核查环节推送人")
	private java.lang.String onsiteStepUser;
	/**现场核查环节推送人姓名*/
	@Excel(name = "现场核查环节推送人姓名", width = 15)
	@ApiModelProperty(value = "现场核查环节推送人姓名")
	private java.lang.String onsiteStepUserName;
	/**现场核查环节推送时间*/
	@Excel(name = "现场核查环节推送时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "现场核查环节推送时间")
	private java.util.Date onsiteStepTime;
	/**当前环节*/
	@Excel(name = "当前环节", width = 15)
	@ApiModelProperty(value = "当前环节{submit:线索提交,hosp:医院复核,cut:核减,onsite:现场核查}")
	private java.lang.String currStep;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
    @ApiModelProperty(value = "更新人姓名")
	private java.lang.String updateUsername;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private java.lang.String updateUser;
	/**明细上传状态{y:已上传,n:未上传}*/
	@Excel(name = "明细上传状态{y:已上传,n:未上传}", width = 15)
	@ApiModelProperty(value = "明细上传状态{y:已上传,n:未上传}")
	private java.lang.String dtlState;
	/**序号*/
	@Excel(name = "序号", width = 15)
	@ApiModelProperty(value = "序号")
	private java.lang.Integer seq;
	/**明细项目数量*/
	@Excel(name = "明细项目数量", width = 15)
	@ApiModelProperty(value = "明细项目数量")
	private java.lang.Integer dtlAmount;
	/**明细违规金额（单位：元）*/
	@Excel(name = "明细违规金额（单位：元）", width = 15)
	@ApiModelProperty(value = "明细违规金额（单位：元）")
	private java.math.BigDecimal dtlWgFee;
}
