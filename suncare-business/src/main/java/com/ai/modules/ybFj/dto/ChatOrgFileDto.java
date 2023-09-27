package com.ai.modules.ybFj.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @author : zhangly
 * @date : 2023/3/21 14:52
 */
@Data
public class ChatOrgFileDto {

    /**医疗机构编码*/
    @ApiModelProperty(value = "医疗机构编码")
    private java.lang.String orgId;
    /**文件类型*/
    @ApiModelProperty(value = "文件类型")
    private java.lang.String fileType;
    /**文件原名*/
    @ApiModelProperty(value = "文件原名")
    private java.lang.String fileSrcname;
    /**发送人姓名*/
    @Excel(name = "发送人姓名", width = 15)
    @ApiModelProperty(value = "发送人姓名")
    private java.lang.String username;
}
