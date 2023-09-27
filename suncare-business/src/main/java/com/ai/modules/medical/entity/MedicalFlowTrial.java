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
 * @Description: 风控模型流程节点试算表
 * @Author: jeecg-boot
 * @Date:   2021-08-26
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_FLOW_TRIAL")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_FLOW_TRIAL对象", description="风控模型流程节点试算表")
public class MedicalFlowTrial {
    
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**流程节点编号*/
	@Excel(name = "流程节点编号", width = 15)
    @ApiModelProperty(value = "流程节点编号")
	private java.lang.String nodeCode;
	/**节点名称*/
	@Excel(name = "节点名称", width = 15)
    @ApiModelProperty(value = "节点名称")
	private java.lang.String nodeName;
	/**模型ID*/
	@Excel(name = "模型ID", width = 15)
    @ApiModelProperty(value = "模型ID")
	private java.lang.String caseId;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	private java.lang.String project;
	/**记录数*/
	@Excel(name = "记录数", width = 15)
    @ApiModelProperty(value = "记录数")
	private java.lang.Long numFound;
	/**序号*/
	@Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "序号")
	private java.lang.Integer orderNo;
	/**类型{formal:模型库,probe:探查库}*/
	@Excel(name = "类型{formal:模型库,probe:探查库}", width = 15)
    @ApiModelProperty(value = "类型{formal:模型库,probe:探查库}")
	private java.lang.String type;
	/**状态{wait:待处理,running:试算中,normal:完成,abnormal:异常}*/
	@Excel(name = "状态{wait:待处理,running:试算中,normal:完成,abnormal:异常}", width = 15)
    @ApiModelProperty(value = "状态{wait:待处理,running:试算中,normal:完成,abnormal:异常}")
	private java.lang.String status;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "开始时间")
	private java.util.Date startTime;
	/**结束时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "结束时间")
	private java.util.Date endTime;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private String remark;
}
