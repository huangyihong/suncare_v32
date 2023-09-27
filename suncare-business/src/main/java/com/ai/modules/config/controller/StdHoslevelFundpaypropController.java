package com.ai.modules.config.controller;

import com.ai.common.utils.ExcelTool;
import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.ai.modules.config.service.IMedicalImportTaskService;
import com.ai.modules.config.service.IStdHoslevelFundpaypropService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 各地不同物价级别报销比例
 * @Author: jeecg-boot
 * @Date:   2020-11-16
 * @Version: V1.0
 */
@Slf4j
@Api(tags="各地不同物价级别报销比例")
@RestController
@RequestMapping("/config/stdHoslevelFundpayprop")
public class StdHoslevelFundpaypropController extends JeecgController<StdHoslevelFundpayprop, IStdHoslevelFundpaypropService> {
	@Autowired
	private IStdHoslevelFundpaypropService stdHoslevelFundpaypropService;
	@Autowired
	IMedicalImportTaskService importTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param stdHoslevelFundpayprop
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "各地不同物价级别报销比例-分页列表查询")
	@ApiOperation(value="各地不同物价级别报销比例-分页列表查询", notes="各地不同物价级别报销比例-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(StdHoslevelFundpayprop stdHoslevelFundpayprop,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<StdHoslevelFundpayprop> queryWrapper = QueryGenerator.initQueryWrapper(stdHoslevelFundpayprop, req.getParameterMap());
		Page<StdHoslevelFundpayprop> page = new Page<StdHoslevelFundpayprop>(pageNo, pageSize);
		IPage<StdHoslevelFundpayprop> pageList = stdHoslevelFundpaypropService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param stdHoslevelFundpayprop
	 * @return
	 */
	@AutoLog(value = "各地不同物价级别报销比例-添加")
	@ApiOperation(value="各地不同物价级别报销比例-添加", notes="各地不同物价级别报销比例-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody StdHoslevelFundpayprop stdHoslevelFundpayprop) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        boolean flag = this.stdHoslevelFundpaypropService.isExist(stdHoslevelFundpayprop);
        if(flag){
			return Result.error("新增失败，该项目地物价级别时间范围的报销比例数据已存在！");
		}
		stdHoslevelFundpaypropService.save(stdHoslevelFundpayprop);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param stdHoslevelFundpayprop
	 * @return
	 */
	@AutoLog(value = "各地不同物价级别报销比例-编辑")
	@ApiOperation(value="各地不同物价级别报销比例-编辑", notes="各地不同物价级别报销比例-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody StdHoslevelFundpayprop stdHoslevelFundpayprop) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		boolean flag = this.stdHoslevelFundpaypropService.isExist(stdHoslevelFundpayprop);
		if(flag){
			return Result.error("修改失败，该项目地物价级别时间范围的报销比例数据已存在！");
		}
		stdHoslevelFundpaypropService.updateById(stdHoslevelFundpayprop);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "各地不同物价级别报销比例-通过id删除")
	@ApiOperation(value="各地不同物价级别报销比例-通过id删除", notes="各地不同物价级别报销比例-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		stdHoslevelFundpaypropService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "各地不同物价级别报销比例-批量删除")
	@ApiOperation(value="各地不同物价级别报销比例-批量删除", notes="各地不同物价级别报销比例-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.stdHoslevelFundpaypropService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "各地不同物价级别报销比例-通过id查询")
	@ApiOperation(value="各地不同物价级别报销比例-通过id查询", notes="各地不同物价级别报销比例-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		StdHoslevelFundpayprop stdHoslevelFundpayprop = stdHoslevelFundpaypropService.getById(id);
		return Result.ok(stdHoslevelFundpayprop);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param stdHoslevelFundpayprop
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, StdHoslevelFundpayprop stdHoslevelFundpayprop) {
      return super.exportXls(request, stdHoslevelFundpayprop, StdHoslevelFundpayprop.class, "各地不同物价级别报销比例");
  }

  /**
   * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws Exception{
	  LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
	  MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
	  Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
	  for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
		  MultipartFile file = entity.getValue();// 获取上传文件对象
		  // 判断文件名是否为空
		  if (file == null) {
			  return Result.error("上传文件为空");
		  }
		  // 获取文件名
		  String name = file.getOriginalFilename();
		  // 判断文件大小、即名称
		  long size = file.getSize();
		  if (name == null || ("").equals(name) && size == 0) {
			  return Result.error("上传文件内容为空");
		  }
		  return importTaskService.saveImportTask("Std_Hoslevel_Fundpayprop","报销比例导入",file,user,
				  (f,u)->{
					  try {
						  return this.stdHoslevelFundpaypropService.importExcel(f,u);
					  } catch (Exception e) {
						  e.printStackTrace();
						  return Result.error(e.getMessage());
					  }
				  });
	  }
	  return Result.error("上传文件为空");
  }

	/**
	 * 直接导出excel
	 *
	 * @param req
	 * @param response
	 * @param stdHoslevelFundpayprop
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportExcel")
	public void exportExcel(HttpServletRequest req, HttpServletResponse response, StdHoslevelFundpayprop stdHoslevelFundpayprop) throws Exception {
		Result<?> result = new Result<>();
		String title = req.getParameter("title");
		if (StringUtils.isBlank(title)) {
			title = "各地不同物价级别报销比例_导出";
		}
		//response.reset();
		response.setContentType("application/octet-stream; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
		try {
			OutputStream os = response.getOutputStream();
			// 选中数据
			String selections = req.getParameter("selections");
			if (StringUtils.isNotEmpty(selections)) {
				stdHoslevelFundpayprop.setId(selections);
			}
			QueryWrapper<StdHoslevelFundpayprop> queryWrapper = QueryGenerator.initQueryWrapper(stdHoslevelFundpayprop, req.getParameterMap());
			List<StdHoslevelFundpayprop> list = stdHoslevelFundpaypropService.list(queryWrapper);
			String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
			stdHoslevelFundpaypropService.exportExcel(list, os, suffix);
		} catch (Exception e) {
			throw e;
		}
	}

}
