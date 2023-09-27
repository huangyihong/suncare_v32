package com.ai.modules.dcmapping.controller;

import com.ai.modules.dcmapping.entity.DcMappingResultTable;
import com.ai.modules.dcmapping.service.IDcMappingResultTableService;
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
 * @Description: 采集映射表名称映射结果表
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="采集映射表名称映射结果表")
@RestController
@RequestMapping("/dcmapping/dcMappingResultTable")
public class DcMappingResultTableController extends JeecgController<DcMappingResultTable, IDcMappingResultTableService> {
	@Autowired
	private IDcMappingResultTableService dcMappingResultTableService;

	/**
	 * 分页列表查询
	 *
	 * @param dcMappingResultTable
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "采集映射表名称映射结果表-分页列表查询")
	@ApiOperation(value="采集映射表名称映射结果表-分页列表查询", notes="采集映射表名称映射结果表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(DcMappingResultTable dcMappingResultTable,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<DcMappingResultTable> queryWrapper = QueryGenerator.initQueryWrapper(dcMappingResultTable, req.getParameterMap());
		Page<DcMappingResultTable> page = new Page<DcMappingResultTable>(pageNo, pageSize);
		IPage<DcMappingResultTable> pageList = dcMappingResultTableService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 列表查询
	  *
	  * @param dcMappingResultTable
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "采集映射表名称映射结果表-列表查询")
	 @ApiOperation(value="采集映射表名称映射结果表-列表查询", notes="采集映射表名称映射结果表-列表查询")
	 @GetMapping(value = "/queryList")
	 public Result<?> queryList(DcMappingResultTable dcMappingResultTable,
									HttpServletRequest req) {
		 QueryWrapper<DcMappingResultTable> queryWrapper = QueryGenerator.initQueryWrapper(dcMappingResultTable, req.getParameterMap());
		 List<DcMappingResultTable> list = dcMappingResultTableService.list(queryWrapper);
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param dcMappingResultTable
	 * @return
	 */
	@AutoLog(value = "采集映射表名称映射结果表-添加")
	@ApiOperation(value="采集映射表名称映射结果表-添加", notes="采集映射表名称映射结果表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody DcMappingResultTable dcMappingResultTable) {
		dcMappingResultTableService.save(dcMappingResultTable);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param dcMappingResultTable
	 * @return
	 */
	@AutoLog(value = "采集映射表名称映射结果表-编辑")
	@ApiOperation(value="采集映射表名称映射结果表-编辑", notes="采集映射表名称映射结果表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody DcMappingResultTable dcMappingResultTable) {
		dcMappingResultTableService.updateById(dcMappingResultTable);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "采集映射表名称映射结果表-通过id删除")
	@ApiOperation(value="采集映射表名称映射结果表-通过id删除", notes="采集映射表名称映射结果表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		dcMappingResultTableService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "采集映射表名称映射结果表-批量删除")
	@ApiOperation(value="采集映射表名称映射结果表-批量删除", notes="采集映射表名称映射结果表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.dcMappingResultTableService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "采集映射表名称映射结果表-通过id查询")
	@ApiOperation(value="采集映射表名称映射结果表-通过id查询", notes="采集映射表名称映射结果表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		DcMappingResultTable dcMappingResultTable = dcMappingResultTableService.getById(id);
		return Result.ok(dcMappingResultTable);
	}

	 /**
	  * 标记有关
	  *
	  * @param taskId
	  * @param destTableName
	  * @param sourceTableName
	  * @return
	  */
	 @AutoLog(value = "采集映射表名称映射结果表-标记有关")
	 @ApiOperation(value="采集映射表名称映射结果表-标记有关", notes="采集映射表名称映射结果表-标记有关")
	 @GetMapping(value = "/relationTable")
	 public Result<?> relationTable(@RequestParam(name="taskId",required=true) String taskId,
									@RequestParam(name="destTableName",required=true) String destTableName,
									@RequestParam(name="sourceTableName",required=true) String sourceTableName) {
		 dcMappingResultTableService.relationTable(taskId,destTableName,sourceTableName);
		 return Result.ok();
	 }

  /**
   * 导出excel
   *
   * @param request
   * @param dcMappingResultTable
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, DcMappingResultTable dcMappingResultTable) {
      return super.exportXls(request, dcMappingResultTable, DcMappingResultTable.class, "采集映射表名称映射结果表");
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
      return super.importExcel(request, response, DcMappingResultTable.class);
  }

}
