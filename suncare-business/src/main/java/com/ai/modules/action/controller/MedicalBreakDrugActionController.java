package com.ai.modules.action.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.modules.engine.util.EngineUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.action.entity.MedicalBreakDrugAction;
import com.ai.modules.action.service.IMedicalBreakDrugActionService;
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
 * @Description: 不合规结果
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
@Slf4j
@Api(tags="不合规结果")
@RestController
@RequestMapping("/action/medicalBreakDrugAction")
public class MedicalBreakDrugActionController extends JeecgController<MedicalBreakDrugAction, IMedicalBreakDrugActionService> {
	@Autowired
	private IMedicalBreakDrugActionService medicalBreakDrugActionService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalBreakDrugAction
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "不合规结果-分页列表查询")
	@ApiOperation(value="不合规结果-分页列表查询", notes="不合规结果-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalBreakDrugAction medicalBreakDrugAction,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Page<MedicalBreakDrugAction> page = new Page<MedicalBreakDrugAction>(pageNo, pageSize);
		IPage<MedicalBreakDrugAction> pageList;
		try {
//			pageList = medicalBreakDrugActionService.pageSolr(page, medicalBreakDrugAction, req);
//			SolrQuery solrQuery = SolrQueryGenerator.initQuery(medicalBreakDrugAction,req.getParameterMap());
//			pageList = SolrQueryGenerator.page(page,solrQuery,EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION);
			pageList = SolrQueryGenerator.page(page,medicalBreakDrugAction,
					EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION,EngineUtil.FIELD_MAPPING,req);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
		return Result.ok(pageList);
	}
	/**
	 * 分页列表查询
	 *
	 * @param medicalBreakDrugAction
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 *//*
	@AutoLog(value = "不合规结果-分页列表查询")
	@ApiOperation(value="不合规结果-分页列表查询", notes="不合规结果-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalBreakDrugAction medicalBreakDrugAction,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalBreakDrugAction> queryWrapper = QueryGenerator.initQueryWrapper(medicalBreakDrugAction, req.getParameterMap());
		Page<MedicalBreakDrugAction> page = new Page<MedicalBreakDrugAction>(pageNo, pageSize);
		IPage<MedicalBreakDrugAction> pageList = medicalBreakDrugActionService.page(page, queryWrapper);
		return Result.ok(pageList);
	}*/

	/**
	 * 添加
	 *
	 * @param medicalBreakDrugAction
	 * @return
	 */
	@AutoLog(value = "不合规结果-添加")
	@ApiOperation(value="不合规结果-添加", notes="不合规结果-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalBreakDrugAction medicalBreakDrugAction) {
		medicalBreakDrugActionService.save(medicalBreakDrugAction);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalBreakDrugAction
	 * @return
	 */
	@AutoLog(value = "不合规结果-编辑")
	@ApiOperation(value="不合规结果-编辑", notes="不合规结果-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalBreakDrugAction medicalBreakDrugAction) {
		medicalBreakDrugActionService.updateById(medicalBreakDrugAction);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合规结果-通过id删除")
	@ApiOperation(value="不合规结果-通过id删除", notes="不合规结果-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalBreakDrugActionService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "不合规结果-批量删除")
	@ApiOperation(value="不合规结果-批量删除", notes="不合规结果-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalBreakDrugActionService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合规结果-通过id查询")
	@ApiOperation(value="不合规结果-通过id查询", notes="不合规结果-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalBreakDrugAction medicalBreakDrugAction = medicalBreakDrugActionService.getById(id);
		return Result.ok(medicalBreakDrugAction);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalBreakDrugAction
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalBreakDrugAction medicalBreakDrugAction) {
      return super.exportXls(request, medicalBreakDrugAction, MedicalBreakDrugAction.class, "不合规结果");
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
      return super.importExcel(request, response, MedicalBreakDrugAction.class);
  }

}
