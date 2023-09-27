package com.ai.modules.his.entity;

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
 * @Description: 批次违反规则关联备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Data
@TableName("HIS_TASK_BATCH_BREAK_RULE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="HIS_TASK_BATCH_BREAK_RULE对象", description="批次违反规则关联备份")
public class HisTaskBatchBreakRule {
    
	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**违反规则ID*/
	@Excel(name = "违反规则ID", width = 15)
    @ApiModelProperty(value = "违反规则ID")
	private java.lang.String ruleId;
	/**违反规则名称*/
	@Excel(name = "违反规则名称", width = 15)
    @ApiModelProperty(value = "违反规则名称")
	private java.lang.String ruleName;
	/**违反规则类型(01模型，02药品,03收费,04临床路径,05KPI)*/
	@Excel(name = "违反规则类型(01模型，02药品,03收费,04临床路径,05KPI)", width = 15)
    @ApiModelProperty(value = "违反规则类型(01模型，02药品,03收费,04临床路径,05KPI)")
	private java.lang.String ruleType;
	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	private java.lang.String batchId;
	/**backTime*/
	@Excel(name = "backTime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "backTime")
	private java.util.Date backTime;
}
