package com.ai.modules.review.dto;

import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.task.entity.TaskActionFieldCol;
import lombok.Data;

import java.util.*;

/**
 * @Auther: zhangpeng
 * @Date: 2021/3/10 15
 * @Description:
 */

@Data
public class DynamicFieldConfig {

    List<String> fields;
    List<String> titles;
    Map<String, Set<String>> tabFieldMap;

    public DynamicFieldConfig(List<TaskActionFieldCol> colList){
        fields = new ArrayList<>();
        titles = new ArrayList<>();
        tabFieldMap = new HashMap<>();
        for (TaskActionFieldCol col : colList) {
            String tableName = col.getTableName();
            String colName = col.getColName();
            if ("action".equals(tableName)) {
                continue;
            } else if(EngineUtil.MEDICAL_UNREASONABLE_ACTION.equals(tableName)){
                fields.add(colName);
            } else {
                fields.add(tableName + "." + colName);
            }

            titles.add(col.getColCnname());
            if (colName.startsWith("ALIA")) {
                colName = colName + ":" + colName.substring(colName.indexOf("_") + 1);
            }
            Set<String> fieldList = tabFieldMap.computeIfAbsent(tableName, k -> new HashSet<>());
            fieldList.add(colName);
        }
    }

}
