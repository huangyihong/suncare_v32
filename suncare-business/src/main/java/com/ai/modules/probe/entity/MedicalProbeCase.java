package com.ai.modules.probe.entity;

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
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 流程图
 * @Author: jeecg-boot
 * @Date:   2019-11-21
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_PROBE_CASE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_PROBE_CASE对象", description="流程图")
public class MedicalProbeCase {

	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	@TableId("CASE_ID")
	private java.lang.String caseId;
	/**编号*/
	@Excel(name = "编号", width = 15)
    @ApiModelProperty(value = "编号")
	private java.lang.String caseCode;
	/**探查名称*/
	@Excel(name = "探查名称", width = 15)
    @ApiModelProperty(value = "探查名称")
	private java.lang.String caseName;
	/**状态{wait:待提交,submited:提交}*/
	@Excel(name = "状态{wait:待提交,submited:提交}", width = 15)
    @ApiModelProperty(value = "状态{wait:待提交,submited:提交}")
//	@MedicalDict(dicCode = "SUBMIT_STATUS")
	private java.lang.String caseStatus;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String caseRemark;
	@Excel(name = "版本号", width = 15)
	@ApiModelProperty(value = "版本号")
	private java.lang.Float caseVersion;
	/**流程图json字符串*/
	@Excel(name = "流程图json字符串", width = 15)
    @ApiModelProperty(value = "流程图json字符串")
	private java.lang.String flowJson;
	/**创建人ID*/
	@Excel(name = "修改人ID", width = 15)
    @ApiModelProperty(value = "修改人ID")
	private java.lang.String updateUserid;
	/**创建人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateUsername;
	/**创建时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
	private java.lang.String createUserid;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUsername;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
}
