package com.ai.modules.ybChargeSearch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 收费项目异常结果表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_overproof_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_overproof_result对象", description="收费项目异常结果表")
public class YbChargeOverproofResult {

	/**异常程度对应金额*/
	@Excel(name = "异常程度对应金额", width = 15)
    @ApiModelProperty(value = "异常程度对应金额")
	private java.math.BigDecimal abnormalMoney;
	/**异常程度*/
	@Excel(name = "异常程度", width = 15)
	@ApiModelProperty(value = "异常程度")
	private java.math.BigDecimal abnormalDistince;
	/**异常阈值*/
	@Excel(name = "异常阈值", width = 15)
    @ApiModelProperty(value = "异常阈值")
	private java.math.BigDecimal abnormalStandard;
	/**人均费用*/
	@Excel(name = "人均费用", width = 15)
    @ApiModelProperty(value = "人均费用")
	private java.math.BigDecimal analyseValue;
	/**对比机构平均值*/
	@Excel(name = "对比机构平均值", width = 15)
    @ApiModelProperty(value = "对比机构平均值")
	private java.math.BigDecimal averageValue;
	/**收费类型*/
	@Excel(name = "收费类型", width = 15)
    @ApiModelProperty(value = "收费类型")
	private java.lang.String chargeClass;
	/**分析对象*/
	@Excel(name = "分析对象", width = 15)
    @ApiModelProperty(value = "分析对象")
	private java.lang.String compareObject;
	/**对比机构数量*/
	@Excel(name = "对比机构数量", width = 15)
    @ApiModelProperty(value = "对比机构数量")
	private java.math.BigDecimal compareOrgAmount;
	/**对比机构参考值*/
	@Excel(name = "对比机构参考值", width = 15)
    @ApiModelProperty(value = "对比机构参考值")
	private java.math.BigDecimal compareValue;
	/**科室名称*/
	@Excel(name = "科室名称", width = 15)
    @ApiModelProperty(value = "科室名称")
	private java.lang.String deptname;
	/**科室名称src*/
	@Excel(name = "科室名称src", width = 15)
    @ApiModelProperty(value = "科室名称src")
	private java.lang.String deptnameSrc;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	private java.lang.String etlSource;
	/**费用发生时间*/
	@Excel(name = "费用发生时间", width = 15)
    @ApiModelProperty(value = "费用发生时间")
	private java.lang.String feeOccurTime;
	/**医院收费名称*/
	@Excel(name = "医院收费名称", width = 15)
    @ApiModelProperty(value = "医院收费名称")
	private java.lang.String hisItemnameSrc;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**医保收费名称*/
	@Excel(name = "医保收费名称", width = 15)
    @ApiModelProperty(value = "医保收费名称")
	private java.lang.String itemnameSrc;
	/**单价*/
	@Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
	private java.math.BigDecimal itemprice;
	/**对比机构参考值*/
	@Excel(name = "对比机构参考值", width = 15)
    @ApiModelProperty(value = "对比机构参考值")
	private java.math.BigDecimal medianValue;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**映射后主诊断*/
	@Excel(name = "映射后主诊断", width = 15)
    @ApiModelProperty(value = "映射后主诊断")
	private java.lang.Object primarydiagName;
	/**主诊断*/
	@Excel(name = "主诊断", width = 15)
    @ApiModelProperty(value = "主诊断")
	private java.lang.Object primarydiagNameSrc;
	/**参考值在机构中的比例*/
	@Excel(name = "参考值在机构中的比例", width = 15)
    @ApiModelProperty(value = "参考值在机构中的比例")
	private java.math.BigDecimal sampleProp;
	/**算法名称*/
	@Excel(name = "算法名称", width = 15)
    @ApiModelProperty(value = "算法名称")
	private java.lang.String tagName;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
	private java.math.BigDecimal totalAmount;
	/**金额*/
	@Excel(name = "金额", width = 15)
    @ApiModelProperty(value = "金额")
	private java.math.BigDecimal totalFee;
	/**就诊号*/
	@Excel(name = "就诊号", width = 15)
    @ApiModelProperty(value = "就诊号")
	private java.lang.String visitidSrc;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;
	/**收费年份*/
	@Excel(name = "收费年份", width = 15)
    @ApiModelProperty(value = "收费年份")
	private java.lang.String year;

	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgid;

	/**级别*/
	@Excel(name = "级别", width = 15)
	@ApiModelProperty(value = "级别")
	private java.lang.String hosplevel;
	/**医院类别*/
	@Excel(name = "医院类别", width = 15)
	@ApiModelProperty(value = "医院类别")
	private java.lang.String orgcategory;
	/**医院大类*/
	@Excel(name = "医院大类", width = 15)
	@ApiModelProperty(value = "医院大类")
	private java.lang.String orgcategory2;
	/**住院人次*/
	@Excel(name = "住院人次", width = 15)
	@ApiModelProperty(value = "住院人次")
	private java.lang.Integer zyCount;
	/**门诊人次*/
	@Excel(name = "门诊人次", width = 15)
	@ApiModelProperty(value = "门诊人次")
	private java.lang.Integer mzCount;
	/**住院总费用*/
	@Excel(name = "住院总费用", width = 15)
	@ApiModelProperty(value = "住院总费用")
	private java.math.BigDecimal zyTotalfeeSum;
	/**住院报销总额*/
	@Excel(name = "住院报销总额", width = 15)
	@ApiModelProperty(value = "住院报销总额")
	private java.math.BigDecimal zyFundpaySum;
	/**门诊总费用*/
	@Excel(name = "门诊总费用", width = 15)
	@ApiModelProperty(value = "门诊总费用")
	private java.math.BigDecimal mzTotalfeeSum;
	/**门诊报销总额*/
	@Excel(name = "门诊报销总额", width = 15)
	@ApiModelProperty(value = "门诊报销总额")
	private java.math.BigDecimal mzFundpaySum;

	/**原主键id*/
	@Excel(name = "原主键id", width = 15)
	@ApiModelProperty(value = "原主键id")
	private java.lang.String srcId;

	/**标注内容*/
	@Excel(name = "标注内容", width = 15)
	@ApiModelProperty(value = "标注内容")
	private java.lang.String labelName;

	/**标注人*/
	@Excel(name = "标注人", width = 15)
	@ApiModelProperty(value = "标注人")
	private java.lang.String labelUser;

	/**标注时间*/
	@Excel(name = "标注时间", width = 15)
	@ApiModelProperty(value = "标注时间")
	private java.lang.String labelTime;

	/**单位地址*/
	@Excel(name = "单位地址", width = 15)
	@ApiModelProperty(value = "单位地址")
	private java.lang.String  workplacename;
}
