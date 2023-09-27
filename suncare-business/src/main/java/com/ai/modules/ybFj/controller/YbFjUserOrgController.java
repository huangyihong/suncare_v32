package com.ai.modules.ybFj.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybFj.vo.OrgUserVo;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjUserOrg;
import com.ai.modules.ybFj.service.IYbFjUserOrgService;
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
 * @Description: 飞检项目账号关联医院
 * @Author: jeecg-boot
 * @Date:   2023-03-23
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目账号关联医院")
@RestController
@RequestMapping("/ybFJ/ybFjUserOrg")
public class YbFjUserOrgController extends JeecgController<YbFjUserOrg, IYbFjUserOrgService> {
	@Autowired
	private IYbFjUserOrgService ybFjUserOrgService;

	/**
	 * 分页列表查询
	 *
	 * @param ybFjUserOrg
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目账号关联医院-分页列表查询")
	@ApiOperation(value="飞检项目账号关联医院-分页列表查询", notes="飞检项目账号关联医院-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbFjUserOrg ybFjUserOrg,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbFjUserOrg> queryWrapper = QueryGenerator.initQueryWrapper(ybFjUserOrg, req.getParameterMap());
		Page<YbFjUserOrg> page = new Page<YbFjUserOrg>(pageNo, pageSize);
		IPage<YbFjUserOrg> pageList = ybFjUserOrgService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybFjUserOrg
	 * @return
	 */
	@AutoLog(value = "飞检项目账号关联医院-添加")
	@ApiOperation(value="飞检项目账号关联医院-添加", notes="飞检项目账号关联医院-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbFjUserOrg ybFjUserOrg) {
		ybFjUserOrgService.save(ybFjUserOrg);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param ybFjUserOrg
	 * @return
	 */
	@AutoLog(value = "飞检项目账号关联医院-编辑")
	@ApiOperation(value="飞检项目账号关联医院-编辑", notes="飞检项目账号关联医院-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbFjUserOrg ybFjUserOrg) {
		ybFjUserOrgService.updateById(ybFjUserOrg);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "飞检项目账号关联医院-通过id删除")
	@ApiOperation(value="飞检项目账号关联医院-通过id删除", notes="飞检项目账号关联医院-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybFjUserOrgService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "飞检项目账号关联医院-批量删除")
	@ApiOperation(value="飞检项目账号关联医院-批量删除", notes="飞检项目账号关联医院-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybFjUserOrgService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	 /**
	  * 批量添加
	  *
	  * @return
	  */
	 @AutoLog(value = "飞检项目账号关联医院-批量添加")
	 @ApiOperation(value="飞检项目账号关联医院-批量添加", notes="飞检项目账号关联医院-批量添加")
	 @GetMapping(value = "/addBatch")
	 public Result<?> addBatch(String orgId,String ids) {
	 	 //先删除
		 this.delBatch(ids);
		 List<String> list = Arrays.asList(ids.split(","));
		 ArrayList<YbFjUserOrg> result = new ArrayList<>();
		 for(String uid:list){
			 YbFjUserOrg ybFjUserOrg = new YbFjUserOrg();
			 ybFjUserOrg.setUserId(uid);
			 ybFjUserOrg.setOrgId(orgId);
			 ybFjUserOrg.setCreateTime(new Date());
			 result.add(ybFjUserOrg);

		 }
		 this.ybFjUserOrgService.saveBatch(result);

		 return Result.ok("批量添加成功！");
	 }


	 /**
	  * 批量删除
	  *
	  * @return
	  */
	 @AutoLog(value = "飞检项目账号关联医院-批量删除")
	 @ApiOperation(value="飞检项目账号关联医院-批量删除", notes="飞检项目账号关联医院-批量删除")
	 @GetMapping(value = "/delBatch")
	 public Result<?> delBatch(String ids) {
		 List<String> list = Arrays.asList(ids.split(","));
		 QueryWrapper<YbFjUserOrg> queryWrapper = new QueryWrapper<>();
		 queryWrapper.in("user_id",list);
		 this.ybFjUserOrgService.remove(queryWrapper);
		 return Result.ok("批量删除成功！");
	 }

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "飞检项目账号关联医院-通过id查询")
	@ApiOperation(value="飞检项目账号关联医院-通过id查询", notes="飞检项目账号关联医院-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbFjUserOrg ybFjUserOrg = ybFjUserOrgService.getById(id);
		return Result.ok(ybFjUserOrg);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybFjUserOrg
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbFjUserOrg ybFjUserOrg) {
      return super.exportXls(request, ybFjUserOrg, YbFjUserOrg.class, "飞检项目账号关联医院");
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
      return super.importExcel(request, response, YbFjUserOrg.class);
  }

}
