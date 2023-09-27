package com.ai.modules.medical.controller;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.mapper.MedicalClinicalMapper;
import com.ai.modules.medical.service.*;
import com.ai.modules.medical.vo.MedicalRuleConfigVO;
import com.alibaba.fastjson.JSONObject;
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
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.internal.Function;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 通用规则配置
 * @Author: jeecg-boot
 * @Date: 2020-12-14
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "通用规则配置")
@RestController
@RequestMapping("/medical/medicalRuleConfig")
public class MedicalRuleConfigController extends JeecgController<MedicalRuleConfig, IMedicalRuleConfigService> {
    @Autowired
    private IMedicalRuleConfigService medicalRuleConfigService;

    @Autowired
    private MedicalClinicalMapper medicalClinicalMapper;

    @Autowired
    private IMedicalRuleConfigChargeService medicalRuleConfigChargeService;

    @Autowired
    private IMedicalRuleConfigTreatService medicalRuleConfigTreatService;

    @Autowired
    private IMedicalRuleConfigDrugService medicalRuleConfigDrugService;

    @Autowired
    private IMedicalRuleConfigDruguseService medicalRuleConfigDruguseService;

    @Autowired
    IMedicalImportTaskService importTaskService;

    /**
     * 分页列表查询
     *
     * @param medicalRuleConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "通用规则配置-分页列表查询")
    @ApiOperation(value = "通用规则配置-分页列表查询", notes = "通用规则配置-分页列表查询")
    @RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
    public Result<?> queryPageList(MedicalRuleConfig medicalRuleConfig,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalRuleConfig> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleConfig, req.getParameterMap());
        groupCodeSql(req, medicalRuleConfig, queryWrapper);

        Page<MedicalRuleConfig> page = new Page<MedicalRuleConfig>(pageNo, pageSize);
        IPage<MedicalRuleConfig> pageList = medicalRuleConfigService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @AutoLog(value = "通用规则配置-全部类型数据")
    @ApiOperation(value = "通用规则配置-全部类型数据", notes = "通用规则配置-全部类型数据")
    @GetMapping(value = "/selectRuleTypeCount")
    public Result<?> selectRuleTypeCount(MedicalRuleConfig medicalRuleConfig,
                               HttpServletRequest req) {
        Map<String,Integer> data = new HashMap<>();
        String[] ruleTypeArr = new String[]{IMedicalRuleConfigChargeService.RULE_TYPE,
                IMedicalRuleConfigTreatService.RULE_TYPE,
                IMedicalRuleConfigDrugService.RULE_TYPE,
                IMedicalRuleConfigDruguseService.RULE_TYPE
        };
        for(String ruleType:ruleTypeArr){
            medicalRuleConfig.setRuleType(ruleType);
            QueryWrapper<MedicalRuleConfig> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleConfig, req.getParameterMap());
            groupCodeSql(req, medicalRuleConfig, queryWrapper);
            data.put(ruleType,medicalRuleConfigService.count(queryWrapper));
        }
        return Result.ok(data);
    }

    @AutoLog(value = "通用规则配置-全选数据")
    @ApiOperation(value = "通用规则配置-全选数据", notes = "通用规则配置-全选数据")
    @GetMapping(value = "/selectAll")
    public Result<?> selectAll(MedicalRuleConfig medicalRuleConfig,
                               HttpServletRequest req) {
        QueryWrapper<MedicalRuleConfig> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleConfig, req.getParameterMap());
        queryWrapper.select("RULE_ID ID", "ITEM_CODES CODE", "ITEM_NAMES NAME");
        List<Map<String, Object>> list = medicalRuleConfigService.listMaps(queryWrapper);
        return Result.ok(list);
    }

    @AutoLog(value = "通用规则配置-添加")
    @ApiOperation(value = "通用规则配置-添加", notes = "通用规则配置-添加")
    @PutMapping(value = "/changeStatus")
    public Result<?> changeStatus(@RequestParam(name = "ids") String ids, @RequestParam(name = "status") String status) {
        String finalStatus = MedicalConstant.SWITCH_NORMAL.equals(status)? MedicalConstant.SWITCH_NORMAL : MedicalConstant.SWITCH_STOP;;
        List<MedicalRuleConfig> list = Arrays.stream(ids.split(",")).map(id -> {
            MedicalRuleConfig bean = new MedicalRuleConfig();
            bean.setRuleId(id);
            bean.setStatus(finalStatus);
            return bean;
        }).collect(Collectors.toList());
        medicalRuleConfigService.updateBatchById(list);
        return Result.ok("修改成功！");
    }

    /**
     * 添加
     *
     * @param medicalRuleConfig
     * @return
     */
    @AutoLog(value = "通用规则配置-添加")
    @ApiOperation(value = "通用规则配置-添加", notes = "通用规则配置-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody MedicalRuleConfigVO medicalRuleConfig) {
        medicalRuleConfig.setStatus(MedicalConstant.SWITCH_NORMAL);
        medicalRuleConfigService.saveVO(medicalRuleConfig);
        return Result.ok("添加成功！");
    }

    @AutoLog(value = "通用规则配置-批量添加")
    @ApiOperation(value = "通用规则配置-添加", notes = "通用规则配置-添加")
    @PostMapping(value = "/addBatch")
    public Result<?> addBatch(@RequestBody MedicalRuleConfigVO medicalRuleConfig) {
        medicalRuleConfig.setStatus(MedicalConstant.SWITCH_NORMAL);
        medicalRuleConfigService.saveBatch(medicalRuleConfig);
        return Result.ok("批量添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalRuleConfig
     * @return
     */
    @AutoLog(value = "通用规则配置-编辑")
    @ApiOperation(value = "通用规则配置-编辑", notes = "通用规则配置-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalRuleConfigVO medicalRuleConfig) {
        String ruleId = medicalRuleConfig.getRuleId();
        List<MedicalRuleConditionSet> accessConditions = medicalRuleConfig.getAccessConditions();
        for (MedicalRuleConditionSet bean : accessConditions) {
            bean.setType("access");
            bean.setRuleId(ruleId);
            if (StringUtils.isBlank(bean.getLogic())) {
                bean.setLogic("AND");
            }
            if (StringUtils.isBlank(bean.getCompare())) {
                bean.setCompare("=");
            }
        }
        List<MedicalRuleConditionSet> judgeConditions = medicalRuleConfig.getJudgeConditions();
        for (MedicalRuleConditionSet bean : judgeConditions) {
            bean.setType("judge");
            bean.setRuleId(ruleId);
            if (StringUtils.isBlank(bean.getLogic())) {
                bean.setLogic("AND");
            }
            if (StringUtils.isBlank(bean.getCompare())) {
                bean.setCompare("=");
            }
        }
        List<MedicalRuleConditionSet> conditionSets = new ArrayList<>(accessConditions);
        conditionSets.addAll(judgeConditions);
        medicalRuleConfig.setRuleId(ruleId);
        medicalRuleConfigService.updateById(medicalRuleConfig, conditionSets);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "通用规则配置-通过id删除")
    @ApiOperation(value = "通用规则配置-通过id删除", notes = "通用规则配置-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        medicalRuleConfigService.removeByRuleId(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "通用规则配置-批量删除")
    @ApiOperation(value = "通用规则配置-批量删除", notes = "通用规则配置-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.medicalRuleConfigService.removeByRuleIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "通用规则配置-通过id查询")
    @ApiOperation(value = "通用规则配置-通过id查询", notes = "通用规则配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalRuleConfig medicalRuleConfig = medicalRuleConfigService.getById(id);
        return Result.ok(medicalRuleConfig);
    }

    @AutoLog(value = "合理用药配置条件组-单个规则条件组查询")
    @ApiOperation(value="合理用药配置条件组-单个规则条件组查询", notes="合理用药配置条件组-单个规则条件组查询")
    @GetMapping(value = "/queryNameByCodes")
    public Result<?> listRuleByRuleId(String diseaseGroups,
                                      String treatGroups,
                                      String drugGroups,
                                      String treatments){
        JSONObject result = new JSONObject();
        // code -> name

        // 数据库取的对应
        if (StringUtils.isNotBlank(diseaseGroups)){
            Map<String, String> diseaseGroupMap = new HashMap<>();

            List<MedicalCodeNameVO> groupMaps = this.medicalClinicalMapper.queryGroupCodeIdInCodes(
                    diseaseGroups.split(","), "5");
            for (MedicalCodeNameVO map : groupMaps) {
                diseaseGroupMap.put(map.getCode(),map.getName());
            }
            result.put("diseaseGroups", diseaseGroupMap);

        }

        if (StringUtils.isNotBlank(treatGroups)) {
            Map<String, String> treatGroupMap = new HashMap<>();

            List<MedicalCodeNameVO> groupMaps = this.medicalClinicalMapper.queryGroupCodeIdInCodes(
                    treatGroups.split(","), "1");
            for (MedicalCodeNameVO map : groupMaps) {
                treatGroupMap.put(map.getCode(),map.getName());
            }
            result.put("treatGroups", treatGroupMap);

        }

        if (StringUtils.isNotBlank(drugGroups)) {
            Map<String, String> drugGroupMap = new HashMap<>();

            List<MedicalCodeNameVO> groupMaps = this.medicalClinicalMapper.queryGroupCodeIdInCodes(
                    drugGroups.split(","), "7");
            for (MedicalCodeNameVO map: groupMaps) {
                drugGroupMap.put(map.getCode(),map.getName());
            }
            result.put("drugGroups", drugGroupMap);

        }

        if (StringUtils.isNotBlank(treatments)) {
            Map<String, String> treatmentMap = new HashMap<>();

            List<MedicalCodeNameVO> groupMaps = this.medicalClinicalMapper.queryItemCodeIdInCodes(
                    treatments.split(","), "1");
            for (MedicalCodeNameVO map : groupMaps) {
                treatmentMap.put(map.getCode(),map.getName());
            }
            result.put("treatments", treatmentMap);

        }

        return Result.ok(result);
    }

    /**
     * 导出excel
     **/
    @RequestMapping(value = "/exportXls")
    public Result<?> exportXls(HttpServletRequest req, HttpServletResponse response, MedicalRuleConfig medicalRuleConfig) throws Exception {
        // 选中数据
        String selections = req.getParameter("selections");
        if (org.apache.commons.lang.StringUtils.isNotEmpty(selections)) {
            medicalRuleConfig.setRuleId(selections);
        }
        QueryWrapper<MedicalRuleConfig> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleConfig, req.getParameterMap());
        groupCodeSql(req, medicalRuleConfig, queryWrapper);

        IMedicalRuleConfigCommonService service = null;

        String title = "合规配置";
        if(IMedicalRuleConfigChargeService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())){
            title = "收费合规配置";
            service = this.medicalRuleConfigChargeService;
        } else if(IMedicalRuleConfigTreatService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())){
            title = "合理诊疗配置";
            service = this.medicalRuleConfigTreatService;
        } else if(IMedicalRuleConfigDrugService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())){
            title = "药品合规配置";
            service = this.medicalRuleConfigDrugService;
        } else if(IMedicalRuleConfigDruguseService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())){
            title = "合理用药配置";
            service = this.medicalRuleConfigDruguseService;
        } else {
//            service = this.medicalRuleConfigDruguseService;
        }

        long count = this.medicalRuleConfigService.count(queryWrapper);

        IMedicalRuleConfigCommonService finalService = service;
        Function<OutputStream, Result> func = (os) -> {
            try {
                finalService.exportExcel(queryWrapper, os);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                if(msg.length() > 1000){
                    msg = msg.substring(0, 1000);
                }
                return Result.error(msg);
            }
            return Result.ok();
        };

        if (count > 5000) {
            ThreadUtils.EXPORT_POOL.add(title + "_导出", "xlsx", (int) count, func);
            return Result.ok("等待导出");
        }
        Result result = func.apply(response.getOutputStream());
        if(!result.isSuccess()){
            return result;
        }
        return null;
    }

    /**
     * 通过excel导入数据
     *
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(@RequestParam("file") MultipartFile file, @RequestParam("ruleType") String ruleType) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        try {
            if (StringUtils.isNotBlank(name) && size > 0) {

                long beginTime = System.currentTimeMillis();
                String msg = "不存在规则类型";

                Result result = Result.error(msg);
                if(IMedicalRuleConfigChargeService.RULE_TYPE.equals(ruleType)){
                   result = importTaskService.saveImportTask("MEDICAL_RULE_CONFIG","收费合规导入",file,user,
                            (filename,multipartRequest)->{
                                try {
                                    return this.medicalRuleConfigChargeService.importExcel(filename);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return Result.error(e.getMessage());
                                }
                            });
//                    msg = this.medicalRuleConfigChargeService.importExcel(file);
                } else if(IMedicalRuleConfigTreatService.RULE_TYPE.equals(ruleType)){
                    result = importTaskService.saveImportTask("MEDICAL_RULE_CONFIG","合理诊疗导入",file,user,
                            (filename,multipartRequest)->{
                                try {
                                    return this.medicalRuleConfigTreatService.importExcel(filename);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return Result.error(e.getMessage());
                                }
                            });
//                    msg = this.medicalRuleConfigTreatService.importExcel(file);
                } else if(IMedicalRuleConfigDrugService.RULE_TYPE.equals(ruleType)){
                    result = importTaskService.saveImportTask("MEDICAL_RULE_CONFIG","药品合规导入",file,user,
                            (filename,multipartRequest)->{
                                try {
                                    return this.medicalRuleConfigDrugService.importExcel(filename);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return Result.error(e.getMessage());
                                }
                            });
//                    msg = this.medicalRuleConfigDrugService.importExcel(file);
                } else if(IMedicalRuleConfigDruguseService.RULE_TYPE.equals(ruleType)){
                    result = importTaskService.saveImportTask("MEDICAL_RULE_CONFIG","合理用药导入",file,user,
                            (filename,multipartRequest)->{
                                try {
                                    return this.medicalRuleConfigDruguseService.importExcel(filename);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return Result.error(e.getMessage());
                                }
                            });
//                    msg = this.medicalRuleConfigDruguseService.importExcel(file);
                }
                msg = result.getMessage();
                if(!result.isSuccess()){
                    return Result.error("导入 " + name + " 失败：" + msg);
                }

                long endTime = System.currentTimeMillis();

                log.info("[" + name + "]导入时间：" + (endTime - beginTime) / 1000 + "秒");
                return Result.ok("导入成功，" + msg);
            } else {
                throw new Exception("请检查文件是否有效");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入 " + name + " 失败：" + e.getMessage());
        }
    }


    /**
     * 导出excel
     **/
    @RequestMapping(value = "/exportInvalidXls")
    public Result<?> exportInvalidXls(HttpServletRequest req, HttpServletResponse response, MedicalRuleConfig medicalRuleConfig) throws Exception {
        QueryWrapper<MedicalRuleConfig> queryWrapper = QueryGenerator.initQueryWrapper(medicalRuleConfig, req.getParameterMap());
        groupCodeSql(req, medicalRuleConfig, queryWrapper);

        long count = this.medicalRuleConfigService.count(queryWrapper);
        IMedicalRuleConfigCommonService service = null;

        String title = "失效明细";
        if(IMedicalRuleConfigChargeService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())){
            title = "收费合规失效明细";
            service = this.medicalRuleConfigChargeService;
        } else if(IMedicalRuleConfigTreatService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())){
            title = "合理诊疗失效明细";
            service = this.medicalRuleConfigTreatService;
        } else if(IMedicalRuleConfigDrugService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())){
            title = "药品合规失效明细";
            service = this.medicalRuleConfigDrugService;
        } else if(IMedicalRuleConfigDruguseService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())){
            title = "合理用药失效明细";
            service = this.medicalRuleConfigDruguseService;
        } else {
//            service = this.medicalRuleConfigDruguseService;
        }

        IMedicalRuleConfigCommonService finalService = service;
        Function<OutputStream, Result> func = (os) -> {
            try {
                finalService.exportInvalidExcel(queryWrapper, os);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                if(msg != null && msg.length() > 1000){
                    msg = msg.substring(0, 1000);
                }
                return Result.error(msg);
            }
            return Result.ok();
        };

//        if (count > 5000) {
//            ThreadUtils.EXPORT_POOL.add(title + "_导出", "xlsx", (int) count, func);
//            return Result.ok("等待导出");
//        }
        Result result = func.apply(response.getOutputStream());
        if(!result.isSuccess()){
            return result;
        }
        return null;
    }

    private void groupCodeSql(HttpServletRequest req, MedicalRuleConfig medicalRuleConfig, QueryWrapper<MedicalRuleConfig> queryWrapper) {
        /**
         * 涉及疾病组
         *  *药品规则
         *          * --限年龄、限科室、限支付时长、给药途径、药品使用缺少必要药品或项目(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='accessDiseaseGroup';
         *          * --限适应症(a.ext2)
         *          * select * from medical_rule_condition_set a where a.FIELD='indication';
         *          * --门慢适应症审核(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='diseaseGroup';
         *          * --限特定人群(ext4)
         *          * select * from medical_rule_condition_set a where a.FIELD='xtdrq';
         *          *
         *  * 收费、合理诊疗规则
         *          * --限年龄、限频次(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='accessDiseaseGroup';
         *          * --限适应症、限禁忌症(a.ext2)
         *          * select * from medical_rule_condition_set a where a.FIELD in('indication','unIndication');
         *          * --诊断与既往项目不符(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='diseaseGroup';
         */
        String diseaseGroupCode = req.getParameter("diseaseGroupCode");//疾病组编码
        if (StringUtils.isNotBlank(diseaseGroupCode)) {
            if (IMedicalRuleConfigChargeService.RULE_TYPE.equals(medicalRuleConfig.getRuleType()) ||
                    IMedicalRuleConfigTreatService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                queryWrapper.inSql("RULE_ID", "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in('indication','unIndication') and ext2 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext2")+", ',', '|'), '|'), '|" + diseaseGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in('accessDiseaseGroup','diseaseGroup') and ext1 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext1")+", ',', '|'), '|'), '|" + diseaseGroupCode + "|')>0");
            } else if (IMedicalRuleConfigDrugService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                queryWrapper.inSql("RULE_ID", "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in('indication','unIndication') and ext2 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext2")+", ',', '|'), '|'), '|" + diseaseGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in('accessDiseaseGroup','diseaseGroup') and ext1 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext1")+", ',', '|'), '|'), '|" + diseaseGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='xtdrq' and ext4 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext4")+", ',', '|'), '|'), '|" + diseaseGroupCode + "|')>0");
            } else if (IMedicalRuleConfigDruguseService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                //如果不加上 union select null会出现意想不到的结果，改成exists写法
            	queryWrapper.exists("select 1 from medical_rule_condition_set a where MEDICAL_RULE_CONFIG.RULE_ID=a.RULE_ID and "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='indication' and ext2 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext2")+", ',', '|'), '|'), '|" + diseaseGroupCode + "|')>0");
            }
        }

        /**
         * 涉及药品组
         *   * 药品规则
         *          * --限适应症(a.ext5)
         *          * select * from medical_rule_condition_set a where a.FIELD='indication';
         *          * --给药途径(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='accessDrugGroup';
         *          * --药品使用缺少必要药品或项目(a.ext3)
         *          * select * from medical_rule_condition_set a where a.FIELD='itemOrDrugGroup';
         *          * --限特定人群(ext6)
         *          * select * from medical_rule_condition_set a where a.FIELD='xtdrq';
         *          *
         *   * 收费、合理诊疗规则
         *          * --限适应症、限禁忌症(a.ext5)
         *          * select * from medical_rule_condition_set a where a.FIELD in('indication', 'unIndication');
         *          * --必要前提条件(a.ext3)
         *          * select * from medical_rule_condition_set a where a.FIELD='fitGroups';
         */
        String drugGroupCode = req.getParameter("drugGroupCode");//药品组编码
        if (StringUtils.isNotBlank(drugGroupCode)) {
            if (IMedicalRuleConfigChargeService.RULE_TYPE.equals(medicalRuleConfig.getRuleType()) ||
                    IMedicalRuleConfigTreatService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                queryWrapper.inSql("RULE_ID", "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in('indication', 'unIndication') and ext5 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext5")+", ',', '|'), '|'), '|" + drugGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='fitGroups' and ext3 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext3")+", ',', '|'), '|'), '|" + drugGroupCode + "|')>0");
            } else if (IMedicalRuleConfigDrugService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                queryWrapper.inSql("RULE_ID", "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='indication' and ext5 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext5")+", ',', '|'), '|'), '|" + drugGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in('accessDrugGroup', 'unpayDrug') and ext1 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext1")+", ',', '|'), '|'), '|" + drugGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='itemOrDrugGroup' and ext3 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext3")+", ',', '|'), '|'), '|" + drugGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='xtdrq' and ext6 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext6")+", ',', '|'), '|'), '|" + drugGroupCode + "|')>0");
            } else if (IMedicalRuleConfigDruguseService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                queryWrapper.exists("select 1 from medical_rule_condition_set a where MEDICAL_RULE_CONFIG.RULE_ID=a.RULE_ID and "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='indication' and ext5 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext5")+", ',', '|'), '|'), '|" + diseaseGroupCode + "|')>0");
            }
        }

        /**
         * 涉及项目组
         *   * 药品规则
         *          * --限适应症(a.ext3)
         *          * select * from medical_rule_condition_set a where a.FIELD='indication';
         *          * --给药途径(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='accessProjectGroup';
         *          * --药品使用缺少必要药品或项目(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='itemOrDrugGroup';
         *          * --限特定人群(ext8)
         *          * select * from medical_rule_condition_set a where a.FIELD='xtdrq';
         *          *
         *  * 收费、合理诊疗规则
         *          * --限频次(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='accessProjectGroup';
         *          * --一日重复收费(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='dayUnfitGroups';
         *          * --一次就诊重复收费(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='unfitGroups';
         *          * --限适应症、限禁忌症(a.ext3)
         *          * select * from medical_rule_condition_set a where a.FIELD in('indication', 'unIndication');
         *          * --必要前提条件(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='fitGroups';
         *          * --诊断与既往项目不符、本次项目与既往项目不符(a.ext1)
         *          * select * from medical_rule_condition_set a where a.FIELD='hisGroups';
         */
        String projectGroupCode = req.getParameter("projectGroupCode");//项目组编码
        if (StringUtils.isNotBlank(projectGroupCode)) {
            if (IMedicalRuleConfigChargeService.RULE_TYPE.equals(medicalRuleConfig.getRuleType()) ||
                    IMedicalRuleConfigTreatService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                queryWrapper.inSql("RULE_ID", "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in('indication', 'unIndication') and ext3 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext3")+", ',', '|'), '|'), '|" + projectGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in ('accessProjectGroup','dayUnfitGroups','unfitGroups','fitGroups','hisGroups') and ext1 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext1")+", ',', '|'), '|'), '|" + projectGroupCode + "|')>0");
            } else if (IMedicalRuleConfigDrugService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                queryWrapper.inSql("RULE_ID", "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='indication' and ext3 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext3")+", ',', '|'), '|'), '|" + projectGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+" in ('accessProjectGroup','itemOrDrugGroup') and ext1 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext1")+", ',', '|'), '|'), '|" + projectGroupCode + "|')>0\n" +
                        "union\n" +
                        "select distinct rule_id from medical_rule_condition_set a where "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='xtdrq' and ext8 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext8")+", ',', '|'), '|'), '|" + projectGroupCode + "|')>0");
            } else if (IMedicalRuleConfigDruguseService.RULE_TYPE.equals(medicalRuleConfig.getRuleType())) {
                queryWrapper.exists("select 1 from medical_rule_condition_set a where MEDICAL_RULE_CONFIG.RULE_ID=a.RULE_ID and "+DbDataEncryptUtil.decryptFunc("a.FIELD")+"='indication' and ext3 is not null\n" +
                        "and instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc("ext3")+", ',', '|'), '|'), '|" + diseaseGroupCode + "|')>0");
            }
        }
    }
}
