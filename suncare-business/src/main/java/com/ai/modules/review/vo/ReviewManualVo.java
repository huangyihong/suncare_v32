package com.ai.modules.review.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/6/5 15
 * @Description:
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class ReviewManualVo extends ReviewSystemViewVo{
    /**就诊机构*/
    @Excel(name = "就诊机构", width = 15)
    @ApiModelProperty(value = "就诊机构")
    private String orgname;
    /**就诊科室*/
    @Excel(name = "就诊科室", width = 15)
    @ApiModelProperty(value = "就诊科室")
    private String deptname;
    /**医生*/
    @Excel(name = "医生", width = 15)
    @ApiModelProperty(value = "医生")
    private String doctorname;
    /**诊断名称*/
/*    @Excel(name = "诊断名称", width = 15)
    @ApiModelProperty(value = "诊断名称")
    private String pathonogyDisease;*/
    /**就诊类型*/
    @Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
    private String visittype;
    /**参保类型*/
    @Excel(name = "参保类型", width = 15)
    @ApiModelProperty(value = "参保类型")
    private String insurancetype;
    /**就诊日期*/
    @Excel(name = "就诊日期", width = 15)
    @ApiModelProperty(value = "就诊日期")
    private String visitdate;
    /**出院日期*/
    @Excel(name = "出院日期", width = 15)
    @ApiModelProperty(value = "出院日期")
    private String leavedate;
    /**违反模型*/
    @Excel(name = "违反模型", width = 15)
    @ApiModelProperty(value = "违反模型")
    private List<String> caseName;
    /**推送模型*/
    @Excel(name = "推送模型", width = 15)
    @ApiModelProperty(value = "推送模型")
    private List<String> reviewCaseIds;
    /**审核人*/
    @Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
    private String firReviewUsername;
    /**审核人ID*/
    @Excel(name = "审核人ID", width = 15)
    @ApiModelProperty(value = "审核人ID")
    private String firReviewUserid;
    /**状态*/
    @Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    private String firReviewStatus;
}
