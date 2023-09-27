package com.ai.modules.ybChargeSearch.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchResult;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchResultService;
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
 * @Description: 收费明细查询结果表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Slf4j
@Api(tags="收费明细查询结果表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeSearchResult")
public class YbChargeSearchResultController extends JeecgController<YbChargeSearchResult, IYbChargeSearchResultService> {
	@Autowired
	private IYbChargeSearchResultService ybChargeSearchResultService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeSearchResult
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "收费明细查询结果表-分页列表查询")
	@ApiOperation(value="收费明细查询结果表-分页列表查询", notes="收费明细查询结果表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeSearchResult ybChargeSearchResult,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeSearchResult> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeSearchResult, req.getParameterMap());
		Page<YbChargeSearchResult> page = new Page<YbChargeSearchResult>(pageNo, pageSize);
		IPage<YbChargeSearchResult> pageList = ybChargeSearchResultService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeSearchResult
	 * @return
	 */
	@AutoLog(value = "收费明细查询结果表-添加")
	@ApiOperation(value="收费明细查询结果表-添加", notes="收费明细查询结果表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeSearchResult ybChargeSearchResult) {
		ybChargeSearchResultService.save(ybChargeSearchResult);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "收费明细查询结果表-批量添加")
	 @ApiOperation(value="收费明细查询结果表-批量添加", notes="收费明细查询结果表-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeSearchResult> addBatchList) throws Exception{
		 ybChargeSearchResultService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargeSearchResult
	 * @return
	 */
	@AutoLog(value = "收费明细查询结果表-编辑")
	@ApiOperation(value="收费明细查询结果表-编辑", notes="收费明细查询结果表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeSearchResult ybChargeSearchResult) {
		ybChargeSearchResultService.updateById(ybChargeSearchResult);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "收费明细查询结果表-通过id删除")
	@ApiOperation(value="收费明细查询结果表-通过id删除", notes="收费明细查询结果表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeSearchResultService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "收费明细查询结果表-批量删除")
	@ApiOperation(value="收费明细查询结果表-批量删除", notes="收费明细查询结果表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeSearchResultService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "收费明细查询结果表-通过id查询")
	@ApiOperation(value="收费明细查询结果表-通过id查询", notes="收费明细查询结果表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeSearchResult ybChargeSearchResult = ybChargeSearchResultService.getById(id);
		return Result.ok(ybChargeSearchResult);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeSearchResult
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeSearchResult ybChargeSearchResult) {
      return super.exportXls(request, ybChargeSearchResult, YbChargeSearchResult.class, "收费明细查询结果表");
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
      return super.importExcel(request, response, YbChargeSearchResult.class);
  }

}
