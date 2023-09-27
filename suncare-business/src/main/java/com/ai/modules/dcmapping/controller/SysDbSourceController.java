package com.ai.modules.dcmapping.controller;

import com.ai.modules.dcmapping.entity.SysDbSource;
import com.ai.modules.dcmapping.service.ISysDbSourceService;
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
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.jeecg.common.util.encryption.AesEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

 /**
 * @Description: 数据库数据源
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="数据库数据源")
@RestController
@RequestMapping("/dcmapping/sysDbSource")
public class SysDbSourceController extends JeecgController<SysDbSource, ISysDbSourceService> {

	@Autowired
	private ISysDbSourceService sysDbSourceService;

	/**
	 * 分页列表查询
	 *
	 * @param sysDbSource
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "数据库数据源-分页列表查询")
	@ApiOperation(value="数据库数据源-分页列表查询", notes="数据库数据源-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDbSource sysDbSource,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<SysDbSource> queryWrapper = QueryGenerator.initQueryWrapper(sysDbSource, req.getParameterMap());
		Page<SysDbSource> page = new Page<SysDbSource>(pageNo, pageSize);
		IPage<SysDbSource> pageList = sysDbSourceService.page(page, queryWrapper);

		return Result.ok(pageList);
	}

	 /**
	  * 列表查询
	  *
	  * @param sysDbSource
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "数据库数据源-列表查询")
	 @ApiOperation(value="数据库数据源-列表查询", notes="数据库数据源-列表查询")
	 @GetMapping(value = "/queryList")
	 public Result<?> queryList(SysDbSource sysDbSource,
									HttpServletRequest req) {
		 QueryWrapper<SysDbSource> queryWrapper = QueryGenerator.initQueryWrapper(sysDbSource, req.getParameterMap());
		 List<SysDbSource> list = sysDbSourceService.list(queryWrapper);
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param sysDbSource
	 * @return
	 */
	@AutoLog(value = "数据库数据源-添加")
	@ApiOperation(value="数据库数据源-添加", notes="数据库数据源-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDbSource sysDbSource) throws Exception {
		//密码加密
		sysDbSource.setDbPassword(DbDataEncryptUtil.dbDataEncryptString(sysDbSource.getDbPassword()));
		sysDbSourceService.save(sysDbSource);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param sysDbSource
	 * @return
	 */
	@AutoLog(value = "数据库数据源-编辑")
	@ApiOperation(value="数据库数据源-编辑", notes="数据库数据源-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SysDbSource sysDbSource) throws Exception {
		//密码加密
		sysDbSource.setDbPassword(DbDataEncryptUtil.dbDataEncryptString(sysDbSource.getDbPassword()));
		sysDbSourceService.updateById(sysDbSource);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "数据库数据源-通过id删除")
	@ApiOperation(value="数据库数据源-通过id删除", notes="数据库数据源-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysDbSourceService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "数据库数据源-批量删除")
	@ApiOperation(value="数据库数据源-批量删除", notes="数据库数据源-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysDbSourceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "数据库数据源-通过id查询")
	@ApiOperation(value="数据库数据源-通过id查询", notes="数据库数据源-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SysDbSource sysDbSource = sysDbSourceService.getById(id);
		return Result.ok(sysDbSource);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param sysDbSource
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, SysDbSource sysDbSource) {
      return super.exportXls(request, sysDbSource, SysDbSource.class, "数据库数据源");
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
      return super.importExcel(request, response, SysDbSource.class);
  }

	 /**
	  * 通过dbName查询数据表字段
	  *
	  * @param dbName
	  * @return
	  */
	 @AutoLog(value = "数据库数据表字段-通过dbName查询")
	 @ApiOperation(value="数据库数据表字段-通过dbName查询", notes="数据库数据表字段-通过dbName查询")
	 @GetMapping(value = "/queryColumnList")
	 public Result<?> queryColumnList(@RequestParam(name="dbName",required=true) String dbName,@RequestParam(name="tableName",required=true) String tableName) {
		 SysDbSource sysDbSource = sysDbSourceService.getById(dbName);
		 List<Map<String, Object>> columnList = sysDbSourceService.getDbColumnList(tableName, sysDbSource);
		 ArrayList<String> column = columnList.stream().map(item->item.get("COLUMN_NAME").toString().toUpperCase()).collect(Collectors.toCollection(ArrayList::new));
		 return Result.ok(column);
	 }

	 /**
	  * 分页列表查询
	  *
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "数据库表数据-分页列表查询")
	 @ApiOperation(value="数据库表数据-分页列表查询", notes="数据库表数据-分页列表查询")
	 @GetMapping(value = "/tableDataList")
	 public Result<?> tableDataList(@RequestParam(name="dbName",required=true) String dbName,
									@RequestParam(name="tableName",required=true) String tableName,
									@RequestParam(name="column",required=true) String column,
									@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									HttpServletRequest req) {
		 SysDbSource sysDbSource = sysDbSourceService.getById(dbName);
		 Page<List<Map<String, Object>>> page = new Page<List<Map<String, Object>>>(pageNo, pageSize);
		 Map<String, Object> data = sysDbSourceService.tableDataByPage(page, sysDbSource,tableName,column);
		 return Result.ok(data);
	 }

	 /**
	  * 测试连接
	  *
	  * @param sysDbSource
	  * @return
	  */
	 @AutoLog(value = "数据库数据源-测试连接")
	 @ApiOperation(value="数据库数据源-测试连接", notes="数据库数据源-测试连接")
	 @PostMapping(value = "/testDbConnection")
	 public Result<?> testDbConnection(@RequestBody SysDbSource sysDbSource) {



		 return sysDbSourceService.testDbConnection(sysDbSource);
	 }

}
