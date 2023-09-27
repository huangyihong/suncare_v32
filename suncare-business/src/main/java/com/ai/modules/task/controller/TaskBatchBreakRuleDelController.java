package com.ai.modules.task.controller;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.utils.ThreadUtils;
import com.ai.modules.his.service.IHisMedicalFormalFlowRuleService;
import com.ai.modules.task.vo.TaskBatchBreakRuleDelVO;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.service.ITaskBatchBreakRuleDelService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description: 违规模型详情
 * @Author: jeecg-boot
 * @Date: 2020-01-17
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "违规模型详情")
@RestController
@RequestMapping("/task/taskBatchBreakRuleDel")
public class TaskBatchBreakRuleDelController extends JeecgController<TaskBatchBreakRuleDel, ITaskBatchBreakRuleDelService> {

    @Autowired
    private ITaskBatchBreakRuleDelService taskBatchBreakRuleDelService;

    @Autowired
    private IHisMedicalFormalFlowRuleService hisMedicalFormalFlowRuleService;


    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 分页列表查询
     *
     * @param taskBatchBreakRuleDel
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "违规模型详情-分页列表查询")
    @ApiOperation(value = "违规模型详情-分页列表查询", notes = "违规模型详情-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(TaskBatchBreakRuleDel taskBatchBreakRuleDel,
                                   @RequestParam(name = "batchId") String batchId,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = QueryGenerator.initQueryWrapper(taskBatchBreakRuleDel, req.getParameterMap());
        Page<TaskBatchBreakRuleDel> page = new Page<TaskBatchBreakRuleDel>(pageNo, pageSize);
        IPage<TaskBatchBreakRuleDelVO> pageList = taskBatchBreakRuleDelService.pageVo(page, queryWrapper, batchId);
        return Result.ok(pageList);
    }

    @AutoLog(value = "违规模型详情-列表查询")
    @ApiOperation(value = "违规模型详情-列表查询", notes = "违规模型详情-列表查询")
    @GetMapping(value = "/queryList")
    public Result<?> queryList(TaskBatchBreakRuleDel taskBatchBreakRuleDel,
                                   HttpServletRequest req) {
        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = QueryGenerator.initQueryWrapper(taskBatchBreakRuleDel, req.getParameterMap());
        List<TaskBatchBreakRuleDel> list = taskBatchBreakRuleDelService.list(queryWrapper);
        return Result.ok(list);
    }

    /*@AutoLog(value = "违规模型详情-分页列表查询")
    @ApiOperation(value = "违规模型详情-分页列表查询", notes = "违规模型详情-分页列表查询")
    @GetMapping(value = "/ruleFieldLoseRate")
    public Result<?> queryPageList(@RequestParam(name = "ruleIds") String ruleIds,
                                   HttpServletRequest req) {
        List<Map<String, Object>> ruleFieldList = jdbcTemplate.queryForList("SELECT RULE_ID,TABLE_NAME,COL_NAME" +
                " FROM his_medical_formal_flow_rule " +
                " WHERE RULE_ID IN (\"" + ruleIds.replaceAll(",","\",\"")  + "\") " +
                " GROUP BY RULE_ID, TABLE_NAME,COL_NAME" +
                " ORDER BY TABLE_NAME,COL_NAME");
        Map<String, List<String>> fieldRuleMap = new HashMap<>();
        for(Map<String, Object> map: ruleFieldList){
            String tableName = map.get("TABLE_NAME").toString();
            String colName = map.get("COL_NAME").toString();
            List<String> ruleIdList = fieldRuleMap.computeIfAbsent(tableName + "." + colName, k -> new ArrayList<>());
        }
        return Result.ok(pageList);
    }*/

    /**
     * 根据批次获取简洁列表
     *
     * @return
     */
    @AutoLog(value = "违规模型详情-根据批次获取简洁列表")
    @ApiOperation(value = "违规模型详情-根据批次获取简洁列表", notes = "违规模型详情-根据批次获取简洁列表")
    @GetMapping(value = "/querySimpleByBatchId")
    public Result<?> querySimpleByBatchId(@RequestParam(name = "batchId") String batchId,
                                          @RequestParam(name = "ruleType") String ruleType,
                                          String status) {
        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("RULE_TYPE",ruleType)
                .eq("BATCH_ID",batchId);
        if(StringUtils.isNotBlank(status)){
            queryWrapper.eq("REVIEW_STATUS", status);
        }
        queryWrapper.select("BUSI_ID","BUSI_NAME","CASE_ID","CASE_NAME","REVIEW_ACOUNT");
        queryWrapper.orderByAsc("BUSI_NAME","CASE_NAME");
        List<TaskBatchBreakRuleDel> list = taskBatchBreakRuleDelService.list(queryWrapper);
        return Result.ok(list);
    }

    /**
     * 添加
     *
     * @param taskBatchBreakRuleDel
     * @return
     */
    @AutoLog(value = "违规模型详情-添加")
    @ApiOperation(value = "违规模型详情-添加", notes = "违规模型详情-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody TaskBatchBreakRuleDel taskBatchBreakRuleDel) {
        taskBatchBreakRuleDelService.save(taskBatchBreakRuleDel);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param taskBatchBreakRuleDel
     * @return
     */
    @AutoLog(value = "违规模型详情-编辑")
    @ApiOperation(value = "违规模型详情-编辑", notes = "违规模型详情-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody TaskBatchBreakRuleDel taskBatchBreakRuleDel) {
        taskBatchBreakRuleDelService.updateById(taskBatchBreakRuleDel);
        return Result.ok("编辑成功!");
    }

    @AutoLog(value = "违规模型详情-更新")
    @ApiOperation(value = "违规模型详情-更新", notes = "违规模型详情-更新")
    @PutMapping(value = "/updateByCaseId")
    public Result<?> updateByCaseId(@RequestBody TaskBatchBreakRuleDel taskBatchBreakRuleDel) {
        taskBatchBreakRuleDelService.update(taskBatchBreakRuleDel,
                new QueryWrapper<TaskBatchBreakRuleDel>()
                        .eq("BATCH_ID", taskBatchBreakRuleDel.getBatchId())
                        .eq("CASE_ID", taskBatchBreakRuleDel.getCaseId()));
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "违规模型详情-通过id删除")
    @ApiOperation(value = "违规模型详情-通过id删除", notes = "违规模型详情-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        taskBatchBreakRuleDelService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "违规模型详情-批量删除")
    @ApiOperation(value = "违规模型详情-批量删除", notes = "违规模型详情-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.taskBatchBreakRuleDelService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "违规模型详情-通过id查询")
    @ApiOperation(value = "违规模型详情-通过id查询", notes = "违规模型详情-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        TaskBatchBreakRuleDel taskBatchBreakRuleDel = taskBatchBreakRuleDelService.getById(id);
        return Result.ok(taskBatchBreakRuleDel);
    }

    /**
     * 通过批次模型查询
     *
     * @return
     */
    @AutoLog(value = "违规模型详情-通过批次模型查询")
    @ApiOperation(value = "违规模型详情-通过批次模型查询", notes = "违规模型详情-通过批次模型查询")
    @GetMapping(value = "/queryByBatchCase")
    public Result<?> queryByBatchCase(@RequestParam(name = "batchId") String batchId,
                                      @RequestParam(name = "caseId") String caseId) {
        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID",batchId)
                .eq("CASE_ID",caseId);
        TaskBatchBreakRuleDel taskBatchBreakRuleDel = taskBatchBreakRuleDelService.getOne(queryWrapper);
        return Result.ok(taskBatchBreakRuleDel);
    }

    /**
     * 导出excel
     *
     */
    @RequestMapping(value = "/exportXls")
    public void exportXls(HttpServletRequest req, HttpServletResponse response, TaskBatchBreakRuleDel bean) throws Exception {
        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());

//        String title = "违规规则运行详情_导出" + System.currentTimeMillis();

        OutputStream os = response.getOutputStream();
        this.taskBatchBreakRuleDelService.exportExcel(bean.getRuleType(),queryWrapper, os);
    }

    @RequestMapping(value = "/exportXlsThread")
    public Result exportXlsThread(HttpServletRequest req, HttpServletResponse response, TaskBatchBreakRuleDel bean) throws Exception {
        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());

        int count = this.taskBatchBreakRuleDelService.count(queryWrapper);
        final String ruleType = bean.getRuleType();
        ThreadUtils.EXPORT_POOL.add("违规规则运行详情_导出", "xls", count, (os) -> {
            try {
                this.taskBatchBreakRuleDelService.exportExcel(ruleType, queryWrapper, os);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }
            return Result.ok();
        });

        return Result.ok("等待导出");

    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, TaskBatchBreakRuleDel.class);
    }

}
