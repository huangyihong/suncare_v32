package com.ai.modules.system.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import com.ai.modules.system.entity.SysQuickMenu;
import com.ai.modules.system.service.ISysQuickMenuService;
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
 * @Description: 用户快捷菜单
 * @Author: jeecg-boot
 * @Date:   2019-12-17
 * @Version: V1.0
 */
@Slf4j
@Api(tags="用户快捷菜单")
@RestController
@RequestMapping("/system/sysQuickMenu")
public class SysQuickMenuController extends JeecgController<SysQuickMenu, ISysQuickMenuService> {
	@Autowired
	private ISysQuickMenuService sysQuickMenuService;

	 /**
	  * 通过用户信息查询
	  *
	  * @return
	  */
	 @AutoLog(value = "用户快捷菜单-通过用户信息查询")
	 @ApiOperation(value="用户快捷菜单-通过用户信息查询", notes="用户快捷菜单-通过用户信息查询")
	 @GetMapping(value = "/queryByUser")
	 public Result<?> queryByUser() {
		 List<SysQuickMenu> list = sysQuickMenuService.queryByUser();
		 return Result.ok(list);
	 }

	 /**
	  * 添加
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "用户快捷菜单-保存")
	 @ApiOperation(value="用户快捷菜单-保存", notes="用户快捷菜单-保存")
	 @PostMapping(value = "/saveUserMenu")
	 public Result<?> saveUserMenu(@RequestParam(name="ids") String ids) {
		 sysQuickMenuService.saveUserMenu(ids.trim().length()>0?ids.split(","):new String[0]);
		 return Result.ok("保存成功！");
	 }

	/**
	 * 分页列表查询
	 *
	 * @param sysQuickMenu
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "用户快捷菜单-分页列表查询")
	@ApiOperation(value="用户快捷菜单-分页列表查询", notes="用户快捷菜单-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysQuickMenu sysQuickMenu,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<SysQuickMenu> queryWrapper = QueryGenerator.initQueryWrapper(sysQuickMenu, req.getParameterMap());
		Page<SysQuickMenu> page = new Page<SysQuickMenu>(pageNo, pageSize);
		IPage<SysQuickMenu> pageList = sysQuickMenuService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param sysQuickMenu
	 * @return
	 */
	@AutoLog(value = "用户快捷菜单-添加")
	@ApiOperation(value="用户快捷菜单-添加", notes="用户快捷菜单-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysQuickMenu sysQuickMenu) {
		sysQuickMenuService.save(sysQuickMenu);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param sysQuickMenu
	 * @return
	 */
	@AutoLog(value = "用户快捷菜单-编辑")
	@ApiOperation(value="用户快捷菜单-编辑", notes="用户快捷菜单-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SysQuickMenu sysQuickMenu) {
		sysQuickMenuService.updateById(sysQuickMenu);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "用户快捷菜单-通过id删除")
	@ApiOperation(value="用户快捷菜单-通过id删除", notes="用户快捷菜单-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysQuickMenuService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "用户快捷菜单-批量删除")
	@ApiOperation(value="用户快捷菜单-批量删除", notes="用户快捷菜单-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysQuickMenuService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "用户快捷菜单-通过id查询")
	@ApiOperation(value="用户快捷菜单-通过id查询", notes="用户快捷菜单-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SysQuickMenu sysQuickMenu = sysQuickMenuService.getById(id);
		return Result.ok(sysQuickMenu);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param sysQuickMenu
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, SysQuickMenu sysQuickMenu) {
      return super.exportXls(request, sysQuickMenu, SysQuickMenu.class, "用户快捷菜单");
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
      return super.importExcel(request, response, SysQuickMenu.class);
  }

}
