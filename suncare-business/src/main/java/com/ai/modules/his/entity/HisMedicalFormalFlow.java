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
 * @Description: 风控模型正式流程备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Data
@TableName("HIS_MEDICAL_FORMAL_FLOW")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="HIS_MEDICAL_FORMAL_FLOW对象", description="风控模型正式流程备份")
public class HisMedicalFormalFlow {
    
	/**batchId*/
	@Excel(name = "batchId", width = 15)
    @ApiModelProperty(value = "batchId")
	private java.lang.String batchId;
	/**节点ID*/
	@Excel(name = "节点ID", width = 15)
    @ApiModelProperty(value = "节点ID")
	private java.lang.String nodeId;
	/**流程节点编号*/
	@Excel(name = "流程节点编号", width = 15)
    @ApiModelProperty(value = "流程节点编号")
	private java.lang.String nodeCode;
	/**节点类型*/
	@Excel(name = "节点类型", width = 15)
    @ApiModelProperty(value = "节点类型")
	private java.lang.String nodeType;
	/**节点名称*/
	@Excel(name = "节点名称", width = 15)
    @ApiModelProperty(value = "节点名称")
	private java.lang.String nodeName;
	/**上一个节点编号*/
	@Excel(name = "上一个节点编号", width = 15)
    @ApiModelProperty(value = "上一个节点编号")
	private java.lang.String prevNodeCode;
	/**上个节点条件{yes:是,no:否}*/
	@Excel(name = "上个节点条件{yes:是,no:否}", width = 15)
    @ApiModelProperty(value = "上个节点条件{yes:是,no:否}")
	private java.lang.String prevNodeCondition;
	/**模型ID*/
	@Excel(name = "模型ID", width = 15)
    @ApiModelProperty(value = "模型ID")
	private java.lang.String caseId;
	/**参数编号*/
	@Excel(name = "参数编号", width = 15)
    @ApiModelProperty(value = "参数编号")
	private java.lang.String paramCode;
	/**序号*/
	@Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "序号")
	private java.lang.Integer orderNo;
	/**backTime*/
	@Excel(name = "backTime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "backTime")
	private java.util.Date backTime;
}
