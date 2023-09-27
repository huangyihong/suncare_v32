<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-item label="关联的任务表主键">
              <a-input placeholder="请输入关联的任务表主键" v-model="queryParam.taskId"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="机构ID">
              <a-input placeholder="请输入机构ID" v-model="queryParam.orgid"></a-input>
            </a-form-item>
          </a-col>
        <template v-if="toggleSearchStatus">
        <a-col :md="6" :sm="8">
            <a-form-item label="原始医院名称">
              <a-input placeholder="请输入原始医院名称" v-model="queryParam.orgnameSrc"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="原始就诊号">
              <a-input placeholder="请输入原始就诊号" v-model="queryParam.visitidSrc"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="原始就诊类型">
              <a-input placeholder="请输入原始就诊类型" v-model="queryParam.visittypeSrc"></a-input>
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
      <a-button type="primary" icon="download" @click="handleExportXls('可疑就诊标签汇总表')">导出</a-button>
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
    <ybChargeSuspiciousResult-modal ref="modalForm" @ok="modalFormOk"></ybChargeSuspiciousResult-modal>
  </a-card>
</template>

<script>
  import YbChargeSuspiciousResultModal from './modules/YbChargeSuspiciousResultModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'

  export default {
    name: "YbChargeSuspiciousResultList",
    mixins:[JeecgListMixin],
    components: {
      YbChargeSuspiciousResultModal
    },
    data () {
      return {
        description: '可疑就诊标签汇总表管理页面',
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
            title: '关联的任务表主键',
            align:"center",
            dataIndex: 'taskId'
           },
		   {
            title: '机构ID',
            align:"center",
            dataIndex: 'orgid'
           },
		   {
            title: '原始医院名称',
            align:"center",
            dataIndex: 'orgnameSrc'
           },
		   {
            title: '原始就诊号',
            align:"center",
            dataIndex: 'visitidSrc'
           },
		   {
            title: '原始就诊类型',
            align:"center",
            dataIndex: 'visittypeSrc'
           },
		   {
            title: '原始科室名称',
            align:"center",
            dataIndex: 'deptnameSrc'
           },
		   {
            title: '医生姓名',
            align:"center",
            dataIndex: 'doctorname'
           },
		   {
            title: '患者姓名',
            align:"center",
            dataIndex: 'clientname'
           },
		   {
            title: '患者标签',
            align:"center",
            dataIndex: 'patientTagName'
           },
		   {
            title: '原始年龄',
            align:"center",
            dataIndex: 'yearageSrc'
           },
		   {
            title: '原始性别',
            align:"center",
            dataIndex: 'sexSrc'
           },
		   {
            title: '就诊日期',
            align:"center",
            dataIndex: 'visitdate'
           },
		   {
            title: '出院日期',
            align:"center",
            dataIndex: 'leavedate'
           },
		   {
            title: '住院天数',
            align:"center",
            dataIndex: 'zyDays'
           },
		   {
            title: '主诊断原始名称',
            align:"center",
            dataIndex: 'diseasenamePrimarySrc'
           },
		   {
            title: '其他诊断原始名称',
            align:"center",
            dataIndex: 'diseasenameOtherSrc'
           },
		   {
            title: '总费用',
            align:"center",
            dataIndex: 'totalfee'
           },
		   {
            title: '基金支付金额',
            align:"center",
            dataIndex: 'fundpay'
           },
		   {
            title: '标签数量',
            align:"center",
            dataIndex: 'tagCount'
           },
		   {
            title: '标签列表',
            align:"center",
            dataIndex: 'tagName'
           },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            scopedSlots: { customRender: 'action' },
          }
        ],
		url: {
          list: "/ybChargeSearch/ybChargeSuspiciousResult/list",
          delete: "/ybChargeSearch/ybChargeSuspiciousResult/delete",
          deleteBatch: "/ybChargeSearch/ybChargeSuspiciousResult/deleteBatch",
          exportXlsUrl: "ybChargeSearch/ybChargeSuspiciousResult/exportXls",
          importExcelUrl: "ybChargeSearch/ybChargeSuspiciousResult/importExcel",
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
