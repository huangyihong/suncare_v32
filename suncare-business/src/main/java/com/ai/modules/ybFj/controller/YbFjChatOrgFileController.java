package com.ai.modules.ybFj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ai.modules.ybFj.dto.ChatOrgFileDto;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import com.ai.modules.ybFj.entity.YbFjChatOrgFile;
import com.ai.modules.ybFj.service.IYbFjChatOrgFileService;
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
 * @Description: 飞检项目聊天附件
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目聊天附件")
@RestController
@RequestMapping("/fj/chat/file")
public class YbFjChatOrgFileController extends JeecgController<YbFjChatOrgFile, IYbFjChatOrgFileService> {
	@Autowired
	private IYbFjChatOrgFileService ybFjChatOrgFileService;
	
	/**
	 * 分页列表查询
	 *
	 * @param dto
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目聊天附件-历史交流文档查询")
	@ApiOperation(value="飞检项目聊天附件-历史交流文档查询", notes="飞检项目聊天附件-历史交流文档查询")
	@GetMapping(value = "/list")
	public Result<IPage<YbFjChatOrgFile>> queryPageList(ChatOrgFileDto dto,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) throws Exception {
		Page<YbFjChatOrgFile> page = new Page<YbFjChatOrgFile>(pageNo, pageSize);
		IPage<YbFjChatOrgFile> pageList = ybFjChatOrgFileService.queryChatOrgFile(page, dto);
		Result<IPage<YbFjChatOrgFile>> result = new Result<>();
		result.setResult(pageList);
		return result;
	}

	 @AutoLog(value = "飞检项目聊天附件-下载附件")
	 @ApiOperation(value="飞检项目聊天附件-下载附件", notes="飞检项目聊天附件-下载附件")
	 @GetMapping(value = "/download")
	 public void download(@RequestParam(name="fileId",required=true) String fileId,
						  HttpServletRequest request, HttpServletResponse response) throws Exception {
		 ybFjChatOrgFileService.download(fileId, request, response);
	 }

	 @AutoLog(value = "飞检项目聊天附件-批量下载附件")
	 @ApiOperation(value="飞检项目聊天附件-批量下载附件", notes="飞检项目线索附件-批量下载附件")
	 @GetMapping(value = "/downloadZip")
	 public void downloadZip(@RequestParam(name="fileIds",required=true) String fileIds,
							 HttpServletRequest request, HttpServletResponse response) throws Exception {
		 ybFjChatOrgFileService.downloadZip(response, fileIds);
	 }
}
