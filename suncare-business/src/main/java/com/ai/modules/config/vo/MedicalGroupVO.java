package com.ai.modules.config.vo;

import org.jeecg.common.aspect.annotation.AutoResultMap;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;


@Data
@AutoResultMap
public class MedicalGroupVO {
	
    private java.lang.String groupCode;
   
    @TableField(typeHandler = EncryptTypeHandler.class)
    private java.lang.String groupName;
    
    private java.lang.String itemId;
    
    private java.lang.String groupId;
    
    private java.lang.String code;
    
    @TableField(typeHandler = EncryptTypeHandler.class)
    private java.lang.String value;
    
    private java.lang.Long isOrder;
    
    private java.lang.String remark;
    
    private java.lang.String tableType;//子项所属表
    
    private java.lang.String actionType;//更新标志
	
}
