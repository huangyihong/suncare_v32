/**
 * StaReportServiceImpl.java	  V1.0   2020年8月21日 上午11:15:23
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.report.impl;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.report.AbstractReportHandler;
import com.ai.modules.engine.handle.report.BaseReportHandler;
import com.ai.modules.engine.handle.report.JsExpressReportHandler;
import com.ai.modules.engine.handle.report.MultiReportHandler;
import com.ai.modules.engine.model.report.ReportFacetBucketField;
import com.ai.modules.engine.model.report.ReportFormField;
import com.ai.modules.engine.model.report.ReportParamModel;
import com.ai.modules.engine.model.report.StatisticsReportModel;
import com.ai.modules.engine.service.report.IStaReportService;
import com.ai.modules.statistics.entity.StaReportDefined;
import com.ai.modules.statistics.service.IStaReportDefinedService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class StaReportServiceImpl implements IStaReportService {

	@Autowired
	private IStaReportDefinedService reportDefinedService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public StatisticsReportModel gerenate(String reportId, List<ReportFormField> whereFields) throws Exception {
		StaReportDefined report = reportDefinedService.getOne(new QueryWrapper<StaReportDefined>().eq("report_id", reportId));
		if(report==null) {
			throw new EngineBizException("未找到报表！");
		}
		if(StringUtils.isBlank(report.getDim1())) {
			throw new EngineBizException("未配置报表维度！");
		}
		ReportParamModel paramModel = this.getReportParamModel(report, whereFields);		

		AbstractReportHandler handler = null;
		if(paramModel.getGroupBy().length==1) {
			// 一维度报表
			handler = new BaseReportHandler(paramModel);
		} else {
			// 二维度
			handler = mappingReportHandler(paramModel, report);
		}
		return handler.handle();
	}
	
	/**
	 *
	 * 功能描述：获取处理报表类
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月12日 下午5:30:07</p>
	 *
	 * @param paramModel
	 * @param report
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private AbstractReportHandler mappingReportHandler(ReportParamModel paramModel, StaReportDefined report) throws Exception {				
		if(paramModel.getxLimit()>0) {
			//限制x轴指标
			if(paramModel.isPeriodReport()) {
				//周期类报表
				settingPeriodXAxis(paramModel);
			} else {
				//非周期类报表
				settingNotPeriodXAxis(paramModel);			
			}
		}
		
		if(StringUtils.isNotBlank(report.getCustomClazz())) {
			// 特殊处理类
			String className = "com.ai.sunCare.statistics.service.handler.".concat(report.getCustomClazz());
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getConstructor(ReportParamModel.class);
			AbstractReportHandler handler = (AbstractReportHandler) constructor.newInstance(paramModel);
			return handler;
		}
		if(StringUtils.isNotBlank(paramModel.getJsCrithExpress())) {
			return new JsExpressReportHandler(paramModel);
		}
		return new MultiReportHandler(paramModel);
	}
	
	private ReportParamModel getReportParamModel(StaReportDefined report, List<ReportFormField> whereFields) {
		ReportParamModel paramModel = new ReportParamModel(whereFields);
		paramModel.setCurrentEchart(report.getDefChartType());
		// 设置分组
		if(StringUtils.isNotBlank(report.getDim2())) {
			paramModel.setGroupBy(new String[] {report.getDim1(), report.getDim2()});
		} else {
			paramModel.setGroupBy(new String[] {report.getDim1()});
		}
		// 设置维度条数限制
		paramModel.setxLimit(-1);
		paramModel.setyLimit(-1);
		if(report.getLimitCnt1()!=null) {
			paramModel.setxLimit(report.getLimitCnt1());
		}
		if(paramModel.getGroupBy().length==2 && report.getLimitCnt2()!=null) {
			paramModel.setyLimit(report.getLimitCnt2());
		}
		paramModel.setStaFunction(report.getStaFunction());
		paramModel.setJsCrithExpress(report.getJsCrithExpress());
		paramModel.setReportType(report.getReportType());
		paramModel.setReportSubtype(report.getReportSubtype());
		paramModel.setSolrCollection(report.getSolrCollection());		
		paramModel.setSort(report.getSort());
		paramModel.setDimDict(report.getDimDict());
		// 添加默认条件
		paramModel.settingDefaultWhere(report.getDefQueryParam());
		// 添加页面查询条件
		paramModel.settingFormWhere();
		return paramModel;
	}
	
	/**
	 * 
	 * 功能描述：周期类报表设置x轴指标
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年8月26日 上午10:15:56</p>
	 *
	 * @param paramModel
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void settingPeriodXAxis(ReportParamModel paramModel) throws Exception {
		int limit = paramModel.getxLimit();
		String reportType = paramModel.getReportType();
		// 起始时间
		String start = null;
		// 结束时间
		String end = null;
		String whereField = paramModel.getGroupBy()[0];
		if(paramModel.getWhereFields()!=null) {
			for(ReportFormField field : paramModel.getWhereFields()) {
				if(StringUtils.isBlank(field.getFieldValue())) {
					continue;
				}
				// 是否存在第一维度作为页面的查询条件
				if(field.getSolrFieldName().equals(whereField)) {
					if("GT".equalsIgnoreCase(field.getOpType())
							|| "GET".equalsIgnoreCase(field.getOpType())) {
						start = field.getFieldValue();
					}
					if("LT".equalsIgnoreCase(field.getOpType())
							|| "LET".equals(field.getOpType())) {
						end = field.getFieldValue();
					}
				}
			}
		}
		if(start!=null && end!=null) {
			if(start.compareTo(end)>0) {
				throw new EngineBizException("起始时间不能大于结束时间！");
			}
		}
		if("D".equalsIgnoreCase(reportType)) {
			// 日报表
			String format = "yyyyMMdd";
			for(int i=limit-1; i>=0; i--) {
				String day = DateUtils.formatDate(DateUtils.addDay(-i), format);
				paramModel.addAxis(day);
			}
			start = DateUtils.formatDate(DateUtils.addDay(-(limit-1)), format);
			end = DateUtils.formatDate(new Date(), format);
			paramModel.addWhere(whereField+":["+start+" TO "+end+"]");
		} else if("M".equalsIgnoreCase(reportType)) {
			// 月报表
			String format = "yyyyMM";
			// 存储结束时间
			Date date = new Date();
			if(start!=null && end!=null) {
				// 重新计算第二维度限制条数
				limit = DateUtils.getMonthSpace(start, end, format) + 1;
			}
			if(end!=null) {
				date = DateUtils.parseDate(end, format);
			} else if(start!=null) {
				date = DateUtils.addMonth(DateUtils.parseDate(start, format), (limit-1));
			}
			for(int i=limit-1; i>=0; i--) {
				String day = DateUtils.formatDate(DateUtils.addMonth(date, -i), format);
				paramModel.addAxis(day);
			}
			start = DateUtils.formatDate(DateUtils.addMonth(date, -(limit-1)), format);
			end = DateUtils.formatDate(date, format);
			paramModel.addWhere(whereField+":["+start+" TO "+end+"]");
		} else if("Y".equalsIgnoreCase(reportType)) {
			// 年报表
			String format = "yyyy";
			// 存储结束时间
			Date date = new Date();
			if(start!=null && end!=null) {
				// 重新计算第二维度限制条数
				limit = DateUtils.getYearSpace(start, end, format) + 1;
			}
			if(end!=null) {
				date = DateUtils.parseDate(end, format);
			} else if(start!=null) {
				date = DateUtils.addMonth(DateUtils.parseDate(start, format), (limit-1));
			}
			for(int i=limit-1; i>=0; i--) {
				String day = DateUtils.formatDate(DateUtils.addYear(date, -i), format);
				paramModel.addAxis(day);
			}
			start = DateUtils.formatDate(DateUtils.addYear(date, -(limit-1)), format);
			end = DateUtils.formatDate(date, format);
			paramModel.addWhere(whereField+":["+start+" TO "+end+"]");
		} else if("Q".equalsIgnoreCase(reportType)) {
			// 季度
			// 查询条件使用20191格式传递
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			// 起始年份
			int year = calendar.get(Calendar.YEAR);
			// 起始季度
		    int month = calendar.get(Calendar.MONTH) + 1;
			int quarter = month/3;
			if(month%3!=0) {
				quarter = (month/3) + 1;
			}

			if(start!=null && end!=null) {
				// 计算时间间隔
				limit = getQuarterSpace(start, end) + 1;
			}
			// 计算出起始季度
			if(start!=null) {
				year = Integer.parseInt(start.substring(0, 4));
				quarter = Integer.parseInt(start.substring(4));
			} else {
				if(end!=null) {
					year = Integer.parseInt(end.substring(0, 4));
					quarter = Integer.parseInt(end.substring(4));
				}
				year = year - limit/4;
				if((limit-quarter)%4==0) {
					quarter = 1;
				} else {
					quarter = (4 - (limit-quarter)%4) + 1;
				}
			}
			quarter = quarter - 1;
			for(int i=0; i<limit; i++) {
				quarter = quarter + 1;
				if(quarter>4) {
					year = year + 1;
					quarter = 1;
				}
				String quarterStr = String.valueOf(year).concat(String.valueOf(quarter));
				paramModel.addAxis(quarterStr);
				if(i==0) {
					start = quarterStr;
				}
				if(i==limit-1) {
					end = quarterStr;
				}
			}
			// 添加季度查询条件
			paramModel.addWhere(whereField+":["+start+" TO "+end+"]");
		}
		if(paramModel.getxAxisSet() !=null && paramModel.getxAxisSet().size()>0) {
			//重新设置第一维度限制条数
			paramModel.setxLimit(paramModel.getxAxisSet().size());
		}
	}
	
	/**
	 * 
	 * 功能描述：非周期类报表设置x轴指标
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年8月26日 上午10:15:56</p>
	 *
	 * @param paramModel
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void settingNotPeriodXAxis(ReportParamModel paramModel) throws Exception {
		//第二维度限制查询条件
		ReportParamModel xParamModel = new ReportParamModel();
		xParamModel.setxLimit(paramModel.getxLimit());
		xParamModel.setGroupBy(new String[] {paramModel.getGroupBy()[0]});
		xParamModel.setStaFunction(paramModel.getStaFunction());
		xParamModel.setSolrCollection(paramModel.getSolrCollection());
		for(String condition : paramModel.getWheres()) {
			xParamModel.addWhere(condition);
		}
		AbstractReportHandler xHandler = new BaseReportHandler(xParamModel);
		List<ReportFacetBucketField> xList = xHandler.singleDimCallSolr();
		log.info("第二维度限制条件：{}", JSON.toJSONString(xList));		
		if(xList!=null && xList.size()>0) {
			StringBuilder sb = new StringBuilder();
			sb.append(paramModel.getGroupBy()[0]).append(":");
			sb.append("(");
			for(int i=0, len=xList.size(); i<len; i++) {
				ReportFacetBucketField bean = xList.get(i);
				if(i>0) {
					sb.append(" OR ");
				}
				sb.append(bean.getField());				
			}
			sb.append(")");
			paramModel.addWhere(sb.toString());
			
			//重新按field排序
			Collections.sort(xList, new Comparator<ReportFacetBucketField>() {
				@Override
				public int compare(ReportFacetBucketField o1, ReportFacetBucketField o2) {
					return o1.getField().compareTo(o2.getField());
				}
				
			});
			for(ReportFacetBucketField bean : xList) {
				paramModel.addAxis(bean.getField());
			}
		}
	}
	
	/**
	 *
	 * 功能描述：季度间隔
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月18日 上午10:30:56</p>
	 *
	 * @param start
	 * @param end
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private int getQuarterSpace(String start, String end) {
		 int year1 = Integer.parseInt(start.substring(0, 4));
		 int year2 = Integer.parseInt(end.substring(0, 4));

		 int q1 = Integer.parseInt(start.substring(4));
		 int q2 = Integer.parseInt(end.substring(4));
		 int y = year2 - year1;
		 if(y==0) {
			 return q2 - q1;
		 }
		 int result = 0;
		 if(q1>q2) {
			 y--;
	         result = y*4 + (q2+4) - q1;
		 } else {
			 result = y*4 + q2 - q1;
		 }
		 return result;
	}
}
