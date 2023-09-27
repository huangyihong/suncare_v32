package com.ai.modules.ybChargeSearch.controller;

import com.ai.modules.ybChargeSearch.entity.YbChargeDoctorRiskResult;
import com.ai.modules.ybChargeSearch.service.IYbChargeDoctorRiskResultService;
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
import java.util.List;

 /**
 * @Description: 医生异常情况明细表
 * @Author: jeecg-boot
 * @Date:   2023-02-09
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医生异常情况明细表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeDoctorRiskResult")
public class YbChargeDoctorRiskResultController extends JeecgController<YbChargeDoctorRiskResult, IYbChargeDoctorRiskResultService> {
	@Autowired
	private IYbChargeDoctorRiskResultService ybChargeDoctorRiskResultService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeDoctorRiskResult
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医生异常情况明细表-分页列表查询")
	@ApiOperation(value="医生异常情况明细表-分页列表查询", notes="医生异常情况明细表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeDoctorRiskResult ybChargeDoctorRiskResult,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeDoctorRiskResult> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDoctorRiskResult, req.getParameterMap());
		Page<YbChargeDoctorRiskResult> page = new Page<YbChargeDoctorRiskResult>(pageNo, pageSize);
		IPage<YbChargeDoctorRiskResult> pageList = ybChargeDoctorRiskResultService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeDoctorRiskResult
	 * @return
	 */
	@AutoLog(value = "医生异常情况明细表-添加")
	@ApiOperation(value="医生异常情况明细表-添加", notes="医生异常情况明细表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeDoctorRiskResult ybChargeDoctorRiskResult) {
		ybChargeDoctorRiskResultService.save(ybChargeDoctorRiskResult);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "医生异常情况明细表-批量添加")
	 @ApiOperation(value="医生异常情况明细表-批量添加", notes="医生异常情况明细表-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeDoctorRiskResult> addBatchList) throws Exception{
		 ybChargeDoctorRiskResultService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargeDoctorRiskResult
	 * @return
	 */
	@AutoLog(value = "医生异常情况明细表-编辑")
	@ApiOperation(value="医生异常情况明细表-编辑", notes="医生异常情况明细表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeDoctorRiskResult ybChargeDoctorRiskResult) {
		ybChargeDoctorRiskResultService.updateById(ybChargeDoctorRiskResult);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医生异常情况明细表-通过id删除")
	@ApiOperation(value="医生异常情况明细表-通过id删除", notes="医生异常情况明细表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeDoctorRiskResultService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医生异常情况明细表-批量删除")
	@ApiOperation(value="医生异常情况明细表-批量删除", notes="医生异常情况明细表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeDoctorRiskResultService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医生异常情况明细表-通过id查询")
	@ApiOperation(value="医生异常情况明细表-通过id查询", notes="医生异常情况明细表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeDoctorRiskResult ybChargeDoctorRiskResult = ybChargeDoctorRiskResultService.getById(id);
		return Result.ok(ybChargeDoctorRiskResult);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeDoctorRiskResult
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeDoctorRiskResult ybChargeDoctorRiskResult) {
      return super.exportXls(request, ybChargeDoctorRiskResult, YbChargeDoctorRiskResult.class, "医生异常情况明细表");
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
      return super.importExcel(request, response, YbChargeDoctorRiskResult.class);
  }

}
