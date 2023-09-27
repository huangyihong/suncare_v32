package com.ai.modules.ybChargeSearch.handle.rule.model;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import lombok.Data;
import org.jeecg.common.util.superSearch.QueryRuleEnum;

/**
 * @author : zhangly
 * @date : 2023/2/16 13:06
 */
@Data
public class RuleWhere {
    private String tableName;
    private String column;
    private QueryRuleEnum compare = QueryRuleEnum.LIKE;
    private String value;
    private String valueType = DcConstants.TYPE_STRING;

    public RuleWhere(String tableName, String column, String value) {
        this.tableName = tableName;
        this.column = column;
        this.value = value;
    }

    public RuleWhere(String tableName, String column, String value, QueryRuleEnum compare) {
        this(tableName, column, value);
        this.compare = compare;
    }

    public RuleWhere(String tableName, String column, String value, QueryRuleEnum compare, String valueType) {
        this(tableName, column, value, compare);
        this.valueType = valueType;
    }
}
