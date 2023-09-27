package com.ai.modules.config.controller;

import com.ai.modules.config.entity.MedicalDrugInstructionItem;
import com.ai.modules.config.service.IMedicalDrugInstructionItemService;
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
 * @Description: 药品说明书子项
 * @Author: jeecg-boot
 * @Date:   2020-11-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="药品说明书子项")
@RestController
@RequestMapping("/config/medicalDrugInstructionItem")
public class MedicalDrugInstructionItemController extends JeecgController<MedicalDrugInstructionItem, IMedicalDrugInstructionItemService> {
	@Autowired
	private IMedicalDrugInstructionItemService medicalDrugInstructionItemService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalDrugInstructionItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品说明书子项-分页列表查询")
	@ApiOperation(value="药品说明书子项-分页列表查询", notes="药品说明书子项-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDrugInstructionItem medicalDrugInstructionItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDrugInstructionItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugInstructionItem, req.getParameterMap());
		Page<MedicalDrugInstructionItem> page = new Page<MedicalDrugInstructionItem>(pageNo, pageSize);
		IPage<MedicalDrugInstructionItem> pageList = medicalDrugInstructionItemService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 不分页列表查询
	  *
	  * @param medicalDrugInstructionItem
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "药品分组子项-不分页列表查询")
	 @ApiOperation(value="药品分组子项-不分页列表查询", notes="药品分组子项-分页列表查询")
	 @GetMapping(value = "/listAll")
	 public Result<?> queryList(MedicalDrugInstructionItem medicalDrugInstructionItem,
								HttpServletRequest req) {
		 QueryWrapper<MedicalDrugInstructionItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugInstructionItem, req.getParameterMap());
		 queryWrapper.orderByAsc("IS_ORDER");
		 List<MedicalDrugInstructionItem> list = medicalDrugInstructionItemService.list(queryWrapper);
		 return Result.ok(list);
	 }

	 /**
	  * 通过code查询
	  *
	  * @param itemCode
	  * @return
	  */
	 @AutoLog(value = "药品分组子项-通过itemCode查询")
	 @ApiOperation(value="药品分组子项-通过itemCode查询", notes="药品分组子项-通过itemCode查询")
	 @GetMapping(value = "/queryByItemCode")
	 public Result<?> queryByCode(@RequestParam(name="itemCode") String itemCode) {
		 MedicalDrugInstructionItem medicalDrugInstructionItem = medicalDrugInstructionItemService.getOne(
				 new QueryWrapper<MedicalDrugInstructionItem>().eq("ITEM_CODE",itemCode));
		 return Result.ok(medicalDrugInstructionItem);
	 }

	/**
	 * 添加
	 *
	 * @param medicalDrugInstructionItem
	 * @return
	 */
	@AutoLog(value = "药品说明书子项-添加")
	@ApiOperation(value="药品说明书子项-添加", notes="药品说明书子项-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalDrugInstructionItem medicalDrugInstructionItem) {
		medicalDrugInstructionItemService.save(medicalDrugInstructionItem);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalDrugInstructionItem
	 * @return
	 */
	@AutoLog(value = "药品说明书子项-编辑")
	@ApiOperation(value="药品说明书子项-编辑", notes="药品说明书子项-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalDrugInstructionItem medicalDrugInstructionItem) {
		medicalDrugInstructionItemService.updateById(medicalDrugInstructionItem);
		return Result.ok("编辑成功!");
	}

	 /**
	  * 更新排序号
	  *
	  * @param itemIds
	  * @return
	  */
	 @AutoLog(value = " 药品说明书子项-更新排序号")
	 @ApiOperation(value=" 药品说明书子项-更新排序号", notes=" 药品说明书子项-更新排序号")
	 @PutMapping(value = "/editOrders")
	 public Result<?> editOrders(String itemIds) {
		 medicalDrugInstructionItemService.updateOrderByItemIds(itemIds);
		 return Result.ok("更新排列顺序成功!");
	 }

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品说明书子项-通过id删除")
	@ApiOperation(value="药品说明书子项-通过id删除", notes="药品说明书子项-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDrugInstructionItemService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "药品说明书子项-批量删除")
	@ApiOperation(value="药品说明书子项-批量删除", notes="药品说明书子项-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDrugInstructionItemService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品说明书子项-通过id查询")
	@ApiOperation(value="药品说明书子项-通过id查询", notes="药品说明书子项-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDrugInstructionItem medicalDrugInstructionItem = medicalDrugInstructionItemService.getById(id);
		return Result.ok(medicalDrugInstructionItem);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDrugInstructionItem
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDrugInstructionItem medicalDrugInstructionItem) {
      return super.exportXls(request, medicalDrugInstructionItem, MedicalDrugInstructionItem.class, "药品说明书子项");
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
      return super.importExcel(request, response, MedicalDrugInstructionItem.class);
  }

}
