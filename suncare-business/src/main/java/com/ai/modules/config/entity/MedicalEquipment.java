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

/**
 * @Description: 医疗器械信息表
 * @Author: jeecg-boot
 * @Date:   2020-05-09
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_EQUIPMENT", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_EQUIPMENT对象", description="医疗器械信息表")
public class MedicalEquipment {

	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**流水号*/
	@Excel(name = "流水号", width = 15)
    @ApiModelProperty(value = "流水号")
	private java.lang.Long orderId;
	/**产品编码*/
	@Excel(name = "产品编码", width = 15)
    @ApiModelProperty(value = "产品编码")
	private java.lang.String productcode;
	/**产品名称*/
	@Excel(name = "产品名称", width = 15)
    @ApiModelProperty(value = "产品名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String productname;
	/**商品名称*/
	@Excel(name = "商品名称", width = 15)
    @ApiModelProperty(value = "商品名称")
	private java.lang.String brandname;
	/**规格型号编码*/
	@Excel(name = "规格型号编码", width = 15)
    @ApiModelProperty(value = "规格型号编码")
	private java.lang.String specificaioncode;
	/**规格型号*/
	@Excel(name = "规格型号", width = 15)
    @ApiModelProperty(value = "规格型号")
	private java.lang.String specificaion;
	/**产品描述*/
	@Excel(name = "产品描述", width = 15)
    @ApiModelProperty(value = "产品描述")
	private java.lang.String productdiscription;
	/**预期用途*/
	@Excel(name = "预期用途", width = 15)
    @ApiModelProperty(value = "预期用途")
	private java.lang.String intendeduse;
	/**医疗器械注册人/备案人编码*/
	@Excel(name = "医疗器械注册人/备案人编码", width = 15)
    @ApiModelProperty(value = "医疗器械注册人/备案人编码")
	private java.lang.String registrantcode;
	/**医疗器械注册人/备案人名称*/
	@Excel(name = "医疗器械注册人/备案人名称", width = 15)
    @ApiModelProperty(value = "医疗器械注册人/备案人名称")
	private java.lang.String registrantname;
	/**医疗器械注册人/备案人英文名称*/
	@Excel(name = "医疗器械注册人/备案人英文名称", width = 15)
    @ApiModelProperty(value = "医疗器械注册人/备案人英文名称")
	private java.lang.String registrantenglishname;
	/**医疗器械唯一标识编码体系名称*/
	@Excel(name = "医疗器械唯一标识编码体系名称", width = 15)
    @ApiModelProperty(value = "医疗器械唯一标识编码体系名称")
	private java.lang.String codingsyetemname;
	/**产品通用名分类父级编码*/
	@Excel(name = "产品通用名分类父级编码", width = 15)
    @ApiModelProperty(value = "产品通用名分类父级编码")
	private java.lang.String equipmentParentcode;
	/**产品通用名分类父级名称*/
	@Excel(name = "产品通用名分类父级名称", width = 15)
    @ApiModelProperty(value = "产品通用名分类父级名称")
	private java.lang.String equipmentParentname;
	/**器械类别*/
	@Excel(name = "器械类别", width = 15)
    @ApiModelProperty(value = "器械类别")
	private java.lang.String equipmentclassification;
	/**原分类编码*/
	@Excel(name = "原分类编码", width = 15)
    @ApiModelProperty(value = "原分类编码")
	private java.lang.String equipmentClassOldcode;
	/**原分类名称*/
	@Excel(name = "原分类名称", width = 15)
    @ApiModelProperty(value = "原分类名称")
	private java.lang.String equipmentClassOldname;
	/**分类编码*/
	@Excel(name = "分类编码", width = 15)
    @ApiModelProperty(value = "分类编码")
	private java.lang.String equipmentClassCode;
	/**分类名称*/
	@Excel(name = "分类名称", width = 15)
    @ApiModelProperty(value = "分类名称")
	private java.lang.String equipmentClassName;
	/**产品类别编码*/
	@Excel(name = "产品类别编码", width = 15)
    @ApiModelProperty(value = "产品类别编码")
	private java.lang.String productClassCode;
	/**产品类别名称*/
	@Excel(name = "产品类别名称", width = 15)
    @ApiModelProperty(value = "产品类别名称")
	private java.lang.String productClassName;
	/**最小销售单元中使用单元的数量*/
	@Excel(name = "最小销售单元中使用单元的数量", width = 15)
    @ApiModelProperty(value = "最小销售单元中使用单元的数量")
	private java.lang.Long unitsnumber;
	/**是否有本体直接标识编码*/
	@Excel(name = "是否有本体直接标识编码", width = 15)
    @ApiModelProperty(value = "是否有本体直接标识编码")
	private java.lang.String isDirectIdentifyCode;
	/**是否有本体直接标识名称*/
	@Excel(name = "是否有本体直接标识名称", width = 15)
    @ApiModelProperty(value = "是否有本体直接标识名称")
	private java.lang.String isDirectIdentifyName;
	/**本体产品标识*/
	@Excel(name = "本体产品标识", width = 15)
    @ApiModelProperty(value = "本体产品标识")
	private java.lang.String directIdentifyCode;
	/**本体产品标识与最小销售单元产品标识是否一致编码*/
	@Excel(name = "本体产品标识与最小销售单元产品标识是否一致编码", width = 15)
    @ApiModelProperty(value = "本体产品标识与最小销售单元产品标识是否一致编码")
	private java.lang.String isSameSaleIdentifyCode;
	/**是否有本体直接标识名称*/
	@Excel(name = "本体产品标识与最小销售单元产品标识是否一致名称", width = 15)
    @ApiModelProperty(value = "本体产品标识与最小销售单元产品标识是否一致名称")
	private java.lang.String isSameSaleIdentifyName;
	/**是否为包类/组套类产品编码*/
	@Excel(name = "是否为包类/组套类产品编码", width = 15)
    @ApiModelProperty(value = "是否为包类/组套类产品编码")
	private java.lang.String isPackageCode;
	/**是否为包类/组套类产品名称*/
	@Excel(name = "是否为包类/组套类产品名称", width = 15)
    @ApiModelProperty(value = "是否为包类/组套类产品名称")
	private java.lang.String isPackageName;
	/**产品货号或编号*/
	@Excel(name = "产品货号或编号", width = 15)
    @ApiModelProperty(value = "产品货号或编号")
	private java.lang.String productArtno;
	/**注册证编号或者备案凭证编号*/
	@Excel(name = "注册证编号或者备案凭证编号", width = 15)
    @ApiModelProperty(value = "注册证编号或者备案凭证编号")
	private java.lang.String sfdaNo;
	/**最大重复使用次数*/
	@Excel(name = "最大重复使用次数", width = 15)
    @ApiModelProperty(value = "最大重复使用次数")
	private java.lang.String maxreusetimes;
	/**是否为无菌包装代码*/
	@Excel(name = "是否为无菌包装代码", width = 15)
    @ApiModelProperty(value = "是否为无菌包装代码")
	private java.lang.String isSterilepackageCode;
	/**是否为无菌包装名称*/
	@Excel(name = "是否为无菌包装名称", width = 15)
    @ApiModelProperty(value = "是否为无菌包装名称")
	private java.lang.String isSterilepackageName;
	/**医保编码*/
	@Excel(name = "医保编码", width = 15)
    @ApiModelProperty(value = "医保编码")
	private java.lang.String medicalinsurancecode;
	/**特殊储存或操作条件*/
	@Excel(name = "特殊储存或操作条件", width = 15)
    @ApiModelProperty(value = "特殊储存或操作条件")
	private java.lang.String storageOperateDescrip;
	/**特殊尺寸说明*/
	@Excel(name = "特殊尺寸说明", width = 15)
    @ApiModelProperty(value = "特殊尺寸说明")
	private java.lang.String sizedescrip;
	/**主键编号*/
	@Excel(name = "主键编号", width = 15)
    @ApiModelProperty(value = "主键编号")
	private java.lang.String primarykeycode;
	/**公开的版本号*/
	@Excel(name = "公开的版本号", width = 15)
    @ApiModelProperty(value = "公开的版本号")
	private java.lang.String publicversionno;
	/**NMPA版本的发布时间*/
	@Excel(name = "NMPA版本的发布时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "NMPA版本的发布时间")
	private java.util.Date nmpaversiontime;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String commentnote;
	/**是否标记为一次性使用代码*/
	@Excel(name = "是否标记为一次性使用代码", width = 15)
    @ApiModelProperty(value = "是否标记为一次性使用代码")
	private java.lang.String isDisposableCode;
	/**是否标记为一次性使用名称*/
	@Excel(name = "是否标记为一次性使用名称", width = 15)
    @ApiModelProperty(value = "是否标记为一次性使用名称")
	private java.lang.String isDisposableName;


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
}
