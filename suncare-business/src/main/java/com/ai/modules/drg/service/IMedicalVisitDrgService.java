package com.ai.modules.drg.service;

import com.ai.modules.drg.entity.MedicalVisitDrg;
import com.ai.modules.drg.handle.model.TaskCatalogModel;
import com.ai.modules.drg.vo.DrgTargetDtlVo;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 病历drg标识
 * @Author: jeecg-boot
 * @Date:   2023-04-07
 * @Version: V1.0
 */
public interface IMedicalVisitDrgService extends IService<MedicalVisitDrg> {

    List<Map<String, Object>> queryDiagByVisitid(String visitid, String orgid, DatasourceAndDatabaseVO dbVo);

    Map<String, Object> querySurgeryByVisitid(String surgeryCode, String visitid, String orgid, DatasourceAndDatabaseVO dbVo, TaskCatalogModel catalog);

    boolean hasRoomSurgeryByVisitid(String id, DatasourceAndDatabaseVO dbVo, TaskCatalogModel catalog);

    boolean hasRoomSurgery(String adrgSurgeryCode, TaskCatalogModel catalog);

    DrgTargetDtlVo query(String id) throws Exception;
}
