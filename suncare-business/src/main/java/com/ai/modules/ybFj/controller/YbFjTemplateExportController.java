package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.entity.YbFjTemplateExport;
import com.ai.modules.ybFj.service.IYbFjTemplateExportService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @Description: 文件导出模板信息
 * @Author: jeecg-boot
 * @Date: 2023-02-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "文件导出模板信息")
@RestController
@RequestMapping("/ybFj/ybFjTemplateExport")
public class YbFjTemplateExportController extends JeecgController<YbFjTemplateExport, IYbFjTemplateExportService> {
    @Autowired
    private IYbFjTemplateExportService ybFjTemplateService;

    /**
     * 分页列表查询
     *
     * @param ybFjTemplate
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "文件模板信息-分页列表查询")
    @ApiOperation(value = "文件模板信息-分页列表查询", notes = "文件模板信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(YbFjTemplateExport ybFjTemplate,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<YbFjTemplateExport> queryWrapper = QueryGenerator.initQueryWrapper(ybFjTemplate, req.getParameterMap());
        String templateCodes = req.getParameter("templateCodes");
        if (StringUtils.isNotBlank(templateCodes)) {
            queryWrapper.in("TEMPLATE_CODE", templateCodes.split(","));
        }
        Page<YbFjTemplateExport> page = new Page<YbFjTemplateExport>(pageNo, pageSize);
        IPage<YbFjTemplateExport> pageList = ybFjTemplateService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param ybFjTemplate
     * @return
     */
    @AutoLog(value = "文件模板信息-添加")
    @ApiOperation(value = "文件模板信息-添加", notes = "文件模板信息-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody YbFjTemplateExport ybFjTemplate) {
        ybFjTemplateService.add(ybFjTemplate);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param ybFjTemplate
     * @return
     */
    @AutoLog(value = "文件模板信息-编辑")
    @ApiOperation(value = "文件模板信息-编辑", notes = "文件模板信息-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody YbFjTemplateExport ybFjTemplate) {
        ybFjTemplateService.edit(ybFjTemplate);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "文件模板信息-通过id删除")
    @ApiOperation(value = "文件模板信息-通过id删除", notes = "文件模板信息-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        ybFjTemplateService.delete(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "文件模板信息-批量删除")
    @ApiOperation(value = "文件模板信息-批量删除", notes = "文件模板信息-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        ybFjTemplateService.deleteBatch(ids);
        return Result.ok("批量删除成功！");
    }

    /**
     * 判断是否重复
     *
     * @param request
     * @param templateName
     * @param id
     * @return
     */
    @AutoLog(value = "文件模板信息-判断名称是否重复 ")
    @ApiOperation(value = "文件模板信息-判断名称是否重复 ", notes = "文件模板信息-判断名称是否重复")
    @GetMapping(value = "/isExistName")
    public Result<?> isExistName(HttpServletRequest request, @RequestParam(name = "templateName", required = true) String templateName, String id) {
        Result<Boolean> result = new Result<>();
        QueryWrapper<YbFjTemplateExport> queryWrapper = new QueryWrapper<YbFjTemplateExport>();
        queryWrapper.eq("TEMPLATE_NAME", templateName);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.notIn("ID", id);
        }
        queryWrapper.eq("USE_STATUS", "1");//在用
        List<YbFjTemplateExport> list = this.ybFjTemplateService.list(queryWrapper);
        if (list.size() > 0) {
            result.setSuccess(false);
            result.setMessage("生成文件模板名称已存在");
            return result;
        }
        result.setSuccess(true);
        return result;
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "文件模板信息-通过id查询")
    @ApiOperation(value = "文件模板信息-通过id查询", notes = "文件模板信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        YbFjTemplateExport ybFjTemplate = ybFjTemplateService.getById(id);
        return Result.ok(ybFjTemplate);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ybFjTemplate
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YbFjTemplateExport ybFjTemplate) {
        return super.exportXls(request, ybFjTemplate, YbFjTemplateExport.class, "文件导出模板信息");
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
        return super.importExcel(request, response, YbFjTemplateExport.class);
    }

    @PostMapping(value = "/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile mf, HttpServletRequest request, HttpServletResponse response) {
        Result<?> result = new Result<>();
        try {
            String bizPath = request.getParameter("bizPath");
            String dbpath = ybFjTemplateService.upload(mf, bizPath);
            result.setMessage(dbpath);
            result.setSuccess(true);
        } catch (IOException e) {
            result.setSuccess(false);
            result.setCode(200);
            result.setMessage(e.getMessage());
            log.error(e.getMessage(), e);

            return Result.error(e.getMessage());
        }
        return result;
    }


}
