package com.ai.modules.ybFj.controller;

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
import com.ai.modules.ybFj.entity.YbFjXstj;
import com.ai.modules.ybFj.service.IYbFjXstjService;
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
 * @Description: 飞检项目线索统计
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目线索统计")
@RestController
@RequestMapping("/ybFj/ybFjXstj")
public class YbFjXstjController extends JeecgController<YbFjXstj, IYbFjXstjService> {
	@Autowired
	private IYbFjXstjService ybFjXstjService;
	
	/**
	 * 分页列表查询
	 *
	 * @param ybFjXstj
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目线索统计-分页列表查询")
	@ApiOperation(value="飞检项目线索统计-分页列表查询", notes="飞检项目线索统计-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbFjXstj ybFjXstj,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbFjXstj> queryWrapper = QueryGenerator.initQueryWrapper(ybFjXstj, req.getParameterMap());
		Page<YbFjXstj> page = new Page<YbFjXstj>(pageNo, pageSize);
		IPage<YbFjXstj> pageList = ybFjXstjService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param ybFjXstj
	 * @return
	 */
	@AutoLog(value = "飞检项目线索统计-添加")
	@ApiOperation(value="飞检项目线索统计-添加", notes="飞检项目线索统计-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbFjXstj ybFjXstj) {
		ybFjXstjService.save(ybFjXstj);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param ybFjXstj
	 * @return
	 */
	@AutoLog(value = "飞检项目线索统计-编辑")
	@ApiOperation(value="飞检项目线索统计-编辑", notes="飞检项目线索统计-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbFjXstj ybFjXstj) {
		ybFjXstjService.updateById(ybFjXstj);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "飞检项目线索统计-通过id删除")
	@ApiOperation(value="飞检项目线索统计-通过id删除", notes="飞检项目线索统计-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybFjXstjService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "飞检项目线索统计-批量删除")
	@ApiOperation(value="飞检项目线索统计-批量删除", notes="飞检项目线索统计-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybFjXstjService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "飞检项目线索统计-通过id查询")
	@ApiOperation(value="飞检项目线索统计-通过id查询", notes="飞检项目线索统计-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbFjXstj ybFjXstj = ybFjXstjService.getById(id);
		return Result.ok(ybFjXstj);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybFjXstj
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbFjXstj ybFjXstj) {
      return super.exportXls(request, ybFjXstj, YbFjXstj.class, "飞检项目线索统计");
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
      return super.importExcel(request, response, YbFjXstj.class);
  }

}
