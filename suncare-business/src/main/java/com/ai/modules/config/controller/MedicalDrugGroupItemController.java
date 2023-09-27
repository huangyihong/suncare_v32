package com.ai.modules.config.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalDrugGroupItem;
import com.ai.modules.config.service.IMedicalDrugGroupItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

 /**
 * @Description: 药品分组子项
 * @Author: jeecg-boot
 * @Date:   2020-03-02
 * @Version: V1.0
 */
@Slf4j
@Api(tags="药品分组子项")
@RestController
@RequestMapping("/config/medicalDrugGroupItem")
public class MedicalDrugGroupItemController extends JeecgController<MedicalDrugGroupItem, IMedicalDrugGroupItemService> {
	@Autowired
	private IMedicalDrugGroupItemService medicalDrugGroupItemService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalDrugGroupItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "药品分组子项-分页列表查询")
	@ApiOperation(value="药品分组子项-分页列表查询", notes="药品分组子项-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDrugGroupItem medicalDrugGroupItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDrugGroupItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugGroupItem, req.getParameterMap());
		Page<MedicalDrugGroupItem> page = new Page<MedicalDrugGroupItem>(pageNo, pageSize);
		IPage<MedicalDrugGroupItem> pageList = medicalDrugGroupItemService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	  * 不分页列表查询
	  *
	  * @param medicalDictItem
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "药品分组子项-不分页列表查询")
	 @ApiOperation(value="药品分组子项-不分页列表查询", notes="药品分组子项-分页列表查询")
	 @GetMapping(value = "/listAll")
	 public Result<?> queryList(MedicalDrugGroupItem medicalDrugGroupItem,
									HttpServletRequest req) {
		 QueryWrapper<MedicalDrugGroupItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalDrugGroupItem, req.getParameterMap());
		 queryWrapper.orderByAsc("IS_ORDER");
		 List<MedicalDrugGroupItem> list = medicalDrugGroupItemService.list(queryWrapper);
		 return Result.ok(list);
	 }

	 /**
	  * 通过code查询
	  *
	  * @param code
	  * @return
	  */
	 @AutoLog(value = "药品分组子项-通过code查询")
	 @ApiOperation(value="药品分组子项-通过code查询", notes="药品分组子项-通过code查询")
	 @GetMapping(value = "/queryByCode")
	 public Result<?> queryByCode(@RequestParam(name="code") String code) {
		 MedicalDrugGroupItem medicalDrugGroupItem = medicalDrugGroupItemService.getOne(
				 new QueryWrapper<MedicalDrugGroupItem>().eq("CODE",code));
		 return Result.ok(medicalDrugGroupItem);
	 }

	/**
	 * 添加
	 *
	 * @param medicalDrugGroupItem
	 * @return
	 */
	@AutoLog(value = "药品分组子项-添加")
	@ApiOperation(value="药品分组子项-添加", notes="药品分组子项-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalDrugGroupItem medicalDrugGroupItem) {
		medicalDrugGroupItem.setItemId(IdUtils.uuid());
		medicalDrugGroupItemService.save(medicalDrugGroupItem);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalDrugGroupItem
	 * @return
	 */
	@AutoLog(value = "药品分组子项-编辑")
	@ApiOperation(value="药品分组子项-编辑", notes="药品分组子项-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalDrugGroupItem medicalDrugGroupItem) {
		medicalDrugGroupItemService.updateById(medicalDrugGroupItem);
		return Result.ok("编辑成功!");
	}

	/**
	 * 更新排序号
	 *
	 * @param itemList
	 * @return
	 */
	@AutoLog(value = "药品分子项-更新排序号")
	@ApiOperation(value="药品分子项-更新排序号", notes="药品分子项-更新排序号")
	@PutMapping(value = "/editOrders")
	public Result<?> editOrders(String itemIds) {
		medicalDrugGroupItemService.updateOrderByItemIds(itemIds);
		return Result.ok("更新排列顺序成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品分组子项-通过id删除")
	@ApiOperation(value="药品分组子项-通过id删除", notes="药品分组子项-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDrugGroupItemService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "药品分组子项-批量删除")
	@ApiOperation(value="药品分组子项-批量删除", notes="药品分组子项-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDrugGroupItemService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "药品分组子项-通过id查询")
	@ApiOperation(value="药品分组子项-通过id查询", notes="药品分组子项-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDrugGroupItem medicalDrugGroupItem = medicalDrugGroupItemService.getById(id);
		return Result.ok(medicalDrugGroupItem);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDrugGroupItem
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDrugGroupItem medicalDrugGroupItem) {
      return super.exportXls(request, medicalDrugGroupItem, MedicalDrugGroupItem.class, "药品分组子项");
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
      return super.importExcel(request, response, MedicalDrugGroupItem.class);
  }

}
