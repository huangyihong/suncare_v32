/**
 * RepeatDocumentVO.java	  V1.0   2022年11月22日 上午10:32:26
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrDocument;

import lombok.Data;

@Data
public class RepeatDocumentVO {
	private String visitid;
	
	public RepeatDocumentVO(String visitid) {
		this.visitid = visitid;
	}
	
	private Set<String> itemcodeSet;
	private Map<String, SolrDocument> documentMap;
	
	public void addItemcode(String itemcode) {
		if(itemcodeSet==null) {
			itemcodeSet = new HashSet<String>();
		}
		itemcodeSet.add(itemcode);
	}
	
	public void addDocument(SolrDocument doc) {
		if(documentMap==null) {
			documentMap = new HashMap<String, SolrDocument>();
		}
		documentMap.put(doc.get("ITEMCODE").toString(), doc);
	}
}
