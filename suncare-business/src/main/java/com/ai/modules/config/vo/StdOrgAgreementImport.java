package com.ai.modules.config.vo;

import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.ai.modules.config.entity.StdOrgAgreement;
import lombok.Data;

@Data
public class StdOrgAgreementImport extends StdOrgAgreement {
    private String startEndDateStr;//使用时间
    private String importActionType;//更新标志
}
