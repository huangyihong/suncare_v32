package com.ai.modules.drg.vo;

import com.ai.modules.drg.entity.DrgCatalog;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = false)
@Data
public class DrgCatalogVo extends DrgCatalog {
    private String mdcCatalogVText;
    private String adrgCatalogVText;
    private String mdcInfoVText;
    private String adrgListVText;
    private String mccInfoVText;
    private String ccInfoVText;
    private String excludeInfoVText;
    private String surgeryInfoVText;

}
