package com.ai.modules.drg.mapper;


import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.vo.SrcYbClientVo;
import com.ai.modules.drg.vo.SrcYbSettlementVo;
import com.ai.modules.drg.vo.VisitDrgVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;
import java.util.Map;


public interface VisitDrgMapper {
    String visitDrgListSql = "select * from (\n" +
            "        SELECT t1.id,t1.visitid,t2.case_id,t2.orgname,t2.clientname,t2.clientid,t2.insurancecard_no,t2.sex,substr(cast(t2.birthday as VARCHAR),1,10) as birthday," +
            "        '' as insurancetype,substr(cast(t2.admitdate as VARCHAR),1,10) as admitdate,substr(cast(t2.leavedate as VARCHAR),1,10) as leavedate,t2.diag_conclusion,t2.totalfee,\n" +
            "        t1.drg,t1.drg_name,t1.adrg,t1.adrg_name,t1.mdc,t1.mdc_name,t1.mdc_diag_code,t1.mdc_diag_name,t1.drg_surgery_code,t1.drg_surgery_name,t1.drg_diag_code,t1.drg_diag_name\n" +
            "        FROM medical_gbdp.medical_visit_drg t1 left join ${schema}.src_his_zy_master_info t2\n" +
            "        on t1.visitid = t2.visitid and t1.orgid = t2.orgid\n" +
            "        where 1=1 \n" +
            "        and t1.project= #{project} and t1.batch_id=#{batchId} and t1.drg is not null and  t1.drg!='QY'\n" +
            "       ) t ${ew.customSqlSegment}";

    String visitNoDrgList = "select * from (\n" +
            "       select COALESCE(t1.id,concat(t2.visitid,'#',t2.orgid)) as id,t2.visitid,t2.case_id,t2.orgname,t2.clientname,t2.clientid,t2.insurancecard_no,t2.sex,substr(cast(t2.birthday as VARCHAR),1,10) as birthday," +
            "        '' as insurancetype,substr(cast(t2.admitdate as VARCHAR),1,10) as admitdate,substr(cast(t2.leavedate as VARCHAR),1,10) as leavedate,t2.diag_conclusion,t2.totalfee,\n" +
            "         t1.drg,t1.drg_name,t1.adrg,t1.adrg_name,t1.mdc,t1.mdc_name,t1.mdc_diag_code,t1.mdc_diag_name,t1.drg_surgery_code,t1.drg_surgery_name,t1.drg_diag_code,t1.drg_diag_name\n" +
            "        from  ${schema}.src_his_zy_master_info t2\n" +
            "         left join medical_gbdp.medical_visit_drg t1\n" +
            "         on t1.visitid = t2.visitid and t1.orgid = t2.orgid\n" +
            "         and t1.project= #{project} and t1.batch_id=#{batchId}\n" +
            "        where 1=1 and (t1.drg is  null or  t1.drg='QY')\n" +
            "        <if test=\"drgTask.orgids != '' and drgTask.orgids != null \">\n" +
            "            and t2.orgid in (${drgTask.orgids})\n" +
            "        </if>\n" +
            "        <if test=\"drgTask.startdate != null \">\n" +
            "            and t2.admitdate &gt;= #{drgTask.startdate}\n" +
            "        </if>\n" +
            "        <if test=\"drgTask.enddate != null \">\n" +
            "            and t2.admitdate &lt;= #{drgTask.enddate}\n" +
            "        </if>\n" +
            "       ) t ${ew.customSqlSegment}";

    @Select({"<script>"+visitDrgListSql+"</script>"})
    @ResultType(VisitDrgVo.class)
    IPage<VisitDrgVo> visitDrgListPage(Page<VisitDrgVo> page, @Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,
                                   @Param("project") String project,@Param("schema") String schema,
                                   @Param("batchId") String batchId);

    @Select({"<script>"+visitNoDrgList+"</script>"})
    @ResultType(VisitDrgVo.class)
    IPage<VisitDrgVo> visitNoDrgListPage(Page<VisitDrgVo> page, @Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,
                                     @Param("project") String project,@Param("schema") String schema,
                                     @Param("batchId") String batchId,@Param("drgTask") DrgTask drgTask);

    @Select({"<script>"+visitDrgListSql+"</script>"})
    @ResultType(VisitDrgVo.class)
    List<VisitDrgVo> visitDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,
                                  @Param("project") String project, @Param("schema") String schema,
                                  @Param("batchId") String batchId);

    @Select({"<script>"+visitNoDrgList+"</script>"})
    @ResultType(VisitDrgVo.class)
    List<VisitDrgVo> visitNoDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,
                                         @Param("project") String project,@Param("schema") String schema,
                                         @Param("batchId") String batchId,@Param("drgTask") DrgTask drgTask);

    @Select({"<script>"+visitDrgListSql+"</script>"})
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000,statementType = StatementType.PREPARED)
    @ResultType(Map.class)
    void streamQueryVisitDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,
                                 @Param("project") String project, @Param("schema") String schema,
                                 @Param("batchId") String batchId,
                                 ResultHandler<Map<String,Object>> handler);

    @Select({"<script>"+visitNoDrgList+"</script>"})
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000,statementType = StatementType.PREPARED)
    @ResultType(Map.class)
    void streamQueryVisitNoDrgList(@Param(Constants.WRAPPER) Wrapper<VisitDrgVo> wrapper,
                                   @Param("project") String project,@Param("schema") String schema,
                                   @Param("batchId") String batchId,@Param("drgTask") DrgTask drgTask,
                                   ResultHandler<Map<String,Object>> handler);

    SrcYbClientVo getSrcYbClientById(@Param("schema") String schema,@Param("clientid") String clientid);

    SrcYbSettlementVo getSrcYbSettlementById(@Param("schema") String schema, @Param("clientid") String clientid, @Param("visitid") String visitid);
}
