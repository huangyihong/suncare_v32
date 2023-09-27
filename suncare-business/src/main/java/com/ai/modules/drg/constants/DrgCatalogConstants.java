package com.ai.modules.drg.constants;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : huangyh
 * @date : 2023/2/23 15:10
 */
public class DrgCatalogConstants {
    //MDC目录
    public static final String MDC_V="MDC_V";
    //ADRG目录
    public static final String ADRG_V="ADRG_V";
    //MDC主诊表
    public static final String MDC_INFO_V="MDC_INFO_V";
    //ADRG列表
    public static final String ADRG_LIST_V="ADRG_LIST_V";
    //MCC信息
    public static final String MCC_INFO_V="MCC_INFO_V";
    //CC信息
    public static final String CC_INFO_V="CC_INFO_V";
    //排除表信息
    public static final String EXCLUDE_INFO_V="EXCLUDE_INFO_V";
    //手术室手术信息
    public static final String SURGERY_INFO_V="SURGERY_INFO_V";
    //DRG目录
    public static final String DRG_V="DRG_V";

    //分组条件
    public static final String CONDITION_V="CONDITION_V";

    //分组 任务状态
    public static final String TASK_INIT = "init";
    public static final String TASK_WAIT = "wait";
    public static final String TASK_RUNNING = "running";
    public static final String TASK_NORMAL = "normal";
    public static final String TASK_ABNORMAL = "abnormal";

    public static final String YES = "是";
    public static final String NO = "否";
    public static final String MDCA = "MDCA";
    public static final String MDCP = "MDCP";
    public static final String MDCY = "MDCY";
    public static final String MDCZ = "MDCZ";

    public static final String CATALOG_TYPE_MDC = "MDC";
    public static final String CATALOG_TYPE_ADRG = "ADRG";
    public static final String CATALOG_TYPE_DRG = "DRG";

    public static final Map<String,CatalogTypeInfo> CATALOG_TYPE_MAP = new HashMap<String, CatalogTypeInfo>();
    static {
        CATALOG_TYPE_MAP.put(MDC_V, new CatalogTypeInfo(
                        "MDC目录",
                        "MDC目录",
                        new String[]{"MDC编码", "MDC名称","备注","审核状态"},
                        new String[]{"code","name","remark", "examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(ADRG_V, new CatalogTypeInfo(
                        "ADRG目录",
                        "ADRG目录",
                        new String[]{"ADRG编码", "ADRG名称","MDC目录编码","备注","分组条件限制","审核状态"},
                        new String[]{"code","name", "mdcCatalogCode", "remark","hasCondition", "examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(MDC_INFO_V, new CatalogTypeInfo(
                        "MDC主诊表目录",
                        "MDC主诊表目录",
                        new String[]{"疾病编码", "疾病名称","MDC目录编码","备注","审核状态"},
                        new String[]{"code","name","mdcCatalogCode","remark", "examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(ADRG_LIST_V, new CatalogTypeInfo(
                        "ADRG列表目录",
                        "ADRG列表目录",
                        new String[]{"MDC目录编码","ADRG目录编码","关联诊断组1", "疾病编码1","疾病名称1","关联诊断组2","疾病编码2","疾病名称2","手术或操作编码1","手术或操作名称1","手术或操作编码2","手术或操作名称2","手术或操作编码3","手术或操作名称3","分组条件","备注","审核状态"},
                        new String[]{"mdcCatalogCode","adrgCatalogCode","diagGroupCode1", "diagCode1","diagName1","diagGroupCode2","diagCode2","diagName2","surgeryCode1","surgeryName1","surgeryCode2","surgeryName2","surgeryCode3","surgeryName3","conditionName","remark","examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(MCC_INFO_V, new CatalogTypeInfo(
                        "MCC信息目录",
                        "MCC信息目录",
                        new String[]{"疾病编码", "疾病名称","排除内容","备注","审核状态"},
                        new String[]{"code","name", "excludeContent","remark","examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(CC_INFO_V, new CatalogTypeInfo(
                        "CC信息目录",
                        "CC信息目录",
                        new String[]{"疾病编码", "疾病名称","排除内容","备注","审核状态"},
                        new String[]{"code","name", "excludeContent","remark","examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(EXCLUDE_INFO_V, new CatalogTypeInfo(
                        "排除表信息目录",
                        "排除表信息目录",
                        new String[]{"疾病编码", "疾病名称","排除内容","备注","审核状态"},
                        new String[]{"code","name", "excludeContent","remark","examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(SURGERY_INFO_V, new CatalogTypeInfo(
                        "手术室手术信息目录",
                        "手术室手术信息目录",
                        new String[]{"手术编码", "手术名称","备注","审核状态"},
                        new String[]{"code","name", "remark","examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(DRG_V, new CatalogTypeInfo(
                        "DRG目录",
                        "DRG目录",
                        new String[]{"id主键","MDC目录编码","ADRG目录编码","DRG编码", "DRG名称","是否判断次要诊断","是否有效MCC","是否有效CC","备注","分组条件限制","审核状态"},
                        new String[]{"id","mdcCatalogCode","adrgCatalogCode","code","name","validSecondDiag","validMcc","validCc","remark","hasCondition","examineStatus"}
                )
        );
        CATALOG_TYPE_MAP.put(CONDITION_V, new CatalogTypeInfo(
                        "DRG分组条件",
                        "DRG分组条件",
                        new String[]{"分组条件编码", "分组条件名称","诊断数量","手术1","手术2","手术3","备注","审核状态"},
                        new String[]{"code","name","diagNum","validSurgery1","validSurgery2","validSurgery3","remark","examineStatus"}
                )
        );

    }


    @Data
    public static class CatalogTypeInfo implements java.io.Serializable{
        //导出文件名
        private String fileName;
        //导出sheet名
        private String sheefName;
        //导出字段标题
        private String[] titleArr;
        //导出字段名
        private String[] fieldArr;

        CatalogTypeInfo(
                     String fileName,
                     String sheefName,
                     String[] titleArr,
                     String[] fieldArr) {
            this.fileName = fileName;
            this.sheefName = sheefName;
            this.titleArr = titleArr;
            this.fieldArr = fieldArr;
        }
    }
}
