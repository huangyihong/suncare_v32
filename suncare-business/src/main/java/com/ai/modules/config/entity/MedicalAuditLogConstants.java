package com.ai.modules.config.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;


/**
 *
 * 审核操作用到的常量类
 *
 */
public class MedicalAuditLogConstants {

	/**
	 * 新增
	 */
	public static final String ACTIONTYPE_ADD = "add";
	/**
	 * 修改
	 */
	public static final String ACTIONTYPE_UPDATE = "update";
	/**
	 * 删除
	 */
	public static final String ACTIONTYPE_DELETE = "delete";
	/**
	 * 清理
	 */
	public static final String ACTIONTYPE_CLEAN = "clean";
	/**
	 * 撤销
	 */
	public static final String ACTIONTYPE_UNDO = "undo";
	/**
	 * Excel导出
	 */
	public static final String ACTIONTYPE_EXPORT = "export";
	/**
	 * 待审核
	 */
	public static final String AUDITRESULT_DSH = "0";
	/**
	 * 审核通过
	 */
	public static final String AUDITRESULT_SHTG = "1";
	/**
	 * 审核不通过
	 */
	public static final String AUDITRESULT_SHBTG = "2";
	/**
	 * 待生效
	 */
	public static final String STATE_DSX = "0";
	/**
	 * 有效
	 */
	public static final String STATE_YX = "1";
	/**
	 * 无效
	 */
	public static final String STATE_WX = "2";

	/**
	 * 批量处理条数
	 */
	public static final int BATCH_SIZE = 500;

	/**
	 * 异步处理条数
	 */
	public static final int THREAD_SIZE = 1000;

	/**
	 * 更新标志(0修改1增加2删除)
	 */
	public static final String[] importActionTypeArr = {"0","1","2"};

	/**
	 * 操作状态
	 */
	public static final Map<String,String> ACTIONTYPE_MAP = new HashMap<String, String>();
	static {
		ACTIONTYPE_MAP.put(ACTIONTYPE_ADD, "新增");
		ACTIONTYPE_MAP.put(ACTIONTYPE_UPDATE, "修改");
		ACTIONTYPE_MAP.put(ACTIONTYPE_DELETE, "删除");
		ACTIONTYPE_MAP.put(ACTIONTYPE_CLEAN, "清理");
		ACTIONTYPE_MAP.put(ACTIONTYPE_UNDO, "撤销");
		ACTIONTYPE_MAP.put(ACTIONTYPE_EXPORT, "Excel导出");
	}

	/**
	 * 审核状态
	 */
	public static final Map<String,String> AUDITRESULT_MAP = new HashMap<String, String>();
	static {
		AUDITRESULT_MAP.put(AUDITRESULT_DSH, "待审核");
		AUDITRESULT_MAP.put(AUDITRESULT_SHTG, "审核通过");
		AUDITRESULT_MAP.put(AUDITRESULT_SHBTG, "审核不通过");
	}

	/**
	 * 数据状态
	 */
	public static final Map<String,String> STATE_MAP = new HashMap<String, String>();
	static {
		STATE_MAP.put(STATE_DSX, "待生效");
		STATE_MAP.put(STATE_YX, "有效");
		STATE_MAP.put(STATE_WX, "无效");
	}

	public static final Map<String,Map<String,String>> TABLE_FIELD_MAP = new HashMap<String,Map<String,String>>();//表需要记录更新值历史的字段
	static {
		//MEDICAL_DISEASE_DIAG表字段及字段对应的中文名称
		Map<String,String> fieldMap =  new HashMap<String, String>();
		String fieldStr = "code,name,remark,typeCode,typeName,type1Code,type1Name,type2Code,type2Name,type3Code,type3Name,type4Code,type4Name";
		String fieldNameStr = "疾病编码,疾病名称,描述,父级分类编码,父级分类名称,章组编码,章名称,节编码,节名称,类目编码,类目名称,亚目编码,亚目名称";
		String[] fields = fieldStr.split(",");
		String[] fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalDiseaseDiag().getClass().toString(), fieldMap);

		//MEDICAL_CHINESE_DRUG表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "code,name,ybCode,sourceCode,source,medicalPartCode,medicalPart,pieceName,pieceSizeCode,pieceSize,"
				+ "dosageTypeCode,dosageType,methodCode,method,prescription,placeCode,place,remark,chargeClassCode,chargeClassName";
		fieldNameStr = "中草药编码,中草药名称,国家医保编码,来源编码,来源名称,药用部位编码,药用部位,饮片名,饮片规格编码,饮片规格名称,"
    			+ "剂型编码,剂型名称,炮制方法编码,炮制方法,常用处方名,产地编码,产地名称,描述,收费类别编码,收费类别名称";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalChineseDrug().getClass().toString(), fieldMap);

		//MEDICAL_OTHER_DICT表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "dictCname,dictEname,parentCode,parentValue,code,value,isOrder,remark";
		fieldNameStr = "字典中文名称,字典英文名称,字典项目父级编码,字典项目父级名称,字典项目编码,字典项目名称,排序序号,备注";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalOtherDict().getClass().toString(), fieldMap);

		//MEDICAL_OPERATION表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "code,name,remark,typeCode,typeName,operationLevel,operationLevelCode,bacterialLevel,bacterialLevelName";
		fieldNameStr = "手术及操作编码,手术及操作名称,备注,上级分类编码,上级分类名称,手术级别,手术级别编码,手术无菌程度代码,手术无菌程度名称";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalOperation().getClass().toString(), fieldMap);

		//MEDICAL_OFFICE表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "code,name,remark,typeCode,typeName,type1Code,type1Name,type2Code,type2Name";
		fieldNameStr = "科室编码,科室名称,描述,所属科目编码,所属科目名称,一级科目编码,一级科目名称,二级科目编码,二级科目名称";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalOffice().getClass().toString(), fieldMap);

		//MEDICAL_TREAT_PROJECT表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "typeCode,typeName,code,treatmentOldcode,name,"
    			+ "chargeCode,charge,chargeTypeCode,chargeType,instructions,exceptContent,projectContent,remark";
		fieldNameStr = "上级分类编码,上级分类名称,医疗服务项目编码,旧医疗服务项目编码,医疗服务项目名称,"
    			+ "收费类别编码,收费类别名称,计价单位编码,计价单位名称,说明,除外内容,医疗服务项目内涵,备注";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalTreatProject().getClass().toString(), fieldMap);

		//MEDICAL_DRUG表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "code,name,generalCode,generalName,productEname,productName,approveNumber,dosageLevelCode,dosageLevel,dosageCode,dosage," +
				"packageNum,packageUnitCode,packageUnit,useUnitCode,useUnit,packageUseRate,preparationUnit,preparationUnitCode,ybCode,ybName,"
				+ "specificationLevelCode,specificationLevel,specification,factoryLevelCode,factoryLevel,enterpriseCode,enterprise,"
				+ "type1Code,type1Name,type2Code,type2Name,type3Code,type3Name,type4Code,type4Name,type5Code,type5Name,"
				+ "act1Code,act1Name,act2Code,act2Name,act3Code,act3Name,act4Code,act4Name,medicare1Code,medicare1Name,"
				+ "medicare2Code,medicare2Name,medicare3Code,medicare3Name,medicare4Code,medicare4Name,remark,chargeClassCode,chargeClassName";
		fieldNameStr = "药品编码,药品名称,药品通用名编码,药品通用名,药品英文名称,药品商品名,批准文号,药品剂型级别编码,药品剂型级别名称,剂型代码,剂型名称," +
				"包装,最小包装单位编码,最小包装单位,使用单位编码,使用单位,包装使用转换率,最小制剂单位,最小制剂单位编码,国家医保药品编码,国家医保药品名称,"
				+ "药品规格级别编码,药品规格级别名称,规格,药品厂家级别编码,药品厂家级别名称,生产企业代码,生产企业名称,"
				+ "药理一级分类编码,药理一级分类名称,药理二级分类编码,药理二级分类名称,药理三级分类编码,药理三级分类名称,药理四级分类编码,药理四级分类名称,药理五级分类编码,药理五级分类名称,"
				+ "ATC药品1级代码,ATC药品1级名称,ATC药品2级代码,ATC药品2级名称,ATC药品3级代码,ATC药品3级名称,ATC药品4级代码,ATC药品4级名称,医保1级分类代码,医保1级分类名称,"
				+ "医保2级分类代码,医保2级分类名称,医保3级分类代码,医保3级分类名称,医保4级分类代码,医保4级分类名称,备注,收费类别编码,收费类别名称";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalDrug().getClass().toString(), fieldMap);

		//MEDICAL_ORG表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr  = "code,name,parentId,orgCode,ybDdbh,orgUsedName,orgLevelCode,orgLevel,orgClassCode,orgClass,"
				+ "provinceCode,provinceName,cityCode,cityName,countyCode,countyName,townCode,townName,villageCode,villageName,"
				+ "address,latLon,administrativeLevelCode,administrativeLevel,orgTypeCode,orgType,healthTypeCode,healthType,"
				+ "businessNatureCode,businessNature,ownershipCode,ownership,priceLevelCode,priceLevel,membershipCode,membership,"
				+ "organiserTypeCode,organiserType,competentUnitCode,competentUnit,postcode,telephone,xnhFlagCode,xnhFlagName,ybFlagCode,ybFlagName,"
				+ "gsFlagCode,gsFlagName,zybFlagCode,zybFlagName,approveBedNum,openBedNum,legalName,legalIdType,legalIdNo,legalAddress,legalpersonPhone,insuranceOrg,insuranceOrgname,remark";
		fieldNameStr = "医疗机构编码,医疗机构名称,上级机构ID,组织机构代码,医保定点编号,医疗机构曾用名,医疗机构级别,医疗机构级别名称,医疗机构等级,医疗机构等级名称,"
				+ "地址-省(自治区、直辖市）代码,地址-省(自治区、直辖市）名称,地址-市(地区、州)代码,地址-市(地区、州)名称,地址-县(区)代码,地址-县(区)名称,地址-乡(镇、街道办事处)代码,地址-乡(镇、街道办事处)名称,地址-村(街、路、弄等)代码,地址-村(街、路、弄等)名称,"
				+ "医疗机构地址,经纬度,医疗机构行政级别编码,医疗机构行政级别名称,医疗机构类型编码,医疗机构类型名称,卫生机构类别,卫生机构类别名称,"
				+ "医疗机构经营性质编码,医疗机构经营性质名称,所有制形式编码,所有制形式名称,物价级别编码,物价级别名称,隶属关系编码,隶属关系名称,"
				+ "设置/主办单位类别编码,设置/主办单位类别名称,主管单位编码,主管单位名称,医疗机构邮政编码,医疗机构联系电话,新农合定点医疗机构标志,新农合定点医疗机构标志名称,医保定点医疗机构标志,医保定点医疗机构标志名称,"
				+ "工伤医疗机构标志,工伤医疗机构标志名称,职业病鉴定机构标志,职业病鉴定机构标志名称,医疗机构批准床位数,医疗机构实际开放床位数,法人姓名,法人证件类型,法人身份证件号码,法人联系地址,法人联系电话,所属医保机构编码,所属医保机构名称,备注";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalOrgan().getClass().toString(), fieldMap);

		//MEDICAL_COMPONENT表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr  = "code,name,ename,otherName,toxicityCode,toxicity,referencetypeCode,referencetype,sourceCode,source,remark";
		fieldNameStr = "成分编码,成分名称,成分英文名称,成分异名,毒性程度编码,毒性程度,参考类型编码,参考类型名称,参考来源编码,参考来源名称,备注";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalComponent().getClass().toString(), fieldMap);

		//MEDICAL_PATHOLOGY表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "code,name,descript,parentCode,parentName,type1Code,type1Name,type2Code,type2Name,type3Code,type3Name,type4Code,type4Name";
		fieldNameStr = "YX形态学编码,YX形态学名称,描述,父级分类编码,父级分类名称,一级分类编码,一级分类名称,二级分类编码,二级分类名称,三级分类编码,三级分类名称,四级分类编码,四级分类名称";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalPathology().getClass().toString(), fieldMap);

		//MEDICAL_DRUG_PROPERTY表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "code,name,dosageCode,dosageName,specificaion,presdrugSign,presdrugSignname,"
    			+ "nonrxtypecode,nonrxtypename,poisonousSign,poisonousSignname,narcoticSign,narcoticSignname,"
    			+ "psych1Sign,psych1Signname,psych2Sign,psych2Signname,psych3Sign,psych3Signname,"
				+ "biologicSign,biologicSignname,bloodprodSign,bloodprodSignname,radioSign,radioSignname";
		fieldNameStr = "ATC药品编码,ATC药品名称,剂型代码,剂型名称,规格,处方药标志,处方药标志名称,"
    			+ "非处方药分类,非处方药分类名称,毒性药品标志,毒性药品标志名称,麻醉药品标志,麻醉药品标志名称,"
    			+ "一类精神药品标志,一类精神药品标志名称,二类精神药品标志,二类精神药品标志名称,三类精神药品标志,三类精神药品标志名称,"
    			+ "生物制品标志,生物制品标志名称,血液制品类标志,血液制品类标志名称,放射性药品标志,放射性药品标志名称";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalDrugProperty().getClass().toString(), fieldMap);

		//MEDICAL_EQUIPMENT表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "orderId,productcode,productname,brandname,specificaioncode,specificaion,isPackageCode,isPackageName,productdiscription,equipmentParentcode,equipmentParentname,"
				+ "intendeduse,productArtno,codingsyetemname,unitsnumber,isDirectIdentifyCode,isDirectIdentifyName,isSameSaleIdentifyCode,isSameSaleIdentifyName,directIdentifyCode,"
				+ "equipmentclassification,equipmentClassOldcode,equipmentClassOldname,equipmentClassCode,equipmentClassName,registrantcode,registrantname,registrantenglishname,sfdaNo,productClassCode,productClassName,"
				+ "isDisposableCode,isDisposableName,maxreusetimes,isSterilepackageCode,isSterilepackageName,medicalinsurancecode,storageOperateDescrip,sizedescrip,primarykeycode,publicversionno,nmpaversiontime,commentnote,chargeClassCode,chargeClassName";
		fieldNameStr = "流水号ID,产品编码,产品名称,商品名称,规格型号编码,规格型号,是否为包类/组套类产品编码,是否为包类/组套类产品名称,产品描述,产品父级分类编码,产品父级分类名称,"
    			+ "预期用途,产品货号或编号,医疗器械唯一标识编码体系名称,最小销售单元中使用单元的数量,是否有本体直接标识编码,是否有本体直接标识名称,本体产品标识与最小销售单元产品标识是否一致编码,本体产品标识与最小销售单元产品标识是否一致名称,本体产品标识,"
    			+ "器械类别,原分类编码,原分类名称,分类编码,分类名称,医疗器械注册人/备案人编码,医疗器械注册人/备案人名称,医疗器械注册人/备案人英文名称,注册证编号或者备案凭证编号,产品类别编码,产品类别名称,"
    			+ "是否标记为一次性使用编码,是否标记为一次性使用名称,最大重复使用次数,是否为无菌包装编码,是否为无菌包装名称,医保编码,特殊储存或操作条件,特殊尺寸说明,主键编号,公开的版本号,NMPA版本的发布时间,备注,收费类别编码,收费类别名称";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalEquipment().getClass().toString(), fieldMap);

		//MEDICAL_STD_ATC表字段及字段对应的中文名称
		fieldMap =  new HashMap<String, String>();
		fieldStr = "code,name,"
				+ "act1Code,act1Name,act2Code,act2Name,act3Code,act3Name,act4Code,act4Name,"
				+ "chargeClassCode,chargeClassName,dosageCode,dosage,remark";
		fieldNameStr = "ATC药品编码,ATC药品名称,"
				+ "ATC药品1级代码,ATC药品1级名称,ATC药品2级代码,ATC药品2级名称,ATC药品3级代码,ATC药品3级名称,ATC药品4级代码,ATC药品4级名称,"
				+ "收费类别编码,收费类别名称,剂型代码,剂型名称,备注";
		fields = fieldStr.split(",");
		fieldNams = fieldNameStr.split(",");
		for(int i=0;i<fields.length;i++){
			fieldMap.put(fields[i], fieldNams[i]);
		}
		TABLE_FIELD_MAP.put(new MedicalStdAtc().getClass().toString(), fieldMap);

	}

	/**
	 * 数据状态
	 */
	public static final Map<String,String> EXCEL_TEMPLETE_MAP = new HashMap<String, String>();
	static {
		EXCEL_TEMPLETE_MAP.put("MEDICAL_DISEASE_DIAG"+"."+ACTIONTYPE_ADD, "ICD国际疾病表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_DISEASE_DIAG"+"."+ACTIONTYPE_UPDATE, "ICD国际疾病表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_CHINESE_DRUG"+"."+ACTIONTYPE_ADD, "中草药信息表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_CHINESE_DRUG"+"."+ACTIONTYPE_UPDATE, "中草药信息表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_OTHER_DICT"+"."+ACTIONTYPE_ADD, "其他字典合集_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_OTHER_DICT"+"."+ACTIONTYPE_UPDATE, "其他字典合集_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_OPERATION"+"."+ACTIONTYPE_ADD, "手术信息表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_OPERATION"+"."+ACTIONTYPE_UPDATE, "手术信息表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_OFFICE"+"."+ACTIONTYPE_ADD, "科室信息表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_OFFICE"+"."+ACTIONTYPE_UPDATE, "科室信息表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_TREAT_PROJECT"+"."+ACTIONTYPE_ADD, "医疗服务项目信息表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_TREAT_PROJECT"+"."+ACTIONTYPE_UPDATE, "医疗服务项目信息表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_DRUG"+"."+ACTIONTYPE_ADD, "药品信息表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_DRUG"+"."+ACTIONTYPE_UPDATE, "药品信息表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_ORGAN"+"."+ACTIONTYPE_ADD, "医疗机构信息表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_ORGAN"+"."+ACTIONTYPE_UPDATE, "医疗机构信息表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_COMPONENT"+"."+ACTIONTYPE_ADD, "成分表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_COMPONENT"+"."+ACTIONTYPE_UPDATE, "成分表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_PATHOLOGY"+"."+ACTIONTYPE_ADD, "形态学编码表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_PATHOLOGY"+"."+ACTIONTYPE_UPDATE, "形态学编码表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_DRUG_PROPERTY"+"."+ACTIONTYPE_ADD, "药品属性表_批量新增模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_DRUG_PROPERTY"+"."+ACTIONTYPE_UPDATE, "药品属性表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_EQUIPMENT"+"."+ACTIONTYPE_ADD, "医疗器械信息表_批量修改模板.xls");
		EXCEL_TEMPLETE_MAP.put("MEDICAL_EQUIPMENT"+"."+ACTIONTYPE_UPDATE, "医疗器械信息表_批量修改模板.xls");
	}


	//比对对象修改的元素
	public static Map<String,String> contrastObj(Object oldBean, Object newBean,Field[] fields){
		Class clas_old = oldBean.getClass();
		Class clas_new = newBean.getClass();
		if(!(clas_old.equals(clas_new))){
			return null;
		}
		/*StringBuilder updateBeanStr = new StringBuilder();
		StringBuilder updateContentStr = new StringBuilder();*/
		JSONObject updateBeanJSON = new JSONObject();
		JSONArray updateContentArr = new JSONArray();
		try {
            // 通过反射获取类的类类型及字段属性
			if(fields==null||fields.length==0){
				fields = clas_old.getDeclaredFields();
			}
            Map<String,String> fieldNames = TABLE_FIELD_MAP.get(clas_old.toString());
            for (Field field : fields) {
                // 排除序列化属性
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                Object o1 = field.get(oldBean);
                Object o2 = field.get(newBean);
                if(fieldNames!=null){
                	 String remark = fieldNames.get(field.getName());
                	 if(o1==null){
                		 o1 = "";
                	 }
                	 if(o2==null){
                		 o2 = "";
                	 }
                     if (remark!=null&&((o1==null&&o2!=null)||(o2==null&&o1!=null)||(!o1.toString().equals(o2.toString())))) {
                         //str.append("字段中文名："+remark.name()+",字段名称:" + field.getName() + ",旧值:" + o1 + ",新值:" + o2 + ";");
                     	/*if(StringUtils.isNotBlank(updateBeanStr)){
                     		updateBeanStr.append(",");
                     		updateContentStr.append(",");
                     	}*/
                     	String oldvalue = "";
                     	String newvalue = "";
                     	if(o1!=null){
                     		oldvalue = o1.toString();
                     	}
                     	if(o2!=null){
                     		newvalue = o2.toString();
                     	}
						 updateBeanJSON.put(field.getName(),newvalue);
                     	 JSONObject updateContentJSON = new JSONObject();
						 updateContentJSON.put("name",remark);
						 updateContentJSON.put("field",field.getName());
						 updateContentJSON.put("oldvalue",oldvalue);
						 updateContentJSON.put("newvalue",newvalue);
						 updateContentArr.add(updateContentJSON);
                     	//updateBeanStr.append(field.getName()+":'"+newvalue+"'");
                     	//updateContentStr.append("{\"name\":\""+remark+"\",\"field\":\""+field.getName()+"\",\"oldvalue\":\""+oldvalue+"\",\"newvalue\":\""+newvalue+"\"}");
                     }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		if(StringUtils.isNotBlank(updateBeanJSON.toString())){
			Map<String,String> map = new HashMap<String,String>();
			map.put("updateBeanStr",updateBeanJSON.toString());
			map.put("updateContentStr", updateContentArr.toString());
			/*map.put("updateBeanStr", "{"+updateBeanStr.toString()+"}");
			map.put("updateContentStr", "["+updateContentStr.toString()+"]");*/
			return map;
		}
        return null;
    }

	public static List<HashSet<String>> getIdSetList(List<String> idList, int size) {
		List<HashSet<String>> idSetList = new ArrayList<HashSet<String>>();
		HashSet<String> idSet = new HashSet<String>();
		if(idList.size()<size){
			idSetList.add(new HashSet(idList));
			return idSetList;
		}
		for (String id : idList) {
			if (idSet.size() >= size) {
				idSetList.add(idSet);
				idSet = new HashSet<String>();
			}
			idSet.add(id);
		}
		if (idSet.size() > 0) {
			idSetList.add(idSet);
		}
		return idSetList;
	}

	/**
	 * 操作时间人员条件查询
	 **/
	public static <T> QueryWrapper<T> queryTime(QueryWrapper<T> queryWrapper, HttpServletRequest req)throws Exception{
		/**操作类型未选  有操作人或者操作时间 start**/
		String createStaffName1 = req.getParameter("createStaffName1");
		if(org.apache.commons.lang.StringUtils.isNotEmpty(createStaffName1)) {
			queryWrapper.and(wrapper ->wrapper.like("CREATE_STAFF_NAME", createStaffName1.replace("*", "")).or().like("UPDATE_STAFF_NAME", createStaffName1.replace("*", "")));
		}
		String createTime1 = req.getParameter("createTime1");
		String createTime2 = req.getParameter("createTime2");
		if(org.apache.commons.lang.StringUtils.isNotEmpty(createTime1)&& org.apache.commons.lang.StringUtils.isNotEmpty(createTime2)) {
			Date time1 = DateUtils.parseDate(createTime1, "yyyy-MM-dd");
			Date time2 = DateUtils.parseDate(createTime2+" 24:00:00", "yyyy-MM-dd HH:mm:ss");
			queryWrapper.and(wrapper ->{
				wrapper.ge("CREATE_TIME", time1).le("CREATE_TIME", time2);
				wrapper.or();
				wrapper.ge("UPDATE_TIME", time1).le("UPDATE_TIME", time2);
				return wrapper;
			});
		}else if(org.apache.commons.lang.StringUtils.isNotEmpty(createTime1)) {
			Date time1 = DateUtils.parseDate(createTime1, "yyyy-MM-dd");
			queryWrapper.and(wrapper ->wrapper.ge("CREATE_TIME", time1).or().ge("UPDATE_TIME", time1));
		}else if(org.apache.commons.lang.StringUtils.isNotEmpty(createTime2)) {
			Date time2 = DateUtils.parseDate(createTime2+" 24:00:00", "yyyy-MM-dd HH:mm:ss");
			queryWrapper.and(wrapper ->wrapper.le("CREATE_TIME", time2).or().le("UPDATE_TIME", time2));
		}
		/**操作类型未选  有操作人或者操作时间 end**/
		return queryWrapper;
	}
}
