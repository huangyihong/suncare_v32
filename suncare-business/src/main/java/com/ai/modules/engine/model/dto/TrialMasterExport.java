package com.ai.modules.engine.model.dto;

import com.ai.modules.review.vo.DwbMasterInfoVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Auther: zhangpeng
 * @Date: 2020/7/1 16
 * @Description:
 */
@Data
public class TrialMasterExport extends DwbMasterInfoVo {

    @Excel(name = "项目类别", width = 15)
    @ApiModelProperty(value = "项目类别")
    private String itemclass;
    @Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
    private String itemname;
    @Excel(name = "项目数量", width = 15)
    @ApiModelProperty(value = "项目数量")
    private Double amount;
    @Excel(name = "项目单价", width = 15)
    @ApiModelProperty(value = "项目单价")
    private Double itemprice;
    @Excel(name = "项目总金额", width = 15)
    @ApiModelProperty(value = "项目总金额")
    private Double fee;
}
