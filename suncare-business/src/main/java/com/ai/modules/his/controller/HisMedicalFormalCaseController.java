package com.ai.modules.his.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.his.service.IHisMedicalFormalCaseService;
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
 * @Description: 风控模型正式备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="风控模型正式备份")
@RestController
@RequestMapping("/his/hisMedicalFormalCase")
public class HisMedicalFormalCaseController extends JeecgController<HisMedicalFormalCase, IHisMedicalFormalCaseService> {
	@Autowired
	private IHisMedicalFormalCaseService hisMedicalFormalCaseService;

	 /**
	  * 业务组关联的所有模型
	  * @param busiId
	  * @param batchId
	  * @return
	  */
	 @AutoLog(value = "风控模型正式备份-业务组关联的所有模型")
	 @ApiOperation(value = "风控模型正式备份-业务组关联的所有模型", notes = "风控模型正式备份-业务组关联的所有模型")
	 @GetMapping(value = "/querySimpleByBusiId")
	 public Result<?> querySimpleByBusiId(@RequestParam(name = "busiId") String busiId,@RequestParam(name = "batchId") String batchId) {
		 QueryWrapper<HisMedicalFormalCase> queryWrapper = new QueryWrapper<HisMedicalFormalCase>()
				 .select("CASE_ID","CASE_NAME")
				 .eq("BATCH_ID",batchId)
				 .inSql("CASE_ID","SELECT CASE_ID FROM HIS_MEDICAL_FORMAL_CASE_BUSI " +
						 "WHERE BATCH_ID = '" + batchId + "' and BUSI_ID = '" + busiId + "'");
		 return Result.ok(hisMedicalFormalCaseService.list(queryWrapper));
	 }


	 /**
	  * 通过id查询所有模型信息
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "流程图-通过id查询所有模型信息")
	 @ApiOperation(value = "流程图-通过id查询所有模型信息", notes = "流程图-通过id查询所有模型信息")
	 @GetMapping(value = "/getHisFormalCaseById")
	 public Result<?> getHisFormalCaseById(@RequestParam(name = "id") String id,@RequestParam(name = "batchId") String batchId, HttpServletRequest req) {
		 JSONObject jsonObject = hisMedicalFormalCaseService.getFormalCaseById(id,batchId);
		 return Result.ok(jsonObject);
	 }

	 @AutoLog(value = "流程图-通过id查询所有模型信息")
	 @ApiOperation(value = "流程图-通过id查询所有模型信息", notes = "流程图-通过id查询所有模型信息")
	 @GetMapping(value = "/getHisFormalCaseByVersion")
	 public Result<?> getHisFormalCaseByVersion(@RequestParam(name = "caseId") String id,
												@RequestParam(name = "version") Float version, HttpServletRequest req) {
		 JSONObject jsonObject = hisMedicalFormalCaseService.getFormalCaseByVersion(id,version);
		 return Result.ok(jsonObject);
	 }

	/**
	 * 分页列表查询
	 *
	 * @param hisMedicalFormalCase
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "风控模型正式备份-分页列表查询")
	@ApiOperation(value="风控模型正式备份-分页列表查询", notes="风控模型正式备份-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(HisMedicalFormalCase hisMedicalFormalCase,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<HisMedicalFormalCase> queryWrapper = QueryGenerator.initQueryWrapper(hisMedicalFormalCase, req.getParameterMap());
		Page<HisMedicalFormalCase> page = new Page<HisMedicalFormalCase>(pageNo, pageSize);
		IPage<HisMedicalFormalCase> pageList = hisMedicalFormalCaseService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param hisMedicalFormalCase
	 * @return
	 */
	@AutoLog(value = "风控模型正式备份-添加")
	@ApiOperation(value="风控模型正式备份-添加", notes="风控模型正式备份-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody HisMedicalFormalCase hisMedicalFormalCase) {
		hisMedicalFormalCaseService.save(hisMedicalFormalCase);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param hisMedicalFormalCase
	 * @return
	 */
	@AutoLog(value = "风控模型正式备份-编辑")
	@ApiOperation(value="风控模型正式备份-编辑", notes="风控模型正式备份-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody HisMedicalFormalCase hisMedicalFormalCase) {
		hisMedicalFormalCaseService.updateById(hisMedicalFormalCase);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "风控模型正式备份-通过id删除")
	@ApiOperation(value="风控模型正式备份-通过id删除", notes="风控模型正式备份-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		hisMedicalFormalCaseService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "风控模型正式备份-批量删除")
	@ApiOperation(value="风控模型正式备份-批量删除", notes="风控模型正式备份-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.hisMedicalFormalCaseService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "风控模型正式备份-通过id查询")
	@ApiOperation(value="风控模型正式备份-通过id查询", notes="风控模型正式备份-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		HisMedicalFormalCase hisMedicalFormalCase = hisMedicalFormalCaseService.getById(id);
		return Result.ok(hisMedicalFormalCase);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param hisMedicalFormalCase
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, HisMedicalFormalCase hisMedicalFormalCase) {
      return super.exportXls(request, hisMedicalFormalCase, HisMedicalFormalCase.class, "风控模型正式备份");
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
      return super.importExcel(request, response, HisMedicalFormalCase.class);
  }

}
