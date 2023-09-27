package com.ai.modules.config.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ai.modules.config.entity.StdToHiveConfig;
import com.ai.modules.config.mapper.StdToHiveConfigMapper;
import com.ai.modules.config.service.IStdToHiveConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 基础数据同步到HIVE的配置文件
 * @Author: jeecg-boot
 * @Date:   2021-01-05
 * @Version: V1.0
 */
@Service
public class StdToHiveConfigServiceImpl extends ServiceImpl<StdToHiveConfigMapper, StdToHiveConfig> implements IStdToHiveConfigService {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
    StdToHiveConfigMapper stdToHiveConfigMapper;
	
	/**
	 * 根据表名，获取需要同步的数据
	 * @param tableName
	 * @return
	 */
	public List<Map<String, Object>> queryStdData(String tableName){
		//根据表名去查询SQL配置
		QueryWrapper<StdToHiveConfig> config =new QueryWrapper<StdToHiveConfig>();
		config.eq("TTABLE_NAME", tableName.toUpperCase());
		
		StdToHiveConfig value = stdToHiveConfigMapper.selectOne(config);
		
		//如果没有SQL配置，则返回null
		if(value == null ) {
			return null;
		}
		
		//根据SQL配置，查询list对象
		List<Map<String, Object>> list = this.jdbcTemplate.queryForList(value.getSqlStr());
		
		return list;
	}
	
	public List<Map<String, Object>> queryMedicalDictForCSV(String tableName){
		//根据表名去查询SQL配置
		QueryWrapper<StdToHiveConfig> wrapper = new QueryWrapper<StdToHiveConfig>();
		wrapper.eq("STABLE_NAME", tableName.toUpperCase());
		
		StdToHiveConfig config = stdToHiveConfigMapper.selectOne(wrapper);		
		if(config == null ) {
			return null;
		}
		String sql = "select * from " + tableName;
		//根据SQL配置，查询list对象
		List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql);		
		return list;
	}
}
