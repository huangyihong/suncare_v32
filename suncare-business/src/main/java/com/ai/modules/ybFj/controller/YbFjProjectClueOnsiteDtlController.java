package com.ai.modules.ybFj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybFj.dto.QryProjectClueDtlDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueDtl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsiteDtl;
import com.ai.modules.ybFj.service.IYbFjProjectClueOnsiteDtlService;
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
 * @Description: 飞检项目现场检查线索明细
 * @Author: jeecg-boot
 * @Date:   2023-03-15
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目现场检查线索明细")
@RestController
@RequestMapping("/fj/clue/onsite/dtl")
public class YbFjProjectClueOnsiteDtlController extends JeecgController<YbFjProjectClueOnsiteDtl, IYbFjProjectClueOnsiteDtlService> {
	@Autowired
	private IYbFjProjectClueOnsiteDtlService ybFjProjectClueOnsiteDtlService;
	
	/**
	 * 分页列表查询
	 *
	 * @param dto
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目现场检查线索明细-分页列表查询")
	@ApiOperation(value="飞检项目现场检查线索明细-分页列表查询", notes="飞检项目现场检查线索明细-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YbFjProjectClueOnsiteDtl>> queryPageList(QryProjectClueDtlDto dto,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		//YbFjProjectClueDtl ybFjProjectClueDtl = BeanUtil.toBean(dto, YbFjProjectClueDtl.class);
		//QueryWrapper<YbFjProjectClueDtl> queryWrapper = QueryGenerator.initQueryWrapper(ybFjProjectClueDtl, req.getParameterMap());
		Page<YbFjProjectClueOnsiteDtl> page = new Page<YbFjProjectClueOnsiteDtl>(pageNo, pageSize);
		IPage<YbFjProjectClueOnsiteDtl> pageList = ybFjProjectClueOnsiteDtlService.queryProjectClueDtl(page, dto);
		Result<IPage<YbFjProjectClueOnsiteDtl>> result = new Result<>();
		result.setResult(pageList);
		return result;
	}
}
