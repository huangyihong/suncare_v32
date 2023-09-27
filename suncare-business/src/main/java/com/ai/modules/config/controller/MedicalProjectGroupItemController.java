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
import com.ai.modules.config.entity.MedicalProjectGroupItem;
import com.ai.modules.config.service.IMedicalProjectGroupItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

 /**
 * @Description: 医疗服务项目分组子项
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医疗服务项目分组子项")
@RestController
@RequestMapping("/config/medicalProjectGroupItem")
public class MedicalProjectGroupItemController extends JeecgController<MedicalProjectGroupItem, IMedicalProjectGroupItemService> {
	@Autowired
	private IMedicalProjectGroupItemService medicalProjectGroupItemService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalProjectGroupItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医疗服务项目分组子项-分页列表查询")
	@ApiOperation(value="医疗服务项目分组子项-分页列表查询", notes="医疗服务项目分组子项-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalProjectGroupItem medicalProjectGroupItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalProjectGroupItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalProjectGroupItem, req.getParameterMap());
		Page<MedicalProjectGroupItem> page = new Page<MedicalProjectGroupItem>(pageNo, pageSize);
		IPage<MedicalProjectGroupItem> pageList = medicalProjectGroupItemService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 通过code查询
	  *
	  * @param code
	  * @return
	  */
	 @AutoLog(value = "医疗服务项目分组子项-通过code查询")
	 @ApiOperation(value="医疗服务项目分组子项-通过code查询", notes="医疗服务项目分组子项-通过code查询")
	 @GetMapping(value = "/queryByCode")
	 public Result<?> queryByCode(@RequestParam(name="code") String code) {
		 MedicalProjectGroupItem medicalProjectGroupItem = medicalProjectGroupItemService.getOne(
		 		new QueryWrapper<MedicalProjectGroupItem>().eq("CODE",code));
		 return Result.ok(medicalProjectGroupItem);
	 }



	 /**
	  * 不分页列表查询
	  *
	  * @param medicalDictItem
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "医疗服务项目分组子项-不分页列表查询")
	 @ApiOperation(value="医疗服务项目分组子项-不分页列表查询", notes="医疗服务项目分组子项-分页列表查询")
	 @GetMapping(value = "/listAll")
	 public Result<?> queryList(MedicalProjectGroupItem medicalProjectGroupItem,
									HttpServletRequest req) {
		 QueryWrapper<MedicalProjectGroupItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalProjectGroupItem, req.getParameterMap());
		 queryWrapper.orderByAsc("IS_ORDER");
		 List<MedicalProjectGroupItem> list = medicalProjectGroupItemService.list(queryWrapper);
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param medicalProjectGroupItem
	 * @return
	 */
	@AutoLog(value = "医疗服务项目分组子项-添加")
	@ApiOperation(value="医疗服务项目分组子项-添加", notes="医疗服务项目分组子项-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalProjectGroupItem medicalProjectGroupItem) {
		medicalProjectGroupItem.setItemId(IdUtils.uuid());
		medicalProjectGroupItemService.save(medicalProjectGroupItem);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalProjectGroupItem
	 * @return
	 */
	@AutoLog(value = "医疗服务项目分组子项-编辑")
	@ApiOperation(value="医疗服务项目分组子项-编辑", notes="医疗服务项目分组子项-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalProjectGroupItem medicalProjectGroupItem) {
		medicalProjectGroupItemService.updateById(medicalProjectGroupItem);
		return Result.ok("编辑成功!");
	}

	/**
	 * 更新排序号
	 *
	 * @param itemList
	 * @return
	 */
	@AutoLog(value = "医疗服务项目分子项-更新排序号")
	@ApiOperation(value="医疗服务项目分子项-更新排序号", notes="医疗服务项目分子项-更新排序号")
	@PutMapping(value = "/editOrders")
	public Result<?> editOrders(String itemIds) {
		medicalProjectGroupItemService.updateOrderByItemIds(itemIds);
		return Result.ok("更新排列顺序成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗服务项目分组子项-通过id删除")
	@ApiOperation(value="医疗服务项目分组子项-通过id删除", notes="医疗服务项目分组子项-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalProjectGroupItemService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医疗服务项目分组子项-批量删除")
	@ApiOperation(value="医疗服务项目分组子项-批量删除", notes="医疗服务项目分组子项-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalProjectGroupItemService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗服务项目分组子项-通过id查询")
	@ApiOperation(value="医疗服务项目分组子项-通过id查询", notes="医疗服务项目分组子项-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalProjectGroupItem medicalProjectGroupItem = medicalProjectGroupItemService.getById(id);
		return Result.ok(medicalProjectGroupItem);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalProjectGroupItem
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalProjectGroupItem medicalProjectGroupItem) {
      return super.exportXls(request, medicalProjectGroupItem, MedicalProjectGroupItem.class, "医疗服务项目分组子项");
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
      return super.importExcel(request, response, MedicalProjectGroupItem.class);
  }

}
