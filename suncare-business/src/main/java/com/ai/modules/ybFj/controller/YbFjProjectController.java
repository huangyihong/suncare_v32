package com.ai.modules.ybFj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybFj.dto.YbFjProjectDto;
import com.ai.modules.ybFj.entity.YbFjUserOrg;
import com.ai.modules.ybFj.service.IYbFjUserOrgService;
import com.ai.modules.ybFj.vo.ProjectOrgClientVo;
import com.ai.modules.ybFj.vo.YbFjProjectOrgVo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjProject;
import com.ai.modules.ybFj.service.IYbFjProjectService;
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
 * @Description: 飞检项目信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目信息")
@RestController
@RequestMapping("/fj/project")
public class YbFjProjectController extends JeecgController<YbFjProject, IYbFjProjectService> {
	@Autowired
	private IYbFjProjectService ybFjProjectService;
	@Autowired
	private IYbFjUserOrgService userOrgService;

	/**
	 * 分页列表查询
	 *
	 * @param ybFjProject
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目信息-分页列表查询")
	@ApiOperation(value="飞检项目信息-分页列表查询", notes="飞检项目信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YbFjProject>> queryPageList(YbFjProject ybFjProject,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Result<IPage<YbFjProject>> result = new Result<IPage<YbFjProject>>();
		QueryWrapper<YbFjProject> queryWrapper = QueryGenerator.initQueryWrapper(ybFjProject, req.getParameterMap());
		queryWrapper.orderByDesc("create_time");
		Page<YbFjProject> page = new Page<YbFjProject>(pageNo, pageSize);
		IPage<YbFjProject> pageList = ybFjProjectService.page(page, queryWrapper);
		result.setResult(pageList);
		return result;
	}

	/**
	 * 添加
	 *
	 * @param dto
	 * @return
	 */
	@RequiresPermissions("fj:project:cud")
	@AutoLog(value = "飞检项目信息-添加")
	@ApiOperation(value="飞检项目信息-添加", notes="飞检项目信息-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbFjProjectDto dto) {
		ybFjProjectService.saveProject(dto);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param dto
	 * @return
	 */
	@RequiresPermissions("fj:project:cud")
	@AutoLog(value = "飞检项目信息-编辑")
	@ApiOperation(value="飞检项目信息-编辑", notes="飞检项目信息-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbFjProjectDto dto) {
		ybFjProjectService.updateProject(dto);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@RequiresPermissions("fj:project:cud")
	@AutoLog(value = "飞检项目信息-通过id删除")
	@ApiOperation(value="飞检项目信息-通过id删除", notes="飞检项目信息-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybFjProjectService.removeProject(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param projectIds
	 * @return
	 */
	@RequiresPermissions("fj:project:cud")
	@AutoLog(value = "飞检项目信息-批量删除")
	@ApiOperation(value="飞检项目信息-批量删除", notes="飞检项目信息-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String projectIds) {
		this.ybFjProjectService.batchRemoveProject(projectIds);
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param projectId
	 * @return
	 */
	@AutoLog(value = "飞检项目信息-通过id查询")
	@ApiOperation(value="飞检项目信息-通过id查询", notes="飞检项目信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YbFjProject> queryById(@RequestParam(name="projectId",required=true) String projectId) {
		Result<YbFjProject> result = new Result<>();
		YbFjProject ybFjProject = ybFjProjectService.getById(projectId);
		result.setResult(ybFjProject);
		return result;
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybFjProject
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbFjProject ybFjProject) {
      return super.exportXls(request, ybFjProject, YbFjProject.class, "飞检项目信息");
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
		  return super.importExcel(request, response, YbFjProject.class);
	  }

	 @AutoLog(value = "飞检项目信息-获取关联的医疗机构信息")
	 @ApiOperation(value="飞检项目信息-获取关联的医疗机构信息", notes="飞检项目信息-获取关联的医疗机构信息")
	 @GetMapping(value = "/orgList")
	 public Result<?> orgList(@RequestParam(name="projectId",required=true) String projectId,
									@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									HttpServletRequest req) {
		 Page<YbFjProjectOrgVo> page = new Page<YbFjProjectOrgVo>(pageNo, pageSize);
		 IPage<YbFjProjectOrgVo> pageList = ybFjProjectService.queryYbFjProjectOrgVo(page, projectId);
		 return Result.ok(pageList);
	 }

	 @RequiresPermissions("fj:project:cud")
	 @AutoLog(value = "飞检项目信息-变更项目状态")
	 @ApiOperation(value="飞检项目信息-变更项目状态", notes="飞检项目信息-变更项目状态")
	 @PostMapping(value = "/chargeState")
	 public Result<?> chargeState(@RequestParam(name="projectIds",required=true) String projectIds, @RequestParam(name="state",required=true) String state) {
		 ybFjProjectService.settingProjectState(projectIds, state);
		 return Result.ok("变更状态成功!");
	 }

	 @RequiresPermissions("fj:project:cud")
	 @AutoLog(value = "飞检项目信息-变更项目关联的医疗机构状态")
	 @ApiOperation(value="飞检项目信息-变更项目关联的医疗机构状态", notes="飞检项目信息-变更项目关联的医疗机构状态")
	 @PostMapping(value = "/org/chargeState")
	 public Result<?> chargeOrgState(@RequestParam(name="projectOrgIds",required=true) String projectOrgIds, @RequestParam(name="state",required=true) String state) {
		 ybFjProjectService.settingOrgState(projectOrgIds, state);
		 return Result.ok("变更状态成功!");
	 }

	 @RequiresPermissions("fj:project:cud")
	 @AutoLog(value = "飞检项目信息-删除项目关联的医疗机构")
	 @ApiOperation(value="飞检项目信息-删除项目关联的医疗机构", notes="飞检项目信息-删除项目关联的医疗机构")
	 @DeleteMapping(value = "/org/remove")
	 public Result<?> removeOrg(@RequestParam(name="projectId",required=true) String projectId,
								@RequestParam(name="orgIds",required=true) String orgIds) {
		 ybFjProjectService.removeOrgs(projectId, orgIds);
		 return Result.ok("删除成功!");
	 }

	 @AutoLog(value = "飞检项目信息（医院端）-检查项目列表")
	 @ApiOperation(value="飞检项目信息（医院端）-检查项目列表", notes="飞检项目信息（医院端）-检查项目列表")
	 @GetMapping(value = "/projectListByOrg")
	 public Result<IPage<ProjectOrgClientVo>> projectListByOrg(YbFjProject ybFjProject,
													 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
													 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
													 HttpServletRequest req) throws Exception {
		 Result<IPage<ProjectOrgClientVo>> result = new Result<IPage<ProjectOrgClientVo>>();
		 QueryWrapper<YbFjProject> queryWrapper = QueryGenerator.initQueryWrapper(ybFjProject, req.getParameterMap());
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 YbFjUserOrg userOrg = userOrgService.getById(user.getId());
		 if(userOrg==null) {
			 throw new Exception("非医院端用户，没权限操作");
		 }

		 Page<ProjectOrgClientVo> page = new Page<ProjectOrgClientVo>(pageNo, pageSize);
		 IPage<ProjectOrgClientVo> pageList = ybFjProjectService.queryYbFjProjectByOrg(page, userOrg.getOrgId(), queryWrapper);
		 result.setResult(pageList);
		 return result;
	 }
}
