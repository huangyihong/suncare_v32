package com.ai.modules.ybFj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.ClueStepFileDto;
import com.ai.modules.ybFj.dto.ProjectFileDto;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjProjectClueFile;
import com.ai.modules.ybFj.service.IYbFjProjectClueFileService;
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
 * @Description: 飞检项目线索附件
 * @Author: jeecg-boot
 * @Date:   2023-03-08
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目线索附件")
@RestController
@RequestMapping("/fj/clue/file")
public class YbFjProjectClueFileController extends JeecgController<YbFjProjectClueFile, IYbFjProjectClueFileService> {
	@Autowired
	private IYbFjProjectClueFileService ybFjProjectClueFileService;
	
	/**
	 * 分页列表查询
	 *
	 * @param ybFjProjectClueFile
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目线索附件-分页列表查询")
	@ApiOperation(value="飞检项目线索附件-分页列表查询", notes="飞检项目线索附件-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(YbFjProjectClueFile ybFjProjectClueFile,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YbFjProjectClueFile> queryWrapper = QueryGenerator.initQueryWrapper(ybFjProjectClueFile, req.getParameterMap());
		Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 /**
	  * 通过id删除
	  *
	  * @param fileId
	  * @return
	  */
	 @RequiresPermissions("fj:clue:file:del")
	 @AutoLog(value = "飞检项目线索附件-通过id删除")
	 @ApiOperation(value="飞检项目线索附件-通过id删除", notes="飞检项目线索附件-通过id删除")
	 @DeleteMapping(value = "/delete")
	 public Result<?> delete(@RequestParam(name="id",required=true) String fileId) throws Exception {
		 ybFjProjectClueFileService.deleteClueFile(fileId);
		 return Result.ok("删除成功!");
	 }

	 /**
	  * 批量删除
	  *
	  * @param fileIds
	  * @return
	  */
	 @RequiresPermissions("fj:clue:file:del")
	 @AutoLog(value = "飞检项目线索附件-批量删除")
	 @ApiOperation(value="飞检项目线索附件-批量删除", notes="飞检项目线索附件-批量删除")
	 @DeleteMapping(value = "/deleteBatch")
	 public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String fileIds) throws Exception {
		 this.ybFjProjectClueFileService.deleteClueFiles(fileIds);
		 return Result.ok("批量删除成功！");
	 }

	 @RequiresPermissions("fj:clue:file:download")
	 @AutoLog(value = "飞检项目线索附件-下载附件")
	 @ApiOperation(value="飞检项目线索附件-下载附件", notes="飞检项目线索附件-下载附件")
	 @GetMapping(value = "/download")
	 public void download(@RequestParam(name="fileId",required=true) String fileId,
						  HttpServletRequest request, HttpServletResponse response) throws Exception {
		 ybFjProjectClueFileService.download(fileId, request, response);
	 }

	 @RequiresPermissions("fj:clue:file:download")
	 @AutoLog(value = "飞检项目线索附件-批量下载附件")
	 @ApiOperation(value="飞检项目线索附件-批量下载附件", notes="飞检项目线索附件-批量下载附件")
	 @GetMapping(value = "/downloadZip")
	 public void downloadZip(@RequestParam(name="fileIds",required=true) String fileIds,
						  HttpServletRequest request, HttpServletResponse response) throws Exception {
		 ybFjProjectClueFileService.downloadZip(response, fileIds);
	 }

	 @AutoLog(value = "文档管理-线索提交环节（全部）")
	 @ApiOperation(value="文档管理-线索提交环节（全部）", notes="文档管理-线索提交环节（全部）")
	 @GetMapping(value = "/submitStepAllFileList")
	 public Result<?> submitStepAllFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
										 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										 HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_SUBMIT);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-线索提交环节（我提交的）")
	 @ApiOperation(value="文档管理-线索提交环节（我提交的）", notes="文档管理-线索提交环节（我提交的）")
	 @GetMapping(value = "/mineSubmitStepFileList")
	 public Result<?> mineSubmitStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
									@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_SUBMIT);
		 dto.setMine(true);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-线索提交环节（线索提交）")
	 @ApiOperation(value="文档管理-线索提交环节（线索提交）", notes="文档管理-线索提交环节（线索提交）")
	 @GetMapping(value = "/submitStepFileList")
	 public Result<?> submitStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
											 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											 HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_SUBMIT);
		 dto.setStepType(DcFjConstants.CLUE_STEP_SUBMIT);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-线索提交环节（审核反馈）")
	 @ApiOperation(value="文档管理-线索提交环节（审核反馈）", notes="文档管理-线索提交环节（审核反馈）")
	 @GetMapping(value = "/submitAuditStepFileList")
	 public Result<?> submitAuditStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
										 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										 HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_SUBMIT);
		 dto.setStepType(DcFjConstants.FILE_STEP_TASK_AUDIT);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "飞检项目线索附件-根据任务ID获取附件列表")
	 @ApiOperation(value="飞检项目线索附件-根据任务ID获取附件列表", notes="飞检项目线索附件-根据任务ID获取附件列表")
	 @GetMapping(value = "/queryProjectTaskFiles")
	 public Result<List<YbFjProjectClueFile>> queryProjectTaskFiles(@RequestParam(name="taskId",required=true) String taskId,
									HttpServletRequest req) {
		 Result<List<YbFjProjectClueFile>> result = new Result<>();
		 List<YbFjProjectClueFile> dataList = ybFjProjectClueFileService.queryProjectTaskFiles(taskId);
		 result.setResult(dataList);
		 return result;
	 }

	 @AutoLog(value = "文档管理-现场检查环节（全部）")
	 @ApiOperation(value="文档管理-现场检查环节（全部）", notes="文档管理-现场检查环节（全部）")
	 @GetMapping(value = "/onsiteStepAllFileList")
	 public Result<?> onsiteStepAllFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
											@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_ONSITE);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-现场检查环节（我提交的）")
	 @ApiOperation(value="文档管理-现场检查环节（我提交的）", notes="文档管理-现场检查环节（我提交的）")
	 @GetMapping(value = "/mineOnsiteStepFileList")
	 public Result<?> mineOnsiteStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
											 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											 HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_ONSITE);
		 dto.setMine(true);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-现场检查环节（我输出的）")
	 @ApiOperation(value="文档管理-现场检查环节（我输出的）", notes="文档管理-现场检查环节（我输出的）")
	 @GetMapping(value = "/outOnsiteStepFileList")
	 public Result<?> outOnsiteStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
											 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											 HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_ONSITE);
		 dto.setMine(true);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesByTemplate(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-检查全流程管理（全部文件）")
	 @ApiOperation(value="文档管理-检查全流程管理（全部文件）", notes="检查全流程管理（全部文件）")
	 @GetMapping(value = "/projectAllFileList")
	 public Result<?> projectAllFileList(@RequestParam(name="projectId",required=true) String projectId,
										 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										 HttpServletRequest req) {
		 ProjectFileDto dto = new ProjectFileDto();
		 dto.setProjectId(projectId);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectFiles(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-检查全流程管理（内部上传）")
	 @ApiOperation(value="文档管理-检查全流程管理（内部上传）", notes="文档管理-检查全流程管理（内部上传）")
	 @GetMapping(value = "/projectUpFileList")
	 public Result<?> projectUpFileList(@RequestParam(name="projectId",required=true) String projectId,
										@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										HttpServletRequest req) {
		 ProjectFileDto dto = new ProjectFileDto();
		 dto.setProjectId(projectId);
		 dto.setOperType(DcFjConstants.FILE_OPER_TYPE_UP);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectFiles(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-检查全流程管理（内部输出）")
	 @ApiOperation(value="文档管理-检查全流程管理（内部输出）", notes="文档管理-检查全流程管理（内部输出）")
	 @GetMapping(value = "/projectOutFileList")
	 public Result<?> projectOutFileList(@RequestParam(name="projectId",required=true) String projectId,
										@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										HttpServletRequest req) {
		 ProjectFileDto dto = new ProjectFileDto();
		 dto.setProjectId(projectId);
		 dto.setOperType(DcFjConstants.FILE_OPER_TYPE_OUT);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectFiles(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-检查全流程管理（医院上传）")
	 @ApiOperation(value="文档管理-检查全流程管理（医院上传）", notes="文档管理-检查全流程管理（医院上传）")
	 @GetMapping(value = "/projectOrgUpFileList")
	 public Result<?> projectOrgUpFileList(@RequestParam(name="projectId",required=true) String projectId,
										@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										HttpServletRequest req) {
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectOrgUploadFiles(page, projectId);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-医院复核环节（全部）")
	 @ApiOperation(value="文档管理-医院复核环节（全部）", notes="文档管理-医院复核环节（全部）")
	 @GetMapping(value = "/hospStepAllFileList")
	 public Result<?> hospStepAllFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
											@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_HOSP);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-医院复核环节（我提交的）")
	 @ApiOperation(value="文档管理-医院复核环节（我提交的）", notes="文档管理-医院复核环节（我提交的）")
	 @GetMapping(value = "/mineHospStepFileList")
	 public Result<?> mineHospStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
											 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											 HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_HOSP);
		 dto.setMine(true);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-医院复核环节（审核反馈）")
	 @ApiOperation(value="文档管理-医院复核环节（审核反馈）", notes="文档管理-医院复核环节（审核反馈）")
	 @GetMapping(value = "/hospAuditStepFileList")
	 public Result<?> hospAuditStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
											  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											  HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_HOSP);
		 dto.setStepType(DcFjConstants.FILE_STEP_HOSP_TASK_AUDIT);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-线上核减环节（全部）")
	 @ApiOperation(value="文档管理-线上核减环节（全部）", notes="文档管理-线上核减环节（全部）")
	 @GetMapping(value = "/cutStepAllFileList")
	 public Result<?> cutStepAllFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
										  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										  HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_CUT);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-线上核减环节（我提交的）")
	 @ApiOperation(value="文档管理-线上核减环节（我提交的）", notes="文档管理-线上核减环节（我提交的）")
	 @GetMapping(value = "/mineCutStepFileList")
	 public Result<?> mineCutStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
										   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										   HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_CUT);
		 dto.setMine(true);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-线上核减环节（医院上传的）")
	 @ApiOperation(value="文档管理-线上核减环节（医院上传的）", notes="文档管理-线上核减环节（医院上传的）")
	 @GetMapping(value = "/cutOrgSubmitFileList")
	 public Result<?> cutOrgSubmitFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
										   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										   HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_CUT);
		 dto.setStepType(DcFjConstants.FILE_STEP_CUT_TASK);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }

	 @AutoLog(value = "文档管理-线上核减环节（审核反馈）")
	 @ApiOperation(value="文档管理-线上核减环节（审核反馈）", notes="文档管理-线上核减环节（审核反馈）")
	 @GetMapping(value = "/cutAuditStepFileList")
	 public Result<?> cutAuditStepFileList(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
											@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											HttpServletRequest req) {
		 ClueStepFileDto dto = new ClueStepFileDto();
		 dto.setProjectOrgId(projectOrgId);
		 dto.setStepGroup(DcFjConstants.CLUE_STEP_CUT);
		 dto.setStepType(DcFjConstants.FILE_STEP_CUT_TASK_AUDIT);
		 Page<YbFjProjectClueFile> page = new Page<YbFjProjectClueFile>(pageNo, pageSize);
		 IPage<YbFjProjectClueFile> pageList = ybFjProjectClueFileService.queryProjectClueFilesFromStep(page, dto);
		 return Result.ok(pageList);
	 }
}
