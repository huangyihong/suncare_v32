package com.ai.modules.formal.controller;

import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.ai.modules.formal.service.IMedicalFormalCaseItemRelaService;
import com.ai.modules.formal.vo.MedicalFormalCaseItemRelaVO;
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
import java.util.List;

 /**
 * @Description: 模型关联项目药品或组
 * @Author: jeecg-boot
 * @Date:   2020-07-17
 * @Version: V1.0
 */
@Slf4j
@Api(tags="模型关联项目药品或组")
@RestController
@RequestMapping("/formal/medicalFormalCaseItemRela")
public class MedicalFormalCaseItemRelaController extends JeecgController<MedicalFormalCaseItemRela, IMedicalFormalCaseItemRelaService> {
	@Autowired
	private IMedicalFormalCaseItemRelaService medicalFormalCaseItemRelaService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalFormalCaseItemRela
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "模型关联项目药品或组-分页列表查询")
	@ApiOperation(value="模型关联项目药品或组-分页列表查询", notes="模型关联项目药品或组-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalFormalCaseItemRela medicalFormalCaseItemRela,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalFormalCaseItemRela> queryWrapper = QueryGenerator.initQueryWrapper(medicalFormalCaseItemRela, req.getParameterMap());
		Page<MedicalFormalCaseItemRela> page = new Page<MedicalFormalCaseItemRela>(pageNo, pageSize);
		IPage<MedicalFormalCaseItemRela> pageList = medicalFormalCaseItemRelaService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalFormalCaseItemRela
	 * @return
	 */
	@AutoLog(value = "模型关联项目药品或组-添加")
	@ApiOperation(value="模型关联项目药品或组-添加", notes="模型关联项目药品或组-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalFormalCaseItemRela medicalFormalCaseItemRela) {
		medicalFormalCaseItemRelaService.save(medicalFormalCaseItemRela);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalFormalCaseItemRela
	 * @return
	 */
	@AutoLog(value = "模型关联项目药品或组-编辑")
	@ApiOperation(value="模型关联项目药品或组-编辑", notes="模型关联项目药品或组-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalFormalCaseItemRela medicalFormalCaseItemRela) {
		medicalFormalCaseItemRelaService.updateById(medicalFormalCaseItemRela);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "模型关联项目药品或组-通过id删除")
	@ApiOperation(value="模型关联项目药品或组-通过id删除", notes="模型关联项目药品或组-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalFormalCaseItemRelaService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "模型关联项目药品或组-批量删除")
	@ApiOperation(value="模型关联项目药品或组-批量删除", notes="模型关联项目药品或组-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalFormalCaseItemRelaService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "模型关联项目药品或组-通过id查询")
	@ApiOperation(value="模型关联项目药品或组-通过id查询", notes="模型关联项目药品或组-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalFormalCaseItemRela medicalFormalCaseItemRela = medicalFormalCaseItemRelaService.getById(id);
		return Result.ok(medicalFormalCaseItemRela);
	}

	 @AutoLog(value = "模型关联项目药品或组-通过模型ID查询")
	 @ApiOperation(value="模型关联项目药品或组-通过模型ID查询", notes="模型关联项目药品或组-通过模型ID查询")
	 @GetMapping(value = "/queryByCaseId")
	 public Result<?> queryByCaseId(@RequestParam(name="caseId") String caseId) {
		 MedicalFormalCaseItemRela medicalFormalCaseItemRela = medicalFormalCaseItemRelaService.getOne(
		 		new QueryWrapper<MedicalFormalCaseItemRela>().eq("CASE_ID", caseId));
		 return Result.ok(medicalFormalCaseItemRela);
	 }

	 /**
	  * 通过批次batchId和模型ID查询
	  *
	  * @param batchId
	  * @param caseIds
	  * @return
	  */
	 @AutoLog(value = "模型关联项目药品或组-通过批次batchId和模型ID查询")
	 @ApiOperation(value="模型关联项目药品或组-通过批次batchId和模型ID查询", notes="模型关联项目药品或组-通过批次batchId和模型ID查询")
	 @GetMapping(value = "/listVoByBatchIdAndCaseIds")
	 public Result<?> listVoByBatchIdAndCaseIds(@RequestParam(name="batchId") String batchId,@RequestParam(name="caseIds") String caseIds) {
		 List<MedicalFormalCaseItemRelaVO> list = medicalFormalCaseItemRelaService.listVoByBatchIdAndCaseIds(batchId, caseIds.split(","));
		 return Result.ok(list);
	 }

  /**
   * 导出excel
   *
   * @param request
   * @param medicalFormalCaseItemRela
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalFormalCaseItemRela medicalFormalCaseItemRela) {
      return super.exportXls(request, medicalFormalCaseItemRela, MedicalFormalCaseItemRela.class, "模型关联项目药品或组");
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
      return super.importExcel(request, response, MedicalFormalCaseItemRela.class);
  }


 }
