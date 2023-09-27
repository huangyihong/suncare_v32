package com.ai.modules.formal.vo;

import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/7/17 16
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalFormalCaseItemRelaVO extends MedicalFormalCaseItemRela {
    private String caseName;
}
