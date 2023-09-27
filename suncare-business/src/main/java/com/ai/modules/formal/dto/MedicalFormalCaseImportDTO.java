package com.ai.modules.formal.dto;

import com.ai.modules.formal.vo.MedicalFormalCaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description: 风控模型正式表导入转换
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MedicalFormalCaseImportDTO extends MedicalFormalCaseVO {

	private String relaItemTypeName;
	private String relaItems;

}
