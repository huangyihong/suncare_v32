package com.ai.modules.medical.entity;

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
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 临床路径主体
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_CLINICAL")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_CLINICAL对象", description="临床路径主体")
public class MedicalClinical {

	/**临床路径ID*/
	@Excel(name = "临床路径ID", width = 15)
    @ApiModelProperty(value = "临床路径ID")
	@TableId("CLINICAL_ID")
	private java.lang.String clinicalId;
	/**临床路径名称*/
	@Excel(name = "临床路径名称", width = 15)
    @ApiModelProperty(value = "临床路径名称")
	private java.lang.String clinicalName;
	/**临床路径编码*/
	@Excel(name = "临床路径编码", width = 15)
    @ApiModelProperty(value = "临床路径编码")
	private java.lang.String clinicalCode;
	/**序号*/
	@Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "序号")
	private Double orderNo;
	/**住院天数(最大值)*/
	@Excel(name = "住院天数(最大值)", width = 15)
    @ApiModelProperty(value = "住院天数(最大值)")
	private java.lang.Integer inhospDaysMax;
	/**住院天数(最小值)*/
	@Excel(name = "住院天数(最小值)", width = 15)
    @ApiModelProperty(value = "住院天数(最小值)")
	private java.lang.Integer inhospDaysMin;
	/**住院费用(最大值)*/
	@Excel(name = "住院费用(最大值)", width = 15)
    @ApiModelProperty(value = "住院费用(最大值)")
	private Double inhospPayMax;
	/**住院费用(最小值)*/
	@Excel(name = "住院费用(最小值)", width = 15)
    @ApiModelProperty(value = "住院费用(最小值)")
	private Double inhospPayMin;
	/**公示年份*/
	@Excel(name = "公示年份", width = 15)
    @ApiModelProperty(value = "公示年份")
	private java.lang.String publicYear;
	/**数据有效性 1有效0无效*/
	@Excel(name = "数据有效性 1有效0无效", width = 15)
    @ApiModelProperty(value = "数据有效性 1有效0无效")
	private java.lang.String dataStatus;
	/**发布状态 1发布0未发布*/
	@Excel(name = "发布状态 1发布0未发布", width = 15)
    @ApiModelProperty(value = "发布状态 1发布0未发布")
	private java.lang.String publicStatus;
	/**修改人用户名*/
	@Excel(name = "修改人用户名", width = 15)
    @ApiModelProperty(value = "修改人用户名")
	private java.lang.String updateUser;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUsername;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**创建人用户名*/
	@Excel(name = "创建人用户名", width = 15)
    @ApiModelProperty(value = "创建人用户名")
	private java.lang.String createUser;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;

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

	/**不合理行为ID*/
	@Excel(name = "不合理行为ID", width = 15)
	@ApiModelProperty(value = "不合理行为ID")
	@MedicalDict(dicCode = "ACTION_LIST")
	private java.lang.String actionId;
	/**不合理行为ID*/
	@Excel(name = "不合理行为ID", width = 15)
	@ApiModelProperty(value = "不合理行为名称")
	private java.lang.String actionName;
	/**不合理行为类型*/
	@Excel(name = "不合理行为类型", width = 15)
	@ApiModelProperty(value = "不合理行为类型")
	@MedicalDict(dicCode = "ACTION_TYPE")
	private java.lang.String actionType;

	/**修改原因*/
	@Excel(name = "修改原因", width = 15)
	@ApiModelProperty(value = "修改原因")
	private java.lang.String updateReason;
}
