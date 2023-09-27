package org.jeecg.modules.quartz.job;

import cn.hutool.core.util.StrUtil;
import com.ai.modules.mail.service.IMailService;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTaskDownload;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskDownloadService;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 示例不带参定时任务
 *
 * @Author Scott
 */
@Slf4j
@Component
public class SendEmailJob  {

	@Autowired
	private IMailService mailService;

	@Autowired
	private ISysRoleService sysRoleService;

	@Autowired
	private ISysUserRoleService sysUserRoleService;

	@Autowired
	private ISysUserService sysUserService;


	@Autowired
	private IYbChargeSearchTaskDownloadService ybChargeSearchTaskDownloadService;

	@Autowired
	private IYbChargeSearchTaskService ybChargeSearchTaskService;


	@Scheduled(cron = "0 30 0 * * ?")//凌晨0点30分执行
	protected void run() {
		boolean isSend=false;
		LocalDate localDate = LocalDate.now().minusDays(1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//昨日下载任务列表
		QueryWrapper<YbChargeSearchTaskDownload> downloadQueryWrapper = new QueryWrapper<>();
		downloadQueryWrapper.apply(true, "TO_DAYS(NOW())-TO_DAYS(create_time) = 1");
		List<YbChargeSearchTaskDownload> downloadList = ybChargeSearchTaskDownloadService.list(downloadQueryWrapper);
		StringBuffer content = new StringBuffer();
		if(downloadList.size()>0){
			isSend=true;
			content.append("<p>"+localDate+"医保数据洞察分析平台使用情况汇总:"+"</p>");

			//表格
			content.append("<table cellspacing=\"0\" border=\"1px\" style=\"width: 700px;background-color: #bbbbbb\">");


			//表头
			content.append("<tr>");

			//下载人
			content.append("<th style=\"width: 33%;background-color: #48c6ef\">");
			content.append("下载人");
			content.append("</th>");

			//下载时间
			content.append("<th style=\"width: 33%;background-color: #48c6ef\">");
			content.append("下载时间");
			content.append("</th>");

			//下载内容
			content.append("<th style=\"width: 33%;background-color: #48c6ef\">");
			content.append("下载文件名");
			content.append("</th>");

			content.append("</tr>");


			for(YbChargeSearchTaskDownload down:downloadList){
				content.append("<tr>");

				//下载人
				content.append("<td>");
				content.append(down.getCreateUser());
				content.append("</td>");

				//下载时间
				content.append("<td>");
				content.append(dateFormat.format(down.getCreateTime()));
				content.append("</td>");

				//下载文件名
				content.append("<td>");
				content.append(down.getFileName());
				content.append("</td>");

				content.append("</tr>");

			}
			content.append("</table>");
		}

		//昨日关键字查询记录列表
		QueryWrapper<YbChargeSearchTask> taskQueryWrapper = new QueryWrapper<>();
		taskQueryWrapper.apply(true, "TO_DAYS(NOW())-TO_DAYS(create_time) = 1");
		List<YbChargeSearchTask> taskList = ybChargeSearchTaskService.list(taskQueryWrapper);
		if(taskList.size()>0){
			isSend=true;
			content.append("</br>");
			//表格
			content.append("<table cellspacing=\"0\" border=\"1px\" style=\"width: 700px;background-color: #bbbbbb\">");

			//表头
			content.append("<tr>");

			//检索人
			content.append("<th style=\"width: 25%;background-color: #48c6ef\">");
			content.append("检索人");
			content.append("</th>");

			//检索时间
			content.append("<th style=\"width: 25%;background-color: #48c6ef\">");
			content.append("检索时间");
			content.append("</th>");

			//检索关键字
			content.append("<th style=\"width: 25%;background-color: #48c6ef\">");
			content.append("检索关键字");
			content.append("</th>");

			//机构
			content.append("<th style=\"width: 25%;background-color: #48c6ef\">");
			content.append("机构");
			content.append("</th>");

			content.append("</tr>");
			for(YbChargeSearchTask task:taskList){
				content.append("<tr>");
				//检索人
				content.append("<td>");
				content.append(task.getCreateUser());
				content.append("</td>");

				//检索时间
				content.append("<td>");
				content.append(dateFormat.format(task.getCreateTime()));
				content.append("</td>");

				//检索关键字
				content.append("<td>");
				content.append(task.getItemname());
				content.append("</td>");

				//机构
				content.append("<td>");
				content.append(task.getOrgs());
				content.append("</td>");

				content.append("</tr>");
			}
			content.append("</table>");
		}




		//接收邮件的人
		ArrayList<String> userIds = new ArrayList<>();
		ArrayList<String> emails = new ArrayList<>();
		List<SysRole> roleList = sysRoleService.list((QueryWrapper) Wrappers.query().eq("ROLE_CODE", "email_role"));
		if(roleList.size()>0){
			SysRole sysRole = roleList.get(0);
			String roleId = sysRole.getId();
			List<SysUserRole> roleUserList = sysUserRoleService.list((QueryWrapper) Wrappers.query().eq("ROLE_ID", roleId));
			roleUserList.stream().forEach(s ->{
				userIds.add(s.getUserId());
			});
		}
		if(userIds.size()>0){
			LambdaQueryWrapper<SysUser> sysUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
			sysUserLambdaQueryWrapper.in(SysUser::getId,userIds);
			List<SysUser> list = sysUserService.list(sysUserLambdaQueryWrapper);
			list.stream().forEach(s ->{
				String email = s.getEmail();
				if(StrUtil.isNotEmpty(email)){
					emails.add(email);
				}

			});

		}

		String tos="";
		if(emails.size()>0){
			tos = emails.stream().map(String::valueOf).collect(Collectors.joining(","));

		}

		log.info("邮件接收:{}",tos);

		try {
			if(isSend && StrUtil.isNotEmpty(tos)){
				mailService.sendMail(tos,"","医保数据洞察分析平台使用情况",content.toString(),null,null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info(String.format(" 发送邮件 !  时间:" + DateUtils.getTimestamp()));
	}




}
