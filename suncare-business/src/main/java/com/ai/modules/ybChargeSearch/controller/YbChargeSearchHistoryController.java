package com.ai.modules.ybChargeSearch.controller;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchHistory;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

 /**
 * @Description: 收费明细查询历史分析表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Slf4j
@Api(tags="收费明细查询历史分析表")
@RestController
@RequestMapping("/ybChargeSearch/ybChargeSearchHistory")
public class YbChargeSearchHistoryController extends JeecgController<YbChargeSearchHistory, IYbChargeSearchHistoryService> {
	@Autowired
	private IYbChargeSearchHistoryService ybChargeSearchHistoryService;

	/**
	 * 分页列表查询
	 *
	 * @param ybChargeSearchHistory
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "收费明细查询历史分析表-分页列表查询")
	@ApiOperation(value="收费明细查询历史分析表-分页列表查询", notes="收费明细查询历史分析表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbChargeSearchHistory ybChargeSearchHistory,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbChargeSearchHistory> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeSearchHistory, req.getParameterMap());
		Page<YbChargeSearchHistory> page = new Page<YbChargeSearchHistory>(pageNo, pageSize);
		IPage<YbChargeSearchHistory> pageList = ybChargeSearchHistoryService.selectPageVO(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybChargeSearchHistory
	 * @return
	 */
	@AutoLog(value = "收费明细查询历史分析表-添加")
	@ApiOperation(value="收费明细查询历史分析表-添加", notes="收费明细查询历史分析表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbChargeSearchHistory ybChargeSearchHistory) {
		ybChargeSearchHistoryService.save(ybChargeSearchHistory);
		return Result.ok("添加成功！");
	}

	 /**
	  * 添加
	  *
	  * @param addBatchList
	  * @return
	  */
	 @AutoLog(value = "收费明细查询历史分析表-批量添加")
	 @ApiOperation(value="收费明细查询历史分析表-批量添加", notes="收费明细查询历史分析表-批量添加")
	 @PostMapping(value = "/saveBatch")
	 public Result<?> saveBatch(@RequestBody List<YbChargeSearchHistory> addBatchList) throws Exception{
		 ybChargeSearchHistoryService.saveBatch(addBatchList);
		 return Result.ok("添加成功！");
	 }

	/**
	 * 编辑
	 *
	 * @param ybChargeSearchHistory
	 * @return
	 */
	@AutoLog(value = "收费明细查询历史分析表-编辑")
	@ApiOperation(value="收费明细查询历史分析表-编辑", notes="收费明细查询历史分析表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbChargeSearchHistory ybChargeSearchHistory) {
		ybChargeSearchHistoryService.updateById(ybChargeSearchHistory);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "收费明细查询历史分析表-通过id删除")
	@ApiOperation(value="收费明细查询历史分析表-通过id删除", notes="收费明细查询历史分析表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybChargeSearchHistoryService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "收费明细查询历史分析表-批量删除")
	@ApiOperation(value="收费明细查询历史分析表-批量删除", notes="收费明细查询历史分析表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybChargeSearchHistoryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "收费明细查询历史分析表-通过id查询")
	@ApiOperation(value="收费明细查询历史分析表-通过id查询", notes="收费明细查询历史分析表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbChargeSearchHistory ybChargeSearchHistory = ybChargeSearchHistoryService.getById(id);
		return Result.ok(ybChargeSearchHistory);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybChargeSearchHistory
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbChargeSearchHistory ybChargeSearchHistory) {
      return super.exportXls(request, ybChargeSearchHistory, YbChargeSearchHistory.class, "收费明细查询历史分析表");
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
      return super.importExcel(request, response, YbChargeSearchHistory.class);
  }

	 /**
	  * 直接导出excel
	  *
	  * @param req
	  * @param response
	  * @param ybChargeSearchHistory
	  * @throws Exception
	  */
	 @RequestMapping(value = "/exportExcel")
	 public void exportExcel(HttpServletRequest req, HttpServletResponse response, YbChargeSearchHistory ybChargeSearchHistory) throws Exception {
		 Result<?> result = new Result<>();
		 String title = req.getParameter("title");
		 if (StringUtils.isBlank(title)) {
			 title = "检索关键字使用统计_导出";
		 }
		 //response.reset();
		 response.setContentType("application/octet-stream; charset=utf-8");
		 response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
		 try {
			 // 选中数据
			 String selections = req.getParameter("selections");
			 if (StringUtils.isNotEmpty(selections)) {
				 ybChargeSearchHistory.setId(selections);
			 }

			 OutputStream os = response.getOutputStream();
			 QueryWrapper<YbChargeSearchHistory> queryWrapper = QueryGenerator.initQueryWrapper(ybChargeSearchHistory, req.getParameterMap());
			 List<YbChargeSearchHistory> list = ybChargeSearchHistoryService.selectListVO(queryWrapper);
			 ybChargeSearchHistoryService.exportExcel(list, os);
		 } catch (Exception e) {
			 throw e;
		 }
	 }

}
