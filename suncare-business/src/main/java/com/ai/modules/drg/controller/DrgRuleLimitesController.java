package com.ai.modules.drg.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgCatalogDetail;
import com.ai.modules.drg.service.IDrgCatalogDetailService;
import com.ai.modules.drg.service.IDrgCatalogService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.drg.service.IDrgRuleLimitesService;
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
 * @Description: drg规则限定条件表
 * @Author: jeecg-boot
 * @Date:   2023-05-08
 * @Version: V1.0
 */
@Slf4j
@Api(tags="drg规则限定条件表")
@RestController
@RequestMapping("/drg/drgRuleLimites")
public class DrgRuleLimitesController extends JeecgController<DrgRuleLimites, IDrgRuleLimitesService> {
	@Autowired
	private IDrgRuleLimitesService drgRuleLimitesService;
	 @Autowired
	 private IDrgCatalogDetailService drgCatalogDetailService;
	 @Autowired
	 private IDrgCatalogService drgCatalogService;

	/**
	 * 分页列表查询
	 *
	 * @param drgRuleLimites
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "drg规则限定条件表-分页列表查询")
	@ApiOperation(value="drg规则限定条件表-分页列表查询", notes="drg规则限定条件表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(DrgRuleLimites drgRuleLimites,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<DrgRuleLimites> queryWrapper = QueryGenerator.initQueryWrapper(drgRuleLimites, req.getParameterMap());
		Page<DrgRuleLimites> page = new Page<DrgRuleLimites>(pageNo, pageSize);
		IPage<DrgRuleLimites> pageList = drgRuleLimitesService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param drgRuleLimites
	 * @return
	 */
	@AutoLog(value = "drg规则限定条件表-添加")
	@ApiOperation(value="drg规则限定条件表-添加", notes="drg规则限定条件表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody DrgRuleLimites drgRuleLimites) {
		drgRuleLimitesService.save(drgRuleLimites);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param drgRuleLimites
	 * @return
	 */
	@AutoLog(value = "drg规则限定条件表-编辑")
	@ApiOperation(value="drg规则限定条件表-编辑", notes="drg规则限定条件表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody DrgRuleLimites drgRuleLimites) {
		drgRuleLimitesService.updateById(drgRuleLimites);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "drg规则限定条件表-通过id删除")
	@ApiOperation(value="drg规则限定条件表-通过id删除", notes="drg规则限定条件表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		drgRuleLimitesService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "drg规则限定条件表-批量删除")
	@ApiOperation(value="drg规则限定条件表-批量删除", notes="drg规则限定条件表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.drgRuleLimitesService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "drg规则限定条件表-通过id查询")
	@ApiOperation(value="drg规则限定条件表-通过id查询", notes="drg规则限定条件表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		DrgRuleLimites drgRuleLimites = drgRuleLimitesService.getById(id);
		return Result.ok(drgRuleLimites);
	}

	 /**
	  * 通过目录编码查询
	  *
	  * @param catalogCode
	  * @return
	  */
	 @AutoLog(value = "drg规则限定条件表-通过drg目录id查询")
	 @ApiOperation(value="drg规则限定条件表-通过drg目录id查询", notes="drg规则限定条件表-通过drg目录id查询")
	 @GetMapping(value = "/queryByCatalogCode")
	 public Result<?> queryByCatalogCode(@RequestParam(name="catalogType",required=true) String catalogType,
										 @RequestParam(name="catalogCode",required=true) String catalogCode,
										 @RequestParam(name="versionCode",required=true) String versionCode) {
		 QueryWrapper<DrgRuleLimites> queryWrapper = new QueryWrapper<DrgRuleLimites>();
		 queryWrapper.eq("CATALOG_TYPE",catalogType);
		 queryWrapper.eq("CATALOG_CODE",catalogCode);
		 queryWrapper.eq("VERSION_CODE",versionCode);
		 queryWrapper.orderByAsc("SEQ");
		 List<DrgRuleLimites> list = this.drgRuleLimitesService.list(queryWrapper);
		 return Result.ok(list);
	 }

  /**
   * 导出excel
   *
   * @param request
   * @param drgRuleLimites
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, DrgRuleLimites drgRuleLimites) {
      return super.exportXls(request, drgRuleLimites, DrgRuleLimites.class, "drg规则限定条件表");
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
      return super.importExcel(request, response, DrgRuleLimites.class);
  }

}
