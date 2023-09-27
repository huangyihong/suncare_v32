package com.ai.modules.drg.handle.rule;

import com.ai.modules.drg.handle.model.DrgResultModel;
import com.ai.modules.drg.handle.model.TaskBatchModel;
import com.ai.modules.drg.handle.model.TaskCatalogModel;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import org.apache.commons.lang3.RegExUtils;

/**
 * @author : zhangly
 * @date : 2023/3/31 14:23
 */
public abstract class AbsDrgHandle {

    protected TaskBatchModel batchModel;
    protected TaskCatalogModel catalogModel;
    protected DatasourceAndDatabaseVO dbVO;

    public AbsDrgHandle(TaskBatchModel batchModel, TaskCatalogModel catalogModel, DatasourceAndDatabaseVO dbVO) {
        this.batchModel = batchModel;
        this.catalogModel = catalogModel;
        this.dbVO = dbVO;
    }

    public abstract DrgResultModel execute() throws Exception;

    protected String removePlaceholder(String sql) {
        //剔除未被替换的行${}
        String regex = ".*\\$\\{.*\\}.*\n";
        sql = RegExUtils.replaceAll(sql, regex, "");
        //剔除--.*--格式的注释
        regex = "--.*--";
        sql = RegExUtils.replaceAll(sql, regex, "");
        //剔除空行
        regex = "^[ \\t]*\\n";
        sql = RegExUtils.replaceAll(sql, regex, "");
        return sql;
    }
}
