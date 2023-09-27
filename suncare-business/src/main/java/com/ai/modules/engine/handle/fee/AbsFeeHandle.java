/**
 * AbsFeeHandle.java	  V1.0   2020年10月29日 上午10:46:16
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.fee;

import org.apache.solr.common.SolrDocument;

import com.ai.modules.medical.entity.MedicalDrugRule;

public abstract class AbsFeeHandle {
	protected MedicalDrugRule rule;
	protected SolrDocument document;
	
	public AbsFeeHandle(MedicalDrugRule rule, SolrDocument document) {
		this.rule = rule;
		this.document = document;
	}
		
	public abstract FeeResult compulate();
}
