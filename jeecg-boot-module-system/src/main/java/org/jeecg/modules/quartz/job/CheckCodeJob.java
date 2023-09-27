package org.jeecg.modules.quartz.job;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.IdUtils;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalDict;
import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.service.IMedicalDictClearService;
import com.ai.modules.config.service.IMedicalDictItemService;
import com.ai.modules.config.service.IMedicalDictService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 示例不带参定时任务
 *
 * @Author Scott
 */
@Slf4j
@Component
public class CheckCodeJob {

	@Autowired
	private IMedicalDictService medicalDictService;

	@Autowired
	IMedicalDictClearService medicalDictClearService;


	@Autowired
	IMedicalDictItemService medicalDictItemService;



	public String getCode(){
		String captcha =RandomUtil.randomStringUpper(4);
		if(captcha.contains("0") || captcha.contains("O") || captcha.contains("1") || captcha.contains("I") || captcha.contains("L")){
			getCode();
		}
		return captcha;
	}

	@Scheduled(cron = "0 5 0 * * ?")//凌晨0点5分执行
	protected void run() {
		//更新登录验证码
		LambdaQueryWrapper<MedicalDict> medicalDictLambdaQueryWrapper = new LambdaQueryWrapper<>();
		medicalDictLambdaQueryWrapper.eq(MedicalDict::getGroupCode,"K");
		List<MedicalDict> medicalDictList = medicalDictService.list(medicalDictLambdaQueryWrapper);
		if(medicalDictList.size()>0){
			MedicalDict medicalDict = medicalDictList.get(0);
			if(ObjectUtil.isNotEmpty(medicalDict)){
				String groupId = medicalDict.getGroupId();
				//随机生成4位验证码
				String loginCode = getCode();
				String downloadCode = getCode();
				// 获取旧的字典
				MedicalDict oldDict = medicalDictService.getById(groupId);
				QueryWrapper<MedicalDictItem> queryWrapper = new QueryWrapper<MedicalDictItem>()
						.eq("GROUP_ID", groupId);
				// 获取旧的字典子项
				List<MedicalDictItem> list = medicalDictItemService.list(queryWrapper);

				// 删除子项
				medicalDictItemService.remove(queryWrapper);
				// 插入子项
				MedicalDictItem dictItem = new MedicalDictItem();
				dictItem.setGroupId(groupId);
				dictItem.setItemId(IdUtils.uuid());
				dictItem.setIsOrder(1L);
				dictItem.setCode(loginCode);
				dictItem.setValue("K1");
				medicalDictItemService.save(dictItem);

				dictItem.setIsOrder(2L);
				dictItem.setCode(downloadCode);
				dictItem.setValue("K2");
				medicalDictItemService.save(dictItem);



				String groupCode = oldDict.getGroupCode();
				String groupKind = oldDict.getKind();
				// 清除旧的子项缓存
				for(MedicalDictItem bean :list){
					medicalDictClearService.clearCache(groupCode, bean.getCode(), groupKind);
					medicalDictClearService.clearTextCache(groupCode, bean.getValue(), groupKind);
				}

				medicalDictClearService.clearCache(groupCode, groupKind);
			}
		}

		log.info(String.format(" 登录验证码更新 定时任务 !  时间:" + DateUtils.getTimestamp()));
	}


}
