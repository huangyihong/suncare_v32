package com.ai.modules.ybChargeSearch.vo;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医保明细查询返回对象定义
 */
@Data
public class YbChargeQuerySql implements java.io.Serializable {

    private String querySql;

    private String splitColumnName; //对结果进行拆分的字段名称,如果为空，则不需要拆分

    private String sheetName; //sheet页名称，如果结果拆分字段名称不为空，则表示sheet页名称的前缀

    private YbChargeSearchTask searchTaskBean =null  ;

    private Boolean isMonth = false;

    private String taskType ;//当前sheet结果类型跟任务类型不一致的情况
}
