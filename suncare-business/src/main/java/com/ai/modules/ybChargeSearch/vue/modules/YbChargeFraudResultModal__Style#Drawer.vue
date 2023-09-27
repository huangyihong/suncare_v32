<template>
  <a-drawer
      :title="title"
      :width="800"
      placement="right"
      :closable="false"
      @close="close"
      :visible="visible"
  >

    <a-spin :spinning="confirmLoading">
      <a-form :form="form">

        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="患者名称">
          <a-input placeholder="请输入患者名称" v-decorator="['clientname', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="联系电话">
          <a-input placeholder="请输入联系电话" v-decorator="['contactorphone', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日数量最大手术项目数量">
          <a-input-number v-decorator="[ 'dayMaxCntSurgeryCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日数量最大手术项目金额">
          <a-input-number v-decorator="[ 'dayMaxCntSurgeryFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日数量最大手术项目名称">
          <a-input placeholder="请输入日数量最大手术项目名称" v-decorator="['dayMaxCntSurgeryName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大药品数量">
          <a-input-number v-decorator="[ 'dayMaxDrugCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大药品金额">
          <a-input-number v-decorator="[ 'dayMaxDrugFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大药品名称">
          <a-input placeholder="请输入日金额最大药品名称" v-decorator="['dayMaxDrugName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大手术项目数量">
          <a-input-number v-decorator="[ 'dayMaxFeeSurgeryCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大手术项目金额">
          <a-input-number v-decorator="[ 'dayMaxFeeSurgeryFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大手术项目名称">
          <a-input placeholder="请输入日金额最大手术项目名称" v-decorator="['dayMaxFeeSurgeryName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大诊疗项目数量">
          <a-input-number v-decorator="[ 'dayMaxTreatCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大诊疗项目金额">
          <a-input-number v-decorator="[ 'dayMaxTreatFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日金额最大诊疗项目名称">
          <a-input placeholder="请输入日金额最大诊疗项目名称" v-decorator="['dayMaxTreatName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="医生数量">
          <a-input-number v-decorator="[ 'doctorCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="二级公立医疗机构数量">
          <a-input-number v-decorator="[ 'erGongSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="二级民营医疗机构数量">
          <a-input-number v-decorator="[ 'erMinSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年基金金额">
          <a-input-number v-decorator="[ 'fundpay', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="门诊口服药金额">
          <a-input-number v-decorator="[ 'fy', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="医院等级">
          <a-input placeholder="请输入医院等级" v-decorator="['hosplevel', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="身份证号">
          <a-input placeholder="请输入身份证号" v-decorator="['idNo', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="是否结伴门诊">
          <a-input placeholder="请输入是否结伴门诊" v-decorator="['ifJbmz', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="是否结伴住院">
          <a-input placeholder="请输入是否结伴住院" v-decorator="['ifJbzy', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="是否连续三天门诊购药/药品名称">
          <a-input placeholder="请输入是否连续三天门诊购药/药品名称" v-decorator="['ifLxstmzgy', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="参保类型">
          <a-input placeholder="请输入参保类型" v-decorator="['insurancetype', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="居民基金金额">
          <a-input-number v-decorator="[ 'jmFundpay', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="本地/异地">
          <a-input placeholder="请输入本地/异地" v-decorator="['localTag', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年数量最大手术项目数量">
          <a-input-number v-decorator="[ 'maxCntSurgeryCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年数量最大手术项目金额">
          <a-input-number v-decorator="[ 'maxCntSurgeryFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年数量最大手术项目名称">
          <a-input placeholder="请输入年数量最大手术项目名称" v-decorator="['maxCntSurgeryName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年数量最大手术项目金额区域占比">
          <a-input-number v-decorator="[ 'maxCntSurgeryZb', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="住院数量最多疾病数量">
          <a-input-number v-decorator="[ 'maxDiagCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="住院数量最多疾病名称">
          <a-input placeholder="请输入住院数量最多疾病名称" v-decorator="['maxDiagName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="住院数量最多疾病平均住院金额区域占比">
          <a-input-number v-decorator="[ 'maxDiagZb', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大药品数量">
          <a-input-number v-decorator="[ 'maxDrugCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大药品金额">
          <a-input-number v-decorator="[ 'maxDrugFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大药品名称">
          <a-input placeholder="请输入年金额最大药品名称" v-decorator="['maxDrugName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大手术项目数量">
          <a-input-number v-decorator="[ 'maxFeeSurgeryCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大手术项目金额">
          <a-input-number v-decorator="[ 'maxFeeSurgeryFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大手术项目名称">
          <a-input placeholder="请输入年金额最大手术项目名称" v-decorator="['maxFeeSurgeryName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大手术项目金额区域占比">
          <a-input-number v-decorator="[ 'maxFeeSurgeryZb', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大诊疗项目数量">
          <a-input-number v-decorator="[ 'maxTreatCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大诊疗项目金额">
          <a-input-number v-decorator="[ 'maxTreatFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年金额最大诊疗项目名称">
          <a-input placeholder="请输入年金额最大诊疗项目名称" v-decorator="['maxTreatName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="理论最小床位数（总床位数/365天）">
          <a-input-number v-decorator="[ 'minBedCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="门诊次均费用">
          <a-input-number v-decorator="[ 'mzAvgFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="单个患者门诊年平均数量">
          <a-input-number v-decorator="[ 'mzAvgTimes', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年门诊次数">
          <a-input-number v-decorator="[ 'mzCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日最大门诊量人次">
          <a-input-number v-decorator="[ 'mzDayMaxCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日最大门诊量日期">
          <a-input placeholder="请输入日最大门诊量日期" v-decorator="['mzDayMaxDate', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="全部门诊诊断">
          <a-input placeholder="请输入全部门诊诊断" v-decorator="['mzDiag', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年门诊基金金额">
          <a-input-number v-decorator="[ 'mzFundpay', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="门诊口服药种类数量">
          <a-input-number v-decorator="[ 'mzKfCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="门诊口服药金额">
          <a-input-number v-decorator="[ 'mzKfFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="全部门诊口服药名称">
          <a-input placeholder="请输入全部门诊口服药名称" v-decorator="['mzKfItemname', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="全部门诊机构">
          <a-input placeholder="请输入全部门诊机构" v-decorator="['mzOrgname', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年门诊金额">
          <a-input-number v-decorator="[ 'mzTotalfee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="门诊/住院占比(不排除自费)">
          <a-input-number v-decorator="[ 'mzZyRatio', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="患者名称+出生日期">
          <a-input placeholder="请输入患者名称+出生日期" v-decorator="['name', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年份">
          <a-input placeholder="请输入年份" v-decorator="['nian', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="定点医疗机构数量">
          <a-input-number v-decorator="[ 'orgSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="类型(药店/医院)">
          <a-input placeholder="请输入类型(药店/医院)" v-decorator="['orgcategory', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="机构名称">
          <a-input placeholder="请输入机构名称" v-decorator="['orgname', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="专科类型">
          <a-input placeholder="请输入专科类型" v-decorator="['orgtype', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年民营机构年基金金额">
          <a-input-number v-decorator="[ 'ownFundpayZb', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="性质">
          <a-input placeholder="请输入性质" v-decorator="['owntype', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="就诊人次">
          <a-input-number v-decorator="[ 'renshu', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="三级公立医疗机构数量">
          <a-input-number v-decorator="[ 'sanGongSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="三级民营医疗机构数量">
          <a-input-number v-decorator="[ 'sanMinSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="性别">
          <a-input placeholder="请输入性别" v-decorator="['sex', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年手术量">
          <a-input-number v-decorator="[ 'surgeryCn', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="低标准入院数量">
          <a-input-number v-decorator="[ 'tagDbzryCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="分解住院数量">
          <a-input-number v-decorator="[ 'tagFjzyCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="结伴门诊数量">
          <a-input-number v-decorator="[ 'tagJbmzCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="结伴住院数量">
          <a-input-number v-decorator="[ 'tagJbzyCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="节假日住院异常程度">
          <a-input-number v-decorator="[ 'tagJjrycCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="门诊就诊雷同数量">
          <a-input-number v-decorator="[ 'tagMzltCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="住院就诊雷同数量">
          <a-input-number v-decorator="[ 'tagZyltCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="关联的任务表主键">
          <a-input placeholder="请输入关联的任务表主键" v-decorator="['taskId', validatorRules.taskId ]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年医疗费用">
          <a-input-number v-decorator="[ 'totalfee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="未评级公立医疗机构数量">
          <a-input-number v-decorator="[ 'weiGongSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="未评级民营医疗机构数量">
          <a-input-number v-decorator="[ 'weiMinSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="地址">
          <a-input placeholder="请输入地址" v-decorator="['workplacename', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="未评级未区分民营公立机构数量">
          <a-input-number v-decorator="[ 'wuSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="异地基金金额">
          <a-input-number v-decorator="[ 'ydFundpay', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="异地门诊基金金额">
          <a-input-number v-decorator="[ 'ydMzRenci', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="药店数量">
          <a-input-number v-decorator="[ 'ydSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="异地住院基金金额">
          <a-input-number v-decorator="[ 'ydZyRenci', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年">
          <a-input placeholder="请输入年" v-decorator="['year', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年龄">
          <a-input placeholder="请输入年龄" v-decorator="['yearage', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="一级公立医疗机构数量">
          <a-input-number v-decorator="[ 'yiGongSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="一级民营医疗机构数量">
          <a-input-number v-decorator="[ 'yiMinSl', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年">
          <a-input placeholder="请输入年" v-decorator="['yyear', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="职工基金金额">
          <a-input-number v-decorator="[ 'zgFundpay', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="平均住院日">
          <a-input-number v-decorator="[ 'zyAvgDays', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="住院均次金额">
          <a-input-number v-decorator="[ 'zyAvgFee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年住院次数">
          <a-input-number v-decorator="[ 'zyCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日在院平均人数">
          <a-input-number v-decorator="[ 'zyDayAvgInClient', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日最大住院量人次">
          <a-input-number v-decorator="[ 'zyDayMaxCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日最大住院量日期">
          <a-input placeholder="请输入日最大住院量日期" v-decorator="['zyDayMaxDate', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日最大在院量人次">
          <a-input-number v-decorator="[ 'zyDayMaxInCnt', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="日最大在院量日期">
          <a-input placeholder="请输入日最大在院量日期" v-decorator="['zyDayMaxInDate', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="全部诊断">
          <a-input placeholder="请输入全部诊断" v-decorator="['zyDiag', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年住院基金金额">
          <a-input-number v-decorator="[ 'zyFundpay', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="全部手术">
          <a-input placeholder="请输入全部手术" v-decorator="['zySurgery', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="年住院金额">
          <a-input-number v-decorator="[ 'zyTotalfee', {}]" />
        </a-form-item>

      </a-form>
    </a-spin>
    <div class="drawer-bottom">
      <a-button @click="handleCancel">关闭</a-button>
      <a-button type="primary" @click="handleOk">确定</a-button>
    </div>
  </a-drawer>
</template>

<script>
  import { httpAction } from '@/api/manage'
  import pick from 'lodash/pick'
  import moment from "moment"

  export default {
    name: "YbChargeFraudResultModal",
    data () {
      return {
        title:"操作",
        visible: false,
        model: {},
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },

        confirmLoading: false,
        form: this.$form.createForm(this),
        validatorRules:{
        taskId:{rules: [{ required: true, message: '请输入关联的任务表主键!' }]},
        },
        url: {
          add: "/ybChargeSearch/ybChargeFraudResult/add",
          edit: "/ybChargeSearch/ybChargeFraudResult/edit",
        },
      }
    },
    created () {
    },
    methods: {
      add () {
        this.edit({});
      },
      edit (record) {
        this.form.resetFields();
        this.model = Object.assign({}, record);
        this.visible = true;
        this.$nextTick(() => {
          this.form.setFieldsValue(pick(this.model,'clientname','contactorphone','dayMaxCntSurgeryCnt','dayMaxCntSurgeryFee','dayMaxCntSurgeryName','dayMaxDrugCnt','dayMaxDrugFee','dayMaxDrugName','dayMaxFeeSurgeryCnt','dayMaxFeeSurgeryFee','dayMaxFeeSurgeryName','dayMaxTreatCnt','dayMaxTreatFee','dayMaxTreatName','doctorCnt','erGongSl','erMinSl','fundpay','fy','hosplevel','idNo','ifJbmz','ifJbzy','ifLxstmzgy','insurancetype','jmFundpay','localTag','maxCntSurgeryCnt','maxCntSurgeryFee','maxCntSurgeryName','maxCntSurgeryZb','maxDiagCnt','maxDiagName','maxDiagZb','maxDrugCnt','maxDrugFee','maxDrugName','maxFeeSurgeryCnt','maxFeeSurgeryFee','maxFeeSurgeryName','maxFeeSurgeryZb','maxTreatCnt','maxTreatFee','maxTreatName','minBedCnt','mzAvgFee','mzAvgTimes','mzCnt','mzDayMaxCnt','mzDayMaxDate','mzDiag','mzFundpay','mzKfCnt','mzKfFee','mzKfItemname','mzOrgname','mzTotalfee','mzZyRatio','name','nian','orgSl','orgcategory','orgname','orgtype','ownFundpayZb','owntype','renshu','sanGongSl','sanMinSl','sex','surgeryCn','tagDbzryCnt','tagFjzyCnt','tagJbmzCnt','tagJbzyCnt','tagJjrycCnt','tagMzltCnt','tagZyltCnt','taskId','totalfee','weiGongSl','weiMinSl','workplacename','wuSl','ydFundpay','ydMzRenci','ydSl','ydZyRenci','year','yearage','yiGongSl','yiMinSl','yyear','zgFundpay','zyAvgDays','zyAvgFee','zyCnt','zyDayAvgInClient','zyDayMaxCnt','zyDayMaxDate','zyDayMaxInCnt','zyDayMaxInDate','zyDiag','zyFundpay','zySurgery','zyTotalfee'))
		  //时间格式化
        });

      },
      close () {
        this.$emit('close');
        this.visible = false;
      },
      handleOk () {
        const that = this;
        // 触发表单验证
        this.form.validateFields((err, values) => {
          if (!err) {
            that.confirmLoading = true;
            let httpurl = '';
            let method = '';
            if(!this.model.id){
              httpurl+=this.url.add;
              method = 'post';
            }else{
              httpurl+=this.url.edit;
               method = 'put';
            }
            let formData = Object.assign(this.model, values);
            //时间格式化

            console.log(formData)
            httpAction(httpurl,formData,method).then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
              }else{
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
              that.close();
            })



          }
        })
      },
      handleCancel () {
        this.close()
      },


    }
  }
</script>

<style lang="less" scoped>
</style>
