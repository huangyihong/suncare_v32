/**
 * RowFailMessage.java	  V1.0   2018年11月30日 上午9:58:06
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.emport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class RowFailMessage implements Serializable, Comparable<RowFailMessage> {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3855839847127848585L;
	/** excel 所在行索引*/
	private int row;
	/** 行数据的异常信息*/
	private List<String> rowMessages;
	
	public RowFailMessage(int row) {
		this.row = row;
	}
	
	public void addRowMessage(String message) {
		if(StringUtils.isBlank(message)) {
			return;
		}
		if(rowMessages==null) {
			rowMessages = new ArrayList<String>();
		}
		rowMessages.add(message);
	}
	
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public List<String> getRowMessages() {
		return rowMessages;
	}
	public void setRowMessages(List<String> rowMessages) {
		this.rowMessages = rowMessages;
	}

	@Override
	public int compareTo(RowFailMessage o) {
		return this.row > o.row ? 1 : -1;
	}
}
