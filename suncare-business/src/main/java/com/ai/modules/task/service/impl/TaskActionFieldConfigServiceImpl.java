package com.ai.modules.task.service.impl;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ReflectHelper;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.dto.DynamicFieldConfig;
import com.ai.modules.review.dto.DynamicLinkProp;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.task.entity.*;
import com.ai.modules.task.mapper.TaskActionFieldColMapper;
import com.ai.modules.task.mapper.TaskActionFieldConfigMapper;
import com.ai.modules.task.service.ITaskActionBatchExtmapService;
import com.ai.modules.task.service.ITaskActionFieldConfigService;

import com.ai.modules.task.service.ITaskActionFieldRelaSerService;
import com.ai.modules.task.service.ITaskActionFieldRelaService;
import com.ai.modules.task.vo.TaskActionFieldColVO;
import com.ai.modules.task.vo.TaskActionFieldConfigVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 不同不合规行为显示字段配置
 * @Author: jeecg-boot
 * @Date:   2020-10-12
 * @Version: V1.0
 */
@Service
public class TaskActionFieldConfigServiceImpl extends ServiceImpl<TaskActionFieldConfigMapper, TaskActionFieldConfig> implements ITaskActionFieldConfigService {

	@Autowired
	TaskActionFieldColMapper taskActionFieldColMapper;

	@Autowired
	ITaskActionFieldRelaService taskActionFieldRelaService;

	@Autowired
	ITaskActionFieldRelaSerService taskActionFieldRelaSerService;

	@Autowired
	ITaskActionBatchExtmapService taskActionBatchExtmapService;

	@Autowired
	private IDynamicFieldService dynamicFieldService;

	// 节点或中心项目运行
	@Override
	public void addGroupByTask(String groupFields, String[] batchIds, String actionId,String[] fqs, boolean async) throws Exception {
		if(StringUtils.isBlank(groupFields)){
			Map<String, String> map = new HashMap<>();
			map.put("actionId", actionId);
			TaskActionFieldConfig taskActionFieldConfig = ApiTokenUtil.getObj("/task/taskActionFieldConfig/queryByActionId", map, TaskActionFieldConfig.class);
			if(taskActionFieldConfig == null || StringUtils.isBlank(taskActionFieldConfig.getGroupFields())
				|| "[]".equals(taskActionFieldConfig.getGroupFields())){
				return;
			}
			groupFields = taskActionFieldConfig.getGroupFields();
		}
		JSONArray groupArray = JSONObject.parseArray(groupFields);

		Map<String, Set<String>> tabFieldMap = new HashMap<>();
		List<String[]> extFieldList = new ArrayList<>();

		for(int i = 0, len = groupArray.size(); i < len; i++){
			JSONArray dataArray = groupArray.getJSONObject(i).getJSONArray("data");
			for(int j = 0; j < dataArray.size(); j++){
				JSONObject itemJson = dataArray.getJSONObject(j);
				String oField = itemJson.getString("oCode");
				if(oField.startsWith("MEDICAL_UNREASONABLE_ACTION")){
					continue;
				}
				String[] tabFiledArray = oField.split("\\.");
				if ("action".equals(tabFiledArray[0])) {
					continue;
				}
				if (tabFiledArray[1].startsWith("ALIA")) {
					tabFiledArray[1] = tabFiledArray[1] + ":" + tabFiledArray[1].substring(tabFiledArray[1].indexOf("_") + 1);
				}
				Set<String> fieldList = tabFieldMap.computeIfAbsent(tabFiledArray[0], k -> new HashSet<>());
				fieldList.add(tabFiledArray[1]);
				extFieldList.add(new String[]{itemJson.getString("code"), oField});
			}
		}


		if(extFieldList.size() == 0) {
			throw new Exception("没有需要沉淀的字段");
		}

		SolrQuery solrQuery = new SolrQuery("*:*");

		solrQuery.addFilterQuery("BATCH_ID:(" + StringUtils.join(batchIds," , ") + ")");
		solrQuery.addFilterQuery("ACTION_ID:\"" + actionId + "\"");
		if(fqs.length > 0){
			solrQuery.addFilterQuery(fqs);
		}

		String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

		long recordCount = SolrQueryGenerator.count(collection, solrQuery);
		if(recordCount == 0){
			throw new Exception("没有需要沉淀分组统计的数据");
		}

		Function<Consumer<Integer>, Result> func = (processFunc) -> {
			try {
				dynamicFieldService.saveExtFieldValue(solrQuery, collection, tabFieldMap
						, extFieldList.toArray(new String[0][]), processFunc);
				return Result.ok("沉淀分组统计数据成功");
			} catch (Exception e) {
				e.printStackTrace();
				return Result.error(e.getMessage());
			}
		};

		String finalGroupFields = groupFields;
		List<TaskActionBatchExtmap> extmapList = Arrays.stream(batchIds).map(batchId -> {
			TaskActionBatchExtmap extmap = new TaskActionBatchExtmap();
			extmap.setBatchId(batchId);
			extmap.setActionId(actionId);
			extmap.setGroupFields(finalGroupFields);
			return extmap;
		}).collect(Collectors.toList());

		if(async){
			String logId = ThreadUtils.ASYNC_POOL.addActionGroupData(actionId, groupFields, solrQuery.getFilterQueries()
				, (int) recordCount, func);
			extmapList.forEach(r -> r.setLogId(logId));
		} else {
			func.apply(count -> {});
		}

		ApiTokenUtil.postBodyApi("/task/taskActionBatchExtmap/addBatch", extmapList);
	}

	@Override
	public void addBreakStateTemplTask(MedicalActionDict medicalActionDict, String actionId, String[] batchIds, String[] fqs, boolean async) throws Exception {
		// 没有传入值的时候去数据库查询
		if(medicalActionDict == null){
			Map<String, String> map = new HashMap<>();
			map.put("actionId", actionId);
			medicalActionDict = ApiTokenUtil.getObj("/config/medicalActionDict/queryByActionId", map, MedicalActionDict.class);
			if(medicalActionDict == null || StringUtils.isBlank(medicalActionDict.getBreakStateTempl())){
				return;
			}
		}
		String breakStateTempl = medicalActionDict.getBreakStateTempl();
		// 切分模板，查找需要查询的字段
		List<TaskActionFieldCol> colList = new ArrayList<>();
		String[] nodes = breakStateTempl.split("##");
		for(int i = 0, len = nodes.length; i < len; i++){
			String node = nodes[i];
			if(node.startsWith("{") && node.endsWith("}")){
				try {
					TaskActionFieldCol col = JSON.parseObject(node, TaskActionFieldCol.class);
					if("this".equals(col.getTableName())){
						nodes[i] = "【" + ReflectHelper.getValue(medicalActionDict, col.getColName()) + "】";
					} else if("ACTION_NAME".equals(col.getColName())){
						nodes[i] = "【" + medicalActionDict.getActionName() + "】";
					} else {
						colList.add(col);
						nodes[i] = null;
					}
				} catch (Exception ignored){ }
			}
		}
		// 初始化反查询表和字段Map
		DynamicFieldConfig fieldConfig = new DynamicFieldConfig(colList);
		Map<String, Set<String>> tabFieldMap = fieldConfig.getTabFieldMap();
		if(tabFieldMap.size() == 0) {
			throw new Exception("模板没有需要反查询的字段");
		}
		// 设置查询条件
		SolrQuery solrQuery = new SolrQuery("*:*");
		solrQuery.addFilterQuery("BATCH_ID:(" + StringUtils.join(batchIds," , ") + ")");
		solrQuery.addFilterQuery("ACTION_ID:\"" + actionId + "\"");
		if(fqs.length > 0){
			solrQuery.addFilterQuery(fqs);
		}
		String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
		// 查询数据量
		long recordCount = SolrQueryGenerator.count(collection, solrQuery);
		if(recordCount == 0){
			throw new Exception("没有需要修改违规说明的数据");
		}
		// 主要执行方法
		Function<Consumer<Integer>, Result> func = (processFunc) -> {
			try {
				dynamicFieldService.saveBreakStateValue(collection, solrQuery, tabFieldMap
						, colList, nodes, processFunc);
				return Result.ok("沉淀违规说明字段成功");
			} catch (Exception e) {
				e.printStackTrace();
				return Result.error(e.getMessage());
			}
		};
		// 同步或异步
		if(async){
			ThreadUtils.ASYNC_POOL.addActionBreakStateData(actionId, breakStateTempl, solrQuery.getFilterQueries()
					, (int) recordCount, func);
		} else {
			func.apply(count -> {});
		}
	}

	/*@Override
	public List<TaskActionFieldColVO> queryColByConfigIds(String[] ids, String platForm) {
		return this.taskActionFieldColMapper.queryColByConfigIds(ids, platForm);
	}*/

	/*@Override
	public List<TaskActionFieldColVO> querySerByConfigIds(String[] ids, String platForm) {
		return this.taskActionFieldColMapper.querySerByConfigIds(ids, platForm);
	}*/

	@Override
	@Cacheable(value = CacheConstant.DYNAMIC_ACTION_FIELD_CONFIG, key = "#actionName")
	public TaskActionFieldConfig queryTaskActionFieldConfigByCache(String actionName) {
		TaskActionFieldConfig config = this.getOne(new QueryWrapper<TaskActionFieldConfig>().eq("ACTION_NAME", actionName));
		return config;
	}

	/*@Override
	@CacheEvict(value=CacheConstant.DYNAMIC_ACTION_FIELD_CONFIG, allEntries=true)
	public void clearCache() {

	}

	@Override
	@Cacheable(value = CacheConstant.DYNAMIC_ACTION_FIELD_CONFIG, key = "#actionName")
	public void clearCache(String actionName) {

	}*/

	@Override
	@Transactional
	public void saveConfig(TaskActionFieldConfigVO taskActionFieldConfig) throws Exception {
		this.save(taskActionFieldConfig);
		this.saveColRelation(taskActionFieldConfig);
		this.saveSerRelation(taskActionFieldConfig);
	}

	@Override
	public void saveConfigs(List<TaskActionFieldConfig> taskActionFieldConfigs
			, List<TaskActionFieldRela> relaCols
			, List<TaskActionFieldRelaSer> relaSers) throws Exception {
		if(relaCols.size() == 0){
			throw new Exception("关联字段不能为空");
		}
		this.saveBatch(taskActionFieldConfigs);
		List<TaskActionFieldRela> colRelalist = taskActionFieldConfigs.stream().map(config -> {
			String configId = config.getId();
			AtomicInteger index = new AtomicInteger(1);
			return relaCols.stream().map(r -> {
				TaskActionFieldRela bean = new TaskActionFieldRela();
				bean.setConfigId(configId);
				bean.setColId(r.getColId());
				bean.setColCnname(r.getColCnname());
				bean.setOrderNo(index.getAndIncrement());
				return bean;
			}).toArray(TaskActionFieldRela[]::new);
		}).flatMap(Arrays::stream).collect(Collectors.toList());

		taskActionFieldRelaService.saveBatch(colRelalist);

		List<TaskActionFieldRelaSer> serRelalist = taskActionFieldConfigs.stream().map(config -> {
			String configId = config.getId();
			AtomicInteger index = new AtomicInteger(1);
			return relaSers.stream().map(r -> {
				TaskActionFieldRelaSer bean = new TaskActionFieldRelaSer();
				bean.setConfigId(configId);
				bean.setColId(r.getColId());
				bean.setColCnname(r.getColCnname());
				bean.setOrderNo(index.getAndIncrement());
				return bean;
			}).toArray(TaskActionFieldRelaSer[]::new);
		}).flatMap(Arrays::stream).collect(Collectors.toList());

		taskActionFieldRelaSerService.saveBatch(serRelalist);
	}

	@Override
	@Transactional
	public void updateConfig(TaskActionFieldConfigVO taskActionFieldConfig) throws Exception {
		this.updateById(taskActionFieldConfig);
		this.deleteColRelation(taskActionFieldConfig.getId());
		this.saveColRelation(taskActionFieldConfig);
		if(taskActionFieldConfig.getSearchs() != null){
			this.deleteSerRelation(taskActionFieldConfig.getId());
			this.saveSerRelation(taskActionFieldConfig);
		}

	}

	@Override
	@Transactional
	public void removeConfigById(String id) {
		this.removeById(id);
		this.deleteColRelation(id);
		this.deleteSerRelation(id);
	}

	@Override
	@Transactional
	public void removeConfigByIds(List<String> ids) {
		this.removeByIds(ids);
		this.deleteColRelation(ids);
		this.deleteSerRelation(ids);
	}



	/*@Override
	public void editSearch(String configId, List<TaskActionFieldRelaSer> cols) {
		this.deleteSerRelation(configId);
		AtomicInteger index = new AtomicInteger(1);
		cols.forEach(col -> {
			col.setConfigId(configId);
			col.setOrderNo(index.getAndIncrement());
		});
		taskActionFieldRelaSerService.saveBatch(cols);
	}*/


	private void deleteColRelation(String configId){
		taskActionFieldRelaService.remove(new QueryWrapper<TaskActionFieldRela>().eq("CONFIG_ID", configId));
	}
	private void deleteColRelation(List<String> configIds){
		taskActionFieldRelaService.remove(new QueryWrapper<TaskActionFieldRela>().in("CONFIG_ID", configIds));
	}
	private void deleteSerRelation(String configId){
		taskActionFieldRelaSerService.remove(new QueryWrapper<TaskActionFieldRelaSer>().eq("CONFIG_ID", configId));
	}
	private void deleteSerRelation(List<String> configIds){
		taskActionFieldRelaSerService.remove(new QueryWrapper<TaskActionFieldRelaSer>().in("CONFIG_ID", configIds));
	}
	private void saveColRelation(TaskActionFieldConfigVO taskActionFieldConfig) throws Exception {
		String configId = taskActionFieldConfig.getId();
		List<TaskActionFieldRela> cols = taskActionFieldConfig.getCols();
		if(StringUtils.isBlank(configId)){
			throw new Exception("配置项主键不能为空");
		}
		if(cols.size() == 0){
			throw new Exception("关联字段不能为空");
		}
		AtomicInteger index = new AtomicInteger(1);
		List<TaskActionFieldRela> list = cols.stream().map(r -> {
			TaskActionFieldRela bean = new TaskActionFieldRela();
			bean.setConfigId(configId);
			bean.setColId(r.getColId());
			bean.setColCnname(r.getColCnname());
			bean.setOrderNo(index.getAndIncrement());
			return bean;
		}).collect(Collectors.toList());
		taskActionFieldRelaService.saveBatch(list);
	}

	private void saveSerRelation(TaskActionFieldConfigVO taskActionFieldConfig) throws Exception {
		String configId = taskActionFieldConfig.getId();
		List<TaskActionFieldRelaSer> list = taskActionFieldConfig.getSearchs();
		if(StringUtils.isBlank(configId)){
			throw new Exception("配置项主键不能为空");
		}
		if(list == null || list.size() == 0){
			return;
		}
		AtomicInteger index = new AtomicInteger(1);
		List<TaskActionFieldRelaSer> serRelaList = list.stream().map(r -> {
			TaskActionFieldRelaSer bean = new TaskActionFieldRelaSer();
			bean.setConfigId(configId);
			bean.setColId(r.getColId());
			bean.setColCnname(r.getColCnname());
			bean.setOrderNo(index.getAndIncrement());
			return bean;
		}).collect(Collectors.toList());
		taskActionFieldRelaSerService.saveBatch(serRelaList);
	}
}
