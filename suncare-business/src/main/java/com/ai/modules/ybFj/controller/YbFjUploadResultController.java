package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.entity.YbFjUploadResult;
import com.ai.modules.ybFj.service.IYbFjUploadResultService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

 /**
 * @Description: 生成文件信息
 * @Author: jeecg-boot
 * @Date:   2023-02-07
 * @Version: V1.0
 */
@Slf4j
@Api(tags="生成文件信息")
@RestController
@RequestMapping("/ybFj/ybFjUploadResult")
public class YbFjUploadResultController extends JeecgController<YbFjUploadResult, IYbFjUploadResultService> {
	@Autowired
	private IYbFjUploadResultService ybFjUploadResultService;

	/**
	 * 分页列表查询
	 *
	 * @param ybFjUploadResult
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "生成文件信息-分页列表查询")
	@ApiOperation(value="生成文件信息-分页列表查询", notes="生成文件信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbFjUploadResult ybFjUploadResult,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbFjUploadResult> queryWrapper = QueryGenerator.initQueryWrapper(ybFjUploadResult, req.getParameterMap());
		Page<YbFjUploadResult> page = new Page<YbFjUploadResult>(pageNo, pageSize);
		IPage<YbFjUploadResult> pageList = ybFjUploadResultService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybFjUploadResult
	 * @return
	 */
	@AutoLog(value = "生成文件信息-添加")
	@ApiOperation(value="生成文件信息-添加", notes="生成文件信息-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbFjUploadResult ybFjUploadResult) {
		ybFjUploadResultService.save(ybFjUploadResult);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param ybFjUploadResult
	 * @return
	 */
	@AutoLog(value = "生成文件信息-编辑")
	@ApiOperation(value="生成文件信息-编辑", notes="生成文件信息-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbFjUploadResult ybFjUploadResult) {
		ybFjUploadResultService.updateById(ybFjUploadResult);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "生成文件信息-通过id删除")
	@ApiOperation(value="生成文件信息-通过id删除", notes="生成文件信息-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybFjUploadResultService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "生成文件信息-批量删除")
	@ApiOperation(value="生成文件信息-批量删除", notes="生成文件信息-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybFjUploadResultService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "生成文件信息-通过id查询")
	@ApiOperation(value="生成文件信息-通过id查询", notes="生成文件信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbFjUploadResult ybFjUploadResult = ybFjUploadResultService.getById(id);
		return Result.ok(ybFjUploadResult);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybFjUploadResult
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbFjUploadResult ybFjUploadResult) {
      return super.exportXls(request, ybFjUploadResult, YbFjUploadResult.class, "生成文件信息");
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
      return super.importExcel(request, response, YbFjUploadResult.class);
  }

}
