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

import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 医疗器械说明书
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_EQUIP_INSTRUCTION", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_EQUIP_INSTRUCTION对象", description="医疗器械说明书")
public class MedicalEquipInstruction {

	/**医疗器械说明书id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "医疗器械说明书id")
	private java.lang.String id;
	/**说明书编码*/
	@Excel(name = "说明书编码", width = 15)
    @ApiModelProperty(value = "说明书编码")
	private java.lang.String code;
	/**说明书器械名称*/
	@Excel(name = "说明书器械名称", width = 15)
    @ApiModelProperty(value = "说明书器械名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String name;
	/**说明书修订日期*/
	@Excel(name = "说明书修订日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "说明书修订日期")
	private java.util.Date revisionDate;
	/**商品名*/
	@Excel(name = "商品名", width = 15)
    @ApiModelProperty(value = "商品名")
	private java.lang.String tradeName;
	/**英文名*/
	@Excel(name = "英文名", width = 15)
    @ApiModelProperty(value = "英文名")
	private java.lang.String ename;
	/**汉语拼音*/
	@Excel(name = "汉语拼音", width = 15)
    @ApiModelProperty(value = "汉语拼音")
	private java.lang.String cname;
	/**产品性能结构及组成*/
	@Excel(name = "产品性能结构及组成", width = 15)
    @ApiModelProperty(value = "产品性能结构及组成")
	private java.lang.String ingredient;
	/**规格*/
	@Excel(name = "规格", width = 15)
    @ApiModelProperty(value = "规格")
	private java.lang.String specificaion;
	/**适用范围*/
	@Excel(name = "适用范围", width = 15)
    @ApiModelProperty(value = "适用范围")
	private java.lang.String applicationRange;
	/**使用说明*/
	@Excel(name = "使用说明", width = 15)
    @ApiModelProperty(value = "使用说明")
	private java.lang.String instructions;
	/**禁忌*/
	@Excel(name = "禁忌", width = 15)
    @ApiModelProperty(value = "禁忌")
	private java.lang.String taboo;
	/**注意事项*/
	@Excel(name = "注意事项", width = 15)
    @ApiModelProperty(value = "注意事项")
	private java.lang.String attention;
	/**贮藏*/
	@Excel(name = "贮藏", width = 15)
    @ApiModelProperty(value = "贮藏")
	private java.lang.String storage;
	/**包装*/
	@Excel(name = "包装", width = 15)
    @ApiModelProperty(value = "包装")
	private java.lang.String packaging;
	/**执行标准*/
	@Excel(name = "执行标准", width = 15)
    @ApiModelProperty(value = "执行标准")
	private java.lang.String standard;
	/**批准文号*/
	@Excel(name = "批准文号", width = 15)
    @ApiModelProperty(value = "批准文号")
	private java.lang.String approveNumber;
	/**生产企业名称*/
	@Excel(name = "生产企业名称", width = 15)
    @ApiModelProperty(value = "生产企业名称")
	private java.lang.String enterprise;
	/**生产地址*/
	@Excel(name = "生产地址", width = 15)
    @ApiModelProperty(value = "生产地址")
	private java.lang.String productionAddress;
	/**说明书来源*/
	@Excel(name = "说明书来源", width = 15)
    @ApiModelProperty(value = "说明书来源")
	private java.lang.String source;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**停用标识*/
	@Excel(name = "停用标识", width = 15)
    @ApiModelProperty(value = "停用标识")
	private java.lang.String isStopUsed;
	/**附件名称*/
	@Excel(name = "附件名称", width = 15)
	@ApiModelProperty(value = "附件名称")
	private java.lang.String filenames;
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
}
