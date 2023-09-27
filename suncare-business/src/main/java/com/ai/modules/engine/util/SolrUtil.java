/**
 * SolrUtil.java	  V1.0   2019年11月29日 下午4:55:15
 * <p>
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package com.ai.modules.engine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.impl.StreamingBinaryResponseParser;
import org.apache.solr.client.solrj.io.stream.JSONTupleStream;
import org.apache.solr.client.solrj.io.stream.TupleStreamParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.common.utils.MD5Util;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.exception.EngineSolrException;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.model.SolrQueryDTO;
import com.ai.solr.DynamicSolrDataSourceProperties;
import com.ai.solr.EngineCloudSolrClient;
import com.ai.solr.EngineHttpSolrClient;
import com.ai.solr.SimplePostTool;
import com.ai.solr.SolrAuth;
import com.ai.solr.SolrDataSourceProperty;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SolrUtil {

    /**
     * 动态solr数据源配置信息
     */
	public static DynamicSolrDataSourceProperties dynamicSolrProperties;
    public static String importFolder;


    @Autowired
    public SolrUtil(DynamicSolrDataSourceProperties dynamicSolrProperties) {
    	SolrUtil.dynamicSolrProperties = dynamicSolrProperties;
    }

    @Value("${solr.importFolder:/home/web/data}")
    public void importFolder(String importFolder) {
        SolrUtil.importFolder = importFolder;
    }

    public static boolean isWeb() {
    	return dynamicSolrProperties.isWeb();
    }

    /**
     *
     * 功能描述：获取solr数据源
     *
     * @author  zhangly
     * <p>创建日期 ：2020年8月20日 下午2:31:38</p>
     *
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static SolrDataSourceProperty getSolrDataSourceProperty() {
    	String ds = ThreadUtils.getDatasource(); //从线程副本获取数据源
    	if(ds==null) {
    		ds = dynamicSolrProperties.getPrimary();
            if(!dynamicSolrProperties.isSingleton() && dynamicSolrProperties.isWeb()) {
            	LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            	ds = user.getDataSource();
            }
    	}
        log.info("solr数据源:{}", ds);
        SolrDataSourceProperty solrDsProperty = dynamicSolrProperties.getSolrDataSourceProperty(ds);
        if(solrDsProperty==null) {
        	throw new RuntimeException("未找到solr数据源:"+ds);
        }
        return solrDsProperty;
    }

    /**
     *
     * 功能描述：获取当前操作员solr地址
     *
     * @author  zhangly
     * <p>创建日期 ：2021年11月4日 下午4:02:42</p>
     *
     * @param collection
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static String getSolrUrl(boolean slave) {
    	SolrDataSourceProperty solrDsProperty = SolrUtil.getSolrDataSourceProperty();
        String solrUrl = slave && StringUtils.isNotBlank(solrDsProperty.getSlaveUrl()) ? solrDsProperty.getSlaveUrl() : solrDsProperty.getUrl();
        if(StringUtils.isNotBlank(solrDsProperty.getUser())
        		&& StringUtils.isNotBlank(solrDsProperty.getPassword())
        		&& solrUrl.indexOf("@")==-1) {
        	//增加身份认证
        	String user = solrDsProperty.getUser();
        	String password = solrDsProperty.getPassword();
        	solrUrl = solrUrl.replace("http://", "http://".concat(user).concat(":").concat(password).concat("@"));
        	solrUrl = solrUrl.replace("https://", "https://".concat(user).concat(":").concat(password).concat("@"));
        }
        return solrUrl;
    }

    public static String getSolrUrl() {
    	return getSolrUrl(false);
    }

    /**
     *
     * 功能描述：根据数据源获取solr地址
     *
     * @author  zhangly
     * <p>创建日期 ：2021年11月5日 上午10:47:11</p>
     *
     * @param datasource
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static String getSolrUrl(String datasource) {
    	log.info("solr数据源:{}", datasource);
        SolrDataSourceProperty solrDsProperty = dynamicSolrProperties.getSolrDataSourceProperty(datasource);
        if(solrDsProperty==null) {
        	throw new RuntimeException("未找到solr数据源:"+datasource);
        }
        return getSolrUrl(solrDsProperty);
    }

    /**
     *
     * 功能描述：根据数据源获取solr地址
     *
     * @author  zhangly
     * <p>创建日期 ：2021年11月5日 上午10:47:11</p>
     *
     * @param solrDsProperty
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static String getSolrUrl(SolrDataSourceProperty solrDsProperty) {
        String solrUrl = solrDsProperty.getUrl();
        if(StringUtils.isNotBlank(solrDsProperty.getUser())
        		&& StringUtils.isNotBlank(solrDsProperty.getPassword())
        		&& solrUrl.indexOf("@")==-1) {
        	//增加身份认证
        	String user = solrDsProperty.getUser();
        	String password = solrDsProperty.getPassword();
        	solrUrl = solrUrl.replace("http://", "http://".concat(user).concat(":").concat(password).concat("@"));
        	solrUrl = solrUrl.replace("https://", "https://".concat(user).concat(":").concat(password).concat("@"));
        }
        return solrUrl;
    }

    /**
     *
     * 功能描述：获取solr身份信息
     *
     * @author  zhangly
     * <p>创建日期 ：2021年11月4日 下午3:31:02</p>
     *
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static SolrAuth getSolrAuth(SolrClient solrClient) {
    	if(solrClient instanceof EngineHttpSolrClient) {
    		EngineHttpSolrClient client = (EngineHttpSolrClient) solrClient;
    		SolrAuth auth = client.getAuth();
    		return auth;
    	}
    	if(solrClient instanceof EngineCloudSolrClient) {
    		EngineCloudSolrClient client = (EngineCloudSolrClient) solrClient;
    		SolrAuth auth = client.getAuth();
    		return auth;
    	}
    	return null;
    }

    public static SolrAuth getSolrAuth(SolrDataSourceProperty solrDsProperty) {
    	String user = solrDsProperty.getUser();
    	String password = solrDsProperty.getPassword();
    	if(StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
    		return null;
    	}
    	return new SolrAuth(user, password);
    }

    /**
     *
     * 功能描述：判断solr是否集群（多分片）模式
     *
     * @author  zhangly
     * <p>创建日期 ：2021年4月13日 上午9:39:20</p>
     *
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static boolean isCluster() {
    	SolrDataSourceProperty solrDsProperty = getSolrDataSourceProperty();
    	return solrDsProperty.isCluster();
    }

    public static SolrClient getClient() {
        return getClient(EngineUtil.DWB_MASTER_INFO);
    }

    /**
     * 获取solr连接
     *
     * @param collection
     * @return
     */
    public static SolrClient getClient(String collection) {
    	return getClient(collection, false);
    }

    /**
     *
     * 功能描述：获取solr连接
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月18日 上午10:02:55</p>
     *
     * @param collection
     * @param slave: 是否使用备用solr服务器
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static SolrClient getClient(String collection, boolean slave) {
    	String ds = ThreadUtils.getDatasource(); //从线程副本获取数据源
    	if(ds==null) {
    		ds = dynamicSolrProperties.getPrimary();
            if(!dynamicSolrProperties.isSingleton() && dynamicSolrProperties.isWeb()) {
            	LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            	ds = user.getDataSource();
            }
    	}
        log.info("solr数据源:{} {}", ds, collection);
        SolrClient solrClient = getSolrClient(collection, ds, slave);
        return solrClient;
    }

    /**
     * 功能描述：调用solr
     *
     * @author zhangly
     */
    public static QueryResponse call(SolrQuery query, String collection, boolean slave) throws Exception {
    	RTimer timer = new RTimer();
    	SolrClient solrClient = null;
        try {
        	solrClient = getClient(collection, slave);
        	String msg = "query:" + collection + writeSolrQuery(query.toQueryString(), "UTF-8");
            log.info(msg);
            QueryResponse response = solrClient.query(query, METHOD.POST);
            displayTiming(msg, timer);
            return response;
        } catch (Exception e) {
//            log.info(URLDecoder.decode(query.toQueryString(), "UTF-8"));
        	log.error("", e);
            throw new EngineSolrException("调用solr失败：" + e.getMessage());
        } finally {
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }
    }

    public static QueryResponse call(SolrQuery query, String collection) throws Exception {
        return call(query, collection, false);
    }

    /**
     * 获取当前用户所连接的数据源
     * @return
     */
    public static String getCurrentDsName(){
        //从线程副本获取数据源
        String ds = ThreadUtils.getDatasource();
        if(ds==null) {
            ds = dynamicSolrProperties.getPrimary();
            if(!dynamicSolrProperties.isSingleton() && dynamicSolrProperties.isWeb()) {
                LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                if(user != null) {
                    ds = user.getDataSource();
                }
            }
        }

        if(ds == null){
            ds = "default";
        }

        return ds;
    }

    /**
     * stream 表达式操作 返回(Map,index) => {}
     *
     * @param expr
     * @param function
     * @return
     * @throws IOException
     */
    public static int stream(String expr, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	String cacheType= "stream";
    	String cacheName = MD5Util.getMD5(getCurrentDsName() +expr );

    	int num = 0;
    	//尝试从缓存取数据
    	int expireSecond = getCacheExpireSeconds();
    	ArrayList<Map<String, Object>> cacheObjectList =(ArrayList<Map<String, Object>>) ObjectCacheWithFile.getObjectFromFile(cacheType, cacheName, expireSecond);

    	if(cacheObjectList != null) {
    		log.error("use file cache --stream2,cacheTime:" + expireSecond);

    		for(Map<String, Object> map :cacheObjectList) {
    			function.accept(map, num++);
    		}
    		return num;
    	}

    	cacheObjectList = new ArrayList<Map<String, Object>>();

    	RTimer timer = new RTimer();
        SolrQuery query = new SolrQuery("expr", expr);
        query.setRequestHandler("/stream");
        try (SolrClient solrClient = getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION)) {
        	String msg = "expr:" + writeSolrQuery(expr, "UTF-8");
            log.info(msg);

            TupleStreamParser streamParser = new JSONTupleStream(jsonReader(solrClient, query));
            Map<String, Object> map = streamParser.next();
            Boolean eof = (Boolean) map.get("EOF");
            while (!(eof != null && eof)) {
                function.accept(map, num++);

                cacheObjectList.add(map);
                map = streamParser.next();
                eof = (Boolean) map.get("EOF");
            }
            streamParser.close();
            if (num == 0 && map.get("EXCEPTION") != null) {
                log.info("stream.EXCEPTION:" + map.get("EXCEPTION").toString());
                throw new EngineSolrException(map.get("EXCEPTION").toString());
            }

            ObjectCacheWithFile.saveObjectToFile(cacheType, cacheName, cacheObjectList);

            displayTiming(msg, timer);
        } catch (SolrServerException | IOException e) {
        	log.error("", e);
            throw new EngineSolrException("调用solr失败：" + e.getMessage());
        }

        return num;
    }

    /**
     * stream 表达式操作 返回List
     *
     * @param expr
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> stream(String expr) throws Exception {

    	String cacheType= "stream";
    	String cacheName = MD5Util.getMD5(getCurrentDsName() + expr );

    	int expireSecond = getCacheExpireSeconds();
    	List<Map<String, Object>> cacheObject =(List<Map<String, Object>>) ObjectCacheWithFile.getObjectFromFile(cacheType, cacheName, expireSecond);

    	if(cacheObject != null) {
    		log.error("use file cache  --->stream1,expire Time=" + expireSecond);
    		return cacheObject;
    	}

    	RTimer timer = new RTimer();
    	List<Map<String, Object>> list;
        SolrQuery query = new SolrQuery("expr", expr);
        query.setRequestHandler("/stream");
        SolrClient solrClient = null;
        try {
        	solrClient = getClient();
        	String msg = "expr:" + writeSolrQuery(expr, "UTF-8");
            log.info(msg);

            QueryResponse response = solrClient.query(query, METHOD.POST);

            NamedList<Object> genericResponse = response.getResponse();
            LinkedHashMap hashMap = (LinkedHashMap) genericResponse.get("result-set");
            list = (List<Map<String, Object>>) hashMap.get("docs");
            // 移除终点项
            Map<String, Object> endMap = list.remove(list.size() - 1);
            Object errorMsg = endMap.get("EXCEPTION");
            if(errorMsg != null){
                throw new EngineSolrException("调用solr失败：" + errorMsg.toString());
            }

            if(list != null) {
            	ObjectCacheWithFile.saveObjectToFile(cacheType, cacheName, list);
            }

            displayTiming(msg, timer);
        } catch (SolrServerException | IOException e) {
        	log.error("", e);
            throw new EngineSolrException("调用solr失败：" + e.getMessage());
        } finally {
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }


        return list;
    }

    /**
     * 全量导出操作 返回(Map,index) -> {}
     *
     * @param solrQuery
     * @param collection
     * @param slave 是否使用备用服务器
     * @param function
     * @return
     * @throws IOException
     * @throws SolrServerException
     */
    public static int export(SolrQuery solrQuery, String collection, boolean slave, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	RTimer timer = new RTimer();
    	if(solrQuery.getRows() == null || solrQuery.getRows() == 0){
            solrQuery.setRows(dynamicSolrProperties.getDruid().getMaxRow());
//            solrQuery.setRows(Integer.MAX_VALUE);
        }
        final AtomicInteger count = new AtomicInteger(0);
        SolrClient solrClient = null;
        try {
        	solrClient = getClient(collection, slave);
        	String msg = "export:"  + collection + writeSolrQuery(solrQuery.toQueryString(), "UTF-8");
            log.info(msg);
            StreamingResponseCallback callback = new StreamingResponseCallback() {
                @Override
                public void streamSolrDocument(SolrDocument doc) {
                    function.accept(doc.getFieldValueMap(), count.getAndIncrement());
                }

                @Override
                public void streamDocListInfo(long numFound, long start, Float maxScore) {
                    log.info("numFound:" + numFound + ",start:" + start + ",maxScore:" + maxScore);
                }
            };
            QueryRequest req = new QueryRequest(solrQuery, METHOD.POST);
            SolrAuth auth = SolrUtil.getSolrAuth(solrClient);
    	    if(auth!=null) {
    	    	//增加身份认证
    	    	req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
    	    }
            ResponseParser parser = new StreamingBinaryResponseParser(callback);
            req.setStreamingResponseCallback(callback);
            req.setResponseParser(parser);
            req.process(solrClient);
            displayTiming(msg, timer);
        } catch (SolrServerException | IOException e) {
            log.error("", e);
            throw new EngineSolrException("调用solr失败：" + e.getMessage());
        } finally {
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }

        return count.get();
    }

    public static int exportDoc(SolrQuery solrQuery, String collection, boolean slave, BiConsumer<SolrDocument, Integer> function) throws Exception {
    	RTimer timer = new RTimer();
    	if(solrQuery.getRows() == null || solrQuery.getRows() == 0){
            solrQuery.setRows(dynamicSolrProperties.getDruid().getMaxRow());
        }
        final AtomicInteger count = new AtomicInteger(0);
        SolrClient solrClient = null;
        try {
        	solrClient = getClient(collection, slave);
        	String msg = "export:"  + collection + writeSolrQuery(solrQuery.toQueryString(), "UTF-8");
            log.info(msg);
            StreamingResponseCallback callback = new StreamingResponseCallback() {
                @Override
                public void streamSolrDocument(SolrDocument doc) {
                    function.accept(doc, count.getAndIncrement());
                }

                @Override
                public void streamDocListInfo(long numFound, long start, Float maxScore) {
                    log.info("numFound:" + numFound + ",start:" + start + ",maxScore:" + maxScore);
                }
            };
            QueryRequest req = new QueryRequest(solrQuery, METHOD.POST);
            SolrAuth auth = SolrUtil.getSolrAuth(solrClient);
    	    if(auth!=null) {
    	    	//增加身份认证
    	    	req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
    	    }
            ResponseParser parser = new StreamingBinaryResponseParser(callback);
            req.setStreamingResponseCallback(callback);
            req.setResponseParser(parser);
            req.process(solrClient);
            displayTiming(msg, timer);
        } catch (SolrServerException | IOException e) {
            log.error("", e);
            throw new EngineSolrException("调用solr失败：" + e.getMessage());
        } finally {
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }

        return count.get();
    }

    public static int exportDoc(List<String> conditionList, String collection, boolean slave, BiConsumer<SolrDocument, Integer> function) throws Exception {
    	SolrQuery solrQuery = new SolrQuery("*:*");
		// 设定查询字段
		solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
		solrQuery.setStart(0);
		solrQuery.setRows(dynamicSolrProperties.getDruid().getMaxRow());
		return exportDoc(solrQuery, collection, slave, function);
    }

    public static int export(SolrQuery solrQuery, String collection, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	return export(solrQuery, collection, false, function);
    }

    /**
     *
     * 功能描述：solr游标分页查询
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月29日 下午3:55:36</p>
     *
     * @param dto
     * @param collection
     * @param slave 是否使用备用服务器
     * @param function
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static int exportDocByPager(SolrQueryDTO dto, String collection, boolean slave, BiConsumer<SolrDocument, Integer> function) throws Exception {
    	RTimer timer = new RTimer();
    	SolrClient solrClient = null;
    	try {
    		solrClient = getClient(collection, slave);

	    	int pageSize = dto.getRows();
	    	String cursorMark = "*";
	    	SolrQuery solrQuery = new SolrQuery("*:*");
			// 设定查询字段
			if(dto.getFq()!=null && dto.getFq().length>0) {
				solrQuery.addFilterQuery(dto.getFq());
			}
			solrQuery.setStart(0);
			solrQuery.setRows(pageSize);
			solrQuery.set("cursorMark", cursorMark);
			if(dto.getSorts()!=null && dto.getSorts().size()>0) {
				for(SortClause sort : dto.getSorts()) {
					solrQuery.addSort(sort);
				}
			}
			if(dto.getFl()!=null) {
				//设置查询字段
				for(String field : dto.getFl()) {
					solrQuery.addField(field);
				}
			}
			String msg = "export:"  + collection + writeSolrQuery(solrQuery.toQueryString(), "UTF-8");

			int index = 1;
			final Long[] total = {0L};
			final AtomicInteger count = new AtomicInteger(0);

			StreamingResponseCallback callback = new StreamingResponseCallback() {
                @Override
                public void streamSolrDocument(SolrDocument doc) {
                    function.accept(doc, count.getAndIncrement());
                }
                @Override
                public void streamDocListInfo(long numFound, long start, Float maxScore) {
                	total[0] = numFound;
                    log.info("numFound:" + numFound + ",start:" + start + ",rows:" + pageSize + ",maxScore:" + maxScore);
                }
			};

			QueryResponse response = SolrUtil.process(solrClient, solrQuery, callback);
			if(total[0]<pageSize) {
				//不需要分页直接返回
				return count.get();
			}
			long pageTotal = (total[0] + pageSize - 1)/pageSize;
			String nextCursorMark = response.getNextCursorMark();
			// 使用游标方式进行分页查询
			while(!cursorMark.equals(nextCursorMark)
					&& count.get()<total[0]) {
				cursorMark = nextCursorMark;
				solrQuery.set("cursorMark", cursorMark);
				log.info("pageNo:{}/{},cursorMark:{}", ++index, pageTotal, cursorMark);
				response = SolrUtil.process(solrClient, solrQuery, callback);
				nextCursorMark = response.getNextCursorMark();
			}
			displayTiming(msg, timer);
    		return count.get();
        } catch (Exception e) {
        	log.error("", e);
            throw new EngineSolrException("调用solr失败：" + e.getMessage());
        } finally {
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }
    }

    /**
     *
     * 功能描述：solr游标分页查询
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月18日 上午10:08:42</p>
     *
     * @param fq
     * @param collection
     * @param fl 查询字段
     * @param slave 是否使用备用服务器
     * @param function
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static int exportDocByPager(String[] fq, String[] fl, String collection, boolean slave, BiConsumer<SolrDocument, Integer> function) throws Exception {
    	SolrQueryDTO dto = new SolrQueryDTO();
    	dto.setFq(fq);
    	dto.setFl(fl);
    	dto.setRows(EngineUtil.PAGE_SIZE);
    	dto.addSortClause(SolrQuery.SortClause.asc("id"));

    	return SolrUtil.exportDocByPager(dto, collection, slave, function);
    }

    public static int exportDocByPager(List<String> conditionList, String collection, boolean slave, BiConsumer<SolrDocument, Integer> function) throws Exception {
    	return SolrUtil.exportDocByPager(conditionList.toArray(new String[0]), null, collection, slave, function);
    }

    public static int exportDocByPager(List<String> conditionList, String collection, BiConsumer<SolrDocument, Integer> function) throws Exception {
    	return SolrUtil.exportDocByPager(conditionList.toArray(new String[0]), null,  collection, false, function);
    }

    public static int exportDocByPager(String[] fq, String[] fl, String collection, BiConsumer<SolrDocument, Integer> function) throws Exception {
        return SolrUtil.exportDocByPager(fq, fl,  collection, false, function);
    }


    /**
     *
     * 功能描述：solr游标分页查询，遇到字段是多值时此方法会丢失值，推荐使用exportDocByPager方法
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月29日 下午3:55:36</p>
     *
     * @param dto
     * @param collection
     * @param slave 是否使用备用服务器
     * @param function
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static int exportByPager(SolrQueryDTO dto, String collection, boolean slave, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	RTimer timer = new RTimer();
    	SolrClient solrClient = null;
    	try {
    		solrClient = getClient(collection, slave);

	    	int pageSize = dto.getRows();
	    	String cursorMark = "*";
	    	SolrQuery solrQuery = new SolrQuery("*:*");
			// 设定查询字段
			solrQuery.addFilterQuery(dto.getFq());
			solrQuery.setStart(0);
			solrQuery.setRows(pageSize);
			solrQuery.set("cursorMark", cursorMark);
			if(dto.getSorts()!=null && dto.getSorts().size()>0) {
				for(SortClause sort : dto.getSorts()) {
					solrQuery.addSort(sort);
				}
			}
			if(dto.getFl()!=null) {
				//设置查询字段
				for(String field : dto.getFl()) {
					solrQuery.addField(field);
				}
			}
			String msg = "export:"  + collection + writeSolrQuery(solrQuery.toQueryString(), "UTF-8");

			int index = 1;
			final Long[] total = {0L};
			final AtomicInteger count = new AtomicInteger(0);

			StreamingResponseCallback callback = new StreamingResponseCallback() {
                @Override
                public void streamSolrDocument(SolrDocument doc) {
                    function.accept(doc.getFieldValueMap(), count.getAndIncrement());
                }
                @Override
                public void streamDocListInfo(long numFound, long start, Float maxScore) {
                	total[0] = numFound;
                    log.info("numFound:" + numFound + ",start:" + start + ",rows:" + pageSize + ",maxScore:" + maxScore);
                }
			};

			QueryResponse response = SolrUtil.process(solrClient, solrQuery, callback);
			if(total[0]<pageSize) {
				//不需要分页直接返回
				return count.get();
			}
			long pageTotal = (total[0] + pageSize - 1)/pageSize;
			String nextCursorMark = response.getNextCursorMark();
			// 使用游标方式进行分页查询
			while(!cursorMark.equals(nextCursorMark)
					&& count.get()<total[0]) {
				cursorMark = nextCursorMark;
				solrQuery.set("cursorMark", cursorMark);
				log.info("pageNo:{}/{},cursorMark:{}", ++index, pageTotal, cursorMark);
				response = SolrUtil.process(solrClient, solrQuery, callback);
				nextCursorMark = response.getNextCursorMark();
			}
			displayTiming(msg, timer);
    		return count.get();
        } catch (Exception e) {
        	log.error("", e);
            throw new EngineSolrException("调用solr失败：" + e.getMessage());
        } finally {
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }
    }

    /**
     *
     * 功能描述：solr分页查询
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月18日 上午10:08:42</p>
     *
     * @param fq
     * @param collection
     * @param fl 查询字段
     * @param slave 是否使用备用服务器
     * @param function
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static int exportByPager(String[] fq, String collection, String[] fl, boolean slave, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	SolrQueryDTO dto = new SolrQueryDTO();
    	dto.setFq(fq);
    	dto.setFl(fl);
    	dto.setRows(EngineUtil.PAGE_SIZE);
    	dto.addSortClause(SolrQuery.SortClause.asc("id"));

    	return SolrUtil.exportByPager(dto, collection, slave, function);
    }

    public static int exportByPager(String[] fq, String collection, boolean slave, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	return SolrUtil.exportByPager(fq, collection, null, slave, function);
    }

    public static int exportByPager(String[] fq, String collection, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	return SolrUtil.exportByPager(fq, collection, false, function);
    }

    /**
     *
     * 功能描述：solr分页查询
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月18日 上午10:19:35</p>
     *
     * @param conditionList
     * @param collection
     * @param slave：是否使用备用服务器
     * @param function
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static int exportByPager(List<String> conditionList, String collection, boolean slave, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	return exportByPager(conditionList.toArray(new String[0]), collection, slave, function);
    }

    public static int exportByPager(List<String> conditionList, String collection, BiConsumer<Map<String, Object>, Integer> function) throws Exception {
    	return exportByPager(conditionList.toArray(new String[0]), collection, false, function);
    }

    public static QueryResponse process(SolrClient solrClient, SolrQuery solrQuery, StreamingResponseCallback callback) throws Exception {
    	log.info("solr query:{}", writeSolrQuery(solrQuery.toQueryString(), "UTF-8"));
    	ResponseParser parser = new StreamingBinaryResponseParser(callback);
	    QueryRequest req = new QueryRequest(solrQuery, METHOD.POST);
	    SolrAuth auth = SolrUtil.getSolrAuth(solrClient);
	    if(auth!=null) {
	    	//增加身份认证
	    	req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
	    }
	    req.setStreamingResponseCallback(callback);
	    req.setResponseParser(parser);
	    QueryResponse response = req.process(solrClient);
	    return response;
    }

    /**
     * 删除数据
     *
     * @param collection
     * @param query
     * @param slave 是否删除的是备用solr服务器
     * @throws Exception
     */
    public static void delete(String collection, String query, boolean slave) throws Exception {
        log.info("delete:" + query);
        SolrClient solrClient = null;
        try {
        	solrClient = SolrUtil.getClient(collection, slave);
        	// 删除原数据
			solrClient.deleteByQuery(query);
			solrClient.commit();
        } catch(Exception e) {
        	throw e;
        } finally {
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }
    }

    public static void delete(String collection, String query) throws Exception {
    	delete(collection, query, false);
    }

    /*public static List<BucketJsonFacet> jsonFacet(String collection, String[] fq, String facetStr) throws Exception {
        SolrQuery solrQuery = new SolrQuery("*:*");
        // 设定查询字段
        solrQuery.addFilterQuery(fq);
        solrQuery.setRows(0);
        solrQuery.set("json.facet", "{body:" + facetStr + "}");

        log.info("jsonFacet:"  + collection + writeSolrQuery(solrQuery.toQueryString(), "UTF-8"));

        QueryResponse response = call(solrQuery,collection);
        NestableJsonFacet nestableJsonFacet = response.getJsonFacetingResponse();
        List<BucketJsonFacet> list = nestableJsonFacet.getBucketBasedFacets("body").getBuckets();
        return list;
    }*/

    /**
     * jsonFacet 查询，只返回第一层数据
     *
     * @param collection
     * @param fq
     * @param facetStr
     * @param function
     * @throws Exception
     */
    public static void jsonFacet(String collection, String[] fq, String facetStr, Consumer<JSONObject> function) throws Exception {
    	String cacheType= "jsonFacet";
    	String cacheName=collection + "_" + facetStr;
    	if(fq !=null) {
    		for(String f :fq) {
    			cacheName = cacheName +"_" + f;
    		}
    	}
    	cacheName = MD5Util.getMD5(getCurrentDsName() + cacheName );

    	//尝试从缓存取数据
    	int expireSecond = getCacheExpireSeconds();
    	ArrayList<JSONObject> cacheObjectList =(ArrayList<JSONObject>) ObjectCacheWithFile.getObjectFromFile(cacheType, cacheName, expireSecond);

    	if(cacheObjectList != null) {
    		log.error("use file cache --jsonFacet1 ,expire time=" + expireSecond);

    		for(JSONObject obj :cacheObjectList) {
    			function.accept( obj);
    		}
    		return ;
    	}

    	cacheObjectList = new  ArrayList<JSONObject>();

    	RTimer timer = new RTimer();
    	SolrQuery solrQuery = new SolrQuery("*:*");
        // 设定查询字段
        if(fq != null && fq.length > 0){
            solrQuery.addFilterQuery(fq);
        }
        solrQuery.setRows(0);
        solrQuery.set("json.facet", "{body:" + facetStr + "}");
        solrQuery.setRequestHandler("/query");

        String msg = "jsonFacet:"  + collection + writeSolrQuery(solrQuery.toQueryString(), "UTF-8");
        log.info(msg);

        SolrClient solrClient = null;
        BufferedReader bufferedReader = null;
        JSONReader jsonReader = null;
        try {
        	solrClient = getClient(collection);
        	bufferedReader = new BufferedReader(jsonReader(solrClient, solrQuery));
            // 流式解析JSON文件
            jsonReader = new JSONReader(bufferedReader);

            jsonReader.startObject();
            while (jsonReader.hasNext()) {
                String elem = jsonReader.readString();
                if ("responseHeader".equals(elem)) {
                    jsonReader.startObject();
                    while (jsonReader.hasNext()) {
                        String key = jsonReader.readString();
                        Object value = jsonReader.readObject();
                        //判断状态
                        if ("status".equalsIgnoreCase(key) && !"0".equals(value.toString())) {
                            throw new EngineSolrException("查询SOLR失败!");
                        }
                    }
                    jsonReader.endObject();
                } else if ("response".equals(elem)) {
                    jsonReader.startObject();
                    // 跳过其他信息
                    while (jsonReader.hasNext()) {
                        String key = jsonReader.readString();
                        if ("numFound".equalsIgnoreCase(key)) {
                            jsonReader.readLong();
                        } else {
                            jsonReader.readObject();
                        }
                    }
                    jsonReader.endObject();
                } else if ("facets".equals(elem)) {
                    jsonReader.startObject();
                    // 跳过其他信息
                    while (jsonReader.hasNext()) {
                        if ("body".equalsIgnoreCase(jsonReader.readString())) {
                            jsonReader.startObject();
                            String key = jsonReader.readString();
                            if ("buckets".equalsIgnoreCase(key)) {
                                //读取docs数组信息
                                jsonReader.startArray();
                                // 循环ARRAY
                                while (jsonReader.hasNext()) {
                                    Object obj = jsonReader.readObject();
                                    function.accept((JSONObject) obj);

                                    //加入缓存
                                    cacheObjectList.add((JSONObject) obj);
                                }
                                jsonReader.endArray();
                            } else {
                                jsonReader.readObject();
                            }
                            jsonReader.endObject();
                        } else {
                            jsonReader.readObject();
                        }
                    }
                    jsonReader.endObject();
                }
            }
            jsonReader.endObject();

            //保存到缓存
            ObjectCacheWithFile.saveObjectToFile(cacheType, cacheName, cacheObjectList);
            displayTiming(msg, timer);
        } catch(Exception e) {
        	throw e;
        } finally {
        	//关闭流
        	if(bufferedReader!=null) {
        		bufferedReader.close();
        	}
        	if(jsonReader!=null) {
        		jsonReader.close();
        	}
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }
    }

    /**
     * jsonFacet 查询，返回facet json对象值
     *
     * @param collection
     * @param fq
     * @param facetStr
     * @throws Exception
     */
    public static JSONObject jsonFacet(String collection, String[] fq, String facetStr) throws Exception {
    	String cacheType= "jsonFacet";
    	String cacheName=collection + "_" + facetStr;
    	if(fq !=null) {
    		for(String f :fq) {
    			cacheName = cacheName +"_" + f;
    		}
    	}
    	cacheName = MD5Util.getMD5(getCurrentDsName() + cacheName );


    	int expireSecond = getCacheExpireSeconds();
    	JSONObject cacheObject =(JSONObject) ObjectCacheWithFile.getObjectFromFile(cacheType, cacheName, expireSecond);

    	if(cacheObject != null) {
    		log.error("use file cache--->jsonFacet2 ,expire second=" + expireSecond);
    		return cacheObject;
    	}

    	RTimer timer = new RTimer();
    	JSONObject resultJson;

        SolrQuery solrQuery = new SolrQuery("*:*");
        // 设定查询字段
        if(fq != null && fq.length > 0){
            solrQuery.addFilterQuery(fq);
        }

        solrQuery.setRows(0);
        solrQuery.set("json.facet", facetStr);
        solrQuery.setRequestHandler("/query");

        String msg = "jsonFacet:"  + collection + writeSolrQuery(solrQuery.toQueryString(), "UTF-8");
        log.info(msg);

        SolrClient solrClient = null;
        BufferedReader bufferedReader = null;
        JSONReader jsonReader = null;
        try {
        	solrClient = getClient(collection);
        	bufferedReader = new BufferedReader(jsonReader(solrClient, solrQuery));
            // 流式解析JSON文件
            jsonReader = new JSONReader(bufferedReader);
            resultJson = (JSONObject)jsonReader.readObject();

            JSONObject errorObj = resultJson.getJSONObject("error");
            if(errorObj != null){
                throw new EngineSolrException("查询SOLR失败:" + errorObj.getString("msg"));
            }
            resultJson = resultJson.getJSONObject("facets");

            //如果resultJson不为空，则放入缓存
            if(resultJson != null) {
            	ObjectCacheWithFile.saveObjectToFile(cacheType, cacheName, resultJson);
            }
            return resultJson;
        } catch(Exception e) {
        	throw e;
        } finally {
        	//关闭流
        	if(bufferedReader!=null) {
        		bufferedReader.close();
        	}
        	if(jsonReader!=null) {
        		jsonReader.close();
        	}
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        	displayTiming(msg, timer);
        }
    }

    // 不能添加过滤条件  用jsonFacet
/*    public static Map<String, Long> termsSingle(String collection, SolrQuery query, String field) throws Exception {
        query.setRequestHandler("/terms");
        query.setTerms(true);
        query.setTermsLimit(-1);
        query.addTermsField(field);
//        query.setTermsMinCount(1);
        List<TermsResponse.Term> terms = SolrUtil.call(query, collection).getTermsResponse().getTerms(field);

        Map<String, Long> map = new HashMap<>();
        for(TermsResponse.Term term: terms){
            map.put(term.getTerm(), term.getFrequency());
        }
        return map;
    }

    public static Map<String, Map<String, Long>> termsMulti(String collection, SolrQuery query, String[] fields) throws Exception {
        query.setRequestHandler("/terms");
        query.setTerms(true);
        query.setTermsLimit(-1);
        for(String field: fields){
            query.addTermsField(field);
        }
        Map<String, List<TermsResponse.Term>> termMap = SolrUtil.call(query, collection).getTermsResponse().getTermMap();
        Map<String, Map<String, Long>> resultMap = new HashMap<>();
        for(Map.Entry<String, List<TermsResponse.Term>> entry: termMap.entrySet()){
            Map<String, Long> map = new HashMap<>();
            resultMap.put(entry.getKey(), map);
            for(TermsResponse.Term term: entry.getValue()){
                map.put(term.getTerm(), term.getFrequency());
            }
        }
        return resultMap;
    }*/

    /**
     * 获取json输出流
     *
     * @param solrClient
     * @param query
     * @return
     * @throws IOException
     * @throws SolrServerException
     */
    private static InputStreamReader jsonReader(SolrClient solrClient, SolrQuery query) throws IOException, SolrServerException {

        QueryRequest request = new QueryRequest(query);
        SolrAuth auth = SolrUtil.getSolrAuth(solrClient);
	    if(auth!=null) {
	    	//增加身份认证
	    	request.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
	    }
        request.setResponseParser(new InputStreamResponseParser("json"));
        request.setMethod(METHOD.POST);

        NamedList<Object> genericResponse = solrClient.request(request);
        InputStream stream = (InputStream) genericResponse.get("stream");
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    /**
     *
     * 功能描述：按数据源切换solr
     *
     * @author  zhangly
     * <p>创建日期 ：2020年8月21日 下午3:30:02</p>
     *
     * @param collection
     * @param dataSource
     * @param salve 是否使用备用solr服务器
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static SolrClient getSolrClient(String collection, String dataSource, boolean salve) {
        // 根据登录用户获取数据源
    	SolrDataSourceProperty solrDsProperty = dynamicSolrProperties.getSolrDataSourceProperty(dataSource);
    	if(solrDsProperty==null) {
    		throw new RuntimeException("未找到solr数据源:"+dataSource);
    	}
        String zk = salve && StringUtils.isNotBlank(solrDsProperty.getSlaveZk()) ? solrDsProperty.getSlaveZk() : solrDsProperty.getZk();
        if(StringUtils.isBlank(zk)) {
        	// 非集群solr
        	String url = salve && StringUtils.isNotBlank(solrDsProperty.getSlaveUrl()) ? solrDsProperty.getSlaveUrl() : solrDsProperty.getUrl();
        	EngineHttpSolrClient.Builder builder = new EngineHttpSolrClient.Builder(url + "/" + collection)
        			.withSocketTimeout(dynamicSolrProperties.getDruid().getSocketTimeout())
        			.withConnectionTimeout(dynamicSolrProperties.getDruid().getCollectionTimeout());
        	HttpSolrClient solrClient = new EngineHttpSolrClient(builder, SolrUtil.getSolrAuth(solrDsProperty));
			/*HttpSolrClient solrClient = builder.build();*/
        	return solrClient;
        }
        List<String> zkUrls =  Arrays.asList(zk.split(","));
        CloudSolrClient.Builder builder = new EngineCloudSolrClient.EngineBuilder(zkUrls, Optional.empty())
        		.withSocketTimeout(dynamicSolrProperties.getDruid().getSocketTimeout())
        		.withConnectionTimeout(dynamicSolrProperties.getDruid().getCollectionTimeout());
        CloudSolrClient solrClient = new EngineCloudSolrClient(builder, SolrUtil.getSolrAuth(solrDsProperty));
        //CloudSolrClient solrClient = builder.build();
        solrClient.setDefaultCollection(collection);
        return solrClient;
    }

    public static Map<String, Object> initActionValue(Object value, String action){
        Map<String, Object> map = new HashMap<>();
        map.put(action,value);
        return map;
    }

    /**
     * 初始化字段转化MAP，驼峰转下划线大写
     *
     * @param clzz
     * @return
     */
    public static Map<String, String> initFieldMap(Class<?> clzz) {
        Map<String, String> map = new HashMap<>();
        List<Field> fieldList = new ArrayList<>();
        Class<?> tempClass = clzz;
        while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
        }
        for (Field f : fieldList) {
            String field = f.getName();
            String mapVal = oConvertUtils.camelToUnderlineUpper(field);
            if ("id".equals(field)) {
                mapVal = field;
            }
            map.put(field, mapVal);
        }
        return map;
    }

    public static Map<String, String> initFieldMap(String[] fields) {
        Map<String, String> map = new HashMap<>();
        for (String field : fields) {
            String mapVal = oConvertUtils.camelToUnderlineUpper(field);
            if ("id".equals(field)) {
                mapVal = field;
            }
            map.put(field, mapVal);
        }
        return map;
    }

    /**
     * 功能描述：SolrDocument与实体类转换
     *
     * @param <T>
     * @param document
     * @param clzz
     * @return
     * @author zhangly
     */
    public static <T> T solrDocumentToPojo(SolrDocument document, Class<T> clzz) throws Exception {
        if (null != document) {
            Object obj = clzz.newInstance();
            Method m = null;
            Class<?> fieldType = null;
            for (String fieldName : document.getFieldNames()) {
                Field[] filedArrays = clzz.getDeclaredFields(); // 获取类中所有属性
                for (Field f : filedArrays) {
                    if (f.getName().equals(fieldName)) {
                        // 获取到的属性名
                        f = clzz.getDeclaredField(fieldName);
                        // 属性类型
                        fieldType = f.getType();
                        // 构造set方法名 setId
                        String dynamicSetMethod = dynamicMethodName(f.getName(), "set");
                        // 获取方法
                        m = clzz.getMethod(dynamicSetMethod, fieldType);
                        // 获取到的值
                        // 如果是 int, float等基本类型，则需要转型
                        if (fieldType.equals(Integer.TYPE)) {
                            fieldType = Integer.class;
                        } else if (fieldType.equals(Float.TYPE)) {
                            fieldType = Float.class;
                        } else if (fieldType.equals(Double.TYPE)) {
                            fieldType = Double.class;
                        } else if (fieldType.equals(Boolean.TYPE)) {
                            fieldType = Boolean.class;
                        } else if (fieldType.equals(Short.TYPE)) {
                            fieldType = Short.class;
                        } else if (fieldType.equals(Long.TYPE)) {
                            fieldType = Long.class;
                        } else if (fieldType.equals(String.class)) {
                            fieldType = String.class;
                        } else if (fieldType.equals(Collection.class)) {
                            fieldType = Collection.class;
                        }
                        m.invoke(obj, fieldType.cast(document.getFieldValue(fieldName)));
                    }
                }
            }
            return clzz.cast(obj);
        }
        return null;
    }

    /**
     * 功能描述：SolrDocument与实体类转换
     *
     * @param <T>
     * @param document
     * @param clzz
     * @param fieldMap:字段映射map,{key:实体类字段名,value:solr存储的字段名}
     * @return
     * @author zhangly
     */
    public static <T> T solrDocumentToPojo(SolrDocument document, Class<T> clzz, Map<String, String> fieldMap) throws Exception {
        if (null == document) {
        	return null;
        }

        Object obj = clzz.newInstance();
        List<Field> fieldList = new ArrayList<>();
        Class<?> tempClass = clzz;
        while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
        }
        Method m = null;
        Class<?> fieldType = null;

        for (String fieldName : document.getFieldNames()) {
            // 需要说明的是返回的结果集中的FieldNames()比类属性多
            for (Field f : fieldList) {
                String mapping = fieldMap.get(f.getName());

                if (mapping == null || mapping.equalsIgnoreCase(fieldName) == false) {
                	continue;
                }

                //如果值为空，则忽略
                if(document.getFieldValue(mapping) == null) {
                	continue;
                }

                //bean class的 属性类型
                fieldType = f.getType();
                // 构造set方法名 setId
                String dynamicSetMethod = dynamicMethodName(f.getName(), "set");
                // 获取方法
                m = clzz.getMethod(dynamicSetMethod, fieldType);
                // 获取到的值
                // 如果是 int, float等基本类型，则需要转型
                if (fieldType.equals(Integer.TYPE)) {
                    fieldType = Integer.class;
                } else if (fieldType.equals(Float.TYPE)) {
                    fieldType = Float.class;
                } else if (fieldType.equals(Double.TYPE)) {
                    fieldType = Double.class;
                } else if (fieldType.equals(Boolean.TYPE)) {
                    fieldType = Boolean.class;
                } else if (fieldType.equals(Short.TYPE)) {
                    fieldType = Short.class;
                } else if (fieldType.equals(Long.TYPE)) {
                    fieldType = Long.class;
                } else if (fieldType.equals(Collection.class)) {
                    fieldType = Collection.class;
                }

                try {
                    if (fieldType.equals(String.class)) {
                        m.invoke(obj, String.valueOf(document.getFieldValue(mapping)));
                    } else if (fieldType.equals(BigDecimal.class)) {
                        m.invoke(obj, new BigDecimal(document.getFieldValue(mapping).toString()));
                    }
                    else if (fieldType.equals(java.util.Date.class)) {
                    	//转换日期为Date类型
                    	String value = document.getFieldValue(mapping).toString().trim();
                    	if("".equals(value)) {
                    		continue;
                    	}

                    	Date d = DateUtils.str2Date(value, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                        m.invoke(obj, d);

                    } else {
                        m.invoke(obj, fieldType.cast(document.getFieldValue(mapping)));
                    }

                } catch (Exception e) {
                    log.info(f.getName() + ":" + fieldType + "类型不正确");
                    throw e;
                }


            }
        }
        return clzz.cast(obj);


    }


    private static String dynamicMethodName(String name, String setOrGet) {
        String setMethodName = setOrGet + name.substring(0, 1).toUpperCase() + name.substring(1);
        return setMethodName;
    }

    /**
     * 功能描述：xml文件导入solr
     *
     * @param xmlPath
     * @param collection
     * @throws Exception <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     * @author zhangly
     * <p>创建日期 ：2020年1月2日 下午3:04:06</p>
     */
    public static void importXmlToSolr(String xmlPath, String collection) throws Exception {
        //如果没有配置postJarPath，则忽略数据导入
        if (StringUtils.isBlank(dynamicSolrProperties.getDruid().getPostJarPath())) {
            return;
        }
        String solrUrl = SolrUtil.getSolrUrl();
        solrUrl = solrUrl.concat("/").concat(collection).concat("/update");
        Properties properties = new Properties();
        properties.setProperty("type", "text/xml");
        properties.setProperty("url", solrUrl);
        SimplePostTool.execute(new String[] {xmlPath}, properties);
        //删除文件
        File file = new File(xmlPath);
        if(file.exists()) {
        	file.delete();
        }
    }

    /**
     *
     * 功能描述：json格式数据导入solr
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月18日 上午10:26:51</p>
     *
     * @param xmlPath
     * @param collection
     * @param slave 是否导入备用solr服务器
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static void importJsonToSolr(String xmlPath, String collection, boolean slave) throws Exception {
        //如果没有配置postJarPath，则忽略数据导入
        if (StringUtils.isBlank(dynamicSolrProperties.getDruid().getPostJarPath())) {
            return;
        }
        String solrUrl = SolrUtil.getSolrUrl(slave);
        solrUrl = solrUrl.concat("/").concat(collection).concat("/update");
        Properties properties = new Properties();
        properties.setProperty("type", "text/json");
        properties.setProperty("url", solrUrl);
        SimplePostTool.execute(new String[] {xmlPath}, properties);
        //删除文件
        File file = new File(xmlPath);
        if(file.exists()) {
        	file.delete();
        }
    }

    public static void importJsonToSolr(String xmlPath, String collection) throws Exception {
        log.info("开始导入：" + xmlPath);
    	importJsonToSolr(xmlPath, collection, false);
    }

    public static void importJsonToSolr(String xmlPath, String collection, boolean slave, boolean commit) throws Exception {
        //如果没有配置postJarPath，则忽略数据导入
        if (StringUtils.isBlank(dynamicSolrProperties.getDruid().getPostJarPath())) {
            return;
        }

        String solrUrl=collection;

        //如果collection不包含全路径，则需要获取配置文件的solr地址
        if(collection.startsWith("http:") ==false && collection.startsWith("https:") ==false) {
	        solrUrl = SolrUtil.getSolrUrl(slave);
	        solrUrl = solrUrl.concat("/").concat(collection).concat("/update");
        }

        Properties properties = new Properties();
        properties.setProperty("type", "text/json");
        properties.setProperty("url", solrUrl);
        properties.setProperty("commit", String.valueOf(commit));
        SimplePostTool.execute(new String[] {xmlPath}, properties);
        //删除文件
        File file = new File(xmlPath);
        if(file.exists()) {
        	file.delete();
        }
    }

    public static void importJsonToSolrNotDeleteFile(String xmlPath, String collection) throws Exception {
        //如果没有配置postJarPath，则忽略数据导入
        if (StringUtils.isBlank(dynamicSolrProperties.getDruid().getPostJarPath())) {
            return;
        }
        String solrUrl = SolrUtil.getSolrUrl();
        solrUrl = solrUrl.concat("/").concat(collection).concat("/update");
        Properties properties = new Properties();
        properties.setProperty("type", "text/json");
        properties.setProperty("url", solrUrl);
        SimplePostTool.execute(new String[] {xmlPath}, properties);
    }

    /**
     *
     * @param solrUpdateUrl 格式类似：http://10.63.82.188:8983/solr/STD_DRUGGROUP/update
     * @param queryString 删除数据的查询条件 ，如 *:*
     * @throws Exception
     */
    public static void deleteSolrDataByPostTool(String solrUpdateUrl, String queryString) throws Exception {
    	Properties properties = new Properties();
        properties.setProperty("data", "args");
        properties.setProperty("url", solrUpdateUrl);
        properties.setProperty("c", "gettingstarted");
        SimplePostTool.execute(new String[] {"<delete> <query>" + queryString + "</query></delete>"}, properties);
    }

    /**
     * 全量导出操作 返回(Map,index) -> {}
     *
     * @param solrQuery
     * @param collection
     * @param function
     * @return
     * @throws IOException
     * @throws SolrServerException
     */
    public static int exportDoc(SolrQuery solrQuery, String collection, BiConsumer<SolrDocument, Integer> function) throws Exception {
    	RTimer timer = new RTimer();
    	if(solrQuery.getRows() == null || solrQuery.getRows() == 0){
            solrQuery.setRows(dynamicSolrProperties.getDruid().getMaxRow());
        }
        final AtomicInteger count = new AtomicInteger(0);
        SolrClient solrClient = null;
        try {
        	solrClient = getClient(collection);
        	String msg = "export:"  + collection + writeSolrQuery(solrQuery.toQueryString(), "UTF-8");
            log.info(msg);
            StreamingResponseCallback callback = new StreamingResponseCallback() {
                @Override
                public void streamSolrDocument(SolrDocument doc) {
                    function.accept(doc, count.getAndIncrement());
                }

                @Override
                public void streamDocListInfo(long numFound, long start, Float maxScore) {
                    log.info("numFound:" + numFound + ",start:" + start + ",maxScore:" + maxScore);
                }
            };
            QueryRequest req = new QueryRequest(solrQuery, METHOD.POST);
            SolrAuth auth = SolrUtil.getSolrAuth(solrClient);
    	    if(auth!=null) {
    	    	//增加身份认证
    	    	req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
    	    }
            ResponseParser parser = new StreamingBinaryResponseParser(callback);
            req.setStreamingResponseCallback(callback);
            req.setResponseParser(parser);
            req.process(solrClient);
            displayTiming(msg, timer);
        } catch (SolrServerException | IOException e) {
            log.error("", e);
            throw new EngineSolrException("调用solr失败：" + e.getMessage());
        } finally {
        	if(solrClient!=null) {
        		solrClient.close();
        	}
        }

        return count.get();
    }

    /**
     *
     * 功能描述：根据登录用户获取solr数据源
     *
     * @author  zhangly
     * <p>创建日期 ：2020年9月21日 下午12:30:13</p>
     *
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static String getLoginUserDatasource() {
    	String datasource = ThreadUtils.getDatasource(); //从线程副本获取数据源
    	if(datasource!=null) {
    		return datasource;
    	}
    	if(!dynamicSolrProperties.isSingleton() && dynamicSolrProperties.isWeb()) {
    		Subject subject = SecurityUtils.getSubject();
       	 	LoginUser user = (LoginUser) subject.getPrincipal();
       	 	datasource = user.getDataSource();
       	 	return datasource;
    	}
    	return dynamicSolrProperties.getPrimary();
    }

    private static String writeSolrQuery(String str, String type){
        try {
            str = URLDecoder.decode(str, type);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(str.length() > 2500){
            str = str.substring(0,2500) + "...";
        }
        return str;

    }

    /**
     *
     * 功能描述：获取记录数
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月30日 上午10:41:32</p>
     *
     * @param conditionList
     * @param collection
     * @param slave
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static long count(List<String> conditionList, String collection, boolean slave) throws Exception {
    	SolrQuery solrQuery = new SolrQuery("*:*");
		// 设定查询字段
		solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
		solrQuery.setStart(0);
		solrQuery.setRows(1);
		QueryResponse queryResponse = SolrUtil.call(solrQuery, collection, slave);
		return queryResponse.getResults().getNumFound();
    }

    /**
     *
     * 功能描述：提交solr索引数据
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月17日 下午2:01:16</p>
     *
     * @param collection
     * @param slave
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static void commitByHttpURLConnection(String collection, boolean slave) {
        HttpURLConnection urlc = null;
        try {
            String solrUrl = SolrUtil.getSolrUrl();
            solrUrl = solrUrl.concat("/").concat(collection).concat("/update?commit=true");
        	URL url = new URL(solrUrl);
            urlc = (HttpURLConnection) url.openConnection();
    		urlc.setConnectTimeout(60000);
    		urlc.setReadTimeout(60000);
    		urlc.connect();
    		checkResponseCode(urlc);
        } catch(Exception e) {
        	log.error("", e);
        } finally {
        	try {
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch(Exception e) {}
        }
    }

    /**
     *
     * 功能描述：获取solr HttpURLConnection
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月18日 下午12:35:12</p>
     *
     * @param collection
     * @param slave
     * @param commit
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static HttpURLConnection getSolrHttpURLConnection(String collection, boolean slave, boolean commit) throws Exception {
        String solrUrl = SolrUtil.getSolrUrl();
        solrUrl = solrUrl.concat("/").concat(collection).concat("/update");
        if(commit) {
        	solrUrl = solrUrl.concat("?commit=true");
        }
        URL url = new URL(solrUrl);
        HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
		urlc.setConnectTimeout(60000);
		urlc.setReadTimeout(60000);
		urlc.setRequestMethod("POST");
		urlc.setDoOutput(true);
		urlc.setDoInput(true);
		urlc.setUseCaches(false);
		urlc.setAllowUserInteraction(false);
		urlc.setRequestProperty("Content-type", "text/json");
		urlc.setChunkedStreamingMode(-1);
		return urlc;
    }

    /**
     *
     * 功能描述：上传solr数据，还未commit（直接上传不使用文件流方式处理）
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月17日 下午1:05:35</p>
     *
     * @param collection
     * @param slave: 是否为备用solr
     * @param dataList: 数据对象
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static void postData(String collection, boolean slave, List<JSONObject> dataList) throws Exception {
    	postData(collection, slave, dataList, true);
    }
    /**
     *
     * 功能描述：上传solr数据（直接上传不使用文件流方式处理）
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月17日 下午2:06:34</p>
     *
     * @param collection
     * @param slave: 是否为备用solr
     * @param dataList: 数据对象
     * @param commit: 是否提交
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public static void postData(String collection, boolean slave, List<JSONObject> dataList, boolean commit) throws Exception {
    	if(dataList==null || dataList.size()==0) {
    		return;
    	}
		HttpURLConnection urlc = null;
		BufferedWriter bw = null;
		try {
			urlc = SolrUtil.getSolrHttpURLConnection(collection, slave, commit);
			urlc.connect();

			OutputStream out = urlc.getOutputStream();
			//写数据流
			bw = new BufferedWriter(new OutputStreamWriter(out));
			bw.write("[");
			for(JSONObject json : dataList) {
				bw.write(json.toJSONString());
				bw.write(',');
	        }
			bw.write("]");
			bw.flush();
			checkResponseCode(urlc);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if(bw!=null) {
					bw.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch(Exception e) {}
		}
	}


    public static void postData(String collection, List<String> dataList) throws Exception {
        if(dataList==null || dataList.size()==0) {
            return;
        }
        HttpURLConnection urlc = null;
        BufferedWriter bw = null;
        try {
            String solrUrl = SolrUtil.getSolrUrl();
            solrUrl = solrUrl.concat("/").concat(collection).concat("/update?commit=true");
            log.info("solrUrl:" + solrUrl);
            URL url = new URL(solrUrl);
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(60000);
            urlc.setReadTimeout(60000);
            urlc.setRequestMethod("POST");
            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", "text/json");
            urlc.setChunkedStreamingMode(-1);
            urlc.connect();

            OutputStream out = urlc.getOutputStream();
            //写数据流
            bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write("[");
            for(String json : dataList) {
                bw.write(json);
                bw.write(',');
            }
            bw.write("]");
            bw.flush();
            checkResponseCode(urlc);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if(bw!=null) {
                    bw.close();
                }
                if (urlc != null) {
                    urlc.disconnect();
                }
            } catch(Exception e) {}
        }
    }

    /**
     *
     * 功能描述：验证solr http请求响应是否正常
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月17日 下午2:08:24</p>
     *
     * @param urlc
     * @return
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private static boolean checkResponseCode(HttpURLConnection urlc) throws Exception {
		if (urlc.getResponseCode() >= 400) {
			Charset charset = StandardCharsets.ISO_8859_1;
			String contentType = urlc.getContentType();
			if (contentType != null) {
				int idx = contentType.toLowerCase(Locale.ROOT).indexOf("charset=");
				if (idx > 0) {
					charset = Charset.forName(contentType.substring(idx + "charset=".length()).trim());
				}
			}
			try (InputStream errStream = urlc.getErrorStream()) {
				if (errStream != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(errStream, charset));
					StringBuilder response = new StringBuilder("Response: ");
					int ch;
					while ((ch = br.read()) != -1) {
						response.append((char) ch);
					}
					throw new Exception(response.toString().trim());
				}
			}
			if (urlc.getResponseCode() == 401) {
				throw new GeneralSecurityException("Solr requires authentication (response 401). Please try again with '-u' option");
			}
			if (urlc.getResponseCode() == 403) {
				throw new GeneralSecurityException("You are not authorized to perform this action against Solr. (response 403)");
			}
			return false;
		}
		return true;
	}

    /**
     *
     * 功能描述：solr调用时长超过5秒输出日志
     *
     * @author  zhangly
     * <p>创建日期 ：2021年4月1日 下午7:48:31</p>
     *
     * @param msg
     * @param timer
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private static void displayTiming(String msg, RTimer timer) {
    	if(timer.getTime()<5000) {
    		return;
    	}
		SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		log.info(writeSolrQuery(msg, "UTF-8") + "\nsolr调用时长: " + df.format(new Date((long) timer.getTime())));
	}

    /**
     * 设置缓存生效时间
     */
    public static void setCacheExpireSeconds(int seconds) {
    	threadLocal_ExpireTime.set(seconds);
    }

    /**
     * 获取缓存生效时间
     * @return
     */
    private static int getCacheExpireSeconds() {
    	//初始化，从环境变量获取默认缓存时间
    	if(DEFAULT_CACHE_SECOND == null) {
			try {
				String cacheSecond = System.getProperty("cacheSecond", "300");
				DEFAULT_CACHE_SECOND =Integer.parseInt(cacheSecond);
			}catch(Exception ex) {
				DEFAULT_CACHE_SECOND=300;
			}
		}

    	//从当前的线程变量获取缓存时间
    	Integer tmpSecond = (Integer)threadLocal_ExpireTime.get();

    	//如果线程没放默认秒数，则返回默认的时间
		if(tmpSecond == null){
			return DEFAULT_CACHE_SECOND;
		}

		//清空线程中的设置
		threadLocal_ExpireTime.set(null);

		return tmpSecond.intValue();
    }

    //获取当前线程的缓存失效时间和默认的时间
    private static ThreadLocal<Integer> threadLocal_ExpireTime = new ThreadLocal<Integer>();
    private static Integer DEFAULT_CACHE_SECOND;

}
