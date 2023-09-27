package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalYbDrug;
import lombok.Data;


@Data
public class MedicalYbDrugVO extends MedicalYbDrug {

    private String actionType;//更新标志

}
