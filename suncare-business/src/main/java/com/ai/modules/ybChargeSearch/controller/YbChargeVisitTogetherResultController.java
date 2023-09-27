package com.ai.modules.ybChargeSearch.controller;

import com.ai.modules.ybChargeSearch.entity.YbChargeVisitTogetherResult;
import com.ai.modules.ybChargeSearch.service.IYbChargeVisitTogetherResultService;
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
 * @Description: 结伴就医明细表
 * @Author: jeecg-boot
 * @Date:   2023-02-22
 * @Version: V1.0
 */
@Slf4j
@Api(tags="结伴就医明细表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeVisitTogetherResult")
public class YbChargeVisitTogetherResultController extends JeecgController<YbChargeVisitTogetherResult, IYbChargeVisitTogetherResultService> {
	@Autowired
	private IYbChargeVisitTogetherResultService ybChargeVisitTogetherResultService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeVisitTogetherResult
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "结伴就医明细表-分页列表查询")
	@ApiOperation(value="结伴就医明细表-分页列表查询", notes="结伴就医明细表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeVisitTogetherResult ybChargeVisitTogetherResult,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeVisitTogetherResult> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeVisitTogetherResult, req.getParameterMap());
		Page<YbChargeVisitTogetherResult> page = new Page<YbChargeVisitTogetherResult>(pageNo, pageSize);
		IPage<YbChargeVisitTogetherResult> pageList = ybChargeVisitTogetherResultService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeVisitTogetherResult
	 * @return
	 */
	@AutoLog(value = "结伴就医明细表-添加")
	@ApiOperation(value="结伴就医明细表-添加", notes="结伴就医明细表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeVisitTogetherResult ybChargeVisitTogetherResult) {
		ybChargeVisitTogetherResultService.save(ybChargeVisitTogetherResult);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "结伴就医明细表-批量添加")
	 @ApiOperation(value="结伴就医明细表-批量添加", notes="结伴就医明细表-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeVisitTogetherResult> addBatchList) throws Exception{
		 ybChargeVisitTogetherResultService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargeVisitTogetherResult
	 * @return
	 */
	@AutoLog(value = "结伴就医明细表-编辑")
	@ApiOperation(value="结伴就医明细表-编辑", notes="结伴就医明细表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeVisitTogetherResult ybChargeVisitTogetherResult) {
		ybChargeVisitTogetherResultService.updateById(ybChargeVisitTogetherResult);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "结伴就医明细表-通过id删除")
	@ApiOperation(value="结伴就医明细表-通过id删除", notes="结伴就医明细表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeVisitTogetherResultService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "结伴就医明细表-批量删除")
	@ApiOperation(value="结伴就医明细表-批量删除", notes="结伴就医明细表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeVisitTogetherResultService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "结伴就医明细表-通过id查询")
	@ApiOperation(value="结伴就医明细表-通过id查询", notes="结伴就医明细表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeVisitTogetherResult ybChargeVisitTogetherResult = ybChargeVisitTogetherResultService.getById(id);
		return Result.ok(ybChargeVisitTogetherResult);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeVisitTogetherResult
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeVisitTogetherResult ybChargeVisitTogetherResult) {
      return super.exportXls(request, ybChargeVisitTogetherResult, YbChargeVisitTogetherResult.class, "结伴就医明细表");
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
      return super.importExcel(request, response, YbChargeVisitTogetherResult.class);
  }

}
