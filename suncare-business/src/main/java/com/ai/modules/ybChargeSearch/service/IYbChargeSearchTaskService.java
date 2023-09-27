package com.ai.modules.ybChargeSearch.service;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 收费明细查询任务表
 * @Author: jeecg-boot
 * @Date:   2022-10-09
 * @Version: V1.0
 */
public interface IYbChargeSearchTaskService extends IService<YbChargeSearchTask> {

    public List<Map<String,Object>> getOrgList(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) throws Exception;

    public List<Map<String,Object>> getDeptList(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) throws Exception;

    public List<Map<String,Object>> getTagList(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) throws Exception;

    public List<Map<String, Object>> getDataminingOrgSum(YbChargeSearchTask bean, DatasourceAndDatabaseVO dbVO) throws Exception;

    public List<Map<String, Object>> genTaskTypeResultData(YbChargeSearchTask bean, DatasourceAndDatabaseVO dbVO) throws Exception;

    public boolean deleteYbChargeSearchTask(String id);

    public Map<String,Object> run(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO);

    public String getSqlByTaskBean(YbChargeSearchTask bean, YbChargeQueryDatabase queryDatabase) throws Exception;

    public List<YbChargeQuerySql> getSqlListByTaskBean(YbChargeSearchTask bean,YbChargeQueryDatabase queryDatabase,String sheetName) throws Exception;

    public YbChargeSearchTask deleteYbChargeSearchTaskByRunAgain(String id);

    public DatasourceAndDatabaseVO getDatasourceAndDatabase(String dataSource);

    public void clearCacheDatasourceAndDatabase(String dataSource);

    IPage<Map<String,Object>> getUseCountPage(Page<YbChargeSearchTask> page, YbChargeSearchTaskCountVo chargeSearchTaskCountVo, String sql);

    public YbChargeQueryDatabase getQueryDatabase(DatasourceAndDatabaseVO dbVO) throws Exception;

    IPage<Map<String, Object>> getSearchTaskCount(YbChargeSearchTaskCountVo chargeSearchTaskCountVo, Page<YbChargeSearchTask> page);

    List<Map<String,Object>> getSearchTaskCountList(YbChargeSearchTaskCountVo chargeSearchTaskCountVo);

    IPage<Map<String, Object>> getSearchTaskFunCount(YbChargeSearchTaskFunCountVo chargeSearchTaskFunCountVo, Page<YbChargeSearchTask> page);

    List<Map<String,Object>> getSearchTaskFunCountList(YbChargeSearchTaskFunCountVo chargeSearchTaskFunCountVo);

    IPage<Map<String, Object>> querySearchResult(YbChargeSearchTask bean, DatasourceAndDatabaseVO dbVO, Page<Map<String, Object>> page)throws Exception;

    Page<OdsCheckorgListVo> getOrgPageList(OdsCheckorgListVo bean, DatasourceAndDatabaseVO dbVO, Page<OdsCheckorgListVo> page) throws Exception;

    void createYearSql(DatasourceAndDatabaseVO dbVO) throws Exception;


}
