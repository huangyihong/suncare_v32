package com.ai.modules.task.service;

import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskActionFieldRela;
import com.ai.modules.task.entity.TaskActionFieldRelaSer;
import com.ai.modules.task.vo.TaskActionFieldColVO;
import com.ai.modules.task.vo.TaskActionFieldConfigVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 不同不合规行为显示字段配置
 * @Author: jeecg-boot
 * @Date:   2020-10-12
 * @Version: V1.0
 */
public interface ITaskActionFieldConfigService extends IService<TaskActionFieldConfig> {

	void addGroupByTask(String extFieldMapStr, String[] batchIds, String actionId, String[] fqs, boolean async) throws Exception;

	void addBreakStateTemplTask(MedicalActionDict medicalActionDict, String actionId, String[] batchIds, String[] fqs, boolean async) throws Exception;

	/*
	List<TaskActionFieldColVO> queryColByConfigIds(String[] ids, String platForm);

	List<TaskActionFieldColVO> querySerByConfigIds(String[] ids, String platForm);*/

	/**
	 *
	 * 功能描述：通过缓存查找不合理行为动态字段配置
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月16日 上午10:14:45</p>
	 *
	 * @param actionName
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	TaskActionFieldConfig queryTaskActionFieldConfigByCache(String actionName);

	/**
	 *
	 * 功能描述：清除缓存
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月16日 上午10:24:20</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
//	void clearCache();

	/**
	 *
	 * 功能描述：按不合理行为动态字段配置清除缓存
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月16日 上午10:24:38</p>
	 *
	 * @param actionName
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
//	void clearCache(String actionName);

	void saveConfig(TaskActionFieldConfigVO taskActionFieldConfig) throws Exception;
	void saveConfigs(List<TaskActionFieldConfig> taskActionFieldConfig
			, List<TaskActionFieldRela> relaCols, List<TaskActionFieldRelaSer> relaSers) throws Exception;

	void updateConfig(TaskActionFieldConfigVO taskActionFieldConfig) throws Exception;

	void removeConfigById(String id);

	void removeConfigByIds(List<String> asList);



//    void editSearch(String configId, List<TaskActionFieldRelaSer> cols);
}
