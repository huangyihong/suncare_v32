package com.ai.modules.config.entity;

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
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 药品属性表
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_DRUG_PROPERTY", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUG_PROPERTY对象", description="药品属性表")
public class MedicalDrugProperty {
	/**主键id*/
    @ApiModelProperty(value = "主键id")
	@TableId("ID")
	private java.lang.String id;
	/**ATC药品编码*/
	@Excel(name = "ATC药品编码", width = 15)
    @ApiModelProperty(value = "ATC药品编码")
	private java.lang.String code;
	/**ATC药品名称*/
	@Excel(name = "ATC药品名称", width = 15)
    @ApiModelProperty(value = "ATC药品名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String name;
	/**剂型代码*/
	@Excel(name = "剂型代码", width = 15)
    @ApiModelProperty(value = "剂型代码")
	private java.lang.String dosageCode;
	/**剂型名称*/
	@Excel(name = "剂型名称", width = 15)
    @ApiModelProperty(value = "剂型名称")
	private java.lang.String dosageName;
	/**规格*/
	@Excel(name = "规格", width = 15)
    @ApiModelProperty(value = "规格")
	private java.lang.String specificaion;
	/**处方药标志*/
	@Excel(name = "处方药标志", width = 15)
    @ApiModelProperty(value = "处方药标志")
	private java.lang.String presdrugSign;
	/**处方药标志名称*/
	@Excel(name = "处方药标志名称", width = 15)
    @ApiModelProperty(value = "处方药标志名称")
	private java.lang.String presdrugSignname;
	/**非处方药分类*/
	@Excel(name = "非处方药分类", width = 15)
    @ApiModelProperty(value = "非处方药分类")
	private java.lang.String nonrxtypecode;
	/**非处方药分类名称*/
	@Excel(name = "非处方药分类名称", width = 15)
    @ApiModelProperty(value = "非处方药分类名称")
	private java.lang.String nonrxtypename;
	/**毒性药品标志*/
	@Excel(name = "毒性药品标志", width = 15)
    @ApiModelProperty(value = "毒性药品标志")
	private java.lang.String poisonousSign;
	/**毒性药品标志名称*/
	@Excel(name = "毒性药品标志名称", width = 15)
    @ApiModelProperty(value = "毒性药品标志名称")
	private java.lang.String poisonousSignname;
	/**麻醉药品标志*/
	@Excel(name = "麻醉药品标志", width = 15)
    @ApiModelProperty(value = "麻醉药品标志")
	private java.lang.String narcoticSign;
	/**麻醉药品标志名称*/
	@Excel(name = "麻醉药品标志名称", width = 15)
    @ApiModelProperty(value = "麻醉药品标志名称")
	private java.lang.String narcoticSignname;
	/**一类精神药品标志*/
	@Excel(name = "一类精神药品标志", width = 15)
    @ApiModelProperty(value = "一类精神药品标志")
	private java.lang.String psych1Sign;
	/**一类精神药品标志名称*/
	@Excel(name = "一类精神药品标志名称", width = 15)
    @ApiModelProperty(value = "一类精神药品标志名称")
	private java.lang.String psych1Signname;
	/**二类精神药品标志*/
	@Excel(name = "二类精神药品标志", width = 15)
    @ApiModelProperty(value = "二类精神药品标志")
	private java.lang.String psych2Sign;
	/**二类精神药品标志名称*/
	@Excel(name = "二类精神药品标志名称", width = 15)
    @ApiModelProperty(value = "二类精神药品标志名称")
	private java.lang.String psych2Signname;
	/**三类精神药品标志*/
	@Excel(name = "三类精神药品标志", width = 15)
    @ApiModelProperty(value = "三类精神药品标志")
	private java.lang.String psych3Sign;
	/**三类精神药品标志名称*/
	@Excel(name = "三类精神药品标志名称", width = 15)
    @ApiModelProperty(value = "三类精神药品标志名称")
	private java.lang.String psych3Signname;
	/**生物制品标志*/
	@Excel(name = "生物制品标志", width = 15)
    @ApiModelProperty(value = "生物制品标志")
	private java.lang.String biologicSign;
	/**生物制品标志名称*/
	@Excel(name = "生物制品标志名称", width = 15)
    @ApiModelProperty(value = "生物制品标志名称")
	private java.lang.String biologicSignname;
	/**血液制品类标志*/
	@Excel(name = "血液制品类标志", width = 15)
    @ApiModelProperty(value = "血液制品类标志")
	private java.lang.String bloodprodSign;
	/**血液制品类标志名称*/
	@Excel(name = "血液制品类标志名称", width = 15)
    @ApiModelProperty(value = "血液制品类标志名称")
	private java.lang.String bloodprodSignname;
	/**放射性药品标志*/
	@Excel(name = "放射性药品标志", width = 15)
    @ApiModelProperty(value = "放射性药品标志")
	private java.lang.String radioSign;
	/**放射性药品标志名称*/
	@Excel(name = "放射性药品标志名称", width = 15)
    @ApiModelProperty(value = "放射性药品标志名称")
	private java.lang.String radioSignname;
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
	
	/**序号*/
	@Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "orderNum")
	private java.lang.Integer orderNum;
}
