<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-item label="目录编码">
              <a-input placeholder="请输入目录编码" v-model="queryParam.catalogCode"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="目录类型{DRG_V、ADRG_V}">
              <a-input placeholder="请输入目录类型{DRG_V、ADRG_V}" v-model="queryParam.catalogType"></a-input>
            </a-form-item>
          </a-col>
        <template v-if="toggleSearchStatus">
        <a-col :md="6" :sm="8">
            <a-form-item label="组内关系">
              <a-input placeholder="请输入组内关系" v-model="queryParam.compareLogic"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="比较符">
              <a-input placeholder="请输入比较符" v-model="queryParam.compareType"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="比较符2">
              <a-input placeholder="请输入比较符2" v-model="queryParam.compareType2"></a-input>
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
      <a-button type="primary" icon="download" @click="handleExportXls('drg规则限定条件表')">导出</a-button>
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
    <drgRuleLimites-modal ref="modalForm" @ok="modalFormOk"></drgRuleLimites-modal>
  </a-card>
</template>

<script>
  import DrgRuleLimitesModal from './modules/DrgRuleLimitesModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'

  export default {
    name: "DrgRuleLimitesList",
    mixins:[JeecgListMixin],
    components: {
      DrgRuleLimitesModal
    },
    data () {
      return {
        description: 'drg规则限定条件表管理页面',
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
            title: '目录编码',
            align:"center",
            dataIndex: 'catalogCode'
           },
		   {
            title: '目录类型{DRG_V、ADRG_V}',
            align:"center",
            dataIndex: 'catalogType'
           },
		   {
            title: '组内关系',
            align:"center",
            dataIndex: 'compareLogic'
           },
		   {
            title: '比较符',
            align:"center",
            dataIndex: 'compareType'
           },
		   {
            title: '比较符2',
            align:"center",
            dataIndex: 'compareType2'
           },
		   {
            title: '值',
            align:"center",
            dataIndex: 'compareValue'
           },
		   {
            title: '值2',
            align:"center",
            dataIndex: 'compareValue2'
           },
		   {
            title: 'createdBy',
            align:"center",
            dataIndex: 'createdBy'
           },
		   {
            title: 'createdByName',
            align:"center",
            dataIndex: 'createdByName'
           },
		   {
            title: 'createdTime',
            align:"center",
            dataIndex: 'createdTime'
           },
		   {
            title: '组与组关系',
            align:"center",
            dataIndex: 'logic'
           },
		   {
            title: '序号',
            align:"center",
            dataIndex: 'seq'
           },
		   {
            title: 'updatedBy',
            align:"center",
            dataIndex: 'updatedBy'
           },
		   {
            title: 'updatedByName',
            align:"center",
            dataIndex: 'updatedByName'
           },
		   {
            title: 'updatedTime',
            align:"center",
            dataIndex: 'updatedTime'
           },
		   {
            title: '版本号',
            align:"center",
            dataIndex: 'versionCode'
           },
		   {
            title: '条件类型{age:年龄,dayAge:天龄,zyDays:住院天数,weight:出生体重,leavetype:离院方式}',
            align:"center",
            dataIndex: 'whereType'
           },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            scopedSlots: { customRender: 'action' },
          }
        ],
		url: {
          list: "/drg/drgRuleLimites/list",
          delete: "/drg/drgRuleLimites/delete",
          deleteBatch: "/drg/drgRuleLimites/deleteBatch",
          exportXlsUrl: "drg/drgRuleLimites/exportXls",
          importExcelUrl: "drg/drgRuleLimites/importExcel",
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
