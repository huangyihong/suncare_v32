package com.ai.modules.config.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.jeecg.common.aspect.annotation.AutoResultMap;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;


@Data
@AutoResultMap
public class MedicalCodeNameVO {
    private String id;

    private String code;

    @TableField(typeHandler = EncryptTypeHandler.class)
    private String name;


    @TableField(typeHandler = EncryptTypeHandler.class)
    private String value;


}
