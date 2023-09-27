package com.ai.modules.ybChargeSearch.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybChargeSearch.entity.YbChargeOverproofResult;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybChargeSearch.entity.YbChargePatientRiskGroup;
import com.ai.modules.ybChargeSearch.service.IYbChargePatientRiskGroupService;
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
 * @Description: 患者异常情况汇总表
 * @Author: jeecg-boot
 * @Date:   2023-01-11
 * @Version: V1.0
 */
@Slf4j
@Api(tags="患者异常情况汇总表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargePatientRiskGroup")
public class YbChargePatientRiskGroupController extends JeecgController<YbChargePatientRiskGroup, IYbChargePatientRiskGroupService> {
	@Autowired
	private IYbChargePatientRiskGroupService ybChargePatientRiskGroupService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargePatientRiskGroup
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "患者异常情况汇总表-分页列表查询")
	@ApiOperation(value="患者异常情况汇总表-分页列表查询", notes="患者异常情况汇总表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargePatientRiskGroup ybChargePatientRiskGroup,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargePatientRiskGroup> queryWrapper = QueryGenerator.initQueryWrapper(ybChargePatientRiskGroup, req.getParameterMap());
		Page<YbChargePatientRiskGroup> page = new Page<YbChargePatientRiskGroup>(pageNo, pageSize);
		IPage<YbChargePatientRiskGroup> pageList = ybChargePatientRiskGroupService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargePatientRiskGroup
	 * @return
	 */
	@AutoLog(value = "患者异常情况汇总表-添加")
	@ApiOperation(value="患者异常情况汇总表-添加", notes="患者异常情况汇总表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargePatientRiskGroup ybChargePatientRiskGroup) {
		ybChargePatientRiskGroupService.save(ybChargePatientRiskGroup);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "患者异常情况汇总表-批量添加")
	 @ApiOperation(value="患者异常情况汇总表-批量添加", notes="患者异常情况汇总表-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargePatientRiskGroup> addBatchList) throws Exception{
		 ybChargePatientRiskGroupService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargePatientRiskGroup
	 * @return
	 */
	@AutoLog(value = "患者异常情况汇总表-编辑")
	@ApiOperation(value="患者异常情况汇总表-编辑", notes="患者异常情况汇总表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargePatientRiskGroup ybChargePatientRiskGroup) {
		ybChargePatientRiskGroupService.updateById(ybChargePatientRiskGroup);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "患者异常情况汇总表-通过id删除")
	@ApiOperation(value="患者异常情况汇总表-通过id删除", notes="患者异常情况汇总表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargePatientRiskGroupService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "患者异常情况汇总表-批量删除")
	@ApiOperation(value="患者异常情况汇总表-批量删除", notes="患者异常情况汇总表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargePatientRiskGroupService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "患者异常情况汇总表-通过id查询")
	@ApiOperation(value="患者异常情况汇总表-通过id查询", notes="患者异常情况汇总表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargePatientRiskGroup ybChargePatientRiskGroup = ybChargePatientRiskGroupService.getById(id);
		return Result.ok(ybChargePatientRiskGroup);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargePatientRiskGroup
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargePatientRiskGroup ybChargePatientRiskGroup) {
      return super.exportXls(request, ybChargePatientRiskGroup, YbChargePatientRiskGroup.class, "患者异常情况汇总表");
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
      return super.importExcel(request, response, YbChargePatientRiskGroup.class);
  }

}
