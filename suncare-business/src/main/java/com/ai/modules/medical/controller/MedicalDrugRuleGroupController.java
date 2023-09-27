package com.ai.modules.medical.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.medical.entity.MedicalDrugRuleGroupDel;
import com.ai.modules.medical.vo.MedicalDrugRuleGroupDict;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.medical.entity.MedicalDrugRuleGroup;
import com.ai.modules.medical.service.IMedicalDrugRuleGroupService;

import java.util.Date;

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
 * @Description: 药品合规规则分组
 * @Author: jeecg-boot
 * @Date: 2019-12-19
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "药品合规规则分组")
@RestController
@RequestMapping("/medical/medicalDrugRuleGroup")
public class MedicalDrugRuleGroupController extends JeecgController<MedicalDrugRuleGroup, IMedicalDrugRuleGroupService> {
    @Autowired
    private IMedicalDrugRuleGroupService medicalDrugRuleGroupService;

    /**
     * 通过id查询
     *
     * @param kinds
     * @return
     */
    @AutoLog(value = "药品合规规则分组-通过kinds查询分组字典")
    @ApiOperation(value = "药品合规规则分组-通过kinds查询分组字典", notes = "药品合规规则分组-通过kinds查询分组字典")
    @GetMapping(value = "/queryGroupDictByKinds")
    public Result<?> queryGroupDictByKinds(@RequestParam(name = "kinds", required = true) String kinds) {
        Map<String, List<MedicalDrugRuleGroupDict>> map = medicalDrugRuleGroupService.getGroupDictMapByKinds(kinds.trim().split(","));
        return Result.ok(map);
    }

    /**
     * 列表查询不分页
     *
     * @param medicalDrugRuleGroup
     * @param req
     * @return
     */
    @AutoLog(value = "药品合规规则分组-列表查询不分页")
    @ApiOperation(value = "药品合规规则分组-列表查询不分页", notes = "药品合规规则分组-列表查询不分页")
    @GetMapping(value = "/queryList")
    public Result<?> queryList(MedicalDrugRuleGroup medicalDrugRuleGroup, HttpServletRequest req) {
        QueryWrapper<MedicalDrugRuleGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugRuleGroup, req.getParameterMap());
        return Result.ok(medicalDrugRuleGroupService.list(queryWrapper));
    }

    /**
     * 分页列表查询
     *
     * @param medicalDrugRuleGroup
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "药品合规规则分组-分页列表查询")
    @ApiOperation(value = "药品合规规则分组-分页列表查询", notes = "药品合规规则分组-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalDrugRuleGroup medicalDrugRuleGroup, MedicalDrugRuleGroupDel medicalDrugRuleGroupDel,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalDrugRuleGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugRuleGroup, req.getParameterMap());
        String inStr = "";
        if (StringUtils.isNotEmpty(medicalDrugRuleGroupDel.getCode())) {
            inStr += "  and CODE like '%" + medicalDrugRuleGroupDel.getCode() + "%'";
        }
        if (StringUtils.isNotEmpty(medicalDrugRuleGroupDel.getValue())) {
            inStr += "  and VALUE like '%" + medicalDrugRuleGroupDel.getValue() + "%'";
        }
        if (inStr.length() > 0) {
            queryWrapper.inSql("ID",
                    "SELECT GROUP_ID FROM MEDICAL_DRUG_RULE_GROUP_DEL where 1=1" + inStr);

        }
        Page<MedicalDrugRuleGroup> page = new Page<>(pageNo, pageSize);
        IPage<MedicalDrugRuleGroup> pageList = medicalDrugRuleGroupService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param medicalDrugRuleGroup
     * @return
     */
    @AutoLog(value = "药品合规规则分组-添加")
    @ApiOperation(value = "药品合规规则分组-添加", notes = "药品合规规则分组-添加")
    @PostMapping(value = "/add")
    public Result<?> add(MedicalDrugRuleGroup medicalDrugRuleGroup,String codes, String names) {
        medicalDrugRuleGroupService.saveGroup(medicalDrugRuleGroup,codes, names);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalDrugRuleGroup
     * @return
     */
    @AutoLog(value = "药品合规规则分组-编辑")
    @ApiOperation(value = "药品合规规则分组-编辑", notes = "药品合规规则分组-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(MedicalDrugRuleGroup medicalDrugRuleGroup,String codes, String names) {
        medicalDrugRuleGroupService.updateGroup(medicalDrugRuleGroup, codes, names);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "药品合规规则分组-通过id删除")
    @ApiOperation(value = "药品合规规则分组-通过id删除", notes = "药品合规规则分组-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        medicalDrugRuleGroupService.deleteGroup(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "药品合规规则分组-批量删除")
    @ApiOperation(value = "药品合规规则分组-批量删除", notes = "药品合规规则分组-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.medicalDrugRuleGroupService.deleteGroups(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "药品合规规则分组-通过id查询")
    @ApiOperation(value = "药品合规规则分组-通过id查询", notes = "药品合规规则分组-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalDrugRuleGroup medicalDrugRuleGroup = medicalDrugRuleGroupService.getById(id);
        return Result.ok(medicalDrugRuleGroup);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param medicalDrugRuleGroup
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MedicalDrugRuleGroup medicalDrugRuleGroup) {
        return super.exportXls(request, medicalDrugRuleGroup, MedicalDrugRuleGroup.class, "药品合规规则分组");
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
        return super.importExcel(request, response, MedicalDrugRuleGroup.class);
    }

}
