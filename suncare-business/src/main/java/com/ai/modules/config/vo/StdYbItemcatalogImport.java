package com.ai.modules.config.vo;

import com.ai.modules.config.entity.StdYbItemcatalog;
import lombok.Data;

@Data
public class StdYbItemcatalogImport extends StdYbItemcatalog {
    private String startEndDateStr;//使用时间
    private String importActionType;//更新标志
}
