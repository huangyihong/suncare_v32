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
import com.ai.modules.ybChargeSearch.entity.YbChargeDwsInhospitalApart;
import com.ai.modules.ybChargeSearch.service.IYbChargeDwsInhospitalApartService;
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
 * @Description: 分解住院明细数据
 * @Author: jeecg-boot
 * @Date:   2023-06-15
 * @Version: V1.0
 */
@Slf4j
@Api(tags="分解住院明细数据")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeDwsInhospitalApart")
public class YbChargeDwsInhospitalApartController extends JeecgController<YbChargeDwsInhospitalApart, IYbChargeDwsInhospitalApartService> {
	@Autowired
	private IYbChargeDwsInhospitalApartService ybChargeDwsInhospitalApartService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeDwsInhospitalApart
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "分解住院明细数据-分页列表查询")
	@ApiOperation(value="分解住院明细数据-分页列表查询", notes="分解住院明细数据-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeDwsInhospitalApart ybChargeDwsInhospitalApart,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeDwsInhospitalApart> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDwsInhospitalApart, req.getParameterMap());
		Page<YbChargeDwsInhospitalApart> page = new Page<YbChargeDwsInhospitalApart>(pageNo, pageSize);
		IPage<YbChargeDwsInhospitalApart> pageList = ybChargeDwsInhospitalApartService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeDwsInhospitalApart
	 * @return
	 */
	@AutoLog(value = "分解住院明细数据-添加")
	@ApiOperation(value="分解住院明细数据-添加", notes="分解住院明细数据-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeDwsInhospitalApart ybChargeDwsInhospitalApart) {
		ybChargeDwsInhospitalApartService.save(ybChargeDwsInhospitalApart);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "分解住院明细数据-批量添加")
	 @ApiOperation(value="分解住院明细数据-批量添加", notes="分解住院明细数据-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeDwsInhospitalApart> addBatchList) throws Exception{
		 ybChargeDwsInhospitalApartService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }


	 /**
	 * 编辑
	 *
	 * @param ybChargeDwsInhospitalApart
	 * @return
	 */
	@AutoLog(value = "分解住院明细数据-编辑")
	@ApiOperation(value="分解住院明细数据-编辑", notes="分解住院明细数据-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeDwsInhospitalApart ybChargeDwsInhospitalApart) {
		ybChargeDwsInhospitalApartService.updateById(ybChargeDwsInhospitalApart);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "分解住院明细数据-通过id删除")
	@ApiOperation(value="分解住院明细数据-通过id删除", notes="分解住院明细数据-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeDwsInhospitalApartService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "分解住院明细数据-批量删除")
	@ApiOperation(value="分解住院明细数据-批量删除", notes="分解住院明细数据-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeDwsInhospitalApartService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "分解住院明细数据-通过id查询")
	@ApiOperation(value="分解住院明细数据-通过id查询", notes="分解住院明细数据-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeDwsInhospitalApart ybChargeDwsInhospitalApart = ybChargeDwsInhospitalApartService.getById(id);
		return Result.ok(ybChargeDwsInhospitalApart);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeDwsInhospitalApart
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeDwsInhospitalApart ybChargeDwsInhospitalApart) {
      return super.exportXls(request, ybChargeDwsInhospitalApart, YbChargeDwsInhospitalApart.class, "分解住院明细数据");
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
      return super.importExcel(request, response, YbChargeDwsInhospitalApart.class);
  }

}
