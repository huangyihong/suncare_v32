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
 * @Description: 违规案例库
 * @Author: jeecg-boot
 * @Date:   2023-01-13
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_case")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_case对象", description="违规案例库")
public class YbChargeCase {

	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**违规类型*/
	@Excel(name = "违规类型", width = 15)
    @ApiModelProperty(value = "违规类型")
	private java.lang.String wgType;
	/**项目编码（国家码）*/
	@Excel(name = "项目编码（国家码）", width = 15)
    @ApiModelProperty(value = "项目编码（国家码）")
	private java.lang.String itemCode;
	/**违规项目*/
	@Excel(name = "违规项目", width = 15)
    @ApiModelProperty(value = "违规项目")
	private java.lang.String wgItemName;
	/**项目关键词*/
	@Excel(name = "项目关键词", width = 15)
    @ApiModelProperty(value = "项目关键词")
	private java.lang.String itemname;
	/**违规描述*/
	@Excel(name = "违规描述", width = 15)
    @ApiModelProperty(value = "违规描述")
	private java.lang.String wgDescription;
	/**整理人*/
	@Excel(name = "整理人", width = 15)
    @ApiModelProperty(value = "整理人")
	private java.lang.String sorter;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createdBy;
	/**创建人名称*/
	@Excel(name = "创建人名称", width = 15)
    @ApiModelProperty(value = "创建人名称")
	private java.lang.String createdByName;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createdTime;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private java.lang.String updatedBy;
	/**更新人名称*/
	@Excel(name = "更新人名称", width = 15)
    @ApiModelProperty(value = "更新人名称")
	private java.lang.String updatedByName;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updatedTime;
	/**审核状态0.未审核1.已审核*/
	@Excel(name = "审核状态0.未审核1.已审核", width = 15)
	@MedicalDict(dicCode = "EXAMINE_STATUS")
    @ApiModelProperty(value = "审核状态0.未审核1.已审核")
	private java.lang.String examineStatus;

	/**政策依据*/
	@Excel(name = "政策依据", width = 15)
	@ApiModelProperty(value = "政策依据")
	private java.lang.String policyBasis;

	/**所属地区*/
	@Excel(name = "所属地区", width = 15)
	@ApiModelProperty(value = "所属地区")
	private java.lang.String region;

	/**所属开始时间*/
	@Excel(name = "所属开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "所属开始时间")
	private java.util.Date startdate;
	/**所属结束时间*/
	@Excel(name = "所属结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "所属结束时间")
	private java.util.Date enddate;

	/**是否可操作*/
	@TableField(exist = false)
	private Boolean isOperation;

	@TableField(exist = false)
	@Excel(name = "所属时间", width = 15)
	@ApiModelProperty(value = "所属时间")
	private java.lang.String startEndDateStr;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { //判断一下如果是同一个对象直接返回true，提高效率
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) { //如果传进来的对象为null或者二者为不同类，直接返回false
			return false;
		}
		YbChargeCase ybChargeCase=(YbChargeCase) obj;
		if(ybChargeCase.getWgType().equals(this.wgType) && ybChargeCase.itemCode.equals(this.itemCode) && ybChargeCase.wgItemName.equals(this.wgItemName)){
			return true;
		}
		return false;
	}

}
