package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalDrugInstruction;
import lombok.Data;


@Data
public class MedicalDrugInstructionVO extends MedicalDrugInstruction {

    private String actionType;//更新标志(1新增0修改2删除)

}
