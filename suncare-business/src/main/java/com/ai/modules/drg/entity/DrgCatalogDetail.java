package com.ai.modules.drg.entity;

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
 * @Description: DRG分组目录数据详细表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
@Data
@TableName("drg_catalog_detail")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="drg_catalog_detail对象", description="DRG分组目录数据详细表")
public class DrgCatalogDetail {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**编码*/
	@Excel(name = "编码", width = 15)
    @ApiModelProperty(value = "编码")
	private java.lang.String code;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
	private java.lang.String name;
	/**所属版本id*/
	@Excel(name = "所属版本id", width = 15)
    @ApiModelProperty(value = "所属版本id")
	private java.lang.String catalogId;
	/**MDC目录code*/
	@Excel(name = "MDC目录code", width = 15)
    @ApiModelProperty(value = "MDC目录code")
	private java.lang.String mdcCatalogCode;
	/**ADRG目录code*/
	@Excel(name = "ADRG目录code", width = 15)
    @ApiModelProperty(value = "ADRG目录code")
	private java.lang.String adrgCatalogCode;
	/**ICD10版本*/
	@Excel(name = "ICD10版本", width = 15)
    @ApiModelProperty(value = "ICD10版本")
	private java.lang.String icd10V;
	/**ICD9版本*/
	@Excel(name = "ICD9版本", width = 15)
    @ApiModelProperty(value = "ICD9版本")
	private java.lang.String icd9V;
	/**目录类型*/
	@Excel(name = "目录类型", width = 15)
    @ApiModelProperty(value = "目录类型")
	private java.lang.String catalogType;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**关联诊断组1*/
	@Excel(name = "关联诊断组1", width = 15)
    @ApiModelProperty(value = "关联诊断组1")
	private java.lang.String diagGroupCode1;
	/**疾病编码1*/
	@Excel(name = "疾病编码1", width = 15)
    @ApiModelProperty(value = "疾病编码1")
	private java.lang.String diagCode1;
	/**疾病名称1*/
	@Excel(name = "疾病名称1", width = 15)
    @ApiModelProperty(value = "疾病名称1")
	private java.lang.String diagName1;
	/**关联诊断组2*/
	@Excel(name = "关联诊断组2", width = 15)
    @ApiModelProperty(value = "关联诊断组2")
	private java.lang.String diagGroupCode2;
	/**疾病编码2*/
	@Excel(name = "疾病编码2", width = 15)
    @ApiModelProperty(value = "疾病编码2")
	private java.lang.String diagCode2;
	/**疾病名称2*/
	@Excel(name = "疾病名称2", width = 15)
    @ApiModelProperty(value = "疾病名称2")
	private java.lang.String diagName2;
	/**手术或操作编码1*/
	@Excel(name = "手术或操作编码1", width = 15)
    @ApiModelProperty(value = "手术或操作编码1")
	private java.lang.String surgeryCode1;
	/**手术或操作名称1*/
	@Excel(name = "手术或操作名称1", width = 15)
    @ApiModelProperty(value = "手术或操作名称1")
	private java.lang.String surgeryName1;
	/**手术或操作编码2*/
	@Excel(name = "手术或操作编码2", width = 15)
    @ApiModelProperty(value = "手术或操作编码2")
	private java.lang.String surgeryCode2;
	/**手术或操作名称2*/
	@Excel(name = "手术或操作名称2", width = 15)
    @ApiModelProperty(value = "手术或操作名称2")
	private java.lang.String surgeryName2;
	/**手术或操作编码3*/
	@Excel(name = "手术或操作编码3", width = 15)
    @ApiModelProperty(value = "手术或操作编码3")
	private java.lang.String surgeryCode3;
	/**手术或操作名称3*/
	@Excel(name = "手术或操作名称3", width = 15)
    @ApiModelProperty(value = "手术或操作名称3")
	private java.lang.String surgeryName3;
	/**分组条件编码*/
	@Excel(name = "分组条件编码", width = 15)
	@ApiModelProperty(value = "分组条件编码")
	private java.lang.String conditionCode;
	/**分组条件*/
	@Excel(name = "分组条件", width = 15)
	@ApiModelProperty(value = "分组条件")
	private java.lang.String conditionName;
	/**是否判断次要诊断*/
	@Excel(name = "是否判断次要诊断", width = 15)
    @ApiModelProperty(value = "是否判断次要诊断")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String validSecondDiag;
	/**是否有效MCC*/
	@Excel(name = "是否有效MCC", width = 15)
    @ApiModelProperty(value = "是否有效MCC")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String validMcc;
	/**是否有效CC*/
	@Excel(name = "是否有效CC", width = 15)
    @ApiModelProperty(value = "是否有效CC")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String validCc;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
    @ApiModelProperty(value = "更新人姓名")
	private java.lang.String updateUsername;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private java.lang.String updateUser;
	/**审核状态0.未审核1.已审核*/
	@Excel(name = "审核状态0.未审核1.已审核", width = 15)
    @ApiModelProperty(value = "审核状态0.未审核1.已审核")
	@MedicalDict(dicCode = "EXAMINE_STATUS")
	private java.lang.String examineStatus;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date examineTime;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private java.lang.String examineUsername;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
	private java.lang.String examineUser;
	/**排除内容*/
	@Excel(name = "排除内容", width = 15)
	@ApiModelProperty(value = "排除内容")
	private java.lang.String excludeContent;
	/**诊断数量*/
	@Excel(name = "诊断数量", width = 15)
	@ApiModelProperty(value = "诊断数量")
	private java.lang.Integer diagNum;
	/**是否手术1*/
	@Excel(name = "是否手术1", width = 15)
	@ApiModelProperty(value = "是否手术1")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String validSurgery1;
	/**是否手术2*/
	@Excel(name = "是否手术2", width = 15)
	@ApiModelProperty(value = "是否手术2")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String validSurgery2;
	/**是否手术3*/
	@Excel(name = "是否手术3", width = 15)
	@ApiModelProperty(value = "是否手术3")
	@MedicalDict(dicCode = "YESNO")
	private java.lang.String validSurgery3;

	/**分组条件限制*/
	@Excel(name = "分组条件限制", width = 15)
	@ApiModelProperty(value = "分组条件限制")
	@MedicalDict(dicCode = "HAVINGORNO")
	private java.lang.String hasCondition;

	@TableField(exist=false)
	private java.lang.String drgRuleLimites;
}
