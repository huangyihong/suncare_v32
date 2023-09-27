package com.ai.modules.ybFj.vo;

import com.ai.modules.ybFj.entity.YbFjChatOrg;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @author : zhangly
 * @date : 2023/3/21 11:02
 */
@Data
public class YbFjChatOrgVo extends YbFjChatOrg {

    /**医疗机构名称*/
    @ApiModelProperty(value = "医疗机构名称")
    private java.lang.String orgName;
    /**负责人*/
    @ApiModelProperty(value = "负责人")
    private java.lang.String responsible;

    /**未读记录数*/
    @ApiModelProperty(value = "未读记录数")
    private Integer noReadCnt;
}
