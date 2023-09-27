package com.ai.modules.ybChargeSearch.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybChargeSearch.entity.YbChargeDwsInhospitalApart;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybChargeSearch.entity.YbChargeDoctorAdmitPatientInsick;
import com.ai.modules.ybChargeSearch.service.IYbChargeDoctorAdmitPatientInsickService;
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
 * @Description: 医生住院期间收治病人明细数据
 * @Author: jeecg-boot
 * @Date:   2023-06-15
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医生住院期间收治病人明细数据")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeDoctorAdmitPatientInsick")
public class YbChargeDoctorAdmitPatientInsickController extends JeecgController<YbChargeDoctorAdmitPatientInsick, IYbChargeDoctorAdmitPatientInsickService> {
	@Autowired
	private IYbChargeDoctorAdmitPatientInsickService ybChargeDoctorAdmitPatientInsickService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeDoctorAdmitPatientInsick
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医生住院期间收治病人明细数据-分页列表查询")
	@ApiOperation(value="医生住院期间收治病人明细数据-分页列表查询", notes="医生住院期间收治病人明细数据-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeDoctorAdmitPatientInsick ybChargeDoctorAdmitPatientInsick,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeDoctorAdmitPatientInsick> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDoctorAdmitPatientInsick, req.getParameterMap());
		Page<YbChargeDoctorAdmitPatientInsick> page = new Page<YbChargeDoctorAdmitPatientInsick>(pageNo, pageSize);
		IPage<YbChargeDoctorAdmitPatientInsick> pageList = ybChargeDoctorAdmitPatientInsickService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeDoctorAdmitPatientInsick
	 * @return
	 */
	@AutoLog(value = "医生住院期间收治病人明细数据-添加")
	@ApiOperation(value="医生住院期间收治病人明细数据-添加", notes="医生住院期间收治病人明细数据-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeDoctorAdmitPatientInsick ybChargeDoctorAdmitPatientInsick) {
		ybChargeDoctorAdmitPatientInsickService.save(ybChargeDoctorAdmitPatientInsick);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "医生住院期间收治病人明细数据-批量添加")
	 @ApiOperation(value="医生住院期间收治病人明细数据-批量添加", notes="医生住院期间收治病人明细数据-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeDoctorAdmitPatientInsick> addBatchList) throws Exception{
		 ybChargeDoctorAdmitPatientInsickService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargeDoctorAdmitPatientInsick
	 * @return
	 */
	@AutoLog(value = "医生住院期间收治病人明细数据-编辑")
	@ApiOperation(value="医生住院期间收治病人明细数据-编辑", notes="医生住院期间收治病人明细数据-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeDoctorAdmitPatientInsick ybChargeDoctorAdmitPatientInsick) {
		ybChargeDoctorAdmitPatientInsickService.updateById(ybChargeDoctorAdmitPatientInsick);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医生住院期间收治病人明细数据-通过id删除")
	@ApiOperation(value="医生住院期间收治病人明细数据-通过id删除", notes="医生住院期间收治病人明细数据-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeDoctorAdmitPatientInsickService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医生住院期间收治病人明细数据-批量删除")
	@ApiOperation(value="医生住院期间收治病人明细数据-批量删除", notes="医生住院期间收治病人明细数据-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeDoctorAdmitPatientInsickService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医生住院期间收治病人明细数据-通过id查询")
	@ApiOperation(value="医生住院期间收治病人明细数据-通过id查询", notes="医生住院期间收治病人明细数据-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeDoctorAdmitPatientInsick ybChargeDoctorAdmitPatientInsick = ybChargeDoctorAdmitPatientInsickService.getById(id);
		return Result.ok(ybChargeDoctorAdmitPatientInsick);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeDoctorAdmitPatientInsick
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeDoctorAdmitPatientInsick ybChargeDoctorAdmitPatientInsick) {
      return super.exportXls(request, ybChargeDoctorAdmitPatientInsick, YbChargeDoctorAdmitPatientInsick.class, "医生住院期间收治病人明细数据");
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
      return super.importExcel(request, response, YbChargeDoctorAdmitPatientInsick.class);
  }

}
