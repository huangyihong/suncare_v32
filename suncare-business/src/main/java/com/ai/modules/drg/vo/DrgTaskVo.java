package com.ai.modules.drg.vo;

import com.ai.modules.drg.entity.DrgTask;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = false)
@Data
public class DrgTaskVo extends DrgTask {

    private String drgCatalogVText;

    private String isRun;

}
