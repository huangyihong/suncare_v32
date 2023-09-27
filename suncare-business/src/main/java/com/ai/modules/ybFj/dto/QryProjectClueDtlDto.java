package com.ai.modules.ybFj.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @author : zhangly
 * @date : 2023/3/8 13:51
 */
@Data
public class QryProjectClueDtlDto {
    /**yb_fj_project_clue.clue_id*/
    @Excel(name = "yb_fj_project_clue.clue_id", width = 15)
    @ApiModelProperty(value = "yb_fj_project_clue.clue_id")
    private java.lang.String clueId;
    /**就诊id*/
    @Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
    private java.lang.String visitid;
    /**医生姓名*/
    @Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
    private java.lang.String doctorname;
    /**科室名称*/
    @Excel(name = "科室名称", width = 15)
    @ApiModelProperty(value = "科室名称")
    private java.lang.String deptname;
    /**就诊类型*/
    @Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
    private java.lang.String visittype;
    /**病人姓名*/
    @Excel(name = "病人姓名", width = 15)
    @ApiModelProperty(value = "病人姓名")
    private java.lang.String clientname;
    /**性别*/
    @Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
    private java.lang.String sex;
    /**就诊日期*/
    @Excel(name = "就诊日期", width = 15)
    @ApiModelProperty(value = "就诊日期")
    private java.lang.String visitdate;
    /**离院日期*/
    @Excel(name = "离院日期", width = 15)
    @ApiModelProperty(value = "离院日期")
    private java.lang.String leavedate;
    /**年龄*/
    @Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
    private java.math.BigDecimal yearage;
    /**疾病名称*/
    @Excel(name = "疾病名称", width = 15)
    @ApiModelProperty(value = "疾病名称")
    private java.lang.String dis;
    /**his项目名称*/
    @Excel(name = "his项目名称", width = 15)
    @ApiModelProperty(value = "his项目名称")
    private java.lang.String hisItemname;
    /**项目名称*/
    @Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
    private java.lang.String itemname;
    /**项目类别*/
    @Excel(name = "项目类别", width = 15)
    @ApiModelProperty(value = "项目类别")
    private java.lang.String chargeattri;
}
