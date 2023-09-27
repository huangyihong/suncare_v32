package com.ai.modules.his.controller;

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
import com.ai.modules.his.entity.HisMedicalFormalCaseBusi;
import com.ai.modules.his.service.IHisMedicalFormalCaseBusiService;
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
 * @Description: 业务组模型关联备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="业务组模型关联备份")
@RestController
@RequestMapping("/his/hisMedicalFormalCaseBusi")
public class HisMedicalFormalCaseBusiController extends JeecgController<HisMedicalFormalCaseBusi, IHisMedicalFormalCaseBusiService> {
	@Autowired
	private IHisMedicalFormalCaseBusiService hisMedicalFormalCaseBusiService;
	
	/**
	 * 分页列表查询
	 *
	 * @param hisMedicalFormalCaseBusi
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "业务组模型关联备份-分页列表查询")
	@ApiOperation(value="业务组模型关联备份-分页列表查询", notes="业务组模型关联备份-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(HisMedicalFormalCaseBusi hisMedicalFormalCaseBusi,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<HisMedicalFormalCaseBusi> queryWrapper = QueryGenerator.initQueryWrapper(hisMedicalFormalCaseBusi, req.getParameterMap());
		Page<HisMedicalFormalCaseBusi> page = new Page<HisMedicalFormalCaseBusi>(pageNo, pageSize);
		IPage<HisMedicalFormalCaseBusi> pageList = hisMedicalFormalCaseBusiService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param hisMedicalFormalCaseBusi
	 * @return
	 */
	@AutoLog(value = "业务组模型关联备份-添加")
	@ApiOperation(value="业务组模型关联备份-添加", notes="业务组模型关联备份-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody HisMedicalFormalCaseBusi hisMedicalFormalCaseBusi) {
		hisMedicalFormalCaseBusiService.save(hisMedicalFormalCaseBusi);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param hisMedicalFormalCaseBusi
	 * @return
	 */
	@AutoLog(value = "业务组模型关联备份-编辑")
	@ApiOperation(value="业务组模型关联备份-编辑", notes="业务组模型关联备份-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody HisMedicalFormalCaseBusi hisMedicalFormalCaseBusi) {
		hisMedicalFormalCaseBusiService.updateById(hisMedicalFormalCaseBusi);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务组模型关联备份-通过id删除")
	@ApiOperation(value="业务组模型关联备份-通过id删除", notes="业务组模型关联备份-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		hisMedicalFormalCaseBusiService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "业务组模型关联备份-批量删除")
	@ApiOperation(value="业务组模型关联备份-批量删除", notes="业务组模型关联备份-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.hisMedicalFormalCaseBusiService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务组模型关联备份-通过id查询")
	@ApiOperation(value="业务组模型关联备份-通过id查询", notes="业务组模型关联备份-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		HisMedicalFormalCaseBusi hisMedicalFormalCaseBusi = hisMedicalFormalCaseBusiService.getById(id);
		return Result.ok(hisMedicalFormalCaseBusi);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param hisMedicalFormalCaseBusi
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, HisMedicalFormalCaseBusi hisMedicalFormalCaseBusi) {
      return super.exportXls(request, hisMedicalFormalCaseBusi, HisMedicalFormalCaseBusi.class, "业务组模型关联备份");
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
      return super.importExcel(request, response, HisMedicalFormalCaseBusi.class);
  }

}
