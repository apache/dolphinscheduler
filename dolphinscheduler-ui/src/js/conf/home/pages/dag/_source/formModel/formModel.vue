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
      <span class="name">{{$t('Current node settings')}}</span>
    </div>
    <div class="content-box" v-if="isContentBox">
      <div class="form-model">
        <template>
          <form-create
            v-model="fApi"
            :rule="rule"
            :option="option"
            @submit="onSubmit"
          ></form-create>
        </template>

      </div>
    </div>
    <div class="bottom-box">
      <div class="submit" style="background: #fff;">
        <el-button type="text" size="small" id="cancelBtn"> {{$t('Cancel')}} </el-button>
        <el-button type="primary" size="small" round :loading="spinnerLoading" @click="ok()" :disabled="isDetails">{{spinnerLoading ? 'Loading...' : $t('Confirm add')}} </el-button>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
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
  import JSP from './../plugIn/jsPlumbHandle'
  import mProcedure from './tasks/procedure'
  import mDependent from './tasks/dependent'
  import mHttp from './tasks/http'
  import mDatax from './tasks/datax'
  import mConditions from './tasks/conditions'
  import mSqoop from './tasks/sqoop'
  import mSubProcess from './tasks/sub_process'
  import mSelectInput from './_source/selectInput'
  import mTimeoutAlarm from './_source/timeoutAlarm'
  import mDependentTimeout from './_source/dependentTimeout'
  import mWorkerGroups from './_source/workerGroups'
  import mPreTasks from './tasks/pre_tasks'
  import clickoutside from '@/module/util/clickoutside'
  import disabledState from '@/module/mixin/disabledState'
  import { isNameExDag, rtBantpl } from './../plugIn/util'
  import mPriority from '@/module/components/priority/priority'

  export default {
    name: 'form-model',
    data () {
      return {
        fApi: null,
        rule: [],
        option: {
          "form": {
            "labelPosition": "right",
            "size": "mini",
            "labelWidth": "125px",
            "hideRequiredAsterisk": false,
            "showMessage": true,
            "inlineMessage": false
          }
        },

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
        // dependence
        dependence: {},
        // cache dependence
        cacheDependence: {},
        // task code
        code: '',
        // Current node params data
        params: {},
        // Running sign
        runFlag: 'NORMAL',
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
        // preTasks
        preTaskIdsInWorkflow: [],
        preTasksToAdd: [], // pre-taskIds to add, used in jsplumb connects
        preTasksToDelete: [] // pre-taskIds to delete, used in jsplumb connects
      }
    },
    /**
     * Click on events that are not generated internally by the component
     */
    directives: { clickoutside },
    mixins: [disabledState],
    props: {
      nodeData: Object
    },
    methods: {
      ...mapActions('dag', ['getTaskInstanceList']),
      /**
       * depend
       */
      _onDependent (o) {
        this.dependence = Object.assign(this.dependence, {}, o)
      },
      /**
       * Pre-tasks in workflow
       */
      _onPreTasks (o) {
        this.preTaskIdsInWorkflow = o.preTasks
        this.preTasksToAdd = o.preTasksToAdd
        this.preTasksToDelete = o.preTasksToDelete
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
        this.waitStartTimeout = Object.assign(this.waitStartTimeout, {}, o.waitStartTimeout)
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
          this.$message.warning(`${i18n.$t('The newly created sub-Process has not yet been executed and cannot enter the sub-Process')}`)
          return
        }
        if (this.router.history.current.name === 'projects-instance-details') {
          let stateId = $(`#${this.nodeData.id}`).attr('data-state-id') || null
          if (!stateId) {
            this.$message.warning(`${i18n.$t('The task has not been executed and cannot enter the sub-Process')}`)
            return
          }
          this.store.dispatch('dag/getSubProcessId', { taskId: stateId }).then(res => {
            this.$emit('onSubProcess', {
              subProcessId: res.data.subProcessInstanceId,
              fromThis: this
            })
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        } else {
          this.$emit('onSubProcess', {
            subProcessId: this.backfillItem.params.processDefinitionId,
            fromThis: this
          })
        }
      },
      /**
       * return params
       */
      _onParams (o) {
        this.params = Object.assign({}, o)
      },

      _onCacheParams (o) {
        this.params = Object.assign(this.params, {}, o)
        this._cacheItem()
      },

      _cacheItem () {
        this.conditionResult.successNode[0] = this.successBranch
        this.conditionResult.failedNode[0] = this.failedBranch
        this.$emit('cacheTaskInfo', {
          item: {
            type: this.nodeData.taskType,
            id: this.nodeData.id,
            name: this.name,
            code: this.code,
            params: this.params,
            desc: this.desc,
            runFlag: this.runFlag,
            conditionResult: this.conditionResult,
            dependence: this.cacheDependence,
            maxRetryTimes: this.maxRetryTimes,
            retryInterval: this.retryInterval,
            delayTime: this.delayTime,
            timeout: this.timeout,
            waitStartTimeout: this.waitStartTimeout,
            taskInstancePriority: this.taskInstancePriority,
            workerGroup: this.workerGroup,
            status: this.status,
            branch: this.branch
          },
          fromThis: this
        })
      },
      /**
       * verification name
       */
      _verifName () {
        // if (!_.trim(this.name)) {
        //   this.$message.warning(`${i18n.$t('Please enter name (required)')}`)
        //   return false
        // }
        if (this.successBranch !== '' && this.successBranch !== null && this.successBranch === this.failedBranch) {
          this.$message.warning(`${i18n.$t('Cannot select the same node for successful branch flow and failed branch flow')}`)
          return false
        }
        if (this.name === this.backfillItem.name) {
          return true
        }
        // Name repeat depends on dom backfill dependent store
        if (isNameExDag(this.name, _.isEmpty(this.backfillItem) ? 'dom' : 'backfill')) {
          this.$message.warning(`${i18n.$t('Name already exists')}`)
          return false
        }
        return true
      },
      _verifWorkGroup () {
        let item = this.store.state.security.workerGroupsListAll.find(item => {
          return item.id === this.workerGroup
        })
        if (item === undefined) {
          this.$message.warning(`${i18n.$t('The Worker group no longer exists, please select the correct Worker group!')}`)
          return false
        }
        return true
      },
      /**
       * Global verification procedure
       */
      _verification () {


        $(`#${this.nodeData.id}`).find('span').text(this.name)
        this.conditionResult.successNode[0] = this.successBranch
        this.conditionResult.failedNode[0] = this.failedBranch
        // Store the corresponding node data structure
        this.$emit('addTaskInfo', {
          item: {
            rule : this.rule,
            type: this.nodeData.taskType,
            id: this.nodeData.id,
            name: this.name,
            code: this.code,
            params: this.params,
            desc: this.desc,
            runFlag: this.runFlag,
            conditionResult: this.conditionResult,
            dependence: this.dependence,
            maxRetryTimes: this.maxRetryTimes,
            retryInterval: this.retryInterval,
            delayTime: this.delayTime,
            timeout: this.timeout,
            waitStartTimeout: this.waitStartTimeout,
            taskInstancePriority: this.taskInstancePriority,
            workerGroup: this.workerGroup,
            status: this.status,
            branch: this.branch
          },
          fromThis: this
        })

        // set run flag
        this._setRunFlag()
      },
      /**
       * Sub-workflow selected node echo name
       */
      _onSetProcessName (name) {
        this.name = name
      },
      /**
       *  set run flag
       */
      _setRunFlag () {
        let dom = $(`#${this.nodeData.id}`).find('.ban-p')
        dom.html('')
        if (this.runFlag === 'FORBIDDEN') {
          dom.append(rtBantpl())
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
      }
    },
    watch: {
      /**
       * Watch the item change, cache the value it changes
       **/
      _item (val) {
        // this._cacheItem()
      }
    },
    created () {
      // Unbind copy and paste events
      JSP.removePaste()

      if (this.nodeData.taskType  === 'SHELL') {
        this.rule=[
          {
            "type": "input",
            "field": "ozt5rtyif8up",
            "title": "节点名称",
            "info": "",
            "_fc_drag_tag": "input",
            "hidden": false,
            "display": true,
            "value": "",
            "props": {
              "clearable": true,
              "prefixIcon": "",
              "autofocus": false
            }
          },
          {
            "type": "radio",
            "field": "6jf5rtyils0m",
            "title": "运行状态",
            "info": "",
            "props": {
              "type": "default",
              "disabled": false,
              "_optionType": 0
            },
            "options": [
              {
                "value": "1",
                "label": "正常"
              },
              {
                "value": "2",
                "label": "禁止执行"
              }
            ],
            "_fc_drag_tag": "radio",
            "hidden": false,
            "display": true,
            "value": "1"
          },
          {
            "type": "FcRow",
            "children": [
              {
                "type": "col",
                "props": {
                  "span": 12
                },
                "children": [
                  {
                    "type": "select",
                    "field": "bqs1lqblz8ljy",
                    "title": "任务优先级",
                    "info": "",
                    "effect": {
                      "fetch": ""
                    },
                    "options": [
                      {
                        "value": "HIGHEST",
                        "label": "HIGHEST",
                        "unicode": "el-icon-top",
                        "color": "#ff0000"
                      },
                      {
                        "value": "HIGH",
                        "label": "HIGH",
                        "unicode": "el-icon-top",
                        "color": "#ff0000"
                      },
                      {
                        "value": "MEDIUM",
                        "label": "MEDIUM",
                        "unicode": "el-icon-top",
                        "color": "#ff0000"
                      },
                      {
                        "value": "LOW",
                        "label": "LOW",
                        "unicode": "el-icon-top",
                        "color": "#ff0000"
                      },
                      {
                        "value": "LOWEST",
                        "label": "LOWEST",
                        "unicode": "el-icon-top",
                        "color": "#ff0000"
                      }
                    ],
                    "_fc_drag_tag": "select",
                    "hidden": false,
                    "display": true,
                    "props": {
                      "filterable": true,
                      "clearable": false
                    },
                    "value": "MEDIUM"
                  }
                ],
                "_fc_drag_tag": "col",
                "hidden": false,
                "display": true
              },
              {
                "type": "col",
                "props": {
                  "span": 12
                },
                "children": [
                  {
                    "type": "select",
                    "field": "8lx1lqbm8ofiq",
                    "title": "work分组",
                    "info": "",
                    "effect": {
                      "fetch": {
                        "action": "https://www.baidu.com",
                        "method": "GET",
                        "data": {},
                        "headers": {},
                        "_parse": "[[FORM-CREATE-PREFIX-function (res){\n   return res.data;\n}-FORM-CREATE-SUFFIX]]",
                        "to": "options",
                        "parse": "[[FORM-CREATE-PREFIX-function (res){\n   return res.data;\n}-FORM-CREATE-SUFFIX]]"
                      }
                    },
                    "_fc_drag_tag": "select",
                    "hidden": false,
                    "display": true,
                    "props": {
                      "_optionType": 1
                    }
                  }
                ],
                "_fc_drag_tag": "col",
                "hidden": false,
                "display": true
              }
            ],
            "_fc_drag_tag": "row",
            "hidden": false,
            "display": true
          },
          {
            "type": "FcRow",
            "children": [
              {
                "type": "col",
                "props": {
                  "span": 12,
                  "offset": 0,
                  "push": 0,
                  "pull": 0
                },
                "children": [
                  {
                    "type": "inputNumber",
                    "field": "kd7g1dleuqld5",
                    "title": "失败重试次数",
                    "info": "",
                    "_fc_drag_tag": "inputNumber",
                    "hidden": false,
                    "display": true
                  }
                ],
                "_fc_drag_tag": "col",
                "hidden": false,
                "display": true
              },
              {
                "type": "col",
                "props": {
                  "span": 12,
                  "offset": 0,
                  "push": 0,
                  "pull": 0
                },
                "children": [
                  {
                    "type": "inputNumber",
                    "field": "x63g1dlewutys",
                    "title": "失败重试间隔",
                    "info": "",
                    "_fc_drag_tag": "inputNumber",
                    "hidden": false,
                    "display": true
                  }
                ],
                "_fc_drag_tag": "col",
                "hidden": false,
                "display": true
              }
            ],
            "_fc_drag_tag": "row",
            "hidden": false,
            "display": true
          },
          {
            "type": "FcRow",
            "children": [
              {
                "type": "col",
                "props": {
                  "span": 12
                },
                "children": [
                  {
                    "type": "inputNumber",
                    "field": "pjug1dlg2z4e3",
                    "title": "延时执行时间",
                    "info": "",
                    "_fc_drag_tag": "inputNumber",
                    "hidden": false,
                    "display": true
                  }
                ],
                "_fc_drag_tag": "col",
                "hidden": false,
                "display": true
              }
            ],
            "_fc_drag_tag": "row",
            "hidden": false,
            "display": true
          },
          {
            "type": "switch",
            "field": "timeout",
            "title": "超时告警",
            "info": "",
            "props": {
              "activeValue": "1",
              "activeColor": "",
              "disabled": false
            },
            "_fc_drag_tag": "switch",
            "hidden": false,
            "display": true,
            "value": false,
            "control":[
              {
                "value":"1",
                "rule":[
                  {
                    "type": "radio",
                    "field": "ebd1lqjhz7k2d",
                    "title": "超时策略",
                    "info": "",
                    "effect": {
                      "fetch": ""
                    },
                    "props": {
                      "disabled": false,
                      "type": "default"
                    },
                    "options": [
                      {
                        "value": "1",
                        "label": "超时告警"
                      },
                      {
                        "value": "2",
                        "label": "超时失败"
                      }
                    ],
                    "_fc_drag_tag": "radio",
                    "hidden": false,
                    "display": true,
                    "value": "1"
                  },
                  {
                    "type": "inputNumber",
                    "field": "s7n1lqk23fu3r",
                    "title": "超时时长",
                    "info": "分钟",
                    "props": {
                      "controls": true,
                      "controlsPosition": "right",
                      "min": 0
                    },
                    "_fc_drag_tag": "inputNumber",
                    "hidden": false,
                    "display": true,
                    "value": 30
                  }
                ]
              }
            ]
          },

          {
            "type": "input",
            "field": "yzmg1dlhrvegp",
            "title": "输入框",
            "info": "",
            "props": {
              "type": "textarea",
              "placeholder": "",
              "clearable": true,
              "showPassword": false
            },
            "_fc_drag_tag": "input",
            "hidden": false,
            "display": true
          }
        ]
      }
    debugger
      // Backfill data
      let taskList = this.store.state.dag.tasks

      // fillback use cacheTasks
      let cacheTasks = this.store.state.dag.cacheTasks
      let o = {}
      if (cacheTasks[this.nodeData.id]) {
        o = cacheTasks[this.nodeData.id]
        this.backfillItem = cacheTasks[this.nodeData.id]
      } else {
        if (taskList.length) {
          taskList.forEach(v => {
            if (v.id === this.nodeData.id) {
              o = v
              this.backfillItem = v
            }
          })
        }
      }
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.rule = o.rule

        this.code = o.code
        this.name = o.name
        this.taskInstancePriority = o.taskInstancePriority
        this.runFlag = o.runFlag || 'NORMAL'
        this.desc = o.desc
        this.maxRetryTimes = o.maxRetryTimes
        this.retryInterval = o.retryInterval
        this.delayTime = o.delayTime
        if (o.conditionResult) {
          this.successBranch = o.conditionResult.successNode[0]
          this.failedBranch = o.conditionResult.failedNode[0]
        }
        // If the workergroup has been deleted, set the default workergroup
        for (let i = 0; i < this.store.state.security.workerGroupsListAll.length; i++) {
          let workerGroup = this.store.state.security.workerGroupsListAll[i].id
          if (o.workerGroup === workerGroup) {
            break
          }
        }
        if (o.workerGroup === undefined) {
          this.store.dispatch('dag/getTaskInstanceList', {
            pageSize: 10, pageNo: 1, processInstanceId: this.nodeData.instanceId, name: o.name
          }).then(res => {
            this.workerGroup = res.totalList[0].workerGroup
          })
        } else {
          this.workerGroup = o.workerGroup
        }

        this.params = o.params || {}
        this.dependence = o.dependence || {}
        this.cacheDependence = o.dependence || {}
      } else {
        this.workerGroup = this.store.state.security.workerGroupsListAll[0].id
      }
      this.cacheBackfillItem = JSON.parse(JSON.stringify(o))
      this.isContentBox = true

      // Init value of preTask selector
      let preTaskIds = $(`#${this.nodeData.id}`).attr('data-targetarr')
      if (!_.isEmpty(this.backfillItem)) {
        if (preTaskIds && preTaskIds.length) {
          this.backfillItem.preTasks = preTaskIds.split(',')
        } else {
          this.backfillItem.preTasks = []
        }
      }
    },
    mounted () {
      let self = this
      $('#cancelBtn').mousedown(function (event) {
        event.preventDefault()
        self.close()
      })
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {
      /**
       * Child workflow entry show/hide
       */
      _isGoSubProcess () {
        return this.nodeData.taskType === 'SUB_PROCESS' && this.name
      },

      // Define the item model
      _item () {
        return {
          rule: this.rule,
          type: this.nodeData.taskType,
          id: this.nodeData.id,
          code: this.code,
          name: this.name,
          desc: this.desc,
          runFlag: this.runFlag,
          dependence: this.cacheDependence,
          maxRetryTimes: this.maxRetryTimes,
          retryInterval: this.retryInterval,
          delayTime: this.delayTime,
          timeout: this.timeout,
          waitStartTimeout: this.waitStartTimeout,
          taskInstancePriority: this.taskInstancePriority,
          workerGroup: this.workerGroup,
          successBranch: this.successBranch,
          failedBranch: this.failedBranch
        }
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
      mSqoop,
      mConditions,
      mSelectInput,
      mTimeoutAlarm,
      mDependentTimeout,
      mPriority,
      mWorkerGroups,
      mPreTasks
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import "./formModel";
  .ans-radio-disabled {
    .ans-radio-inner:after {
      background-color: #6F8391
    }
  }
</style>
