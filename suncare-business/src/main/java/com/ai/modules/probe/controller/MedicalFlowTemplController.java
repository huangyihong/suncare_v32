package com.ai.modules.probe.controller;

import com.ai.modules.probe.entity.MedicalFlowTempl;
import com.ai.modules.probe.entity.MedicalFlowTemplRule;
import com.ai.modules.probe.service.IMedicalFlowTemplRuleService;
import com.ai.modules.probe.service.IMedicalFlowTemplService;
import com.ai.modules.probe.vo.MedicalFlowTemplVO;
import com.alibaba.fastjson.JSONObject;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

 /**
 * @Description: 节点模板
 * @Author: jeecg-boot
 * @Date:   2020-04-17
 * @Version: V1.0
 */
@Slf4j
@Api(tags="节点模板")
@RestController
@RequestMapping("/probe/medicalFlowTempl")
public class MedicalFlowTemplController extends JeecgController<MedicalFlowTempl, IMedicalFlowTemplService> {
	@Autowired
	private IMedicalFlowTemplService medicalFlowTemplService;

	@Autowired
	private IMedicalFlowTemplRuleService medicalFlowTemplRuleService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalFlowTempl
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "节点模板-分页列表查询")
	@ApiOperation(value="节点模板-分页列表查询", notes="节点模板-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalFlowTempl medicalFlowTempl,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalFlowTempl> queryWrapper = QueryGenerator.initQueryWrapper(medicalFlowTempl, req.getParameterMap());
		Page<MedicalFlowTempl> page = new Page<MedicalFlowTempl>(pageNo, pageSize);
		IPage<MedicalFlowTempl> pageList = medicalFlowTemplService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalFlowTempl
	 * @return
	 */
	@AutoLog(value = "节点模板-添加")
	@ApiOperation(value="节点模板-添加", notes="节点模板-添加")
	@PostMapping(value = "/add")
	@Transactional
	public Result<?> add(@RequestBody MedicalFlowTemplVO medicalFlowTempl) {
		String code = medicalFlowTempl.getNodeCode();
		int count = medicalFlowTemplService.count(new QueryWrapper<MedicalFlowTempl>().eq("NODE_CODE",code));
		if(count > 0){
			return Result.error("当前编码已存在！");
		}
		// 插入节点主体
		medicalFlowTemplService.save(medicalFlowTempl);
		String nodeId = medicalFlowTempl.getNodeId();
		// 插入规则
		List<MedicalFlowTemplRule> ruleList = medicalFlowTempl.getRules();
		for (MedicalFlowTemplRule rule : ruleList) {
			rule.setNodeId(nodeId);
		}
		medicalFlowTemplRuleService.saveBatch(ruleList);

		return Result.ok(medicalFlowTempl);
	}

	/**
	 * 编辑
	 *
	 * @param medicalFlowTempl
	 * @return
	 */
	@AutoLog(value = "节点模板-编辑")
	@ApiOperation(value="节点模板-编辑", notes="节点模板-编辑")
	@PutMapping(value = "/edit")
	@Transactional
	public Result<?> edit(@RequestBody MedicalFlowTemplVO medicalFlowTempl) {
		String code = medicalFlowTempl.getNodeCode();
		int count = medicalFlowTemplService.count(new QueryWrapper<MedicalFlowTempl>().eq("NODE_CODE",code).notIn("NODE_ID",medicalFlowTempl.getNodeId()));
		if(count > 0){
			return Result.error("当前编码已存在！");
		}
		medicalFlowTemplService.updateById(medicalFlowTempl);

		String nodeId = medicalFlowTempl.getNodeId();
		// 移除旧规则
		medicalFlowTemplRuleService.remove(new QueryWrapper<MedicalFlowTemplRule>().eq("NODE_ID",nodeId));
		// 插入规则
		List<MedicalFlowTemplRule> ruleList = medicalFlowTempl.getRules();
		for (MedicalFlowTemplRule rule : ruleList) {
			rule.setNodeId(nodeId);
		}
		medicalFlowTemplRuleService.saveBatch(ruleList);

		return Result.ok(medicalFlowTempl);
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "节点模板-通过id删除")
	@ApiOperation(value="节点模板-通过id删除", notes="节点模板-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalFlowTemplService.removeById(id);
		// 移除旧规则
		medicalFlowTemplRuleService.remove(new QueryWrapper<MedicalFlowTemplRule>().eq("NODE_ID",id));
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "节点模板-批量删除")
	@ApiOperation(value="节点模板-批量删除", notes="节点模板-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		List<String> idList = Arrays.asList(ids.split(","));
		this.medicalFlowTemplService.removeByIds(idList);
		// 移除旧规则
		medicalFlowTemplRuleService.remove(new QueryWrapper<MedicalFlowTemplRule>().in("NODE_ID", idList));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "节点模板-通过id查询")
	@ApiOperation(value="节点模板-通过id查询", notes="节点模板-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalFlowTempl medicalFlowTempl = medicalFlowTemplService.getById(id);
		List<MedicalFlowTemplRule> ruleList = medicalFlowTemplRuleService.list(new QueryWrapper<MedicalFlowTemplRule>().eq("NODE_ID", id));

		JSONObject json = (JSONObject) JSONObject.toJSON(medicalFlowTempl);
		json.put("rules",ruleList);
		return Result.ok(medicalFlowTempl);
	}

	 /**
	  * 通过id查询
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "节点模板-通过id查询规则")
	 @ApiOperation(value="节点模板-通过id查询规则", notes="节点模板-通过id查询规则")
	 @GetMapping(value = "/queryRulesById")
	 public Result<?> queryRulesById(@RequestParam(name="id",required=true) String id) {
		 List<MedicalFlowTemplRule> ruleList = medicalFlowTemplRuleService.list(new QueryWrapper<MedicalFlowTemplRule>().eq("NODE_ID", id));

		 return Result.ok(ruleList);
	 }

	 /**
	  * 通过id查询
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "节点模板-通过id查询规则")
	 @ApiOperation(value="节点模板-通过id查询规则", notes="节点模板-通过id查询规则")
	 @GetMapping(value = "/queryRulesByIds")
	 public Result<?> queryRulesByIds(@RequestParam(name="ids",required=true) String ids) {
		 List<MedicalFlowTemplRule> ruleList = medicalFlowTemplRuleService.list(
		 		new QueryWrapper<MedicalFlowTemplRule>()
						.in("NODE_ID", Arrays.asList(ids.split(",")))
		 );

		 return Result.ok(ruleList);
	 }

  /**
   * 导出excel
   *
   * @param request
   * @param medicalFlowTempl
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalFlowTempl medicalFlowTempl) {
      return super.exportXls(request, medicalFlowTempl, MedicalFlowTempl.class, "节点模板");
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
      return super.importExcel(request, response, MedicalFlowTempl.class);
  }

}
