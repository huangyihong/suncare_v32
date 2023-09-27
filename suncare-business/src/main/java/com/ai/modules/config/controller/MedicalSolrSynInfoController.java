package com.ai.modules.config.controller;

import com.ai.modules.config.entity.MedicalSolrSynInfo;
import com.ai.modules.config.service.IMedicalSolrSynInfoService;
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
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: SOLR数据同步情况表
 * @Author: jeecg-boot
 * @Date:   2020-11-20
 * @Version: V1.0
 */
@Slf4j
@Api(tags="SOLR数据同步情况表")
@RestController
@RequestMapping("/config/medicalSolrSynInfo")
public class MedicalSolrSynInfoController extends JeecgController<MedicalSolrSynInfo, IMedicalSolrSynInfoService> {
	@Autowired
	private IMedicalSolrSynInfoService medicalSolrSynInfoService;

	/**
	 * 分页列表查询
	 *
	 * @param medicalSolrSynInfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "SOLR数据同步情况表-分页列表查询")
	@ApiOperation(value="SOLR数据同步情况表-分页列表查询", notes="SOLR数据同步情况表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(MedicalSolrSynInfo medicalSolrSynInfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<MedicalSolrSynInfo> queryWrapper = QueryGenerator.initQueryWrapper(medicalSolrSynInfo, req.getParameterMap());
		Page<MedicalSolrSynInfo> page = new Page<MedicalSolrSynInfo>(pageNo, pageSize);
		IPage<MedicalSolrSynInfo> pageList = medicalSolrSynInfoService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param medicalSolrSynInfo
	 * @return
	 */
	@AutoLog(value = "SOLR数据同步情况表-添加")
	@ApiOperation(value="SOLR数据同步情况表-添加", notes="SOLR数据同步情况表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody MedicalSolrSynInfo medicalSolrSynInfo) {
		medicalSolrSynInfoService.save(medicalSolrSynInfo);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param medicalSolrSynInfo
	 * @return
	 */
	@AutoLog(value = "SOLR数据同步情况表-编辑")
	@ApiOperation(value="SOLR数据同步情况表-编辑", notes="SOLR数据同步情况表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody MedicalSolrSynInfo medicalSolrSynInfo) {
		medicalSolrSynInfoService.updateById(medicalSolrSynInfo);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "SOLR数据同步情况表-通过id删除")
	@ApiOperation(value="SOLR数据同步情况表-通过id删除", notes="SOLR数据同步情况表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		medicalSolrSynInfoService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "SOLR数据同步情况表-批量删除")
	@ApiOperation(value="SOLR数据同步情况表-批量删除", notes="SOLR数据同步情况表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.medicalSolrSynInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "SOLR数据同步情况表-通过id查询")
	@ApiOperation(value="SOLR数据同步情况表-通过id查询", notes="SOLR数据同步情况表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		MedicalSolrSynInfo medicalSolrSynInfo = medicalSolrSynInfoService.getById(id);
		return Result.ok(medicalSolrSynInfo);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param medicalSolrSynInfo
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, MedicalSolrSynInfo medicalSolrSynInfo) {
      return super.exportXls(request, medicalSolrSynInfo, MedicalSolrSynInfo.class, "SOLR数据同步情况表");
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
      return super.importExcel(request, response, MedicalSolrSynInfo.class);
  }

 /**
  * 通过tableName更新生效时间
  *
  * @param tableName
  * @param tableName
  * @return
  */
 @AutoLog(value = "SOLR数据同步情况表-通过tableName更新生效时间")
 @ApiOperation(value="SOLR数据同步情况表-通过tableName更新生效时间", notes="SOLR数据同步情况表-通过tableName更新生效时间")
 @GetMapping(value = "/solrUpdateTime")
 public Result<?> solrUpdateTime(@RequestParam(name="tableName",required=true) String tableName,@RequestParam(name="project",required=true) String project,@RequestParam(name="fieldname",required=true) String fieldname,String date) throws Exception {
 	QueryWrapper<MedicalSolrSynInfo> queryWrapper = new QueryWrapper<MedicalSolrSynInfo>();
	 queryWrapper.eq("TABLE_NAME",tableName.toUpperCase());
	 queryWrapper.eq("PROJECT",project);
	 List<MedicalSolrSynInfo> list = this.medicalSolrSynInfoService.list(queryWrapper);
	 if(list.size()==0){
	 	return Result.error("该表数据不存在");
	 }
	 for(MedicalSolrSynInfo medicalSolrSynInfo:list){
	 	Date updateTime = new Date();
	 	if(StringUtils.isNotBlank(date)){
			updateTime = DateUtils.parseDate(date,"yyyy-MM-dd HH:mm:ss");
		}
		if("HIVE_UPDATE_TIME".equals(fieldname)){
			medicalSolrSynInfo.setHiveUpdateTime(updateTime);
		}else if("INDEX_CREATE_TIME".equals(fieldname)){
			medicalSolrSynInfo.setIndexCreateTime(updateTime);
		}else if("SOLR_UPDATE_TIME".equals(fieldname)){
			medicalSolrSynInfo.setSolrUpdateTime(updateTime);
		}else{
			return Result.error("参数fieldname异常");
		}
		 medicalSolrSynInfoService.updateById(medicalSolrSynInfo);
	 }
	 return Result.ok();
 }

}
