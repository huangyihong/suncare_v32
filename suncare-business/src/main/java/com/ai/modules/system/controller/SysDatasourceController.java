package com.ai.modules.system.controller;

import com.ai.modules.system.entity.SysDatasource;
import com.ai.modules.system.service.ISysDatasourceService;
import com.ai.modules.system.vo.RoleVo;
import com.ai.modules.system.vo.SysDatasourceVo;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.ai.modules.ybFj.vo.OrgUserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 项目地配置
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Slf4j
@Api(tags="项目地配置")
@RestController
@RequestMapping("/system/sysDatasource")
public class SysDatasourceController extends JeecgController<SysDatasource, ISysDatasourceService> {
	@Autowired
	private ISysDatasourceService sysDatasourceService;
	@Autowired
	private IYbChargeSearchTaskService ybChargeSearchTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param sysDatasource
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@ApiOperation(value="项目地配置-分页列表查询", notes="项目地配置-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDatasource sysDatasource,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<SysDatasource> queryWrapper = QueryGenerator.initQueryWrapper(sysDatasource, req.getParameterMap());
		Page<SysDatasource> page = new Page<SysDatasource>(pageNo, pageSize);
		IPage<SysDatasource> pageList = sysDatasourceService.getPage(page, queryWrapper);
		return Result.ok(pageList);
	}


	/**
	 * 添加
	 *
	 * @param sysDatasource
	 * @return
	 */
	@AutoLog(value = "项目地配置-添加")
	@ApiOperation(value="项目地配置-添加", notes="项目地配置-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDatasource sysDatasource) throws Exception {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		ybChargeSearchTaskService.clearCacheDatasourceAndDatabase(dataSource);

//		sysDatasourceService.save(sysDatasource);
		sysDatasourceService.addByTransactional(sysDatasource);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param sysDatasource
	 * @return
	 */
	@AutoLog(value = "项目地配置-编辑")
	@ApiOperation(value="项目地配置-编辑", notes="项目地配置-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SysDatasource sysDatasource) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String dataSource = user.getDataSource();
		ybChargeSearchTaskService.clearCacheDatasourceAndDatabase(dataSource);

		sysDatasourceService.updateByTransactional(sysDatasource);
//		sysDatasourceService.updateById(sysDatasource);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目地配置-通过id删除")
	@ApiOperation(value="项目地配置-通过id删除", notes="项目地配置-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysDatasourceService.removeByTransactional(id);
//		sysDatasourceService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目地配置-批量删除")
	@ApiOperation(value="项目地配置-批量删除", notes="项目地配置-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		sysDatasourceService.removeByTransactionals(ids);
//		this.sysDatasourceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目地配置-通过id查询")
	@ApiOperation(value="项目地配置-通过id查询", notes="项目地配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SysDatasource sysDatasource = sysDatasourceService.getById(id);
		return Result.ok(sysDatasource);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param sysDatasource
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, SysDatasource sysDatasource) {
      return super.exportXls(request, sysDatasource, SysDatasource.class, "项目地配置");
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
      return super.importExcel(request, response, SysDatasource.class);
  }

	 /**
	  * 通过code查询
	  *
	  * @param code
	  * @return
	  */
	 @AutoLog(value = "项目地配置-通过code查询")
	 @ApiOperation(value="项目地配置-通过code查询", notes="项目地配置-通过code查询")
	 @GetMapping(value = "/queryByCode")
	 public Result<?> queryByCode(@RequestParam(name="code",required=true) String code) {
		 SysDatasource sysDatasource = sysDatasourceService.getByCode(code);
		 return Result.ok(sysDatasource);
	 }


	/**
	 * 角色列表
	 *
	 * @param sysDatasource
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "项目地配置-角色列表")
	@ApiOperation(value = "项目地配置-角色列表", notes = "项目地配置-角色列表")
	@GetMapping(value = "/roleList")
	public Result<IPage<RoleVo>> queryRolePageList(SysDatasourceVo sysDatasource,
												   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
												   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
												   HttpServletRequest req) {
		Page<RoleVo> page = new Page<RoleVo>(pageNo, pageSize);
		IPage<RoleVo> pageList = sysDatasourceService.getRoleList(page, sysDatasource);
		Result<IPage<RoleVo>> result = new Result<IPage<RoleVo>>();
		result.setResult(pageList);
		return result;
	}

	/**
	 * 批量删除授权角色
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目地配置-批量删除授权角色")
	@ApiOperation(value = "项目地配置-批量删除授权角色", notes = "项目地配置-批量删除授权角色")
	@GetMapping(value = "/delRoleBatch")
	public Result<?> delRoleBatch(String code,String ids) {
		sysDatasourceService.delRoleBatch(code,ids);
		return Result.ok("批量删除授权角色成功！");
	}

	/**
	 * 批量添加授权角色
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目地配置-批量添加授权角色")
	@ApiOperation(value = "项目地配置-批量添加授权角色", notes = "项目地配置-批量添加授权角色")
	@GetMapping(value = "/addRoleBatch")
	public Result<?> addRoleBatch(String code,String ids) {
		sysDatasourceService.addRoleBatch(code,ids);
		return Result.ok("批量添加授权角色！");
	}

}
