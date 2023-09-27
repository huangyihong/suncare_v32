/**
 * EngineServiceImpl.java	  V1.0   2019年11月29日 上午11:06:14
 * <p>
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package com.ai.modules.drg.service.impl;

import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.mapper.VisitDrgMapper;
import com.ai.modules.drg.service.IVisitDrgService;
import com.ai.modules.drg.vo.SrcYbClientVo;
import com.ai.modules.drg.vo.SrcYbSettlementVo;
import com.ai.modules.drg.vo.VisitDrgVo;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@DS("greenplum")
public class VisitDrgServiceImpl implements IVisitDrgService {


    @Autowired
    private VisitDrgMapper visitDrgMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public IPage<VisitDrgVo> visitDrgListPage(Page<VisitDrgVo> page, @Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,String project,String schema,String batchId) {
        return this.visitDrgMapper.visitDrgListPage(page,wrapper,project,schema,batchId);
    }

    @Override
    public IPage<VisitDrgVo> visitNoDrgListPage(Page<VisitDrgVo> page, @Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper, String project, String schema, String batchId, DrgTask drgTask) {
        return this.visitDrgMapper.visitNoDrgListPage(page,wrapper,project,schema,batchId,drgTask);
    }

    @Override
    public List<VisitDrgVo> visitDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper, String project, String schema, String batchId) {
        return this.visitDrgMapper.visitDrgList(wrapper,project,schema,batchId);
    }

    @Override
    public List<VisitDrgVo> visitNoDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper, String project, String schema, String batchId, DrgTask drgTask) {
        return this.visitDrgMapper.visitNoDrgList(wrapper,project,schema,batchId,drgTask);
    }

    @Override
    public void deleteVisitDrgByBatchId(String batchId) {
        String sql = "delete from medical_gbdp.medical_visit_drg where batch_id='%s' ";
        sql = String.format(sql, batchId);
        jdbcTemplate.execute(sql);
    }

    @Override
    public SrcYbClientVo getSrcYbClientById(String schema, String clientid) {
        return this.visitDrgMapper.getSrcYbClientById(schema,clientid);
    }

    @Override
    public SrcYbSettlementVo getSrcYbSettlementById(String schema, String clientid, String visitid) {
        return this.visitDrgMapper.getSrcYbSettlementById(schema,clientid,visitid);
    }

    @Override
    public void streamQueryVisitDrgList(Wrapper<VisitDrgVo> wrapper, String project, String schema, String batchId, ResultHandler<Map<String,Object>> handler) {
        this.visitDrgMapper.streamQueryVisitDrgList(wrapper,project,schema,batchId,handler);
    }

    @Override
    public void streamQueryVisitNoDrgList(Wrapper<VisitDrgVo> wrapper, String project, String schema, String batchId, DrgTask drgTask, ResultHandler<Map<String,Object>> handler) {
        this.visitDrgMapper.streamQueryVisitNoDrgList(wrapper,project,schema,batchId,drgTask,handler);
    }


}
