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
  <div class="form-model-wrapper" v-clickoutside="_handleClose">
    <div class="title-box">
      <span class="name">{{ $t("Current node settings") }}</span>
      <span class="go-subtask">
        <!-- Component can't pop up box to do component processing -->
        <m-log
          v-if="type === 'instance' && taskInstance"
          :item="backfillItem"
          :task-instance-id="taskInstance.id"
        >
          <template slot="history"
            ><a href="javascript:" @click="_seeHistory"
              ><em class="ansicon el-icon-alarm-clock"></em
              ><em>{{ $t("View history") }}</em></a
            ></template
          >
          <template slot="log"
            ><a href="javascript:"
              ><em class="ansicon el-icon-document"></em
              ><em>{{ $t("View log") }}</em></a
            ></template
          >
        </m-log>
        <a href="javascript:" @click="_goSubProcess" v-if="_isGoSubProcess"
          ><em class="ansicon ri-node-tree"></em
          ><em>{{ $t("Enter this child node") }}</em></a
        >
      </span>
    </div>
    <div class="content-box" v-if="isContentBox">
      <div class="form-model">

        <!-- Reference from task -->
        <!-- <reference-from-task :taskType="nodeData.taskType" /> -->

        <!-- Node name -->
        <m-list-box>
          <div slot="text">{{ $t("Node name") }}</div>
          <div slot="content">
            <el-input
              type="text"
              v-model="name"
              size="small"
              :disabled="isDetails"
              :placeholder="$t('Please enter name (required)')"
              maxlength="100"
              @blur="_verifName()"
            >
            </el-input>
          </div>
        </m-list-box>

        <!-- Running sign -->
        <m-list-box>
          <div slot="text">{{ $t("Run flag") }}</div>
          <div slot="content">
            <el-radio-group v-model="runFlag" size="small">
              <el-radio :label="'YES'" :disabled="isDetails">{{
                $t("Normal")
              }}</el-radio>
              <el-radio :label="'NO'" :disabled="isDetails">{{
                $t("Prohibition execution")
              }}</el-radio>
            </el-radio-group>
          </div>
        </m-list-box>

        <!-- description -->
        <m-list-box>
          <div slot="text">{{ $t("Description") }}</div>
          <div slot="content">
            <el-input
              :rows="2"
              type="textarea"
              :disabled="isDetails"
              v-model="desc"
              :placeholder="$t('Please enter description')"
            >
            </el-input>
          </div>
        </m-list-box>

        <!-- Task priority -->
        <m-list-box>
          <div slot="text">{{ $t("Task priority") }}</div>
          <div slot="content">
            <span class="label-box" style="width: 193px; display: inline-block">
              <m-priority v-model="taskInstancePriority"></m-priority>
            </span>
          </div>
        </m-list-box>

        <!-- Worker group and environment -->
        <m-list-box>
          <div slot="text">{{ $t("Worker group") }}</div>
          <div slot="content">
            <span class="label-box" style="width: 193px; display: inline-block">
              <m-worker-groups v-model="workerGroup"></m-worker-groups>
            </span>
            <span class="text-b">{{ $t("Environment Name") }}</span>
            <m-related-environment
              v-model="environmentCode"
              :workerGroup="workerGroup"
              :isNewCreate="isNewCreate"
              v-on:environmentCodeEvent="_onUpdateEnvironmentCode"
            ></m-related-environment>
          </div>
        </m-list-box>

        <!-- Number of failed retries -->
        <m-list-box v-if="nodeData.taskType !== 'SUB_PROCESS'">
          <div slot="text">{{ $t("Number of failed retries") }}</div>
          <div slot="content">
            <m-select-input
              v-model="maxRetryTimes"
              :list="[0, 1, 2, 3, 4]"
            ></m-select-input>
            <span>({{ $t("Times") }})</span>
            <span class="text-b">{{ $t("Failed retry interval") }}</span>
            <m-select-input
              v-model="retryInterval"
              :list="[1, 10, 30, 60, 120]"
            ></m-select-input>
            <span>({{ $t("Minute") }})</span>
          </div>
        </m-list-box>

        <!-- Delay execution time -->
        <m-list-box
          v-if="
            nodeData.taskType !== 'SUB_PROCESS' &&
            nodeData.taskType !== 'CONDITIONS' &&
            nodeData.taskType !== 'DEPENDENT' &&
            nodeData.taskType !== 'SWITCH'
          "
        >
          <div slot="text">{{ $t("Delay execution time") }}</div>
          <div slot="content">
            <m-select-input
              v-model="delayTime"
              :list="[0, 1, 5, 10]"
            ></m-select-input>
            <span>({{ $t("Minute") }})</span>
          </div>
        </m-list-box>

        <!-- Branch flow -->
        <m-list-box v-if="nodeData.taskType === 'CONDITIONS'">
          <div slot="text">{{ $t("State") }}</div>
          <div slot="content">
            <span class="label-box" style="width: 193px; display: inline-block">
              <el-select
                style="width: 157px"
                size="small"
                v-model="successNode"
                :disabled="true"
              >
                <el-option
                  v-for="item in stateList"
                  :key="item.value"
                  :value="item.value"
                  :label="item.label"
                ></el-option>
              </el-select>
            </span>
            <span class="text-b" style="padding-left: 38px">{{
              $t("Branch flow")
            }}</span>
            <el-select
              style="width: 157px"
              size="small"
              v-model="successBranch"
              clearable
              :disabled="isDetails"
            >
              <el-option
                v-for="item in postTasks"
                :key="item.code"
                :value="item.name"
                :label="item.name"
              ></el-option>
            </el-select>
          </div>
        </m-list-box>
        <m-list-box v-if="nodeData.taskType === 'CONDITIONS'">
          <div slot="text">{{ $t("State") }}</div>
          <div slot="content">
            <span class="label-box" style="width: 193px; display: inline-block">
              <el-select
                style="width: 157px"
                size="small"
                v-model="failedNode"
                :disabled="true"
              >
                <el-option
                  v-for="item in stateList"
                  :key="item.value"
                  :value="item.value"
                  :label="item.label"
                ></el-option>
              </el-select>
            </span>
            <span class="text-b" style="padding-left: 38px">{{
              $t("Branch flow")
            }}</span>
            <el-select
              style="width: 157px"
              size="small"
              v-model="failedBranch"
              clearable
              :disabled="isDetails"
            >
              <el-option
                v-for="item in postTasks"
                :key="item.code"
                :value="item.name"
                :label="item.name"
              ></el-option>
            </el-select>
          </div>
        </m-list-box>

        <div v-if="backfillRefresh">
          <!-- Task timeout alarm -->
          <m-timeout-alarm
            v-if="nodeData.taskType !== 'DEPENDENT'"
            ref="timeout"
            :backfill-item="backfillItem"
            @on-timeout="_onTimeout"
          >
          </m-timeout-alarm>
          <!-- Dependent timeout alarm -->
          <m-dependent-timeout
            v-if="nodeData.taskType === 'DEPENDENT'"
            ref="dependentTimeout"
            :backfill-item="backfillItem"
            @on-timeout="_onDependentTimeout"
          >
          </m-dependent-timeout>

          <!-- shell node -->
          <m-shell
            v-if="nodeData.taskType === 'SHELL'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="SHELL"
            :backfill-item="backfillItem"
          >
          </m-shell>
          <!-- sub_process node -->
          <m-sub-process
            v-if="nodeData.taskType === 'SUB_PROCESS'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            @on-set-process-name="_onSetProcessName"
            ref="SUB_PROCESS"
            :backfill-item="backfillItem"
          >
          </m-sub-process>
          <!-- procedure node -->
          <m-procedure
            v-if="nodeData.taskType === 'PROCEDURE'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="PROCEDURE"
            :backfill-item="backfillItem"
          >
          </m-procedure>
          <!-- sql node -->
          <m-sql
            v-if="nodeData.taskType === 'SQL'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="SQL"
            :create-node-id="nodeData.id"
            :backfill-item="backfillItem"
          >
          </m-sql>
          <!-- spark node -->
          <m-spark
            v-if="nodeData.taskType === 'SPARK'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="SPARK"
            :backfill-item="backfillItem"
          >
          </m-spark>
          <m-flink
            v-if="nodeData.taskType === 'FLINK'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="FLINK"
            :backfill-item="backfillItem"
          >
          </m-flink>
          <!-- mr node -->
          <m-mr
            v-if="nodeData.taskType === 'MR'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="MR"
            :backfill-item="backfillItem"
          >
          </m-mr>
          <!-- python node -->
          <m-python
            v-if="nodeData.taskType === 'PYTHON'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="PYTHON"
            :backfill-item="backfillItem"
          >
          </m-python>
          <!-- dependent node -->
          <m-dependent
            v-if="nodeData.taskType === 'DEPENDENT'"
            @on-dependent="_onDependent"
            @on-cache-dependent="_onCacheDependent"
            ref="DEPENDENT"
            :backfill-item="backfillItem"
          >
          </m-dependent>
          <m-http
            v-if="nodeData.taskType === 'HTTP'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="HTTP"
            :backfill-item="backfillItem"
          >
          </m-http>
          <m-datax
            v-if="nodeData.taskType === 'DATAX'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="DATAX"
            :backfill-item="backfillItem"
          >
          </m-datax>
        <m-pigeon
          v-if="nodeData.taskType === 'PIGEON'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          :backfill-item="backfillItem"
          ref="PIGEON">
        </m-pigeon>
          <m-sqoop
            v-if="nodeData.taskType === 'SQOOP'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="SQOOP"
            :backfill-item="backfillItem"
          >
          </m-sqoop>
          <m-conditions
            v-if="nodeData.taskType === 'CONDITIONS'"
            ref="CONDITIONS"
            @on-dependent="_onDependent"
            @on-cache-dependent="_onCacheDependent"
            :backfill-item="backfillItem"
            :prev-tasks="prevTasks"
          >
          </m-conditions>
          <m-switch
            v-if="nodeData.taskType === 'SWITCH'"
            ref="SWITCH"
            @on-switch-result="_onSwitchResult"
            :backfill-item="backfillItem"
            :nodeData="nodeData"
            :postTasks="postTasks"
          ></m-switch>
          <!-- waterdrop node -->
          <m-waterdrop
            v-if="nodeData.taskType === 'WATERDROP'"
            @on-params="_onParams"
            @on-cache-params="_onCacheParams"
            ref="WATERDROP"
            :backfill-item="backfillItem"
          >
          </m-waterdrop>
        </div>
        <!-- Pre-tasks in workflow -->
        <m-pre-tasks
          ref="preTasks"
          v-if="['SHELL', 'SUB_PROCESS'].indexOf(nodeData.taskType) > -1"
          :code="code"
        />
      </div>
    </div>
    <div class="bottom-box">
      <div class="submit" style="background: #fff">
        <el-button type="text" size="small" id="cancelBtn">
          {{ $t("Cancel") }}
        </el-button>
        <el-button
          type="primary"
          size="small"
          round
          :loading="spinnerLoading"
          @click="ok()"
          :disabled="isDetails"
          >{{ spinnerLoading ? $t("Loading...") : $t("Confirm add") }}
        </el-button>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions, mapState } from 'vuex'
  import mLog from './log'
  import mMr from './tasks/mr'
  import mSql from './tasks/sql'
  import i18n from '@/module/i18n'
  import mListBox from './tasks/_source/listBox'
  import mShell from './tasks/shell'
  import mWaterdrop from './tasks/waterdrop'
  import mSpark from './tasks/spark'
  import mFlink from './tasks/flink'
  import mPython from './tasks/python'
  import mProcedure from './tasks/procedure'
  import mDependent from './tasks/dependent'
  import mHttp from './tasks/http'
  import mDatax from './tasks/datax'
  import mPigeon from './tasks/pigeon'
  import mConditions from './tasks/conditions'
  import mSwitch from './tasks/switch.vue'
  import mSqoop from './tasks/sqoop'
  import mSubProcess from './tasks/sub_process'
  import mSelectInput from './_source/selectInput'
  import mTimeoutAlarm from './_source/timeoutAlarm'
  import mDependentTimeout from './_source/dependentTimeout'
  import mWorkerGroups from './_source/workerGroups'
  import mRelatedEnvironment from './_source/relatedEnvironment'
  import mPreTasks from './tasks/pre_tasks'
  import clickoutside from '@/module/util/clickoutside'
  import disabledState from '@/module/mixin/disabledState'
  import mPriority from '@/module/components/priority/priority'
  import { findComponentDownward } from '@/module/util/'
  // import ReferenceFromTask from './_source/referenceFromTask.vue'

  export default {
    name: 'form-model',
    data () {
      return {
        // loading
        spinnerLoading: false,
        // node name
        name: '',
        // description
        desc: '',
        // Node echo data
        backfillItem: {},
        cacheBackfillItem: {},
        // Resource(list)
        resourcesList: [],
        successNode: 'success',
        failedNode: 'failed',
        successBranch: '',
        failedBranch: '',
        conditionResult: {
          successNode: [],
          failedNode: []
        },
        switchResult: {},
        // dependence
        dependence: {},
        // cache dependence
        cacheDependence: {},
        // task code
        code: 0,
        // Current node params data
        params: {},
        // Running sign
        runFlag: 'YES',
        // The second echo problem caused by the node data is specifically which node hook caused the unfinished special treatment
        isContentBox: false,
        // Number of failed retries
        maxRetryTimes: '0',
        // Failure retry interval
        retryInterval: '1',
        // Delay execution time
        delayTime: '0',
        // Task timeout alarm
        timeout: {},
        // (For Dependent nodes) Wait start timeout alarm
        waitStartTimeout: {},
        // Task priority
        taskInstancePriority: 'MEDIUM',
        // worker group id
        workerGroup: 'default',
        // selected environment
        environmentCode: '',
        selectedWorkerGroup: '',
        stateList: [
          {
            value: 'success',
            label: `${i18n.$t('Success')}`
          },
          {
            value: 'failed',
            label: `${i18n.$t('Failed')}`
          }
        ],
        // for CONDITIONS and SWITCH
        postTasks: [],
        prevTasks: [],
        // refresh part of the formModel, after set backfillItem outside
        backfillRefresh: true,
        // whether this is a new Task
        isNewCreate: true
      }
    },
    provide () {
      return {
        formModel: this
      }
    },
    /**
     * Click on events that are not generated internally by the component
     */
    directives: { clickoutside },
    mixins: [disabledState],
    props: {
      nodeData: Object,
      type: {
        type: String,
        default: ''
      }
    },
    inject: ['dagChart'],
    methods: {
      ...mapActions('dag', ['getTaskInstanceList']),
      taskToBackfillItem (task) {
        return {
          code: task.code,
          conditionResult: task.taskParams.conditionResult,
          switchResult: task.taskParams.switchResult,
          delayTime: task.delayTime,
          dependence: task.taskParams.dependence,
          desc: task.description,
          id: task.id,
          maxRetryTimes: task.failRetryTimes,
          name: task.name,
          params: _.omit(task.taskParams, [
            'conditionResult',
            'dependence',
            'waitStartTimeout',
            'switchResult'
          ]),
          retryInterval: task.failRetryInterval,
          runFlag: task.flag,
          taskInstancePriority: task.taskPriority,
          timeout: {
            interval: task.timeout,
            strategy: task.timeoutNotifyStrategy,
            enable: task.timeoutFlag === 'OPEN'
          },
          type: task.taskType,
          waitStartTimeout: task.taskParams.waitStartTimeout,
          workerGroup: task.workerGroup,
          environmentCode: task.environmentCode
        }
      },
      /**
       * depend
       */
      _onDependent (o) {
        this.dependence = Object.assign(this.dependence, {}, o)
      },
      _onSwitchResult (o) {
        this.switchResult = o
      },
      /**
       * cache dependent
       */
      _onCacheDependent (o) {
        this.cacheDependence = Object.assign(this.cacheDependence, {}, o)
      },
      /**
       * Task timeout alarm
       */
      _onTimeout (o) {
        this.timeout = Object.assign(this.timeout, {}, o)
      },
      /**
       * Dependent timeout alarm
       */
      _onDependentTimeout (o) {
        this.timeout = Object.assign(this.timeout, {}, o.waitCompleteTimeout)
        this.waitStartTimeout = Object.assign(
          this.waitStartTimeout,
          {},
          o.waitStartTimeout
        )
      },
      /**
       * Click external to close the current component
       */
      _handleClose () {
      // this.close()
      },
      /**
       * Jump to task instance
       */
      _seeHistory () {
        this.$emit('seeHistory', this.backfillItem.name)
      },
      /**
       * Enter the child node to judge the process instance or the process definition
       * @param  type = instance
       */
      _goSubProcess () {
        if (_.isEmpty(this.backfillItem)) {
          this.$message.warning(
            `${i18n.$t(
              'The newly created sub-Process has not yet been executed and cannot enter the sub-Process'
            )}`
          )
          return
        }
        if (this.router.history.current.name === 'projects-instance-details') {
          if (!this.taskInstance) {
            this.$message.warning(
              `${i18n.$t(
                'The task has not been executed and cannot enter the sub-Process'
              )}`
            )
            return
          }
          this.store
            .dispatch('dag/getSubProcessId', { taskId: this.taskInstance.id })
            .then((res) => {
              this.$emit('onSubProcess', {
                subInstanceId: res.data.subProcessInstanceId,
                fromThis: this
              })
            })
            .catch((e) => {
              this.$message.error(e.msg || '')
            })
        } else {
          const processDefinitionId =
            this.backfillItem.params.processDefinitionId
          const process = this.processListS.find(
            (process) => process.processDefinition.id === processDefinitionId
          )
          this.$emit('onSubProcess', {
            subProcessCode: process.processDefinition.code,
            fromThis: this
          })
        }
      },
      _onUpdateWorkerGroup (o) {
        this.selectedWorkerGroup = o
      },
      /**
       * return params
       */
      _onParams (o) {
        this.params = Object.assign({}, o)
      },
      _onUpdateEnvironmentCode (o) {
        this.environmentCode = o
      },
      /**
       * _onCacheParams is reserved
       */
      _onCacheParams (o) {
        this.params = Object.assign(this.params, {}, o)
      },
      /**
       * verification name
       */
      _verifName () {
        if (!_.trim(this.name)) {
          this.$message.warning(`${i18n.$t('Please enter name (required)')}`)
          return false
        }
        if (
          this.successBranch !== '' &&
          this.successBranch !== null &&
          this.successBranch === this.failedBranch
        ) {
          this.$message.warning(
            `${i18n.$t(
              'Cannot select the same node for successful branch flow and failed branch flow'
            )}`
          )
          return false
        }
        if (this.name === this.backfillItem.name) {
          return true
        }
        // Name repeat depends on dom backfill dependent store
        const tasks = this.store.state.dag.tasks
        const task = tasks.find((t) => t.name === this.name)
        if (task) {
          this.$message.warning(`${i18n.$t('Name already exists')}`)
          return false
        }
        return true
      },
      _verifWorkGroup () {
        let item = this.store.state.security.workerGroupsListAll.find((item) => {
          return item.id === this.workerGroup
        })
        if (item === undefined) {
          this.$message.warning(
            `${i18n.$t(
              'The Worker group no longer exists, please select the correct Worker group!'
            )}`
          )
          return false
        }
        return true
      },
      /**
       * Global verification procedure
       */
      _verification () {
        // Verify name
        if (!this._verifName()) {
          return
        }
        // verif workGroup
        if (!this._verifWorkGroup()) {
          return
        }
        // Verify task alarm parameters
        if (this.nodeData.taskType === 'DEPENDENT') {
          if (!this.$refs.dependentTimeout._verification()) {
            return
          }
        } else {
          if (!this.$refs.timeout._verification()) {
            return
          }
        }
        // Verify node parameters
        if (!this.$refs[this.nodeData.taskType]._verification()) {
          return
        }
        // set preTask
        if (this.$refs.preTasks) {
          this.$refs.preTasks.setPreNodes()
        }
        this.conditionResult.successNode[0] = this.successBranch
        this.conditionResult.failedNode[0] = this.failedBranch
        this.$emit('addTaskInfo', {
          item: {
            code: this.nodeData.id,
            name: this.name,
            description: this.desc,
            taskType: this.nodeData.taskType,
            taskParams: {
              ...this.params,
              dependence: this.cacheDependence,
              conditionResult: this.conditionResult,
              waitStartTimeout: this.waitStartTimeout,
              switchResult: this.switchResult
            },
            flag: this.runFlag,
            taskPriority: this.taskInstancePriority,
            workerGroup: this.workerGroup,
            failRetryTimes: this.maxRetryTimes,
            failRetryInterval: this.retryInterval,
            timeoutFlag: this.timeout.enable ? 'OPEN' : 'CLOSE',
            timeoutNotifyStrategy: this.timeout.strategy,
            timeout: this.timeout.interval || 0,
            delayTime: this.delayTime,
            environmentCode: this.environmentCode || -1,
            status: this.status,
            branch: this.branch
          },
          fromThis: this
        })
        // set run flag
        this._setRunFlag()
        // set edge label
        this._setEdgeLabel()
      },
      /**
       * Sub-workflow selected node echo name
       */
      _onSetProcessName (name) {
        this.name = name
      },
      /**
       *  set run flag
       *  TODO
       */
      _setRunFlag () {},
      _setEdgeLabel () {
        if (this.successBranch || this.failedBranch) {
          const canvas = findComponentDownward(this.dagChart, 'dag-canvas')
          const edges = canvas.getEdges()
          const successTask = this.postTasks.find(
            (t) => t.name === this.successBranch
          )
          const failedTask = this.postTasks.find(
            (t) => t.name === this.failedBranch
          )
          const sEdge = edges.find(
            (edge) =>
              successTask &&
              edge.sourceId === this.code &&
              edge.targetId === successTask.code
          )
          const fEdge = edges.find(
            (edge) =>
              failedTask &&
              edge.sourceId === this.code &&
              edge.targetId === failedTask.code
          )
          sEdge && canvas.setEdgeLabel(sEdge.id, this.$t('Success'))
          fEdge && canvas.setEdgeLabel(fEdge.id, this.$t('Failed'))
        }
      },
      /**
       * Submit verification
       */
      ok () {
        this._verification()
      },
      /**
       * Close and destroy component and component internal events
       */
      close () {
        let flag = false
        // Delete node without storage
        if (!this.backfillItem.name) {
          flag = true
        }
        this.isContentBox = false
        // flag Whether to delete a node this.$destroy()
        this.$emit('close', {
          item: this.cacheBackfillItem,
          flag: flag,
          fromThis: this
        })
      },
      backfill (backfillItem) {
        const o = backfillItem
        // Non-null objects represent backfill
        if (!_.isEmpty(o)) {
          this.code = o.code
          this.name = o.name
          this.taskInstancePriority = o.taskInstancePriority
          this.runFlag = o.runFlag || 'YES'
          this.desc = o.desc
          this.maxRetryTimes = o.maxRetryTimes
          this.retryInterval = o.retryInterval
          this.delayTime = o.delayTime
          if (o.conditionResult) {
            this.successBranch = o.conditionResult.successNode[0]
            this.failedBranch = o.conditionResult.failedNode[0]
          }
          if (o.switchResult) {
            this.switchResult = o.switchResult
          }
          // If the workergroup has been deleted, set the default workergroup
          for (
            let i = 0;
            i < this.store.state.security.workerGroupsListAll.length;
            i++
          ) {
            let workerGroup = this.store.state.security.workerGroupsListAll[i].id
            if (o.workerGroup === workerGroup) {
              break
            }
          }
          if (o.workerGroup === undefined) {
            this.store
              .dispatch('dag/getTaskInstanceList', {
                pageSize: 10,
                pageNo: 1,
                processInstanceId: this.nodeData.instanceId,
                name: o.name
              })
              .then((res) => {
                this.workerGroup = res.totalList[0].workerGroup
              })
          } else {
            this.workerGroup = o.workerGroup
          }
          this.environmentCode = o.environmentCode === -1 ? '' : o.environmentCode
          this.params = o.params || {}
          this.dependence = o.dependence || {}
          this.cacheDependence = o.dependence || {}
        } else {
          this.workerGroup = this.store.state.security.workerGroupsListAll[0].id
        }
        this.cacheBackfillItem = JSON.parse(JSON.stringify(o))
        this.isContentBox = true
      }
    },
    created () {
      // Backfill data
      let taskList = this.store.state.dag.tasks
      let o = {}
      if (taskList.length) {
        taskList.forEach((task) => {
          if (task.code === this.nodeData.id) {
            const backfillItem = this.taskToBackfillItem(task)
            o = backfillItem
            this.backfillItem = backfillItem
            this.isNewCreate = false
          }
        })
      }
      this.code = this.nodeData.id
      this.backfill(o)

      if (this.dagChart) {
        const canvas = findComponentDownward(this.dagChart, 'dag-canvas')
        const postNodes = canvas.getPostNodes(this.code)
        const prevNodes = canvas.getPrevNodes(this.code)
        const buildTask = (node) => ({
          code: node.id,
          name: node.data.taskName,
          type: node.data.taskType
        })
        this.postTasks = postNodes.map(buildTask)
        this.prevTasks = prevNodes.map(buildTask)
      }
    },
    mounted () {
      let self = this
      $('#cancelBtn').mousedown(function (event) {
        event.preventDefault()
        self.close()
      })
    },
    updated () {},
    beforeDestroy () {},
    destroyed () {},
    computed: {
      ...mapState('dag', ['processListS', 'taskInstances']),
      /**
       * Child workflow entry show/hide
       */
      _isGoSubProcess () {
        return this.nodeData.taskType === 'SUB_PROCESS' && this.name
      },
      taskInstance () {
        if (this.taskInstances.length > 0) {
          return this.taskInstances.find(
            (instance) => instance.taskCode === this.nodeData.id
          )
        }
        return null
      }
    },
    components: {
      mListBox,
      mMr,
      mShell,
      mWaterdrop,
      mSubProcess,
      mProcedure,
      mSql,
      mLog,
      mSpark,
      mFlink,
      mPython,
      mDependent,
      mHttp,
      mDatax,
      mPigeon,
      mSqoop,
      mConditions,
      mSwitch,
      mSelectInput,
      mTimeoutAlarm,
      mDependentTimeout,
      mPriority,
      mWorkerGroups,
      mRelatedEnvironment,
      mPreTasks
      // ReferenceFromTask
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
@import "./formModel";
.ans-radio-disabled {
  .ans-radio-inner:after {
    background-color: #6f8391;
  }
}
</style>
