package com.ai.modules.api.controller;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.config.entity.StdToHiveConfig;
import com.ai.modules.config.service.IStdToHiveConfigService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 /**
 * @Description: 基础数据同步到HIVE的配置文件
 * @Author: jeecg-boot
 * @Date:   2021-01-05
 * @Version: V1.0
 */
@Slf4j
@Api(tags="基础数据同步到HIVE的配置文件")
@RestController
@RequestMapping("/oauth/api")
public class ApiStdDataToHiveController extends JeecgController<StdToHiveConfig, IStdToHiveConfigService> {
	@Autowired
	private IStdToHiveConfigService stdToHiveConfigService;

	 @Autowired
	 private JdbcTemplate jdbcTemplate;


	/**
	 * 将基础数据以JSON格式返回
	 *
	 * @param tableName
	 * @return
	 */
	@AutoLog(value = "(中心库服务)将基础数据以JSON格式返回")
	@ApiOperation(value="基础数据同步到HIVE的配置文件-通过id查询", notes="基础数据同步到HIVE的配置文件-通过id查询")
	@PostMapping(value = "/queryStdDataForJson")
	public void queryStdDataForJson(HttpServletResponse response,
			@RequestParam(name="tableName",required=true) String tableName)  {
		List<Map<String, Object>> stdDataList = stdToHiveConfigService.queryStdData(tableName);

		try {
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();

			//如果没有数据，则返回0
			if(stdDataList == null || stdDataList.size()==0) {
				log.error("根据表名" + tableName + "查询不到字典数据！ ");
				writer.write("0");
				writer.flush();
				return;
			}

			writer.write("[");

			for(Map<String, Object> mapObj :stdDataList ) {
				//如果有id字段，则将id设置成小写（SOLR使用小写id字段）
				if(mapObj.containsKey("ID")) {
					mapObj.put("id", mapObj.get("ID"));
					//mapObj.remove("ID");
				}

				String jsonStr = JSON.toJSONString(mapObj);
				writer.write(jsonStr);
				writer.write(",");
			}

			writer.write("]");

			writer.flush();
		} catch (Exception e) {
			log.error("",e);
		}
	}

	/**
	 * 将基础数据以cvs格式返回
	 *
	 * @param tableName
	 * @return
	 */
	@AutoLog(value = "(中心库服务)将基础数据以cvs格式返回")
	@ApiOperation(value="基础数据同步到HIVE的配置文件-通过id查询", notes="基础数据同步到HIVE的配置文件-通过id查询")
	@PostMapping(value = "/queryStdDataForCSV")
	public void queryStdDataForCSV(HttpServletResponse response,
			@RequestParam(name="tableName",required=true) String tableName,@RequestParam(name="fields",required=true) String fields,String isNeedHead)  {
		/*List<Map<String, Object>> stdDataList = stdToHiveConfigService.queryStdData(tableName);*/
		StdToHiveConfig bean = stdToHiveConfigService.getOne(new QueryWrapper<StdToHiveConfig>().eq("TTABLE_NAME", tableName.toUpperCase()));
		String sql = bean.getSqlStr();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		int recordCount = 0;
		try {
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();

			Pattern p = Pattern.compile("\\t|\r|\n");
			String[] field_arr = fields.split(",");

			conn = this.jdbcTemplate.getDataSource().getConnection();
			pstmt = conn.prepareStatement(sql);
			rSet = pstmt.executeQuery();
			int colNum = rSet.getMetaData().getColumnCount();
			while (rSet.next()) {
				recordCount++;
				//加表头
				if(recordCount==1&&"true".equals(isNeedHead)){
					StringBuilder writeTitleStr = new StringBuilder();
					for(String colum:field_arr ){
						colum = colum.toUpperCase();
						writeTitleStr.append(colum);
						writeTitleStr.append("\001");
					}
					writer.write(writeTitleStr.toString());
					writer.write("\r\n");
				}

				Map<String, Object> mapObj=new HashMap<String, Object>();
				for (int j = 1; j <= colNum; j++) {
					String colName=rSet.getMetaData().getColumnLabel(j);
					mapObj.put(colName.toUpperCase(), rSet.getObject(j));
				}

				StringBuilder writeTextStr = new StringBuilder();
				for(String colum:field_arr ){
					colum = colum.toUpperCase();

					if(mapObj.get(colum) == null) {
						writeTextStr.append("");
					}
					else if(mapObj.get(colum)!=null&&mapObj.get(colum).getClass().getName().equals("java.lang.String")){
						Matcher m = p.matcher(DbDataEncryptUtil.dbDataDecryptString((String)mapObj.get(colum)));
						writeTextStr.append(m.replaceAll(" "));
					}else{
						writeTextStr.append(mapObj.get(colum));
					}
					writeTextStr.append("\001");
				}
				writer.write(writeTextStr.toString());
				writer.write("\r\n");

			}

			//如果没有数据，则返回0
			if(recordCount==0) {
				log.error("根据表名" + tableName + "查询不到字典数据！ ");
				writer.write("0");
			}


			writer.flush();
			writer.close();
		} catch (Exception e) {
			log.error("",e);
		} finally {
			try {
				if (rSet != null) {
					rSet.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rSet = null;
				pstmt = null;
				conn = null;
			}
		}
	}



	@AutoLog(value = "(中心库服务)将基础数据以cvs格式返回")
	@ApiOperation(value="(中心库服务)将基础数据以cvs格式返回", notes="(中心库服务)将基础数据以cvs格式返回")
	@PostMapping(value = "/queryMedicalDictForCSV")
	public void syncStdDataForCSV(HttpServletResponse response, @RequestParam(name="tableName",required=true) String tableName,String isNeedHead)  {
		//List<Map<String, Object>> stdDataList = stdToHiveConfigService.queryMedicalDictForCSV(tableName);
		StdToHiveConfig bean = stdToHiveConfigService.getOne(
				new QueryWrapper<StdToHiveConfig>().eq("STABLE_NAME", tableName.toUpperCase()));

		if(bean == null){
			bean = stdToHiveConfigService.getOne(
					new QueryWrapper<StdToHiveConfig>().eq("TTABLE_NAME", tableName.toUpperCase()));

		}
		String sql = bean.getSqlStr();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		int recordCount = 0;
		try {
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();
			Pattern p = Pattern.compile("\\t|\r|\n");

			conn = this.jdbcTemplate.getDataSource().getConnection();
			pstmt = conn.prepareStatement(sql);
			rSet = pstmt.executeQuery();
			int colNum = rSet.getMetaData().getColumnCount();
			while (rSet.next()) {
				recordCount++;

				Map<String, Object> mapObj=new HashMap<String, Object>();
				for (int j = 1; j <= colNum; j++) {
					String colName=rSet.getMetaData().getColumnLabel(j);
					mapObj.put(colName.toUpperCase(), rSet.getObject(j));
				}

				//加表头
				if(recordCount==1&&"true".equals(isNeedHead)){
					StringBuilder writeTitleStr = new StringBuilder();
					for(Map.Entry<String, Object> entry : mapObj.entrySet()) {
						String colum = entry.getKey();
						writeTitleStr.append(colum);
						writeTitleStr.append("\001");
					}
					writeTitleStr.deleteCharAt(writeTitleStr.length()-1);
					writer.write(writeTitleStr.toString());
					writer.write("\r\n");
				}

				StringBuilder writeTextStr = new StringBuilder();
				for(Map.Entry<String, Object> entry : mapObj.entrySet()) {
					String colum = entry.getKey();
					if(mapObj.get(colum) == null) {
						writeTextStr.append("");
					} else if(mapObj.get(colum)!=null&&mapObj.get(colum).getClass().getName().equals("java.lang.String")){
						Matcher m = p.matcher(DbDataEncryptUtil.dbDataDecryptString((String)mapObj.get(colum)));
						writeTextStr.append(m.replaceAll(" "));
					} else{
						writeTextStr.append(mapObj.get(colum));
					}
					writeTextStr.append("\001");
				}
				writeTextStr.deleteCharAt(writeTextStr.length()-1);
				writer.write(writeTextStr.toString());
				writer.write("\r\n");
			}

			//如果没有数据，则返回0
			if(recordCount==0) {
				log.error("根据表名" + tableName + "查询不到字典数据！ ");
				writer.write("0");
			}

			writer.flush();
			writer.close();
		} catch (Exception e) {
			log.error("",e);
		} finally {
			try {
				if (rSet != null) {
					rSet.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rSet = null;
				pstmt = null;
				conn = null;
			}
		}
	}

	/**
	 * 将基础数据以JSON格式返回
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "(地方库服务)调用中心服务，获取将基础数据")
	@ApiOperation(value="基础数据同步到HIVE的配置文件-通过id查询", notes="基础数据同步到HIVE的配置文件-通过id查询")
	@GetMapping(value = "/queryStdData")
	public Result<?> queryStdData(@RequestParam(name="tableName",required=true) String tableName) throws Exception {

		Map<String,String> busiParams = new HashMap<String,String>();
		busiParams.put("tableName", tableName);

		String fileName = tableName + ".json";
		File file =new File(fileName);

		FileOutputStream outStream = new FileOutputStream(file);

		ApiOauthUtil.writeResultToStream("/config/stdToHiveConfig/queryStdDataForCenter", busiParams, outStream);

		outStream.close();

		//判断文件的大小
		if(file.length()<10) {
			return Result.ok("同步失败！");
		}


		return Result.ok("同步成功！");
	}



}
