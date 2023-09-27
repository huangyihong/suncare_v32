package com.ai.modules.ybFj.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : zhangly
 * @date : 2023/3/17 17:02
 */
@Data
public class StatStepClueVo extends StatProjectClueVo {
    /**问题类别数*/
    @ApiModelProperty(value = "问题类别数")
    private Integer issueTypeCnt;
    /**问题类型数*/
    @ApiModelProperty(value = "问题类型数")
    private Integer issueSubtypeCnt;
    /**项目数*/
    @ApiModelProperty(value = "项目数")
    private Integer clueNameCnt;
    /**完成审核数*/
    @ApiModelProperty(value = "完成审核数")
    private Integer auditClueNameCnt;
}
