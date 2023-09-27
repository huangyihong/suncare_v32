package com.ai.modules.config.vo;

import org.jeecg.common.aspect.annotation.AutoResultMap;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;


@Data
@AutoResultMap
public class MedicalTreatProjectEquipmentVO {
	
	private String id;//主键
	
    private String code;//项目编码
   
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String name;//项目名称
    
    private String typeCode;//上级分类编码
    
    private String typeName;// 上级分类名称
    
    private String state;// 数据状态
	
}
