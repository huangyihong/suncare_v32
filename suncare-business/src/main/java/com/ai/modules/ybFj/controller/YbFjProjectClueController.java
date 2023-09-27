package com.ai.modules.ybFj.controller;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.YbFjProjectClueDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueOnsiteDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsite;
import com.ai.modules.ybFj.entity.YbFjUserOrg;
import com.ai.modules.ybFj.service.IYbFjProjectClueOnsiteService;
import com.ai.modules.ybFj.service.IYbFjUserOrgService;
import com.ai.modules.ybFj.service.impl.YbFjProjectClueServiceImpl;
import com.ai.modules.ybFj.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjProjectClue;
import com.ai.modules.ybFj.service.IYbFjProjectClueService;
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
 * @Description: 飞检项目线索
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目线索")
@RestController
@RequestMapping("/fj/clue")
public class YbFjProjectClueController extends JeecgController<YbFjProjectClue, IYbFjProjectClueService> {
	@Autowired
	private IYbFjProjectClueService ybFjProjectClueService;
	@Autowired
	private IYbFjProjectClueOnsiteService clueOnsiteService;
	 @Autowired
	 private IYbFjUserOrgService userOrgService;

	/**
	 * 分页列表查询
	 *
	 * @param ybFjProjectClue
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目线索-线索总览（线索提交环节）")
	@ApiOperation(value="飞检项目线索-线索总览（线索提交环节）", notes="飞检项目线索-线索总览（线索提交环节）")
	@GetMapping(value = "/list")
	public Result<IPage<YbFjProjectClue>> queryPageList(YbFjProjectClue ybFjProjectClue,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		QueryWrapper<YbFjProjectClue> queryWrapper = this.buileQueryWrapper(ybFjProjectClue);
		queryWrapper.orderByDesc("create_time").orderByAsc("seq");
		Page<YbFjProjectClue> page = new Page<YbFjProjectClue>(pageNo, pageSize);
		IPage<YbFjProjectClue> pageList = ybFjProjectClueService.page(page, queryWrapper);
		Result<IPage<YbFjProjectClue>> result = new Result<>();
		result.setResult(pageList);
		return result;
	}

	 @AutoLog(value = "飞检项目线索-线索总览（医院复核环节）")
	 @ApiOperation(value="飞检项目线索-线索总览（医院复核环节）", notes="飞检项目线索-线索总览（医院复核环节）")
	 @GetMapping(value = "/inHospStepClueList")
	 public Result<IPage<YbFjProjectClue>> inHospStepClueList(YbFjProjectClue ybFjProjectClue,
														 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														 HttpServletRequest req) throws Exception {
		 QueryWrapper<YbFjProjectClue> queryWrapper = this.buileQueryWrapper(ybFjProjectClue);
		 queryWrapper.isNotNull("hosp_audit_state");
		 queryWrapper.orderByDesc("hosp_step_time");
		 Page<YbFjProjectClue> page = new Page<YbFjProjectClue>(pageNo, pageSize);
		 IPage<YbFjProjectClue> pageList = ybFjProjectClueService.page(page, queryWrapper);
		 Result<IPage<YbFjProjectClue>> result = new Result<>();
		 result.setResult(pageList);
		 return result;
	 }

	 /*@AutoLog(value = "飞检项目线索-线索总览（现场检查环节）")
	 @ApiOperation(value="飞检项目线索-线索总览（现场检查环节）", notes="飞检项目线索-线索总览（现场检查环节）")
	 @GetMapping(value = "/inOnsiteStepClueList")
	 public Result<IPage<YbFjProjectClue>> inOnsiteStepClueList(YbFjProjectClue ybFjProjectClue,
															 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															 HttpServletRequest req) {
		 QueryWrapper<YbFjProjectClue> queryWrapper = QueryGenerator.initQueryWrapper(ybFjProjectClue, req.getParameterMap());
		 queryWrapper.eq("curr_step", DcFjConstants.CLUE_STEP_ONSITE);
		 Page<YbFjProjectClue> page = new Page<YbFjProjectClue>(pageNo, pageSize);
		 IPage<YbFjProjectClue> pageList = ybFjProjectClueService.page(page, queryWrapper);
		 Result<IPage<YbFjProjectClue>> result = new Result<>();
		 result.setResult(pageList);
		 return result;
	 }*/

	/**
	 * 添加
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "飞检项目线索-添加")
	@ApiOperation(value="飞检项目线索-添加", notes="飞检项目线索-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody YbFjProjectClueDto dto) throws Exception {
		ybFjProjectClueService.saveProjectClue(dto);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param dto
	 * @return
	 */
	@AutoLog(value = "飞检项目线索-编辑")
	@ApiOperation(value="飞检项目线索-编辑", notes="飞检项目线索-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody YbFjProjectClueDto dto) throws Exception {
		ybFjProjectClueService.updateProjectClue(dto);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param clueId
	 * @return
	 */
	@AutoLog(value = "飞检项目线索-通过id删除")
	@ApiOperation(value="飞检项目线索-通过id删除", notes="飞检项目线索-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String clueId) throws Exception {
		ybFjProjectClueService.removeProjectClue(clueId);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param clueIds
	 * @return
	 */
	@AutoLog(value = "飞检项目线索-批量删除")
	@ApiOperation(value="飞检项目线索-批量删除", notes="飞检项目线索-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String clueIds) throws Exception {
		this.ybFjProjectClueService.removeProjectClues(clueIds);
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param clueId
	 * @return
	 */
	@AutoLog(value = "飞检项目线索-通过id查询")
	@ApiOperation(value="飞检项目线索-通过id查询", notes="飞检项目线索-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="clueId",required=true) String clueId) {
		YbFjProjectClue ybFjProjectClue = ybFjProjectClueService.getById(clueId);
		return Result.ok(ybFjProjectClue);
	}

	 @RequiresPermissions("fj:clue:import")
	 @AutoLog(value = "飞检项目线索-线索上传（线索汇总）")
	 @ApiOperation(value="飞检项目线索-线索上传（线索汇总）", notes="飞检项目线索-线索上传（线索汇总）")
	 @PostMapping(value = "/import")
	 public Result<?> importExcel(@RequestParam MultipartFile file,
								  @RequestParam(name="projectOrgId",required=true) String projectOrgId,
								  HttpServletRequest req) throws Exception {
		 ybFjProjectClueService.importClue(projectOrgId, file);
		 return Result.ok("线索上传成功！");
	 }

	 @RequiresPermissions("fj:clue:import")
	 @AutoLog(value = "飞检项目线索-重新线索上传（线索汇总）")
	 @ApiOperation(value="飞检项目线索-重新线索上传（线索汇总）", notes="飞检项目线索-重新线索上传（线索汇总）")
	 @PostMapping(value = "/cover")
	 public Result<?> cover(@RequestParam MultipartFile file,
							@RequestParam(name="projectOrgId",required=true) String projectOrgId,
							HttpServletRequest req) throws Exception {
		 ybFjProjectClueService.coverClue(projectOrgId, file);
		 return Result.ok("线索上传成功！");
	 }

	 @RequiresPermissions("fj:clue:import")
	 @AutoLog(value = "飞检项目线索-线索明细上传")
	 @ApiOperation(value="飞检项目线索-线索明细上传", notes="飞检项目线索-线索明细上传")
	 @PostMapping(value = "/dtl/import")
	 public Result<?> importExcelDtl(@RequestParam MultipartFile file,
									 @RequestParam(name="clueId",required=true) String clueId,
								  HttpServletRequest req) throws Exception {
		 ybFjProjectClueService.importClueDtl(clueId, file);
		 return Result.ok("线索明细上传成功！");
	 }

	 @RequiresPermissions("fj:clue:import")
	 @AutoLog(value = "飞检项目线索-线索明细覆盖")
	 @ApiOperation(value="飞检项目线索-线索明细覆盖", notes="飞检项目线索-线索明细覆盖")
	 @PostMapping(value = "/dtl/cover")
	 public Result<?> coverDtl(@RequestParam MultipartFile file,
							@RequestParam(name="clueId",required=true) String clueId,
							HttpServletRequest req) throws Exception {
		 ybFjProjectClueService.coverClueDtl(clueId, file);
		 return Result.ok("线索明细上传成功！");
	 }

	 @AutoLog(value = "飞检项目线索-下载模板（线索汇总）")
	 @ApiOperation(value="飞检项目线索-下载模板（线索汇总）", notes="飞检项目线索-下载模板（线索汇总）")
	 @GetMapping(value = "/download/clue")
	 public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filePath = DcFjConstants.TEMPLATE_PATH + "clue.xlsx";
		ybFjProjectClueService.downTemplate(filePath, "线索汇总表", request, response);
	 }

	 @AutoLog(value = "飞检项目线索-下载模板（线索明细）")
	 @ApiOperation(value="飞检项目线索-下载模板（线索明细）", notes="飞检项目线索-下载模板（线索明细）")
	 @GetMapping(value = "/download/clueDtl")
	 public void downloadClueDtl(HttpServletRequest request, HttpServletResponse response) throws Exception {
		 String filePath = DcFjConstants.TEMPLATE_PATH + "project_clue_dtl.xlsx";
		 ybFjProjectClueService.downTemplate(filePath, "线索明细表", request, response);
	 }

	 @RequiresPermissions("fj:clue:submit:audit")
	 @AutoLog(value = "飞检项目线索-线索审核（线上提交环节）")
	 @ApiOperation(value="飞检项目线索-线索审核（线上提交环节）", notes="飞检项目线索-线索审核（线上提交环节）")
	 @PostMapping(value = "/audit")
	 public Result<?> audit(@RequestParam(name="clueId",required=true) String clueId,
						  @RequestParam(name="auditState",required=true) String auditState,
						  @RequestParam(name="auditOpinion") String auditOpinion) throws Exception {
		 ybFjProjectClueService.auditProjectClue(clueId, auditState, auditOpinion);
		 return Result.ok("线索审核成功！");
	 }

	 @RequiresPermissions("fj:clue:submit:audit")
	 @AutoLog(value = "飞检项目线索-批量线索审核（线上提交环节）")
	 @ApiOperation(value="飞检项目线索-批量线索审核（线上提交环节）", notes="飞检项目线索-批量线索审核（线上提交环节）")
	 @PostMapping(value = "/auditBatch")
	 public Result<?> auditBatch(@RequestParam(name="clueIds",required=true) String clueIds,
							@RequestParam(name="auditState",required=true) String auditState,
							@RequestParam(name="auditOpinion") String auditOpinion) throws Exception {
		 ybFjProjectClueService.auditProjectClues(clueIds, auditState, auditOpinion);
		 return Result.ok("线索审核成功！");
	 }

	 @RequiresPermissions("fj:clue:download")
	 @AutoLog(value = "飞检项目线索-线索附件下载（压缩包）")
	 @ApiOperation(value="飞检项目线索-线索附件下载（压缩包）", notes="飞检项目线索-线索附件下载（压缩包）")
	 @GetMapping(value = "/downloadZip")
	 public void downloadZip(@RequestParam(name="clueId",required=true) String clueId,
							 HttpServletRequest request, HttpServletResponse response) throws Exception {
		 ybFjProjectClueService.downloadProjectClueFilesZip(clueId, response);
	 }

	 @RequiresPermissions("fj:clue:push")
	 @AutoLog(value = "飞检项目线索-线索推送到其他环节")
	 @ApiOperation(value="飞检项目线索-线索推送到其他环节", notes="飞检项目线索-线索推送到其他环节")
	 @PostMapping(value = "/push")
	 public Result<?> push(@RequestParam(name="clueIds",required=true) String clueIds,
						   @RequestParam(name="step",required=true) String step,
						   @RequestParam(name="prevStep",required=true) String prevStep) throws Exception {
		 ybFjProjectClueService.pushProjectClue(clueIds, step, prevStep);
		 return Result.ok("推送成功！");
	 }

	 @AutoLog(value = "飞检项目线索-线索统计（按医院）")
	 @ApiOperation(value="飞检项目线索-线索统计（按医院）", notes="飞检项目线索-线索统计（按医院）")
	 @GetMapping(value = "/stat")
	 public Result<StatClueVo> stat(@RequestParam(name="projectOrgId",required=true) String projectOrgId) throws Exception {
		 StatProjectClueVo submit = ybFjProjectClueService.statisticsProjectClue(projectOrgId);
		 StatOnsiteClueVo onsite = clueOnsiteService.statisticsOnsiteClue(projectOrgId);
		 Result<StatClueVo> result = new Result<>();
		 result.setResult(new StatClueVo(submit, onsite));
		 return result;
	 }

	 @AutoLog(value = "飞检项目线索-统计项目管理线索数")
	 @ApiOperation(value="飞检项目线索-统计项目管理线索数", notes="飞检项目线索-统计项目管理线索数")
	 @GetMapping(value = "/statProjectClueAmount")
	 public Result<Integer> statProjectClueAmount(@RequestParam(name="projectId",required=true) String projectId) throws Exception {
		 Integer count = ybFjProjectClueService.statisticsProjectClueAmount(projectId);
		 Result<Integer> result = new Result<>();
		 result.setResult(count);
		 return result;
	 }

	 private QueryWrapper<YbFjProjectClue> buileQueryWrapper(YbFjProjectClue clue) throws Exception {
		 if(StringUtils.isBlank(clue.getProjectOrgId())) {
			 throw new Exception("参数projectOrgId不能为空");
		 }
		 QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
		 wrapper.eq("project_org_id", clue.getProjectOrgId());
		 if(StringUtils.isNotBlank(clue.getIssueType())) {
			 wrapper.eq("issue_type", clue.getIssueType());
		 }
		 if(StringUtils.isNotBlank(clue.getIssueSubtype())) {
			 wrapper.eq("issue_subtype", clue.getIssueSubtype());
		 }
		 if(StringUtils.isNotBlank(clue.getClueName())) {
			 wrapper.eq("clue_name", clue.getClueName());
		 }
		 if(StringUtils.isNotBlank(clue.getClueType())) {
			 wrapper.eq("clue_type", clue.getClueType());
		 }
		 return wrapper;
	 }

	 @AutoLog(value = "飞检项目线索-线索实时汇总（线索提交环节）")
	 @ApiOperation(value="飞检项目线索-线索实时汇总（线索提交环节）", notes="飞检项目线索-线索实时汇总（线索提交环节）")
	 @GetMapping(value = "/statClueInSubmit")
	 public Result<StatStepClueVo> statClueInSubmit(YbFjProjectClue ybFjProjectClue,
													HttpServletRequest req) throws Exception {
		 QueryWrapper<YbFjProjectClue> wrapper = this.buileQueryWrapper(ybFjProjectClue);
		 Result<StatStepClueVo> result = new Result<>();
		 result.setResult(ybFjProjectClueService.statisticsStepClue(DcFjConstants.CLUE_STEP_SUBMIT, wrapper));
		 return result;
	 }

	 @AutoLog(value = "飞检项目线索-线索实时汇总（医院复核环节）")
	 @ApiOperation(value="飞检项目线索-线索实时汇总（医院复核环节）", notes="飞检项目线索-线索实时汇总（医院复核环节）")
	 @GetMapping(value = "/statClueInHosp")
	 public Result<StatStepClueVo> statClueInHosp(YbFjProjectClue ybFjProjectClue,
													   HttpServletRequest req) throws Exception {
		 QueryWrapper<YbFjProjectClue> wrapper = this.buileQueryWrapper(ybFjProjectClue);
		 wrapper.isNotNull("hosp_audit_state");
		 Result<StatStepClueVo> result = new Result<>();
		 result.setResult(ybFjProjectClueService.statisticsStepClue(DcFjConstants.CLUE_STEP_HOSP, wrapper));
		 return result;
	 }

	 /*@AutoLog(value = "飞检项目线索-线索导出")
	 @ApiOperation(value="飞检项目线索-线索导出", notes="飞检项目线索-线索导出")
	 @GetMapping(value = "/export")
	 public void export(YbFjProjectClueDto dto,
						HttpServletRequest request,
						HttpServletResponse response) throws Exception {
		 QueryWrapper<YbFjProjectClue> queryWrapper = this.buileQueryWrapper(dto);
		 queryWrapper.orderByDesc("create_time");
		 List<YbFjProjectClue> dataList = ybFjProjectClueService.list(queryWrapper);
		 ybFjProjectClueService.exportProjectClues(dataList, response);
	 }*/
	 @RequiresPermissions("fj:clue:download")
	 @AutoLog(value = "飞检项目线索-线索导出")
	 @ApiOperation(value="飞检项目线索-线索导出", notes="飞检项目线索-线索导出")
	 @GetMapping(value = "/export")
	 public void export(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
						String stepType,
						HttpServletRequest request,
						HttpServletResponse response) throws Exception {
		 ybFjProjectClueService.exportProjectClues(projectOrgId, stepType, response);
	 }

	 private QueryWrapper<YbFjProjectClue> buileQueryWrapper(YbFjProjectClueDto dto) throws Exception {
		 if(StringUtils.isBlank(dto.getProjectOrgId())) {
			 throw new Exception("参数projectOrgId不能为空");
		 }
		 QueryWrapper<YbFjProjectClue> wrapper = new QueryWrapper<>();
		 wrapper.eq("project_org_id", dto.getProjectOrgId());
		 if(StringUtils.isNotBlank(dto.getSelections())) {
			 String clueId = dto.getSelections();
			 String[] ids = clueId.split(",");
			 wrapper.in("clue_id", ids);
		 }
		 if(StringUtils.isNotBlank(dto.getIssueType())) {
			 wrapper.eq("issue_type", dto.getIssueType());
		 }
		 if(StringUtils.isNotBlank(dto.getIssueSubtype())) {
			 wrapper.eq("issue_subtype", dto.getIssueSubtype());
		 }
		 if(StringUtils.isNotBlank(dto.getClueName())) {
			 wrapper.eq("clue_name", dto.getClueName());
		 }
		 if(StringUtils.isNotBlank(dto.getClueType())) {
			 wrapper.eq("clue_type", dto.getClueType());
		 }
		 return wrapper;
	 }

	 @RequiresPermissions("fj:clue:hosp:audit")
	 @AutoLog(value = "飞检项目线索-线索审核（医院复核环节）")
	 @ApiOperation(value="飞检项目线索-线索审核（医院复核环节）", notes="飞检项目线索-线索审核（医院复核环节）")
	 @PostMapping(value = "/hospAudit")
	 public Result<?> hospAudit(@RequestParam(name="clueId",required=true) String clueId,
							@RequestParam(name="auditState",required=true) String auditState,
							@RequestParam(name="auditOpinion") String auditOpinion) throws Exception {
		 ybFjProjectClueService.auditHospClue(clueId, auditState, auditOpinion);
		 return Result.ok("线索审核成功！");
	 }

	 @RequiresPermissions("fj:clue:hosp:audit")
	 @AutoLog(value = "飞检项目线索-批量线索审核（医院复核环节）")
	 @ApiOperation(value="飞检项目线索-批量线索审核（医院复核环节）", notes="飞检项目线索-批量线索审核（医院复核环节）")
	 @PostMapping(value = "/hospAuditBatch")
	 public Result<?> hospAuditBatch(@RequestParam(name="clueIds",required=true) String clueIds,
								 @RequestParam(name="auditState",required=true) String auditState,
								 @RequestParam(name="auditOpinion") String auditOpinion) throws Exception {
		 ybFjProjectClueService.auditHospClues(clueIds, auditState, auditOpinion);
		 return Result.ok("线索审核成功！");
	 }
}
