package com.ai.modules.ybFj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.common.utils.BeanUtil;
import com.ai.modules.ybFj.dto.QryProjectClueDtlDto;
import com.ai.modules.ybFj.entity.YbFjProjectClue;
import com.ai.modules.ybFj.service.IYbFjDynamicConfigService;
import com.ai.modules.ybFj.service.IYbFjProjectClueService;
import com.ai.modules.ybFj.vo.FjDynamicConfigVo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjProjectClueDtl;
import com.ai.modules.ybFj.service.IYbFjProjectClueDtlService;
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
 * @Description: 飞检项目线索明细
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目线索明细")
@RestController
@RequestMapping("/fj/clue/dtl")
public class YbFjProjectClueDtlController extends JeecgController<YbFjProjectClueDtl, IYbFjProjectClueDtlService> {
	@Autowired
	private IYbFjProjectClueDtlService ybFjProjectClueDtlService;
	 @Autowired
	 private IYbFjProjectClueService ybFjProjectClueService;
	 @Autowired
	 private IYbFjDynamicConfigService ybFjDynamicConfigService;
	
	/**
	 * 分页列表查询
	 *
	 * @param dto
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequiresPermissions("fj:clue:dtl:list")
	@AutoLog(value = "飞检项目线索明细-分页列表查询")
	@ApiOperation(value="飞检项目线索明细-分页列表查询", notes="飞检项目线索明细-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YbFjProjectClueDtl>> queryPageList(QryProjectClueDtlDto dto,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		//YbFjProjectClueDtl ybFjProjectClueDtl = BeanUtil.toBean(dto, YbFjProjectClueDtl.class);
		//QueryWrapper<YbFjProjectClueDtl> queryWrapper = QueryGenerator.initQueryWrapper(ybFjProjectClueDtl, req.getParameterMap());
		Page<YbFjProjectClueDtl> page = new Page<YbFjProjectClueDtl>(pageNo, pageSize);
		IPage<YbFjProjectClueDtl> pageList = ybFjProjectClueDtlService.queryProjectClueDtl(page, dto);
		Result<IPage<YbFjProjectClueDtl>> result = new Result<>();
		result.setResult(pageList);
		return result;
	}

	 @RequiresPermissions("fj:clue:dtl:list")
	 @AutoLog(value = "飞检项目线索明细-获取明细动态表头")
	 @ApiOperation(value="飞检项目线索明细-获取明细动态表头", notes="飞检项目线索明细-获取明细动态表头")
	 @GetMapping(value = "/head")
	 public Result<List<FjDynamicConfigVo>> head(@RequestParam(name="issueSubtype",required=true) String issueSubtype,
												 HttpServletRequest req) throws Exception {
		 Result<List<FjDynamicConfigVo>> result = new Result<>();
		 String category = "fj";
		 List<FjDynamicConfigVo> dataList = ybFjDynamicConfigService.queryFjDynamicConfig(category, issueSubtype);
		 if(dataList==null) {
			 dataList = ybFjDynamicConfigService.queryFjDynamicConfigDefault(category);
		 }
		 result.setResult(dataList);
		 return result;
	 }
}
