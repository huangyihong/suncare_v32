package com.ai.modules.ybFj.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : zhangly
 * @date : 2023/6/7 16:12
 */
@Data
public class FjDynamicConfigVo {

    /**字段名*/
    @ApiModelProperty(value = "字段名")
    private java.lang.String colName;
    /**字段中文名*/
    @ApiModelProperty(value = "字段中文名")
    private java.lang.String colCname;
    /**字段类型*/
    @ApiModelProperty(value = "字段类型")
    private java.lang.String colType;
    /**宽度*/
    @ApiModelProperty(value = "宽度")
    private java.lang.Integer colWidth;
    /**字段顺序*/
    @ApiModelProperty(value = "字段顺序")
    private java.math.BigDecimal colSeq;
}
