package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.YbFjProjectClueCutDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueOnsiteDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsite;
import com.ai.modules.ybFj.service.IYbFjProjectClueOnsiteService;
import com.ai.modules.ybFj.service.IYbFjProjectClueService;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

 /**
 * @Description: 飞检项目现场检查线索
 * @Author: jeecg-boot
 * @Date:   2023-03-15
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目现场检查线索")
@RestController
@RequestMapping("/fj/clue/onsite")
public class YbFjProjectClueOnsiteController extends JeecgController<YbFjProjectClueOnsite, IYbFjProjectClueOnsiteService> {
	@Autowired
	private IYbFjProjectClueOnsiteService ybFjProjectClueOnsiteService;
	@Autowired
	private IYbFjProjectClueService projectClueService;
	
	/**
	 * 分页列表查询
	 *
	 * @param dto
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目现场检查线索-线索总览列表")
	@ApiOperation(value="飞检项目现场检查线索-线索总览列表", notes="飞检项目现场检查线索-线索总览列表")
	@GetMapping(value = "/list")
	public Result<IPage<YbFjProjectClueOnsite>> queryPageList(YbFjProjectClueOnsiteDto dto,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		Page<YbFjProjectClueOnsite> page = new Page<YbFjProjectClueOnsite>(pageNo, pageSize);
		IPage<YbFjProjectClueOnsite> pageList = ybFjProjectClueOnsiteService.queryOnsiteClues(page, dto);
		Result<IPage<YbFjProjectClueOnsite>> result = new Result<>();
		result.setResult(pageList);
		return result;
	}

	/**
	 * 通过id查询
	 *
	 * @param clueId
	 * @return
	 */
	@AutoLog(value = "飞检项目现场检查线索-通过id查询")
	@ApiOperation(value="飞检项目现场检查线索-通过id查询", notes="飞检项目现场检查线索-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="clueId",required=true) String clueId) {
		YbFjProjectClueOnsite ybFjProjectClueOnsite = ybFjProjectClueOnsiteService.getById(clueId);
		return Result.ok(ybFjProjectClueOnsite);
	}

	 @AutoLog(value = "飞检项目现场检查线索-编辑")
	 @ApiOperation(value="飞检项目现场检查线索-编辑", notes="飞检项目现场检查线索-编辑")
	 @PutMapping(value = "/edit")
	 @RequiresPermissions("fj:clue:onsite:edit")
	 public Result<?> edit(@RequestBody YbFjProjectClueOnsiteDto dto) throws Exception {
		 ybFjProjectClueOnsiteService.updateProjectClueOnsite(dto);
		 return Result.ok("编辑成功!");
	 }

	 @RequiresPermissions("fj:clue:onsite:edit")
	 @AutoLog(value = "飞检项目现场检查线索-批量编辑")
	 @ApiOperation(value="飞检项目现场检查线索-批量编辑", notes="飞检项目现场检查线索-批量编辑")
	 @PutMapping(value = "/editBatch")
	 public Result<?> editBatch(@RequestBody List<YbFjProjectClueOnsiteDto> dtoList) throws Exception {
		 ybFjProjectClueOnsiteService.updateProjectClueOnsite(dtoList);
		 return Result.ok("编辑成功!");
	 }

	 /**
	  * 通过id删除
	  *
	  * @param clueId
	  * @return
	  */
	 @RequiresPermissions("fj:clue:onsite:del")
	 @AutoLog(value = "飞检项目现场检查线索-通过id删除")
	 @ApiOperation(value="飞检项目现场检查线索-通过id删除", notes="飞检项目线索-通过id删除")
	 @DeleteMapping(value = "/delete")
	 public Result<?> delete(@RequestParam(name="id",required=true) String clueId) throws Exception {
		 ybFjProjectClueOnsiteService.removeProjectClueOnsite(clueId);
		 return Result.ok("删除成功!");
	 }

	 /**
	  * 批量删除
	  *
	  * @param clueIds
	  * @return
	  */
	 @RequiresPermissions("fj:clue:onsite:del")
	 @AutoLog(value = "飞检项目现场检查线索-批量删除")
	 @ApiOperation(value="飞检项目现场检查线索-批量删除", notes="飞检项目线索-批量删除")
	 @DeleteMapping(value = "/deleteBatch")
	 public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String clueIds) throws Exception {
		 this.ybFjProjectClueOnsiteService.removeProjectClueOnsites(clueIds);
		 return Result.ok("批量删除成功！");
	 }

	 @AutoLog(value = "飞检项目现场检查线索-保存核减")
	 @ApiOperation(value="飞检项目现场检查线索-保存核减", notes="飞检项目现场检查线索-保存核减")
	 @PostMapping(value = "/cutSave")
	 public Result<?> cutSave(@RequestBody YbFjProjectClueCutDto dto) throws Exception {
		 ybFjProjectClueOnsiteService.saveProjectClueOnsiteCut(dto);
		 return Result.ok("保存成功！");
	 }

	 @RequiresPermissions("fj:clue:import")
	 @AutoLog(value = "飞检项目现场检查线索-线索上传（线索汇总）")
	 @ApiOperation(value="飞检项目现场检查线索-线索上传（线索汇总）", notes="飞检项目现场检查线索-线索上传（线索汇总）")
	 @PostMapping(value = "/import")
	 public Result<?> importExcel(@RequestParam MultipartFile file,
								  @RequestParam(name="projectOrgId",required=true) String projectOrgId,
								  HttpServletRequest req) throws Exception {
		 ybFjProjectClueOnsiteService.importOnsiteClue(projectOrgId, file);
		 return Result.ok("线索上传成功！");
	 }

	 @RequiresPermissions("fj:clue:import")
	 @AutoLog(value = "飞检项目现场检查线索-重新线索上传（线索汇总）")
	 @ApiOperation(value="飞检项目现场检查线索-重新线索上传（线索汇总）", notes="飞检项目现场检查线索-重新线索上传（线索汇总）")
	 @PostMapping(value = "/cover")
	 public Result<?> cover(@RequestParam MultipartFile file,
							@RequestParam(name="projectOrgId",required=true) String projectOrgId,
							HttpServletRequest req) throws Exception {
		 ybFjProjectClueOnsiteService.coverOnsiteClue(projectOrgId, file);
		 return Result.ok("线索上传成功！");
	 }

	 @RequiresPermissions("fj:clue:import")
	 @AutoLog(value = "飞检项目现场检查线索-线索明细上传")
	 @ApiOperation(value="飞检项目现场检查线索-线索明细上传", notes="飞检项目现场检查线索-线索明细上传")
	 @PostMapping(value = "/dtl/import")
	 public Result<?> importExcelDtl(@RequestParam MultipartFile file,
								  @RequestParam(name="clueId",required=true) String clueId,
								  HttpServletRequest req) throws Exception {
		 ybFjProjectClueOnsiteService.importOnsiteClueDtl(clueId, file);
		 return Result.ok("线索上传成功！");
	 }

	 @RequiresPermissions("fj:clue:import")
	 @AutoLog(value = "飞检项目现场检查线索-线索明细覆盖")
	 @ApiOperation(value="飞检项目现场检查线索-线索明细覆盖", notes="飞检项目现场检查线索-线索明细覆盖")
	 @PostMapping(value = "/dtl/cover")
	 public Result<?> coverDtl(@RequestParam MultipartFile file,
							@RequestParam(name="clueId",required=true) String clueId,
							HttpServletRequest req) throws Exception {
		 ybFjProjectClueOnsiteService.coverOnsiteClueDtl(clueId, file);
		 return Result.ok("线索上传成功！");
	 }

	 @AutoLog(value = "飞检项目现场检查线索-下载模板（线索汇总）")
	 @ApiOperation(value="飞检项目现场检查线索-下载模板（线索汇总）", notes="飞检项目现场检查线索-下载模板（线索汇总）")
	 @GetMapping(value = "/download/clue")
	 public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		 String filePath = DcFjConstants.TEMPLATE_PATH + "onsite_clue.xlsx";
		 projectClueService.downTemplate(filePath, "线索汇总表", request, response);
	 }

	 @AutoLog(value = "飞检项目现场检查线索-下载模板（线索明细）")
	 @ApiOperation(value="飞检项目现场检查线索-下载模板（线索明细）", notes="飞检项目现场检查线索-下载模板（线索明细）")
	 @GetMapping(value = "/download/clueDtl")
	 public void downloadClueDtl(HttpServletRequest request, HttpServletResponse response) throws Exception {
		 String filePath = DcFjConstants.TEMPLATE_PATH + "project_clue_dtl.xlsx";
		 projectClueService.downTemplate(filePath, "线索明细表", request, response);
	 }

	 @RequiresPermissions("fj:clue:download")
	 @AutoLog(value = "飞检项目现场检查线索-线索导出")
	 @ApiOperation(value="飞检项目现场检查线索-线索导出", notes="飞检项目现场检查线索-线索导出")
	 @GetMapping(value = "/export")
	 public void export(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
						HttpServletRequest request,
						HttpServletResponse response) throws Exception {
		ybFjProjectClueOnsiteService.exportOnsiteClues(projectOrgId, response);
	 }

	 /*@AutoLog(value = "飞检项目现场检查线索-线索实时汇总覆盖")
	 @ApiOperation(value="飞检项目现场检查线索-线索实时汇总覆盖", notes="飞检项目现场检查线索-线索实时汇总覆盖")
	 @PostMapping(value = "/coverClue")
	 public void coverClue(@RequestParam MultipartFile file,
	 		@RequestParam(name="projectOrgId",required=true) String projectOrgId,
	 		HttpServletRequest req) throws Exception {
		 ybFjProjectClueOnsiteService.importOnsiteClues(projectOrgId, file);
	 }*/

	 @AutoLog(value = "飞检项目现场检查线索-线索附件下载（压缩包）")
	 @ApiOperation(value="飞检项目现场检查线索-线索附件下载（压缩包）", notes="飞检项目线索-飞检项目现场检查线索（压缩包）")
	 @GetMapping(value = "/downloadZip")
	 public void downloadZip(@RequestParam(name="clueId",required=true) String clueId,
							 HttpServletRequest request, HttpServletResponse response) throws Exception {
		 ybFjProjectClueOnsiteService.downloadOnsiteClueFilesZip(clueId, response);
	 }

	 @RequiresPermissions("fj:clue:onsite:upload")
	 @AutoLog(value = "飞检项目现场检查线索-上传材料")
	 @ApiOperation(value="飞检项目现场检查线索-上传材料", notes="飞检项目现场检查线索-上传材料")
	 @PostMapping(value = "/upload")
	 public Result<?> upload(@RequestParam MultipartFile file,
								  @RequestParam(name="projectOrgId",required=true) String projectOrgId,
								  HttpServletRequest req) throws Exception {
		 ybFjProjectClueOnsiteService.uploadOnsiteFile(projectOrgId, file);
		 return Result.ok("上传成功！");
	 }

	 @AutoLog(value = "飞检项目现场检查线索-下载通知书")
	 @ApiOperation(value="飞检项目现场检查线索-下载通知书", notes="飞检项目现场检查线索-下载通知书")
	 @GetMapping(value = "/download/notice")
	 public void downloadNotice(HttpServletRequest request, HttpServletResponse response) throws Exception {
		 String filePath = DcFjConstants.TEMPLATE_PATH + "onsite_notice.docx";
		 projectClueService.downTemplate(filePath, "询问笔录+现场检查通知书.docx", request, response);
	 }

	 @AutoLog(value = "飞检项目现场检查线索-下载签字反馈表")
	 @ApiOperation(value="飞检项目现场检查线索-下载签字反馈表", notes="飞检项目现场检查线索-下载签字反馈表")
	 @GetMapping(value = "/download/feedback")
	 public void downloadFeedback(YbFjProjectClueOnsiteDto dto,
								  HttpServletRequest request,
								  HttpServletResponse response) throws Exception {
		 ybFjProjectClueOnsiteService.exportOnsiteFeedback(dto, response);
	 }

	 @RequiresPermissions("fj:clue:onsite:output")
	 @AutoLog(value = "飞检项目现场检查线索-归档文件输出")
	 @ApiOperation(value="飞检项目现场检查线索-归档文件输出", notes="飞检项目现场检查线索-归档文件输出")
	 @GetMapping(value = "/template/out")
	 public void templateOut(YbFjProjectClueOnsiteDto dto,
							 @RequestParam(name="templateCode",required=true) String templateCode,
							 HttpServletRequest request,
							 HttpServletResponse response) throws Exception {
		 ybFjProjectClueOnsiteService.outOnsiteClueTemplate(response, dto, templateCode.split(","));
	 }

	 @AutoLog(value = "飞检项目现场检查线索-线索实时汇总（现场检查环节）")
	 @ApiOperation(value="飞检项目现场检查线索-线索实时汇总（现场检查环节）", notes="飞检项目现场检查线索-线索实时汇总（现场检查环节）")
	 @GetMapping(value = "/statClueInOnsite")
	 public Result<StatStepClueVo> statClueInCut(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
												 HttpServletRequest req) throws Exception {
		 QueryWrapper<YbFjProjectClueOnsite> wrapper = new QueryWrapper<>();
		 wrapper.eq("project_org_id", projectOrgId);
		 Result<StatStepClueVo> result = new Result<>();
		 result.setResult(ybFjProjectClueOnsiteService.statisticsStepClue(wrapper));
		 return result;
	 }
}
