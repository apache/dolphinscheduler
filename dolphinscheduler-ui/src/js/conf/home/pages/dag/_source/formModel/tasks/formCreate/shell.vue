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
  <form-create v-model="$f" :rule="rule" :option="option"></form-create>
</template>

<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mListBox from '../_source/listBox'
  import mScriptBox from '../_source/scriptBox'
  import mLocalParams from '../_source/localParams'
  import disabledState from '@/module/mixin/disabledState'
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'
  import Clipboard from 'clipboard'
  import { diGuiTree, searchTree } from '././_source/resourceTree'
  import clickoutside from '@/module/util/clickoutside'
  import formCreate from '@form-create/element-ui'

  import { mapActions } from 'vuex'
  import JSP from './././plugIn/jsPlumbHandle'
  import mSelectInput from '../_source/selectInput'
  import mTimeoutAlarm from '../_source/timeoutAlarm'
  import mDependentTimeout from '../_source/dependentTimeout'
  import mWorkerGroups from '../_source/workerGroups'
  import mPreTasks from '../tasks/pre_tasks'
  import { isNameExDag, rtBantpl } from './././plugIn/util'
  import mPriority from '@/module/components/priority/priority'

  let editor
  formCreate.directive('clickoutside', clickoutside)
  formCreate.component('mSelectInput', mSelectInput)
  formCreate.component('mTimeoutAlarm', mTimeoutAlarm)
  formCreate.component('mDependentTimeout', mDependentTimeout)
  formCreate.component('mPriority', mPriority)
  formCreate.component('mWorkerGroups', mWorkerGroups)
  formCreate.component('mPreTasks', mPreTasks)
  formCreate.component('mDependentTimeout', mDependentTimeout)
  formCreate.component('treeselect', Treeselect)
  formCreate.component('mListBox', mListBox)
  formCreate.component('mLocalParams', mLocalParams)
  formCreate.component('mScriptBox', mScriptBox)
  export default {
    name: 'shell-form-model',
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
        preTasksToDelete: [], // pre-taskIds to delete, used in jsplumb connects

        valueConsistsOf: 'LEAF_PRIORITY',
        // script
        rawScript: '',
        // Custom parameter
        localParams: [],
        // resource(list)
        resourceList: [],
        // Cache ResourceList
        cacheResourceList: [],
        // resource select options
        resourceOptions: [],
        // define options
        option: {
          submitBtn: false
        },
        rule: [],
        normalizer (node) {
          return {
            label: node.name
          }
        },
        allNoResources: [],
        noRes: [],
        item: '',
        scriptBoxDialog: false
      }
    },
    directives: { clickoutside },
    mixins: [disabledState],
    props: {
      nodeData: Object
    },
    methods: {
      ...mapActions('dag', ['getTaskInstanceList']),

      /**
       * Pre-tasks in workflow
       */
      _onPreTasks (o) {
        this.preTaskIdsInWorkflow = o.preTasks
        this.preTasksToAdd = o.preTasksToAdd
        this.preTasksToDelete = o.preTasksToDelete
      },
      /**
       * Task timeout alarm
       */
      _onTimeout (o) {
        this.timeout = Object.assign(this.timeout, {}, o)
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
        if (!_.trim(this.name)) {
          this.$message.warning(`${i18n.$t('Please enter name (required)')}`)
          return false
        }
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
        // Verify name
        if (!this._verifName()) {
          return
        }
        // verif workGroup
        if (!this._verifWorkGroup()) {
          return
        }
        // Verify task alarm parameters
        if (!this.$refs.timeout._verification()) {
          return
        }

        // Verify node parameters
        if (!this._shellVerification()) {
          return
        }
        // Verify preTasks and update dag-things
        if (this.$refs.PRE_TASK) {
          if (!this.$refs.PRE_TASK._verification()) {
            return
          } else {
            // Sync data-targetarr
            $(`#${this.nodeData.id}`).attr(
              'data-targetarr', this.preTaskIdsInWorkflow ? this.preTaskIdsInWorkflow.join(',') : '')

            // Update JSP connections
            let plumbIns = JSP.JspInstance
            let targetId = this.nodeData.id

            // Update new connections
            this.preTasksToAdd.map(sourceId => {
              plumbIns.connect({
                source: sourceId,
                target: targetId,
                type: 'basic',
                paintStyle: { strokeWidth: 2, stroke: '#2d8cf0' },
                HoverPaintStyle: { stroke: '#ccc', strokeWidth: 3 }
              })
            })

            // Update remove connections
            let currentConnects = plumbIns.getAllConnections()
            let len = currentConnects.length
            for (let i = 0; i < len; i++) {
              if (this.preTasksToDelete.indexOf(currentConnects[i].sourceId) > -1 && currentConnects[i].targetId === targetId) {
                plumbIns.deleteConnection(currentConnects[i])
                i -= 1
                len -= 1
              }
            }
          }
        }

        $(`#${this.nodeData.id}`).find('span').text(this.name)
        this.conditionResult.successNode[0] = this.successBranch
        this.conditionResult.failedNode[0] = this.failedBranch
        // Store the corresponding node data structure
        this.$emit('addTaskInfo', {
          item: {
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
      },
      _initRule () {
        this.rule = [
          {
            type: 'div',
            class: 'form-model-wrapper',
            directives: [
              {
                name: 'clickoutside',
                value: this._handleClose
              }
            ],
            children: [
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
                      {
                        type: 'm-log',
                        props: {
                          item: this.backfillItem
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
                      }
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
                                props: {
                                  type: 'text',
                                  value: this.name,
                                  size: 'small',
                                  disabled: this.isDetails,
                                  placeholder: i18n.$t('Please enter name (required)'),
                                  maxlength: '100'
                                },
                                on: {
                                  blur: this._veriName
                                }
                              }
                            ]
                          }
                        ]
                      },
                      /* Running Sign */
                      {
                        type: 'm-list-box',
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
                                type: 'el-radio-group',
                                props: {
                                  value: this.runFlag,
                                  size: 'small'
                                },
                                children: [
                                  {
                                    type: 'el-radio',
                                    props: {
                                      label: '"NORMAL"',
                                      disabled: this.isDetails,
                                      children: [$t('Normal')]
                                    }
                                  },
                                  {
                                    type: 'el-radio',
                                    props: {
                                      label: '"FORBIDDEN"',
                                      disabled: this.isDetails,
                                      children: [$t('Prohibition execution')]
                                    }
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      },
                      /* description */
                      {
                        type: 'm-list-box',
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
                                props: {
                                  rows: '2',
                                  type: 'textarea',
                                  disabled: this.isDetails,
                                  value: this.desc,
                                  placeholder: i18n.$t('Please enter description')
                                }
                              }
                            ]
                          }
                        ]
                      },
                      /* Task priority */
                      {
                        type: 'm-list-box',
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
                                    props: {
                                      value: this.taskInstancePriority
                                    }
                                  }
                                ]
                              },
                              {
                                type: 'span',
                                class: 'text-b',
                                children: [$t('Worker group')]
                              },
                              {
                                type: 'm-worker-groups',
                                props: {
                                  value: this.workerGroup
                                }
                              }
                            ]
                          }
                        ]
                      },
                      /* Number of failed retries */
                      {
                        type: 'm-list-box',
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
                                value: this.maxRetryTimes,
                                props: {
                                  list: '[0,1,2,3,4]'
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
                                value: this.retryInterval,
                                props: {
                                  list: '[1,10,30,60,120]'
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
                                props: {
                                  value: this.delayTime,
                                  list: '[0,1,5,10]'
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
                        props: {
                          ref: 'timeout',
                          backfillItem: this.backfillItem
                        },
                        on: {
                          onTimeout: this._onTimeout
                        }
                      },
                      /* shell node */
                      {
                        type: 'div',
                        class: 'shell-model',
                        props: {
                          ref: 'SHELL',
                          backfillItem: this.backfillItem
                        },
                        on: {
                          onParams: this._onParams,
                          onCacheParams: this._onCacheParams
                        },
                        children: [
                          {
                            type: 'm-list-box',
                            props: {},
                            children: [
                              {
                                type: 'div',
                                slot: 'text',
                                children: [i18n.$t('Script')]
                              },
                              {
                                type: 'div',
                                slot: 'content',
                                title: i18n.$t('Script'),
                                children: [
                                  {
                                    type: 'div',
                                    class: 'form-mirror',
                                    children: [
                                      {
                                        type: 'input',
                                        name: 'code-shell-mirror',
                                        attrs: { id: 'code-shell-mirror' },
                                        value: '',
                                        props: {
                                          type: 'textarea'
                                        }
                                      },
                                      {
                                        type: 'a',
                                        class: 'ans-modal-box-max',
                                        children: [
                                          {
                                            type: 'em',
                                            class: 'el-icon-full-screen',
                                            on: {
                                              click: this.setEditorVal
                                            }
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            type: 'm-list-box',
                            children: [
                              {
                                type: 'div',
                                slot: 'text',
                                children: [i18n.$t('Resources')]
                              },
                              {
                                type: 'div',
                                slot: 'content',
                                children: [
                                  {
                                    type: 'treeselect',
                                    field: 'resource',
                                    name: 'treeselect',
                                    value: this.resourceList,
                                    props: {
                                      placeholder: i18n.$t('Please select resources'),
                                      multiple: 'true',
                                      maxHeight: '200',
                                      options: this.resourceOptions,
                                      normalizer: this.normalizer,
                                      disabled: this.isDetails,
                                      valueConsistsOf: this.valueConsistsOf
                                    },
                                    children: [
                                      {
                                        type: 'div',
                                        slot: 'value-label',
                                        slotScope: 'node',
                                        children: [
                                          '{{ node.raw.fullName }}',
                                          {
                                            type: 'span',
                                            class: 'copy-path',
                                            on: {
                                              mousedown: ($event, node) => this._copyPath($event, node)
                                            },
                                            children: [
                                              {
                                                type: 'em',
                                                class: 'el-icon-copy-document',
                                                title: i18n.$t('Copy path'),
                                                props: {
                                                  dataContainer: 'body',
                                                  dataToggle: 'tooltip'
                                                }
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            type: 'm-list-box',
                            children: [
                              {
                                type: 'div',
                                slot: 'text',
                                children: [i18n.$t('Custom Parameters')]
                              },
                              {
                                type: 'div',
                                slot: 'content',
                                children: [
                                  {
                                    type: 'm-local-params',
                                    props: {
                                      ref: 'refLocalParams',
                                      udpList: this.localParams,
                                      hide: true
                                    },
                                    on: {
                                      onLocalParams: this._onLocalParams
                                    }
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            type: 'ElDialog',
                            props: {
                              visible: this.scriptBoxDialog,
                              appendToBody: true,
                              width: '80%'
                            },
                            sync: ['visible'],
                            children: [
                              {
                                type: 'mScriptBox',
                                props: {
                                  item: this.item
                                },
                                on: {
                                  getSriptBoxValue: this.getSriptBoxValue,
                                  closeAble: this.closeAble
                                }
                              }
                            ]
                          }
                        ]
                      },
                      /* Pre-tasks in workflow */
                      {
                        type: 'm-pre-tasks',
                        props: {
                          ref: 'PRE_TASK',
                          backfillItem: this.backfillItem
                        },
                        on: {
                          onPreTasks: this._onPreTasks
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
                          id: 'cancelBtn',
                          children: [$t('Cancel')]
                        }
                      },
                      {
                        type: 'el-button',
                        props: {
                          type: 'primary',
                          size: 'small',
                          round: true,
                          loading: this.spinnerLoading,
                          disabled: this.isDetails,
                          children: [this.spinnerLoading ? 'Loading...' : $t('Confirm add')]
                        },
                        on: {
                          click: this.ok
                        }
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      },
      _copyPath (e, node) {
        e.stopPropagation()
        let clipboard = new Clipboard('.copy-path', {
          text: function () {
            return node.raw.fullName
          }
        })
        clipboard.on('success', handler => {
          this.$message.success(`${i18n.$t('Copy success')}`)
          // Free memory
          clipboard.destroy()
        })
        clipboard.on('error', handler => {
          // Copy is not supported
          this.$message.warning(`${i18n.$t('The browser does not support automatic copying')}`)
          // Free memory
          clipboard.destroy()
        })
      },
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },
      setEditorVal () {
        this.item = editor.getValue()
        this.scriptBoxDialog = true
      },
      getSriptBoxValue (val) {
        editor.setValue(val)
        // this.scriptBoxDialog = false
      },
      closeAble () {
        // this.scriptBoxDialog = false
      },
      /**
       * return resourceList
       *
       */
      _onResourcesData (a) {
        this.resourceList = a
      },
      /**
       * cache resourceList
       */
      _onCacheResourcesData (a) {
        this.cacheResourceList = a
      },
      /**
       * verification
       */
      _shellVerification () {
        // rawScript verification
        if (!editor.getValue()) {
          this.$message.warning(`${i18n.$t('Please enter script(required)')}`)
          return false
        }

        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }
        // noRes
        if (this.noRes.length > 0) {
          this.$message.warning(`${i18n.$t('Please delete all non-existent resources')}`)
          return false
        }
        // Process resourcelist
        let dataProcessing = _.map(this.resourceList, v => {
          return {
            id: v
          }
        })
        // storage
        this.$emit('on-params', {
          resourceList: dataProcessing,
          localParams: this.localParams,
          rawScript: editor.getValue()
        })
        return true
      },
      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        // editor
        editor = codemirror('code-shell-mirror', {
          mode: 'shell',
          readOnly: this.isDetails
        })

        this.keypress = () => {
          if (!editor.getOption('readOnly')) {
            editor.showHint({
              completeSingle: false
            })
          }
        }

        // Monitor keyboard
        editor.on('keypress', this.keypress)
        editor.setValue(this.rawScript)

        return editor
      },
      dataProcess (backResource) {
        let isResourceId = []
        let resourceIdArr = []
        if (this.resourceList.length > 0) {
          this.resourceList.forEach(v => {
            this.resourceOptions.forEach(v1 => {
              if (searchTree(v1, v)) {
                isResourceId.push(searchTree(v1, v))
              }
            })
          })
          resourceIdArr = isResourceId.map(item => {
            return item.id
          })
          Array.prototype.diff = function (a) {
            return this.filter(function (i) { return a.indexOf(i) < 0 })
          }
          let diffSet = this.resourceList.diff(resourceIdArr)
          let optionsCmp = []
          if (diffSet.length > 0) {
            diffSet.forEach(item => {
              backResource.forEach(item1 => {
                if (item === item1.id || item === item1.res) {
                  optionsCmp.push(item1)
                }
              })
            })
          }
          let noResources = [{
            id: -1,
            name: $t('Unauthorized or deleted resources'),
            fullName: '/' + $t('Unauthorized or deleted resources'),
            children: []
          }]
          if (optionsCmp.length > 0) {
            this.allNoResources = optionsCmp
            optionsCmp = optionsCmp.map(item => {
              return { id: item.id, name: item.name, fullName: item.res }
            })
            optionsCmp.forEach(item => {
              item.isNew = true
            })
            noResources[0].children = optionsCmp
            this.resourceOptions = this.resourceOptions.concat(noResources)
          }
        }
      }
    },
    watch: {
      /**
       * Watch the item change, cache the value it changes
       **/
      _item (val) {
        // this._cacheItem()
      },
      // Watch the cacheParams
      cacheParams (val) {
        this.$emit('on-cache-params', val)
      },
      resourceIdArr (arr) {
        let result = []
        arr.forEach(item => {
          this.allNoResources.forEach(item1 => {
            if (item.id === item1.id) {
              // resultBool = true
              result.push(item1)
            }
          })
        })
        this.noRes = result
      }
    },
    computed: {
      // Define the item model
      _item () {
        return {
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
      },
      resourceIdArr () {
        let isResourceId = []
        let resourceIdArr = []
        if (this.resourceList.length > 0) {
          this.resourceList.forEach(v => {
            this.resourceOptions.forEach(v1 => {
              if (searchTree(v1, v)) {
                isResourceId.push(searchTree(v1, v))
              }
            })
          })
          resourceIdArr = isResourceId.map(item => {
            return { id: item.id, name: item.name, res: item.fullName }
          })
        }
        return resourceIdArr
      },
      cacheParams () {
        return {
          resourceList: this.resourceIdArr,
          localParams: this.localParams
        }
      }
    },
    created () {
      // Unbind copy and paste events
      JSP.removePaste()
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

      let item = this.store.state.dag.resourcesListS
      diGuiTree(item)
      this.resourceOptions = item
      o = this.backfillItem

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.rawScript = o.params.rawScript || ''

        // backfill resourceList
        let backResource = o.params.resourceList || []
        let resourceList = o.params.resourceList || []
        if (resourceList.length) {
          _.map(resourceList, v => {
            if (!v.id) {
              this.store.dispatch('dag/getResourceId', {
                type: 'FILE',
                fullName: '/' + v.res
              }).then(res => {
                this.resourceList.push(res.id)
                this.dataProcess(backResource)
              }).catch(e => {
                this.resourceList.push(v.res)
                this.dataProcess(backResource)
              })
            } else {
              this.resourceList.push(v.id)
              this.dataProcess(backResource)
            }
          })
          this.cacheResourceList = resourceList
        }

        // backfill localParams
        let localParams = o.params.localParams || []
        if (localParams.length) {
          this.localParams = localParams
        }
      }
    },
    mounted () {
      let self = this
      $('#cancelBtn').mousedown(function (event) {
        event.preventDefault()
        self.close()
      })

      this._initRule()
      setTimeout(() => {
        this._handlerEditor()
      }, 200)
    },
    destroyed () {
      if (editor) {
        editor.toTextArea() // Uninstall
        editor.off($('.code-shell-mirror'), 'keypress', this.keypress)
      }
    },
    /* eslint-disable */
    components: {
      mSelectInput,
      mTimeoutAlarm,
      mDependentTimeout,
      mPriority,
      mWorkerGroups,
      mPreTasks,
      mLocalParams,
      mListBox,
      mScriptBox,
      Treeselect
    }
    /* eslint-enable */
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import "././formModel";
  .ans-radio-disabled {
    .ans-radio-inner:after {
      background-color: #6F8391
    }
  }
</style>
