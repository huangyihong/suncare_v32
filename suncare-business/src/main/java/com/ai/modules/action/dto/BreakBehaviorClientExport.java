package com.ai.modules.action.dto;

import lombok.Data;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @Auther: zhangpeng
 * @Date: 2020/6/29 16
 * @Description: 不合规病例导出
 */
@Data
public class BreakBehaviorClientExport {
    public BreakBehaviorClientExport(String clientid, String targetName){
        this.clientid = clientid;
        this.targetName = targetName;
        this.casePay = new BigDecimal(0);
        this.caseNum = 0;
        this.ruleIdSet = new HashSet<>();
    }

    @Excel(name = "参保人编号", width = 15)
    private String clientid;

    @Excel(name = "违规主体名称（参保人）", width = 15)
    private String targetName;

    @Excel(name = "违规金额（元）", width = 15)
    private BigDecimal casePay;

    @Excel(name = "违规病例数（个）", width = 15)
    private Integer caseNum;

    @Excel(name = "违规模型数量（个）", width = 15)
    private Integer ruleNum;

    private Set<String> ruleIdSet;

    public void addCasePay(BigDecimal casePay){
        this.casePay = this.casePay.add(casePay).setScale(2, RoundingMode.HALF_UP);;
    }

    public void addCaseNum(Integer caseNum){
        this.caseNum += caseNum;
    }

    public void addRuleIds(Collection<String> ruleIds){
        this.ruleIdSet.addAll(ruleIds);
        this.ruleNum = this.ruleIdSet.size();
    }


    /*  @Excel(name = "不合规行为类型", width = 15)
    @MedicalDict(dicCode = "ACTION_JKLX")
    private String targetType;

    @Excel(name = "不合规行为名称", width = 15)
    private String behaviorName;*/
}
