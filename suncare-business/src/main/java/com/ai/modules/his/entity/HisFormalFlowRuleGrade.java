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
 * @Description: 模型流程节点规则备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Data
@TableName("HIS_FORMAL_FLOW_RULE_GRADE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="HIS_FORMAL_FLOW_RULE_GRADE对象", description="模型流程节点规则备份")
public class HisFormalFlowRuleGrade {
    
	/**batchId*/
	@Excel(name = "batchId", width = 15)
    @ApiModelProperty(value = "batchId")
	private java.lang.String batchId;
	/**ID*/
	@Excel(name = "ID", width = 15)
    @ApiModelProperty(value = "ID")
	private java.lang.String gradeId;
	/**模型ID*/
	@Excel(name = "模型ID", width = 15)
    @ApiModelProperty(value = "模型ID")
	private java.lang.String caseId;
	/**评价指标所属表*/
	@Excel(name = "评价指标所属表", width = 15)
    @ApiModelProperty(value = "评价指标所属表")
	private java.lang.String evaluateTable;
	/**评价指标*/
	@Excel(name = "评价指标", width = 15)
    @ApiModelProperty(value = "评价指标")
	private java.lang.String evaluateField;
	/**评价指标中文名称*/
	@Excel(name = "评价指标中文名称", width = 15)
    @ApiModelProperty(value = "评价指标中文名称")
	private java.lang.String evaluateFieldname;
	/**基准值*/
	@Excel(name = "基准值", width = 15)
    @ApiModelProperty(value = "基准值")
	private java.math.BigDecimal standardVal;
	/**权重*/
	@Excel(name = "权重", width = 15)
    @ApiModelProperty(value = "权重")
	private java.math.BigDecimal weight;
	/**计算方式*/
	@Excel(name = "计算方式", width = 15)
    @ApiModelProperty(value = "计算方式")
	private java.lang.String method;
	/**节点ID*/
	@Excel(name = "节点ID", width = 15)
    @ApiModelProperty(value = "节点ID")
	private java.lang.String nodeId;
	/**节点编码*/
	@Excel(name = "节点编码", width = 15)
    @ApiModelProperty(value = "节点编码")
	private java.lang.String nodeCode;
	/**backTime*/
	@Excel(name = "backTime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "backTime")
	private java.util.Date backTime;
}
