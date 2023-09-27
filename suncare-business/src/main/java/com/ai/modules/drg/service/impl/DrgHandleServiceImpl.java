package com.ai.modules.drg.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.handle.model.DrgResultModel;
import com.ai.modules.drg.handle.model.DrgRuleModel;
import com.ai.modules.drg.handle.model.TaskBatchModel;
import com.ai.modules.drg.handle.model.TaskCatalogModel;
import com.ai.modules.drg.handle.rule.AbsDrgHandle;
import com.ai.modules.drg.handle.rule.DrgHandle;
import com.ai.modules.drg.mapper.DrgRuleLimitesMapper;
import com.ai.modules.drg.service.IApiDrgTaskService;
import com.ai.modules.drg.service.IDrgHandleService;
import com.ai.modules.drg.service.IDrgRuleLimitesService;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.ai.modules.ybChargeSearch.service.impl.GenHiveQuerySqlTools;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.TextHorizontalOverflow;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : zhangly
 * @date : 2023/4/6 10:30
 */
@Service
@Slf4j
public class DrgHandleServiceImpl implements IDrgHandleService {
    @Autowired
    private IApiDrgTaskService apiDrgTaskService;
    @Autowired
    private IYbChargeSearchTaskService ybChargeSearchTaskService;
    @Autowired
    private IDrgRuleLimitesService ruleLimitesService;

    @Override
    public void execute(String taskId) throws Exception {
        DrgTask task = apiDrgTaskService.findDrgTask(taskId);
        if(task==null) {
            throw new Exception("未找到任务批次");
        }
        if(MedicalConstant.RUN_STATE_WAIT.equals(task.getStatus())
                || MedicalConstant.RUN_STATE_RUNNING.equals(task.getStatus())) {
            throw new Exception("任务批次正在运行中");
        }
        String version = task.getDrgCatalogV();
        DrgCatalog drgCatalog = apiDrgTaskService.findDrgCatalog(task.getDrgCatalogV());
        if(drgCatalog==null) {
            throw new Exception("未找到DRG目录版本("+version+")");
        }
        List<DrgRuleModel> drgRuleList = ruleLimitesService.queryDrgRule(version);
        version = drgCatalog.getMdcCatalogV();
        DrgCatalog mdcCatalog = apiDrgTaskService.findMdcCatalog(version);
        if(mdcCatalog==null) {
            throw new Exception("未找到MDC目录版本("+version+")");
        }
        version = drgCatalog.getAdrgCatalogV();
        DrgCatalog adrgCatalog = apiDrgTaskService.findAdrgCatalog(version);
        if(adrgCatalog==null) {
            throw new Exception("未找到ADRG目录版本("+version+")");
        }
        List<DrgRuleModel> adrgRuleList = ruleLimitesService.queryAdrgRule(version);
        version = drgCatalog.getMdcInfoV();
        DrgCatalog mdcDiagCatalog = apiDrgTaskService.findMdcDiagCatalog(version);
        if(mdcDiagCatalog==null) {
            throw new Exception("未找到MDC主诊表目录版本("+version+")");
        }
        version = drgCatalog.getAdrgListV();
        DrgCatalog adrgListCatalog = apiDrgTaskService.findAdrgListCatalog(version);
        if(adrgListCatalog==null) {
            throw new Exception("未找到ADRG列表目录版本("+version+")");
        }
        version = drgCatalog.getMccInfoV();
        DrgCatalog mccCatalog = apiDrgTaskService.findMccCatalog(version);
        if(mccCatalog==null) {
            throw new Exception("未找到MCC目录版本("+version+")");
        }
        version = drgCatalog.getCcInfoV();
        DrgCatalog ccCatalog = apiDrgTaskService.findCcCatalog(version);
        if(ccCatalog==null) {
            throw new Exception("未找到CC目录版本("+version+")");
        }
        version = drgCatalog.getSurgeryInfoV();
        DrgCatalog surgeryCatalog = apiDrgTaskService.findSurgeryCatalog(version);
        if(surgeryCatalog==null) {
            throw new Exception("未找到手术室手术目录版本("+version+")");
        }
        version = drgCatalog.getExcludeInfoV();
        DrgCatalog excludeCatalog = apiDrgTaskService.findExcludeCatalog(version);
        if(excludeCatalog==null) {
            throw new Exception("未找到排除表目录版本("+version+")");
        }
        TaskCatalogModel catalogModel = new TaskCatalogModel();
        catalogModel.setDrgCatalogId(drgCatalog.getId());
        catalogModel.setMdcCatalogId(mdcCatalog.getId());
        catalogModel.setMdcDiagCatalogId(mdcDiagCatalog.getId());
        catalogModel.setAdrgCatalogId(adrgCatalog.getId());
        catalogModel.setAdrgDiagCatalogId(adrgListCatalog.getId());
        catalogModel.setMccCatalogId(mccCatalog.getId());
        catalogModel.setCcCatalogId(ccCatalog.getId());
        catalogModel.setSurgeryCatalogId(surgeryCatalog.getId());
        catalogModel.setExcludeCatalogId(excludeCatalog.getId());

        TaskBatchModel batchModel = new TaskBatchModel();
        batchModel.setBatchId(task.getBatchId());
        String project = task.getDataSource();
        project = StringUtils.replace(project, "__gp", "");
        batchModel.setProject(project);
        batchModel.setOrgIds(task.getOrgids());
        if(task.getStartdate()!=null) {
            batchModel.setStartVisitdate(DateUtils.formatDate(task.getStartdate(), "yyyy-MM-dd"));
        }
        if(task.getEnddate()!=null) {
            batchModel.setEndVisitdate(DateUtils.formatDate(task.getEnddate(), "yyyy-MM-dd"));
        }
        batchModel.setCurrentSqlSeq(GenHiveQuerySqlTools.getCurrentSqlSeq());
        batchModel.setDrgRuleList(drgRuleList);
        batchModel.setAdrgRuleList(adrgRuleList);

        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(task.getDataSource());
        //更新任务批次状态-待运行
        DrgTask up = new DrgTask();
        up.setStatus(MedicalConstant.RUN_STATE_WAIT);
        up.setErrorMsg("");
        apiDrgTaskService.updateDrgTask(task.getBatchId(), up);
        //执行计算任务
        ThreadUtils.THREAD_DRG_POOL.add(new Runnable() {
            @Override
            public void run() {
                execute(batchModel, catalogModel, dbVO);
            }
        });
    }

    private void execute(TaskBatchModel batchModel, TaskCatalogModel catalogModel, DatasourceAndDatabaseVO dbVO) {
        boolean success = true;
        String error = null;
        DrgResultModel resultModel = null;
        try {
            //更新任务批次状态-运行中
            DrgTask entity = new DrgTask();
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            entity.setRunStartdate(DateUtils.getDate());
            apiDrgTaskService.updateDrgTask(batchModel.getBatchId(), entity);
            AbsDrgHandle handle = new DrgHandle(batchModel, catalogModel, dbVO);
            resultModel = handle.execute();
        } catch (Exception e) {
            success = false;
            error = e.getMessage();
            log.error("", e);
        } finally {
            DrgTask entity = new DrgTask();
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                if(error.length() > 2000) {
                    error = error.substring(0, 2000);
                }
                entity.setErrorMsg(error);
            }
            if(resultModel!=null && resultModel.isSuccess()) {
                //保存入组结果
                entity.setGroupNum(resultModel.getEnrollment());
                entity.setNogroupNum(resultModel.getNoEnrollment());
                int total = resultModel.getEnrollment()+resultModel.getNoEnrollment();
                if(total==0) {
                    entity.setRate(BigDecimal.ZERO);
                } else {
                    BigDecimal enrollment = BigDecimal.valueOf(resultModel.getEnrollment());
                    BigDecimal rate = enrollment.divide(BigDecimal.valueOf(total), 4, BigDecimal.ROUND_HALF_DOWN);
                    entity.setRate(rate);
                }
            }
            entity.setRunEnddate(DateUtils.getDate());
            apiDrgTaskService.updateDrgTask(batchModel.getBatchId(), entity);
        }
    }
}
