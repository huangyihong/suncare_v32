package com.ai.modules.config.vo;

import com.ai.modules.config.entity.StdWjItemcatalog;
import com.ai.modules.config.entity.StdYbDrugcatalog;
import lombok.Data;

@Data
public class StdYbDrugcatalogImport extends StdYbDrugcatalog {
    private String startEndDateStr;//使用时间
    private String importActionType;//更新标志
}
