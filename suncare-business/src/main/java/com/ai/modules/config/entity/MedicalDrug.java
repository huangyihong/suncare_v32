package com.ai.modules.config.entity;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

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

/**
 * @Description: 药品信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_DRUG", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUG对象", description="药品信息")
public class MedicalDrug {

	/**药品信息id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "药品信息id")
	private java.lang.String id;
	/**药品类别码*/
	@Excel(name = "药品类别码", width = 15)
    @ApiModelProperty(value = "药品类别码")
	private java.lang.String typeCode;
	/**药品类别名称*/
	@Excel(name = "药品类别名称", width = 15)
    @ApiModelProperty(value = "药品类别名称")
	private java.lang.String typeName;
	/**药品编码*/
	@Excel(name = "药品编码", width = 15)
    @ApiModelProperty(value = "药品编码")
	private java.lang.String code;
	/**药品名称*/
	@Excel(name = "药品名称", width = 15)
    @ApiModelProperty(value = "药品名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String name;
	/**剂型*/
	@Excel(name = "剂型", width = 15)
    @ApiModelProperty(value = "剂型")
	private java.lang.String dosageType;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @ApiModelProperty(value = "排序号")
	private java.lang.Long orderNum;
	/**描述*/
	@Excel(name = "描述", width = 15)
    @ApiModelProperty(value = "描述")
	private java.lang.String remark;
	/**药品通用名编码*/
	@Excel(name = "药品通用名编码", width = 15)
    @ApiModelProperty(value = "药品通用名编码")
	private java.lang.String generalCode;
	/**药品通用名*/
	@Excel(name = "药品通用名", width = 15)
    @ApiModelProperty(value = "药品通用名")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String generalName;
	/**药品商品名称*/
	@Excel(name = "药品商品名称", width = 15)
    @ApiModelProperty(value = "药品商品名称")
	private java.lang.String productName;
	/**药品英文名称*/
	@Excel(name = "药品英文名称", width = 15)
    @ApiModelProperty(value = "药品英文名称")
	private java.lang.String productEname;
	/**批准文号*/
	@Excel(name = "批准文号", width = 15)
    @ApiModelProperty(value = "批准文号")
	private java.lang.String approveNumber;
	/**药品剂型级别编码*/
	@Excel(name = "药品剂型级别编码", width = 15)
    @ApiModelProperty(value = "药品剂型级别编码")
	private java.lang.String dosageLevelCode;
	/**药品剂型级别*/
	@Excel(name = "药品剂型级别", width = 15)
    @ApiModelProperty(value = "药品剂型级别")
	private java.lang.String dosageLevel;
	/**剂型代码*/
	@Excel(name = "剂型代码", width = 15)
    @ApiModelProperty(value = "剂型代码")
	private java.lang.String dosageCode;
	/**剂型名称*/
	@Excel(name = "剂型名称", width = 15)
    @ApiModelProperty(value = "剂型名称")
	private java.lang.String dosage;
	/**包装数量*/
	@Excel(name = "包装数量", width = 15)
    @ApiModelProperty(value = "包装数量")
	private java.lang.String packageNum;
	/**药品规格级别编码*/
	@Excel(name = "药品规格级别编码", width = 15)
    @ApiModelProperty(value = "药品规格级别编码")
	private java.lang.String specificationLevelCode;
	/**药品规格级别名称*/
	@Excel(name = "药品规格级别名称", width = 15)
    @ApiModelProperty(value = "药品规格级别名称")
	private java.lang.String specificationLevel;
	/**规格*/
	@Excel(name = "规格", width = 15)
    @ApiModelProperty(value = "规格")
	private java.lang.String specification;
	/**药品厂家级别编码*/
	@Excel(name = "药品厂家级别编码", width = 15)
    @ApiModelProperty(value = "药品厂家级别编码")
	private java.lang.String factoryLevelCode;
	/**药品厂家级别名称*/
	@Excel(name = "药品厂家级别名称", width = 15)
    @ApiModelProperty(value = "药品厂家级别名称")
	private java.lang.String factoryLevel;
	/**生产企业代码*/
	@Excel(name = "生产企业代码", width = 15)
    @ApiModelProperty(value = "生产企业代码")
	private java.lang.String enterpriseCode;
	/**生产企业名称*/
	@Excel(name = "生产企业名称", width = 15)
    @ApiModelProperty(value = "生产企业名称")
	private java.lang.String enterprise;
	/**药理一级分类编码*/
	@Excel(name = "药理一级分类编码", width = 15)
    @ApiModelProperty(value = "药理一级分类编码")
	private java.lang.String type1Code;
	/**药理一级分类名称*/
	@Excel(name = "药理一级分类名称", width = 15)
    @ApiModelProperty(value = "药理一级分类名称")
	private java.lang.String type1Name;
	/**药理二级分类编码*/
	@Excel(name = "药理二级分类编码", width = 15)
    @ApiModelProperty(value = "药理二级分类编码")
	private java.lang.String type2Code;
	/**药理二级分类名称*/
	@Excel(name = "药理二级分类名称", width = 15)
    @ApiModelProperty(value = "药理二级分类名称")
	private java.lang.String type2Name;
	/**药理三级分类编码*/
	@Excel(name = "药理三级分类编码", width = 15)
    @ApiModelProperty(value = "药理三级分类编码")
	private java.lang.String type3Code;
	/**药理三级分类名称*/
	@Excel(name = "药理三级分类名称", width = 15)
    @ApiModelProperty(value = "药理三级分类名称")
	private java.lang.String type3Name;
	/**药理四级分类编码*/
	@Excel(name = "药理四级分类编码", width = 15)
    @ApiModelProperty(value = "药理四级分类编码")
	private java.lang.String type4Code;
	/**药理四级分类名称*/
	@Excel(name = "药理四级分类名称", width = 15)
    @ApiModelProperty(value = "药理四级分类名称")
	private java.lang.String type4Name;
	/**药理五级分类编码*/
	@Excel(name = "药理五级分类编码", width = 15)
    @ApiModelProperty(value = "药理五级分类编码")
	private java.lang.String type5Code;
	/**药理五级分类名称*/
	@Excel(name = "药理五级分类名称", width = 15)
    @ApiModelProperty(value = "药理五级分类名称")
	private java.lang.String type5Name;
	/**ATC药品1级代码*/
	@Excel(name = "ATC药品1级代码", width = 15)
    @ApiModelProperty(value = "ATC药品1级代码")
	private java.lang.String act1Code;
	/**ATC药品1级名称*/
	@Excel(name = "ATC药品1级名称", width = 15)
    @ApiModelProperty(value = "ATC药品1级名称")
	private java.lang.String act1Name;
	/**ATC药品2级代码*/
	@Excel(name = "ATC药品2级代码", width = 15)
    @ApiModelProperty(value = "ATC药品2级代码")
	private java.lang.String act2Code;
	/**ATC药品2级名称*/
	@Excel(name = "ATC药品2级名称", width = 15)
    @ApiModelProperty(value = "ATC药品2级名称")
	private java.lang.String act2Name;
	/**ATC药品3级代码*/
	@Excel(name = "ATC药品3级代码", width = 15)
    @ApiModelProperty(value = "ATC药品3级代码")
	private java.lang.String act3Code;
	/**ATC药品3级名称*/
	@Excel(name = "ATC药品3级名称", width = 15)
    @ApiModelProperty(value = "ATC药品3级名称")
	private java.lang.String act3Name;
	/**ATC药品4级代码*/
	@Excel(name = "ATC药品4级代码", width = 15)
    @ApiModelProperty(value = "ATC药品4级代码")
	private java.lang.String act4Code;
	/**ATC药品4级名称*/
	@Excel(name = "ATC药品4级名称", width = 15)
    @ApiModelProperty(value = "ATC药品4级名称")
	private java.lang.String act4Name;
	/**医保1级分类代码*/
	@Excel(name = "医保1级分类代码", width = 15)
    @ApiModelProperty(value = "医保1级分类代码")
	private java.lang.String medicare1Code;
	/**医保1级分类名称*/
	@Excel(name = "医保1级分类名称", width = 15)
    @ApiModelProperty(value = "医保1级分类名称")
	private java.lang.String medicare1Name;
	/**医保2级分类代码*/
	@Excel(name = "医保2级分类代码", width = 15)
    @ApiModelProperty(value = "医保2级分类代码")
	private java.lang.String medicare2Code;
	/**医保2级分类名称*/
	@Excel(name = "医保2级分类名称", width = 15)
    @ApiModelProperty(value = "医保2级分类名称")
	private java.lang.String medicare2Name;
	/**医保3级分类代码*/
	@Excel(name = "医保3级分类代码", width = 15)
    @ApiModelProperty(value = "医保3级分类代码")
	private java.lang.String medicare3Code;
	/**医保3级分类名称*/
	@Excel(name = "医保3级分类名称", width = 15)
    @ApiModelProperty(value = "医保3级分类名称")
	private java.lang.String medicare3Name;
	/**医保4级分类代码*/
	@Excel(name = "医保4级分类代码", width = 15)
    @ApiModelProperty(value = "医保4级分类代码")
	private java.lang.String medicare4Code;
	/**医保4级分类名称*/
	@Excel(name = "医保4级分类名称", width = 15)
    @ApiModelProperty(value = "医保4级分类名称")
	private java.lang.String medicare4Name;

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
	/**收费类别编码*/
	@Excel(name = "收费类别编码", width = 15)
	@ApiModelProperty(value = "收费类别编码")
	private java.lang.String chargeClassCode;
	/**收费类别名称*/
	@Excel(name = "收费类别名称", width = 15)
	@ApiModelProperty(value = "收费类别名称")
	private java.lang.String chargeClassName;
	/**最小包装单位*/
	@Excel(name = "最小包装单位", width = 15)
	@ApiModelProperty(value = "最小包装单位")
	private java.lang.String packageUnit;
	/**最小包装单位编码*/
	@Excel(name = "最小包装单位编码", width = 15)
	@ApiModelProperty(value = "最小包装单位编码")
	private java.lang.String packageUnitCode;
	/**使用单位*/
	@Excel(name = "使用单位", width = 15)
	@ApiModelProperty(value = "使用单位")
	private java.lang.String useUnit;
	/**使用单位编码*/
	@Excel(name = "使用单位编码", width = 15)
	@ApiModelProperty(value = "使用单位编码")
	private java.lang.String useUnitCode;
	/**包装使用转化率*/
	@Excel(name = "包装使用转换率", width = 15)
	@ApiModelProperty(value = "包装使用转换率")
	private java.lang.String packageUseRate;
	/**最小制剂单位*/
	@Excel(name = "最小制剂单位", width = 15)
	@ApiModelProperty(value = "最小制剂单位")
	private java.lang.String preparationUnit;
	/**最小制剂单位编码*/
	@Excel(name = "最小制剂单位编码", width = 15)
	@ApiModelProperty(value = "最小制剂单位编码")
	private java.lang.String preparationUnitCode;
	/**国家医保药品编码*/
	@Excel(name = "国家医保药品编码", width = 15)
	@ApiModelProperty(value = "国家医保药品编码")
	private java.lang.String ybCode;
	/**国家医保药品名称*/
	@Excel(name = "国家医保药品名称", width = 15)
	@ApiModelProperty(value = "国家医保药品名称")
	private java.lang.String ybName;
}
