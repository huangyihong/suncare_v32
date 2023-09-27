package com.ai.modules.his.entity;

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
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 风控模型正式备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Data
@TableName(value="HIS_MEDICAL_FORMAL_CASE", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="HIS_MEDICAL_FORMAL_CASE对象", description="风控模型正式备份")
public class HisMedicalFormalCase {

	/**batchId*/
	@Excel(name = "batchId", width = 15)
    @ApiModelProperty(value = "batchId")
	private java.lang.String batchId;
	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	private java.lang.String caseId;
	/**编号*/
	@Excel(name = "编号", width = 15)
    @ApiModelProperty(value = "编号")
	private java.lang.String caseCode;
	/**模型名称*/
	@Excel(name = "模型名称", width = 15)
    @ApiModelProperty(value = "模型名称")
	private java.lang.String caseName;
	/**归纳流程编号*/
	@Excel(name = "归纳流程编号", width = 15)
	@ApiModelProperty(value = "归纳流程编号")
	private java.lang.String caseCodes;
	/**归纳流程名称*/
	@Excel(name = "归纳流程名称", width = 15)
	@ApiModelProperty(value = "归纳流程名称")
	private java.lang.String caseNames;
	/**状态{wait:待提交,submited:已提交,reject:驳回,stop:停用,lock:锁定,normal:正常}*/
	@Excel(name = "状态{wait:待提交,submited:已提交,reject:驳回,stop:停用,lock:锁定,normal:正常}", width = 15)
    @ApiModelProperty(value = "状态{wait:待提交,submited:已提交,reject:驳回,stop:停用,lock:锁定,normal:正常}")
	private java.lang.String caseStatus;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String caseRemark;
	@Excel(name = "版本号", width = 15)
	@ApiModelProperty(value = "版本号")
	private java.lang.Float caseVersion;
	@Excel(name = "模型归类", width = 15)
	@ApiModelProperty(value = "模型归类")
	private java.lang.String caseClassify;
	/**流程图json字符串*/
	@Excel(name = "流程图json字符串", width = 15)
    @ApiModelProperty(value = "流程图json字符串")
	private java.lang.String flowJson;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
	private java.lang.String createUserid;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUsername;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	@Excel(name = "修改备注", width = 15)
	@ApiModelProperty(value = "修改备注")
	private String updateRemark;
	/**提交人ID*/
	@Excel(name = "提交人ID", width = 15)
    @ApiModelProperty(value = "提交人ID")
	private java.lang.String submitUserid;
	/**提交人*/
	@Excel(name = "提交人", width = 15)
    @ApiModelProperty(value = "提交人")
	private java.lang.String submitUsername;
	/**提交时间*/
	@Excel(name = "提交时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "提交时间")
	private java.util.Date submitTime;
	/**不合理行为名称*/
	@Excel(name = "不合理行为名称", width = 15)
    @ApiModelProperty(value = "不合理行为名称")
	private java.lang.String actionName;
	/**不合理行为描述*/
	@Excel(name = "不合理行为描述", width = 15)
    @ApiModelProperty(value = "不合理行为描述")
	private java.lang.String actionDesc;
	@Excel(name = "政策依据", width = 15)
	@ApiModelProperty(value = "政策依据")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String ruleBasis;
	/**所属地区*/
	@Excel(name = "所属地区", width = 15)
	@ApiModelProperty(value = "所属地区")
	private java.lang.String ruleSource;
	/**所属地区编码*/
	@Excel(name = "所属地区编码", width = 15)
	@ApiModelProperty(value = "所属地区编码")
	private java.lang.String ruleSourceCode;
	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	/**开始时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "结束时间")
	private Date endTime;
	/**不合理行为组ID*/
	@Excel(name = "不合理行为ID", width = 15)
    @ApiModelProperty(value = "不合理行为ID")
	private java.lang.String actionId;
	/**不合理行为类型*/
	@Excel(name = "不合理行为类型", width = 15)
    @ApiModelProperty(value = "不合理行为类型")
	private java.lang.String actionType;
	/**不合理行为类型名称*/
	@Excel(name = "不合理行为类型名称", width = 15)
	@ApiModelProperty(value = "不合理行为类型名称")
	private java.lang.String actionTypeName;
	/**序列号*/
	@Excel(name = "序列号", width = 15)
    @ApiModelProperty(value = "序列号")
	private java.lang.Integer orderNo;
	/**数据生成状态(1数据未生成2数据正在生成中3数据已生成)*/
	@Excel(name = "数据生成状态(1数据未生成2数据正在生成中3数据已生成)", width = 15)
    @ApiModelProperty(value = "数据生成状态(1数据未生成2数据正在生成中3数据已生成)")
	private java.lang.Integer resultDataStatus;
	/**诊断ID数*/
	@Excel(name = "诊断ID数", width = 15)
    @ApiModelProperty(value = "诊断ID数")
	private java.lang.Long idDataCount;
	/**主体数*/
	@Excel(name = "主体数", width = 15)
    @ApiModelProperty(value = "主体数")
	private java.lang.Long objDataCount;
	/**金额*/
	@Excel(name = "金额", width = 15)
    @ApiModelProperty(value = "金额")
	private java.math.BigDecimal dataMoney;
	/**数据生成开始时间*/
	@Excel(name = "数据生成开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据生成开始时间")
	private java.util.Date resultStartTime;
	/**数据生成结束时间*/
	@Excel(name = "数据生成结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据生成结束时间")
	private java.util.Date resultEndTime;
	/**数据生成信息*/
	@Excel(name = "数据生成信息", width = 15)
    @ApiModelProperty(value = "数据生成信息")
	private java.lang.String resuleMessage;
	/**backTime*/
	@Excel(name = "backTime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "backTime")
	private java.util.Date backTime;

	/**规则级别*/
	@Excel(name = "规则级别", width = 15)
	@ApiModelProperty(value = "规则级别")
	private java.lang.String ruleGrade;

	/**级别备注*/
	@Excel(name = "级别备注", width = 15)
	@ApiModelProperty(value = "级别备注")
	private java.lang.String ruleGradeRemark;
}
