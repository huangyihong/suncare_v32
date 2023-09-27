package com.ai.modules.ybFj.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * @author : zhangly
 * @date : 2023/3/15 16:29
 */
@Data
public class SyncClueDto {
    /**线索总览编码集合*/
    private List<String> clueIds;
    /**创建时间*/
    private java.util.Date createTime;
    /**创建人*/
    private java.lang.String createUser;
    /**创建人姓名*/
    private java.lang.String createUsername;

    /*上一个环节*/
    private String prevStep;
}
