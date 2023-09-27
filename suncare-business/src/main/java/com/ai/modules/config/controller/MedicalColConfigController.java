package com.ai.modules.config.controller;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 表字段配置
 * @Author: jeecg-boot
 * @Date: 2019-12-06
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "表字段配置")
@RestController
@RequestMapping("/config/medicalColConfig")
public class MedicalColConfigController extends JeecgController<MedicalColConfig, IMedicalColConfigService> {
    @Autowired
    private IMedicalColConfigService medicalColConfigService;

    @Autowired
    private IMedicalDictService medicalDictService;

    @Autowired
    IMedicalImportTaskService importTaskService;

    /**
     * 分页列表查询
     *
     * @param medicalColConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "表字段配置-分页列表查询")
    @ApiOperation(value = "表字段配置-分页列表查询", notes = "表字段配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalColConfig medicalColConfig,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<MedicalColConfig> queryWrapper = QueryGenerator.initQueryWrapper(medicalColConfig, req.getParameterMap());
        // 限制获取表为DATA中配置的表
        List<MedicalDictItemVO> listTable = medicalDictService.queryByType("DATA");
        List<String> tabNameList = new ArrayList<String>();
        for (MedicalDictItemVO item : listTable) {
            tabNameList.add(item.getCode());
        }
        queryWrapper.in("TAB_NAME", tabNameList);

        Page<MedicalColConfig> page = new Page<MedicalColConfig>(pageNo, pageSize);
        /*queryWrapper.orderByAsc("TAB_NAME");
        queryWrapper.orderByAsc("COL_NAME");*/
        IPage<MedicalColConfig> pageList = medicalColConfigService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 列表查询
     *
     * @param medicalColConfig
     * @param req
     * @return
     */
    @AutoLog(value = "表字段配置-列表查询")
    @ApiOperation(value = "表字段配置-列表查询", notes = "表字段配置-列表查询")
    @GetMapping(value = "/queryList")
    public Result<?> queryList(MedicalColConfig medicalColConfig, HttpServletRequest req) {
        QueryWrapper<MedicalColConfig> queryWrapper = QueryGenerator.initQueryWrapper(medicalColConfig, req.getParameterMap());
        List<MedicalColConfig> list = medicalColConfigService.list(queryWrapper);
        return Result.ok(list);
    }

    /**
     * 添加
     *
     * @param medicalColConfig
     * @return
     */
    @AutoLog(value = "表字段配置-添加")
    @ApiOperation(value = "表字段配置-添加", notes = "表字段配置-添加")
    @PostMapping(value = "/updateDisplayCol")
    public Result<?> updateDisplayCol(
                                      @RequestParam(name = "addIds") String addIds,
                                      @RequestParam(name = "delIds") String delIds) {
        List<MedicalColConfig> updateList = new ArrayList<>();
        if(StringUtils.isNotBlank(addIds)){
            List<MedicalColConfig> list = Arrays.stream(addIds.split(",")).map(id -> {
                    MedicalColConfig bean = new MedicalColConfig();
                bean.setId(id);
                bean.setIsDisplayCol(1);
                return bean;
            }).collect(Collectors.toList());
            updateList.addAll(list);
        }
        if(StringUtils.isNotBlank(delIds)){
            List<MedicalColConfig> list = Arrays.stream(delIds.split(",")).map(id -> {
                MedicalColConfig bean = new MedicalColConfig();
                bean.setId(id);
                bean.setIsDisplayCol(0);
                return bean;
            }).collect(Collectors.toList());
            updateList.addAll(list);
        }

        medicalColConfigService.updateBatchById(updateList);
        this.clearCache(updateList);
        return Result.ok("添加成功！");
    }

    /**
     * 添加
     *
     * @param medicalColConfig
     * @return
     */
    @AutoLog(value = "表字段配置-添加")
    @ApiOperation(value = "表字段配置-添加", notes = "表字段配置-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody MedicalColConfig bean) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        bean.setId(IdUtils.uuid());
        bean.setCreateStaff(user.getId());
        bean.setCreateTime(new Date());
        bean.setDataStatus("0");
        medicalColConfigService.save(bean);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param medicalColConfig
     * @return
     */
    @AutoLog(value = "表字段配置-编辑")
    @ApiOperation(value = "表字段配置-编辑", notes = "表字段配置-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody MedicalColConfig bean) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        bean.setUpdateStaff(user.getId());
        bean.setUpdateTime(new Date());
        this.clearCache(bean);
        medicalColConfigService.updateById(bean);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "表字段配置-通过id删除")
    @ApiOperation(value = "表字段配置-通过id删除", notes = "表字段配置-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        MedicalColConfig bean = medicalColConfigService.getById(id);
        this.clearCache(bean);
        medicalColConfigService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "表字段配置-批量删除")
    @ApiOperation(value = "表字段配置-批量删除", notes = "表字段配置-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        Collection<MedicalColConfig> list = medicalColConfigService.listByIds(idList);
        this.clearCache(list);
        this.medicalColConfigService.removeByIds(idList);
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "表字段配置-通过id查询")
    @ApiOperation(value = "表字段配置-通过id查询", notes = "表字段配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        MedicalColConfig medicalColConfig = medicalColConfigService.getById(id);
        return Result.ok(medicalColConfig);
    }

    @AutoLog(value = "表字段配置-通过表名字段名查询")
    @ApiOperation(value = "表字段配置-通过表名字段名查询", notes = "表字段配置-通过表名字段名查询")
    @GetMapping(value = "/queryByCol")
    public Result<?> queryByCol(@RequestParam(name = "tableName") String tableName,
                                @RequestParam(name = "colName") String colName) {
        MedicalColConfig medicalColConfig = medicalColConfigService.getOne(new QueryWrapper<MedicalColConfig>()
                .eq("TAB_NAME", tableName)
                .eq("COL_NAME", colName)
        );
        return Result.ok(medicalColConfig);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param medicalColConfig
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, MedicalColConfig medicalColConfig) {
        return super.exportXls(request, medicalColConfig, MedicalColConfig.class, "表字段配置");
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
            return importTaskService.saveImportTask("MEDICAL_COL_CONFIG","表字段配置导入",file,user,
                    (f,u)->{
                        try {
                            return this.medicalColConfigService.importExcel(f,u);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Result.error(e.getMessage());
                        }
                    });

        }
        return Result.error("上传文件为空");
    }

    /**
     * 获取表的实体字段
     *
     * @return
     */
    @AutoLog(value = "表字段配置-获取表的实体字段")
    @ApiOperation(value = "流程图-获取表的实体字段", notes = "流程图-获取表的实体字段")
    @GetMapping(value = "/queryEntityColByTable")
    public Result<?> queryEntityColByTable(@RequestParam(name = "tableName") String tableName) {
        QueryWrapper<MedicalColConfig> queryWrapper = new QueryWrapper<MedicalColConfig>()
                .eq("TAB_NAME", tableName)
                .eq("COL_TYPE", "1")
                .select("COL_CHN_NAME", "COL_NAME", "COL_DESC");
        return Result.ok(medicalColConfigService.list(queryWrapper));
    }


    /**
     * 获取配置规则需要的字典与字段
     *
     * @return
     */
    @AutoLog(value = "流程图-获取配置规则需要的字典与字段")
    @ApiOperation(value = "流程图-获取配置规则需要的字典与字段", notes = "流程图-获取配置规则需要的字典与字段")
    @GetMapping(value = "/getRuleColConfig")
    public Result<?> getColConfig(@RequestParam(name = "tableStartWidth", required = false) String tableStartWidth) {
        JSONObject json = new JSONObject();
        json.put("types", medicalDictService.queryByType("RULE_COL_VALUE_TYPE_WHERE"));
        json.put("cols",medicalColConfigService.getRuleColConfig(tableStartWidth));

        return Result.ok(json);
    }

    /**
     * 通过表名获取配置规则需要字段
     *
     * @return
     */
    @AutoLog(value = "流程图-通过表名获取配置规则需要字段")
    @ApiOperation(value = "流程图-通过表名获取配置规则需要字段", notes = "流程图-通过表名获取配置规则需要字段")
    @GetMapping(value = "/getRuleColConfigByTable")
    public Result<?> getRuleColConfigByTable(@RequestParam(name = "tableName") String tableName) {

        return Result.ok(medicalColConfigService.getRuleColConfig(tableName));
    }

    /**
     * 获取打分需要的字段
     *
     * @return
     */
    @AutoLog(value = "流程图-获取打分需要的字段")
    @ApiOperation(value = "流程图-获取打分需要的字段", notes = "流程图-获取打分需要的字段")
    @GetMapping(value = "/getGradeColConfig")
    public Result<?> getGradeColConfig() {
        return Result.ok(medicalColConfigService.getGradeColConfig());
    }

    /**
     * 获取统计（分组）需要的字段
     *
     * @return
     */
    @AutoLog(value = "流程图-获取统计（分组）需要的字段")
    @ApiOperation(value = "流程图-获取统计（分组）需要的字段", notes = "流程图-获取统计（分组）需要的字段")
    @GetMapping(value = "/getGroupByColConfig")
    public Result<?> getGroupByColConfig() {
        return Result.ok(medicalColConfigService.getGroupByColConfig());
    }

    /**
     * 判断同一张表字段是否重复
     *
     * @param request
     * @param tabName
     * @param colName
     * @param id
     * @return
     */
    @AutoLog(value = "表字段配置-判断同一张表字段是否重复 ")
    @ApiOperation(value = "表字段配置-判断同一张表字段是否重复 ", notes = "表字段配置-判断同一张表字段是否重复 ")
    @GetMapping(value = "/isExist")
    public Result<?> isExist(HttpServletRequest request, @RequestParam(name = "tabName", required = true) String tabName, @RequestParam(name = "colName", required = true) String colName, String id) {
        boolean flag = medicalColConfigService.isExist(tabName, colName, id);
        return Result.ok(flag);
    }

    /**
     * 直接导出excel
     *
     * @param req
     * @param response
     * @param medicalColConfig
     * @throws Exception
     */
    @RequestMapping(value = "/exportExcel")
    public void exportExcel(HttpServletRequest req, HttpServletResponse response, MedicalColConfig medicalColConfig) throws Exception {
        Result<?> result = new Result<>();
        String title = req.getParameter("title");
        if (StringUtils.isBlank(title)) {
            title = "数仓库表字段_导出";
        }
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
        try {
            OutputStream os = response.getOutputStream();
            // 选中数据
            String selections = req.getParameter("selections");
            if (StringUtils.isNotEmpty(selections)) {
                medicalColConfig.setId(selections);
            }
            QueryWrapper<MedicalColConfig> queryWrapper = QueryGenerator.initQueryWrapper(medicalColConfig, req.getParameterMap());
            // 限制获取表为DATA中配置的表
            List<MedicalDictItemVO> listTable = medicalDictService.queryByType("DATA");
            List<String> tabNameList = new ArrayList<String>();
            for (MedicalDictItemVO item : listTable) {
                tabNameList.add(item.getCode());
            }
            queryWrapper.in("TAB_NAME", tabNameList);

            List<MedicalColConfig> list = medicalColConfigService.list(queryWrapper);
            medicalColConfigService.exportExcel(list, os);
        } catch (Exception e) {
            throw e;
        }
    }

    private void clearCache(Collection<MedicalColConfig> list){
        for(MedicalColConfig bean: list){
            this.clearCache(bean);
        }
    }

    private void clearCache(MedicalColConfig bean){
        medicalColConfigService.clearCacheByCol(bean.getColName(), bean.getTabName());
    }

 /*   @AutoLog(value = "表字段配置-清除缓存")
    @ApiOperation(value = "表字段配置-清除缓存", notes = "表字段配置-清除缓存")
    @GetMapping(value = "/clearCache")
    public Result<?> clearCache() {
        medicalColConfigService.clearCache();
        return Result.ok("清除缓存成功！");
    }

    @AutoLog(value = "表字段配置-按字段清除缓存")
    @ApiOperation(value = "表字段配置-按字段清除缓存", notes = "表字段配置-按字段清除缓存")
    @GetMapping(value = "/clearCacheByCol")
    public Result<?> clearCacheByCol(@RequestParam(name = "tableName") String tableName,
    		@RequestParam(name = "colName") String colName) {
        medicalColConfigService.clearCacheByCol(colName, tableName);
        return Result.ok("清除缓存成功！");
    }*/
}
