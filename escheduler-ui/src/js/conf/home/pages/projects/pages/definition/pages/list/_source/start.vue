<template>
  <div class="start-process-model">
    <div class="title-box">
      <span>{{$t('启动前请先设置参数')}}</span>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('失败策略')}}
      </div>
      <div class="cont">
        <x-radio-group v-model="failureStrategy" style="margin-top: 7px;">
          <x-radio :label="'CONTINUE'">{{$t('继续')}}</x-radio>
          <x-radio :label="'END'">{{$t('结束')}}</x-radio>
        </x-radio-group>
      </div>
    </div>
    <div class="clearfix list" v-if="sourceType === 'contextmenu'">
      <div class="text">
        {{$t('节点执行')}}
      </div>
      <div class="cont">
        <x-radio-group v-model="taskDependType">
          <x-radio :label="'TASK_POST'">{{$t('向后执行')}}</x-radio>
          <x-radio :label="'TASK_PRE'">{{$t('向前执行')}}</x-radio>
          <x-radio :label="'TASK_ONLY'">{{$t('仅执行当前节点')}}</x-radio>
        </x-radio-group>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('通知策略')}}
      </div>
      <div class="cont">
        <x-select style="width: 200px;" v-model="warningType">
          <x-option
                  v-for="city in warningTypeList"
                  :key="city.id"
                  :value="city"
                  :label="city.code">
          </x-option>
        </x-select>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('流程优先级')}}
      </div>
      <div class="cont">
        <m-priority v-model="processInstancePriority"></m-priority>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('通知组')}}
      </div>
      <div class="cont">
        <x-select
                style="width: 200px;"
                v-model="warningGroupId"
                :disabled="!notifyGroupList.length">
          <x-input slot="trigger" slot-scope="{ selectedModel }" readonly :placeholder="$t('请选择通知组')" :value="selectedModel ? selectedModel.label : ''" style="width: 200px;" @on-click-icon.stop="warningGroupId = {}">
            <i slot="suffix" class="fa fa-times-circle" style="font-size: 15px;cursor: pointer;" v-show="warningGroupId.id"></i>
            <i slot="suffix" class="ans-icon-arrow-down" style="font-size: 12px;" v-show="!warningGroupId.id"></i>
          </x-input>
          <x-option
                  v-for="city in notifyGroupList"
                  :key="city.id"
                  :value="city"
                  :label="city.code">
          </x-option>
        </x-select>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('收件人')}}
      </div>
      <div class="cont">
        <m-email v-model="receivers" :repeat-data="receiversCc"></m-email>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('抄送人')}}
      </div>
      <div class="cont">
        <m-email v-model="receiversCc" :repeat-data="receivers"></m-email>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('补数')}}
      </div>
      <div class="cont">
        <div style="padding-top: 6px;">
          <x-checkbox v-model="execType">{{$t('是否补数')}}</x-checkbox>
        </div>
      </div>
    </div>
    <template v-if="execType">
      <div class="clearfix list" style="margin:-6px 0 16px 0">
        <div class="text">
          {{$t('执行方式')}}
        </div>
        <div class="cont">
          <x-radio-group v-model="runMode" style="margin-top: 7px;">
            <x-radio :label="'RUN_MODE_SERIAL'">{{$t('串行执行')}}</x-radio>
            <x-radio :label="'RUN_MODE_PARALLEL'">{{$t('并行执行')}}</x-radio>
          </x-radio-group>
        </div>
      </div>
      <div class="clearfix list">
        <div class="text">
          {{$t('时间')}}
        </div>
        <div class="cont">
          <x-datepicker
                  style="width: 360px;"
                  :panel-num="2"
                  placement="bottom-start"
                  @on-change="_datepicker"
                  :value="scheduleTime"
                  type="daterange"
                  :placeholder="$t('选择日期区间')"
                  format="YYYY-MM-DD HH:mm:ss">
          </x-datepicker>
        </div>
      </div>
    </template>
    <div class="submit">
      <x-button type="text" @click="close()"> {{$t('取消')}} </x-button>
      <x-button type="primary" shape="circle" :loading="spinnerLoading" @click="ok()" v-ps="['GENERAL_USER']">{{spinnerLoading ? 'Loading...' : $t('启动')}} </x-button>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import dayjs from 'dayjs'
  import mEmail from './email.vue'
  import store from '@/conf/home/store'
  import { warningTypeList } from './util'
  import mPriority from '@/module/components/priority/priority'

  export default {
    name: 'start-process',
    data () {
      return {
        store,
        processDefinitionId: 0,
        failureStrategy: 'CONTINUE',
        warningTypeList: warningTypeList,
        warningType: {},
        notifyGroupList: [],
        warningGroupId: {},
        scheduleTime: '',
        spinnerLoading: false,
        execType: false,
        taskDependType: 'TASK_POST',
        receivers: [],
        receiversCc: [],
        runMode: 'RUN_MODE_SERIAL',
        processInstancePriority: 'MEDIUM'
      }
    },
    props: {
      item: Object,
      startNodeList: {
        type: String,
        default: ''
      },
      sourceType: String
    },
    methods: {
      _datepicker (val) {
        this.scheduleTime = val
      },
      _start () {
        this.spinnerLoading = true
        let param = {
          processDefinitionId: this.item.id,
          scheduleTime: this.scheduleTime.length && this.scheduleTime.join(',') || '',
          failureStrategy: this.failureStrategy,
          warningType: this.warningType.id,
          warningGroupId: _.isEmpty(this.warningGroupId) ? 0 : this.warningGroupId.id,
          execType: this.execType ? 'COMPLEMENT_DATA' : null,
          startNodeList: this.startNodeList,
          taskDependType: this.taskDependType,
          runMode: this.runMode,
          processInstancePriority: this.processInstancePriority,
          receivers: this.receivers.join(',') || '',
          receiversCc: this.receiversCc.join(',') || ''
        }
        // Executed from the specified node
        if (this.sourceType === 'contextmenu') {
          param.taskDependType = this.taskDependType
        }
        this.store.dispatch('dag/processStart', param).then(res => {
          this.$message.success(res.msg)
          this.$emit('onUpdate')
          setTimeout(() => {
            this.spinnerLoading = false
            this.close()
          }, 500)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.spinnerLoading = false
        })
      },
      _getNotifyGroupList () {
        return new Promise((resolve, reject) => {
          let notifyGroupListS = _.cloneDeep(this.store.state.dag.notifyGroupListS) || []
          if (!notifyGroupListS.length) {
            this.store.dispatch('dag/getNotifyGroupList').then(res => {
              this.notifyGroupList = res
              resolve()
            })
          } else {
            this.notifyGroupList = notifyGroupListS
            resolve()
          }
        })
      },
      _getReceiver () {
        this.store.dispatch('dag/getReceiver', { processDefinitionId: this.item.id }).then(res => {
          this.receivers = res.receivers && res.receivers.split(',') || []
          this.receiversCc = res.receiversCc && res.receiversCc.split(',') || []
        })
      },
      ok () {
        this._start()
      },
      close () {
        this.$emit('close')
      }
    },
    watch: {
      execType (a) {
        this.scheduleTime = a ? [dayjs().format('YYYY-MM-DD 00:00:00'), dayjs().format('YYYY-MM-DD 00:00:00')] : ''
      }
    },
    created () {
      this.warningType = this.warningTypeList[0]

      this._getReceiver()
    },
    mounted () {
      this._getNotifyGroupList().then(() => {
        this.$nextTick(() => {
          this.warningGroupId = { id: 0 }
        })
      })
    },
    computed: {},
    components: { mEmail, mPriority }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .start-process-model {
    width: 860px;
    min-height: 300px;
    background: #fff;
    border-radius: 3px;
    .title-box {
      margin-bottom: 18px;
      span {
        padding-left: 30px;
        font-size: 16px;
        padding-top: 29px;
        display: block;
      }
    }
    .list {
      margin-bottom: 14px;
      .text {
        width: 140px;
        float: left;
        text-align: right;
        line-height: 32px;
        padding-right: 8px;
      }
      .cont {
        width: 350px;
        float: left;
        .add-email-model {
          padding: 20px;
        }

      }
    }
    .submit {
      text-align: right;
      padding-right: 30px;
      padding-top: 10px;
      padding-bottom: 30px;
    }
  }
</style>
