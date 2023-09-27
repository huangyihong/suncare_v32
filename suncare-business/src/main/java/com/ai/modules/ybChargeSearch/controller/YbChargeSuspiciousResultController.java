package com.ai.modules.ybChargeSearch.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybChargeSearch.entity.YbChargeVisitTogetherResult;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybChargeSearch.entity.YbChargeSuspiciousResult;
import com.ai.modules.ybChargeSearch.service.IYbChargeSuspiciousResultService;
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
 * @Description: 可疑就诊标签汇总表
 * @Author: jeecg-boot
 * @Date:   2023-04-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="可疑就诊标签汇总表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeSuspiciousResult")
public class YbChargeSuspiciousResultController extends JeecgController<YbChargeSuspiciousResult, IYbChargeSuspiciousResultService> {
	@Autowired
	private IYbChargeSuspiciousResultService ybChargeSuspiciousResultService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeSuspiciousResult
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "可疑就诊标签汇总表-分页列表查询")
	@ApiOperation(value="可疑就诊标签汇总表-分页列表查询", notes="可疑就诊标签汇总表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeSuspiciousResult ybChargeSuspiciousResult,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeSuspiciousResult> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeSuspiciousResult, req.getParameterMap());
		Page<YbChargeSuspiciousResult> page = new Page<YbChargeSuspiciousResult>(pageNo, pageSize);
		IPage<YbChargeSuspiciousResult> pageList = ybChargeSuspiciousResultService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeSuspiciousResult
	 * @return
	 */
	@AutoLog(value = "可疑就诊标签汇总表-添加")
	@ApiOperation(value="可疑就诊标签汇总表-添加", notes="可疑就诊标签汇总表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeSuspiciousResult ybChargeSuspiciousResult) {
		ybChargeSuspiciousResultService.save(ybChargeSuspiciousResult);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "可疑就诊标签汇总表-批量添加")
	 @ApiOperation(value="可疑就诊标签汇总表-批量添加", notes="可疑就诊标签汇总表-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeSuspiciousResult> addBatchList) throws Exception{
		 ybChargeSuspiciousResultService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargeSuspiciousResult
	 * @return
	 */
	@AutoLog(value = "可疑就诊标签汇总表-编辑")
	@ApiOperation(value="可疑就诊标签汇总表-编辑", notes="可疑就诊标签汇总表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeSuspiciousResult ybChargeSuspiciousResult) {
		ybChargeSuspiciousResultService.updateById(ybChargeSuspiciousResult);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "可疑就诊标签汇总表-通过id删除")
	@ApiOperation(value="可疑就诊标签汇总表-通过id删除", notes="可疑就诊标签汇总表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeSuspiciousResultService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "可疑就诊标签汇总表-批量删除")
	@ApiOperation(value="可疑就诊标签汇总表-批量删除", notes="可疑就诊标签汇总表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeSuspiciousResultService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "可疑就诊标签汇总表-通过id查询")
	@ApiOperation(value="可疑就诊标签汇总表-通过id查询", notes="可疑就诊标签汇总表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeSuspiciousResult ybChargeSuspiciousResult = ybChargeSuspiciousResultService.getById(id);
		return Result.ok(ybChargeSuspiciousResult);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeSuspiciousResult
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeSuspiciousResult ybChargeSuspiciousResult) {
      return super.exportXls(request, ybChargeSuspiciousResult, YbChargeSuspiciousResult.class, "可疑就诊标签汇总表");
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
      return super.importExcel(request, response, YbChargeSuspiciousResult.class);
  }

}
