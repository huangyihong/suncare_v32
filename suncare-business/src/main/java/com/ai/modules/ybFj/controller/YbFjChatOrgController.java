package com.ai.modules.ybFj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybFj.entity.YbFjOrg;
import com.ai.modules.ybFj.vo.YbFjChatOrgVo;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjChatOrg;
import com.ai.modules.ybFj.service.IYbFjChatOrgService;
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
 * @Description: 飞检项目聊天医疗机构
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目聊天医疗机构")
@RestController
@RequestMapping("/fj/chat/org")
public class YbFjChatOrgController extends JeecgController<YbFjChatOrg, IYbFjChatOrgService> {
	@Autowired
	private IYbFjChatOrgService ybFjChatOrgService;
	
	/**
	 * 分页列表查询
	 *
	 * @param org
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目聊天医疗机构-分页列表查询")
	@ApiOperation(value="飞检项目聊天医疗机构-分页列表查询", notes="飞检项目聊天医疗机构-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YbFjChatOrgVo>> queryPageList(YbFjOrg org,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Page<YbFjChatOrgVo> page = new Page<YbFjChatOrgVo>(pageNo, pageSize);
		IPage<YbFjChatOrgVo> pageList = ybFjChatOrgService.queryYbFjChatOrgVo(page, org);
		Result<IPage<YbFjChatOrgVo>> result = new Result<IPage<YbFjChatOrgVo>>();
		result.setResult(pageList);
		return result;
	}
	
	/**
	 * 添加
	 *
	 * @param orgId
	 * @return
	 */
	@AutoLog(value = "飞检项目聊天医疗机构-添加")
	@ApiOperation(value="飞检项目聊天医疗机构-添加", notes="飞检项目聊天医疗机构-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestParam(name="orgId",required=true) String orgId) {
		ybFjChatOrgService.saveChatOrg(orgId);
		return Result.ok("添加成功！");
	}
}
