package com.ai.modules.statistics.controller;

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
import com.ai.modules.statistics.entity.StaReportGroup;
import com.ai.modules.statistics.service.IStaReportGroupService;
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
 * @Description: 报表分组定义表
 * @Author: jeecg-boot
 * @Date:   2020-08-21
 * @Version: V1.0
 */
@Slf4j
@Api(tags="报表分组定义表")
@RestController
@RequestMapping("/statistics/staReportGroup")
public class StaReportGroupController extends JeecgController<StaReportGroup, IStaReportGroupService> {
	@Autowired
	private IStaReportGroupService staReportGroupService;
	
	/**
	 * 分页列表查询
	 *
	 * @param staReportGroup
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "报表分组定义表-分页列表查询")
	@ApiOperation(value="报表分组定义表-分页列表查询", notes="报表分组定义表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(StaReportGroup staReportGroup,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<StaReportGroup> queryWrapper = QueryGenerator.initQueryWrapper(staReportGroup, req.getParameterMap());
		Page<StaReportGroup> page = new Page<StaReportGroup>(pageNo, pageSize);
		IPage<StaReportGroup> pageList = staReportGroupService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param staReportGroup
	 * @return
	 */
	@AutoLog(value = "报表分组定义表-添加")
	@ApiOperation(value="报表分组定义表-添加", notes="报表分组定义表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody StaReportGroup staReportGroup) {
		staReportGroupService.save(staReportGroup);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param staReportGroup
	 * @return
	 */
	@AutoLog(value = "报表分组定义表-编辑")
	@ApiOperation(value="报表分组定义表-编辑", notes="报表分组定义表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody StaReportGroup staReportGroup) {
		staReportGroupService.updateById(staReportGroup);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "报表分组定义表-通过id删除")
	@ApiOperation(value="报表分组定义表-通过id删除", notes="报表分组定义表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		staReportGroupService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "报表分组定义表-批量删除")
	@ApiOperation(value="报表分组定义表-批量删除", notes="报表分组定义表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.staReportGroupService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "报表分组定义表-通过id查询")
	@ApiOperation(value="报表分组定义表-通过id查询", notes="报表分组定义表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		StaReportGroup staReportGroup = staReportGroupService.getById(id);
		return Result.ok(staReportGroup);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param staReportGroup
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, StaReportGroup staReportGroup) {
      return super.exportXls(request, staReportGroup, StaReportGroup.class, "报表分组定义表");
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
      return super.importExcel(request, response, StaReportGroup.class);
  }

}
