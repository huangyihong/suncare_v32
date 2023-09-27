package com.ai.modules.action.dto;

import lombok.Data;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Auther: zhangpeng
 * @Date: 2020/6/29 16
 * @Description: 不合规病例导出
 */
@Data
public class BreakBehaviorDocExport {

    @Excel(name = "违规主体名称（医护）", width = 15)
    private String targetName;

    @Excel(name = "违规金额（元）", width = 15)
    private Double casePay;

    @Excel(name = "违规病例数（个）", width = 15)
    private Integer caseNum;

    @Excel(name = "不合规行为类型", width = 15)
    @MedicalDict(dicCode = "ACTION_JKLX")
    private String targetType;

    @Excel(name = "不合规行为名称", width = 15)
    private String behaviorName;
}
