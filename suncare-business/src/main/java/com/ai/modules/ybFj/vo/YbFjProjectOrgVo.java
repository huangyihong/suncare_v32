package com.ai.modules.ybFj.vo;

import com.ai.modules.ybFj.entity.YbFjProjectOrg;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

@EqualsAndHashCode(callSuper = false)
@Data
public class YbFjProjectOrgVo extends YbFjProjectOrg {

    /**项目名称*/
    @Excel(name = "统一社会信用代码", width = 15)
    @ApiModelProperty(value = "统一社会信用代码")
    private java.lang.String socialCode;
    /**负责人*/
    @Excel(name = "负责人", width = 15)
    @ApiModelProperty(value = "负责人")
    private String responsible;
    /**负责人联系电话*/
    @Excel(name = "负责人联系电话", width = 15)
    @ApiModelProperty(value = "负责人联系电话")
    private String responsiblePhone;
    /**详细地址*/
    @Excel(name = "详细地址", width = 15)
    @ApiModelProperty(value = "详细地址")
    private String orgAddress;
}
