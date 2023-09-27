package com.ai.modules.review.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Component;

import com.ai.common.utils.StringCamelUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SolrQueryUtil {
	
	//获取查询信息
	public static List<String> getFqList(HttpServletRequest req,Map<String,String> keySymbolMap,Map<String, String> fieldMapping) {
		List<String> fqList = new ArrayList<>();//查询字段
		Iterator<Map.Entry<String,String>> entries = keySymbolMap.entrySet().iterator();
		while (entries.hasNext()) {
		  Map.Entry<String,String> entry = entries.next();
		  String symbol = entry.getValue();
		  String key = entry.getKey();
		  if(fieldMapping!=null&&StringUtils.isNotBlank(fieldMapping.get(entry.getKey()))) {
			  key = fieldMapping.get(entry.getKey());
		  }else {
			  key = StringCamelUtils.camel2Underline(key); 
		  }
		  if("between".equals(symbol)) {
				String value1 = req.getParameter(entry.getKey()+"1");
				String value2 = req.getParameter(entry.getKey()+"2");
				if(StringUtils.isNotBlank(value1)||StringUtils.isNotBlank(value2)) {
					fqList.add(key + ":[" + (StringUtils.isNotBlank(value1)?value1:"*") 
							+ " TO " + (StringUtils.isNotBlank(value2)?value2:"*") +"]");
				}
		  }else {
			  String value = req.getParameter(entry.getKey());
			  if(StringUtils.isNotBlank(value)) {
				  fqList.add(key+":" + value + "");
			  } 
		  }
		  
		}
		return fqList;
	}
	
	//solr结果集转List<Map<String,Object>>
	public static List<Map<String,Object>> putSolrResult(SolrDocumentList results) {
		List<Map<String,Object>> list = new ArrayList<>();
		for(SolrDocument docment: results){
			Map item = new HashMap<String,Object>();
			Set<String> set = docment.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = it.next();
				Object value = "";
				if (docment.get(key) instanceof ArrayList) {
					ArrayList arr = (ArrayList) docment.get(key);
					value = StringUtils.join(arr, ",");
				} else {
					value =  docment.get(key);
				}
				item.put(key, value);
			}
			list.add(item);
		}
		return list;
	}
	
	//solr结果转 Map<String,Object>
	public static  Map<String,Object> putSolrResult(SolrDocument docment) {
		Set<String> set = docment.keySet();
		Iterator<String> it = set.iterator();
		Map item = new HashMap<String,Object>();
		while (it.hasNext()) {
			String key = it.next();
			Object value = "";
			if (docment.get(key) instanceof ArrayList) {
				ArrayList arr = (ArrayList) docment.get(key);
				value = StringUtils.join(arr, ",");
			} else {
				value =  docment.get(key);
			}
			item.put(key, value);
		}
		return item;
	}
}
