package com.ai.modules.drg.handle.rule;

import com.ai.modules.drg.entity.MedicalVisitDrg;
import com.ai.modules.drg.vo.DrgTargetDtlVo;

/**
 * @author : zhangly
 * @date : 2023/4/10 11:03
 */
public abstract class AbsDrgTargetHandle {
    protected MedicalVisitDrg visitDrg;

    public AbsDrgTargetHandle(MedicalVisitDrg visitDrg) {
        this.visitDrg = visitDrg;
    }

    public abstract DrgTargetDtlVo parse();
}
