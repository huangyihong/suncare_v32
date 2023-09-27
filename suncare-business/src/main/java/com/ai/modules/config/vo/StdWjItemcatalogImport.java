package com.ai.modules.config.vo;

import com.ai.modules.config.entity.StdWjItemcatalog;
import lombok.Data;

@Data
public class StdWjItemcatalogImport extends StdWjItemcatalog {
    private String startEndDateStr;//使用时间
    private String importActionType;//更新标志
}
