package com.ai.modules.ybFj.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;


@Data
public class OrgUserVo {
    /**用户id*/
    @ApiModelProperty(value = "用户id")
    private String id;
    /**用户名称*/
    @ApiModelProperty(value = "用户名称")
    private String username;
    /**真实姓名*/
    @ApiModelProperty(value = "真实姓名")
    private String realname;
    /**医院id*/
    @ApiModelProperty(value = "医院id")
    private String orgId;
    /**方向*/
    @ApiModelProperty(value = "方向")
    private String direction;
    /**左边关键字*/
    @ApiModelProperty(value = "左边关键字")
    private String leftWord;
    /**右边关键字*/
    @ApiModelProperty(value = "右边关键字")
    private String rightWord;
    /**所属系统*/
    @ApiModelProperty(value = "所属系统")
    private String systemCode;
    /**医疗机构名称*/
    @ApiModelProperty(value = "医疗机构名称")
    private String orgName;
}
