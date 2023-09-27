package com.ai.modules.formal.entity;

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
 * @Description: 模型归纳节点列表
 * @Author: jeecg-boot
 * @Date:   2019-11-29
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_FORMAL_FLOW")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_FORMAL_FLOW对象", description="模型归纳节点列表")
public class MedicalFormalFlow {

	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	@TableId("NODE_ID")
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
	/**上一个节点*/
	@Excel(name = "上一个节点", width = 15)
    @ApiModelProperty(value = "上一个节点")
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
}
