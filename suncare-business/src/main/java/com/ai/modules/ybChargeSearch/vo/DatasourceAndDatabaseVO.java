package com.ai.modules.ybChargeSearch.vo;

import com.ai.modules.system.entity.SysDatabase;
import com.ai.modules.system.entity.SysDatasource;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class DatasourceAndDatabaseVO {
    private SysDatabase sysDatabase;
    private SysDatasource sysDatasource;
}
