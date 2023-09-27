package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalEquipInstructionItem;
import lombok.Data;


@Data
public class MedicalEquipInstructionItemVO extends MedicalEquipInstructionItem {

    private String code;

    private String name;

    private String actionType;//更新标志(1新增0修改2删除)

}
