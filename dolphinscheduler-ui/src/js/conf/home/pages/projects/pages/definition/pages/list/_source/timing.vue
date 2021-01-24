/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
<template>
  <div class="timing-process-model">
    <div class="clearfix list">
      <div class="text">
        {{$t('Start and stop time')}}
      </div>
      <div class="cont">
        <el-date-picker
            style="width: 360px"
            v-model="scheduleTime"
            size="small"
            @change="_datepicker"
            type="datetimerange"
            range-separator="-"
            :start-placeholder="$t('startDate')"
            :end-placeholder="$t('endDate')"
            value-format="yyyy-MM-dd HH:mm:ss">
          </el-date-picker>
      </div>
    </div>
    <div class="clearfix list">
      <el-button type="info"  style="margin-left:20px" size="small" round :loading="spinnerLoading" @click="preview()">{{$t('Execute time')}}</el-button>
      <div class="text">
        {{$t('Timing')}}
      </div>

      <div class="cont">
        <template>
          <el-popover
            placement="bottom-start"
            trigger="click">
            <template slot="reference">
              <el-input
                      style="width: 360px;"
                      type="text"
                      size="small"
                      readonly
                      :value="crontab">
              </el-input>
            </template>
            <div class="crontab-box">
              <v-crontab v-model="crontab" :locale="i18n"></v-crontab>
            </div>
          </el-popover>
        </template>
      </div>
    </div>
    <div class="clearfix list">
      <div style = "padding-left: 150px;">{{$t('Next five execution times')}}</div>
      <ul style = "padding-left: 150px;">
        <li v-for="(time,i) in previewTimes" :key='i'>{{time}}</li>
      </ul>
    </div>

    <div class="clearfix list">
      <div class="text">
        {{$t('Failure Strategy')}}
      </div>
      <div class="cont">
        <el-radio-group v-model="failureStrategy" style="margin-top: 7px;" size="small">
          <el-radio :label="'CONTINUE'">{{$t('Continue')}}</el-radio>
          <el-radio :label="'END'">{{$t('End')}}</el-radio>
        </el-radio-group>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('Notification strategy')}}
      </div>
      <div class="cont">
        <el-select
          style="width: 200px;"
          size="small"
          v-model="warningType">
          <el-option
            v-for="city in warningTypeList"
            :key="city.id"
            :value="city.id"
            :label="city.code">
          </el-option>
        </el-select>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('Process priority')}}
      </div>
      <div class="cont">
        <m-priority v-model="processInstancePriority"></m-priority>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('Worker group')}}
      </div>
      <div class="cont">
        <m-worker-groups v-model="workerGroup"></m-worker-groups>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('Notification group')}}
      </div>
      <div class="cont">
        <el-select
          style="width: 200px;"
          size="small"
          :disabled="!notifyGroupList.length"
          v-model="warningGroupId">
          <el-input slot="trigger" readonly slot-scope="{ selectedModel }" :placeholder="$t('Please select a notification group')" :value="selectedModel ? selectedModel.label : ''" style="width: 200px;" @on-click-icon.stop="warningGroupId = {}">
            <em slot="suffix" class="el-icon-error" style="font-size: 15px;cursor: pointer;" v-show="warningGroupId.id"></em>
            <em slot="suffix" class="el-icon-bottom" style="font-size: 12px;" v-show="!warningGroupId.id"></em>
          </el-input>
          <el-option
            v-for="city in notifyGroupList"
            :key="city.id"
            :value="city.id"
            :label="city.code">
          </el-option>
        </el-select>
      </div>
    </div>
    <div class="submit">
      <el-button type="text" size="small" @click="close()"> {{$t('Cancel')}} </el-button>
      <el-button type="primary" size="small" round :loading="spinnerLoading" @click="ok()">{{spinnerLoading ? 'Loading...' : (timingData.item.crontab ? $t('Edit') : $t('Create'))}} </el-button>
    </div>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import { warningTypeList } from './util'
  import { vCrontab } from '@/module/components/crontab/index'
  import { formatDate } from '@/module/filter/filter'
  import mPriority from '@/module/components/priority/priority'
  import mWorkerGroups from '@/conf/home/pages/dag/_source/formModel/_source/workerGroups'

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
        warningGroupId: '',
        spinnerLoading: false,
        scheduleTime: '',
        crontab: '0 0 * * * ? *',
        cronPopover: false,
        i18n: i18n.globalScope.LOCALE,
        processInstancePriority: 'MEDIUM',
        workerGroup: '',
        previewTimes: []
      }
    },
    props: {
      timingData: Object
    },
    methods: {
      _datepicker (val) {
        this.scheduleTime = val
      },
      _verification () {
        if (!this.scheduleTime) {
          this.$message.warning(`${i18n.$t('Please select time')}`)
          return false
        }

        if (this.scheduleTime[0] === this.scheduleTime[1]) {
          this.$message.warning(`${i18n.$t('The start time must not be the same as the end')}`)
          return false
        }

        if (!this.crontab) {
          this.$message.warning(`${i18n.$t('Please enter crontab')}`)
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
            warningGroupId: this.warningGroupId === '' ? 0 : this.warningGroupId,
            workerGroup: this.workerGroup
          }
          let msg = ''

          // edit
          if (this.timingData.item.crontab) {
            api = 'dag/updateSchedule'
            searchParams.id = this.timingData.item.id
            msg = `${i18n.$t('Edit')}${i18n.$t('success')},${i18n.$t('Please go online')}`
          } else {
            api = 'dag/createSchedule'
            searchParams.processDefinitionId = this.timingData.item.id
            msg = `${i18n.$t('Create')}${i18n.$t('success')}`
          }

          this.store.dispatch(api, searchParams).then(res => {
            this.$message.success(msg)
            this.$emit('onUpdateTiming')
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },

      _preview () {
        if (this._verification()) {
          let api = 'dag/previewSchedule'
          let searchParams = {
            schedule: JSON.stringify({
              startTime: this.scheduleTime[0],
              endTime: this.scheduleTime[1],
              crontab: this.crontab
            })
          }

          this.store.dispatch(api, searchParams).then(res => {
            if (res.length) {
              this.previewTimes = res
            } else {
              this.$message.warning(`${i18n.$t('There is no data for this period of time')}`)
            }
          })
        }
      },

      _getNotifyGroupList () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getNotifyGroupList').then(res => {
            this.notifyGroupList = res
            if (this.notifyGroupList.length) {
              resolve()
            } else {
              reject(new Error(0))
            }
          })
        })
      },
      ok () {
        this._timing()
      },
      close () {
        this.$emit('closeTiming')
      },
      preview () {
        this._preview()
      }
    },
    watch: {
    },
    created () {
      if (this.timingData.item.workerGroup === undefined) {
        let stateWorkerGroupsList = this.store.state.security.workerGroupsListAll || []
        if (stateWorkerGroupsList.length) {
          this.workerGroup = stateWorkerGroupsList[0].id
        } else {
          this.store.dispatch('security/getWorkerGroupsAll').then(res => {
            this.$nextTick(() => {
              this.workerGroup = res[0].id
            })
          })
        }
      } else {
        this.workerGroup = this.timingData.item.workerGroup
      }
      if (this.timingData.item.crontab !== null) {
        this.crontab = this.timingData.item.crontab
      }
      if (this.timingData.type === 'timing') {
        let date = new Date()
        let year = date.getFullYear()
        let month = date.getMonth() + 1
        let day = date.getDate()
        if (month < 10) {
          month = '0' + month
        }
        if (day < 10) {
          day = '0' + day
        }
        let startDate = year + '-' + month + '-' + day + ' ' + '00:00:00'
        let endDate = (year + 100) + '-' + month + '-' + day + ' ' + '00:00:00'
        let times = []
        times[0] = startDate
        times[1] = endDate
        this.crontab = '0 0 * * * ? *'
        this.scheduleTime = times
      }
    },
    mounted () {
      let item = this.timingData.item
      // Determine whether to echo
      if (this.timingData.item.crontab) {
        this.crontab = item.crontab
        this.scheduleTime = [formatDate(item.startTime), formatDate(item.endTime)]
        this.failureStrategy = item.failureStrategy
        this.warningType = item.warningType
        this.processInstancePriority = item.processInstancePriority
        this._getNotifyGroupList().then(() => {
          this.$nextTick(() => {
            // let list = _.filter(this.notifyGroupList, v => v.id === item.warningGroupId)
            this.warningGroupId = item.warningGroupId
          })
        }).catch(() => { this.warningGroupId = '' })
      } else {
        this._getNotifyGroupList().then(() => {
          this.$nextTick(() => {
            this.warningGroupId = ''
          })
        }).catch(() => { this.warningGroupId = '' })
      }
    },
    components: { vCrontab, mPriority, mWorkerGroups }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .timing-process-model {
    width: 860px;
    min-height: 300px;
    background: #fff;
    border-radius: 3px;
    margin-top: 0;
    .crontab-box {
      margin: -6px;
      .v-crontab {
      }
    }
    .form-model {
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
  .v-crontab-form-model {
    .list-box {
      padding: 0;
    }
  }
  .x-date-packer-panel .x-date-packer-day .lattice label.bg-hover {
    background: #00BFFF!important;
    margin-top: -4px;
  }
  .x-date-packer-panel .x-date-packer-day .lattice em:hover {
    background: #0098e1!important;
  }
</style>
