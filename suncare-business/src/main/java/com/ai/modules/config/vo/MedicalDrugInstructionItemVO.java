package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalDrugInstructionItem;
import lombok.Data;


@Data
public class MedicalDrugInstructionItemVO extends MedicalDrugInstructionItem {

    private String code;

    private String name;

    private String actionType;//更新标志(1新增0修改2删除)

}
