package com.ai.modules.review.mapper;


import com.ai.modules.review.dto.ReviewInfoDTO;
import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface MedicalUnreasonableActionMapper extends BaseMapper<MedicalUnreasonableAction> {
    IPage<MedicalUnreasonableAction> selectPageVO(Page<MedicalUnreasonableAction> page, @Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                                                    @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("fields") String fields,@Param("orderbySql") String orderbySql);

    int selectCount(@Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                     @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("fields") String fields);
    IPage<Map<String,Object>> selectMapPageVO(Page<Map<String,Object>> page, @Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                                              @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("fields") String fields,@Param("orderbySql") String orderbySql,@Param("linkFields") String linkFields);
    List<Map<String,Object>> selectMapVO(@Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                                              @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("fields") String fields,@Param("orderbySql") String orderbySql,@Param("linkFields") String linkFields);
    List<Map<String,Object>> facetFields(@Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                                         @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("selectFields") String selectFields,@Param("factFields") String factFields);
    IPage<Map<String,Object>> facetFieldsPage(Page<Map<String,Object>> page,@Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                                         @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("selectFields") String selectFields,@Param("factFields") String factFields,
                                         @Param("groupByFields") String groupByFields,@Param("orderbySql") String orderbySql);
    int facetFieldsCount(@Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                         @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("selectFields") String selectFields,@Param("factFields") String factFields,
                         @Param("groupByFields") String groupByFields);
    List<Map<String,Object>> facetActionData(@Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                                         @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("selectFields") String selectFields);


    void updateReviewStatus(@Param(Constants.WRAPPER) Wrapper<MedicalUnreasonableAction> wrapper,
                          @Param("joinSql") String joinSql, @Param("whereSql") String whereSql, @Param("fields") String fields,@Param("reviewObj") ReviewInfoDTO reviewObj);
}
