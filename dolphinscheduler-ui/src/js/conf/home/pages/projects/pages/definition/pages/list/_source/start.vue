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
  <div class="start-process-model">
    <div class="clearfix list">
      <div class="text">
        {{$t('Process Name')}}
      </div>
      <div style="line-height: 32px;">{{workflowName}}</div>
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
    <div class="clearfix list" v-if="sourceType === 'contextmenu'" style="margin-top: -8px;">
      <div class="text">
        {{$t('Node execution')}}
      </div>
      <div class="cont" style="padding-top: 6px;">
        <el-radio-group v-model="taskDependType" size="small">
          <el-radio :label="'TASK_POST'">{{$t('Backward execution')}}</el-radio>
          <el-radio :label="'TASK_PRE'">{{$t('Forward execution')}}</el-radio>
          <el-radio :label="'TASK_ONLY'">{{$t('Execute only the current node')}}</el-radio>
        </el-radio-group>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('Notification strategy')}}
      </div>
      <div class="cont">
        <el-select style="width: 200px;" v-model="warningType" size="small">
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
        {{$t('Environment Name')}}
      </div>
      <div class="cont">
        <m-related-environment v-model="environmentCode" :workerGroup="workerGroup" v-on:environmentCodeEvent="_onUpdateEnvironmentCode"></m-related-environment>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{$t('Alarm group')}}
      </div>
      <div class="cont">
        <el-select
           style="width: 200px;"
           clearable
           size="small"
           v-model="warningGroupId"
          :disabled="!notifyGroupList.length">
          <el-input slot="trigger" slot-scope="{ selectedModel }" readonly size="small" :value="selectedModel ? selectedModel.label : ''" style="width: 200px;" @on-click-icon.stop="warningGroupId = ''">
            <em slot="suffix" class="el-icon-error" style="font-size: 15px;cursor: pointer;" v-show="warningGroupId"></em>
            <em slot="suffix" class="el-icon-bottom" style="font-size: 12px;" v-show="!warningGroupId"></em>
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
    <div class="clearfix list">
      <div class="text">
        {{$t('Complement Data')}}
      </div>
      <div class="cont">
        <div style="padding-top: 6px;">
          <el-checkbox v-model="execType" size="small">{{$t('Whether it is a complement process?')}}</el-checkbox>
        </div>
      </div>
    </div>
    <template v-if="execType">
      <div class="clearfix list" style="margin:-6px 0 16px 0">
        <div class="text">
          {{$t('Mode of execution')}}
        </div>
        <div class="cont">
          <el-radio-group @change="_updateParallelStatus" style="margin-top: 7px;"
                          v-model="runMode">
            <el-radio :label="'RUN_MODE_SERIAL'">{{$t('Serial execution')}}</el-radio>
            <el-radio :label="'RUN_MODE_PARALLEL'">{{$t('Parallel execution')}}</el-radio>
          </el-radio-group>
        </div>
      </div>
      <div class="clearfix list" style="margin:-6px 0 16px 0" v-if="runMode === 'RUN_MODE_PARALLEL'">
        <div class="text" style="padding-top: 6px;">
          <em @click="_showParallelismInfo" class="ans el-icon-warning"></em>
          {{$t('Parallelism')}}
        </div>
        <div class="cont" style="padding-top: 8px;">
          <el-checkbox @change="_updateEnableCustomParallel" size="small"
                       v-model="enableCustomParallelism">{{$t('Custom Parallelism')}}
            <el-input :disabled="!enableCustomParallelism"
                      :placeholder="$t('Please enter Parallelism')"
                      ref="parallelismInput"
                      size="mini"
                      type="input"
                      v-model="parallismNumber">
            </el-input>
          </el-checkbox>
        </div>
      </div>
      <div class="clearfix list">
        <div class="text">
          {{$t('Schedule date')}}
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
    </template>
    <div class="clearfix list">
      <div class="text">
        <span>{{$t('Startup parameter')}}</span>
      </div>
      <div class="cont" style="width: 688px;">
        <div style="padding-top: 6px;">
          <m-local-params
            ref="refLocalParams"
            @on-local-params="_onLocalParams"
            :udp-list="udpList"
            :hide="false">
          </m-local-params>
        </div>
      </div>
    </div>
    <div class="clearfix list">
      <span class="text">{{$t('Whether dry-run')}}</span>
      <span class="cont" style="padding-top: 5px;">
          <el-switch v-model="dryRun" size="small" active-value="1" inactive-value="0"></el-switch>
      </span>
    </div>
    <div class="submit">
      <el-button type="text" size="small" @click="close()"> {{$t('Cancel')}} </el-button>
      <el-button type="primary" size="small" round :loading="spinnerLoading" @click="ok()" id="btnSubmit">{{spinnerLoading ? $t('Loading...') : $t('Start')}} </el-button>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import dayjs from 'dayjs'
  import store from '@/conf/home/store'
  import { warningTypeList } from './util'
  import mPriority from '@/module/components/priority/priority'
  import mWorkerGroups from '@/conf/home/pages/dag/_source/formModel/_source/workerGroups'
  import mRelatedEnvironment from '@/conf/home/pages/dag/_source/formModel/_source/relatedEnvironment'
  import mLocalParams from '@/conf/home/pages/dag/_source/formModel/tasks/_source/localParams'
  import disabledState from '@/module/mixin/disabledState'
  import { mapMutations } from 'vuex'

  export default {
    name: 'start-process',
    data () {
      return {
        store,
        processDefinitionId: 0,
        failureStrategy: 'CONTINUE',
        warningTypeList: warningTypeList,
        workflowName: '',
        warningType: '',
        notifyGroupList: [],
        warningGroupId: '',
        scheduleTime: '',
        spinnerLoading: false,
        execType: false,
        enableCustomParallelism: false,
        parallismNumber: null,
        taskDependType: 'TASK_POST',
        runMode: 'RUN_MODE_SERIAL',
        processInstancePriority: 'MEDIUM',
        workerGroup: 'default',
        environmentCode: '',
        // Global custom parameters
        definitionGlobalParams: [],
        udpList: [],
        dryRun: 0
      }
    },
    mixins: [disabledState],
    props: {
      startData: Object,
      startNodeList: {
        type: String,
        default: ''
      },
      sourceType: String
    },
    methods: {
      ...mapMutations('dag', ['setIsDetails', 'resetParams']),
      _showParallelismInfo () {
        this.$message.warning({
          dangerouslyUseHTMLString: true,
          message: `<p style='font-size: 14px;'>${i18n.$t('Parallelism tip')}</p>`
        })
      },
      _onLocalParams (a) {
        this.udpList = a
      },
      _datepicker (val) {
        this.scheduleTime = val
      },
      _onUpdateEnvironmentCode (o) {
        this.environmentCode = o
      },
      _verification () {
        if (this.enableCustomParallelism && !this.parallismNumber) {
          this.$message.warning(`${i18n.$t('Parallelism number should be positive integer')}`)
          return false
        }
        if (this.parallismNumber && !(/(^[1-9]\d*$)/.test(this.parallismNumber))) {
          this.$message.warning(`${i18n.$t('Parallelism number should be positive integer')}`)
          return false
        }
        return true
      },
      _start () {
        if (!this._verification()) {
          return
        }
        this.spinnerLoading = true
        let startParams = {}
        for (const item of this.udpList) {
          if (item.value !== '') {
            startParams[item.prop] = item.value
          }
        }
        let param = {
          processDefinitionCode: this.startData.code,
          scheduleTime: this.scheduleTime.length && this.scheduleTime.join(',') || '',
          failureStrategy: this.failureStrategy,
          warningType: this.warningType,
          warningGroupId: this.warningGroupId === '' ? 0 : this.warningGroupId,
          execType: this.execType ? 'COMPLEMENT_DATA' : null,
          startNodeList: this.startNodeList,
          taskDependType: this.taskDependType,
          runMode: this.runMode,
          processInstancePriority: this.processInstancePriority,
          workerGroup: this.workerGroup,
          environmentCode: this.environmentCode,
          startParams: !_.isEmpty(startParams) ? JSON.stringify(startParams) : '',
          expectedParallelismNumber: this.parallismNumber,
          dryRun: this.dryRun
        }
        // Executed from the specified node
        if (this.sourceType === 'contextmenu') {
          param.taskDependType = this.taskDependType
        }
        this.store.dispatch('dag/processStart', param).then(res => {
          this.$message.success(res.msg)
          this.$emit('onUpdateStart')
          // recovery
          this.udpList = _.cloneDeep(this.definitionGlobalParams)
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
          this.store.dispatch('dag/getNotifyGroupList').then(res => {
            this.notifyGroupList = res
            resolve()
          })
        })
      },
      _updateParallelStatus () {
        this.enableCustomParallelism = false
        this.parallismNumber = null
      },
      _updateEnableCustomParallel () {
        if (!this.enableCustomParallelism) {
          this.parallismNumber = null
        } else {
          this.$nextTick(() => {
            this.$refs.parallelismInput.focus()
          })
        }
      },
      _getGlobalParams () {
        this.store.dispatch('dag/getProcessDetails', this.startData.code).then(res => {
          this.definitionGlobalParams = _.cloneDeep(this.store.state.dag.globalParams)
          this.udpList = _.cloneDeep(this.store.state.dag.globalParams)
        })
      },
      ok () {
        this._start()
      },
      close () {
        this.$emit('closeStart')
      }
    },
    watch: {
      execType (a) {
        this.scheduleTime = a ? [dayjs().format('YYYY-MM-DD 00:00:00'), dayjs().format('YYYY-MM-DD 00:00:00')] : ''
      }
    },
    created () {
      this.warningType = this.warningTypeList[0].id
      this.workflowName = this.startData.name
      this._getGlobalParams()
      let stateWorkerGroupsList = this.store.state.security.workerGroupsListAll || []
      if (stateWorkerGroupsList.length) {
        this.workerGroup = stateWorkerGroupsList[0].id
      } else {
        this.store.dispatch('security/getWorkerGroupsAll').then(res => {
          this.$nextTick(() => {
            if (res.length > 0) {
              this.workerGroup = res[0].id
            }
          })
        })
      }
    },
    mounted () {
      this._getNotifyGroupList().then(() => {
        this.$nextTick(() => {
          this.warningGroupId = ''
        })
      })
      this.workflowName = this.startData.name
    },
    computed: {},
    components: { mPriority, mWorkerGroups, mLocalParams, mRelatedEnvironment }
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
    .ans {
      color: #0097e0;
      font-size: 14px;
      vertical-align: middle;
      cursor: pointer;
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
