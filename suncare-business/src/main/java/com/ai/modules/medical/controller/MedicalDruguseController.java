package com.ai.modules.medical.controller;

import com.ai.common.utils.MD5Util;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.service.IMedicalDruguseService;
import com.ai.modules.medical.vo.MedicalDruguseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 合理用药配置
 * @Author: jeecg-boot
 * @Date: 2020-11-05
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "合理用药配置")
@RestController
@RequestMapping("/medical/medicalDruguse")
public class MedicalDruguseController extends JeecgController<MedicalDruguse, IMedicalDruguseService> {
    @Autowired
    private IMedicalDruguseService medicalDruguseService;

    /**
     * 分页列表查询
     *
     * @param medicalDruguse
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "合理用药配置-分页列表查询")
    @ApiOperation(value = "合理用药配置-分页列表查询", notes = "合理用药配置-分页列表查询")
    @RequestMapping(value = "/list",method = { RequestMethod.GET,RequestMethod.POST })
    public Result<?> queryPageList(MedicalDruguse medicalDruguse,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   String actionTime,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalDruguse> queryWrapper = QueryGenerator.initQueryWrapper(medicalDruguse, req.getParameterMap());
        if(StringUtils.isNotBlank(actionTime)){
            String sql = "SELECT RULE_ID FROM MEDICAL_DRUGUSE WHERE "
//                    + "to_char(CREATE_TIME,'yyyy-MM-dd HH24:MI:SS') like '%" + actionTime + "%'"
                    + "DATE_FORMAT(CREATE_TIME,'%Y-%m-%d %H:%i:%S') like '%" + actionTime + "%'"
//                    + " OR " + "to_char(UPDATE_TIME,'yyyy-MM-dd HH24:MI:SS') like '%" + actionTime + "%'";
                    + " OR " + "DATE_FORMAT(UPDATE_TIME,'%Y-%m-%d %H:%i:%S') like '%" + actionTime + "%'";
            queryWrapper.inSql("RULE_ID", sql);
        }
        Page<MedicalDruguse> page = new Page<MedicalDruguse>(pageNo, pageSize);
        IPage<MedicalDruguse> pageList = medicalDruguseService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @AutoLog(value = "医疗机构信息-列表全选")
    @ApiOperation(value = "医疗机构信息-列表全选", notes = "医疗机构信息-列表全选")
    @GetMapping(value = "/selectAll")
    public Result<?> selectAll(MedicalDruguse medicalDruguse,
                               HttpServletRequest req) {
        QueryWrapper<MedicalDruguse> queryWrapper = QueryGenerator.initQueryWrapper(medicalDruguse, req.getParameterMap());
        queryWrapper.select("RULE_ID ID", "ITEM_NAMES NAME");
        List<Map<String, Object>> list = medicalDruguseService.listMaps(queryWrapper);
        return Result.ok(list);
    }

    /**
     * 添加
     *
     * @param medicalDruguse
     * @return
     */
    @AutoLog(value = "合理用药配置-添加")
    @ApiOperation(value = "合理用药配置-添加", notes = "合理用药配置-添加")
    @PostMapping(value = "/add")
    @Transactional
    public Result<?> add(@RequestBody MedicalDruguseVO medicalDruguse) {
        // 构造每个药品的规则编码
        String ruleCodeBase = "_" + medicalDruguse.getActionId()
                + "_" + medicalDruguse.getSex()
                + "_" + medicalDruguse.getAge()
                + "_" + medicalDruguse.getAgeUnit();
        String[] itemCodes = medicalDruguse.getItemCodes().split(",");
        List<String> codes = Arrays.stream(itemCodes)
                .map(r -> MD5Util.getMD5(r + ruleCodeBase)).collect(Collectors.toList());
        // 验证所有的规则编码存在情况
        List<Map<String, Object>> listMaps = medicalDruguseService.listMaps(
                new QueryWrapper<MedicalDruguse>()
                        .select("ITEM_CODES")
                        .in("RULE_CODE", codes)
        );
        if(listMaps.size() > 0){
            String existCodes = listMaps.stream().map(r -> r.get("ITEM_CODES").toString()).collect(Collectors.joining(","));
            return Result.error("药品规则已存在，药品：" + existCodes);
        }

        // 遍历存储每个药品
        String[] itemNames = medicalDruguse.getItemNames().split(",");
        int index = 0;
        for(String code: codes){
            medicalDruguse.setRuleCode(code);
            medicalDruguse.setItemCodes(itemCodes[index]);
            medicalDruguse.setItemNames(itemNames[index]);
            medicalDruguseService.save(medicalDruguse, medicalDruguse.getRuleGroups());
            index++;
        }
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalDruguse
     * @return
     */
    @AutoLog(value = "合理用药配置-编辑")
    @ApiOperation(value = "合理用药配置-编辑", notes = "合理用药配置-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalDruguseVO medicalDruguse) {
        // 构造药品的规则编码
        String ruleCode = MD5Util.getMD5(
                medicalDruguse.getItemCodes()
                        + "_" + dealEmpty(medicalDruguse.getActionId())
                        + "_" + dealEmpty(medicalDruguse.getSex())
                        + "_" + dealEmpty(medicalDruguse.getAge())
                        + "_" + dealEmpty(medicalDruguse.getAgeUnit())
        );

        log.info(medicalDruguse.getItemCodes()
                + "_" + dealEmpty(medicalDruguse.getActionId())
                + "_" + dealEmpty(medicalDruguse.getSex())
                + "_" + dealEmpty(medicalDruguse.getAge())
                + "_" + dealEmpty(medicalDruguse.getAgeUnit()));

        int ruleCodeCount = medicalDruguseService.count(new QueryWrapper<MedicalDruguse>()
                .eq("RULE_CODE", ruleCode)
                .ne("RULE_ID", medicalDruguse.getRuleId())
        );

        if(ruleCodeCount > 0){
            return Result.error("药品规则已存在");
        }
        medicalDruguse.setRuleCode(ruleCode);
        medicalDruguseService.updateById(medicalDruguse, medicalDruguse.getRuleGroups());
        return Result.ok("编辑成功!");
    }

    private String dealEmpty(String obj){
        return StringUtils.isBlank(obj)?"":obj;

    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "合理用药配置-通过id删除")
    @ApiOperation(value = "合理用药配置-通过id删除", notes = "合理用药配置-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        medicalDruguseService.removeById(id);
        medicalDruguseService.delRuleGroup(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "合理用药配置-批量删除")
    @ApiOperation(value = "合理用药配置-批量删除", notes = "合理用药配置-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        this.medicalDruguseService.removeByIds(idList);
        this.medicalDruguseService.delRuleGroup(idList);
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "合理用药配置-通过id查询")
    @ApiOperation(value = "合理用药配置-通过id查询", notes = "合理用药配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalDruguse medicalDruguse = medicalDruguseService.getById(id);
        return Result.ok(medicalDruguse);
    }

    /**
     * 导出excel
     *
     * @param req
     * @param response
     * @param medicalDruguse
     */
    @RequestMapping(value = "/exportXls")
    public Result<?> exportXls(HttpServletRequest req, HttpServletResponse response, MedicalDruguse medicalDruguse) throws Exception {
        QueryWrapper<MedicalDruguse> queryWrapper = QueryGenerator.initQueryWrapper(medicalDruguse, req.getParameterMap());

        long count = this.medicalDruguseService.count(queryWrapper);
        String title = "用药合理配置";

        if (count > 5000) {
            ThreadUtils.EXPORT_POOL.add(title + "_导出", "xlsx", (int) count, (os) -> {
                try {
                    this.medicalDruguseService.exportExcel(queryWrapper, os);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.error(e.getMessage());
                }
                return Result.ok();
            });
            return Result.ok("等待导出");
        }
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "_导出" + System.currentTimeMillis() + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        OutputStream os = response.getOutputStream();
        this.medicalDruguseService.exportExcel(queryWrapper, os);
        return null;
//      return super.exportXls(request, medicalDruguse, MedicalDruguse.class, "合理用药配置");
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
//      return super.importExcel(request, response, MedicalDruguse.class);
        int[] rowCounts = new int[2];

        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                long beginTime = System.currentTimeMillis();
                int[] nums = this.medicalDruguseService.importExcel(file);
                long endTime = System.currentTimeMillis();

                log.info("[" + name + "]导入时间：" + (endTime - beginTime) / 1000 + "秒");

                for (int i = 0; i < rowCounts.length; i++) {
                    rowCounts[i] += nums[i];
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        }

        return Result.ok("操作成功，新增 " + rowCounts[0] + " 条记录，更新" + rowCounts[1] + " 条记录");

    }

    @RequestMapping(value = "/exportInvalidXls")
    public Result<?> exportInvalidXls(HttpServletRequest req, HttpServletResponse response, MedicalDruguse medicalDruguse) throws Exception {
        QueryWrapper<MedicalDruguse> queryWrapper = QueryGenerator.initQueryWrapper(medicalDruguse, req.getParameterMap());

        long count = this.medicalDruguseService.count(queryWrapper);
        String title = "用药合理配置失效明细";

        if (count > 3000) {
            ThreadUtils.EXPORT_POOL.add(title + "_导出", "xlsx", (int) count, (os) -> {
                try {
                    this.medicalDruguseService.exportInvalid(queryWrapper, os);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.error(e.getMessage());
                }
                return Result.ok();
            });
            return Result.ok("等待导出");
        }
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "_导出" + System.currentTimeMillis() + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        OutputStream os = response.getOutputStream();
        this.medicalDruguseService.exportInvalid(queryWrapper, os);
        return null;
//      return super.exportXls(request, medicalDruguse, MedicalDruguse.class, "合理用药配置");
    }

}
