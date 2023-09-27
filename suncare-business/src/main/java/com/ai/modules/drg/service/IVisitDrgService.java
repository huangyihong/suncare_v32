package com.ai.modules.drg.service;

import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.vo.SrcYbClientVo;
import com.ai.modules.drg.vo.SrcYbSettlementVo;
import com.ai.modules.drg.vo.VisitDrgVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;
import java.util.Map;

public interface IVisitDrgService {

    /**
     * 入组病历数据列表
     * @param page
     * @param wrapper
     * @return
     */
    public IPage<VisitDrgVo> visitDrgListPage(Page<VisitDrgVo> page, @Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,String project,String schema,String batchId);
    /**
     * 未组病历数据列表
     * @param page
     * @param wrapper
     * @return
     */
    public IPage<VisitDrgVo> visitNoDrgListPage(Page<VisitDrgVo> page, @Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper, String project, String schema, String batchId, DrgTask drgTask);

    public List<VisitDrgVo> visitDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper, String project, String schema, String batchId);

    public List<VisitDrgVo> visitNoDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper, String project, String schema, String batchId, DrgTask drgTask);

    public void deleteVisitDrgByBatchId(String batchId);

    public SrcYbClientVo getSrcYbClientById(String schema,String clientid);

    public SrcYbSettlementVo getSrcYbSettlementById(String schema, String clientid,String visitid);

    public void streamQueryVisitDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,String project,String schema,String batchId,ResultHandler<Map<String,Object>> handler);

    public void streamQueryVisitNoDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,String project,String schema,String batchId, DrgTask drgTask,ResultHandler<Map<String,Object>> handler);
}
