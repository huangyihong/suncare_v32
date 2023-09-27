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
 * @Description: 基础字典合并日志表
 * @Author: jeecg-boot
 * @Date:   2021-07-20
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DICT_MERGE_LOG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DICT_MERGE_LOG对象", description="基础字典合并日志表")
public class MedicalDictMergeLog {
    
	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	private java.lang.String logId;
	/**主项，保留项*/
	@Excel(name = "主项，保留项", width = 15)
    @ApiModelProperty(value = "主项，保留项")
	private java.lang.String dictMain;
	/**主项名称*/
	@Excel(name = "主项名称", width = 15)
    @ApiModelProperty(value = "主项名称")
	private java.lang.String dictMainname;
	/**重复项，逻辑删除项*/
	@Excel(name = "重复项，逻辑删除项", width = 15)
    @ApiModelProperty(value = "重复项，逻辑删除项")
	private java.lang.String dictRepeat;
	/**重复项名称*/
	@Excel(name = "重复项名称", width = 15)
    @ApiModelProperty(value = "重复项名称")
	private java.lang.String dictRepeatname;
	/**字典类型*/
	@Excel(name = "字典类型", width = 15)
    @ApiModelProperty(value = "字典类型")
	private java.lang.String dictType;
	/**状态{finish:完成,hand:待处理}*/
	@Excel(name = "状态{finish:完成,hand:待处理}", width = 15)
    @ApiModelProperty(value = "状态{finish:完成,hand:待处理}")
	private java.lang.String dictStatus;
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
