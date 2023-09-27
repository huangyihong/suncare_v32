package com.ai.modules.ybChargeSearch.controller;

import com.ai.modules.ybChargeSearch.entity.YbChargeDoctorRiskGroup;
import com.ai.modules.ybChargeSearch.service.IYbChargeDoctorRiskGroupService;
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
 * @Description: 医生异常情况汇总表
 * @Author: jeecg-boot
 * @Date:   2023-02-09
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医生异常情况汇总表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeDoctorRiskGroup")
public class YbChargeDoctorRiskGroupController extends JeecgController<YbChargeDoctorRiskGroup, IYbChargeDoctorRiskGroupService> {
	@Autowired
	private IYbChargeDoctorRiskGroupService ybChargeDoctorRiskGroupService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeDoctorRiskGroup
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医生异常情况汇总表-分页列表查询")
	@ApiOperation(value="医生异常情况汇总表-分页列表查询", notes="医生异常情况汇总表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeDoctorRiskGroup ybChargeDoctorRiskGroup,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeDoctorRiskGroup> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeDoctorRiskGroup, req.getParameterMap());
		Page<YbChargeDoctorRiskGroup> page = new Page<YbChargeDoctorRiskGroup>(pageNo, pageSize);
		IPage<YbChargeDoctorRiskGroup> pageList = ybChargeDoctorRiskGroupService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeDoctorRiskGroup
	 * @return
	 */
	@AutoLog(value = "医生异常情况汇总表-添加")
	@ApiOperation(value="医生异常情况汇总表-添加", notes="医生异常情况汇总表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeDoctorRiskGroup ybChargeDoctorRiskGroup) {
		ybChargeDoctorRiskGroupService.save(ybChargeDoctorRiskGroup);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "医生异常情况汇总表-批量添加")
	 @ApiOperation(value="医生异常情况汇总表-批量添加", notes="医生异常情况汇总表-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeDoctorRiskGroup> addBatchList) throws Exception{
		 ybChargeDoctorRiskGroupService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargeDoctorRiskGroup
	 * @return
	 */
	@AutoLog(value = "医生异常情况汇总表-编辑")
	@ApiOperation(value="医生异常情况汇总表-编辑", notes="医生异常情况汇总表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeDoctorRiskGroup ybChargeDoctorRiskGroup) {
		ybChargeDoctorRiskGroupService.updateById(ybChargeDoctorRiskGroup);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医生异常情况汇总表-通过id删除")
	@ApiOperation(value="医生异常情况汇总表-通过id删除", notes="医生异常情况汇总表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeDoctorRiskGroupService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医生异常情况汇总表-批量删除")
	@ApiOperation(value="医生异常情况汇总表-批量删除", notes="医生异常情况汇总表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeDoctorRiskGroupService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医生异常情况汇总表-通过id查询")
	@ApiOperation(value="医生异常情况汇总表-通过id查询", notes="医生异常情况汇总表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeDoctorRiskGroup ybChargeDoctorRiskGroup = ybChargeDoctorRiskGroupService.getById(id);
		return Result.ok(ybChargeDoctorRiskGroup);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeDoctorRiskGroup
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeDoctorRiskGroup ybChargeDoctorRiskGroup) {
      return super.exportXls(request, ybChargeDoctorRiskGroup, YbChargeDoctorRiskGroup.class, "医生异常情况汇总表");
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
      return super.importExcel(request, response, YbChargeDoctorRiskGroup.class);
  }

}
