package com.ai.modules.ybChargeSearch.entity;

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
 * @Description: 收费明细查询历史分析表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_search_history")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_search_history对象", description="收费明细查询历史分析表")
public class YbChargeSearchHistory {


	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
	private java.lang.String createUserId;

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;

	/**机构*/
	@Excel(name = "机构", width = 15)
    @ApiModelProperty(value = "机构")
	private java.lang.String orgs;

	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgids;
	/**记录数*/
	@Excel(name = "记录数", width = 15)
    @ApiModelProperty(value = "记录数")
	private java.lang.Integer recordCount;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;

	/**收费项目名称*/
	@Excel(name = "收费项目名称", width = 15)
	@ApiModelProperty(value = "收费项目名称")
	private java.lang.String itemname;
	/**重复收费项目名称*/
	@Excel(name = "重复收费项目名称", width = 15)
	@ApiModelProperty(value = "重复收费项目名称")
	private java.lang.String itemname1;

	/**收费项目名称类型*/
	@Excel(name = "收费项目名称类型", width = 15)
	@ApiModelProperty(value = "收费项目名称类型")
	@MedicalDict(dicCode = "ITEM_TYPE")
	private java.lang.String itemType;

	/**重复收费类型*/
	@Excel(name = "重复收费类型", width = 15)
	@ApiModelProperty(value = "重复收费类型")
	@MedicalDict(dicCode = "ITEM1_TYPE")
	private java.lang.String item1Type;

	/**收费项目B违规判断*/
	@Excel(name = "收费项目B违规判断", width = 15)
	@ApiModelProperty(value = "收费项目B违规判断")
	@MedicalDict(dicCode = "ITEM1_WGTYPE")
	private java.lang.String item1Wgtype;


	/**是否输出同一天的手术项目(1是0否)*/
	@Excel(name = "是否输出同一天的手术项目(1是0否)", width = 15)
	@ApiModelProperty(value = "是否输出同一天的手术项目(1是0否)")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String isSameDay;

	/**超量检查的类型*/
	@Excel(name = "超量检查的类型", width = 15)
	@ApiModelProperty(value = "超量检查的类型")
	@MedicalDict(dicCode = "QTY_TYPE")
	private java.lang.String qtyType;

	/**超量的数值(不含)*/
	@Excel(name = "超量的数值(不含)", width = 15)
	@ApiModelProperty(value = "超量的数值(不含)")
	private java.lang.Integer qtyNum;

	/**金额累计*/
	@Excel(name = "金额累计", width = 15)
	@ApiModelProperty(value = "金额累计")
	private java.math.BigDecimal totalFee;

	@TableField(exist = false)
	private java.lang.Integer queryCount;
	@TableField(exist = false)
	private java.lang.String isRule;




}
