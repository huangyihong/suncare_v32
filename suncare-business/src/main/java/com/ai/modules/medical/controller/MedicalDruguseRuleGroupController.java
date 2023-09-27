package com.ai.modules.medical.controller;

import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.ai.modules.medical.entity.MedicalDruguseRuleGroup;
import com.ai.modules.medical.mapper.MedicalClinicalMapper;
import com.ai.modules.medical.service.IMedicalDruguseRuleGroupService;
import com.alibaba.fastjson.JSONObject;
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
import java.util.*;

 /**
 * @Description: 合理用药配置条件组
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
@Slf4j
@Api(tags="合理用药配置条件组")
@RestController
@RequestMapping("/medical/medicalDruguseRuleGroup")
public class MedicalDruguseRuleGroupController extends JeecgController<MedicalDruguseRuleGroup, IMedicalDruguseRuleGroupService> {
	@Autowired
	private IMedicalDruguseRuleGroupService medicalDruguseRuleGroupService;

	 @Autowired
	 MedicalClinicalMapper medicalClinicalMapper;
	/**
	 * 分页列表查询
	 *
	 * @param medicalDruguseRuleGroup
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "合理用药配置条件组-分页列表查询")
	@ApiOperation(value="合理用药配置条件组-分页列表查询", notes="合理用药配置条件组-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalDruguseRuleGroup medicalDruguseRuleGroup,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalDruguseRuleGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalDruguseRuleGroup, req.getParameterMap());
		Page<MedicalDruguseRuleGroup> page = new Page<MedicalDruguseRuleGroup>(pageNo, pageSize);
		IPage<MedicalDruguseRuleGroup> pageList = medicalDruguseRuleGroupService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	 @AutoLog(value = "合理用药配置条件组-单个规则条件组查询")
	 @ApiOperation(value="合理用药配置条件组-单个规则条件组查询", notes="合理用药配置条件组-单个规则条件组查询")
	 @GetMapping(value = "/listByRuleId")
	 public Result<?> listByRuleId(@RequestParam(name="ruleId") String ruleId,
								   HttpServletRequest req) {
		 List<MedicalDruguseRuleGroup> list = medicalDruguseRuleGroupService.list(
				 new QueryWrapper<MedicalDruguseRuleGroup>()
						 .eq("RULE_ID", ruleId)
						 .orderByAsc("GROUP_NO")
		 );
		 return Result.ok(list);
	 }

	 @AutoLog(value = "合理用药配置条件组-单个规则条件组查询")
	 @ApiOperation(value="合理用药配置条件组-单个规则条件组查询", notes="合理用药配置条件组-单个规则条件组查询")
	 @GetMapping(value = "/listRuleGroupByRuleId")
	 public Result<?> listRuleByRuleId(@RequestParam(name="ruleId") String ruleId,
									HttpServletRequest req) {
		 List<MedicalDruguseRuleGroup> list = medicalDruguseRuleGroupService.list(
		 		new QueryWrapper<MedicalDruguseRuleGroup>()
						.eq("RULE_ID", ruleId)
						.orderByAsc("GROUP_NO")
		 );

		 // code
		 Set<String> diseaseGroupSet = new HashSet<>();
		 Set<String> treatGroupSet = new HashSet<>();
		 Set<String> treatmentGroupSet = new HashSet<>();
		 // code -> name
		 Map<String, String> diseaseGroupMap = new HashMap<>();
		 Map<String, String> treatGroupMap = new HashMap<>();
		 Map<String, String> treatmentMap = new HashMap<>();

		 for(MedicalDruguseRuleGroup bean: list){
			 if(StringUtils.isNotBlank(bean.getDiseaseGroups())){
				 String[] diseaseGroups = bean.getDiseaseGroups().split("[|,]");
				 diseaseGroupSet.addAll(Arrays.asList(diseaseGroups));
			 }

			 if(StringUtils.isNotBlank(bean.getTreatGroups())) {
				 String[] treatGroups = bean.getTreatGroups().split("[|,]");
				 treatGroupSet.addAll(Arrays.asList(treatGroups));
			 }
			 if(StringUtils.isNotBlank(bean.getTreatmentGroups())) {
				 String[] treatmentGroups = bean.getTreatmentGroups().split("[|,]");
				 treatmentGroupSet.addAll(Arrays.asList(treatmentGroups));
			 }
		 }

		 // 数据库取的对应
		 if (diseaseGroupSet.size() > 0) {
			 List<MedicalCodeNameVO> groupMaps = this.medicalClinicalMapper.queryGroupCodeIdInCodes(
			 		diseaseGroupSet.toArray(new String[0]), "5");
			 for (MedicalCodeNameVO map : groupMaps) {
				 diseaseGroupMap.put(map.getCode(),map.getName());
			 }
		 }

		 if (treatGroupSet.size() > 0) {
			 List<MedicalCodeNameVO> groupMaps = this.medicalClinicalMapper.queryGroupCodeIdInCodes(
					 treatGroupSet.toArray(new String[0]), "1");
			 for (MedicalCodeNameVO map : groupMaps) {
				 treatGroupMap.put(map.getCode(),map.getName());
			 }
		 }

		 if (treatmentGroupSet.size() > 0) {
			 List<MedicalCodeNameVO> groupMaps = this.medicalClinicalMapper.queryItemCodeIdInCodes(
					 treatmentGroupSet.toArray(new String[0]), "1");
			 for (MedicalCodeNameVO map : groupMaps) {
				 treatmentMap.put(map.getCode(),map.getName());
			 }
		 }

		 JSONObject result = new JSONObject();
		 result.put("list", list);
		 result.put("diseaseGroupMap", diseaseGroupMap);
		 result.put("treatGroupMap", treatGroupMap);
		 result.put("treatmentMap", treatmentMap);

		 return Result.ok(result);
	 }

	/**
	 * 添加
	 *
	 * @param medicalDruguseRuleGroup
	 * @return
	 */
	@AutoLog(value = "合理用药配置条件组-添加")
	@ApiOperation(value="合理用药配置条件组-添加", notes="合理用药配置条件组-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalDruguseRuleGroup medicalDruguseRuleGroup) {
		medicalDruguseRuleGroupService.save(medicalDruguseRuleGroup);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalDruguseRuleGroup
	 * @return
	 */
	@AutoLog(value = "合理用药配置条件组-编辑")
	@ApiOperation(value="合理用药配置条件组-编辑", notes="合理用药配置条件组-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalDruguseRuleGroup medicalDruguseRuleGroup) {
		medicalDruguseRuleGroupService.updateById(medicalDruguseRuleGroup);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "合理用药配置条件组-通过id删除")
	@ApiOperation(value="合理用药配置条件组-通过id删除", notes="合理用药配置条件组-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalDruguseRuleGroupService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "合理用药配置条件组-批量删除")
	@ApiOperation(value="合理用药配置条件组-批量删除", notes="合理用药配置条件组-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalDruguseRuleGroupService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "合理用药配置条件组-通过id查询")
	@ApiOperation(value="合理用药配置条件组-通过id查询", notes="合理用药配置条件组-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalDruguseRuleGroup medicalDruguseRuleGroup = medicalDruguseRuleGroupService.getById(id);
		return Result.ok(medicalDruguseRuleGroup);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalDruguseRuleGroup
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalDruguseRuleGroup medicalDruguseRuleGroup) {
      return super.exportXls(request, medicalDruguseRuleGroup, MedicalDruguseRuleGroup.class, "合理用药配置条件组");
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
      return super.importExcel(request, response, MedicalDruguseRuleGroup.class);
  }

}
