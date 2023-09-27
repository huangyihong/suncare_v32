package com.ai.modules.review.vo;

import lombok.Data;

/**
 * @Auther: zhangpeng
 * @Date: 2020/6/6 15
 * @Description:
 */
@Data
public class DwbOrderVo {
    private String id;
    //就诊id
    private String visitid;
    //就诊医疗机构编码
    private String orgid;
    //就诊医疗机构名称
    private String orgname;
    //就诊科室编码
    private String deptid;
    //就诊科室名称
    private String deptname;
    //就诊类型名称
    private String visittype;
    //就诊类型代码
    private String visittypeId;
    //处方号
    private String prescriptno;
    //项目编码
    private String itemcode;
    //项目名称
    private String itemname;
    //项目编码_src
    private String itemcodeSrc;
    //项目名称_src
    private String itemnameSrc;
    //药物剂型代码
    private String dosageform;
    //剂型名称
    private String dosagename;
    //规格
    private String specificaion;
    //医嘱类型
    private String ordertype;
    //医嘱编号
    private String orderid;
    //子医嘱号
    private String subOrderid;
    //组号
    private String ordergroupno;
    //项目类别代码
    private String itemclassId;
    //项目类别名称
    private String itemclass;
    //收费类别编码
    private String chargeclassId;
    //收费类别名称
    private String chargeclass;
    //基药标志
    private String basicdrugSign;
    //抗生素标志
    private String antibioticSign;
    //中药使用类别代码
    private String chimedicineUsetype;
    //药物类型
    private String drugtype;
    //单次剂量
    private String dosage;
    //剂量单位
    private String dosageunit;
    //给药途径
    private String usetype;
    //中药煎煮法
    private String chimedicineDecoct;
    //频次
    private String frequency;
    //数量
    private Double amount;
    //数量单位
    private String amountunit;
    //用药天数
    private String days;
    //项目单价
    private Double itemprice;
    //计价单位
    private String chargeunit;
    //金额
    private Double fee;
    //开医嘱时间
    private String ordertime;
    //开医嘱医师姓名
    private String doctorname;
    //开医嘱医师代码
    private String doctorid;
    //开医嘱科室编码
    private String orderDeptid;
    //停医嘱时间
    private String orderendDate;
    //停医嘱医师代码
    private String orderendDoctorid;
    //停医嘱医师名称
    private String orderendDoctor;
    //出院带药标志代码
    private String dischargeDrugSign;
    //出院带药标志名称
    private String dischargeDrugSignname;
    //核对护士
    private String checknurse;
    //核对时间
    private String checktime;
    //执行护士
    private String implementnurse;
    //医嘱执行时间
    private String implementtime;
    //医嘱执行时间类别
    private String implementtype;
    //执行科室编码
    private String impleDeptid;
    //执行科室名称
    private String impleDeptname;
    //计费科室编码
    private String chargeDeptid;
    //计费科室名称
    private String chargeDeptname;
    //备注
    private String commentnote;
    //状态
    private String status;
    //创建者
    private String creator;
    //创建日期
    private String createdate;
    //修改者
    private String modifier;
    //修改日期
    private String modifydate;
    //项目地名称
    private String projectarea;
    //项目地编码，省级项目就是省级编码，区县级项目就是区县编码
    private String projectareaid;
    //数据来源机构编码
    private String dataResouceId;
    //数据来源机构名称
    private String dataResouce;
    //etl数据来源代码
    private String etlSource;
    //etl数据来源名称
    private String etlSourceName;
    //etl处理时间
    private String etlTime;
}
