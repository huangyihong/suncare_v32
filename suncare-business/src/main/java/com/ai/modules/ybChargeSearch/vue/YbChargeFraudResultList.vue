<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-item label="患者名称">
              <a-input placeholder="请输入患者名称" v-model="queryParam.clientname"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="联系电话">
              <a-input placeholder="请输入联系电话" v-model="queryParam.contactorphone"></a-input>
            </a-form-item>
          </a-col>
        <template v-if="toggleSearchStatus">
        <a-col :md="6" :sm="8">
            <a-form-item label="日数量最大手术项目数量">
              <a-input placeholder="请输入日数量最大手术项目数量" v-model="queryParam.dayMaxCntSurgeryCnt"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="日数量最大手术项目金额">
              <a-input placeholder="请输入日数量最大手术项目金额" v-model="queryParam.dayMaxCntSurgeryFee"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="日数量最大手术项目名称">
              <a-input placeholder="请输入日数量最大手术项目名称" v-model="queryParam.dayMaxCntSurgeryName"></a-input>
            </a-form-item>
          </a-col>
          </template>
          <a-col :md="6" :sm="8" >
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" class="m-l-6">重置</a-button>

              <a @click="handleToggleSearch" class="m-l-6">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('欺诈专题结果表')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1" @click="batchDel"><a-icon type="delete"/>删除</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /></a-button>
      </a-dropdown>
    </div>

    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>

      <a-table
        ref="table"
        size="middle"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        v-bind="tableAttrs"
        @change="handleTableChange">

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>

          <a-divider type="vertical" />
          <a-dropdown>
            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>
    </div>
    <!-- table区域-end -->

    <!-- 表单区域 -->
    <ybChargeFraudResult-modal ref="modalForm" @ok="modalFormOk"></ybChargeFraudResult-modal>
  </a-card>
</template>

<script>
  import YbChargeFraudResultModal from './modules/YbChargeFraudResultModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'

  export default {
    name: "YbChargeFraudResultList",
    mixins:[JeecgListMixin],
    components: {
      YbChargeFraudResultModal
    },
    data () {
      return {
        description: '欺诈专题结果表管理页面',
        // 表头
        columns: [
          {
            title: '#',
            dataIndex: '',
            key:'rowIndex',
            width:60,
            align:"center",
            customRender:function (t,r,index) {
              return parseInt(index)+1;
            }
           },
		   {
            title: '患者名称',
            align:"center",
            dataIndex: 'clientname'
           },
		   {
            title: '联系电话',
            align:"center",
            dataIndex: 'contactorphone'
           },
		   {
            title: '日数量最大手术项目数量',
            align:"center",
            dataIndex: 'dayMaxCntSurgeryCnt'
           },
		   {
            title: '日数量最大手术项目金额',
            align:"center",
            dataIndex: 'dayMaxCntSurgeryFee'
           },
		   {
            title: '日数量最大手术项目名称',
            align:"center",
            dataIndex: 'dayMaxCntSurgeryName'
           },
		   {
            title: '日金额最大药品数量',
            align:"center",
            dataIndex: 'dayMaxDrugCnt'
           },
		   {
            title: '日金额最大药品金额',
            align:"center",
            dataIndex: 'dayMaxDrugFee'
           },
		   {
            title: '日金额最大药品名称',
            align:"center",
            dataIndex: 'dayMaxDrugName'
           },
		   {
            title: '日金额最大手术项目数量',
            align:"center",
            dataIndex: 'dayMaxFeeSurgeryCnt'
           },
		   {
            title: '日金额最大手术项目金额',
            align:"center",
            dataIndex: 'dayMaxFeeSurgeryFee'
           },
		   {
            title: '日金额最大手术项目名称',
            align:"center",
            dataIndex: 'dayMaxFeeSurgeryName'
           },
		   {
            title: '日金额最大诊疗项目数量',
            align:"center",
            dataIndex: 'dayMaxTreatCnt'
           },
		   {
            title: '日金额最大诊疗项目金额',
            align:"center",
            dataIndex: 'dayMaxTreatFee'
           },
		   {
            title: '日金额最大诊疗项目名称',
            align:"center",
            dataIndex: 'dayMaxTreatName'
           },
		   {
            title: '医生数量',
            align:"center",
            dataIndex: 'doctorCnt'
           },
		   {
            title: '二级公立医疗机构数量',
            align:"center",
            dataIndex: 'erGongSl'
           },
		   {
            title: '二级民营医疗机构数量',
            align:"center",
            dataIndex: 'erMinSl'
           },
		   {
            title: '年基金金额',
            align:"center",
            dataIndex: 'fundpay'
           },
		   {
            title: '门诊口服药金额',
            align:"center",
            dataIndex: 'fy'
           },
		   {
            title: '医院等级',
            align:"center",
            dataIndex: 'hosplevel'
           },
		   {
            title: '身份证号',
            align:"center",
            dataIndex: 'idNo'
           },
		   {
            title: '是否结伴门诊',
            align:"center",
            dataIndex: 'ifJbmz'
           },
		   {
            title: '是否结伴住院',
            align:"center",
            dataIndex: 'ifJbzy'
           },
		   {
            title: '是否连续三天门诊购药/药品名称',
            align:"center",
            dataIndex: 'ifLxstmzgy'
           },
		   {
            title: '参保类型',
            align:"center",
            dataIndex: 'insurancetype'
           },
		   {
            title: '居民基金金额',
            align:"center",
            dataIndex: 'jmFundpay'
           },
		   {
            title: '本地/异地',
            align:"center",
            dataIndex: 'localTag'
           },
		   {
            title: '年数量最大手术项目数量',
            align:"center",
            dataIndex: 'maxCntSurgeryCnt'
           },
		   {
            title: '年数量最大手术项目金额',
            align:"center",
            dataIndex: 'maxCntSurgeryFee'
           },
		   {
            title: '年数量最大手术项目名称',
            align:"center",
            dataIndex: 'maxCntSurgeryName'
           },
		   {
            title: '年数量最大手术项目金额区域占比',
            align:"center",
            dataIndex: 'maxCntSurgeryZb'
           },
		   {
            title: '住院数量最多疾病数量',
            align:"center",
            dataIndex: 'maxDiagCnt'
           },
		   {
            title: '住院数量最多疾病名称',
            align:"center",
            dataIndex: 'maxDiagName'
           },
		   {
            title: '住院数量最多疾病平均住院金额区域占比',
            align:"center",
            dataIndex: 'maxDiagZb'
           },
		   {
            title: '年金额最大药品数量',
            align:"center",
            dataIndex: 'maxDrugCnt'
           },
		   {
            title: '年金额最大药品金额',
            align:"center",
            dataIndex: 'maxDrugFee'
           },
		   {
            title: '年金额最大药品名称',
            align:"center",
            dataIndex: 'maxDrugName'
           },
		   {
            title: '年金额最大手术项目数量',
            align:"center",
            dataIndex: 'maxFeeSurgeryCnt'
           },
		   {
            title: '年金额最大手术项目金额',
            align:"center",
            dataIndex: 'maxFeeSurgeryFee'
           },
		   {
            title: '年金额最大手术项目名称',
            align:"center",
            dataIndex: 'maxFeeSurgeryName'
           },
		   {
            title: '年金额最大手术项目金额区域占比',
            align:"center",
            dataIndex: 'maxFeeSurgeryZb'
           },
		   {
            title: '年金额最大诊疗项目数量',
            align:"center",
            dataIndex: 'maxTreatCnt'
           },
		   {
            title: '年金额最大诊疗项目金额',
            align:"center",
            dataIndex: 'maxTreatFee'
           },
		   {
            title: '年金额最大诊疗项目名称',
            align:"center",
            dataIndex: 'maxTreatName'
           },
		   {
            title: '理论最小床位数（总床位数/365天）',
            align:"center",
            dataIndex: 'minBedCnt'
           },
		   {
            title: '门诊次均费用',
            align:"center",
            dataIndex: 'mzAvgFee'
           },
		   {
            title: '单个患者门诊年平均数量',
            align:"center",
            dataIndex: 'mzAvgTimes'
           },
		   {
            title: '年门诊次数',
            align:"center",
            dataIndex: 'mzCnt'
           },
		   {
            title: '日最大门诊量人次',
            align:"center",
            dataIndex: 'mzDayMaxCnt'
           },
		   {
            title: '日最大门诊量日期',
            align:"center",
            dataIndex: 'mzDayMaxDate'
           },
		   {
            title: '全部门诊诊断',
            align:"center",
            dataIndex: 'mzDiag'
           },
		   {
            title: '年门诊基金金额',
            align:"center",
            dataIndex: 'mzFundpay'
           },
		   {
            title: '门诊口服药种类数量',
            align:"center",
            dataIndex: 'mzKfCnt'
           },
		   {
            title: '门诊口服药金额',
            align:"center",
            dataIndex: 'mzKfFee'
           },
		   {
            title: '全部门诊口服药名称',
            align:"center",
            dataIndex: 'mzKfItemname'
           },
		   {
            title: '全部门诊机构',
            align:"center",
            dataIndex: 'mzOrgname'
           },
		   {
            title: '年门诊金额',
            align:"center",
            dataIndex: 'mzTotalfee'
           },
		   {
            title: '门诊/住院占比(不排除自费)',
            align:"center",
            dataIndex: 'mzZyRatio'
           },
		   {
            title: '患者名称+出生日期',
            align:"center",
            dataIndex: 'name'
           },
		   {
            title: '年份',
            align:"center",
            dataIndex: 'nian'
           },
		   {
            title: '定点医疗机构数量',
            align:"center",
            dataIndex: 'orgSl'
           },
		   {
            title: '类型(药店/医院)',
            align:"center",
            dataIndex: 'orgcategory'
           },
		   {
            title: '机构名称',
            align:"center",
            dataIndex: 'orgname'
           },
		   {
            title: '专科类型',
            align:"center",
            dataIndex: 'orgtype'
           },
		   {
            title: '年民营机构年基金金额',
            align:"center",
            dataIndex: 'ownFundpayZb'
           },
		   {
            title: '性质',
            align:"center",
            dataIndex: 'owntype'
           },
		   {
            title: '就诊人次',
            align:"center",
            dataIndex: 'renshu'
           },
		   {
            title: '三级公立医疗机构数量',
            align:"center",
            dataIndex: 'sanGongSl'
           },
		   {
            title: '三级民营医疗机构数量',
            align:"center",
            dataIndex: 'sanMinSl'
           },
		   {
            title: '性别',
            align:"center",
            dataIndex: 'sex'
           },
		   {
            title: '年手术量',
            align:"center",
            dataIndex: 'surgeryCn'
           },
		   {
            title: '低标准入院数量',
            align:"center",
            dataIndex: 'tagDbzryCnt'
           },
		   {
            title: '分解住院数量',
            align:"center",
            dataIndex: 'tagFjzyCnt'
           },
		   {
            title: '结伴门诊数量',
            align:"center",
            dataIndex: 'tagJbmzCnt'
           },
		   {
            title: '结伴住院数量',
            align:"center",
            dataIndex: 'tagJbzyCnt'
           },
		   {
            title: '节假日住院异常程度',
            align:"center",
            dataIndex: 'tagJjrycCnt'
           },
		   {
            title: '门诊就诊雷同数量',
            align:"center",
            dataIndex: 'tagMzltCnt'
           },
		   {
            title: '住院就诊雷同数量',
            align:"center",
            dataIndex: 'tagZyltCnt'
           },
		   {
            title: '关联的任务表主键',
            align:"center",
            dataIndex: 'taskId'
           },
		   {
            title: '年医疗费用',
            align:"center",
            dataIndex: 'totalfee'
           },
		   {
            title: '未评级公立医疗机构数量',
            align:"center",
            dataIndex: 'weiGongSl'
           },
		   {
            title: '未评级民营医疗机构数量',
            align:"center",
            dataIndex: 'weiMinSl'
           },
		   {
            title: '地址',
            align:"center",
            dataIndex: 'workplacename'
           },
		   {
            title: '未评级未区分民营公立机构数量',
            align:"center",
            dataIndex: 'wuSl'
           },
		   {
            title: '异地基金金额',
            align:"center",
            dataIndex: 'ydFundpay'
           },
		   {
            title: '异地门诊基金金额',
            align:"center",
            dataIndex: 'ydMzRenci'
           },
		   {
            title: '药店数量',
            align:"center",
            dataIndex: 'ydSl'
           },
		   {
            title: '异地住院基金金额',
            align:"center",
            dataIndex: 'ydZyRenci'
           },
		   {
            title: '年',
            align:"center",
            dataIndex: 'year'
           },
		   {
            title: '年龄',
            align:"center",
            dataIndex: 'yearage'
           },
		   {
            title: '一级公立医疗机构数量',
            align:"center",
            dataIndex: 'yiGongSl'
           },
		   {
            title: '一级民营医疗机构数量',
            align:"center",
            dataIndex: 'yiMinSl'
           },
		   {
            title: '年',
            align:"center",
            dataIndex: 'yyear'
           },
		   {
            title: '职工基金金额',
            align:"center",
            dataIndex: 'zgFundpay'
           },
		   {
            title: '平均住院日',
            align:"center",
            dataIndex: 'zyAvgDays'
           },
		   {
            title: '住院均次金额',
            align:"center",
            dataIndex: 'zyAvgFee'
           },
		   {
            title: '年住院次数',
            align:"center",
            dataIndex: 'zyCnt'
           },
		   {
            title: '日在院平均人数',
            align:"center",
            dataIndex: 'zyDayAvgInClient'
           },
		   {
            title: '日最大住院量人次',
            align:"center",
            dataIndex: 'zyDayMaxCnt'
           },
		   {
            title: '日最大住院量日期',
            align:"center",
            dataIndex: 'zyDayMaxDate'
           },
		   {
            title: '日最大在院量人次',
            align:"center",
            dataIndex: 'zyDayMaxInCnt'
           },
		   {
            title: '日最大在院量日期',
            align:"center",
            dataIndex: 'zyDayMaxInDate'
           },
		   {
            title: '全部诊断',
            align:"center",
            dataIndex: 'zyDiag'
           },
		   {
            title: '年住院基金金额',
            align:"center",
            dataIndex: 'zyFundpay'
           },
		   {
            title: '全部手术',
            align:"center",
            dataIndex: 'zySurgery'
           },
		   {
            title: '年住院金额',
            align:"center",
            dataIndex: 'zyTotalfee'
           },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            scopedSlots: { customRender: 'action' },
          }
        ],
		url: {
          list: "/ybChargeSearch/ybChargeFraudResult/list",
          delete: "/ybChargeSearch/ybChargeFraudResult/delete",
          deleteBatch: "/ybChargeSearch/ybChargeFraudResult/deleteBatch",
          exportXlsUrl: "ybChargeSearch/ybChargeFraudResult/exportXls",
          importExcelUrl: "ybChargeSearch/ybChargeFraudResult/importExcel",
       },
    }
  },
  computed: {
    importExcelUrl: function(){
      return `${this.$config['domianURL']}/${this.url.importExcelUrl}`;
    }
  },
    methods: {

    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>
