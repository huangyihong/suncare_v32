package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.entity.YbFjOcr;
import com.ai.modules.ybFj.service.IYbFjOcrService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: OCR识别工具
 * @Author: jeecg-boot
 * @Date: 2023-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "OCR识别工具")
@RestController
@RequestMapping("/ybFj/ybFjOcr")
public class YbFjOcrController extends JeecgController<YbFjOcr, IYbFjOcrService> {
    @Autowired
    private IYbFjOcrService ybFjOcrService;

    /**
     * 分页列表查询
     *
     * @param ybFjOcr
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "OCR识别工具-分页列表查询")
    @ApiOperation(value = "OCR识别工具-分页列表查询", notes = "OCR识别工具-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(YbFjOcr ybFjOcr,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<YbFjOcr> queryWrapper = QueryGenerator.initQueryWrapper(ybFjOcr, req.getParameterMap());
        Page<YbFjOcr> page = new Page<YbFjOcr>(pageNo, pageSize);
        IPage<YbFjOcr> pageList = ybFjOcrService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param ybFjOcr
     * @return
     */
    @AutoLog(value = "OCR识别工具-添加")
    @ApiOperation(value = "OCR识别工具-添加", notes = "OCR识别工具-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody YbFjOcr ybFjOcr) {
        ybFjOcrService.add(ybFjOcr);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param ybFjOcr
     * @return
     */
    @AutoLog(value = "OCR识别工具-编辑")
    @ApiOperation(value = "OCR识别工具-编辑", notes = "OCR识别工具-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody YbFjOcr ybFjOcr) {
        ybFjOcrService.edit(ybFjOcr);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "OCR识别工具-通过id删除")
    @ApiOperation(value = "OCR识别工具-通过id删除", notes = "OCR识别工具-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        ybFjOcrService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "OCR识别工具-批量删除")
    @ApiOperation(value = "OCR识别工具-批量删除", notes = "OCR识别工具-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.ybFjOcrService.delByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "OCR识别工具-通过id查询")
    @ApiOperation(value = "OCR识别工具-通过id查询", notes = "OCR识别工具-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        YbFjOcr ybFjOcr = ybFjOcrService.getById(id);
        return Result.ok(ybFjOcr);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param ybFjOcr
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YbFjOcr ybFjOcr) {
        return super.exportXls(request, ybFjOcr, YbFjOcr.class, "OCR识别工具");
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
        return super.importExcel(request, response, YbFjOcr.class);
    }


    /**
     * 文件批量下载
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "OCR识别工具-文件批量下载")
    @ApiOperation(value = "OCR识别工具-文件批量下载", notes = "OCR识别工具-文件批量下载")
    @GetMapping(value = "/downloadZip")
    public Result<?> downloadZip(@RequestParam(name = "ids", required = true) String ids) throws Exception {
        try {
            String zipPath = ybFjOcrService.downloadZip(ids);
            return Result.ok(zipPath);
        } catch (Exception e) {
            return Result.error("打包文件批量下载失败");
        }
    }

}
