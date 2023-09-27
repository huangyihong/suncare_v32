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
          label="关联的任务表主键">
          <a-input placeholder="请输入关联的任务表主键" v-decorator="['taskId', validatorRules.taskId ]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="机构ID">
          <a-input placeholder="请输入机构ID" v-decorator="['orgid', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="原始医院名称">
          <a-input placeholder="请输入原始医院名称" v-decorator="['orgnameSrc', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="原始就诊号">
          <a-input placeholder="请输入原始就诊号" v-decorator="['visitidSrc', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="原始就诊类型">
          <a-input placeholder="请输入原始就诊类型" v-decorator="['visittypeSrc', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="原始科室名称">
          <a-input placeholder="请输入原始科室名称" v-decorator="['deptnameSrc', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="医生姓名">
          <a-input placeholder="请输入医生姓名" v-decorator="['doctorname', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="患者姓名">
          <a-input placeholder="请输入患者姓名" v-decorator="['clientname', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="患者标签">
          <a-input placeholder="请输入患者标签" v-decorator="['patientTagName', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="原始年龄">
          <a-input placeholder="请输入原始年龄" v-decorator="['yearageSrc', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="原始性别">
          <a-input placeholder="请输入原始性别" v-decorator="['sexSrc', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="就诊日期">
          <a-input placeholder="请输入就诊日期" v-decorator="['visitdate', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="出院日期">
          <a-input placeholder="请输入出院日期" v-decorator="['leavedate', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="住院天数">
          <a-input placeholder="请输入住院天数" v-decorator="['zyDays', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="主诊断原始名称">
          <a-input placeholder="请输入主诊断原始名称" v-decorator="['diseasenamePrimarySrc', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="其他诊断原始名称">
          <a-input placeholder="请输入其他诊断原始名称" v-decorator="['diseasenameOtherSrc', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="总费用">
          <a-input-number v-decorator="[ 'totalfee', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="基金支付金额">
          <a-input-number v-decorator="[ 'fundpay', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="标签数量">
          <a-input-number v-decorator="[ 'tagCount', {}]" />
        </a-form-item>
        <a-form-item
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          label="标签列表">
          <a-input placeholder="请输入标签列表" v-decorator="['tagName', {}]" />
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
    name: "YbChargeSuspiciousResultModal",
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
          add: "/ybChargeSearch/ybChargeSuspiciousResult/add",
          edit: "/ybChargeSearch/ybChargeSuspiciousResult/edit",
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
          this.form.setFieldsValue(pick(this.model,'taskId','orgid','orgnameSrc','visitidSrc','visittypeSrc','deptnameSrc','doctorname','clientname','patientTagName','yearageSrc','sexSrc','visitdate','leavedate','zyDays','diseasenamePrimarySrc','diseasenameOtherSrc','totalfee','fundpay','tagCount','tagName'))
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
