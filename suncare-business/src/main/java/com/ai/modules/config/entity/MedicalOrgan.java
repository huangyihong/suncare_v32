package com.ai.modules.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: 医疗机构
 * @Author: jeecg-boot
 * @Date:   2019-12-31
 * @Version: V1.0
 */
@Data
@TableName(value="medical_organ", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="medical_organ对象", description="医疗机构")
public class MedicalOrgan implements Serializable {
	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**医疗机构编码*/
	@Excel(name = "医疗机构编码", width = 15)
    @ApiModelProperty(value = "医疗机构编码")
	private java.lang.String code;
	
	/**医疗定点编号*/
	@Excel(name = "医疗定点编号", width = 50)
    @ApiModelProperty(value = "医疗定点编号")
	private java.lang.String ybDdbh;
	
	/**医疗机构名称	*/
	@Excel(name = "医疗机构名称	", width = 15)
    @ApiModelProperty(value = "医疗机构名称	")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String name;
	/**上级机构ID*/
	@Excel(name = "上级机构ID", width = 15)
    @ApiModelProperty(value = "上级机构ID")
	private java.lang.String parentId;
	/**组织机构代码*/
	@Excel(name = "组织机构代码", width = 15)
    @ApiModelProperty(value = "组织机构代码")
	private java.lang.String orgCode;
	/**医疗机构曾用名*/
	@Excel(name = "医疗机构曾用名", width = 15)
    @ApiModelProperty(value = "医疗机构曾用名")
	private java.lang.String orgUsedName;
	/**医疗机构级别编码*/
	@Excel(name = "医疗机构级别编码", width = 15)
    @ApiModelProperty(value = "医疗机构级别编码")
	private java.lang.String orgLevelCode;
	/**医疗机构级别名称*/
	@Excel(name = "医疗机构级别名称", width = 15)
    @ApiModelProperty(value = "医疗机构级别名称")
	private java.lang.String orgLevel;
	/**医疗机构等级编码*/
	@Excel(name = "医疗机构等级编码", width = 15)
    @ApiModelProperty(value = "医疗机构等级编码")
	private java.lang.String orgClassCode;
	/**医疗机构等级名称*/
	@Excel(name = "医疗机构等级名称", width = 15)
    @ApiModelProperty(value = "医疗机构等级名称")
	private java.lang.String orgClass;
	/**地址-省(自治区、直辖市)代码*/
	@Excel(name = "地址-省(自治区、直辖市)代码", width = 15)
    @ApiModelProperty(value = "地址-省(自治区、直辖市)代码")
	private java.lang.String provinceCode;
	/**地址-省(自治区、直辖市)名称*/
	@Excel(name = "地址-省(自治区、直辖市)名称", width = 15)
    @ApiModelProperty(value = "地址-省(自治区、直辖市)名称")
	private java.lang.String provinceName;
	/**地址-市(地区、州)代码*/
	@Excel(name = "地址-市(地区、州)代码", width = 15)
    @ApiModelProperty(value = "地址-市(地区、州)代码")
	private java.lang.String cityCode;
	/**地址-市(地区、州)名称*/
	@Excel(name = "地址-市(地区、州)名称", width = 15)
    @ApiModelProperty(value = "地址-市(地区、州)名称")
	private java.lang.String cityName;
	/**地址-县(区)代码*/
	@Excel(name = "地址-县(区)代码", width = 15)
    @ApiModelProperty(value = "地址-县(区)代码")
	private java.lang.String countyCode;
	/**地址-县(区)名称*/
	@Excel(name = "地址-县(区)名称", width = 15)
    @ApiModelProperty(value = "地址-县(区)名称")
	private java.lang.String countyName;
	/**地址-乡(镇、街道办事处)代码*/
	@Excel(name = "地址-乡(镇、街道办事处)代码", width = 15)
    @ApiModelProperty(value = "地址-乡(镇、街道办事处)代码")
	private java.lang.String townCode;
	/**地址-乡(镇、街道办事处)名称*/
	@Excel(name = "地址-乡(镇、街道办事处)名称", width = 15)
    @ApiModelProperty(value = "地址-乡(镇、街道办事处)名称")
	private java.lang.String townName;
	/**地址-村(街、路、弄等)代码*/
	@Excel(name = "地址-村(街、路、弄等)代码", width = 15)
    @ApiModelProperty(value = "地址-村(街、路、弄等)代码")
	private java.lang.String villageCode;
	/**地址-村(街、路、弄等)名称*/
	@Excel(name = "地址-村(街、路、弄等)名称", width = 15)
    @ApiModelProperty(value = "地址-村(街、路、弄等)名称")
	private java.lang.String villageName;
	/**医疗机构地址*/
	@Excel(name = "医疗机构地址", width = 15)
    @ApiModelProperty(value = "医疗机构地址")
	private java.lang.String address;
	/**医疗机构行政级别编码*/
	@Excel(name = "医疗机构行政级别编码", width = 15)
    @ApiModelProperty(value = "医疗机构行政级别编码")
	private java.lang.String administrativeLevelCode;
	/**医疗机构行政级别名称*/
	@Excel(name = "医疗机构行政级别名称", width = 15)
    @ApiModelProperty(value = "医疗机构行政级别名称")
	private java.lang.String administrativeLevel;
	/**医疗机构类型编码*/
	@Excel(name = "医疗机构类型编码", width = 15)
    @ApiModelProperty(value = "医疗机构类型编码")
	private java.lang.String orgTypeCode;
	/**医疗机构类型名称*/
	@Excel(name = "医疗机构类型名称", width = 15)
    @ApiModelProperty(value = "医疗机构类型名称")
	private java.lang.String orgType;
	/**卫生机构类别编码*/
	@Excel(name = "卫生机构类别编码", width = 15)
    @ApiModelProperty(value = "卫生机构类别编码")
	private java.lang.String healthTypeCode;
	/**卫生机构类别名称*/
	@Excel(name = "卫生机构类别名称", width = 15)
    @ApiModelProperty(value = "卫生机构类别名称")
	private java.lang.String healthType;
	/**医疗机构经营性质编码*/
	@Excel(name = "医疗机构经营性质编码", width = 15)
    @ApiModelProperty(value = "医疗机构经营性质编码")
	private java.lang.String businessNatureCode;
	/**医疗机构经营性质名称*/
	@Excel(name = "医疗机构经营性质名称", width = 15)
    @ApiModelProperty(value = "医疗机构经营性质名称")
	private java.lang.String businessNature;
	/**所有制形式编码*/
	@Excel(name = "所有制形式编码", width = 15)
    @ApiModelProperty(value = "所有制形式编码")
	private java.lang.String ownershipCode;
	/**所有制形式名称*/
	@Excel(name = "所有制形式名称", width = 15)
    @ApiModelProperty(value = "所有制形式名称")
	private java.lang.String ownership;
	/**物价级别编码*/
	@Excel(name = "物价级别编码", width = 15)
    @ApiModelProperty(value = "物价级别编码")
	private java.lang.String priceLevelCode;
	/**物价级别名称*/
	@Excel(name = "物价级别名称", width = 15)
    @ApiModelProperty(value = "物价级别名称")
	private java.lang.String priceLevel;
	/**隶属关系编码*/
	@Excel(name = "隶属关系编码", width = 15)
    @ApiModelProperty(value = "隶属关系编码")
	private java.lang.String membershipCode;
	/**隶属关系名称*/
	@Excel(name = "隶属关系名称", width = 15)
    @ApiModelProperty(value = "隶属关系名称")
	private java.lang.String membership;
	/**设置/主办单位类别编码*/
	@Excel(name = "设置/主办单位类别编码", width = 15)
    @ApiModelProperty(value = "设置/主办单位类别编码")
	private java.lang.String organiserTypeCode;
	/**设置/主办单位类别名称*/
	@Excel(name = "设置/主办单位类别名称", width = 15)
    @ApiModelProperty(value = "设置/主办单位类别名称")
	private java.lang.String organiserType;
	/**主管单位编码*/
	@Excel(name = "主管单位编码", width = 15)
    @ApiModelProperty(value = "主管单位编码")
	private java.lang.String competentUnitCode;
	/**主管单位名称*/
	@Excel(name = "主管单位名称", width = 15)
    @ApiModelProperty(value = "主管单位名称")
	private java.lang.String competentUnit;
	/**医疗机构邮政编码*/
	@Excel(name = "医疗机构邮政编码", width = 15)
    @ApiModelProperty(value = "医疗机构邮政编码")
	private java.lang.String postcode;
	/**医疗机构联系电话*/
	@Excel(name = "医疗机构联系电话", width = 15)
    @ApiModelProperty(value = "医疗机构联系电话")
	private java.lang.String telephone;
	/**新农合定点医疗机构标志编码*/
	@Excel(name = "新农合定点医疗机构标志编码", width = 15)
    @ApiModelProperty(value = "新农合定点医疗机构标志编码")
	private java.lang.String xnhFlagCode;
	/**新农合定点医疗机构标志名称*/
	@Excel(name = "新农合定点医疗机构标志名称", width = 15)
    @ApiModelProperty(value = "新农合定点医疗机构标志名称")
	private java.lang.String xnhFlagName;
	/**医保定点医疗机构标志编码*/
	@Excel(name = "医保定点医疗机构标志编码", width = 15)
    @ApiModelProperty(value = "医保定点医疗机构标志编码")
	private java.lang.String ybFlagCode;
	/**医保定点医疗机构标志名称*/
	@Excel(name = "医保定点医疗机构标志名称", width = 15)
    @ApiModelProperty(value = "医保定点医疗机构标志名称")
	private java.lang.String ybFlagName;
	/**工伤医疗机构标志编码*/
	@Excel(name = "工伤医疗机构标志编码", width = 15)
    @ApiModelProperty(value = "工伤医疗机构标志编码")
	private java.lang.String gsFlagCode;
	/**工伤医疗机构标志名称*/
	@Excel(name = "工伤医疗机构标志名称", width = 15)
    @ApiModelProperty(value = "工伤医疗机构标志名称")
	private java.lang.String gsFlagName;
	/**职业病鉴定机构标志编码*/
	@Excel(name = "职业病鉴定机构标志编码", width = 15)
    @ApiModelProperty(value = "职业病鉴定机构标志编码")
	private java.lang.String zybFlagCode;
	/**职业病鉴定机构标志名称*/
	@Excel(name = "职业病鉴定机构标志名称", width = 15)
    @ApiModelProperty(value = "职业病鉴定机构标志名称")
	private java.lang.String zybFlagName;
	/**医疗机构批准床位数*/
	@Excel(name = "医疗机构批准床位数", width = 15)
    @ApiModelProperty(value = "医疗机构批准床位数")
	private java.lang.String approveBedNum;
	/**医疗机构实际开放床位数*/
	@Excel(name = "医疗机构实际开放床位数", width = 15)
    @ApiModelProperty(value = "医疗机构实际开放床位数")
	private java.lang.String openBedNum;
	/**法人姓名*/
	@Excel(name = "法人姓名", width = 15)
    @ApiModelProperty(value = "法人姓名")
	private java.lang.String legalName;
	/**法人证件类型*/
	@Excel(name = "法人证件类型", width = 15)
    @ApiModelProperty(value = "法人证件类型")
	private java.lang.String legalIdType;
	/**法人证件号码*/
	@Excel(name = "法人证件号码", width = 15)
    @ApiModelProperty(value = "法人证件号码")
	private java.lang.String legalIdNo;
	/**法人联系地址*/
	@Excel(name = "法人联系地址", width = 15)
    @ApiModelProperty(value = "法人联系地址")
	private java.lang.String legalAddress;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**数据状态(0待生效 1有效 2无效)*/
	@Excel(name = "数据状态(0待生效 1有效 2无效)", width = 15)
    @ApiModelProperty(value = "数据状态(0待生效 1有效 2无效)")
	@Dict(dicCode = "base_data_state")
	private java.lang.String state;
	/**审核结果(0待审核 1审核通过 2审核不通过)*/
	@Excel(name = "审核结果(0待审核 1审核通过 2审核不通过)", width = 15)
    @ApiModelProperty(value = "审核结果(0待审核 1审核通过 2审核不通过)")
	@Dict(dicCode = "base_auditResult")
	private java.lang.String auditResult;
	/**操作类型(新增add 修改update 删除delete)*/
	@Excel(name = "操作类型(新增add 修改update 删除delete)", width = 15)
    @ApiModelProperty(value = "操作类型(新增add 修改update 删除delete)")
	@Dict(dicCode = "base_actionType")
	private java.lang.String actionType;
	/**新增人*/
	@Excel(name = "新增人", width = 15)
    @ApiModelProperty(value = "新增人")
	private java.lang.String createStaff;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
    @ApiModelProperty(value = "新增人姓名")
	private java.lang.String createStaffName;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**新增原因*/
	@Excel(name = "新增原因", width = 15)
    @ApiModelProperty(value = "新增原因")
	private java.lang.String createReason;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateStaff;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateStaffName;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**修改原因*/
	@Excel(name = "修改原因", width = 15)
    @ApiModelProperty(value = "修改原因")
	private java.lang.String updateReason;
	/**删除人*/
	@Excel(name = "删除人", width = 15)
    @ApiModelProperty(value = "删除人")
	private java.lang.String deleteStaff;
	/**删除人姓名*/
	@Excel(name = "删除人姓名", width = 15)
    @ApiModelProperty(value = "删除人姓名")
	private java.lang.String deleteStaffName;
	/**删除时间*/
	@Excel(name = "删除时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "删除时间")
	private java.util.Date deleteTime;
	/**删除原因*/
	@Excel(name = "删除原因", width = 15)
    @ApiModelProperty(value = "删除原因")
	private java.lang.String deleteReason;
	/**最新操作人*/
	@Excel(name = "最新操作人", width = 15)
    @ApiModelProperty(value = "最新操作人")
	private java.lang.String actionStaff;
	/**操作人姓名*/
	@Excel(name = "操作人姓名", width = 15)
    @ApiModelProperty(value = "操作人姓名")
	private java.lang.String actionStaffName;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "操作时间")
	private java.util.Date actionTime;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
	private java.lang.String auditStaff;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private java.lang.String auditStaffName;
	/**审核时间*/
	@Excel(name = "审核时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
	private java.util.Date auditTime;
	/**审核意见*/
	@Excel(name = "审核意见", width = 15)
    @ApiModelProperty(value = "审核意见")
	private java.lang.String auditOpinion;
	/**法人联系电话*/
	@Excel(name = "法人联系电话", width = 15)
    @ApiModelProperty(value = "法人联系电话")
	private java.lang.String legalpersonPhone;
	/**所属医保机构编码*/
	@Excel(name = "所属医保机构编码", width = 15)
    @ApiModelProperty(value = "所属医保机构编码")
	private java.lang.String insuranceOrg;
	/**所属医保机构编码*/
	@Excel(name = "所属医保机构编码", width = 15)
    @ApiModelProperty(value = "所属医保机构名称")
	private java.lang.String insuranceOrgname;
	/**所属医保机构编码*/
	@Excel(name = "经纬度", width = 15)
	@ApiModelProperty(value = "经纬度")
	private java.lang.String latLon;
}
