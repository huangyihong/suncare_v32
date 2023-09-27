package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.dto.YbFjProjectClueCutDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueOnsiteDto;
import com.ai.modules.ybFj.entity.YbFjProjectClue;
import com.ai.modules.ybFj.entity.YbFjProjectClueCut;
import com.ai.modules.ybFj.entity.YbFjUserOrg;
import com.ai.modules.ybFj.service.IYbFjProjectClueCutService;
import com.ai.modules.ybFj.service.IYbFjProjectClueService;
import com.ai.modules.ybFj.service.IYbFjUserOrgService;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.ai.modules.ybFj.vo.YbFjProjectClueCutVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

 /**
 * @Description: 飞检项目线索核减环节
 * @Author: jeecg-boot
 * @Date:   2023-03-14
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目线索核减环节")
@RestController
@RequestMapping("/fj/clue/cut")
public class YbFjProjectClueCutController extends JeecgController<YbFjProjectClueCut, IYbFjProjectClueCutService> {
	@Autowired
	private IYbFjProjectClueCutService ybFjProjectClueCutService;
	 @Autowired
	 private IYbFjProjectClueService ybFjProjectClueService;
	 @Autowired
	 private IYbFjUserOrgService userOrgService;

	 @AutoLog(value = "飞检项目线索核减环节-线索总览列表")
	 @ApiOperation(value="飞检项目线索核减环节-线索总览列表", notes="飞检项目线索核减环节-线索总览列表")
	 @GetMapping(value = "/list")
	 public Result<IPage<YbFjProjectClueCut>> queryPageList(YbFjProjectClueOnsiteDto dto,
															   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															   HttpServletRequest req) throws Exception {
		 Page<YbFjProjectClueCut> page = new Page<YbFjProjectClueCut>(pageNo, pageSize);
		 IPage<YbFjProjectClueCut> pageList = ybFjProjectClueCutService.queryCutClues(page, dto);
		 Result<IPage<YbFjProjectClueCut>> result = new Result<>();
		 result.setResult(pageList);
		 return result;
	 }
	
	/**
	 * 添加
	 *
	 * @param dto
	 * @return
	 */
	@RequiresPermissions("fj:clue:cut:save")
	@AutoLog(value = "飞检项目线索核减环节-保存核减")
	@ApiOperation(value="飞检项目线索核减环节-保存核减", notes="飞检项目线索核减环节-保存核减")
	@PostMapping(value = "/save")
	public Result<?> add(@RequestBody YbFjProjectClueCutDto dto) throws Exception {
		ybFjProjectClueCutService.saveProjectClueCut(dto);
		return Result.ok("保存成功！");
	}

	 @RequiresPermissions("fj:clue:push")
	 @AutoLog(value = "飞检项目线索核减环节-线索推送到医院")
	 @ApiOperation(value="飞检项目线索核减环节-线索推送到医院", notes="飞检项目线索核减环节-线索推送到医院")
	 @PostMapping(value = "/pushOrg")
	 public Result<?> pushOrg(@RequestParam(name="clueIds",required=true) String clueIds) throws Exception {
		 ybFjProjectClueCutService.pushProjectClueToOrg(clueIds);
		 return Result.ok("推送成功！");
	 }

	 @AutoLog(value = "飞检项目线索核减环节-线索实时汇总")
	 @ApiOperation(value="飞检项目线索核减环节-线索实时汇总", notes="飞检项目线索核减环节-线索实时汇总")
	 @GetMapping(value = "/statClueInCut")
	 public Result<StatStepClueVo> statClueInCut(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
												 HttpServletRequest req) throws Exception {
		 QueryWrapper<YbFjProjectClueCut> wrapper = new QueryWrapper<>();
		 wrapper.eq("project_org_id", projectOrgId);
		 Result<StatStepClueVo> result = new Result<>();
		 result.setResult(ybFjProjectClueCutService.statisticsStepClue(wrapper));
		 return result;
	 }

	 @RequiresPermissions("fj:clue:download")
	 @AutoLog(value = "飞检项目线索核减环节-线索导出")
	 @ApiOperation(value="飞检项目线索核减环节-线索导出", notes="飞检项目线索核减环节-线索导出")
	 @GetMapping(value = "/export")
	 public void export(@RequestParam(name="projectOrgId",required=true) String projectOrgId,
						HttpServletRequest request,
						HttpServletResponse response) throws Exception {
		 ybFjProjectClueCutService.exportProjectClues(projectOrgId, response);
	 }

	 @AutoLog(value = "飞检项目线索核减环节-线索附件下载（压缩包）")
	 @ApiOperation(value="飞检项目线索核减环节-线索附件下载（压缩包）", notes="飞检项目线索核减环节-飞检项目现场检查线索（压缩包）")
	 @GetMapping(value = "/downloadZip")
	 public void downloadZip(@RequestParam(name="clueId",required=true) String clueId,
							 HttpServletRequest request, HttpServletResponse response) throws Exception {
		 ybFjProjectClueCutService.downloadCutClueFilesZip(clueId, response);
	 }

	 @RequiresPermissions("fj:clue:cut:audit")
	 @AutoLog(value = "飞检项目线索核减环节-线索审核（线上核减环节）")
	 @ApiOperation(value="飞检项目线索核减环节-线索审核（线上核减环节）", notes="飞检项目线索核减环节-线索审核（线上核减环节）")
	 @PostMapping(value = "/cutAudit")
	 public Result<?> cutAudit(@RequestParam(name="clueId",required=true) String clueId,
							   @RequestParam(name="auditState",required=true) String auditState,
							   @RequestParam(name="auditOpinion") String auditOpinion) throws Exception {
		 ybFjProjectClueService.auditCutClue(clueId, auditState, auditOpinion);
		 return Result.ok("线索审核成功！");
	 }

	 @RequiresPermissions("fj:clue:cut:audit")
	 @AutoLog(value = "飞检项目线索核减环节-批量线索审核（线上核减环节）")
	 @ApiOperation(value="飞检项目线索核减环节-批量线索审核（线上核减环节）", notes="飞检项目线索核减环节-批量线索审核（线上核减环节）")
	 @PostMapping(value = "/cutAuditBatch")
	 public Result<?> cutAuditBatch(@RequestParam(name="clueIds",required=true) String clueIds,
									@RequestParam(name="auditState",required=true) String auditState,
									@RequestParam(name="auditOpinion") String auditOpinion) throws Exception {
		 ybFjProjectClueService.auditCutClues(clueIds, auditState, auditOpinion);
		 return Result.ok("线索审核成功！");
	 }

	 @AutoLog(value = "飞检项目线索核减环节（医院端）-线索总览")
	 @ApiOperation(value="飞检项目线索核减环节（医院端）-线索总览", notes="飞检项目线索核减环节（医院端）-线索总览")
	 @GetMapping(value = "/queryClueByOrg")
	 public Result<IPage<YbFjProjectClueCutVo>> queryClueByOrg(YbFjProjectClue ybFjProjectClue,
															   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															   HttpServletRequest req) throws Exception {
		 Page<YbFjProjectClueCutVo> page = new Page<YbFjProjectClueCutVo>(pageNo, pageSize);
		 String orgId = this.getUserOrgId();
		 IPage<YbFjProjectClueCutVo> pageList = ybFjProjectClueService.queryProjectClueByOrg(page, orgId, ybFjProjectClue);
		 Result<IPage<YbFjProjectClueCutVo>> result = new Result<>();
		 result.setResult(pageList);
		 return result;
	 }

	 @AutoLog(value = "飞检项目线索核减环节（医院端）-线索实时汇总")
	 @ApiOperation(value="飞检项目线索核减环节（医院端）-线索实时汇总", notes="飞检项目线索核减环节（医院端）-线索实时汇总")
	 @GetMapping(value = "/statClueInCutByOrg")
	 public Result<StatStepClueVo> statClueInCutByOrg() throws Exception {
		 String orgId = this.getUserOrgId();
		 Result<StatStepClueVo> result = new Result<>();
		 result.setResult(ybFjProjectClueService.statisticsStepClueByOrg(orgId));
		 return result;
	 }

	 @AutoLog(value = "飞检项目线索核减环节（医院端）-线索导出")
	 @ApiOperation(value="飞检项目线索核减环节（医院端）-线索导出", notes="飞检项目线索核减环节（医院端）-线索导出")
	 @GetMapping(value = "/exportByOrg")
	 public void exportByOrg(YbFjProjectClue ybFjProjectClue,
							 HttpServletRequest request,
							 HttpServletResponse response) throws Exception {
		 String orgId = this.getUserOrgId();
		 List<YbFjProjectClueCutVo> dataList = ybFjProjectClueService.queryProjectClueByOrg(orgId, ybFjProjectClue);
		 ybFjProjectClueService.exportOrgClientClues(dataList, response);
	 }

	 private String getUserOrgId() throws Exception {
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 YbFjUserOrg userOrg = userOrgService.getById(user.getId());
		 if(userOrg==null) {
			 throw new Exception("非医院端用户，没权限操作");
		 }
		 return userOrg.getOrgId();
	 }
}
