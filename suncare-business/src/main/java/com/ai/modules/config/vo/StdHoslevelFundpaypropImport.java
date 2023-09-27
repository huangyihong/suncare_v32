package com.ai.modules.config.vo;

import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import lombok.Data;

@Data
public class StdHoslevelFundpaypropImport extends StdHoslevelFundpayprop {
    private String startEndDateStr;//使用时间
    private String importActionType;//更新标志
}
