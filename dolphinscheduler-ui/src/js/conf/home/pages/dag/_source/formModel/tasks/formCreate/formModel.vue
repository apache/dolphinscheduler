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
    <form-create v-model="$f" :rule="rule" :option="option"></form-create>
  </div>
</template>

<script>
  import _ from 'lodash'
  import formCreate from '@form-create/element-ui'
  import { mapActions, mapState } from 'vuex'
  import mLog from '../../log'
  import mMr from '../mr'
  import mSql from '../sql'
  import i18n from '@/module/i18n'
  import mListBox from '../_source/listBox'
  import mShell from './shell'
  import mWaterdrop from '../waterdrop'
  import mSpark from '../spark'
  import mFlink from '../flink'
  import mPython from '../python'
  import mProcedure from '../procedure'
  import mDependent from '../dependent'
  import mHttp from '../http'
  import mDatax from '../datax'
  import mConditions from '../conditions'
  import mSwitch from '../switch'
  import mSqoop from '../sqoop'
  import mSubProcess from '../sub_process'
  import mSelectInput from '../../_source/selectInput'
  import mTimeoutAlarm from '../../_source/timeoutAlarm'
  import mDependentTimeout from '../../_source/dependentTimeout'
  import mWorkerGroups from '../../_source/workerGroups'
  import mRelatedEnvironment from '../../_source/relatedEnvironment'
  import mPreTasks from '../pre_tasks'
  import clickoutside from '@/module/util/clickoutside'
  import disabledState from '@/module/mixin/disabledState'
  import mPriority from '@/module/components/priority/priority'
  import { findComponentDownward } from '@/module/util/'

  formCreate.directive('clickoutside', clickoutside)
  formCreate.component('mLog', mLog)
  formCreate.component('mSelectInput', mSelectInput)
  formCreate.component('mTimeoutAlarm', mTimeoutAlarm)
  formCreate.component('mDependentTimeout', mDependentTimeout)
  formCreate.component('mPriority', mPriority)
  formCreate.component('mWorkerGroups', mWorkerGroups)
  formCreate.component('mPreTasks', mPreTasks)
  formCreate.component('mDependentTimeout', mDependentTimeout)
  formCreate.component('mRelatedEnvironment', mRelatedEnvironment)
  formCreate.component('mListBox', mListBox)
  formCreate.component('mShell', mShell)

  export default {
    name: 'form-model',
    data () {
      return {
        $f: {},
        rule: [],
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
        // for CONDITIONS
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
          delayTime: task.delayTime,
          dependence: task.taskParams.dependence,
          desc: task.description,
          id: task.id,
          maxRetryTimes: task.failRetryTimes,
          name: task.name,
          params: _.omit(task.taskParams, [
            'conditionResult',
            'dependence',
            'waitStartTimeout'
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
        if (!_.trim(this.$f.form.name)) {
          this.$message.warning(`${i18n.$t('Please enter name (required)')}`)
          return false
        }
        if (this.successBranch !== '' && this.successBranch !== null && this.successBranch === this.failedBranch) {
          this.$message.warning(`${i18n.$t('Cannot select the same node for successful branch flow and failed branch flow')}`)
          return false
        }
        if (this.$f.form.name === this.backfillItem.name) {
          return true
        }
        // Name repeat depends on dom backfill dependent store
        const tasks = this.store.state.dag.tasks
        const task = tasks.find((t) => t.name === this.$f.form.name)
        if (task) {
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
        // Verify name
        if (!this._verifName()) {
          return
        }
        // verif workGroup
        if (!this._verifWorkGroup()) {
          return
        }
        // Verify task alarm parameters
        if (!this.$f.el('timeout')._verification()) {
          return
        }

        // Verify node parameters
        if (!this.$refs[this.nodeData.taskType]._shellVerification()) {
          return
        }
        // Verify preTasks and update dag-things
        if (this.$refs.preTasks) {
          this.$refs.preTasks.setPreNodes()
        }
        this.conditionResult.successNode[0] = this.successBranch
        this.conditionResult.failedNode[0] = this.failedBranch
        // Store the corresponding node data structure
        // $(this.$f.el()).append(form);
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
              waitStartTimeout: this.waitStartTimeout
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
      _initRule () {
        const taskNode = {
          type: 'mShell',
          props: {
            backfillItem: this.backfillItem
          },
          on: {
            onParams: this._onParams,
            onCacheParams: this._onCacheParams
          }
        }
        const rule = [
          {
            type: 'div',
            class: 'title-box',
            children: [
              {
                type: 'span',
                class: 'name',
                children: [$t('Current node settings')]
              },
              {
                type: 'span',
                class: 'go-subtask',
                children: [
                  (this.type === 'instance' && this.taskInstance) ? {
                    type: 'm-log',
                    field: 'log',
                    native: true,
                    props: {
                      item: this.backfillItem,
                      taskInstanceId: this.taskInstance.id
                    },
                    children: [
                      {
                        type: 'template',
                        slot: 'history',
                        children: [
                          {
                            type: 'a',
                            href: '#',
                            on: {
                              click: this._seeHistory
                            },
                            children: [
                              {
                                type: 'em',
                                class: 'ansicon el-icon-alarm-clock'
                              },
                              {
                                type: 'em',
                                children: [$t('View history')]
                              }
                            ]
                          }
                        ]
                      },
                      {
                        type: 'template',
                        slot: 'log',
                        children: [
                          {
                            type: 'a',
                            href: '#',
                            children: [
                              {
                                type: 'em',
                                class: 'ansicon el-icon-document'
                              },
                              {
                                type: 'em',
                                children: [$t('View log')]
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  } : undefined
                ]
              }
            ]
          },
          {
            type: 'div',
            class: 'content-box',
            children: [
              {
                type: 'div',
                class: 'form-model',
                children: [
                  {
                    type: 'm-list-box',
                    field: 'nodeName',
                    native: true,
                    children: [
                      {
                        type: 'div',
                        slot: 'text',
                        children: [$t('Node name')]
                      },
                      {
                        type: 'div',
                        slot: 'content',
                        children: [
                          {
                            type: 'el-input',
                            field: 'name',
                            native: true,
                            props: {
                              type: 'text',
                              size: 'small',
                              disabled: this.isDetails,
                              placeholder: i18n.$t('Please enter name (required)'),
                              maxlength: '100'
                            },
                            sync: [{ disabled: this.isDetails }],
                            on: {
                              blur: this._verifName
                            }
                          }
                        ]
                      }
                    ]
                  },
                  /* Running Sign */
                  {
                    type: 'm-list-box',
                    field: 'runningSign',
                    native: true,
                    children: [
                      {
                        type: 'div',
                        slot: 'text',
                        children: [$t('Run flag')]
                      },
                      {
                        type: 'div',
                        slot: 'content',
                        children: [
                          {
                            type: 'radio',
                            field: 'runFlag',
                            props: {
                              size: 'small'
                            },
                            style: {
                              verticalAlign: 'middle',
                              paddingTop: '0',
                              marginTop: '0'
                            },
                            options: [
                              {
                                value: 'NORMAL',
                                label: i18n.$t('Normal'),
                                disabled: this.isDetails
                              },
                              {
                                value: 'FORBIDDEN',
                                label: i18n.$t('Prohibition execution'),
                                disabled: this.isDetails
                              }]
                          }
                        ]
                      }
                    ]
                  },
                  /* description */
                  {
                    type: 'm-list-box',
                    field: 'description',
                    native: true,
                    children: [
                      {
                        type: 'div',
                        slot: 'text',
                        children: [$t('Description')]
                      },
                      {
                        type: 'div',
                        slot: 'content',
                        children: [
                          {
                            type: 'el-input',
                            field: 'desc',
                            native: true,
                            props: {
                              rows: '2',
                              type: 'textarea',
                              disabled: this.isDetails,
                              placeholder: i18n.$t('Please enter description')
                            },
                            sync: [{ disabled: this.isDetails }]
                          }
                        ]
                      }
                    ]
                  },
                  /* Task priority */
                  {
                    type: 'm-list-box',
                    field: 'taskPriority',
                    native: true,
                    children: [
                      {
                        type: 'div',
                        slot: 'text',
                        children: [$t('Task priority')]
                      },
                      {
                        type: 'div',
                        slot: 'content',
                        children: [
                          {
                            type: 'span',
                            class: 'label-box',
                            style: 'width: 193px;display: inline-block;',
                            children: [
                              {
                                type: 'm-priority',
                                field: 'taskInstancePriority',
                                native: true
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  /* Worker group and environment */
                  {
                    type: 'm-list-box',
                    field: '_workerGroup',
                    native: true,
                    children: [
                      {
                        type: 'div',
                        slot: 'text',
                        children: [$t('Worker group')]
                      },
                      {
                        type: 'div',
                        slot: 'content',
                        children: [
                          {
                            type: 'span',
                            class: 'label-box',
                            style: 'width: 193px; display: inline-block',
                            children: [
                              {
                                type: 'm-worker-groups',
                                field: 'workerGroup',
                                native: true
                              }
                            ]
                          },
                          {
                            type: 'span',
                            class: 'text-b',
                            children: [$t('Environment Name')]
                          },
                          {
                            type: 'm-related-environment',
                            field: 'environmentCode',
                            native: true,
                            props: {
                              workerGroup: this.workerGroup,
                              isNewCreate: this.isNewCreate
                            },
                            on: {
                              environmentCodeEvent: this._onUpdateEnvironmentCode
                            }
                          }
                        ]
                      }
                    ]
                  },
                  /* Number of failed retries */
                  {
                    type: 'm-list-box',
                    field: 'retryNum',
                    native: true,
                    children: [
                      {
                        type: 'div',
                        slot: 'text',
                        children: [$t('Number of failed retries')]
                      },
                      {
                        type: 'div',
                        slot: 'content',
                        children: [
                          {
                            type: 'm-select-input',
                            field: 'maxRetryTimes',
                            native: true,
                            value: this.maxRetryTimes,
                            props: {
                              list: [0, 1, 2, 3, 4]
                            }
                          },
                          {
                            type: 'span',
                            children: [$t('Times')]
                          },
                          {
                            type: 'span',
                            class: 'text-b',
                            children: [$t('Failed retry interval')]
                          },
                          {
                            type: 'm-select-input',
                            field: 'retryInterval',
                            native: true,
                            value: this.retryInterval,
                            props: {
                              list: [1, 10, 30, 60, 120]
                            }
                          },
                          {
                            type: 'span',
                            children: [$t('Minute')]
                          }
                        ]
                      }
                    ]
                  },
                  /* Delay execution time */
                  {
                    type: 'm-list-box',
                    field: 'delayExec',
                    native: true,
                    children: [
                      {
                        type: 'div',
                        slot: 'text',
                        children: [$t('Delay execution time')]
                      },
                      {
                        type: 'div',
                        slot: 'content',
                        children: [
                          {
                            type: 'm-select-input',
                            field: 'delayTime',
                            native: true,
                            value: this.delayTime,
                            props: {
                              list: [0, 1, 5, 10]
                            }
                          },
                          {
                            type: 'span',
                            children: [$t('Minute')]
                          }
                        ]
                      }
                    ]
                  },
                  /* Task timeout alarm */
                  {
                    type: 'm-timeout-alarm',
                    field: 'timeout',
                    native: true,
                    props: {
                      backfillItem: this.backfillItem
                    },
                    sync: [{ backfillItem: this.backfillItem }],
                    on: {
                      onTimeout: this._onTimeout
                    }
                  },
                  /* Task node */
                  {
                    type: 'div',
                    field: 'nodeContainer',
                    children: [Object.assign({}, taskNode)]
                  },
                  /* Pre-tasks in workflow */
                  {
                    type: 'm-pre-tasks',
                    field: 'preTasks',
                    native: true,
                    props: {
                      code: this.code
                    }
                  }
                ]
              }
            ]
          },
          {
            type: 'div',
            class: 'bottom-box',
            children: [
              {
                type: 'div',
                class: 'submit',
                style: 'background: #fff;',
                children: [
                  {
                    type: 'el-button',
                    props: {
                      type: 'text',
                      size: 'small',
                      id: 'cancelBtn'
                    },
                    children: [$t('Cancel')]
                  },
                  {
                    type: 'el-button',
                    props: {
                      type: 'primary',
                      size: 'small',
                      round: true,
                      loading: this.spinnerLoading,
                      disabled: this.isDetails
                    },
                    sync: [{ backfillItem: this.backfillItem }, { loading: this.spinnerLoading }, { disabled: this.isDetails }],
                    children: [this.spinnerLoading ? 'Loading...' : $t('Confirm add')],
                    on: {
                      click: this.ok
                    }
                  }
                ]
              }
            ]
          }
        ]
        this.rule = [...rule]
      },
      backfill () {
        let o = Object.assign({}, this.backfillItem)
        if (!_.isEmpty(o)) {
          this.code = o.code
          this.$f.form.name = o.name
          this.$f.form.taskInstancePriority = o.taskInstancePriority
          this.$f.form.runFlag = o.runFlag || 'YES'
          this.$f.form.desc = o.desc
          this.$f.form.maxRetryTimes = o.maxRetryTimes
          this.$f.form.retryInterval = o.retryInterval
          this.$f.form.delayTime = o.delayTime
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
              this.$f.form.workerGroup = res.totalList[0].workerGroup
            })
          } else {
            this.$f.form.workerGroup = o.workerGroup
          }

          this.$f.form.params = o.params || {}
          this.$f.form.dependence = o.dependence || {}
          this.$f.form.cacheDependence = o.dependence || {}
        } else {
          this.$f.form.workerGroup = this.store.state.security.workerGroupsListAll[0].id
        }

        this.cacheBackfillItem = JSON.parse(JSON.stringify(o))
        this.isContentBox = true
      }
    },
    created () {
      // Backfill data
      let taskList = this.store.state.dag.tasks
      if (taskList.length) {
        taskList.forEach((task) => {
          if (task.code === this.nodeData.id) {
            const backfillItem = this.taskToBackfillItem(task)
            this.backfillItem = backfillItem
            this.isNewCreate = false
          }
        })
      }
      this.code = this.nodeData.id

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
      this._initRule()
      setTimeout(() => {
        $('#cancelBtn').mousedown(function (event) {
          window.$$ = this
          event.preventDefault()
          self.close()
        })
        this.backfill(this.backfillItem)
      }, 200)
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
    /* eslint-disable */
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
      mSwitch,
      mSelectInput,
      mTimeoutAlarm,
      mDependentTimeout,
      mPriority,
      mWorkerGroups,
      mRelatedEnvironment,
      mPreTasks
    }
    /* eslint-enable */
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import "../../formModel.scss";
  .ans-radio-disabled {
    .ans-radio-inner:after {
      background-color: #6F8391
    }
  }
  .el-form-item {
    margin-bottom: 0px;
  }
  .el-radio-group {
    vertical-align: middle;
    padding-top: 0px;
    margin-top: 0px;
  }
</style>
