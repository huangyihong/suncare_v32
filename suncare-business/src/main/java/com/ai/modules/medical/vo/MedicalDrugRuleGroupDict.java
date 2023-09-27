package com.ai.modules.medical.vo;

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
public class MedicalDrugRuleGroupDict {
    private String dictType;
    private String remark;
    private String kind;
}
