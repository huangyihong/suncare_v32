package com.ai.modules.ybChargeSearch.controller;

import com.ai.modules.ybChargeSearch.entity.YbChargeFraudResult;
import com.ai.modules.ybChargeSearch.service.IYbChargeFraudResultService;
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
 * @Description: 欺诈专题结果表
 * @Author: jeecg-boot
 * @Date:   2023-05-09
 * @Version: V1.0
 */
@Slf4j
@Api(tags="欺诈专题结果表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeFraudResult")
public class YbChargeFraudResultController extends JeecgController<YbChargeFraudResult, IYbChargeFraudResultService> {
	@Autowired
	private IYbChargeFraudResultService ybChargeFraudResultService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeFraudResult
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "欺诈专题结果表-分页列表查询")
	@ApiOperation(value="欺诈专题结果表-分页列表查询", notes="欺诈专题结果表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeFraudResult ybChargeFraudResult,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeFraudResult> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeFraudResult, req.getParameterMap());
		Page<YbChargeFraudResult> page = new Page<YbChargeFraudResult>(pageNo, pageSize);
		IPage<YbChargeFraudResult> pageList = ybChargeFraudResultService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeFraudResult
	 * @return
	 */
	@AutoLog(value = "欺诈专题结果表-添加")
	@ApiOperation(value="欺诈专题结果表-添加", notes="欺诈专题结果表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeFraudResult ybChargeFraudResult) {
		ybChargeFraudResultService.save(ybChargeFraudResult);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "欺诈专题结果表-批量添加")
	 @ApiOperation(value="欺诈专题结果表-批量添加", notes="欺诈专题结果表-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeFraudResult> addBatchList) throws Exception{
		 ybChargeFraudResultService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargeFraudResult
	 * @return
	 */
	@AutoLog(value = "欺诈专题结果表-编辑")
	@ApiOperation(value="欺诈专题结果表-编辑", notes="欺诈专题结果表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeFraudResult ybChargeFraudResult) {
		ybChargeFraudResultService.updateById(ybChargeFraudResult);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "欺诈专题结果表-通过id删除")
	@ApiOperation(value="欺诈专题结果表-通过id删除", notes="欺诈专题结果表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeFraudResultService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "欺诈专题结果表-批量删除")
	@ApiOperation(value="欺诈专题结果表-批量删除", notes="欺诈专题结果表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeFraudResultService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "欺诈专题结果表-通过id查询")
	@ApiOperation(value="欺诈专题结果表-通过id查询", notes="欺诈专题结果表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeFraudResult ybChargeFraudResult = ybChargeFraudResultService.getById(id);
		return Result.ok(ybChargeFraudResult);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeFraudResult
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeFraudResult ybChargeFraudResult) {
      return super.exportXls(request, ybChargeFraudResult, YbChargeFraudResult.class, "欺诈专题结果表");
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
      return super.importExcel(request, response, YbChargeFraudResult.class);
  }

}
