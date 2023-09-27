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
import com.ai.modules.config.entity.MedicalDiseaseGroupItem;
import com.ai.modules.config.service.IMedicalDiseaseGroupItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

 /**
 * @Description: 疾病分组子项
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="疾病分组子项")
@RestController
@RequestMapping("/config/medicalDiseaseGroupItem")
public class MedicalDiseaseGroupItemController extends JeecgController<MedicalDiseaseGroupItem, IMedicalDiseaseGroupItemService> {
	@Autowired
	private IMedicalDiseaseGroupItemService medicalDiseaseGroupItemService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalDiseaseGroupItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "疾病分组子项-分页列表查询")
	@ApiOperation(value="疾病分组子项-分页列表查询", notes="疾病分组子项-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDiseaseGroupItem medicalDiseaseGroupItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDiseaseGroupItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalDiseaseGroupItem, req.getParameterMap());
		Page<MedicalDiseaseGroupItem> page = new Page<MedicalDiseaseGroupItem>(pageNo, pageSize);
		IPage<MedicalDiseaseGroupItem> pageList = medicalDiseaseGroupItemService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	  * 不分页列表查询
	  *
	  * @param medicalDictItem
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "疾病分组子项-不分页列表查询")
	 @ApiOperation(value="疾病分组子项-不分页列表查询", notes="疾病分组子项-分页列表查询")
	 @GetMapping(value = "/listAll")
	 public Result<?> queryList(MedicalDiseaseGroupItem medicalDiseaseGroupItem,
									HttpServletRequest req) {
		 QueryWrapper<MedicalDiseaseGroupItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalDiseaseGroupItem, req.getParameterMap());
		 queryWrapper.orderByAsc("IS_ORDER");
		 List<MedicalDiseaseGroupItem> list = medicalDiseaseGroupItemService.list(queryWrapper);
		 return Result.ok(list);
	 }

	 /**
	  * 通过code查询
	  *
	  * @param code
	  * @return
	  */
	 @AutoLog(value = "疾病分组子项-通过code查询")
	 @ApiOperation(value="疾病分组子项-通过code查询", notes="疾病分组子项-通过code查询")
	 @GetMapping(value = "/queryByCode")
	 public Result<?> queryByCode(@RequestParam(name="code") String code) {
		 MedicalDiseaseGroupItem medicalDiseaseGroupItem = medicalDiseaseGroupItemService.getOne(
				 new QueryWrapper<MedicalDiseaseGroupItem>().eq("CODE",code));
		 return Result.ok(medicalDiseaseGroupItem);
	 }

	/**
	 * 添加
	 *
	 * @param medicalDiseaseGroupItem
	 * @return
	 */
	@AutoLog(value = "疾病分组子项-添加")
	@ApiOperation(value="疾病分组子项-添加", notes="疾病分组子项-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalDiseaseGroupItem medicalDiseaseGroupItem) {
		medicalDiseaseGroupItem.setItemId(IdUtils.uuid());
		medicalDiseaseGroupItemService.save(medicalDiseaseGroupItem);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalDiseaseGroupItem
	 * @return
	 */
	@AutoLog(value = "疾病分组子项-编辑")
	@ApiOperation(value="疾病分组子项-编辑", notes="疾病分组子项-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalDiseaseGroupItem medicalDiseaseGroupItem) {
		medicalDiseaseGroupItemService.updateById(medicalDiseaseGroupItem);
		return Result.ok("编辑成功!");
	}

	/**
	 * 更新排序号
	 *
	 * @param itemList
	 * @return
	 */
	@AutoLog(value = "疾病分子项-更新排序号")
	@ApiOperation(value="疾病分子项-更新排序号", notes="疾病分子项-更新排序号")
	@PutMapping(value = "/editOrders")
	public Result<?> editOrders(String itemIds) {
		medicalDiseaseGroupItemService.updateOrderByItemIds(itemIds);
		return Result.ok("更新排列顺序成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "疾病分组子项-通过id删除")
	@ApiOperation(value="疾病分组子项-通过id删除", notes="疾病分组子项-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDiseaseGroupItemService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "疾病分组子项-批量删除")
	@ApiOperation(value="疾病分组子项-批量删除", notes="疾病分组子项-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDiseaseGroupItemService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "疾病分组子项-通过id查询")
	@ApiOperation(value="疾病分组子项-通过id查询", notes="疾病分组子项-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDiseaseGroupItem medicalDiseaseGroupItem = medicalDiseaseGroupItemService.getById(id);
		return Result.ok(medicalDiseaseGroupItem);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDiseaseGroupItem
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDiseaseGroupItem medicalDiseaseGroupItem) {
      return super.exportXls(request, medicalDiseaseGroupItem, MedicalDiseaseGroupItem.class, "疾病分组子项");
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
      return super.importExcel(request, response, MedicalDiseaseGroupItem.class);
  }

}
