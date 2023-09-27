package com.ai.modules.medical.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.service.IMedicalSysDictService;
import com.ai.modules.engine.service.IEngineChargeService;
import com.ai.modules.engine.service.IEngineDrugService;
import com.ai.modules.engine.service.IEngineTreatService;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.service.IMedicalDrugRuleGroupService;
import com.ai.modules.medical.service.IMedicalDrugRuleService;
import com.ai.modules.medical.vo.MedicalDrugRuleVO;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.service.ITaskBatchStepItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 药品合规规则
 * @Author: jeecg-boot
 * @Date: 2019-12-19
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "药品合规规则")
@RestController
@RequestMapping("/medical/medicalDrugRule")
public class MedicalDrugRuleController extends JeecgController<MedicalDrugRule, IMedicalDrugRuleService> {


    @Autowired
    private IMedicalDrugRuleService medicalDrugRuleService;

    @Autowired
    IMedicalSysDictService medicalSysDictService;

    @Autowired
    IMedicalDrugRuleGroupService groupService;

    @Autowired
    ITaskBatchStepItemService taskBatchStepItemService;

    @Autowired
    IEngineDrugService engineDrugService;

    @Autowired
    IEngineChargeService engineChargeService;

    @Autowired
    IEngineTreatService engineTreatService;

    /**
     * 添加
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "药品合规规则-添加")
    @ApiOperation(value = "药品合规规则-添加", notes = "药品合规规则-添加")
    @PostMapping(value = "/saveMedicalDrugRule")
    public Result<?> saveMedicalDrugRule(@RequestBody MedicalDrugRule bean) {
        try {
            medicalDrugRuleService.saveMedicalDrugRule(bean);
        } catch (Exception e) {
            Result.error(e.getMessage());
        }
        Result result = Result.ok(bean);
        result.setMessage("添加成功");
        return result;
    }

    /**
     * 修改
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "药品合规规则-修改")
    @ApiOperation(value = "药品合规规则-修改", notes = "药品合规规则-修改")
    @PutMapping(value = "/updateMedicalDrugRule")
    public Result<?> updateMedicalDrugRule(@RequestBody MedicalDrugRule bean) {
        try {
            medicalDrugRuleService.updateMedicalDrugRule(bean);
        } catch (Exception e) {
            Result.error(e.getMessage());
        }
        return Result.ok("修改成功");
    }

    /**
     * 分页列表查询
     *
     * @param medicalDrugRule
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "药品合规规则-分页列表查询")
    @ApiOperation(value = "药品合规规则-分页列表查询", notes = "药品合规规则-分页列表查询")
    @RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
    public Result<?> queryPageList(MedicalDrugRuleVO medicalDrugRule,
                                   String mLimitScope,
                                   String isFrequencyBlank,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalDrugRule> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugRule, req.getParameterMap());
        if (StringUtils.isNotBlank(mLimitScope)) {
            String[] limits = mLimitScope.split(",");
            for (String limit : limits) {
                queryWrapper.like("LIMIT_SCOPE", limit);
            }
        }
        if (StringUtils.isNotBlank(isFrequencyBlank)) {
            if ("1".equals(isFrequencyBlank)) {
                queryWrapper.isNull("FREQUENCY").isNull("TWO_FREQUENCY");
            } else {
                queryWrapper.and(wrapper ->wrapper.isNotNull("FREQUENCY")
                        .or().isNotNull("TWO_FREQUENCY"));
            }
        }
        Page<MedicalDrugRuleVO> page = new Page<>(pageNo, pageSize);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        IPage<MedicalDrugRuleVO> pageList = medicalDrugRuleService.pageVO(page, queryWrapper, sysUser.getDataSource());
        return Result.ok(pageList);
    }

    @AutoLog(value = "药品合规规则-列表全选")
    @ApiOperation(value = "药品合规规则-列表全选", notes = "药品合规规则-列表全选")
    @GetMapping(value = "/selectAll")
    public Result<?> selectAll(MedicalDrugRule medicalDrugRule,
                               String mLimitScope,
                               HttpServletRequest req) {
        QueryWrapper<MedicalDrugRule> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugRule, req.getParameterMap());
        if (StringUtils.isNotBlank(mLimitScope)) {
            String[] limits = mLimitScope.split(",");
            for (String limit : limits) {
                queryWrapper.like("LIMIT_SCOPE", limit);
            }
        }
        String nameCol = "1".equals(medicalDrugRule.getRuleType()) ? "DRUG_NAMES" : "CHARGE_ITEMS";
        queryWrapper.select("RULE_ID ID", nameCol + " NAME"); // code: 'chargeItemCodes',
        List<Map<String, Object>> list = medicalDrugRuleService.listMaps(queryWrapper);
        return Result.ok(list);
    }

    @AutoLog(value = "药品合规规则-通过规则ID查询试算状态")
    @ApiOperation(value = "药品合规规则-通过规则ID查询试算状态", notes = "药品合规规则-通过规则ID查询试算状态")
    @GetMapping(value = "/trailStatusList")
    public Result<?> trailStatusList(String ruleIds) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<TaskBatchStepItem> queryWrapper = new QueryWrapper<TaskBatchStepItem>()
                .in("BATCH_ID", Arrays.asList(ruleIds.split(",")))
                .eq("DATA_SOURCE", sysUser.getDataSource())
                .select("BATCH_ID", "STATUS");
        List<TaskBatchStepItem> list = taskBatchStepItemService.list(queryWrapper);
        return Result.ok(list);
    }

    @AutoLog(value = "药品合规规则-试算")
    @ApiOperation(value = "药品合规规则-试算", notes = "药品合规规则-试算")
    @GetMapping(value = "/trail")
    public Result<?> trail(@RequestParam(name = "ruleId") String ruleId, String etlSource, String ruleType) {
        if ("1".equals(ruleType)) {
            engineDrugService.trailDrugActionThreadPool(ruleId, etlSource);
        } else if ("2".equals(ruleType)) {
            engineChargeService.trailChargeActionThreadPool(ruleId, etlSource);
        } else if ("4".equals(ruleType)) {
            engineTreatService.trailTreatActionThreadPool(ruleId, etlSource);
        } else if ("CHARGE".equals(ruleType)) {
            return Result.error("还未开发完成");
        }

        return Result.ok("操作成功");
    }

    /**
     * 添加
     *
     * @param medicalDrugRule
     * @return
     */
    @AutoLog(value = "药品合规规则-添加")
    @ApiOperation(value = "药品合规规则-添加", notes = "药品合规规则-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody MedicalDrugRule medicalDrugRule) {
        medicalDrugRuleService.save(medicalDrugRule);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalDrugRule
     * @return
     */
    @AutoLog(value = "药品合规规则-编辑")
    @ApiOperation(value = "药品合规规则-编辑", notes = "药品合规规则-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalDrugRule medicalDrugRule) {
        medicalDrugRuleService.updateById(medicalDrugRule);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "药品合规规则-通过id删除")
    @ApiOperation(value = "药品合规规则-通过id删除", notes = "药品合规规则-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        medicalDrugRuleService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "药品合规规则-批量删除")
    @ApiOperation(value = "药品合规规则-批量删除", notes = "药品合规规则-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.medicalDrugRuleService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "药品合规规则-通过id查询")
    @ApiOperation(value = "药品合规规则-通过id查询", notes = "药品合规规则-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalDrugRule medicalDrugRule = medicalDrugRuleService.getById(id);
        return Result.ok(medicalDrugRule);
    }

    /**
     * 直接导出excel
     *
     * @param req
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/exportXls")
    public void exportXls(HttpServletRequest req, HttpServletResponse response, MedicalDrugRule bean) throws Exception {
        QueryWrapper<MedicalDrugRule> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());
        String excelName = "药品合规规则_导出";
        if ("2".equals(bean.getRuleType())) {
            excelName = "收费合规规则_导出";
        }else if ("4".equals(bean.getRuleType())) {
            excelName = "诊疗合理规则_导出";
        }
        String title = excelName + System.currentTimeMillis();
        String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "." + suffix).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

        OutputStream os = response.getOutputStream();
        this.medicalDrugRuleService.exportExcel(bean.getRuleType(), queryWrapper, os, suffix);
    }

    @RequestMapping(value = "/exportXlsThread")
    public Result exportXlsThread(HttpServletRequest req, HttpServletResponse response, MedicalDrugRule bean) throws Exception {
        QueryWrapper<MedicalDrugRule> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());

        int count = this.medicalDrugRuleService.count(queryWrapper);
        final String ruleType = bean.getRuleType();
        String excelName = "药品合规规则_导出";
        if ("2".equals(bean.getRuleType())) {
            excelName = "收费合规规则_导出";
        }else if ("4".equals(bean.getRuleType())) {
            excelName = "诊疗合理规则_导出";
        }
        String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
        ThreadUtils.EXPORT_POOL.add(excelName, suffix, count, (os) -> {
            try {
                this.medicalDrugRuleService.exportExcel(ruleType, queryWrapper, os, suffix);
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
     * @throws Exception
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// 获取上传文件对象
            // 判断文件名是否为空
            if (file == null) {
                return Result.error("上传文件为空");
            }
            // 获取文件名
            String name = file.getOriginalFilename();
            // 判断文件大小、即名称
            long size = file.getSize();
            if (name == null || ("").equals(name) && size == 0) {
                return Result.error("上传文件内容为空");
            }
            return this.medicalDrugRuleService.importExcel(file, multipartRequest, user);

        }
        return Result.error("上传文件为空");
    }

    @RequestMapping(value = "/exportRuleLoseThread")
    public Result exportRuleLoseThread(HttpServletRequest req, HttpServletResponse response, MedicalDrugRule bean) throws Exception {
        QueryWrapper<MedicalDrugRule> queryWrapper = QueryGenerator.initQueryWrapper(bean, req.getParameterMap());

        int count = this.medicalDrugRuleService.count(queryWrapper);
        final String ruleType = bean.getRuleType();
        String excelName = "药品合规规则编码失效明细_导出";
        if ("2".equals(bean.getRuleType())) {
            excelName = "收费合规规则编码失效明细_导出";
        }
        String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
        ThreadUtils.EXPORT_POOL.add(excelName, suffix, count, (os) -> {
            try {
                this.medicalDrugRuleService.exportRuleLoseExcel(ruleType, queryWrapper, os, suffix);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }
            return Result.ok();
        });

        return Result.ok("等待导出");

    }


}
