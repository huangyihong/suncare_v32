/**
 * AbsDictMergeHandleFactory.java	  V1.0   2021年7月12日 下午4:58:35
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.handle;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.context.ApplicationContext;

import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDictMergeConfirm;
import com.ai.modules.medical.entity.MedicalDictMergeLog;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.medical.service.IMedicalDictMergeConfirmService;
import com.ai.modules.medical.service.IMedicalDictMergeLogService;

public abstract class AbsDictMergeHandleFactory {
	//主项
	protected String main;
	//被替换项
	protected String repeat;
	
	/**
	 * 
	 * 构造函数：
	 *
	 * @param main 主项，保留项
	 * @param repeat 被替换项，逻辑删除项
	 */
	public AbsDictMergeHandleFactory(String main, String repeat) {
		this.main = main;
		this.repeat = repeat;
	}
	
	public abstract List<DictMergeVO> merge() throws Exception;
	
	/**
	 * 
	 * 功能描述：基础字典合并日志
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年7月20日 下午5:36:16</p>
	 *
	 * @param mainname
	 * @param repeatname
	 * @param dictType
	 * @param itemList
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void addMedicalDictMergeLog(String mainname, String repeatname, 
			String dictType, List<DictMergeVO> itemList) {
		ApplicationContext context = SpringContextUtils.getApplicationContext();		
		LoginUser user = null;
		if(SolrUtil.isWeb()) {
			Subject subject = SecurityUtils.getSubject();
			user = (LoginUser) subject.getPrincipal();
		}
		String logId = UUIDGenerator.generate();
		if(itemList!=null && itemList.size()>0) {
			IMedicalDictMergeConfirmService confirmSV = context.getBean(IMedicalDictMergeConfirmService.class);
			List<MedicalDictMergeConfirm> confirmList = new ArrayList<MedicalDictMergeConfirm>();
			for(DictMergeVO vo : itemList) {				
				MedicalDictMergeConfirm confirm = new MedicalDictMergeConfirm();
				confirm.setConfirmId(UUIDGenerator.generate());
				confirm.setItemId(vo.getItemid());
				confirm.setItemName(vo.getItemname());
				confirm.setItemType(vo.getItemtype());
				confirm.setLogId(logId);
				confirm.setConfirmStatus("hand");
				confirm.setCreateTime(DateUtils.getDate());
				if(user!=null) {
					confirm.setCreateUser(user.getUsername());
				}
				confirmList.add(confirm);
			}
			confirmSV.saveBatch(confirmList);
		}
		
		IMedicalDictMergeLogService service = context.getBean(IMedicalDictMergeLogService.class);
		MedicalDictMergeLog log = new MedicalDictMergeLog();
		log.setLogId(logId);
		log.setDictMain(main);
		log.setDictRepeat(repeat);
		log.setDictMainname(mainname);
		log.setDictRepeatname(repeatname);
		log.setDictType(dictType);
		if(itemList!=null && itemList.size()>0) {
			log.setDictStatus("hand");
		} else {
			log.setDictStatus("finish");
		}
		log.setCreateTime(DateUtils.getDate());		
		if(user!=null) {
			log.setCreateUser(user.getUsername());
		}
		service.save(log);
	}	

	public String getMain() {
		return main;
	}

	public String getRepeat() {
		return repeat;
	}
}
