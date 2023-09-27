package com.ai.modules.his.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.his.entity.HisFormalFlowRuleGrade;
import com.ai.modules.his.service.IHisFormalFlowRuleGradeService;
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
 * @Description: 模型流程节点规则备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="模型流程节点规则备份")
@RestController
@RequestMapping("/his/hisFormalFlowRuleGrade")
public class HisFormalFlowRuleGradeController extends JeecgController<HisFormalFlowRuleGrade, IHisFormalFlowRuleGradeService> {
	@Autowired
	private IHisFormalFlowRuleGradeService hisFormalFlowRuleGradeService;

	 /**
	  * 通过caseId查询
	  *
	  * @param caseId
	  * @return
	  */
	 @AutoLog(value = "评分表-通过caseId查询")
	 @ApiOperation(value="评分表-通过caseId查询", notes="评分表-通过caseId查询")
	 @GetMapping(value = "/queryByCaseId")
	 public Result<?> queryByCaseId(@RequestParam(name="caseId") String caseId,
									@RequestParam(name="batchId") String batchId) {
		 List<HisFormalFlowRuleGrade> list = hisFormalFlowRuleGradeService.list(
		 		new QueryWrapper<HisFormalFlowRuleGrade>()
						.eq("BATCH_ID",batchId)
						.eq("CASE_ID",caseId));
		 return Result.ok(list);
	 }

	/**
	 * 分页列表查询
	 *
	 * @param hisFormalFlowRuleGrade
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "模型流程节点规则备份-分页列表查询")
	@ApiOperation(value="模型流程节点规则备份-分页列表查询", notes="模型流程节点规则备份-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(HisFormalFlowRuleGrade hisFormalFlowRuleGrade,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<HisFormalFlowRuleGrade> queryWrapper = QueryGenerator.initQueryWrapper(hisFormalFlowRuleGrade, req.getParameterMap());
		Page<HisFormalFlowRuleGrade> page = new Page<HisFormalFlowRuleGrade>(pageNo, pageSize);
		IPage<HisFormalFlowRuleGrade> pageList = hisFormalFlowRuleGradeService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param hisFormalFlowRuleGrade
	 * @return
	 */
	@AutoLog(value = "模型流程节点规则备份-添加")
	@ApiOperation(value="模型流程节点规则备份-添加", notes="模型流程节点规则备份-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody HisFormalFlowRuleGrade hisFormalFlowRuleGrade) {
		hisFormalFlowRuleGradeService.save(hisFormalFlowRuleGrade);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param hisFormalFlowRuleGrade
	 * @return
	 */
	@AutoLog(value = "模型流程节点规则备份-编辑")
	@ApiOperation(value="模型流程节点规则备份-编辑", notes="模型流程节点规则备份-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody HisFormalFlowRuleGrade hisFormalFlowRuleGrade) {
		hisFormalFlowRuleGradeService.updateById(hisFormalFlowRuleGrade);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "模型流程节点规则备份-通过id删除")
	@ApiOperation(value="模型流程节点规则备份-通过id删除", notes="模型流程节点规则备份-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		hisFormalFlowRuleGradeService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "模型流程节点规则备份-批量删除")
	@ApiOperation(value="模型流程节点规则备份-批量删除", notes="模型流程节点规则备份-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.hisFormalFlowRuleGradeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "模型流程节点规则备份-通过id查询")
	@ApiOperation(value="模型流程节点规则备份-通过id查询", notes="模型流程节点规则备份-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		HisFormalFlowRuleGrade hisFormalFlowRuleGrade = hisFormalFlowRuleGradeService.getById(id);
		return Result.ok(hisFormalFlowRuleGrade);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param hisFormalFlowRuleGrade
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, HisFormalFlowRuleGrade hisFormalFlowRuleGrade) {
      return super.exportXls(request, hisFormalFlowRuleGrade, HisFormalFlowRuleGrade.class, "模型流程节点规则备份");
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
      return super.importExcel(request, response, HisFormalFlowRuleGrade.class);
  }

}
