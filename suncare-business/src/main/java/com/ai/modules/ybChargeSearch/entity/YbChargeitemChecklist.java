package com.ai.modules.ybChargeSearch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description: 收费明细风控检查内容
 * @Author: jeecg-boot
 * @Date:   2022-11-25
 * @Version: V1.0
 */
@Data
@TableName("yb_chargeitem_checklist")
@ApiModel(value="yb_chargeitem_checklist对象", description="收费明细风控检查内容")
public class YbChargeitemChecklist implements Serializable {
	private static final long serialVersionUID = 1L;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
	@ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**收费项目A关键字*/
	@Excel(name = "（必填）收费项目A关键字\n" +
			"（多项目用#分隔,通配符用&&)", width = 15)
    @ApiModelProperty(value = "收费项目A关键字")
	private java.lang.String itemname;
	/**收费项目B关键字*/
	@Excel(name = "(选填）收费项目B关键字\n" +
			"（多项目用#分隔,通配符用&& )", width = 15)
    @ApiModelProperty(value = "收费项目B关键字")
	private java.lang.String itemname1;
	/**收费项目名称类型*/
	@Excel(name = "（必填）收费项目名称类型（填:医保收费项目名称(默认)/HIS收费项目名称）", width = 15)
    @ApiModelProperty(value = "收费项目名称类型")
	@MedicalDict(dicCode = "ITEM_TYPE")
	private java.lang.String itemType;

	/**重复收费类型*/
	@Excel(name = "(选填）重复收费类型（填:同一天/同一次就诊）", width = 15)
	@ApiModelProperty(value = "重复收费类型")
	@MedicalDict(dicCode = "ITEM1_TYPE")
	private java.lang.String  item1Type;

	/**收费项目B违规判断*/
	@Excel(name = "(选填）收费项目B违规判断(填:B项目存在违规(重复收费)(默认)/A项目存在，B项目不存在违规)", width = 15)
	@ApiModelProperty(value = "收费项目B违规判断")
	@MedicalDict(dicCode = "ITEM1_WGTYPE")
	private java.lang.String item1Wgtype;


	/**是否输出同一天的手术项目*/
	@Excel(name = "(选填）是否输出同一天的手术项目（默认否）", width = 15)
	@ApiModelProperty(value = "是否输出同一天的手术项目")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String isSameDay;


	/**创建人*/
    @ApiModelProperty(value = "创建人")
	private java.lang.String createdBy;
	/**创建人名称*/
    @ApiModelProperty(value = "创建人名称")
	private java.lang.String createdByName;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createdTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
	private java.lang.String updatedBy;
	/**更新人名称*/
    @ApiModelProperty(value = "更新人名称")
	private java.lang.String updatedByName;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updatedTime;

	/**审核状态*/
	@MedicalDict(dicCode = "EXAMINE_STATUS")
	@ApiModelProperty(value = "审核状态")
	private java.lang.String examineStatus;

	/**审核人*/
	@ApiModelProperty(value = "审核人")
	private java.lang.String examineBy;

	/**审核时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "审核时间")
	private java.util.Date examineTime;

	/**超量检查的类型*/
	@Excel(name = "(选填）超量检查的类型(填:一天超量/一次就诊超量/一次就诊超过住院天数/一次就诊超过住院天数*24)", width = 15)
	@ApiModelProperty(value = "超量检查的类型")
	@MedicalDict(dicCode = "QTY_TYPE")
	private java.lang.String qtyType;

	/**超量的数值(不含)*/
	@Excel(name = "(选填）超量的数值(不含)", width = 15)
	@ApiModelProperty(value = "超量的数值(不含)")
	private java.lang.Integer qtyNum;

	/**收费项目A编码*/
	@Excel(name = "(选填）收费项目A编码", width = 15)
	@ApiModelProperty(value = "收费项目A编码")
	private java.lang.String itemCode1;

	/**收费项目A名称*/
	@Excel(name = "(必填）收费项目A名称", width = 15)
	@ApiModelProperty(value = "收费项目A名称")
	private java.lang.String packageItem1;

	/**收费项目B编码*/
	@Excel(name = "(选填）收费项目B编码", width = 15)
	@ApiModelProperty(value = "收费项目B编码")
	private java.lang.String itemCode2;

	/**收费项目B名称*/
	@Excel(name = "(选填）收费项目B名称", width = 15)
	@ApiModelProperty(value = "收费项目B名称")
	private java.lang.String packageItem2;

	/**违规案例提示*/
	@Excel(name = "(选填）违规案例提示", width = 15)
	@ApiModelProperty(value = "违规案例提示")
	private java.lang.String wgCaseExample;

	/**整理人*/
	@Excel(name = "(必填）整理人", width = 15)
	@ApiModelProperty(value = "整理人")
	private java.lang.String sorter;

	/**是否可操作*/
	@TableField(exist = false)
	private Boolean isOperation;

	@TableField(exist = false)
	private java.lang.Integer queryCount;
	@TableField(exist = false)
	private java.lang.Integer recordCount;
	@TableField(exist = false)
	private java.math.BigDecimal totalFee;

}
