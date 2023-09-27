package com.ai.modules.drg.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgCatalogDetail;
import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.entity.MedicalVisitDrg;
import com.ai.modules.drg.handle.model.TaskCatalogModel;
import com.ai.modules.drg.handle.rule.AbsDrgTargetHandle;
import com.ai.modules.drg.handle.rule.DrgTargetHandle;
import com.ai.modules.drg.mapper.MedicalVisitDrgMapper;
import com.ai.modules.drg.service.IApiDrgTaskService;
import com.ai.modules.drg.service.IDrgCatalogDetailService;
import com.ai.modules.drg.service.IMedicalVisitDrgService;
import com.ai.modules.drg.vo.DrgTargetDtlVo;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * @Description: 病历drg标识
 * @Author: jeecg-boot
 * @Date:   2023-04-07
 * @Version: V1.0
 */
@Service
@DS("greenplum")
public class MedicalVisitDrgServiceImpl extends ServiceImpl<MedicalVisitDrgMapper, MedicalVisitDrg> implements IMedicalVisitDrgService {

    @Autowired
    private IYbChargeSearchTaskService ybChargeSearchTaskService;
    @Autowired
    private IApiDrgTaskService apiDrgTaskService;
    @Autowired
    private IDrgCatalogDetailService drgCatalogDetailService;

    @Override
    public List<Map<String, Object>> queryDiagByVisitid(String visitid, String orgid, DatasourceAndDatabaseVO dbVo) {
        String schema = this.getSchema(dbVo);
        return baseMapper.queryDiagByVisitid(schema, visitid, orgid);
    }

    @Override
    public Map<String, Object> querySurgeryByVisitid(String surgeryCode, String visitid, String orgid, DatasourceAndDatabaseVO dbVo, TaskCatalogModel catalog) {
        String schema = this.getSchema(dbVo);
        return baseMapper.querySurgeryByVisitid(schema, surgeryCode, visitid, orgid, catalog);
    }

    @Override
    public boolean hasRoomSurgeryByVisitid(String id, DatasourceAndDatabaseVO dbVo, TaskCatalogModel catalog) {
        String schema = this.getSchema(dbVo);
        Integer count = baseMapper.countRoomSurgeryByVisitid(schema, id, catalog);
        return count!=null && count>0;
    }

    @Override
    public boolean hasRoomSurgery(String adrgSureryCode, TaskCatalogModel catalog) {
        String[] array = adrgSureryCode.split(",");
        QueryWrapper<DrgCatalogDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("catalog_type", "SURGERY_INFO_V");
        wrapper.eq("catalog_id", catalog.getSurgeryCatalogId());
        wrapper.in("code", array);
        Integer count = drgCatalogDetailService.count(wrapper);
        return count!=null && count>0;
    }

    private String getSchema(DatasourceAndDatabaseVO dbVo) {
        String url = dbVo.getSysDatabase().getUrl();
        String paramStr = StringUtils.substringAfter(url, "?");
        String[] array = StringUtils.split(paramStr, "&");
        String schema = null;
        for(String value : array) {
            if(value.startsWith("currentSchema=")) {
                schema = StringUtils.substringAfter(value, "=");
                break;
            }
        }
        if(StringUtils.isBlank(schema)){
            schema = dbVo.getSysDatabase().getDbname();
        }
        return schema;
    }

    @Override
    public DrgTargetDtlVo query(String id) throws Exception {
        MedicalVisitDrg visitDrg = this.getById(id);

        DrgTask task = apiDrgTaskService.findDrgTaskByBatch(visitDrg.getBatchId());
        if(task==null) {
            throw new Exception("未找到任务批次");
        }
        String version = task.getDrgCatalogV();
        DrgCatalog drgCatalog = apiDrgTaskService.findDrgCatalog(task.getDrgCatalogV());
        if(drgCatalog==null) {
            throw new Exception("未找到DRG目录版本("+version+")");
        }
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

        AbsDrgTargetHandle handle = new DrgTargetHandle(visitDrg);
        DrgTargetDtlVo vo = handle.parse();
        if(vo==null) {
            vo = new DrgTargetDtlVo();
        }
        vo.setVisitDrg(visitDrg);

        DatasourceAndDatabaseVO dbVO = ybChargeSearchTaskService.getDatasourceAndDatabase(task.getDataSource());
        boolean isBaby = this.isBaby(visitDrg.getVisitid(), dbVO);
        vo.setBabyFlag(isBaby ? DrgCatalogConstants.YES : DrgCatalogConstants.NO);
        if(StringUtils.isNotBlank(visitDrg.getDrgSurgeryCode())) {
            Map<String, Object> map = this.querySurgeryByVisitid(visitDrg.getDrgSurgeryCode(), visitDrg.getVisitid(),
                    visitDrg.getOrgid(), dbVO, catalogModel);
            if(map!=null && map.size()>0) {
                String surgeryCode = map.get("operationcode").toString();
                String surgeryName = map.get("operationname").toString();
                vo.setSurgeryCodes(surgeryCode);
                vo.setSurgeryNames(surgeryName);
                if(map.get("operationdate")!=null) {
                    String operationdate = map.get("operationdate").toString();
                    vo.setSurgeryTimes(operationdate);
                }
                //是否手术室手术
                boolean hasRoomSurgery = this.hasRoomSurgery(visitDrg.getDrgSurgeryCode(), catalogModel);
                vo.setRoomSurgeryFlag(hasRoomSurgery ? DrgCatalogConstants.YES : DrgCatalogConstants.NO);
            }
        }
        return vo;
    }

    private boolean isBaby(String visitid, DatasourceAndDatabaseVO dbVo) {
        String schema = this.getSchema(dbVo);
        Integer count = baseMapper.getVisitDayAge(schema, visitid);
        return count!=null && count<29;
    }
}
