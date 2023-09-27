package com.ai.modules.ybChargeSearch.vo;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import lombok.Data;

/**
 * 任务查询数仓参数
 */
@Data
public class YbChargeQueryDatabase implements java.io.Serializable {

    private String dataStoreVersion;//数仓版本

    private String dataStoreProject;//数仓项目

    private String dbtype;//数据库类型
}
