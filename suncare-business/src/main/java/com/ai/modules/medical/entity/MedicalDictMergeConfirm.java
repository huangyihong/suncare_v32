package com.ai.modules.medical.entity;

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
 * @Description: 基础字典合并操作影响到需人工确认的规则表
 * @Author: jeecg-boot
 * @Date:   2021-07-20
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DICT_MERGE_CONFIRM")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DICT_MERGE_CONFIRM对象", description="基础字典合并操作影响到需人工确认的规则表")
public class MedicalDictMergeConfirm {
    
	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	private java.lang.String confirmId;
	/**规则ID*/
	@Excel(name = "规则ID", width = 15)
    @ApiModelProperty(value = "规则ID")
	private java.lang.String itemId;
	/**规则名称*/
	@Excel(name = "规则名称", width = 15)
    @ApiModelProperty(value = "规则名称")
	private java.lang.String itemName;
	/**规则类型*/
	@Excel(name = "规则类型", width = 15)
    @ApiModelProperty(value = "规则类型")
	private java.lang.String itemType;
	/**MEDICAL_DICT_MERGE_LOG.log_id*/
	@Excel(name = "MEDICAL_DICT_MERGE_LOG.log_id", width = 15)
    @ApiModelProperty(value = "MEDICAL_DICT_MERGE_LOG.log_id")
	private java.lang.String logId;
	/**状态{finish:完成,hand:待处理}*/
	@Excel(name = "状态{finish:完成,hand:待处理}", width = 15)
    @ApiModelProperty(value = "状态{finish:完成,hand:待处理}")
	private java.lang.String confirmStatus;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
}
