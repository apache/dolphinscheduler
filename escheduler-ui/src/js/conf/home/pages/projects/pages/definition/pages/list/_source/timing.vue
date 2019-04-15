<template>
  <div class="timing-process-model">
    <div class="title-box">
      <span>{{$t('定时前请先设置参数')}}</span>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('起止时间')}}
      </div>
      <div class="cont">
        <x-datepicker
                style="width: 300px;"
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
    <div class="clearfix list">
      <div class="text">
        {{$t('定时')}}
      </div>
      <div class="cont">
        <template>
          <x-poptip :ref="'poptip'" placement="bottom-start">
            <div class="crontab-box">
              <v-crontab v-model="crontab" :locale="i18n"></v-crontab>
            </div>
            <template slot="reference">
              <x-input
                      style="width: 300px;"
                      type="text"
                      readonly
                      :value="crontab"
                      autocomplete="off">
              </x-input>
            </template>
          </x-poptip>
        </template>
      </div>
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
    <div class="clearfix list">
      <div class="text">
        {{$t('通知策略')}}
      </div>
      <div class="cont">
        <x-select
                style="width: 200px;"
                v-model="warningType">
          <x-option
                  v-for="city in warningTypeList"
                  :key="city.id"
                  :value="city.id"
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
                :disabled="!notifyGroupList.length"
                v-model="warningGroupId">
          <x-input slot="trigger" readonly slot-scope="{ selectedModel }" :placeholder="$t('请选择通知组')" :value="selectedModel ? selectedModel.label : ''" style="width: 200px;" @on-click-icon.stop="warningGroupId = {}">
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
      <div class="cont" style="width: 680px;">
        <m-email v-model="receivers" :repeat-data="receiversCc"></m-email>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('抄送人')}}
      </div>
      <div class="cont" style="width: 680px;">
        <m-email v-model="receiversCc" :repeat-data="receivers"></m-email>
      </div>
    </div>
    <div class="submit">
      <x-button type="text" @click="close()"> {{$t('取消')}} </x-button>
      <x-button type="primary" shape="circle" :loading="spinnerLoading" @click="ok()" v-ps="['GENERAL_USER']">{{spinnerLoading ? 'Loading...' : (item.crontab ? $t('编辑') : $t('创建'))}} </x-button>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mEmail from './email.vue'
  import '~/@vue/crontab/dist/index.css'
  import store from '@/conf/home/store'
  import { warningTypeList } from './util'
  import { vCrontab } from '~/@vue/crontab/dist'
  import { formatDate } from '@/module/filter/filter'
  import mPriority from '@/module/components/priority/priority'

  export default {
    name: 'timing-process',
    data () {
      return {
        store,
        processDefinitionId: 0,
        failureStrategy: 'CONTINUE',
        warningTypeList: warningTypeList,
        warningType: 'NONE',
        notifyGroupList: [],
        warningGroupId: {},
        spinnerLoading: false,
        scheduleTime: '',
        crontab: '* * * * * ? *',
        cronPopover: false,
        receivers: [],
        receiversCc: [],
        i18n: i18n.globalScope.LOCALE,
        processInstancePriority: 'MEDIUM'
      }
    },
    props: {
      item: Object,
      receiversD: Array,
      receiversCcD: Array
    },
    methods: {
      _datepicker (val) {
        this.scheduleTime = val
      },
      _verification () {
        if (!this.scheduleTime) {
          this.$message.warning(`${i18n.$t('请选择时间')}`)
          return false
        }

        if (!this.crontab) {
          this.$message.warning(`${i18n.$t('请填写 crontab')}`)
          return false
        }
        return true
      },
      _timing () {
        if (this._verification()) {
          let api = ''
          let searchParams = {
            schedule: JSON.stringify({
              startTime: this.scheduleTime[0],
              endTime: this.scheduleTime[1],
              crontab: this.crontab
            }),
            failureStrategy: this.failureStrategy,
            warningType: this.warningType,
            processInstancePriority: this.processInstancePriority,
            warningGroupId: _.isEmpty(this.warningGroupId) ? 0 : this.warningGroupId.id,
            receivers: this.receivers.join(',') || '',
            receiversCc: this.receiversCc.join(',') || ''
          }
          let msg = ''

          // edit
          if (this.item.crontab) {
            api = 'dag/updateSchedule'
            searchParams.id = this.item.id
            msg = '编辑成功！不要忘记上线'
          } else {
            api = 'dag/createSchedule'
            searchParams.processDefinitionId = this.item.id
            msg = '创建成功'
          }

          this.store.dispatch(api, searchParams).then(res => {
            this.$message.success(msg)
            this.$emit('onUpdate')
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },

      _getNotifyGroupList () {
        return new Promise((resolve, reject) => {
          let notifyGroupListS = _.cloneDeep(this.store.state.dag.notifyGroupListS) || []
          if (!notifyGroupListS.length) {
            this.store.dispatch('dag/getNotifyGroupList').then(res => {
              this.notifyGroupList = res
              if (this.notifyGroupList.length) {
                resolve()
              } else {
                reject(new Error(0))
              }
            })
          } else {
            this.notifyGroupList = notifyGroupListS
            resolve()
          }
        })
      },
      ok () {
        this._timing()
      },
      close () {
        this.$emit('close')
      }
    },
    watch: {
    },
    created () {
      this.receivers = _.cloneDeep(this.receiversD)
      this.receiversCc = _.cloneDeep(this.receiversCcD)
    },
    mounted () {
      let item = this.item

      // Determine whether to echo
      if (this.item.crontab) {
        this.crontab = item.crontab
        this.scheduleTime = [formatDate(item.startTime), formatDate(item.endTime)]
        this.failureStrategy = item.failureStrategy
        this.warningType = item.warningType
        this.processInstancePriority = item.processInstancePriority
        this._getNotifyGroupList().then(() => {
          this.$nextTick(() => {
            let list = _.filter(this.notifyGroupList, v => v.id === item.warningGroupId)
            this.warningGroupId = list.length && list[0] || { id: 0 }
          })
        }).catch(() => this.warningGroupId = { id: 0 })
      } else {
        this._getNotifyGroupList().then(() => {
          this.$nextTick(() => {
            this.warningGroupId = { id: 0 }
          })
        }).catch(() => this.warningGroupId = { id: 0 })
      }
    },
    components: { vCrontab, mEmail, mPriority }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .timing-process-model {
    width: 860px;
    min-height: 300px;
    background: #fff;
    border-radius: 3px;
    margin-top: -24%;
    .crontab-box {
      margin: -6px;
      .v-crontab {
      }
    }
    .from-model {
      padding-top: 0;
    }
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
      >.text {
        width: 140px;
        float: left;
        text-align: right;
        line-height: 32px;
        padding-right: 8px;
      }
      .cont {
        width: 350px;
        float: left;
      }
    }
    .submit {
      text-align: right;
      padding-right: 30px;
      padding-top: 10px;
      padding-bottom: 30px;
    }
  }
  .v-crontab-from-model {
    .list-box {
      padding: 0;
    }
  }
</style>
