package com.ai.modules.drg.mapper;

import java.util.List;
import java.util.Map;

import com.ai.modules.drg.handle.model.TaskCatalogModel;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.drg.entity.MedicalVisitDrg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 病历drg标识
 * @Author: jeecg-boot
 * @Date:   2023-04-07
 * @Version: V1.0
 */
public interface MedicalVisitDrgMapper extends BaseMapper<MedicalVisitDrg> {

    List<Map<String, Object>> queryDiagByVisitid(@Param("schema") String schema, @Param("visitid") String visitid,
                                                 @Param("orgid") String orgid);

    Map<String, Object> querySurgeryByVisitid(@Param("schema") String schema, @Param("surgeryCode") String surgeryCode, @Param("visitid") String visitid,
                                              @Param("orgid") String orgid, @Param("catalog") TaskCatalogModel catalog);

    Integer countRoomSurgeryByVisitid(@Param("schema") String schema, @Param("id") String id, @Param("catalog") TaskCatalogModel catalog);

    Integer getVisitDayAge(@Param("schema") String schema, @Param("visitid") String visitid);
}
