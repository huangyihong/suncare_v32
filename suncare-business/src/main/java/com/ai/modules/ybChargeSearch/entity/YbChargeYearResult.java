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
 * @Description: 年度统计指标结果表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_year_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_year_result对象", description="年度统计指标结果表")
public class YbChargeYearResult {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**门诊人次*/
	@Excel(name = "门诊人次", width = 15)
    @ApiModelProperty(value = "门诊人次")
	private java.lang.Integer mzCount;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**原始机构名称*/
	@Excel(name = "原始机构名称", width = 15)
    @ApiModelProperty(value = "原始机构名称")
	private java.lang.String orgnameSrc;
	/**基金使用总额*/
	@Excel(name = "基金使用总额", width = 15)
    @ApiModelProperty(value = "基金使用总额")
	private java.math.BigDecimal sumFundpay;
	/**医疗费用总额*/
	@Excel(name = "医疗费用总额", width = 15)
    @ApiModelProperty(value = "医疗费用总额")
	private java.math.BigDecimal sumTotalfee;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**就诊人次*/
	@Excel(name = "就诊人次", width = 15)
    @ApiModelProperty(value = "就诊人次")
	private java.lang.Integer totalCount;
	/**年*/
	@Excel(name = "年", width = 15)
    @ApiModelProperty(value = "年")
	private java.lang.String year;
	/**住院人次*/
	@Excel(name = "住院人次", width = 15)
    @ApiModelProperty(value = "住院人次")
	private java.lang.Integer zyCount;

	/**购药人次*/
	@Excel(name = "购药人次", width = 15)
	@ApiModelProperty(value = "购药人次")
	private java.lang.Integer gyCount;

	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgid;


	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
	@ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;


	/**收费项目大类*/
	@Excel(name = "收费项目大类", width = 15)
	@ApiModelProperty(value = "收费项目大类")
	private java.lang.String itemtype;


	/**收费项目类型*/
	@Excel(name = "收费项目类型", width = 15)
	@ApiModelProperty(value = "收费项目类型")
	private java.lang.String chargeclass;

	/**收费项目名称*/
	@Excel(name = "收费项目名称", width = 15)
	@ApiModelProperty(value = "收费项目名称")
	private java.lang.String itemname;

	/**医保项目使用率*/
	@Excel(name = "医保项目使用率", width = 15)
	@ApiModelProperty(value = "医保项目使用率")
	private java.math.BigDecimal userate;

	/**医疗费用总额(医保项目汇总)*/
	@Excel(name = "医疗费用总额", width = 15)
	@ApiModelProperty(value = "医疗费用总额")
	private java.math.BigDecimal fee;

	/**次均住院金额*/
	@Excel(name = "次均住院金额", width = 15)
	@ApiModelProperty(value = "次均住院金额")
	private java.math.BigDecimal avgZyFee;

	/**次均住院天数*/
	@Excel(name = "次均住院天数", width = 15)
	@ApiModelProperty(value = "次均住院天数")
	private java.math.BigDecimal avgZyDay;

	/**平均床日费用*/
	@Excel(name = "平均床日费用", width = 15)
	@ApiModelProperty(value = "平均床日费用")
	private java.math.BigDecimal avgBedFee;

	/**门诊入院率(%)*/
	@Excel(name = "门诊入院率(%)", width = 15)
	@ApiModelProperty(value = "门诊入院率(%)")
	private java.math.BigDecimal zyMzRate;

	/**床位利用率(%)*/
	@Excel(name = "床位利用率(%)", width = 15)
	@ApiModelProperty(value = "床位利用率(%)")
	private java.lang.String bedAmount;

	/**本地异地*/
	@Excel(name = "本地异地", width = 15)
	@ApiModelProperty(value = "本地异地")
	private java.lang.String localTag;

	/**手术名称*/
	@Excel(name = "手术名称", width = 15)
	@ApiModelProperty(value = "手术名称")
	private java.lang.String surgeryName;

	/**医保编码*/
	@Excel(name = "医保编码", width = 15)
	@ApiModelProperty(value = "医保编码")
	private java.lang.String itemcode;

	/**医疗费用总额*/
	@Excel(name = "全年总次数", width = 15)
	@ApiModelProperty(value = "全年总次数")
	private java.math.BigDecimal cntSum;

	/**日均次数*/
	@Excel(name = "日均次数", width = 15)
	@ApiModelProperty(value = "日均次数")
	private java.math.BigDecimal avgDayCnt;

	/**最大日次数*/
	@Excel(name = "最大日次数", width = 15)
	@ApiModelProperty(value = "最大日次数")
	private java.math.BigDecimal maxDayCnt;

	/**最大日日期*/
	@Excel(name = "最大日日期", width = 15)
	@ApiModelProperty(value = "最大日日期")
	private java.lang.String maxDayDate;

	/**最高单价*/
	@Excel(name = "最高单价", width = 15)
	@ApiModelProperty(value = "最高单价")
	private java.math.BigDecimal maxPrice;

	/**最低单价*/
	@Excel(name = "最低单价", width = 15)
	@ApiModelProperty(value = "最低单价")
	private java.math.BigDecimal minPrice;


	/**统计日期日期*/
	@ApiModelProperty(value = "统计日期日期")
	private java.util.Date ddate;
	/**长假标志*/
	@Excel(name = "长假标志", width = 15)
	@ApiModelProperty(value = "长假标志")
	private java.lang.String longHolidayTag;
	/**入院人次*/
	@Excel(name = "入院人次", width = 15)
	@ApiModelProperty(value = "入院人次")
	private java.math.BigDecimal admitCnt;
	/**出院人次*/
	@Excel(name = "出院人次", width = 15)
	@ApiModelProperty(value = "出院人次")
	private java.math.BigDecimal leaveCnt;
	/**当日在院总人数*/
	@Excel(name = "当日在院总人数", width = 15)
	@ApiModelProperty(value = "当日在院总人数")
	private java.math.BigDecimal inhospitalCnt;
}
