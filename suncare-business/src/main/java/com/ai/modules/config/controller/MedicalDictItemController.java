package com.ai.modules.config.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.service.IMedicalDictClearService;
import com.ai.modules.config.vo.MedicalDictItemVO;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.service.IMedicalDictItemService;
import java.util.Date;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 医疗字典子项
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医疗字典子项")
@RestController
@RequestMapping("/config/medicalDictItem")
public class MedicalDictItemController extends JeecgController<MedicalDictItem, IMedicalDictItemService> {
	@Autowired
	private IMedicalDictItemService medicalDictItemService;

	 @Autowired
	 private IMedicalDictClearService medicalDictClearService;
	/**
	 * 分页列表查询
	 *
	 * @param medicalDictItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医疗字典子项-分页列表查询")
	@ApiOperation(value="医疗字典子项-分页列表查询", notes="医疗字典子项-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDictItem medicalDictItem,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDictItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalDictItem, req.getParameterMap());
		Page<MedicalDictItem> page = new Page<MedicalDictItem>(pageNo, pageSize);
		IPage<MedicalDictItem> pageList = medicalDictItemService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 不分页列表查询
	  *
	  * @param medicalDictItem
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "医疗字典子项-不分页列表查询")
	 @ApiOperation(value="医疗字典子项-不分页列表查询", notes="医疗字典子项-分页列表查询")
	 @GetMapping(value = "/listAll")
	 public Result<?> queryList(MedicalDictItem medicalDictItem,
									HttpServletRequest req) {
		 QueryWrapper<MedicalDictItem> queryWrapper = QueryGenerator.initQueryWrapper(medicalDictItem, req.getParameterMap());
		 queryWrapper.orderByAsc("IS_ORDER");
		 List<MedicalDictItem> list = medicalDictItemService.list(queryWrapper);
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param medicalDictItem
	 * @return
	 */
	@AutoLog(value = "医疗字典子项-添加")
	@ApiOperation(value="医疗字典子项-添加", notes="医疗字典子项-添加")
	@PostMapping(value = "/add")
	public Result<?> add(MedicalDictItem medicalDictItem) {
		medicalDictItem.setItemId(IdUtils.uuid());
		medicalDictItemService.save(medicalDictItem);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalDictItem
	 * @return
	 */
	@AutoLog(value = "医疗字典子项-编辑")
	@ApiOperation(value="医疗字典子项-编辑", notes="医疗字典子项-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(MedicalDictItem medicalDictItem) {
		medicalDictItemService.updateById(medicalDictItem);
		return Result.ok("编辑成功!");
	}

	/**
	 * 更新排序号
	 *
	 * @param itemList
	 * @return
	 */
	@AutoLog(value = "医疗字典子项-更新排序号")
	@ApiOperation(value="医疗字典子项-更新排序号", notes="医疗字典子项-更新排序号")
	@PutMapping(value = "/editOrders")
	public Result<?> editOrders(String itemIds) {
		medicalDictItemService.updateOrderByItemIds(itemIds);
		return Result.ok("更新排列顺序成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗字典子项-通过id删除")
	@ApiOperation(value="医疗字典子项-通过id删除", notes="医疗字典子项-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id,
							String groupCode, String kind) {
		medicalDictItemService.removeById(id);
		if(StringUtils.isNotBlank(groupCode)){
			medicalDictClearService.clearCache(groupCode, kind);
		}
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医疗字典子项-批量删除")
	@ApiOperation(value="医疗字典子项-批量删除", notes="医疗字典子项-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDictItemService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗字典子项-通过id查询")
	@ApiOperation(value="医疗字典子项-通过id查询", notes="医疗字典子项-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDictItem medicalDictItem = medicalDictItemService.getById(id);
		return Result.ok(medicalDictItem);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDictItem
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDictItem medicalDictItem) {
      return super.exportXls(request, medicalDictItem, MedicalDictItem.class, "医疗字典子项");
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
      return super.importExcel(request, response, MedicalDictItem.class);
  }

}
