package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.entity.YbFjUpload;
import com.ai.modules.ybFj.service.IYbFjUploadService;
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

 /**
 * @Description: 飞检项目上传文件
 * @Author: jeecg-boot
 * @Date:   2023-02-06
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目上传文件")
@RestController
@RequestMapping("/ybFj/ybFjUpload")
public class YbFjUploadController extends JeecgController<YbFjUpload, IYbFjUploadService> {
	 @Autowired
	 private IYbFjUploadService ybFjUploadService;


	/**
	 * 分页列表查询
	 *
	 * @param ybFjUpload
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目上传文件-分页列表查询")
	@ApiOperation(value="飞检项目上传文件-分页列表查询", notes="飞检项目上传文件-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbFjUpload ybFjUpload,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbFjUpload> queryWrapper = QueryGenerator.initQueryWrapper(ybFjUpload, req.getParameterMap());
		Page<YbFjUpload> page = new Page<YbFjUpload>(pageNo, pageSize);
		IPage<YbFjUpload> pageList = ybFjUploadService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param ybFjUpload
	 * @return
	 */
	@AutoLog(value = "飞检项目上传文件-添加")
	@ApiOperation(value="飞检项目上传文件-添加", notes="飞检项目上传文件-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbFjUpload ybFjUpload) {
		ybFjUploadService.add(ybFjUpload);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param ybFjUpload
	 * @return
	 */
	@AutoLog(value = "飞检项目上传文件-编辑")
	@ApiOperation(value="飞检项目上传文件-编辑", notes="飞检项目上传文件-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbFjUpload ybFjUpload) {
		ybFjUploadService.edit(ybFjUpload);
		return Result.ok("编辑成功!");
	}



	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "飞检项目上传文件-通过id删除")
	@ApiOperation(value="飞检项目上传文件-通过id删除", notes="飞检项目上传文件-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ybFjUploadService.delete(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "飞检项目上传文件-批量删除")
	@ApiOperation(value="飞检项目上传文件-批量删除", notes="飞检项目上传文件-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ybFjUploadService.deleteBatch(ids);
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "飞检项目上传文件-通过id查询")
	@ApiOperation(value="飞检项目上传文件-通过id查询", notes="飞检项目上传文件-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		YbFjUpload ybFjUpload = ybFjUploadService.getById(id);
		return Result.ok(ybFjUpload);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param ybFjUpload
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, YbFjUpload ybFjUpload) {
      return super.exportXls(request, ybFjUpload, YbFjUpload.class, "飞检项目上传文件");
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
      return super.importExcel(request, response, YbFjUpload.class);
  }

	 /**
	  * 通过ids查询
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "飞检项目上传文件-生成文件批量下载")
	 @ApiOperation(value="飞检项目上传文件-生成文件批量下载", notes="飞检项目上传文件-生成文件批量下载")
	 @GetMapping(value = "/downloadZip")
	 public Result<?> downloadZip(@RequestParam(name="ids",required=true) String ids,String templateCode,String resultIds) throws Exception {
		try{
			String zipPath = ybFjUploadService.downloadZip(ids,templateCode,resultIds);
			return Result.ok(zipPath);
		}catch (Exception e){
			return Result.error("打包文件批量下载失败");
		}
	 }

}
