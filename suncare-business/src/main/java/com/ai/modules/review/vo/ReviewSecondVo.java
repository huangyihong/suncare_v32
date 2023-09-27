package com.ai.modules.review.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/6/5 15
 * @Description:
 */

@EqualsAndHashCode()
@Data
public class ReviewSecondVo{

    @Excel(name = "结果ID", width = 15)
    @ApiModelProperty(value = "结果ID")
    private String id;

    @Excel(name = "就诊ID号", width = 15)
    @ApiModelProperty(value = "就诊ID号")
    private String visitid;

    @Excel(name = "原始就诊ID", width = 15)
    @ApiModelProperty(value = "就诊ID号")
    private String ybVisitid;

    @Excel(name = "疑似程度", width = 15)
    @ApiModelProperty(value = "疑似程度")
    private String firReviewStatus;

    @Excel(name = "就诊机构", width = 15)
    @ApiModelProperty(value = "就诊机构")
    private String orgname;

    @ApiModelProperty(value = "医疗机构等级 ")
    @MedicalDict(dicCode = "YYDJ")
    private String hospgrade;

    @Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
    private String visittype;

    @Excel(name = "就诊科室", width = 15)
    @ApiModelProperty(value = "就诊科室")
    private String deptname;

    @Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
    private String doctorname;

    @Excel(name = "病人姓名", width = 15)
    @ApiModelProperty(value = "病人姓名")
    private String clientname;

    @Excel(name = "医疗费用总金额", width = 15)
    @ApiModelProperty(value = "医疗费用总金额")
    private Double totalfee;

    @Excel(name = "就诊日期", width = 15)
    @ApiModelProperty(value = "就诊日期")
    private String visitdate;

    /**第二次审查状态{wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回}*/
    @Excel(name = "第二次审查状态", width = 15)
    @ApiModelProperty(value = "第二次审查状态")
    private String secReviewStatus;

    @ApiModelProperty(value = "模型ID")
    private List<String> reviewCaseIds;

    @Excel(name = "关联项目数", width = 15)
    @ApiModelProperty(value = "关联项目数")
    private Integer relaItemKind;

    @Excel(name = "关联项目总个数", width = 15)
    @ApiModelProperty(value = "关联项目总个数")
    private Integer relaItemCount;

    @Excel(name = "关联项目总金额", width = 15)
    @ApiModelProperty(value = "关联项目总金额")
    private Double relaItemFee;

}
