package com.ai.modules.medical.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.config.entity.*;
import com.ai.modules.config.service.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.service.IMedicalClinicalAccessGroupService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description: 临床路径准入条件组
 * @Author: jeecg-boot
 * @Date: 2020-03-09
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "临床路径准入条件组")
@RestController
@RequestMapping("/medical/medicalClinicalAccessGroup")
public class MedicalClinicalAccessGroupController extends JeecgController<MedicalClinicalAccessGroup, IMedicalClinicalAccessGroupService> {

    @Autowired
    private IMedicalClinicalAccessGroupService medicalClinicalAccessGroupService;
    @Autowired
    private IMedicalDiseaseGroupService medicalDiseaseGroupService;
    @Autowired
    private IMedicalOperationService medicalOperationService;
    @Autowired
    private IMedicalTreatProjectService medicalTreatProjectService;
    @Autowired
    private IMedicalDrugGroupService medicalDrugGroupService;
    @Autowired
    private IMedicalPathologyService medicalPathologyService;

    /**
     * 通过临床路径ID查询
     *
     * @param clinicalId
     * @return
     */
    @AutoLog(value = "临床路径准入条件组-通过临床路径ID查询")
    @ApiOperation(value = "临床路径准入条件组-通过临床路径ID查询", notes = "临床路径准入条件组-通过临床路径ID查询")
    @GetMapping(value = "/queryByClinicalId")
    public Result<?> queryByClinicalId(@RequestParam(name = "clinicalId") String clinicalId, @RequestParam(name = "groupType",defaultValue = "approve") String groupType) {
        List<MedicalClinicalAccessGroup> list = medicalClinicalAccessGroupService.list(
                new QueryWrapper<MedicalClinicalAccessGroup>()
                        .eq("CLINICAL_ID", clinicalId)
                        .eq("GROUP_TYPE", groupType).orderByAsc("GROUP_NO")
        );
        Set<String> diseaseCodes = new HashSet<>();
        Set<String> operationCodes = new HashSet<>();
        Set<String> projectCodes = new HashSet<>();
        Set<String> drugGroupCodes = new HashSet<>();
        Set<String> pathologyCodes = new HashSet<>();
        for (MedicalClinicalAccessGroup group : list) {
            if (StringUtils.isNotBlank(group.getDiseaseGroups())) {
                diseaseCodes.addAll(Arrays.asList(group.getDiseaseGroups().split(",")));
            }
            if (StringUtils.isNotBlank(group.getOperations())) {
                operationCodes.addAll(Arrays.asList(group.getOperations().split(",")));
            }
            if (StringUtils.isNotBlank(group.getCheckItems())) {
                projectCodes.addAll(Arrays.asList(group.getCheckItems().split(",")));
            }
            if (StringUtils.isNotBlank(group.getLabworkItems())) {
                projectCodes.addAll(Arrays.asList(group.getLabworkItems().split(",")));
            }
            if (StringUtils.isNotBlank(group.getDrugGroups())) {
                drugGroupCodes.addAll(Arrays.asList(group.getDrugGroups().split(",")));
            }
            if (StringUtils.isNotBlank(group.getPathologys())) {
                pathologyCodes.addAll(Arrays.asList(group.getPathologys().split(",")));
            }
        }
        List<MedicalDiseaseGroup> diseaseGroups = diseaseCodes.size()>0?medicalDiseaseGroupService.list(new QueryWrapper<MedicalDiseaseGroup>()
                .in("GROUP_CODE", diseaseCodes)):new ArrayList<>();
        List<MedicalOperation> operations = operationCodes.size()>0?medicalOperationService.list(new QueryWrapper<MedicalOperation>()
                .in("code", operationCodes)):new ArrayList<>();
        List<MedicalTreatProject> projects = projectCodes.size()>0?medicalTreatProjectService.list(new QueryWrapper<MedicalTreatProject>()
                .in("code", projectCodes)):new ArrayList<>();
        List<MedicalDrugGroup> drugGroups = drugGroupCodes.size()>0?medicalDrugGroupService.list(new QueryWrapper<MedicalDrugGroup>()
                .in("GROUP_CODE", drugGroupCodes)):new ArrayList<>();
        List<MedicalPathology> pathologyGroups = pathologyCodes.size()>0?medicalPathologyService.list(new QueryWrapper<MedicalPathology>()
                .in("code", pathologyCodes)):new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accessGroups", list);
        jsonObject.put("diseaseGroups", diseaseGroups);
        jsonObject.put("operations", operations);
        jsonObject.put("projects", projects);
        jsonObject.put("drugGroups", drugGroups);
        jsonObject.put("pathologys", pathologyGroups);
        return Result.ok(jsonObject);
    }

    /**
     * 分页列表查询
     *
     * @param medicalClinicalAccessGroup
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "临床路径准入条件组-分页列表查询")
    @ApiOperation(value = "临床路径准入条件组-分页列表查询", notes = "临床路径准入条件组-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalClinicalAccessGroup medicalClinicalAccessGroup,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalClinicalAccessGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalClinicalAccessGroup, req.getParameterMap());
        Page<MedicalClinicalAccessGroup> page = new Page<MedicalClinicalAccessGroup>(pageNo, pageSize);
        IPage<MedicalClinicalAccessGroup> pageList = medicalClinicalAccessGroupService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param medicalClinicalAccessGroup
     * @return
     */
    @AutoLog(value = "临床路径准入条件组-添加")
    @ApiOperation(value = "临床路径准入条件组-添加", notes = "临床路径准入条件组-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody MedicalClinicalAccessGroup medicalClinicalAccessGroup) {
        medicalClinicalAccessGroupService.save(medicalClinicalAccessGroup);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalClinicalAccessGroup
     * @return
     */
    @AutoLog(value = "临床路径准入条件组-编辑")
    @ApiOperation(value = "临床路径准入条件组-编辑", notes = "临床路径准入条件组-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalClinicalAccessGroup medicalClinicalAccessGroup) {
        medicalClinicalAccessGroupService.updateById(medicalClinicalAccessGroup);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "临床路径准入条件组-通过id删除")
    @ApiOperation(value = "临床路径准入条件组-通过id删除", notes = "临床路径准入条件组-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        medicalClinicalAccessGroupService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "临床路径准入条件组-批量删除")
    @ApiOperation(value = "临床路径准入条件组-批量删除", notes = "临床路径准入条件组-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.medicalClinicalAccessGroupService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "临床路径准入条件组-通过id查询")
    @ApiOperation(value = "临床路径准入条件组-通过id查询", notes = "临床路径准入条件组-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalClinicalAccessGroup medicalClinicalAccessGroup = medicalClinicalAccessGroupService.getById(id);
        return Result.ok(medicalClinicalAccessGroup);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param medicalClinicalAccessGroup
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MedicalClinicalAccessGroup medicalClinicalAccessGroup) {
        return super.exportXls(request, medicalClinicalAccessGroup, MedicalClinicalAccessGroup.class, "临床路径准入条件组");
    }

    /**
     * 通过excel导入数据
     *
     * @param file
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(@RequestParam("file") MultipartFile file, HttpServletResponse response) {

        int count = 0;

        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                long beginTime = System.currentTimeMillis();
                count = this.medicalClinicalAccessGroupService.importExcel(file);
                long endTime = System.currentTimeMillis();

                log.info("[" + name + "]导入时间：" + (endTime - beginTime) / 1000 + "秒");


            } catch (Exception e) {
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        }

        return Result.ok("操作成功，导入 " + count + " 条准入条件组记录");
    }

}
