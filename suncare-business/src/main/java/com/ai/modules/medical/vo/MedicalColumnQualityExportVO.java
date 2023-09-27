package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalColumnQuality;
import lombok.Data;

@Data
public class MedicalColumnQualityExportVO extends MedicalColumnQuality {
    /**
     * 数据完整性
     */
    private Double result;

    /**
     * 是否有质控结果
     */
    private String hasResult;

}
