package com.ai.modules.task.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.MedicalDict;

@Data
@ApiModel(value = "AI_MODEL_RESULT对象", description = "AI模型结果")
public class AiModelResultVO {
    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "明细id")
    private String mxId;

    @ApiModelProperty(value = "模型id")
    private String batchId;

    @ApiModelProperty(value = "医疗机构名称")
    private String orgname;

    @ApiModelProperty(value = "就诊id")
    private String visitid;

    @ApiModelProperty(value = "项目编码")
    private String itemcode;

    @ApiModelProperty(value = "项目名称")
    private String itemname;

    @ApiModelProperty(value = "概率")
    private Double probability;

    @ApiModelProperty(value = "预测结果")
    private Integer predictResult;

    @ApiModelProperty(value = "特征详情")
    private String feature;

    @ApiModelProperty(value = "原因")
    private String reason;

    @ApiModelProperty(value = "任务ID")
    private String taskId;

    @ApiModelProperty(value = "临床是否合理{init:待处理,white:白名单,blank:黑名单,grey:灰名单}")
    //@MedicalDict(dicCode = "FIRST_REVIEW_STATUS")
    private String handleLabel;

    @ApiModelProperty(value = "临床原因")
    private String handleReason;

    @ApiModelProperty(value = "人工审核状态")
    private Integer handleStatus;

    @ApiModelProperty(value = "是否为审核黑名单")
    private Integer isBlack;

    @ApiModelProperty(value = "项目地")
    private String dataSource;

    @ApiModelProperty(value = "概率")
    private String probabilityStr;

    @ApiModelProperty(value = "人工审核状态")
    private String handleStatusStr;
}
