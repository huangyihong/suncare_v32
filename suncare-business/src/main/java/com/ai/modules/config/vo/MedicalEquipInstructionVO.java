package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalEquipInstruction;
import lombok.Data;


@Data
public class MedicalEquipInstructionVO extends MedicalEquipInstruction {

    private String actionType;//更新标志(1新增0修改2删除)

}
