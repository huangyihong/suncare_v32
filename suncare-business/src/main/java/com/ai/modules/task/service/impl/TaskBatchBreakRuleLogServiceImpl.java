package com.ai.modules.task.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.task.dto.TaskBatchBreakRuleLogDTO;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.mapper.TaskBatchBreakRuleLogMapper;
import com.ai.modules.task.service.ITaskBatchBreakRuleLogService;
import com.ai.modules.task.vo.TaskBatchBreakRuleLogVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;

/**
 * @Description: 批次任务运行日志
 * @Author: jeecg-boot
 * @Date:   2020-10-12
 * @Version: V1.0
 */
@Service
public class TaskBatchBreakRuleLogServiceImpl extends ServiceImpl<TaskBatchBreakRuleLogMapper, TaskBatchBreakRuleLog> implements ITaskBatchBreakRuleLogService {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private IMedicalDictService dictService;

	@Override
	public Map<String, Integer> groupByStatus(String batchId, String busiType) {
		StringBuilder sb = new StringBuilder();
    	sb.append("select STATUS, count(1) COUNT");
    	sb.append(" from task_batch_break_rule_log");
    	sb.append(" where item_type=? and batch_id=? group by status");
    	List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString(), busiType, batchId);
    	Map<String, Integer> statusMap = new HashMap<String, Integer>();
    	for(Map<String, Object> map : list) {
    		String status = map.get("STATUS").toString();
    		int count = Integer.parseInt(map.get("COUNT").toString());
    		statusMap.put(status, count);
    	}
    	return statusMap;
	}

	@Override
	public void waitTaskBatchBreakRuleLog(String batchId, String busiType, List<String> codes) {
		//设置状态
		TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
        log.setStatus(MedicalConstant.RUN_STATE_WAIT);
		int pageSize = 1000;
		if(codes.size()<pageSize) {
			QueryWrapper<TaskBatchBreakRuleLog> logWrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
		            .eq("BATCH_ID", batchId)
		            .eq("ITEM_TYPE", busiType)
		            .in("ITEM_ID", codes);
	        this.update(log, logWrapper);
		} else {
			int pageNum = (codes.size() + pageSize - 1) / pageSize;
			//数据分割
			List<List<String>> mglist = new ArrayList<>();
		    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
		    	mglist.add(codes.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
		    });
		    for(List<String> sublist : mglist) {
		    	QueryWrapper<TaskBatchBreakRuleLog> logWrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
			            .eq("BATCH_ID", batchId)
			            .eq("ITEM_TYPE", busiType)
			            .in("ITEM_ID", sublist);
		        this.update(log, logWrapper);
		    }
		}
	}

	@Override
	public IPage<TaskBatchBreakRuleLogVO> queryTaskBatchBreakRuleLog(IPage<TaskBatchBreakRuleLogVO> page, Wrapper<TaskBatchBreakRuleLogDTO> wrapper) {
		IPage<TaskBatchBreakRuleLogVO> result = baseMapper.queryTaskBatchBreakRuleLog(page, wrapper);
		String text = null;
		for(TaskBatchBreakRuleLogVO record : result.getRecords()) {
			if(MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE.equalsIgnoreCase(record.getItemType())) {
				//收费
				text = dictService.queryDictTextByKey("RULE_LIMIT_CHARGE", record.getRuleLimit());
			} else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG.equalsIgnoreCase(record.getItemType())) {
				//药品
				text = dictService.queryDictTextByKey("RULE_LIMIT_DRUG", record.getRuleLimit());
			} else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT.equalsIgnoreCase(record.getItemType())) {
				//诊疗
				text = dictService.queryDictTextByKey("RULE_LIMIT_TREAT", record.getRuleLimit());
			}
			if(StringUtils.isNotBlank(text)) {
				record.setRuleLimit(text);
			}
		}
		return result;
	}

	@Override
	public IPage<TaskBatchBreakRuleLogVO> queryTaskBatchBreakRuleLog(IPage<TaskBatchBreakRuleLogVO> page,
			TaskBatchBreakRuleLogDTO dto) {
		QueryWrapper<TaskBatchBreakRuleLogDTO> wrapper = new QueryWrapper<TaskBatchBreakRuleLogDTO>();
        wrapper.eq("batch_id", dto.getBatchId());
        if(StringUtils.isNotBlank(dto.getItemType())) {
        	wrapper.eq("a.item_type", dto.getItemType());
        }
        if(StringUtils.isNotBlank(dto.getStatus())) {
        	wrapper.eq("a.status", dto.getStatus());
        }
        if(StringUtils.isNotBlank(dto.getActionId())) {
        	wrapper.eq("b.action_id", dto.getActionId());
        }
        if(StringUtils.isNotBlank(dto.getRuleLimit())) {
        	wrapper.eq("b.rule_limit", dto.getRuleLimit());
        }
        if(StringUtils.isNotBlank(dto.getItemName())) {
        	String value = dto.getItemName();
        	value = StringUtils.replace(value, "*", "");
        	wrapper.like("a.item_name", value);
        }
		return queryTaskBatchBreakRuleLog(page, wrapper);
	}

	@Override
	public List<Map<String, Object>> queryTaskBatchBreakRuleLimit(TaskBatchBreakRuleLogDTO dto) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		QueryWrapper<TaskBatchBreakRuleLogDTO> wrapper = new QueryWrapper<TaskBatchBreakRuleLogDTO>();
        wrapper.eq("batch_id", dto.getBatchId());
        if(StringUtils.isNotBlank(dto.getItemType())) {
        	wrapper.eq("a.item_type", dto.getItemType());
        }
		List<Map<String, Object>> mapList = baseMapper.queryTaskBatchBreakRuleLimit(wrapper);
		String text = null;
		for(Map<String, Object> map : mapList) {
			String itemType = map.get("ITEM_TYPE").toString();
			String ruleLimit = map.get("RULE_LIMIT").toString();
			if(MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE.equalsIgnoreCase(itemType)) {
				//收费
				text = dictService.queryDictTextByKey("RULE_LIMIT_CHARGE", ruleLimit);
			} else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG.equalsIgnoreCase(itemType)) {
				//药品
				text = dictService.queryDictTextByKey("RULE_LIMIT_DRUG", ruleLimit);
			} else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT.equalsIgnoreCase(itemType)) {
				//诊疗
				text = dictService.queryDictTextByKey("RULE_LIMIT_TREAT", ruleLimit);
			}
			if(StringUtils.isNotBlank(ruleLimit)) {
				map.put("RULE_LIMIT_NAME", text);
			}
			result.add(map);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> queryTaskBatchBreakRuleAction(TaskBatchBreakRuleLogDTO dto) {
		QueryWrapper<TaskBatchBreakRuleLogDTO> wrapper = new QueryWrapper<TaskBatchBreakRuleLogDTO>();
        wrapper.eq("batch_id", dto.getBatchId());
        if(StringUtils.isNotBlank(dto.getItemType())) {
        	wrapper.eq("a.item_type", dto.getItemType());
        }
		return baseMapper.queryTaskBatchBreakRuleAction(wrapper);
	}

	@Override
	public IPage<TaskBatchBreakRuleLogVO> queryDruguseLog(IPage<TaskBatchBreakRuleLogVO> page,
			TaskBatchBreakRuleLogDTO dto) {
		QueryWrapper<TaskBatchBreakRuleLogDTO> wrapper = new QueryWrapper<TaskBatchBreakRuleLogDTO>();
        wrapper.eq("batch_id", dto.getBatchId());
        if(StringUtils.isNotBlank(dto.getItemType())) {
        	wrapper.eq("a.item_type", dto.getItemType());
        }
        if(StringUtils.isNotBlank(dto.getStatus())) {
        	wrapper.eq("a.status", dto.getStatus());
        }
		return baseMapper.queryDruguseRuleLog(page, wrapper);
	}

	@Override
	public IPage<TaskBatchBreakRuleLog> queryDrugLog(IPage<TaskBatchBreakRuleLog> page,
			TaskBatchBreakRuleLogDTO dto) {
		QueryWrapper<TaskBatchBreakRuleLog> wrapper = new QueryWrapper<TaskBatchBreakRuleLog>();
        wrapper.eq("batch_id", dto.getBatchId());
        if(StringUtils.isNotBlank(dto.getItemType())) {
        	if(MedicalConstant.ENGINE_BUSI_TYPE_DRUGREPEAT.equals(dto.getItemType())) {
        		//重复用药
        		wrapper.eq("item_type", MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
        		wrapper.eq("item_stype", dto.getItemType());
        	} else {
        		wrapper.eq("item_type", dto.getItemType());
        	}
        }
        if(StringUtils.isNotBlank(dto.getStatus())) {
        	wrapper.eq("status", dto.getStatus());
        }
        if(StringUtils.isNotBlank(dto.getItemName())) {
        	String value = dto.getItemName();
        	value = StringUtils.replace(value, "*", "");
        	wrapper.like("item_name", value);
        }
		return baseMapper.selectPage(page, wrapper);
	}

	@Override
	public List<Map<String, Object>> queryTaskBatchBreakRuleEngine(String batchId) {
		return baseMapper.queryTaskBatchBreakRuleEngine(batchId);
	}
}
