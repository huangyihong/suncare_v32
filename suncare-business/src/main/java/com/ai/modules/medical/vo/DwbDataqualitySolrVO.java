package com.ai.modules.medical.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DwbDataqualitySolrVO  {
    /**ID*/
    @ApiModelProperty(value = "ID")
    private java.lang.String id;
    /**COLUMN_ID*/
    @ApiModelProperty(value = "COLUMN_ID")
    private java.lang.String columnId;
    /**表名*/
    @ApiModelProperty(value = "表名")
    private java.lang.String tableName;
    /**表中文名*/
    @ApiModelProperty(value = "表中文名")
    private java.lang.String tableCnname;
    /**表字段名*/
    @ApiModelProperty(value = "表字段名")
    private java.lang.String columnName;
    /**表字段中文名*/
    @ApiModelProperty(value = "表字段中文名")
    private java.lang.String columnCnname;
    /**涉及不合规行为*/
    @ApiModelProperty(value = "涉及不合规行为")
    private java.lang.String actionNames;
    /**涉及不合规行为数量*/
    @ApiModelProperty(value = "涉及不合规行为数量")
    private java.lang.Double actionCnt;
    /**涉及规则数量*/
    @ApiModelProperty(value = "涉及规则数量")
    private java.lang.Double ruleCnt;
    /**创建时间*/
    @ApiModelProperty(value = "创建时间")
    private java.lang.String createTime;

    /**dwb_dataquality主键*/
    @ApiModelProperty(value = "dwb_dataquality主键")
    private java.lang.String dataqualityId;
    /**数据库名称*/
    @ApiModelProperty(value = "数据库名称")
    private java.lang.String databasename;
   /* *//**表名称 *//*
    @ApiModelProperty(value = "表名称 ")
    private java.lang.String tablename;*/
    /**机构ID*/
    @ApiModelProperty(value = "机构ID")
    private java.lang.String orgid;
    /**机构名称*/
    @ApiModelProperty(value = "机构名称")
    private java.lang.String orgname;
    /**数据来源*/
    @ApiModelProperty(value = "数据来源")
    private java.lang.String etlSource;
    /**数据来源名称*/
    @ApiModelProperty(value = "数据来源名称")
    private java.lang.String etlSourceName;
    /**字段名*/
   /* @ApiModelProperty(value = "字段名")
    private java.lang.String columnname;*/
    /**质控类型编码*/
    @ApiModelProperty(value = "质控类型编码")
    private java.lang.String ruletypeId;
    /**质控类型名称*/
    @ApiModelProperty(value = "质控类型名称")
    private java.lang.String ruletypeName;
    /**质控检查结果*/
    @ApiModelProperty(value = "质控检查结果")
    private java.lang.Double result;
    /**报告编号*/
    @ApiModelProperty(value = "报告编号")
    private java.lang.String reportid;
    /**流水号*/
    @ApiModelProperty(value = "流水号")
    private java.lang.String serialNumber;
    /**质量发布日期时间*/
    @ApiModelProperty(value = "质量发布日期时间")
    private java.lang.String createdate;
    /**对比表*/
    @ApiModelProperty(value = "对比表")
    private java.lang.String compareTable;
    /**质控检查结果字符串*/
    @ApiModelProperty(value = "质控检查结果字符串")
    private java.lang.String resultChar;
    /**项目名称*/
    @ApiModelProperty(value = "项目名称")
    private java.lang.String project;
    /**
     * 是否有质控结果
     */
    @ApiModelProperty(value = "是否有质控结果")
    private String hasResult;
    /**风控项目ID*/
    @ApiModelProperty(value = "风控项目ID")
    private java.lang.String projectId;
    /**风控项目名称*/
    @ApiModelProperty(value = "风控项目名称")
    private java.lang.String projectName;
}
