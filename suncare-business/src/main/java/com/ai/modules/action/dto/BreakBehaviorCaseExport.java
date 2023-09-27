package com.ai.modules.action.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/6/29 16
 * @Description: 不合规病例导出
 */
@Data
public class BreakBehaviorCaseExport {

    @Excel(name = "就诊ID", width = 15)
    private String visitid;

    @Excel(name = "违规主体名称", width = 15)
    private String targetName;

    @Excel(name = "不合规行为类型", width = 15)
    @MedicalDict(dicCode = "ACTION_JKLX")
    private String targetType;

    @Excel(name = "不合规行为名称", width = 15)
    private String behaviorName;

    @Excel(name = "医疗机构名称", width = 15)
    private String orgname;

    @Excel(name = "就诊类型", width = 15)
    private String visittype;

    @Excel(name = "参保类型", width = 15)
    private String insurancetype;

    @Excel(name = "就诊科室", width = 15)
    private String deptname;

    @Excel(name = "医生姓名", width = 15)
    private String doctorname;

    @Excel(name = "病人姓名", width = 15)
    private String clientname;

    @Excel(name = "就诊金额", width = 15)
    private Double totalfee;

    @Excel(name = "就诊日期", width = 15)
    private String visitdate;

    private List<String> reviewCaseIds;

    // 违规主体字段
    private String clientid;
//    private String clientname;
    private String doctorid;
//    private String doctorname;
    private String orgid;
//    private String orgname;
}
