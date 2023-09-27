package com.ai.modules.drg.controller;

import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.service.IDrgCatalogService;
import com.ai.modules.drg.vo.DrgCatalogVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description: DRG分组目录版本表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
@Slf4j
@Api(tags="DRG分组目录版本表")
@RestController
@RequestMapping("/drg/drgCatalog")
public class DrgCatalogController extends JeecgController<DrgCatalog, IDrgCatalogService> {
	@Autowired
	private IDrgCatalogService drgCatalogService;

	/**
	 * 分页列表查询
	 *
	 * @param drgCatalog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "DRG分组目录版本表-分页列表查询")
	@ApiOperation(value="DRG分组目录版本表-分页列表查询", notes="DRG分组目录版本表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(DrgCatalog drgCatalog,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<DrgCatalog> queryWrapper = QueryGenerator.initQueryWrapper(drgCatalog, req.getParameterMap());
		Page<DrgCatalog> page = new Page<DrgCatalog>(pageNo, pageSize);
		IPage<DrgCatalogVo> pageList = drgCatalogService.pageVO(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param drgCatalog
	 * @return
	 */
	@AutoLog(value = "DRG分组目录版本表-添加")
	@ApiOperation(value="DRG分组目录版本表-添加", notes="DRG分组目录版本表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody DrgCatalog drgCatalog) {
		drgCatalogService.save(drgCatalog);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param drgCatalog
	 * @return
	 */
	@AutoLog(value = "DRG分组目录版本表-编辑")
	@ApiOperation(value="DRG分组目录版本表-编辑", notes="DRG分组目录版本表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody DrgCatalog drgCatalog) {
		drgCatalogService.updateById(drgCatalog);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DRG分组目录版本表-通过id删除")
	@ApiOperation(value="DRG分组目录版本表-通过id删除", notes="DRG分组目录版本表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		return drgCatalogService.delete(id);
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "DRG分组目录版本表-批量删除")
	@ApiOperation(value="DRG分组目录版本表-批量删除", notes="DRG分组目录版本表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        return drgCatalogService.deleteBatch(ids);
	}

	/**
	 * 判断是否重复
	 * @param request
	 * @param versionCode
	 * @param catalogType
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DRG分组目录版本表-判断版本编号是否重复 ")
	@ApiOperation(value="DRG分组目录版本表-判断版本编号是否重复 ", notes="DRG分组目录版本表-判断版本编号是否重复")
	@GetMapping(value = "/isExistName")
	public Result<?> isExistName(HttpServletRequest request,@RequestParam(name="versionCode",required=true)String versionCode,@RequestParam(name="catalogType",required=true)String catalogType,String id){
		Result<Boolean> result = new Result<>();
		QueryWrapper<DrgCatalog> queryWrapper = new QueryWrapper<DrgCatalog>();
		queryWrapper.eq("VERSION_CODE", versionCode);
		queryWrapper.eq("CATALOG_TYPE", catalogType);
		if(StringUtils.isNotBlank(id)){
			queryWrapper.notIn("ID", id);
		}
		List<DrgCatalog> list = this.drgCatalogService.list(queryWrapper);
		if(list.size()>0){
			result.setSuccess(false);
			result.setMessage("版本编号已存在");
			return result;
		}
		result.setSuccess(true);
		return result;
	}



	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "DRG分组目录版本表-通过id查询")
	@ApiOperation(value="DRG分组目录版本表-通过id查询", notes="DRG分组目录版本表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		DrgCatalog drgCatalog = drgCatalogService.getById(id);
		return Result.ok(drgCatalog);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param drgCatalog
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, DrgCatalog drgCatalog) {
      return super.exportXls(request, drgCatalog, DrgCatalog.class, "DRG分组目录版本表");
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
      return super.importExcel(request, response, DrgCatalog.class);
  }

}
