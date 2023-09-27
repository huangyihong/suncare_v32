package com.ai.modules.ybChargeSearch.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class YbMeetingMaterialsVo {
	/**主键*/
	@ApiModelProperty(value = "主键")
	private String id;
	/**明细表id*/
	@ApiModelProperty(value = "明细表id")
	private String detailId;
	/**医院名称*/
	@Excel(name = "医院名称", width = 15)
	@ApiModelProperty(value = "医院名称")
	private String orgname;
	/**行动名称*/
	@Excel(name = "行动名称", width = 15)
    @ApiModelProperty(value = "行动名称")
	private String actionname;
	/**文书字号*/
	@Excel(name = "文书字号", width = 15)
    @ApiModelProperty(value = "文书字号")
	private String codename;
	/**检查小组*/
	@Excel(name = "检查小组", width = 15)
	@ApiModelProperty(value = "检查小组")
	private String groupTeam;
	/**调查开始时间*/
	@Excel(name = "调查开始时间", width = 15)
	@ApiModelProperty(value = "调查开始时间")
	private String startdate;
	/**检查时间范围*/
	@Excel(name = "检查时间范围", width = 15)
	@ApiModelProperty(value = "检查时间范围")
	private String timerange;
	/**序号*/
	@Excel(name = "序号", width = 15)
	@ApiModelProperty(value = "序号")
	private java.lang.Integer seq;
	/**问题类别(一级指标)*/
	@Excel(name = "问题类别\n" +
			"（一级指标）", width = 15)
	@ApiModelProperty(value = "问题类别(一级指标)")
	private java.lang.String cat;
	/**违规项目*/
	@Excel(name = "违规项目", width = 15)
	@ApiModelProperty(value = "违规项目")
	private java.lang.String item;
	/**违规情形描述*/
	@Excel(name = "违规情形描述", width = 15)
	@ApiModelProperty(value = "违规情形描述")
	private java.lang.String wgDesc;
	/**违规人次*/
	@Excel(name = "违规人次", width = 15)
	@ApiModelProperty(value = "违规人次")
	private java.lang.Integer pax;
	/**违规医保基金金额（元）*/
	@Excel(name = "违规医保基金金额（元）", width = 15)
	@ApiModelProperty(value = "违规医保基金金额（元）")
	private java.math.BigDecimal fundAmt;
	/**罚款倍数*/
	@Excel(name = "罚款倍数", width = 15)
	@ApiModelProperty(value = "罚款倍数")
	private java.lang.Integer penaltyN;
	/**罚款金额*/
	@Excel(name = "罚款金额", width = 15)
	@ApiModelProperty(value = "罚款金额")
	private java.math.BigDecimal penaltyAmt;
	/**违规第几条*/
	@Excel(name = "违规第几条", width = 15)
	@ApiModelProperty(value = "违规第几条")
	private java.lang.String clauseT;
	/**违规第几款*/
	@Excel(name = "违规第几款", width = 15)
	@ApiModelProperty(value = "违规第几款")
	private java.lang.String clauseK;
	/**创建人名称*/
    @ApiModelProperty(value = "创建人名称")
	private String createdByName;
	/**创建时间*/
    @ApiModelProperty(value = "创建时间")
	private String createdTime;
	/**行政处罚事先告知书下载地址*/
    @ApiModelProperty(value = "行政处罚事先告知书下载地址")
	private String xzcftzsUrl;
	/**拟处理意见报告下载地址*/
	@ApiModelProperty(value = "拟处理意见报告下载地址")
	private String nclyjbgUrl;


}
