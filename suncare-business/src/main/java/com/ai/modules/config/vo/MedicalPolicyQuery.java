package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalPolicy;

import lombok.Data;

@Data
public class MedicalPolicyQuery extends MedicalPolicy {
  
    private String fileContent;//全文文本搜索
    
    private String importActionType; //导入数据时的更新标志
}
