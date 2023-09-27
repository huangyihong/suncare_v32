package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalDrugRuleGroupDel;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2019/11/29 19
 * @Description:
 */
@Data
public class MedicalDrugRuleGroupDelVO extends MedicalDrugRuleGroupDel {
    private String groupType;
    private String kind;
}
