package com.ai.modules.medical.controller;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.config.entity.MedicalDrugGroupItem;
import com.ai.modules.config.entity.MedicalProjectGroupItem;
import com.ai.modules.config.service.IMedicalDrugGroupItemService;
import com.ai.modules.config.service.IMedicalDrugGroupService;
import com.ai.modules.config.service.IMedicalProjectGroupItemService;
import com.ai.modules.config.service.IMedicalProjectGroupService;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.ai.modules.medical.vo.MedicalClinicalRangeGroupVO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.service.IMedicalClinicalRangeGroupService;
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
 * @Description: 临床路径范围组
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
@Slf4j
@Api(tags="临床路径范围组")
@RestController
@RequestMapping("/medical/medicalClinicalRangeGroup")
public class MedicalClinicalRangeGroupController extends JeecgController<MedicalClinicalRangeGroup, IMedicalClinicalRangeGroupService> {
	@Autowired
	private IMedicalClinicalRangeGroupService medicalClinicalRangeGroupService;

	 @Autowired
	 private IMedicalDrugGroupService medicalDrugGroupService;

	 @Autowired
	 private IMedicalProjectGroupService medicalProjectGroupService;

	 /**
	  * 通过临床路径ID查询
	  *
	  * @param clinicalId
	  * @return
	  */
	 @AutoLog(value = "临床路径项目范围-通过临床路径ID查询")
	 @ApiOperation(value="临床路径项目范围-通过临床路径ID查询", notes="临床路径项目范围-通过临床路径ID查询")
	 @GetMapping(value = "/queryByClinicalId")
	 public Result<?> queryByClinicalId(@RequestParam(name="clinicalId") String clinicalId,String type ) {
	     QueryWrapper<MedicalClinicalRangeGroup> queryWrapper = new QueryWrapper<MedicalClinicalRangeGroup>()
                 .eq("t.CLINICAL_ID",clinicalId)
				 .orderByAsc("t.GROUP_CODE");
		 if(StringUtils.isNotBlank(type)){
		     queryWrapper.eq("t.GROUP_TYPE", type);
         }
         List<MedicalClinicalRangeGroupVO> list = medicalClinicalRangeGroupService.listDetail(queryWrapper,type);
		 return Result.ok(list);
	 }

	 /**
	  * 通过临床路径ID查询子项
	  *
	  * @param clinicalId
	  * @return
	  */
	 @AutoLog(value = "临床路径项目范围-通过临床路径ID查询子项")
	 @ApiOperation(value="临床路径项目范围-通过临床路径ID查询子项", notes="临床路径项目范围-通过临床路径ID查询子项")
	 @GetMapping(value = "/queryGroupAndItemByClinicalId")
	 public Result<?> queryItemByClinicalId(@RequestParam(name="clinicalId") String clinicalId,String type ) {
		 QueryWrapper<MedicalClinicalRangeGroup> queryWrapper = new QueryWrapper<MedicalClinicalRangeGroup>()
				 .eq("CLINICAL_ID",clinicalId)
				 .orderByAsc("GROUP_CODE");
		 if(StringUtils.isNotBlank(type)){
			 queryWrapper.eq("GROUP_TYPE", type);
		 }
		 List<MedicalClinicalRangeGroup> list = medicalClinicalRangeGroupService.list(queryWrapper);
		 List<MedicalClinicalRangeGroup> drugGroups = new ArrayList<>();
		 List<MedicalClinicalRangeGroup> projectGroups = new ArrayList<>();
		 for(MedicalClinicalRangeGroup group: list){
		 	if("drug".equals(group.getGroupType())){
				drugGroups.add(group);
			} else if("project".equals(group.getGroupType())){
				projectGroups.add(group);
			}
		 }

		 List<MedicalGroupVO> drugItems = new ArrayList<>();
		 List<MedicalGroupVO> projectItems = new ArrayList<>();
		 if(drugGroups.size() > 0){
		 	 List<String> drugGroupCodes = drugGroups.stream().map(MedicalClinicalRangeGroup::getGroupCode).collect(Collectors.toList());
			 drugItems = medicalDrugGroupService.queryGroupItemByGroupCodes(drugGroupCodes);
		 }

		 if(projectGroups.size() > 0){
			 List<String> projectGroupCodes = projectGroups.stream().map(MedicalClinicalRangeGroup::getGroupCode).collect(Collectors.toList());
			 projectItems = medicalProjectGroupService.queryGroupItemByGroupCodes(projectGroupCodes);
		 }

		 JSONObject result = new JSONObject();
		 result.put("drugGroups",drugGroups);
		 result.put("projectGroups",projectGroups);
		 result.put("drugItems",drugItems);
		 result.put("projectItems",projectItems);

		 return Result.ok(result);
	 }

	/**
	 * 分页列表查询
	 *
	 * @param medicalClinicalRangeGroup
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "临床路径范围组-分页列表查询")
	@ApiOperation(value="临床路径范围组-分页列表查询", notes="临床路径范围组-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalClinicalRangeGroup medicalClinicalRangeGroup,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalClinicalRangeGroup> queryWrapper = QueryGenerator.initQueryWrapper(medicalClinicalRangeGroup, req.getParameterMap());
		Page<MedicalClinicalRangeGroup> page = new Page<MedicalClinicalRangeGroup>(pageNo, pageSize);
		IPage<MedicalClinicalRangeGroup> pageList = medicalClinicalRangeGroupService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalClinicalRangeGroup
	 * @return
	 */
	@AutoLog(value = "临床路径范围组-添加")
	@ApiOperation(value="临床路径范围组-添加", notes="临床路径范围组-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalClinicalRangeGroup medicalClinicalRangeGroup) {
		medicalClinicalRangeGroupService.save(medicalClinicalRangeGroup);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalClinicalRangeGroup
	 * @return
	 */
	@AutoLog(value = "临床路径范围组-编辑")
	@ApiOperation(value="临床路径范围组-编辑", notes="临床路径范围组-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalClinicalRangeGroup medicalClinicalRangeGroup) {
		medicalClinicalRangeGroupService.updateById(medicalClinicalRangeGroup);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "临床路径范围组-通过id删除")
	@ApiOperation(value="临床路径范围组-通过id删除", notes="临床路径范围组-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalClinicalRangeGroupService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "临床路径范围组-批量删除")
	@ApiOperation(value="临床路径范围组-批量删除", notes="临床路径范围组-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalClinicalRangeGroupService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "临床路径范围组-通过id查询")
	@ApiOperation(value="临床路径范围组-通过id查询", notes="临床路径范围组-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalClinicalRangeGroup medicalClinicalRangeGroup = medicalClinicalRangeGroupService.getById(id);
		return Result.ok(medicalClinicalRangeGroup);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalClinicalRangeGroup
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalClinicalRangeGroup medicalClinicalRangeGroup) {
      return super.exportXls(request, medicalClinicalRangeGroup, MedicalClinicalRangeGroup.class, "临床路径范围组");
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
      return super.importExcel(request, response, MedicalClinicalRangeGroup.class);
  }

}
