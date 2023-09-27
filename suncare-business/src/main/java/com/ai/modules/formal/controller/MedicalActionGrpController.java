package com.ai.modules.formal.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.ai.common.utils.IdUtils;
import com.ai.modules.formal.entity.MedicalActionGrp;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.service.IMedicalActionGrpService;
import com.ai.modules.formal.service.IMedicalFormalCaseService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

 /**
 * @Description: 不合理行为组表
 * @Author: jeecg-boot
 * @Date:   2019-12-02
 * @Version: V1.0
 */
@Slf4j
@Api(tags="不合理行为组表")
@RestController
@RequestMapping("/formal/medicalActionGrp")
public class MedicalActionGrpController extends JeecgController<MedicalActionGrp, IMedicalActionGrpService> {
	@Autowired
	private IMedicalActionGrpService medicalActionGrpService;

	@Autowired
    private IMedicalFormalCaseService medicalFormalCaseService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalActionGrp
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "不合理行为组表-分页列表查询")
	@ApiOperation(value="不合理行为组表-分页列表查询", notes="不合理行为组表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalActionGrp medicalActionGrp,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalActionGrp> queryWrapper = QueryGenerator.initQueryWrapper(medicalActionGrp, req.getParameterMap());
		Page<MedicalActionGrp> page = new Page<MedicalActionGrp>(pageNo, pageSize);
		IPage<MedicalActionGrp> pageList = medicalActionGrpService.page(page, queryWrapper);
		return Result.ok(pageList);
	}


	@AutoLog(value = "不合理行为组表-所有列表查询")
	@ApiOperation(value="不合理行为组表-所有列表查询", notes="不合理行为组表-所有列表查询")
	@GetMapping(value = "/allList")
	public Result<List<MedicalActionGrp>> allList(HttpServletRequest req) {
		Result<List<MedicalActionGrp>> result = new Result<>();
		LambdaQueryWrapper<MedicalActionGrp> query = new LambdaQueryWrapper<>();
		query.orderByAsc(MedicalActionGrp::getSortNo);
		List<MedicalActionGrp> list = medicalActionGrpService.list(query);
		result.setSuccess(true);
		result.setResult(list);
		return result;
	}

	@AutoLog(value = "不合理行为组表-树列表查询")
	@ApiOperation(value="不合理行为组表-树列表查询", notes="不合理行为组表-树列表查询")
	@GetMapping(value = "/treeList")
	public Result<?> treeList(HttpServletRequest req) {
		LambdaQueryWrapper<MedicalActionGrp> query = new LambdaQueryWrapper<>();
		query.orderByAsc(MedicalActionGrp::getSortNo);
		List<MedicalActionGrp> list = medicalActionGrpService.list(query);
		List<JSONObject> allTree = new ArrayList<JSONObject>();
		JSONObject scopedSlots = new JSONObject();
		scopedSlots.put("title", "custom");
		for(MedicalActionGrp bean:list) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("title", bean.getActionGrpName());
			jsonObject.put("key", bean.getActionGrpId());
			jsonObject.put("type", "hasEdit");//是否可以编辑删除
			jsonObject.put("scopedSlots", scopedSlots);
			LambdaQueryWrapper<MedicalFormalCase> queryFormal = new LambdaQueryWrapper<>();
//			queryFormal.eq(MedicalFormalCase::getActionGrpId,bean.getActionGrpId());
			List<MedicalFormalCase> childrenList = medicalFormalCaseService.list(queryFormal);
			List<JSONObject> childrenTree = new ArrayList<JSONObject>();
			for(MedicalFormalCase childrenBean:childrenList) {
				JSONObject childrenObject = new JSONObject();
				childrenObject.put("title", childrenBean.getActionName());
				childrenObject.put("key", childrenBean.getCaseId());
				childrenObject.put("scopedSlots",scopedSlots);
				childrenTree.add(childrenObject);
			}
			jsonObject.put("children", childrenTree);
			allTree.add(jsonObject);
		}
		return Result.ok(allTree);
	}



	/**
	 * 添加
	 *
	 * @param medicalActionGrp
	 * @return
	 */
	@AutoLog(value = "不合理行为组表-添加")
	@ApiOperation(value="不合理行为组表-添加", notes="不合理行为组表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalActionGrp medicalActionGrp) {
		medicalActionGrp.setActionGrpId(IdUtils.uuid());
		medicalActionGrpService.save(medicalActionGrp);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalActionGrp
	 * @return
	 */
	@AutoLog(value = "不合理行为组表-编辑")
	@ApiOperation(value="不合理行为组表-编辑", notes="不合理行为组表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalActionGrp medicalActionGrp) {
		medicalActionGrpService.updateById(medicalActionGrp);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合理行为组表-通过id删除")
	@ApiOperation(value="不合理行为组表-通过id删除", notes="不合理行为组表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalActionGrpService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "不合理行为组表-批量删除")
	@ApiOperation(value="不合理行为组表-批量删除", notes="不合理行为组表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalActionGrpService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不合理行为组表-通过id查询")
	@ApiOperation(value="不合理行为组表-通过id查询", notes="不合理行为组表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalActionGrp medicalActionGrp = medicalActionGrpService.getById(id);
		return Result.ok(medicalActionGrp);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalActionGrp
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalActionGrp medicalActionGrp) {
      return super.exportXls(request, medicalActionGrp, MedicalActionGrp.class, "不合理行为组表");
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
      return super.importExcel(request, response, MedicalActionGrp.class);
  }

}
