package com.ai.modules.ybChargeSearch.service;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;

import java.util.List;
import java.util.Map;

public interface IYbChargeTagLabelService  {
    public List<Map<String,Object>> checkImportExcel(List<List<String>> list, String taskType,boolean checkFlag) throws Exception;

    public int getExistCount(List<Map<String,Object>> dataList, DatasourceAndDatabaseVO dbVO,String taskType ) throws Exception;

    public void deleteExistData(List<Map<String,Object>> dataList, DatasourceAndDatabaseVO dbVO,String taskType ) throws Exception;

    public void insertImportData(List<Map<String,Object>> dataList, DatasourceAndDatabaseVO dbVO,String taskType) throws Exception;

    public void deleteAndInsertImportData(List<Map<String,Object>> dataList, DatasourceAndDatabaseVO dbVO,String taskType) throws Exception;

    public int dwbVisitTagCountByTagname(DatasourceAndDatabaseVO dbVO, YbChargeSearchTask ybChargeSearchTask, String tagName, String tagId)  throws Exception;

    public void deleteExistDwbVisitTagAndImport(List<Map<String,Object>> dataList,DatasourceAndDatabaseVO dbVO, YbChargeSearchTask ybChargeSearchTask, String tagName, String tagId) throws Exception;
    public void dwbVisitTagImport(List<Map<String,Object>> dataList,DatasourceAndDatabaseVO dbVO, YbChargeSearchTask ybChargeSearchTask, String tagName, String tagId) throws Exception;
    public List<Map<String, Object>> getTaskFileData(List<List<String>> list, String taskType) throws Exception;
}
