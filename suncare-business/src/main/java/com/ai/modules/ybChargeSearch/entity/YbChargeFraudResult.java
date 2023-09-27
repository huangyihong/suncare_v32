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
 * @Description: 欺诈专题结果表
 * @Author: jeecg-boot
 * @Date:   2023-05-22
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_fraud_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_fraud_result对象", description="欺诈专题结果表")
public class YbChargeFraudResult {

	/**患者名称*/
	@Excel(name = "患者名称", width = 15)
    @ApiModelProperty(value = "患者名称")
	private java.lang.String clientname;
	/**联系电话*/
	@Excel(name = "联系电话", width = 15)
    @ApiModelProperty(value = "联系电话")
	private java.lang.String contactorphone;
	/**日数量最大手术项目数量*/
	@Excel(name = "日数量最大手术项目数量", width = 15)
    @ApiModelProperty(value = "日数量最大手术项目数量")
	private java.lang.Integer dayMaxCntSurgeryCnt;
	/**日数量最大手术项目金额*/
	@Excel(name = "日数量最大手术项目金额", width = 15)
    @ApiModelProperty(value = "日数量最大手术项目金额")
	private java.math.BigDecimal dayMaxCntSurgeryFee;
	/**日数量最大手术项目名称*/
	@Excel(name = "日数量最大手术项目名称", width = 15)
    @ApiModelProperty(value = "日数量最大手术项目名称")
	private java.lang.String dayMaxCntSurgeryName;
	/**日金额最大药品数量*/
	@Excel(name = "日金额最大药品数量", width = 15)
    @ApiModelProperty(value = "日金额最大药品数量")
	private java.math.BigDecimal dayMaxDrugCnt;
	/**日金额最大药品金额*/
	@Excel(name = "日金额最大药品金额", width = 15)
    @ApiModelProperty(value = "日金额最大药品金额")
	private java.math.BigDecimal dayMaxDrugFee;
	/**日金额最大药品名称*/
	@Excel(name = "日金额最大药品名称", width = 15)
    @ApiModelProperty(value = "日金额最大药品名称")
	private java.lang.String dayMaxDrugName;
	/**日金额最大手术项目数量*/
	@Excel(name = "日金额最大手术项目数量", width = 15)
    @ApiModelProperty(value = "日金额最大手术项目数量")
	private java.lang.Integer dayMaxFeeSurgeryCnt;
	/**日金额最大手术项目金额*/
	@Excel(name = "日金额最大手术项目金额", width = 15)
    @ApiModelProperty(value = "日金额最大手术项目金额")
	private java.math.BigDecimal dayMaxFeeSurgeryFee;
	/**日金额最大手术项目名称*/
	@Excel(name = "日金额最大手术项目名称", width = 15)
    @ApiModelProperty(value = "日金额最大手术项目名称")
	private java.lang.String dayMaxFeeSurgeryName;
	/**日金额最大诊疗项目数量*/
	@Excel(name = "日金额最大诊疗项目数量", width = 15)
    @ApiModelProperty(value = "日金额最大诊疗项目数量")
	private java.math.BigDecimal dayMaxTreatCnt;
	/**日金额最大诊疗项目金额*/
	@Excel(name = "日金额最大诊疗项目金额", width = 15)
    @ApiModelProperty(value = "日金额最大诊疗项目金额")
	private java.math.BigDecimal dayMaxTreatFee;
	/**日金额最大诊疗项目名称*/
	@Excel(name = "日金额最大诊疗项目名称", width = 15)
    @ApiModelProperty(value = "日金额最大诊疗项目名称")
	private java.lang.String dayMaxTreatName;
	/**医生数量*/
	@Excel(name = "医生数量", width = 15)
    @ApiModelProperty(value = "医生数量")
	private java.lang.Integer doctorCnt;
	/**二级公立医疗机构数量*/
	@Excel(name = "二级公立医疗机构数量", width = 15)
    @ApiModelProperty(value = "二级公立医疗机构数量")
	private java.math.BigDecimal erGongSl;
	/**二级民营医疗机构数量*/
	@Excel(name = "二级民营医疗机构数量", width = 15)
    @ApiModelProperty(value = "二级民营医疗机构数量")
	private java.math.BigDecimal erMinSl;
	/**年基金金额*/
	@Excel(name = "年基金金额", width = 15)
    @ApiModelProperty(value = "年基金金额")
	private java.math.BigDecimal fundpay;
	/**门诊口服药金额*/
	@Excel(name = "门诊口服药金额", width = 15)
    @ApiModelProperty(value = "门诊口服药金额")
	private java.math.BigDecimal fy;
	/**医院等级*/
	@Excel(name = "医院等级", width = 15)
    @ApiModelProperty(value = "医院等级")
	private java.lang.String hosplevel;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**身份证号*/
	@Excel(name = "身份证号", width = 15)
    @ApiModelProperty(value = "身份证号")
	private java.lang.String idNo;
	/**是否结伴门诊*/
	@Excel(name = "是否结伴门诊", width = 15)
    @ApiModelProperty(value = "是否结伴门诊")
	private java.lang.String ifJbmz;
	/**是否结伴住院*/
	@Excel(name = "是否结伴住院", width = 15)
    @ApiModelProperty(value = "是否结伴住院")
	private java.lang.String ifJbzy;
	/**是否连续三天门诊购药/药品名称*/
	@Excel(name = "是否连续三天门诊购药/药品名称", width = 15)
    @ApiModelProperty(value = "是否连续三天门诊购药/药品名称")
	private java.lang.String ifLxstmzgy;
	/**参保类型*/
	@Excel(name = "参保类型", width = 15)
    @ApiModelProperty(value = "参保类型")
	private java.lang.String insurancetype;
	/**居民基金金额*/
	@Excel(name = "居民基金金额", width = 15)
    @ApiModelProperty(value = "居民基金金额")
	private java.math.BigDecimal jmFundpay;
	/**本地/异地*/
	@Excel(name = "本地/异地", width = 15)
    @ApiModelProperty(value = "本地/异地")
	private java.lang.String localTag;
	/**年数量最大手术项目数量*/
	@Excel(name = "年数量最大手术项目数量", width = 15)
    @ApiModelProperty(value = "年数量最大手术项目数量")
	private java.lang.Integer maxCntSurgeryCnt;
	/**年数量最大手术项目金额*/
	@Excel(name = "年数量最大手术项目金额", width = 15)
    @ApiModelProperty(value = "年数量最大手术项目金额")
	private java.math.BigDecimal maxCntSurgeryFee;
	/**年数量最大手术项目名称*/
	@Excel(name = "年数量最大手术项目名称", width = 15)
    @ApiModelProperty(value = "年数量最大手术项目名称")
	private java.lang.String maxCntSurgeryName;
	/**年数量最大手术项目金额区域占比*/
	@Excel(name = "年数量最大手术项目金额区域占比", width = 15)
    @ApiModelProperty(value = "年数量最大手术项目金额区域占比")
	private java.math.BigDecimal maxCntSurgeryZb;
	/**住院数量最多疾病数量*/
	@Excel(name = "住院数量最多疾病数量", width = 15)
    @ApiModelProperty(value = "住院数量最多疾病数量")
	private java.lang.Integer maxDiagCnt;
	/**住院数量最多疾病名称*/
	@Excel(name = "住院数量最多疾病名称", width = 15)
    @ApiModelProperty(value = "住院数量最多疾病名称")
	private java.lang.String maxDiagName;
	/**住院数量最多疾病平均住院金额区域占比*/
	@Excel(name = "住院数量最多疾病平均住院金额区域占比", width = 15)
    @ApiModelProperty(value = "住院数量最多疾病平均住院金额区域占比")
	private java.math.BigDecimal maxDiagZb;
	/**年金额最大药品数量*/
	@Excel(name = "年金额最大药品数量", width = 15)
    @ApiModelProperty(value = "年金额最大药品数量")
	private java.math.BigDecimal maxDrugCnt;
	/**年金额最大药品金额*/
	@Excel(name = "年金额最大药品金额", width = 15)
    @ApiModelProperty(value = "年金额最大药品金额")
	private java.math.BigDecimal maxDrugFee;
	/**年金额最大药品名称*/
	@Excel(name = "年金额最大药品名称", width = 15)
    @ApiModelProperty(value = "年金额最大药品名称")
	private java.lang.String maxDrugName;
	/**年金额最大手术项目数量*/
	@Excel(name = "年金额最大手术项目数量", width = 15)
    @ApiModelProperty(value = "年金额最大手术项目数量")
	private java.lang.Integer maxFeeSurgeryCnt;
	/**年金额最大手术项目金额*/
	@Excel(name = "年金额最大手术项目金额", width = 15)
    @ApiModelProperty(value = "年金额最大手术项目金额")
	private java.math.BigDecimal maxFeeSurgeryFee;
	/**年金额最大手术项目名称*/
	@Excel(name = "年金额最大手术项目名称", width = 15)
    @ApiModelProperty(value = "年金额最大手术项目名称")
	private java.lang.String maxFeeSurgeryName;
	/**年金额最大手术项目金额区域占比*/
	@Excel(name = "年金额最大手术项目金额区域占比", width = 15)
    @ApiModelProperty(value = "年金额最大手术项目金额区域占比")
	private java.math.BigDecimal maxFeeSurgeryZb;
	/**年金额最大诊疗项目数量*/
	@Excel(name = "年金额最大诊疗项目数量", width = 15)
    @ApiModelProperty(value = "年金额最大诊疗项目数量")
	private java.math.BigDecimal maxTreatCnt;
	/**年金额最大诊疗项目金额*/
	@Excel(name = "年金额最大诊疗项目金额", width = 15)
    @ApiModelProperty(value = "年金额最大诊疗项目金额")
	private java.math.BigDecimal maxTreatFee;
	/**年金额最大诊疗项目名称*/
	@Excel(name = "年金额最大诊疗项目名称", width = 15)
    @ApiModelProperty(value = "年金额最大诊疗项目名称")
	private java.lang.String maxTreatName;
	/**理论最小床位数（总床位数/365天）*/
	@Excel(name = "理论最小床位数（总床位数/365天）", width = 15)
    @ApiModelProperty(value = "理论最小床位数（总床位数/365天）")
	private java.math.BigDecimal minBedCnt;
	/**门诊次均费用*/
	@Excel(name = "门诊次均费用", width = 15)
    @ApiModelProperty(value = "门诊次均费用")
	private java.math.BigDecimal mzAvgFee;
	/**单个患者门诊年平均数量*/
	@Excel(name = "单个患者门诊年平均数量", width = 15)
    @ApiModelProperty(value = "单个患者门诊年平均数量")
	private java.math.BigDecimal mzAvgTimes;
	/**年门诊次数*/
	@Excel(name = "年门诊次数", width = 15)
    @ApiModelProperty(value = "年门诊次数")
	private java.math.BigDecimal mzCnt;
	/**日最大门诊量人次*/
	@Excel(name = "日最大门诊量人次", width = 15)
    @ApiModelProperty(value = "日最大门诊量人次")
	private java.lang.Integer mzDayMaxCnt;
	/**日最大门诊量日期*/
	@Excel(name = "日最大门诊量日期", width = 15)
    @ApiModelProperty(value = "日最大门诊量日期")
	private java.lang.String mzDayMaxDate;
	/**全部门诊诊断*/
	@Excel(name = "全部门诊诊断", width = 15)
    @ApiModelProperty(value = "全部门诊诊断")
	private java.lang.String mzDiag;
	/**年门诊基金金额*/
	@Excel(name = "年门诊基金金额", width = 15)
    @ApiModelProperty(value = "年门诊基金金额")
	private java.math.BigDecimal mzFundpay;
	/**门诊口服药种类数量*/
	@Excel(name = "门诊口服药种类数量", width = 15)
    @ApiModelProperty(value = "门诊口服药种类数量")
	private java.math.BigDecimal mzKfCnt;
	/**门诊口服药金额*/
	@Excel(name = "门诊口服药金额", width = 15)
    @ApiModelProperty(value = "门诊口服药金额")
	private java.math.BigDecimal mzKfFee;
	/**全部门诊口服药名称*/
	@Excel(name = "全部门诊口服药名称", width = 15)
    @ApiModelProperty(value = "全部门诊口服药名称")
	private java.lang.String mzKfItemname;
	/**全部门诊机构*/
	@Excel(name = "全部门诊机构", width = 15)
    @ApiModelProperty(value = "全部门诊机构")
	private java.lang.String mzOrgname;
	/**年门诊金额*/
	@Excel(name = "年门诊金额", width = 15)
    @ApiModelProperty(value = "年门诊金额")
	private java.math.BigDecimal mzTotalfee;
	/**门诊/住院占比(不排除自费)*/
	@Excel(name = "门诊/住院占比(不排除自费)", width = 15)
    @ApiModelProperty(value = "门诊/住院占比(不排除自费)")
	private java.math.BigDecimal mzZyRatio;
	/**患者名称+出生日期*/
	@Excel(name = "患者名称+出生日期", width = 15)
    @ApiModelProperty(value = "患者名称+出生日期")
	private java.lang.String name;
	/**年份*/
	@Excel(name = "年份", width = 15)
    @ApiModelProperty(value = "年份")
	private java.lang.String nian;
	/**定点医疗机构数量*/
	@Excel(name = "定点医疗机构数量", width = 15)
    @ApiModelProperty(value = "定点医疗机构数量")
	private java.math.BigDecimal orgSl;
	/**类型(药店/医院)*/
	@Excel(name = "类型(药店/医院)", width = 15)
    @ApiModelProperty(value = "类型(药店/医院)")
	private java.lang.String orgcategory;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**专科类型*/
	@Excel(name = "专科类型", width = 15)
    @ApiModelProperty(value = "专科类型")
	private java.lang.String orgtype;
	/**性质*/
	@Excel(name = "性质", width = 15)
    @ApiModelProperty(value = "性质")
	private java.lang.String owntype;
	/**就诊人次*/
	@Excel(name = "就诊人次", width = 15)
    @ApiModelProperty(value = "就诊人次")
	private java.math.BigDecimal renshu;
	/**三级公立医疗机构数量*/
	@Excel(name = "三级公立医疗机构数量", width = 15)
    @ApiModelProperty(value = "三级公立医疗机构数量")
	private java.math.BigDecimal sanGongSl;
	/**三级民营医疗机构数量*/
	@Excel(name = "三级民营医疗机构数量", width = 15)
    @ApiModelProperty(value = "三级民营医疗机构数量")
	private java.math.BigDecimal sanMinSl;
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private java.lang.String sex;
	/**年手术量*/
	@Excel(name = "年手术量", width = 15)
    @ApiModelProperty(value = "年手术量")
	private java.math.BigDecimal surgeryCn;
	/**低标准入院数量*/
	@Excel(name = "低标准入院数量", width = 15)
    @ApiModelProperty(value = "低标准入院数量")
	private java.lang.Integer tagDbzryCnt;
	/**分解住院数量*/
	@Excel(name = "分解住院数量", width = 15)
    @ApiModelProperty(value = "分解住院数量")
	private java.lang.Integer tagFjzyCnt;
	/**结伴门诊数量*/
	@Excel(name = "结伴门诊数量", width = 15)
    @ApiModelProperty(value = "结伴门诊数量")
	private java.lang.Integer tagJbmzCnt;
	/**结伴住院数量*/
	@Excel(name = "结伴住院数量", width = 15)
    @ApiModelProperty(value = "结伴住院数量")
	private java.lang.Integer tagJbzyCnt;
	/**节假日住院异常程度*/
	@Excel(name = "节假日住院异常程度", width = 15)
    @ApiModelProperty(value = "节假日住院异常程度")
	private java.lang.Integer tagJjrycCnt;
	/**门诊就诊雷同数量*/
	@Excel(name = "门诊就诊雷同数量", width = 15)
    @ApiModelProperty(value = "门诊就诊雷同数量")
	private java.lang.Integer tagMzltCnt;
	/**住院就诊雷同数量*/
	@Excel(name = "住院就诊雷同数量", width = 15)
    @ApiModelProperty(value = "住院就诊雷同数量")
	private java.lang.Integer tagZyltCnt;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**年医疗费用*/
	@Excel(name = "年医疗费用", width = 15)
    @ApiModelProperty(value = "年医疗费用")
	private java.math.BigDecimal totalfee;
	/**未评级公立医疗机构数量*/
	@Excel(name = "未评级公立医疗机构数量", width = 15)
    @ApiModelProperty(value = "未评级公立医疗机构数量")
	private java.math.BigDecimal weiGongSl;
	/**未评级民营医疗机构数量*/
	@Excel(name = "未评级民营医疗机构数量", width = 15)
    @ApiModelProperty(value = "未评级民营医疗机构数量")
	private java.math.BigDecimal weiMinSl;
	/**地址*/
	@Excel(name = "地址", width = 15)
    @ApiModelProperty(value = "地址")
	private java.lang.String workplacename;

	/**异地基金金额*/
	@Excel(name = "异地基金金额", width = 15)
    @ApiModelProperty(value = "异地基金金额")
	private java.math.BigDecimal ydFundpay;
	/**药店数量*/
	@Excel(name = "药店数量", width = 15)
    @ApiModelProperty(value = "药店数量")
	private java.math.BigDecimal ydSl;
	/**年*/
	@Excel(name = "年", width = 15)
    @ApiModelProperty(value = "年")
	private java.lang.String year;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
	private java.lang.String yearage;
	/**一级公立医疗机构数量*/
	@Excel(name = "一级公立医疗机构数量", width = 15)
    @ApiModelProperty(value = "一级公立医疗机构数量")
	private java.math.BigDecimal yiGongSl;
	/**一级民营医疗机构数量*/
	@Excel(name = "一级民营医疗机构数量", width = 15)
    @ApiModelProperty(value = "一级民营医疗机构数量")
	private java.math.BigDecimal yiMinSl;
	/**年*/
	@Excel(name = "年", width = 15)
    @ApiModelProperty(value = "年")
	private java.lang.String yyear;
	/**职工基金金额*/
	@Excel(name = "职工基金金额", width = 15)
    @ApiModelProperty(value = "职工基金金额")
	private java.math.BigDecimal zgFundpay;
	/**平均住院日*/
	@Excel(name = "平均住院日", width = 15)
    @ApiModelProperty(value = "平均住院日")
	private java.math.BigDecimal zyAvgDays;
	/**住院均次金额*/
	@Excel(name = "住院均次金额", width = 15)
    @ApiModelProperty(value = "住院均次金额")
	private java.math.BigDecimal zyAvgFee;
	/**年住院次数*/
	@Excel(name = "年住院次数", width = 15)
    @ApiModelProperty(value = "年住院次数")
	private java.math.BigDecimal zyCnt;
	/**日在院平均人数*/
	@Excel(name = "日在院平均人数", width = 15)
    @ApiModelProperty(value = "日在院平均人数")
	private java.lang.Integer zyDayAvgInClient;
	/**日最大住院量人次*/
	@Excel(name = "日最大住院量人次", width = 15)
    @ApiModelProperty(value = "日最大住院量人次")
	private java.lang.Integer zyDayMaxCnt;
	/**日最大住院量日期*/
	@Excel(name = "日最大住院量日期", width = 15)
    @ApiModelProperty(value = "日最大住院量日期")
	private java.lang.String zyDayMaxDate;
	/**日最大在院量人次*/
	@Excel(name = "日最大在院量人次", width = 15)
    @ApiModelProperty(value = "日最大在院量人次")
	private java.lang.Integer zyDayMaxInCnt;
	/**日最大在院量日期*/
	@Excel(name = "日最大在院量日期", width = 15)
    @ApiModelProperty(value = "日最大在院量日期")
	private java.lang.String zyDayMaxInDate;
	/**全部诊断*/
	@Excel(name = "全部诊断", width = 15)
    @ApiModelProperty(value = "全部诊断")
	private java.lang.String zyDiag;
	/**年住院基金金额*/
	@Excel(name = "年住院基金金额", width = 15)
    @ApiModelProperty(value = "年住院基金金额")
	private java.math.BigDecimal zyFundpay;
	/**全部手术*/
	@Excel(name = "全部手术", width = 15)
    @ApiModelProperty(value = "全部手术")
	private java.lang.String zySurgery;
	/**年住院金额*/
	@Excel(name = "年住院金额", width = 15)
    @ApiModelProperty(value = "年住院金额")
	private java.math.BigDecimal zyTotalfee;


	/**年民营机构基金金额占比*/
	@Excel(name = "年民营机构基金金额占比", width = 15)
	@ApiModelProperty(value = "年民营机构基金金额占比")
	private java.math.BigDecimal myFundpayZb;

	/**年未知民营公立机构基金金额占比*/
	@Excel(name = "年未知民营公立机构基金金额占比", width = 15)
	@ApiModelProperty(value = "年未知民营公立机构基金金额占比")
	private java.math.BigDecimal wzFundpayZb;

	/**三级未知民营公立医疗机构数量*/
	@Excel(name = "三级未知民营公立医疗机构数量", width = 15)
	@ApiModelProperty(value = "三级未知民营公立医疗机构数量")
	private java.math.BigDecimal sanWzSl;

	/**二级未知民营公立医疗机构数量*/
	@Excel(name = "二级未知民营公立医疗机构数量", width = 15)
	@ApiModelProperty(value = "二级未知民营公立医疗机构数量")
	private java.math.BigDecimal erWzSl;

	/**一级未知民营公立医疗机构数量*/
	@Excel(name = "一级未知民营公立医疗机构数量", width = 15)
	@ApiModelProperty(value = "一级未知民营公立医疗机构数量")
	private java.math.BigDecimal yiWzSl;

	/**未评级未知民营公立医疗机构数量*/
	@Excel(name = "未评级未知民营公立医疗机构数量", width = 15)
	@ApiModelProperty(value = "未评级未知民营公立医疗机构数量")
	private java.math.BigDecimal weiWzSl;


	/**门诊口服药金额*/
	@Excel(name = "门诊口服药金额", width = 15)
	@ApiModelProperty(value = "门诊口服药金额")
	private java.math.BigDecimal mzOralFy;

	/**预警标签列表*/
	@Excel(name = "预警标签列表", width = 15)
	@ApiModelProperty(value = "预警标签列表")
	private java.lang.String tagList;



}
