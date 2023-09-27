package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalDrugRule;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Auther: zhangpeng
 * @Date: 2020/6/19 17
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalDrugRuleVO extends MedicalDrugRule {

    /**试算状态*/
    @Excel(name = "试算状态", width = 15)
    @ApiModelProperty(value = "试算状态")
//    @MedicalDict(dicCode = "RUN_STATUS")
    private String trailStatus;

    /**试算日志*/
    @Excel(name = "试算日志", width = 15)
    @ApiModelProperty(value = "试算日志")
    private String trailMsg;
}
