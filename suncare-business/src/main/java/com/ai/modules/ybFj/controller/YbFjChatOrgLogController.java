package com.ai.modules.ybFj.controller;

import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.entity.YbFjChatOrgLog;
import com.ai.modules.ybFj.service.IYbFjChatOrgLogService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

 /**
 * @Description: 飞检项目聊天记录
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
@Slf4j
@Api(tags="飞检项目聊天记录")
@RestController
@RequestMapping("/fj/chat/log")
public class YbFjChatOrgLogController extends JeecgController<YbFjChatOrgLog, IYbFjChatOrgLogService> {
	@Autowired
	private IYbFjChatOrgLogService ybFjChatOrgLogService;
	@Autowired
	private WebSocket webSocket;
	
	/**
	 * 分页列表查询
	 *
	 * @param orgId
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "飞检项目聊天记录-分页列表查询")
	@ApiOperation(value="飞检项目聊天记录-分页列表查询", notes="飞检项目聊天记录-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(String orgId,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Page<YbFjChatOrgLog> page = new Page<YbFjChatOrgLog>(pageNo, pageSize);
		IPage<YbFjChatOrgLog> pageList = ybFjChatOrgLogService.queryChatOrgLog(page, orgId);
		return Result.ok(pageList);
	}
	
	@AutoLog(value = "飞检项目聊天记录-向医院端发送消息")
	@ApiOperation(value="飞检项目聊天记录-向医院端发送消息", notes="飞检项目聊天记录-向医院端发送消息")
	@PostMapping(value = "/server/send")
	public Result<?> sendFromServer(@RequestParam(name="orgId",required=true) String orgId,
						  @RequestParam(name="chatMsg",required=true) String chatMsg) throws Exception {
		YbFjChatOrgLog chatLog = ybFjChatOrgLogService.sendFromServer(orgId, chatMsg);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("cmd", DcFjConstants.WS_CMD_FJCHAT_SYS);
		jsonObject.put("orgId", orgId);
		jsonObject.put("msgTxt", chatLog);
		webSocket.sendAllMessage(JSON.toJSONString(jsonObject));
		return Result.ok(chatLog);
	}

	 @AutoLog(value = "飞检项目聊天记录-向医院端发送文件")
	 @ApiOperation(value="飞检项目聊天记录-向医院端发送文件", notes="飞检项目聊天记录-向医院端发送文件")
	 @PostMapping(value = "/server/sendFile")
	 public Result<?> sendFromServer(@RequestParam(name="orgId",required=true) String orgId,
						   @RequestParam(name="file",required=true) MultipartFile file) throws Exception {
		 YbFjChatOrgLog chatLog = ybFjChatOrgLogService.sendFromServer(orgId, file);
		 JSONObject jsonObject = new JSONObject();
		 jsonObject.put("cmd", DcFjConstants.WS_CMD_FJCHAT_SYS);
		 jsonObject.put("orgId", orgId);
		 jsonObject.put("msgTxt", chatLog);
		 webSocket.sendAllMessage(JSON.toJSONString(jsonObject));
		 return Result.ok(chatLog);
	 }

	 @AutoLog(value = "飞检项目聊天记录-向服务端发送消息")
	 @ApiOperation(value="飞检项目聊天记录-向服务端发送消息", notes="飞检项目聊天记录-向服务端发送消息")
	 @PostMapping(value = "/client/send")
	 public Result<?> sendFromOrg(@RequestParam(name="orgId",required=true) String orgId,
									 @RequestParam(name="chatMsg",required=true) String chatMsg) throws Exception {
		 YbFjChatOrgLog chatLog = ybFjChatOrgLogService.sendFromOrg(orgId, chatMsg);
		 JSONObject jsonObject = new JSONObject();
		 jsonObject.put("cmd", DcFjConstants.WS_CMD_FJCHAT_ORG);
		 jsonObject.put("orgId", orgId);
		 jsonObject.put("msgTxt", chatLog);
		 webSocket.sendAllMessage(JSON.toJSONString(jsonObject));
		 return Result.ok(chatLog);
	 }

	 @AutoLog(value = "飞检项目聊天记录-向服务端发送文件")
	 @ApiOperation(value="飞检项目聊天记录-向服务端发送文件", notes="飞检项目聊天记录-向服务端发送文件")
	 @PostMapping(value = "/client/sendFile")
	 public Result<?> sendFromOrg(@RequestParam(name="orgId",required=true) String orgId,
									 @RequestParam(name="file",required=true) MultipartFile file) throws Exception {
		 YbFjChatOrgLog chatLog = ybFjChatOrgLogService.sendFromOrg(orgId, file);
		 JSONObject jsonObject = new JSONObject();
		 jsonObject.put("cmd", DcFjConstants.WS_CMD_FJCHAT_ORG);
		 jsonObject.put("orgId", orgId);
		 jsonObject.put("msgTxt", chatLog);
		 webSocket.sendAllMessage(JSON.toJSONString(jsonObject));
		 return Result.ok(chatLog);
	 }

	 @AutoLog(value = "飞检项目聊天记录-设置已读")
	 @ApiOperation(value="飞检项目聊天记录-设置已读", notes="飞检项目聊天记录-设置已读")
	 @PostMapping(value = "/read")
	 public Result<?> read(@RequestParam(name="logIds",required=true) String logIds) throws Exception {
		 ybFjChatOrgLogService.settingRead(logIds);
		 return Result.ok("设置成功");
	 }
}
