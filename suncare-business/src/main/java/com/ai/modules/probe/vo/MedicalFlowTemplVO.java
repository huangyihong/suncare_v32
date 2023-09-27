package com.ai.modules.probe.vo;

import com.ai.modules.probe.entity.MedicalFlowTempl;
import com.ai.modules.probe.entity.MedicalFlowTemplRule;
import lombok.Data;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/4/20 10
 * @Description:
 */
@Data
public class MedicalFlowTemplVO extends MedicalFlowTempl {
    private List<MedicalFlowTemplRule> rules;
}
