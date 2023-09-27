package com.ai.modules.probe.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.config.service.IMedicalSysDictService;
import com.ai.modules.engine.service.IEngineTrialService;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.ai.modules.probe.service.IMedicalProbeFlowRuleService;
import com.ai.modules.probe.vo.MedicalProbeCaseVO;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.sf.saxon.expr.Component;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.probe.entity.MedicalProbeCase;
import com.ai.modules.probe.service.IMedicalProbeCaseService;
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
 * @Description: 流程图
 * @Author: jeecg-boot
 * @Date:   2019-11-21
 * @Version: V1.0
 */
@Slf4j
@Api(tags="流程图")
@RestController
@RequestMapping("/probe/medicalProbeCase")
public class MedicalProbeCaseController extends JeecgController<MedicalProbeCase, IMedicalProbeCaseService> {
	@Autowired
	private IMedicalProbeCaseService medicalProbeCaseService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalProbeCase
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "流程图-分页列表查询")
	@ApiOperation(value="流程图-分页列表查询", notes="流程图-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalProbeCase medicalProbeCase,
								   String searchCode,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalProbeCase> queryWrapper = QueryGenerator.initQueryWrapper(medicalProbeCase, req.getParameterMap());

		if(StringUtils.isNotBlank(searchCode)) {
			String finalSearchCode = searchCode.substring(1, searchCode.length() - 1);
			queryWrapper.and(wrapper ->
					wrapper.like("CASE_CODE", finalSearchCode)
							.or().like("CASE_NAME", finalSearchCode)
							.or().inSql("CASE_CODE", "SELECT CASE_CODE FROM MEDICAL_PROBE_CASE where 1=1 and upper(CASE_CODE) like '%" + finalSearchCode.toUpperCase() + "%'")
			);
		}
		// 默认排序
		queryWrapper.orderByDesc("IF(CASE_STATUS = 'wait',2,1)","UPDATE_TIME");
//		log.info(queryWrapper.getCustomSqlSegment());
		Page<MedicalProbeCase> page = new Page<>(pageNo, pageSize);
		IPage<MedicalProbeCase> pageList = medicalProbeCaseService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 已提交分页列表查询
	 *
	 * @param medicalProbeCase
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "流程图-已提交分页列表查询")
	@ApiOperation(value="流程图-已提交分页列表查询", notes="流程图-已提交分页列表查询")
	@GetMapping(value = "/submitList")
	public Result<?> submitList(MedicalProbeCase medicalProbeCase,
								String searchCode,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		medicalProbeCase.setCaseStatus("submited");
		QueryWrapper<MedicalProbeCase> queryWrapper = QueryGenerator.initQueryWrapper(medicalProbeCase, req.getParameterMap());
		if(StringUtils.isNotBlank(searchCode)) {
			String finalSearchCode = searchCode.substring(1, searchCode.length() - 1);
			queryWrapper.and(wrapper ->
					wrapper.like("CASE_CODE", finalSearchCode)
							.or().like("CASE_NAME", finalSearchCode)
							.or().inSql("CASE_CODE", "SELECT CASE_CODE FROM MEDICAL_PROBE_CASE where 1=1 and upper(CASE_CODE) like '%" + finalSearchCode.toUpperCase() + "%'")
			);
		}
		Page<MedicalProbeCase> page = new Page<MedicalProbeCase>(pageNo, pageSize);
		IPage<MedicalProbeCase> pageList = medicalProbeCaseService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalProbeCase
	 * @return
	 */
	@AutoLog(value = "流程图-添加")
	@ApiOperation(value="流程图-添加", notes="流程图-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalProbeCase medicalProbeCase) {
		medicalProbeCaseService.save(medicalProbeCase);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalProbeCase
	 * @return
	 */
	@AutoLog(value = "流程图-编辑")
	@ApiOperation(value="流程图-编辑", notes="流程图-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalProbeCase medicalProbeCase) {
		medicalProbeCaseService.updateById(medicalProbeCase);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "流程图-通过id删除")
	@ApiOperation(value="流程图-通过id删除", notes="流程图-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalProbeCaseService.removeProbeCaseById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "流程图-批量删除")
	@ApiOperation(value="流程图-批量删除", notes="流程图-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalProbeCaseService.removeProbeCaseByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "流程图-通过id查询")
	@ApiOperation(value="流程图-通过id查询", notes="流程图-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalProbeCase medicalProbeCase = medicalProbeCaseService.getById(id);
		return Result.ok(medicalProbeCase);
	}


	 /**
	  * 添加模型探查信息
	  *
	  * @param medicalProbeCaseVO
	  * @return
	  */
	 @AutoLog(value = "流程图-添加模型探查信息")
	 @ApiOperation(value="流程图-添加模型探查信息", notes="流程图-添加模型探查信息")
	 @PostMapping(value = "/addProbeCase")
	 public Result<?> addProbeCase(@RequestBody MedicalProbeCaseVO medicalProbeCaseVO) {
		 Date nowTime = new Date();
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 medicalProbeCaseVO.setCaseId(IdUtils.uuid());
//		 medicalProbeCaseVO.setCaseCode(DateUtils.mmddhhmmss.format(nowTime));
		 medicalProbeCaseVO.setCaseStatus("wait");
		 medicalProbeCaseVO.setCaseVersion(1.0f);
//		 medicalProbeCaseVO.setCreateTime(nowTime); //自动注入
		 medicalProbeCaseVO.setCreateUserid(user.getId());
		 medicalProbeCaseVO.setCreateUsername(user.getRealname());
		 medicalProbeCaseVO.setUpdateTime(nowTime); // 排序用
		 medicalProbeCaseService.addProbeCase(medicalProbeCaseVO, medicalProbeCaseVO.getRules());
		 return Result.ok(medicalProbeCaseVO);
	 }

	 /**
	  * 更新模型探查信息
	  *
	  * @param medicalProbeCaseVO
	  * @return
	  */
	 @AutoLog(value = "流程图-更新模型探查信息")
	 @ApiOperation(value="流程图-更新模型探查信息", notes="流程图-更新模型探查信息")
	 @PostMapping(value = "/updateProbeCase")
	 public Result<?> updateProbeCase(@RequestBody MedicalProbeCaseVO medicalProbeCaseVO) {
	 	 medicalProbeCaseService.updateProbeCase(medicalProbeCaseVO, medicalProbeCaseVO.getRules());
		 return Result.ok("修改成功！");
	 }

	 /**
	  * 通过id查询所有探查信息
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "流程图-通过id查询所有探查信息")
	 @ApiOperation(value="流程图-通过id查询所有探查信息", notes="流程图-通过id查询所有rule")
	 @GetMapping(value = "/getProbeCaseById")
	 public Result<?> getProbeCaseById(@RequestParam(name="id") String id, HttpServletRequest req) {
		 JSONObject jsonObject = medicalProbeCaseService.getProbeCaseById(id);
		 String copyCreate = req.getParameter("copyCreate");
		 if(StringUtils.isNotEmpty(copyCreate)){
			 jsonObject.put("caseCode", UUIDGenerator.getShortCode());
		 }
		 return Result.ok(jsonObject);
	 }
	 /**
	  * 通过id查询所有探查信息
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "流程图-通过ids查询所有探查信息")
	 @ApiOperation(value="流程图-通过ids查询所有探查信息", notes="流程图-通过id查询所有rule")
	 @GetMapping(value = "/getProbeCaseByIds")
	 public Result<?> getProbeCaseByIds(@RequestParam(name="ids") String ids) {

		 return Result.ok(medicalProbeCaseService.getProbeCaseByIds(Arrays.asList(ids.split(","))));
	 }

	 /**
	  * 获取CODE
	  *
	  * @return
	  */
	 @AutoLog(value = "流程图-获取CODE")
	 @ApiOperation(value="流程图-获取CODE", notes="流程图-获取CODE")
	 @GetMapping(value = "/getProbeCaseCode")
	 public Result<?> getProbeCaseCode() {
		 return Result.ok((Object) UUIDGenerator.getShortCode());
	 }

	 /**
	  * 提交流程图信息
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "流程图-提交流程图信息")
	 @ApiOperation(value="流程图-提交流程图信息", notes="流程图-提交流程图信息")
	 @GetMapping(value = "/setProbeCaseSubmit")
	 public Result<?> setProbeCaseSubmit(@RequestParam(name="ids") String ids) {
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

		 MedicalProbeCase medicalProbeCase = new MedicalProbeCase();
		 medicalProbeCase.setCaseStatus("submited");
//		 medicalProbeCase.setUpdateTime(new Date());
		 medicalProbeCase.setUpdateUserid(user.getId());
		 medicalProbeCase.setUpdateUsername(user.getRealname());
		 /*medicalProbeCase.setSubmitTime(new Date());
		 medicalProbeCase.setSubmitUserid(user.getId());
		 medicalProbeCase.setSubmitUsername(user.getRealname());*/
		 medicalProbeCaseService.update(medicalProbeCase,new QueryWrapper<MedicalProbeCase>()
				 .in("CASE_ID",Arrays.asList(ids.split(",")))
				 .ne("CASE_STATUS","submited"));
		 return Result.ok("提交成功！");
	 }
	 /**
	  * 驳回流程图信息
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "流程图-驳回流程图信息")
	 @ApiOperation(value="流程图-驳回流程图信息", notes="流程图-驳回流程图信息")
	 @GetMapping(value = "/setProbeCaseReject")
	 public Result<?> setProbeCaseReject(@RequestParam(name="ids") String ids) {
		 LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

		 MedicalProbeCase medicalProbeCase = new MedicalProbeCase();
		 medicalProbeCase.setCaseStatus("reject");
//		 medicalProbeCase.setUpdateTime(new Date());
		 medicalProbeCase.setUpdateUserid(user.getId());
		 medicalProbeCase.setUpdateUsername(user.getRealname());
		 medicalProbeCaseService.update(medicalProbeCase,new QueryWrapper<MedicalProbeCase>()
				 .in("CASE_ID",Arrays.asList(ids.split(",")))
				 .eq("CASE_STATUS","submited"));
		 return Result.ok("操作成功！");
	 }
}
