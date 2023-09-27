package com.ai.modules.config.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.config.entity.MedicalSysDict;
import com.ai.modules.config.service.IMedicalSysDictService;
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
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2019-11-22
 * @Version: V1.0
 */
@Slf4j
@Api(tags="医疗字典")
@RestController
@RequestMapping("/config/medicalSysDict")
public class MedicalSysDictController extends JeecgController<MedicalSysDict, IMedicalSysDictService> {
	@Autowired
	private IMedicalSysDictService medicalSysDictService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalSysDict
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "医疗字典-分页列表查询")
	@ApiOperation(value="医疗字典-分页列表查询", notes="医疗字典-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalSysDict medicalSysDict,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalSysDict> queryWrapper = QueryGenerator.initQueryWrapper(medicalSysDict, req.getParameterMap());
		Page<MedicalSysDict> page = new Page<MedicalSysDict>(pageNo, pageSize);
		IPage<MedicalSysDict> pageList = medicalSysDictService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalSysDict
	 * @return
	 */
	@AutoLog(value = "医疗字典-添加")
	@ApiOperation(value="医疗字典-添加", notes="医疗字典-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalSysDict medicalSysDict) {
		medicalSysDictService.save(medicalSysDict);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalSysDict
	 * @return
	 */
	@AutoLog(value = "医疗字典-编辑")
	@ApiOperation(value="医疗字典-编辑", notes="医疗字典-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalSysDict medicalSysDict) {
		medicalSysDictService.updateById(medicalSysDict);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗字典-通过id删除")
	@ApiOperation(value="医疗字典-通过id删除", notes="医疗字典-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalSysDictService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "医疗字典-批量删除")
	@ApiOperation(value="医疗字典-批量删除", notes="医疗字典-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalSysDictService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "医疗字典-通过id查询")
	@ApiOperation(value="医疗字典-通过id查询", notes="医疗字典-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalSysDict medicalSysDict = medicalSysDictService.getById(id);
		return Result.ok(medicalSysDict);
	}

	 /**
	  * 通过type查询
	  *
	  * @param type
	  * @return
	  */
	 @AutoLog(value = "医疗字典-通过type查询")
	 @ApiOperation(value="医疗字典-通过type查询", notes="医疗字典-通过type查询")
	 @GetMapping(value = "/queryByType")
	 public Result<?> queryByType(@RequestParam(name="type",required=true) String type) {
		return Result.ok(medicalSysDictService.queryByType(type));
	 }

	 /**
	  * 通过types查询
	  *
	  * @param types
	  * @return
	  */
	 @AutoLog(value = "医疗字典-通过types查询")
	 @ApiOperation(value="医疗字典-通过types查询", notes="医疗字典-通过types查询")
	 @GetMapping(value = "/queryByTypes")
	 public Result<?> queryByTypes(@RequestParam(name="types",required=true) String types) {
		 return Result.ok(medicalSysDictService.queryByTypes(types.split(",")));
	 }

	 /**
	  * 通过type查询
	  *
	  * @param type
	  * @return
	  */
	 @AutoLog(value = "医疗字典-通过type和code查询值")
	 @ApiOperation(value="医疗字典-通过type和code查询值", notes="医疗字典-通过type和code查询值")
	 @GetMapping(value = "/queryValByTypeCode")
	 public Result<?> queryValByTypeKey(@RequestParam(name="type",required=true) String type,@RequestParam(name="code",required=true) String code) {
		 return Result.ok(medicalSysDictService.queryDictTextByKey(type, code));
	 }

  /**
   * 导出excel
   *
   * @param request
   * @param medicalSysDict
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalSysDict medicalSysDict) {
      return super.exportXls(request, medicalSysDict, MedicalSysDict.class, "医疗字典");
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
      return super.importExcel(request, response, MedicalSysDict.class);
  }

}
