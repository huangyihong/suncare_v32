package com.ai.modules.formal.vo;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2019/11/29 19
 * @Description:
 */
public class CaseNode {
    private List<CaseNode> children;
    private CaseNode parent;
    private JSONObject data;
    private String key;
    private boolean fromYes;

    public CaseNode(){
        children = new ArrayList<>();
        fromYes = true;
    }

    public CaseNode getParent() {
        return parent;
    }

    public void setParent(CaseNode parent) {
        this.parent = parent;
    }

    public List<CaseNode> getChildren() {
        return children;
    }

    public void setChildren(List<CaseNode> children) {
        this.children = children;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isFromYes() {
        return fromYes;
    }

    public void setFromYes(boolean fromYes) {
        this.fromYes = fromYes;
    }

    public void addChild(CaseNode node){
        children.add(node);
    }
}
