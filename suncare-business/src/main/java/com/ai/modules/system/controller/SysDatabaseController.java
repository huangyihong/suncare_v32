package com.ai.modules.system.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;
import com.ai.modules.system.entity.SysDatasource;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import com.ai.modules.system.entity.SysDatabase;
import com.ai.modules.system.service.ISysDatabaseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 数据源配置
 * @Author: jeecg-boot
 * @Date:   2022-11-22
 * @Version: V1.0
 */
@Slf4j
@Api(tags="数据源配置")
@RestController
@RequestMapping("/system/sysDatabase")
public class SysDatabaseController extends JeecgController<SysDatabase, ISysDatabaseService> {
	@Autowired
	private ISysDatabaseService sysDatabaseService;
	@Autowired
	private IYbChargeSearchTaskService ybChargeSearchTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param sysDatabase
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "数据源配置-分页列表查询")
	@ApiOperation(value="数据源配置-分页列表查询", notes="数据源配置-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDatabase sysDatabase,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<SysDatabase> queryWrapper = QueryGenerator.initQueryWrapper(sysDatabase, req.getParameterMap());
		Page<SysDatabase> page = new Page<SysDatabase>(pageNo, pageSize);
		IPage<SysDatabase> pageList = sysDatabaseService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 列表查询
	  *
	  * @param sysDatabase
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "数据库数据源-列表查询")
	 @ApiOperation(value="数据库数据源-列表查询", notes="数据库数据源-列表查询")
	 @GetMapping(value = "/queryList")
	 public Result<?> queryList(SysDatabase sysDatabase,
								HttpServletRequest req) {
		 QueryWrapper<SysDatabase> queryWrapper = QueryGenerator.initQueryWrapper(sysDatabase, req.getParameterMap());
		 List<SysDatabase> list = sysDatabaseService.list(queryWrapper);
		 return Result.ok(list);
	 }

	/**
	 * 添加
	 *
	 * @param sysDatabase
	 * @return
	 */
	@AutoLog(value = "数据源配置-添加")
	@ApiOperation(value="数据源配置-添加", notes="数据源配置-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDatabase sysDatabase) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		ybChargeSearchTaskService.clearCacheDatasourceAndDatabase(dataSource);
		//密码加密
		sysDatabase.setDbPassword(DbDataEncryptUtil.dbDataEncryptString(sysDatabase.getDbPassword()));
		//dbname重复校验
		String dbname = sysDatabase.getDbname();
		LambdaQueryWrapper<SysDatabase> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysDatabase::getDbname,dbname);
		List<SysDatabase> list = sysDatabaseService.list(queryWrapper);
		if(list.size()>0){
			throw new Exception("数据库编码已存在!");
		}
		sysDatabaseService.save(sysDatabase);

		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param sysDatabase
	 * @return
	 */
	@AutoLog(value = "数据源配置-编辑")
	@ApiOperation(value="数据源配置-编辑", notes="数据源配置-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SysDatabase sysDatabase) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		ybChargeSearchTaskService.clearCacheDatasourceAndDatabase(dataSource);

		//密码加密
		sysDatabase.setDbPassword(DbDataEncryptUtil.dbDataEncryptString(sysDatabase.getDbPassword()));
		//dbname重复校验
		String dbname = sysDatabase.getDbname();
		LambdaQueryWrapper<SysDatabase> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysDatabase::getDbname,dbname);
		queryWrapper.notIn(SysDatabase::getId,sysDatabase.getId());
		List<SysDatabase> list = sysDatabaseService.list(queryWrapper);
		if(list.size()>0){
			throw new Exception("数据库编码已存在!");
		}
		sysDatabaseService.updateById(sysDatabase);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "数据源配置-通过id删除")
	@ApiOperation(value="数据源配置-通过id删除", notes="数据源配置-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysDatabaseService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "数据源配置-批量删除")
	@ApiOperation(value="数据源配置-批量删除", notes="数据源配置-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysDatabaseService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "数据源配置-通过id查询")
	@ApiOperation(value="数据源配置-通过id查询", notes="数据源配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SysDatabase sysDatabase = sysDatabaseService.getById(id);
		return Result.ok(sysDatabase);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param sysDatabase
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, SysDatabase sysDatabase) {
      return super.exportXls(request, sysDatabase, SysDatabase.class, "数据源配置");
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
      return super.importExcel(request, response, SysDatabase.class);
  }

	 /**
	  * 测试连接
	  *
	  * @param sysDatabase
	  * @return
	  */
	 @AutoLog(value = "数据库数据源-测试连接")
	 @ApiOperation(value="数据库数据源-测试连接", notes="数据库数据源-测试连接")
	 @PostMapping(value = "/testDbConnection")
	 public Result<?> testDbConnection(@RequestBody SysDatabase sysDatabase) {



		 return sysDatabaseService.testDbConnection(sysDatabase);
	 }


	 /**
	  * 通过dbname查询
	  *
	  * @param dbname
	  * @return
	  */
	 @AutoLog(value = "数据库数据源-通过dbname查询")
	 @ApiOperation(value="数据库数据源-通过dbname查询", notes="数据库数据源-通过dbname查询")
	 @GetMapping(value = "/queryByDbname")
	 public Result<?> queryByDbname(@RequestParam(name="dbname",required=true) String dbname) {
		 SysDatabase sysDatabase = sysDatabaseService.getByDbname(dbname);
		 return Result.ok(sysDatabase);
	 }

}
