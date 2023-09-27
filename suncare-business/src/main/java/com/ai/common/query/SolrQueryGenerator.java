package com.ai.common.query;

import com.ai.common.utils.StringCamelUtils;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.review.vo.ReviewSecondVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.springframework.util.NumberUtils;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SolrQueryGenerator {

    private static int LIST_ALL_ROWS = 100000;

    public static Map<String, String> REVIEW_SECOND_MAPPING = SolrUtil.initFieldMap(ReviewSecondVo.class);
    public static Map<String, String> UNREASONABLE_ACTION_MAPPING = SolrUtil.initFieldMap(MedicalUnreasonableActionVo.class);

    public static SolrQuery initQuery(Object searchObj, Map<String, String[]> parameterMap) {
        long start = System.currentTimeMillis();
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.add("q", "*:*");
        // 前台只有单字段排序，多字段需要自己添加
        installQuery(solrQuery, searchObj, parameterMap);
        log.debug("---查询条件构造器初始化完成,耗时:" + (System.currentTimeMillis() - start) + "毫秒----");
        return solrQuery;
    }

    private static <T> void installQuery(SolrQuery solrQuery, T searchObj, Map<String, String[]> parameterMap) {
		/*
		 * 注意:权限查询由前端配置数据规则 当一个人有多个所属部门时候 可以在规则配置包含条件 orgCode 包含 #{sys_org_code}
		但是不支持在自定义SQL中写orgCode in #{sys_org_code}
		当一个人只有一个部门 就直接配置等于条件: orgCode 等于 #{sys_org_code} 或者配置自定义SQL: orgCode = '#{sys_org_code}'
		*/

        //区间条件组装 模糊查询 高级查询组装 简单排序 权限查询
        PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(searchObj);
        Map<String, SysPermissionDataRule> ruleMap = getRuleMap();

        String name, type;
        for (int i = 0; i < origDescriptors.length; i++) {
            //aliasName = origDescriptors[i].getName();  mybatis  不存在实体属性 不用处理别名的情况
            name = origDescriptors[i].getName();
            type = origDescriptors[i].getPropertyType().toString();
            try {
                if (judgedIsUselessField(name) || !PropertyUtils.isReadable(searchObj, name)) {
                    continue;
                }

                //数据权限查询
                if (ruleMap.containsKey(name)) {
                    addRuleToQueryWrapper(ruleMap.get(name), name, origDescriptors[i].getPropertyType(), solrQuery);
                }


                if (parameterMap != null) {
                    // 添加 判断是否有区间值
                    String endValue, beginValue;
                    if (parameterMap.containsKey(name + QueryGenerator.BEGIN) && parameterMap.containsKey(name + QueryGenerator.END)) {
                        beginValue = parameterMap.get(name + QueryGenerator.BEGIN)[0].trim();
                        endValue = parameterMap.get(name + QueryGenerator.END)[0].trim();
                        if (DATA_TIME_PATTERN.matcher(beginValue).find(0)) {
                            String timeStr = beginValue.substring(11);
                            if ("00:00:00".equals(timeStr)) {
                                beginValue = beginValue.substring(0, 10);
                            } else {
                                beginValue = beginValue.substring(0, 10) + "T" + beginValue.substring(11);
                            }
                        }
                        if (DATA_TIME_PATTERN.matcher(endValue).find(0)) {
                            endValue = endValue.substring(0, 10) + "T" + endValue.substring(11);
                        }
                        solrQuery.addFilterQuery(oConvertUtils.camelToUnderlineUpper(name) + ":[" + beginValue + " TO " + endValue + "]");
                    } else if (parameterMap.containsKey(name + QueryGenerator.BEGIN)) {
                        beginValue = parameterMap.get(name + QueryGenerator.BEGIN)[0].trim();
                        if (DATA_TIME_PATTERN.matcher(beginValue).find(0)) {
                            String timeStr = beginValue.substring(11);
                            if ("00:00:00".equals(timeStr)) {
                                beginValue = beginValue.substring(0, 10);
                            } else {
                                beginValue = beginValue.substring(0, 10) + "T" + beginValue.substring(11);
                            }
                        }
                        addQueryByRule(solrQuery, name, type, beginValue, QueryRuleEnum.GE);
                    } else if (parameterMap.containsKey(name + QueryGenerator.END)) {
                        endValue = parameterMap.get(name + QueryGenerator.END)[0].trim();
                        if (DATA_TIME_PATTERN.matcher(endValue).find(0)) {
                            endValue = endValue.substring(0, 10) + "T" + endValue.substring(11);
                        }
                        addQueryByRule(solrQuery, name, type, endValue, QueryRuleEnum.LE);
                    }
                }

                //判断单值  参数带不同标识字符串 走不同的查询
                // 这种前后带逗号的支持分割后模糊查询需要否 使多选字段的查询生效
                Object value = PropertyUtils.getSimpleProperty(searchObj, name);
                if (null != value && value.toString().startsWith(QueryGenerator.COMMA) && value.toString().endsWith(QueryGenerator.COMMA)) {
                    String multiLikeval = value.toString().replace(",,", QueryGenerator.COMMA);
                    String[] vals = multiLikeval.substring(1, multiLikeval.length()).split(QueryGenerator.COMMA);
                    final String field = oConvertUtils.camelToUnderlineUpper(name);
                    //TODO
					/*if(vals.length>1) {
						queryWrapper.and(j -> {
							j = j.like(field,vals[0]);
							for (int k=1;k<vals.length;k++) {
								j = j.or().like(field,vals[k]);
							}
							return j;
						});
					}else {
						queryWrapper.and(j -> j.like(field,vals[0]));
					}*/
                    solrQuery.addFilterQuery(field + ":(*" + StringUtils.join(vals, "* OR *") + "*)");
                } else {
                    //根据参数值带什么关键字符串判断走什么类型的查询
                    QueryRuleEnum rule = convert2Rule(value);
                    value = replaceValue(rule, value);
                    // add -begin 添加判断为字符串时设为全模糊查询
                    //if( (rule==null || QueryRuleEnum.EQ.equals(rule)) && "class java.lang.String".equals(type)) {
                    // 可以设置左右模糊或全模糊，因人而异
                    //rule = QueryRuleEnum.LIKE;
                    //}
                    // add -end 添加判断为字符串时设为全模糊查询
                    addEasyQuery(solrQuery, name, rule, value);
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        // 排序逻辑 处理
        doMultiFieldsOrder(solrQuery, parameterMap, searchObj);

        //高级查询
//		doSuperQuery(queryWrapper, parameterMap);

    }

    //多字段排序
    private static void doMultiFieldsOrder(SolrQuery solrQuery, Map<String, String[]> parameterMap, Object searchObj) {
        String column = null, order = null;
        if (parameterMap != null && parameterMap.containsKey(QueryGenerator.ORDER_COLUMN)) {
            column = parameterMap.get(QueryGenerator.ORDER_COLUMN)[0];
            if ("createTime".equals(column)) {
                try {
                    searchObj.getClass().getDeclaredField("createTime");
                } catch (NoSuchFieldException e) {
                    column = null;
                }
            }
        }
        if (parameterMap != null && parameterMap.containsKey(QueryGenerator.ORDER_TYPE)) {
            order = parameterMap.get(QueryGenerator.ORDER_TYPE)[0];
        }
        log.debug("排序规则>>列:" + column + ",排序方式:" + order);

        if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
            String[] columns = column.split(",");
            String[] orders = order.split(",");
            for (int i = 0, len = columns.length; i < len; i++) {
                String col = columns[i];
                //字典字段，去掉字典翻译文本后缀
                if (col.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
                    col = col.substring(0, col.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
                }
                //SQL注入check
//                SqlInjectionUtil.filterContent(col);

                if (orders.length <= i || orders[i].toUpperCase().contains(QueryGenerator.ORDER_TYPE_ASC)) {
                    solrQuery.addSort(oConvertUtils.camelToUnderlineUpper(col), SolrQuery.ORDER.asc);
                } else {
                    solrQuery.addSort(oConvertUtils.camelToUnderlineUpper(col), SolrQuery.ORDER.desc);
                }
            }

        }
    }

    public static <T> IPage<T> page(Page<T> page, T searchObj, String collection, Map<String, String> fieldMap, HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = initQuery(searchObj, req.getParameterMap());
        return page(page, (Class<T>) searchObj.getClass(), solrQuery, collection, fieldMap);
    }

    public static <T> IPage<T> page(Page<T> page, Class<T> clzz, SolrQuery solrQuery, String collection, Map<String, String> fieldMap) throws Exception {
        // 设置返回字段
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            solrQuery.addField(entry.getValue());
        }
        // 设定查询字段
        solrQuery.setStart(((Long) ((page.getCurrent() - 1) * page.getSize())).intValue());
        solrQuery.setRows(((Long) page.getSize()).intValue());
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        SolrDocumentList documents = queryResponse.getResults();
        List<T> result = new ArrayList<>();
        // 获取第一个类型参数的真实类型
        for (SolrDocument doc : documents) {
            T bean = SolrUtil.solrDocumentToPojo(doc, clzz, fieldMap);
            result.add(bean);
        }
        page.setRecords(result);
        page.setTotal(documents.getNumFound());
        return page;
    }

    public static IPage<SolrDocument> page(Page<SolrDocument> page, SolrQuery solrQuery, String collection, String[] fields) throws Exception {
        // 设置返回字段
        solrQuery.setFields(fields);
        solrQuery.addField("id");
        // 设定查询字段
        solrQuery.setStart(((Long) ((page.getCurrent() - 1) * page.getSize())).intValue());
        solrQuery.setRows(((Long) page.getSize()).intValue());
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        SolrDocumentList documents = queryResponse.getResults();
        List<SolrDocument> result = new ArrayList<>(documents);
        page.setRecords(result);
        page.setTotal(documents.getNumFound());
        return page;
    }

/*	public static <T> List<T> list(Class<T> clzz, SolrQuery solrQuery, String collection,Map<String,String> fieldMap) throws Exception {
		// 设定查询字段
if(solrQuery.getRows() == null || solrQuery.getRows() == 0) {
            solrQuery.setRows(LIST_ALL_ROWS);
        }
        		QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
		SolrDocumentList documents = queryResponse.getResults();
		List<T> result = new ArrayList<>();
		// 获取第一个类型参数的真实类型
		for (SolrDocument doc : documents) {
			T bean = SolrUtil.solrDocumentToPojo(doc, clzz, fieldMap);
			result.add(bean);
		}
		return result;
	}

	public static <T> SolrDocumentList list(SolrQuery solrQuery, String collection) throws Exception {
		// 设定查询字段
if(solrQuery.getRows() == null || solrQuery.getRows() == 0) {
            solrQuery.setRows(LIST_ALL_ROWS);
        }
        		QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
		SolrDocumentList documents = queryResponse.getResults();
		return documents;
	}*/

    public static <T> SolrDocumentList list(String collection, String[] fqs, String[] fls) throws Exception {
        // 设定查询字段
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(fqs);
        if (fls != null && fls.length > 0) {
            solrQuery.setFields(fls);
        }
        if (solrQuery.getRows() == null || solrQuery.getRows() == 0) {
            solrQuery.setRows(LIST_ALL_ROWS);
        }
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        SolrDocumentList documents = queryResponse.getResults();
        return documents;
    }

    public static <T> SolrDocumentList list(String collection, SolrQuery solrQuery) throws Exception {
        // 设定查询字段
        if (solrQuery.getRows() == null || solrQuery.getRows() == 0) {
            solrQuery.setRows(LIST_ALL_ROWS);
        }
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        SolrDocumentList documents = queryResponse.getResults();
        return documents;
    }

    public static <T> List<T> list(String collection, String[] fqs, Class<T> clzz, Map<String, String> fieldMap) throws Exception {
        // 设定查询字段
        SolrQuery solrQuery = new SolrQuery("*:*");
        // 设置返回字段
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            solrQuery.addField(entry.getValue());
        }
        solrQuery.addFilterQuery(fqs);
        if (solrQuery.getRows() == null || solrQuery.getRows() == 0) {
            solrQuery.setRows(LIST_ALL_ROWS);
        }
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        SolrDocumentList documents = queryResponse.getResults();

        List<T> list = new ArrayList<>();
        for (SolrDocument document : documents) {
            list.add(SolrUtil.solrDocumentToPojo(document, clzz, fieldMap));
        }
        return list;
    }

    public static <T> List<T> list(String collection, SolrQuery solrQuery, Class<T> clzz, Map<String, String> fieldMap) throws Exception {
        // 设置返回字段
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            solrQuery.addField(entry.getValue());
        }
        // 设定查询字段
        if (solrQuery.getRows() == null || solrQuery.getRows() == 0) {
            solrQuery.setRows(LIST_ALL_ROWS);
        }

        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        SolrDocumentList documents = queryResponse.getResults();

        List<T> list = new ArrayList<>();
        for (SolrDocument document : documents) {
            list.add(SolrUtil.solrDocumentToPojo(document, clzz, fieldMap));
        }
        return list;
    }

    public static <T> List<T> list(T searchObj, Map<String, String[]> parameterMap, String collection, Map<String, String> fieldMap) throws Exception {

        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, parameterMap);
        List<T> list = SolrQueryGenerator.list(collection, solrQuery,
                (Class<T>) searchObj.getClass(), fieldMap);
        return list;
    }

    public static <T> void updateById(String collection, T obj) throws Exception {
        SolrInputDocument doc = initInputDocument(obj);
        if (doc.getField("id") == null) {
            throw new Exception("id不能为空");
        }
        SolrClient solrClient = null;
        try {
            solrClient = SolrUtil.getClient(collection);
            solrClient.add(doc);
            solrClient.commit();
        } catch (Exception e) {
            throw e;
        } finally {
            if (solrClient != null) {
                solrClient.close();
            }
        }
    }

    // 字段转大写 id不变
    public static void updateByIds(String collection, Object obj, List<String> ids) throws Exception {
        updateByIds(collection, initInputDocument(obj), ids);
    }

    // 字段名与SOLR一致
    public static void updateByIds(String collection, JSONObject commonJson, List<String> ids) throws Exception {
        SolrInputDocument commonDoc = new SolrInputDocument();
        for (Map.Entry<String, Object> entry : commonJson.entrySet()) {
            commonDoc.setField(entry.getKey(), entry.getValue());
        }
        updateByIds(collection, commonDoc, ids);

    }

    // 字段名与SOLR一致
    public static void updateByIds(String collection, SolrInputDocument commonDoc, List<String> ids) throws Exception {
        SolrClient solr = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        for (String id : ids) {
            SolrInputDocument document = commonDoc.deepCopy();
            document.setField("id", id);
            solr.add(document);
        }
        solr.commit();
        solr.close();
    }


    // 字段转大写 id不变 字段更新，多值覆盖
    public static void updateByQuery(String collection, Object obj, SolrQuery query) throws Exception {
        updateByQuery(collection, initInputDocument(obj), query);
    }

    // 字段名与SOLR一致 SolrInputField 里的 value 应该为 {'set': value} 对象
    public static void updateByQuery(String collection, SolrInputDocument commonDoc, SolrQuery query) throws Exception {
        JSONObject commonJson = new JSONObject();
        for (Map.Entry<String, SolrInputField> entry : commonDoc.entrySet()) {
            SolrInputField inputField = entry.getValue();
            commonJson.put(inputField.getName(), inputField.getValue());
        }
        updateByQuery(collection, commonJson, query);

    }

    // 字段名与SOLR一致 字段更新，多值覆盖
    public static void updateByQuery(String collection, JSONObject commonJson, SolrQuery solrQuery) throws Exception {
        // 数据写入xml
        String importFilePath = SolrUtil.importFolder + "/updateByQuery/" + System.currentTimeMillis() + "_" + solrQuery.toQueryString().length() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");

        solrQuery.setFields("id");
        SolrUtil.export(solrQuery, collection, (map, index) -> {
            commonJson.put("id", map.get("id"));
            try {
                fileWriter.write(commonJson.toJSONString());
                fileWriter.write(',');
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fileWriter.write("]");
        fileWriter.close();
        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, collection);
    }

    private static SolrInputDocument initInputDocument(Object obj) throws IllegalAccessException {
        SolrInputDocument doc = new SolrInputDocument();
        Class clzz = obj.getClass();
        List<Field> fieldList = new ArrayList<>();
        do {
            fieldList.addAll(Arrays.asList(clzz.getDeclaredFields()));
            clzz = clzz.getSuperclass(); //得到父类,然后赋给自己
            //当父类为null的时候说明到达了最上层的父类(Object类).
        } while (clzz != null);

        for (Field field : fieldList) {
            field.setAccessible(true);
            Object valObj = field.get(obj);
            field.setAccessible(false);
            if (valObj == null) {
                continue;
            }
            String val = String.valueOf(valObj);
            if (StringUtils.isBlank(val)) {
                continue;
            }
            String name = field.getName();
            if ("id".equals(name)) {
                doc.setField(name, val);
            } else {
                doc.setField(StringCamelUtils.camel2Underline(name), SolrUtil.initActionValue(val, "set"));
            }

        }

        return doc;
    }


    public static <T> T getOne(String collection, String[] fqs, Class<T> clzz, Map<String, String> fieldMap) throws Exception {
        // 设定查询字段
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(fqs);
        solrQuery.setRows(1);
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        SolrDocumentList documents = queryResponse.getResults();
        if (documents.size() == 0) {
            return null;
        } else {
            return SolrUtil.solrDocumentToPojo(documents.get(0), clzz, fieldMap);
        }
    }

    public static SolrDocument getOne(String collection, String[] fqs, String[] fl) throws Exception {
        // 设定查询字段
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(fqs);
        solrQuery.setFields(fl);
        solrQuery.setRows(1);
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        SolrDocumentList documents = queryResponse.getResults();
        if (documents.size() == 0) {
            return null;
        } else {
            return documents.get(0);
        }
    }


    public static long count(String collection, String[] fqs) throws Exception {
        // 设定查询字段
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(fqs);
        solrQuery.setRows(0);
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        return queryResponse.getResults().getNumFound();

    }

    public static long count(String collection, SolrQuery solrQuery) throws Exception {
        // 设定查询字段
        solrQuery.setRows(0);
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        return queryResponse.getResults().getNumFound();

    }

    /**
     * 高级查询
     * @param queryWrapper
     * @param parameterMap
     */
//	public static void doSuperQuery(SolrQuery solrQuery,Map<String, String[]> parameterMap) {
//		if(parameterMap!=null&& parameterMap.containsKey(SUPER_QUERY_PARAMS)){
//			String superQueryParams = parameterMap.get(SUPER_QUERY_PARAMS)[0];
//			// 解码
//			try {
//				superQueryParams = URLDecoder.decode(superQueryParams, "UTF-8");
//			} catch (UnsupportedEncodingException e) {
//				log.error("--高级查询参数转码失败!", e);
//			}
//			List<QueryCondition> conditions = JSON.parseArray(superQueryParams, QueryCondition.class);
//			log.info("---高级查询参数-->"+conditions.toString());
//
//			for (QueryCondition rule : conditions) {
//				if(oConvertUtils.isNotEmpty(rule.getField()) && oConvertUtils.isNotEmpty(rule.getRule()) && oConvertUtils.isNotEmpty(rule.getVal())){
//					addEasyQuery(queryWrapper, rule.getField(), QueryRuleEnum.getByValue(rule.getRule()), rule.getVal());
//				}
//			}
//		}
//	}

    /**
     * 根据所传的值 转化成对应的比较方式
     * 支持><= like in !
     *
     * @param value
     * @return
     */
    public static QueryRuleEnum convert2Rule(Object value) {
        // 避免空数据
        if (value == null) {
            return null;
        }
        String val;
        if(value instanceof List){
            List vals = (List) value;
            if(vals.size() == 1){
                val = String.valueOf(vals.get(0)).trim();
            } else {
                return QueryRuleEnum.EQ;
            }
        } else {
            val = (value + "").trim();
        }

        if (val.length() == 0) {
            return null;
        }
        QueryRuleEnum rule = null;

        //update-begin--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
        //TODO 此处规则，只适用于 le lt ge gt
        // step 2 .>= =<
        if (val.length() >= 3) {
            if (QueryGenerator.QUERY_SEPARATE_KEYWORD.equals(val.substring(2, 3))) {
                rule = QueryRuleEnum.getByValue(val.substring(0, 2));
            }
        }
        // step 1 .> <
        if (rule == null && val.length() >= 2) {
            if (QueryGenerator.QUERY_SEPARATE_KEYWORD.equals(val.substring(1, 2))) {
                rule = QueryRuleEnum.getByValue(val.substring(0, 1));
            }
        }
        //update-end--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284---------------------

        // more
        if (rule == null && (val.startsWith(QueryGenerator.MORE))) {
            rule = QueryRuleEnum.MORE;
        }

        // step 3 like
        if (rule == null && val.contains(QueryGenerator.STAR)) {
            if (QueryGenerator.NOT_NULL.equals(value)) {
                rule = QueryRuleEnum.NOT_NULL;
            } else if (QueryGenerator.NULL.equals(value)) {
                // !*为空
                rule = QueryRuleEnum.NULL;
            } else if (val.startsWith(QueryGenerator.STAR) && val.endsWith(QueryGenerator.STAR)) {
                rule = QueryRuleEnum.LIKE;
            } else if (val.startsWith(QueryGenerator.STAR)) {
                rule = QueryRuleEnum.LEFT_LIKE;
            } else if (val.endsWith(QueryGenerator.STAR)) {
                rule = QueryRuleEnum.RIGHT_LIKE;
            }
        }

        // step 4 in
        if (rule == null && val.contains(QueryGenerator.MORE)) {
            //TODO in 查询这里应该有个bug  如果一字段本身就是多选 此时用in查询 未必能查询出来
            rule = QueryRuleEnum.IN;
        }
        if (rule == null && val.contains(QueryGenerator.COMMA)) {
            rule = QueryRuleEnum.EQ;
        }
        // step 5 !=
        if (rule == null && val.startsWith(QueryGenerator.NOT_EQUAL)) {
            rule = QueryRuleEnum.NE;
        }
        return rule != null ? rule : QueryRuleEnum.EQ;
    }

    private static Pattern DATA_TIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}.*");//实例化baiPattern

    /**
     * 替换掉关键字字符
     *
     * @param rule
     * @param value
     * @return
     */
    public static Object replaceValue(QueryRuleEnum rule, Object value) {
        if (rule == null) {
            return null;
        }
        if (value instanceof List) {
            List valList = (List) value;
            if (valList.size() == 0) {
                return null;
            } else if (valList.size() > 1 && rule == QueryRuleEnum.IN || rule == QueryRuleEnum.EQ) {
                value = valList.toArray(new Object[0]);
            } else {
                value = valList.get(0);
            }
        }

        String val;

        if (value instanceof String) {
            val = (value + "").trim();
            Matcher matcher = DATA_TIME_PATTERN.matcher(val);
            if (matcher.find(0)) {
                value = val.substring(0, 10) + "T" + val.substring(11);
                return value;
            }
        } else {
            return value;
        }

        if (rule == QueryRuleEnum.LIKE) {
            value = val.substring(1, val.length() - 1);
        } else if (rule == QueryRuleEnum.LEFT_LIKE || rule == QueryRuleEnum.NE) {
            value = val.substring(1);
        } else if (rule == QueryRuleEnum.RIGHT_LIKE) {
            value = val.substring(0, val.length() - 1);
        } else if (rule == QueryRuleEnum.IN) {
            value = val.split("\\|");
        } else if (rule == QueryRuleEnum.MORE) {
            value = value.toString().replace("，", ",").substring(1);
        } else {
            //update-begin--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
            if (val.startsWith(rule.getValue())) {
                //TODO 此处逻辑应该注释掉-> 如果查询内容中带有查询匹配规则符号，就会被截取的（比如：>=您好）
                value = val.replaceFirst(rule.getValue(), "");
            } else if (val.startsWith(rule.getCondition() + QueryGenerator.QUERY_SEPARATE_KEYWORD)) {
                value = val.replaceFirst(rule.getCondition() + QueryGenerator.QUERY_SEPARATE_KEYWORD, "").trim();
            }
            //update-end--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
        }

        return value;
    }

    private static void addQueryByRule(SolrQuery solrQuery, String name, String type, String value, QueryRuleEnum rule) throws ParseException {
        if (!"".equals(value)) {
            Object temp;
            switch (type) {
                case "class java.lang.Integer":
                    temp = Integer.parseInt(value);
                    break;
                case "class java.math.BigDecimal":
                    temp = new BigDecimal(value);
                    break;
                case "class java.lang.Short":
                    temp = Short.parseShort(value);
                    break;
                case "class java.lang.Long":
                    temp = Long.parseLong(value);
                    break;
                case "class java.lang.Float":
                    temp = Float.parseFloat(value);
                    break;
                case "class java.lang.Double":
                    temp = Double.parseDouble(value);
                    break;
                case "class java.util.Date":
                    temp = getDateQueryByRule(value, rule);
                    break;
                default:
                    temp = value;
                    break;
            }
            addEasyQuery(solrQuery, name, rule, temp);
        }
    }

    /**
     * 获取日期类型的值
     *
     * @param value
     * @param rule
     * @return
     * @throws ParseException
     */
    private static Date getDateQueryByRule(String value, QueryRuleEnum rule) throws ParseException {
        Date date = null;
        if (value.length() == 10) {
            if (rule == QueryRuleEnum.GE) {
                //比较大于
                date = QueryGenerator.getTime().parse(value + " 00:00:00");
            } else if (rule == QueryRuleEnum.LE) {
                //比较小于
                date = QueryGenerator.getTime().parse(value + " 23:59:59");
            }
            //TODO 日期类型比较特殊 可能oracle下不一定好使
        }
        if (date == null) {
            date = QueryGenerator.getTime().parse(value);
        }
        return date;
    }

    /**
     * 根据规则走不同的查询
     *
     * @param solrQuery SolrQuery
     * @param name      字段名字
     * @param rule      查询规则
     * @param value     查询条件值
     */
    private static void addEasyQuery(SolrQuery solrQuery, String name, QueryRuleEnum rule, Object value) {
//        if (oConvertUtils.isEmpty(value) || rule == null) {
        if (value == null || value.toString().length() == 0 || rule == null) {
            return;
        }

        name = oConvertUtils.camelToUnderlineUpper(name);
//		log.info("--查询规则-->"+name+" "+rule.getValue()+" "+value);
        List<String> queryList = getQueryRule(name, rule, value);
        queryList.forEach(solrQuery::addFilterQuery);
    }


    public static List<String> getQueryRule(String name, QueryRuleEnum rule, Object value) {

        if (QueryGenerator.NOT_NULL.equals(value)) {
            rule = QueryRuleEnum.NOT_NULL;
        } else if (QueryGenerator.NULL.equals(value)) {
            // !*为空
            rule = QueryRuleEnum.NULL;
        }

        List<String> results = new ArrayList<>();
        String result = null;
        switch (rule) {
            case LIKE:
                result = name + ":*" + escapeQueryChars(value.toString()) + "*";
                break;
            case EQ:
                if (value.getClass().isArray()) {
                    for (Object val : (Object[]) value) {
                        results.add(name + ":" + escapeQueryChars(val.toString()) );
                    }
                } else {
                    result = name + ":" + escapeQueryChars(value.toString());
                }
                break;
            case NE:
                result = "-" + name + ":" + escapeQueryChars(value.toString()) ;
                break;
            case IN:
                if (value.getClass().isArray()) {
                    result = name + ":(" + escapeJoin((Object[]) value, " OR ") + ")";
                } else {
                    result = name + ":(" + escapeQueryChars(value.toString()) + ")";
                }
                break;
            case NULL:
                result = "-" + name + ":?*";
                break;
            case NOT_NULL:
                result = name + ":?*";
                break;
            case LEFT_LIKE:
                result = name + ":*" + escapeQueryChars(value.toString());
                break;
            case RIGHT_LIKE:
                result = name + ":" + escapeQueryChars(value.toString()) + "*";
                break;
            case MORE:
                // ,为且  |为或
                String[] andArr = value.toString().split(",");//and
                for (String andStr : andArr) {
                    String[] orArr = andStr.split("\\|");//or
                    results.add(name + ":(*" + escapeJoin(orArr, "* OR *") + "*)");
                }
                break;
            case GT:
                result = name + ":{" + value + " TO *]";
                break;
            case GE:
                result =  name + ":[" + value + " TO *]";
                break;
            case LT:
                result = name + ":[* TO " + value + "}";
                break;
            case LE:
                result = name + ":[* TO " + value + "]";
                break;
            default:
                log.info("--查询规则未匹配到---");
                break;
        }
        if(result != null){
            results.add(result);
        }
        return results;
    }


    private static String escapeJoin(Object[] array, String separator) {
        if (array == null) {
            return null;
        } else {
            StringBuilder buf = new StringBuilder();

            for (Object o : array) {
                if (o == null) {
                    continue;
                }
                if (buf.length() > 0) {
                    buf.append(separator);
                }

                buf.append(escapeQueryChars(o.toString()));
            }

            return buf.toString();
        }
    }

    private static String escapeQueryChars(String str){
        return EngineUtil.escapeQueryChars(str);
    }

    /**
     * @param name
     * @return
     */
    private static boolean judgedIsUselessField(String name) {
        return "class".equals(name) || "ids".equals(name)
                || "page".equals(name) || "rows".equals(name)
                || "sort".equals(name) || "order".equals(name);
    }


    /**
     * @return
     */
    public static Map<String, SysPermissionDataRule> getRuleMap() {
        Map<String, SysPermissionDataRule> ruleMap = new HashMap<String, SysPermissionDataRule>();
        List<SysPermissionDataRule> list = JeecgDataAutorUtils.loadDataSearchConditon();
        if (list != null && list.size() > 0) {
            if (list.get(0) == null) {
                return ruleMap;
            }
            for (SysPermissionDataRule rule : list) {
                String column = rule.getRuleColumn();
                if (QueryRuleEnum.SQL_RULES.getValue().equals(rule.getRuleConditions())) {
                    column = QueryGenerator.SQL_RULES_COLUMN + rule.getId();
                }
                ruleMap.put(column, rule);
            }
        }
        return ruleMap;
    }

    private static void addRuleToQueryWrapper(SysPermissionDataRule dataRule, String name, Class propertyType, SolrQuery solrQuery) {
        QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
        if (rule.equals(QueryRuleEnum.IN) && !propertyType.equals(String.class)) {
            String[] values = dataRule.getRuleValue().split("\\|");
            Object[] objs = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                objs[i] = NumberUtils.parseNumber(values[i], propertyType);
            }
            addEasyQuery(solrQuery, name, rule, objs);
        } else {
            if (propertyType.equals(String.class)) {
                addEasyQuery(solrQuery, name, rule, converRuleValue(dataRule.getRuleValue()));
            } else {
                addEasyQuery(solrQuery, name, rule, NumberUtils.parseNumber(dataRule.getRuleValue(), propertyType));
            }
        }
    }

    public static String converRuleValue(String ruleValue) {
        String value = JwtUtil.getSessionData(ruleValue);
        if (oConvertUtils.isEmpty(value)) {
            value = JwtUtil.getUserSystemData(ruleValue, null);
        }
        return value != null ? value : ruleValue;
    }

    public static String getSqlRuleValue(String sqlRule) {
        try {
            Set<String> varParams = getSqlRuleParams(sqlRule);
            for (String var : varParams) {
                String tempValue = converRuleValue(var);
                sqlRule = sqlRule.replace("#{" + var + "}", tempValue);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return sqlRule;
    }

    /**
     * 获取sql中的#{key} 这个key组成的set
     */
    public static Set<String> getSqlRuleParams(String sql) {
        if (oConvertUtils.isEmpty(sql)) {
            return null;
        }
        Set<String> varParams = new HashSet<String>();
        String regex = "\\#\\{\\w+\\}";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(sql);
        while (m.find()) {
            String var = m.group();
            varParams.add(var.substring(var.indexOf("{") + 1, var.indexOf("}")));
        }
        return varParams;
    }

    /**
     * 获取查询条件
     *
     * @param field
     * @param alias
     * @param value
     * @param isString
     * @return
     */
    public static String getSingleQueryConditionSql(String field, String alias, Object value, boolean isString) {
        if (value == null) {
            return "";
        }
        field = alias + oConvertUtils.camelToUnderlineUpper(field);
        QueryRuleEnum rule = SolrQueryGenerator.convert2Rule(value);
        return getSingleSqlByRule(rule, field, value, isString);
    }

    public static String getSingleSqlByRule(QueryRuleEnum rule, String field, Object value, boolean isString) {
        String res = "";
        switch (rule) {
            case GT:
                res = field + rule.getValue() + getFieldConditionValue(value, isString);
                break;
            case GE:
                res = field + rule.getValue() + getFieldConditionValue(value, isString);
                break;
            case LT:
                res = field + rule.getValue() + getFieldConditionValue(value, isString);
                break;
            case LE:
                res = field + rule.getValue() + getFieldConditionValue(value, isString);
                break;
            case EQ:
                res = field + rule.getValue() + getFieldConditionValue(value, isString);
                break;
            case NE:
                res = field + " <> " + getFieldConditionValue(value, isString);
                break;
            case IN:
                res = field + " in " + getInConditionValue(value, isString);
                break;
            case LIKE:
                res = field + " like " + getLikeConditionValue(value);
                break;
            case LEFT_LIKE:
                res = field + " like " + getLikeConditionValue(value);
                break;
            case RIGHT_LIKE:
                res = field + " like " + getLikeConditionValue(value);
                break;
            default:
                res = field + " = " + getFieldConditionValue(value, isString);
                break;
        }
        return res;
    }

    private static String getFieldConditionValue(Object value, boolean isString) {
        String str = value.toString().trim();
        if (str.startsWith("!")) {
            str = str.substring(1);
        } else if (str.startsWith(">=")) {
            str = str.substring(2);
        } else if (str.startsWith("<=")) {
            str = str.substring(2);
        } else if (str.startsWith(">")) {
            str = str.substring(1);
        } else if (str.startsWith("<")) {
            str = str.substring(1);
        }
        if (isString) {
            return " '" + str + "' ";
        } else {
            return value.toString();
        }
    }

    private static String getInConditionValue(Object value, boolean isString) {
        if (isString) {
            String temp[] = value.toString().split(",");
            String res = "";
            for (String string : temp) {
                res += ",'" + string + "'";
            }
            return "(" + res.substring(1) + ")";
        } else {
            return "(" + value.toString() + ")";
        }
    }

    private static String getLikeConditionValue(Object value) {
        String str = value.toString().trim();
        if (str.startsWith("*") && str.endsWith("*")) {
            return "'%" + str.substring(1, str.length() - 1) + "%'";
        } else if (str.startsWith("*")) {
            return "'%" + str.substring(1) + "'";
        } else if (str.endsWith("*")) {
            return "'" + str.substring(0, str.length() - 1) + "%'";
        } else {
            if (str.indexOf("%") >= 0) {
                return str;
            } else {
                return "'%" + str + "%'";
            }
        }
    }

    /**
     *   根据权限相关配置生成相关的SQL 语句
     * @param searchObj
     * @param parameterMap
     * @return
     */
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public static String installAuthJdbc(Class<?> clazz) {
//		StringBuffer sb = new StringBuffer();
//		//权限查询
//		Map<String,SysPermissionDataRule> ruleMap = getRuleMap();
//		PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(clazz);
//		String sql_and = " and ";
//		for (String c : ruleMap.keySet()) {
//			if(oConvertUtils.isNotEmpty(c) && c.startsWith(QueryGenerator.SQL_RULES_COLUMN)){
//				sb.append(sql_and+getSqlRuleValue(ruleMap.get(c).getRuleValue()));
//			}
//		}
//		String name;
//		for (int i = 0; i < origDescriptors.length; i++) {
//			name = origDescriptors[i].getName();
//			if (judgedIsUselessField(name)) {
//				continue;
//			}
//			if(ruleMap.containsKey(name)) {
//				SysPermissionDataRule dataRule = ruleMap.get(name);
//				QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
//				Class propType = origDescriptors[i].getPropertyType();
//				boolean isString = propType.equals(String.class);
//				Object value;
//				if(isString) {
//					value = converRuleValue(dataRule.getRuleValue());
//				}else {
//					value = NumberUtils.parseNumber(dataRule.getRuleValue(),propType);
//				}
//				String filedSql = getSingleSqlByRule(rule, oConvertUtils.camelToUnderlineUpper(name), value,isString);
//				sb.append(sql_and+filedSql);
//			}
//		}
//		log.info("query auth sql is:"+sb.toString());
//		return sb.toString();
//	}

    /**
     * 根据权限相关配置 组装mp需要的权限
     * @param searchObj
     * @param parameterMap
     * @return
     */
//	public static void installAuthMplus(SolrQuery solrQuery,Class<?> clazz) {
//		//权限查询
//		Map<String,SysPermissionDataRule> ruleMap = getRuleMap();
//		PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(clazz);
//		for (String c : ruleMap.keySet()) {
//			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
//				queryWrapper.and(i ->i.apply(getSqlRuleValue(ruleMap.get(c).getRuleValue())));
//			}
//		}
//		String name;
//		for (int i = 0; i < origDescriptors.length; i++) {
//			name = origDescriptors[i].getName();
//			if (judgedIsUselessField(name)) {
//				continue;
//			}
//			if(ruleMap.containsKey(name)) {
//				addRuleToQueryWrapper(ruleMap.get(name), name, origDescriptors[i].getPropertyType(), queryWrapper);
//			}
//		}
//	}

}
