package com.ai.modules.engine.exception;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.jeecg.common.api.vo.Result;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * 功能描述：异常处理器
 *
 * @author  zhangly
 * Date: 2021年8月17日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class EngineExceptionHandler {

	/**
	 * 处理自定义异常
	 */
    @ExceptionHandler(EngineSolrException.class)
	public Result<?> handleEngineSolrException(EngineSolrException e) {
		log.error("EngineSolrException:" + e.getMessage());
		return Result.error("调用solr失败，请联系管理员");
//		return Result.error(e.getMessage());
	}

    @ExceptionHandler(SolrServerException.class)
    public Result<?> handleSolrServerException(SolrServerException e) {
		log.error("SolrServerException:" + e.getMessage());
		return Result.error("调用solr失败，请联系管理员");
	}

    @ExceptionHandler(SolrException.class)
    public Result<?> handleSolrServerException(SolrException e) {
		log.error("SolrException:" + e.getMessage());
		return Result.error("调用solr失败，请联系管理员");
	}

    @ExceptionHandler(SQLException.class)
    public Result<?> handleSQLException(SQLException e) {
		log.error("SQLException:" + e.getMessage());
		return Result.error("数据库操作失败，请联系管理员");
	}

    @ExceptionHandler(MyBatisSystemException.class)
    public Result<?> handleMyBatisSystemException(MyBatisSystemException e) {
		log.error("MyBatisSystemException:" + e.getMessage(), e);
		return Result.error("数据库操作失败，请联系管理员");
	}

    @ExceptionHandler(EngineBizException.class)
	public Result<?> handleEngineBizException(EngineBizException e){
		log.error(e.getMessage());
		String msg = e.getMessage();
		if(StringUtils.isBlank(msg)) {
			msg = "系统异常";
		}
		return Result.error("操作失败，" + (msg.length() > 1000 ? msg.substring(0, 1000) + "..." : msg));
	}
}
