package com.ai.modules.dcmapping.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.dcmapping.entity.DcMappingResultManual;
import com.ai.modules.dcmapping.service.IDcMappingResultManualService;
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
 * @Description: 采集映射源表字段手工标记结果表
 * @Author: jeecg-boot
 * @Date:   2022-04-25
 * @Version: V1.0
 */
@Slf4j
@Api(tags="采集映射源表字段手工标记结果表")
@RestController
@RequestMapping("/dcmapping/dcMappingResultManual")
public class DcMappingResultManualController extends JeecgController<DcMappingResultManual, IDcMappingResultManualService> {
	@Autowired
	private IDcMappingResultManualService dcMappingResultManualService;

	/**
	 * 分页列表查询
	 *
	 * @param dcMappingResultManual
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "采集映射源表字段手工标记结果表-分页列表查询")
	@ApiOperation(value="采集映射源表字段手工标记结果表-分页列表查询", notes="采集映射源表字段手工标记结果表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(DcMappingResultManual dcMappingResultManual,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<DcMappingResultManual> queryWrapper = QueryGenerator.initQueryWrapper(dcMappingResultManual, req.getParameterMap());
		Page<DcMappingResultManual> page = new Page<DcMappingResultManual>(pageNo, pageSize);
		IPage<DcMappingResultManual> pageList = dcMappingResultManualService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param dcMappingResultManual
	 * @return
	 */
	@AutoLog(value = "采集映射源表字段手工标记结果表-添加")
	@ApiOperation(value="采集映射源表字段手工标记结果表-添加", notes="采集映射源表字段手工标记结果表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody DcMappingResultManual dcMappingResultManual) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		dcMappingResultManual.setCreatedBy(user.getUsername());
		dcMappingResultManual.setCreatedByName(user.getRealname());
		dcMappingResultManual.setCreatedTime(new Date());
		dcMappingResultManualService.save(dcMappingResultManual);
		return Result.ok(dcMappingResultManual);
	}

	/**
	 * 编辑
	 *
	 * @param dcMappingResultManual
	 * @return
	 */
	@AutoLog(value = "采集映射源表字段手工标记结果表-编辑")
	@ApiOperation(value="采集映射源表字段手工标记结果表-编辑", notes="采集映射源表字段手工标记结果表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody DcMappingResultManual dcMappingResultManual) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		dcMappingResultManual.setUpdatedBy(user.getUsername());
		dcMappingResultManual.setUpdatedByName(user.getRealname());
		dcMappingResultManual.setUpdatedTime(new Date());
		dcMappingResultManualService.updateById(dcMappingResultManual);
		return Result.ok(dcMappingResultManual);
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "采集映射源表字段手工标记结果表-通过id删除")
	@ApiOperation(value="采集映射源表字段手工标记结果表-通过id删除", notes="采集映射源表字段手工标记结果表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		dcMappingResultManualService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "采集映射源表字段手工标记结果表-批量删除")
	@ApiOperation(value="采集映射源表字段手工标记结果表-批量删除", notes="采集映射源表字段手工标记结果表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.dcMappingResultManualService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "采集映射源表字段手工标记结果表-通过id查询")
	@ApiOperation(value="采集映射源表字段手工标记结果表-通过id查询", notes="采集映射源表字段手工标记结果表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		DcMappingResultManual dcMappingResultManual = dcMappingResultManualService.getById(id);
		return Result.ok(dcMappingResultManual);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param dcMappingResultManual
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, DcMappingResultManual dcMappingResultManual) {
      return super.exportXls(request, dcMappingResultManual, DcMappingResultManual.class, "采集映射源表字段手工标记结果表");
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
      return super.importExcel(request, response, DcMappingResultManual.class);
  }

}
