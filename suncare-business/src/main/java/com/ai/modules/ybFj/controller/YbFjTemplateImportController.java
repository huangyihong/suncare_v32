package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.entity.YbFjTemplateImport;
import com.ai.modules.ybFj.service.IYbFjTemplateImportService;
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
import org.jeecg.common.system.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 导入文件模板信息
 * @Author: jeecg-boot
 * @Date: 2023-02-06
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "导入文件模板信息")
@RestController
@RequestMapping("/ybFj/ybFjTemplateImport")
public class YbFjTemplateImportController extends JeecgController<YbFjTemplateImport, IYbFjTemplateImportService> {
    @Autowired
    private IYbFjTemplateImportService ybFjTemplateImportService;

    /**
     * 分页列表查询
     *
     * @param ybFjTemplateImport
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "导入文件模板信息-分页列表查询")
    @ApiOperation(value = "导入文件模板信息-分页列表查询", notes = "导入文件模板信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(YbFjTemplateImport ybFjTemplateImport,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<YbFjTemplateImport> queryWrapper = QueryGenerator.initQueryWrapper(ybFjTemplateImport, req.getParameterMap());
        Page<YbFjTemplateImport> page = new Page<YbFjTemplateImport>(pageNo, pageSize);
        IPage<YbFjTemplateImport> pageList = ybFjTemplateImportService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param ybFjTemplateImport
     * @return
     */
    @AutoLog(value = "导入文件模板信息-添加")
    @ApiOperation(value = "导入文件模板信息-添加", notes = "导入文件模板信息-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody YbFjTemplateImport ybFjTemplateImport) {
        ybFjTemplateImport.setUseStatus("1");
        ybFjTemplateImportService.save(ybFjTemplateImport);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param ybFjTemplateImport
     * @return
     */
    @AutoLog(value = "导入文件模板信息-编辑")
    @ApiOperation(value = "导入文件模板信息-编辑", notes = "导入文件模板信息-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody YbFjTemplateImport ybFjTemplateImport) {
        ybFjTemplateImportService.updateById(ybFjTemplateImport);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "导入文件模板信息-通过id删除")
    @ApiOperation(value = "导入文件模板信息-通过id删除", notes = "导入文件模板信息-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        ybFjTemplateImportService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "导入文件模板信息-批量删除")
    @ApiOperation(value = "导入文件模板信息-批量删除", notes = "导入文件模板信息-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.ybFjTemplateImportService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "导入文件模板信息-通过id查询")
    @ApiOperation(value = "导入文件模板信息-通过id查询", notes = "导入文件模板信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        YbFjTemplateImport ybFjTemplateImport = ybFjTemplateImportService.getById(id);
        return Result.ok(ybFjTemplateImport);
    }

    /**
     * 判断是否重复
     *
     * @param request
     * @param importName
     * @param id
     * @return
     */
    @AutoLog(value = "导入文件模板信息-判断名称是否重复 ")
    @ApiOperation(value = "导入文件模板信息-判断名称是否重复 ", notes = "导入文件模板信息-判断名称是否重复")
    @GetMapping(value = "/isExistName")
    public Result<?> isExistName(HttpServletRequest request, @RequestParam(name = "importName", required = true) String importName, String id) {
        Result<Boolean> result = new Result<>();
        QueryWrapper<YbFjTemplateImport> queryWrapper = new QueryWrapper<YbFjTemplateImport>();
        queryWrapper.eq("IMPORT_NAME", importName);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.notIn("ID", id);
        }
        queryWrapper.eq("USE_STATUS", "1");//在用
        List<YbFjTemplateImport> list = this.ybFjTemplateImportService.list(queryWrapper);
        if (list.size() > 0) {
            result.setSuccess(false);
            result.setMessage("导入文件模板名称已存在");
            return result;
        }
        result.setSuccess(true);
        return result;
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ybFjTemplateImport
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YbFjTemplateImport ybFjTemplateImport) {
        return super.exportXls(request, ybFjTemplateImport, YbFjTemplateImport.class, "导入文件模板信息");
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
        return super.importExcel(request, response, YbFjTemplateImport.class);
    }


}
