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
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 上会材料详细表
 * @Author: jeecg-boot
 * @Date:   2022-11-29
 * @Version: V1.0
 */
@Data
@TableName("yb_meeting_materials_detail")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_meeting_materials_detail对象", description="上会材料详细表")
public class YbMeetingMaterialsDetail {

	/**问题类别(一级指标)*/
	@Excel(name = "问题类别(一级指标)", width = 15)
    @ApiModelProperty(value = "问题类别(一级指标)")
	private java.lang.String cat;
	/**违规第几款*/
	@Excel(name = "违规第几款", width = 15)
    @ApiModelProperty(value = "违规第几款")
	private java.lang.String clauseK;
	/**违规第几条*/
	@Excel(name = "违规第几条", width = 15)
    @ApiModelProperty(value = "违规第几条")
	private java.lang.String clauseT;
	/**违规情形描述*/
	@Excel(name = "违规情形描述", width = 15)
    @ApiModelProperty(value = "违规情形描述")
	private java.lang.String wgDesc;
	/**违规医保基金金额（元）*/
	@Excel(name = "违规医保基金金额（元）", width = 15)
    @ApiModelProperty(value = "违规医保基金金额（元）")
	private java.math.BigDecimal fundAmt;
	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**违规项目*/
	@Excel(name = "违规项目", width = 15)
    @ApiModelProperty(value = "违规项目")
	private java.lang.String item;
	/**上会材料主表id*/
	@Excel(name = "上会材料主表id", width = 15)
    @ApiModelProperty(value = "上会材料主表id")
	private java.lang.String mid;
	/**违规人次*/
	@Excel(name = "违规人次", width = 15)
    @ApiModelProperty(value = "违规人次")
	private java.lang.Integer pax;
	/**罚款金额*/
	@Excel(name = "罚款金额", width = 15)
    @ApiModelProperty(value = "罚款金额")
	private java.math.BigDecimal penaltyAmt;
	/**罚款倍数*/
	@Excel(name = "罚款倍数", width = 15)
    @ApiModelProperty(value = "罚款倍数")
	private java.lang.Integer penaltyN;
	/**序号*/
	@Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "序号")
	private java.lang.Integer seq;
}
