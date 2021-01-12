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
  <div class="clearfix dag-model" >
    <div class="toolbar">
      <div class="title"><span>{{$t('Toolbar')}}</span></div>
      <div class="toolbar-btn">
        <div class="bar-box roundedRect jtk-draggable jtk-droppable jtk-endpoint-anchor jtk-connected"
             :class="v === dagBarId ? 'active' : ''"
             :id="v"
             :key="v"
             v-for="(item,v) in tasksTypeList"
             @mousedown="_getDagId(v)">
          <div data-toggle="tooltip" :title="item.desc">
            <div class="icos" :class="'icos-' + v" ></div>
          </div>
        </div>
      </div>
    </div>
    <div class="dag-contect">
      <div class="dag-toolbar">
        <div class="assist-btn">
          <x-button
                  style="vertical-align: middle;"
                  data-toggle="tooltip"
                  :title="$t('View variables')"
                  data-container="body"
                  type="primary"
                  size="xsmall"
                  :disabled="$route.name !== 'projects-instance-details'"
                  @click="_toggleView"
                  icon="ans-icon-code">
          </x-button>
          <x-button
            style="vertical-align: middle;"
            data-toggle="tooltip"
            :title="$t('Startup parameter')"
            data-container="body"
            type="primary"
            size="xsmall"
            :disabled="$route.name !== 'projects-instance-details'"
            @click="_toggleParam"
            icon="ans-icon-arrow-circle-right">
          </x-button>
          <span class="name">{{name}}</span>
          &nbsp;
          <span v-if="name"  class="copy-name" @click="_copyName" :data-clipboard-text="name"><em class="ans-icon-copy" data-container="body"  data-toggle="tooltip" :title="$t('Copy name')" ></em></span>
        </div>
        <div class="save-btn">
          <div class="operation" style="vertical-align: middle;">
            <a href="javascript:"
               v-for="(item,$index) in toolOperList"
               :class="_operationClass(item)"
               :id="item.code"
               :key="$index"
               @click="_ckOperation(item,$event)">
              <x-button type="text" data-container="body" :icon="item.icon" v-tooltip.light="item.desc"></x-button>
            </a>
          </div>
          <x-button
                  type="primary"
                  v-tooltip.light="$t('Format DAG')"
                  icon="ans-icon-triangle-solid-right"
                  size="xsmall"
                  data-container="body"
                  v-if="(type === 'instance' || 'definition') && urlParam.id !=undefined"
                  style="vertical-align: middle;"
                  @click="dagAutomaticLayout">
          </x-button>
          <x-button
                  v-tooltip.light="$t('Refresh DAG status')"
                  data-container="body"
                  style="vertical-align: middle;"
                  icon="ans-icon-refresh"
                  type="primary"
                  :loading="isRefresh"
                  v-if="type === 'instance'"
                  @click="!isRefresh && _refresh()"
                  size="xsmall" >
          </x-button>
          <x-button
                  v-if="isRtTasks"
                  style="vertical-align: middle;"
                  type="primary"
                  size="xsmall"
                  icon="ans-icon-play"
                  @click="_rtNodesDag" >
            {{$t('Return_1')}}
          </x-button>
          <x-button
                  style="vertical-align: middle;"
                  type="primary"
                  size="xsmall"
                  :loading="spinnerLoading"
                  @click="_saveChart"
                  icon="ans-icon-save"
                  >
            {{spinnerLoading ? 'Loading...' : $t('Save')}}
          </x-button>
        </div>
      </div>
      <div class="scrollbar dag-container">
        <div class="jtk-demo" id="jtk-demo">
          <div class="jtk-demo-canvas canvas-wide statemachine-demo jtk-surface jtk-surface-nopan jtk-draggable" id="canvas" ></div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import Dag from './dag'
  import mUdp from './udp/udp'
  import i18n from '@/module/i18n'
  import { jsPlumb } from 'jsplumb'
  import Clipboard from 'clipboard'
  import { allNodesId } from './plugIn/util'
  import { toolOper, tasksType } from './config'
  import mFormModel from './formModel/formModel'
  import { formatDate } from '@/module/filter/filter'
  import { findComponentDownward } from '@/module/util/'
  import disabledState from '@/module/mixin/disabledState'
  import { mapActions, mapState, mapMutations } from 'vuex'

  let eventModel

  export default {
    name: 'dag-chart',
    data () {
      return {
        tasksTypeList: tasksType,
        toolOperList: toolOper(this),
        dagBarId: null,
        toolOperCode: '',
        spinnerLoading: false,
        urlParam: {
          id: this.$route.params.id || null
        },
        isRtTasks: false,
        isRefresh: false,
        isLoading: false,
        taskId: null,
        arg: false,

      }
    },
    mixins: [disabledState],
    props: {
      type: String,
      releaseState: String
    },
    methods: {
      ...mapActions('dag', ['saveDAGchart', 'updateInstance', 'updateDefinition', 'getTaskState']),
      ...mapMutations('dag', ['addTasks', 'cacheTasks', 'resetParams', 'setIsEditDag', 'setName']),

      // DAG automatic layout
      dagAutomaticLayout() {
        if(this.store.state.dag.isEditDag) {
          this.$message.warning(`${i18n.$t('Please save the DAG before formatting')}`)
          return false
        }
        $('#canvas').html('')

      // Destroy round robin
        Dag.init({
        dag: this,
        instance: jsPlumb.getInstance({
          Endpoint: [
            'Dot', { radius: 1, cssClass: 'dot-style' }
          ],
          Connector: 'Bezier',
          PaintStyle: { lineWidth: 2, stroke: '#456' }, // Connection style
          ConnectionOverlays: [
            [
              'Arrow',
              {
                location: 1,
                id: 'arrow',
                length: 12,
                foldback: 0.8
              }
            ]
          ],
          Container: 'canvas'
        })
      })
        if (this.tasks.length) {
          Dag.backfill(true)
          if (this.type === 'instance') {
            this._getTaskState(false).then(res => {})
          }
        } else {
          Dag.create()
        }
      },

      init (args) {
        if (this.tasks.length) {
          Dag.backfill(args)
          // Process instances can view status
          if (this.type === 'instance') {
            this._getTaskState(false).then(res => {})
            // Round robin acquisition status
            this.setIntervalP = setInterval(() => {
              this._getTaskState(true).then(res => {})
            }, 90000)
          }
        } else {
          Dag.create()
        }
      },
      /**
       * copy name
       */
      _copyName(){
        let clipboard = new Clipboard(`.copy-name`)
        clipboard.on('success', e => {
          this.$message.success(`${i18n.$t('Copy success')}`)
          // Free memory
          clipboard.destroy()
        })
        clipboard.on('error', e => {
          // Copy is not supported
          this.$message.warning(`${i18n.$t('The browser does not support automatic copying')}`)
          // Free memory
          clipboard.destroy()
        })
      },
      /**
       * Get state interface
       * @param isReset Whether to manually refresh
       */
      _getTaskState (isReset) {
        return new Promise((resolve, reject) => {
          this.getTaskState(this.urlParam.id).then(res => {
            let data = res.list
            let state = res.processInstanceState
            let taskList = res.taskList
            let idArr = allNodesId()
            const titleTpl = (item, desc) => {
              let $item = _.filter(taskList, v => v.name === item.name)[0]
              return `<div style="text-align: left">${i18n.$t('Name')}：${$item.name}</br>${i18n.$t('State')}：${desc}</br>${i18n.$t('type')}：${$item.taskType}</br>${i18n.$t('host')}：${$item.host || '-'}</br>${i18n.$t('Retry Count')}：${$item.retryTimes}</br>${i18n.$t('Submit Time')}：${formatDate($item.submitTime)}</br>${i18n.$t('Start Time')}：${formatDate($item.startTime)}</br>${i18n.$t('End Time')}：${$item.endTime ? formatDate($item.endTime) : '-'}</br></div>`
            }

            // remove tip state dom
            $('.w').find('.state-p').html('')

            data.forEach(v1 => {
              idArr.forEach(v2 => {
                if (v2.name === v1.name) {
                  let dom = $(`#${v2.id}`)
                  let state = dom.find('.state-p')
                  let depState = ''
                   taskList.forEach(item=>{
                    if(item.name==v1.name) {
                      depState = item.state
                    }
                  })
                  dom.attr('data-state-id', v1.stateId)
                  dom.attr('data-dependent-result', v1.dependentResult || '')
                  dom.attr('data-dependent-depState', depState)
                  state.append(`<strong class="${v1.icoUnicode} ${v1.isSpin ? 'as as-spin' : ''}" style="color:${v1.color}" data-toggle="tooltip" data-html="true" data-container="body"></strong>`)
                  state.find('strong').attr('title', titleTpl(v2, v1.desc))
                }
              })
            })
            if (state === 'PAUSE' || state === 'STOP' || state === 'FAILURE' || this.state === 'SUCCESS') {
              // Manual refresh does not regain large json
              if (isReset) {
                findComponentDownward(this.$root, `${this.type}-details`)._reset()
              }
            }
            resolve()
          })
        })
      },
      /**
       * Get the action bar id
       * @param item
       */
      _getDagId (v) {
        // if (this.isDetails) {
        //   return
        // }
        this.dagBarId = v
      },
      /**
       * operating
       */
      _ckOperation (item) {
        let is = true
        let code = ''

        if (item.disable) {
          return
        }

        if (this.toolOperCode === item.code) {
          this.toolOperCode = ''
          code = item.code
          is = false
        } else {
          this.toolOperCode = item.code
          code = this.toolOperCode
          is = true
        }

        // event type
        Dag.toolbarEvent({
          item: item,
          code: code,
          is: is
        })
      },
      _operationClass (item) {
        return this.toolOperCode === item.code ? 'active' : ''
        // if (item.disable) {
        //   return this.toolOperCode === item.code ? 'active' : ''
        // } else {
        //   return 'disable'
        // }
      },
      /**
       * Storage interface
       */
      _save (sourceType) {
        return new Promise((resolve, reject) => {
          this.spinnerLoading = true
          // Storage store
          Dag.saveStore().then(res => {
            if(this._verifConditions(res.tasks)) {
              if (this.urlParam.id) {
                /**
                 * Edit
                 * @param saveInstanceEditDAGChart => Process instance editing
                 * @param saveEditDAGChart => Process definition editing
                 */
                this[this.type === 'instance' ? 'updateInstance' : 'updateDefinition'](this.urlParam.id).then(res => {
                  this.$message.success(res.msg)
                  this.spinnerLoading = false
                  resolve()
                }).catch(e => {
                  this.$message.error(e.msg || '')
                  this.spinnerLoading = false
                  reject(e)
                })
              } else {
                // New
                this.saveDAGchart().then(res => {
                  this.$message.success(res.msg)
                  this.spinnerLoading = false
                  // source @/conf/home/pages/dag/_source/editAffirmModel/index.js
                  if (sourceType !== 'affirm') {
                    // Jump process definition
                    this.$router.push({ name: 'projects-definition-list' })
                  }
                  resolve()
                }).catch(e => {
                  this.$message.error(e.msg || '')
                  this.setName('')
                  this.spinnerLoading = false
                  reject(e)
                })
              }
            }
          })
        })
      },
      _verifConditions (value) {
        let tasks = value
        let bool = true
        tasks.map(v=>{
          if(v.type == 'CONDITIONS' && (v.conditionResult.successNode[0] =='' || v.conditionResult.successNode[0] == null || v.conditionResult.failedNode[0] =='' || v.conditionResult.failedNode[0] == null)) {
            bool = false
            return false
          }
        })
        if(!bool) {
          this.$message.warning(`${i18n.$t('Successful branch flow and failed branch flow are required')}`)
          this.spinnerLoading = false
          return false
        }
        return true
      },
      /**
       * Global parameter
       * @param Promise
       */
      _udpTopFloorPop () {
        return new Promise((resolve, reject) => {
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mUdp, {
                on: {
                  onUdp () {
                    modal.remove()
                    resolve()
                  },
                  close () {
                    modal.remove()
                  }
                }
              })
            }
          })
        })
      },
      /**
       * Save chart
       */
      _saveChart () {
        // Verify node
        if (!this.tasks.length) {
          this.$message.warning(`${i18n.$t('Failed to create node to save')}`)
          return
        }
        // Global parameters (optional)
        this._udpTopFloorPop().then(() => {
          return this._save()
        })
      },
      /**
       * Return to the previous child node
       */
      _rtNodesDag () {
        let getIds = this.$route.query.subProcessIds
        let idsArr = getIds.split(',')
        let ids = idsArr.slice(0, idsArr.length - 1)
        let id = idsArr[idsArr.length - 1]
        let query = {}

        if (id !== idsArr[0]) {
          query = { subProcessIds: ids.join(',') }
        }
        let $name = this.$route.name.split('-')
        this.$router.push({ path: `/${$name[0]}/${$name[1]}/list/${id}`, query: query })
      },
      /**
       * Subprocess processing
       * @param subProcessId Subprocess ID
       */
      _subProcessHandle (subProcessId) {
        let subProcessIds = []
        let getIds = this.$route.query.subProcessIds
        if (getIds) {
          let newId = getIds.split(',')
          newId.push(this.urlParam.id)
          subProcessIds = newId
        } else {
          subProcessIds.push(this.urlParam.id)
        }
        let $name = this.$route.name.split('-')
        this.$router.push({ path: `/${$name[0]}/${$name[1]}/list/${subProcessId}`, query: { subProcessIds: subProcessIds.join(',') } })
      },
      /**
       * Refresh data
       */
      _refresh () {
        this.isRefresh = true
        this._getTaskState(false).then(res => {
          setTimeout(() => {
            this.isRefresh = false
            this.$message.success(`${i18n.$t('Refresh status succeeded')}`)
          }, 2200)
        })
      },
      /**
       * View variables
       */
      _toggleView () {
        findComponentDownward(this.$root, `assist-dag-index`)._toggleView()
      },

      /**
       * Starting parameters
       */
      _toggleParam () {
        findComponentDownward(this.$root, `starting-params-dag-index`)._toggleParam()
      },
      /**
       * Create a node popup layer
       * @param Object id
       */
      _createNodes ({ id, type }) {
        let self = this
        self.$modal.destroy()
        let preNode = []
        let rearNode = []
        let rearList = []
        $('div[data-targetarr*="' + id + '"]').each(function(){
          rearNode.push($(this).attr("id"))
        })

        if (rearNode.length>0) {
          rearNode.forEach(v => {
            let rearobj = {}
            rearobj.value = $(`#${v}`).find('.name-p').text()
            rearobj.label = $(`#${v}`).find('.name-p').text()
            rearList.push(rearobj)
          })
        } else {
          rearList = []
        }
        let target = $(`#${id}`).attr('data-targetarr')
        if (target) {
          let nodearr = target.split(',')
          nodearr.forEach(v => {
            let nodeobj = {}
            nodeobj.value = $(`#${v}`).find('.name-p').text()
            nodeobj.label = $(`#${v}`).find('.name-p').text()
            preNode.push(nodeobj)
          })
        } else {
          preNode = []
        }
        if (eventModel) {
          eventModel.remove()
        }

        const removeNodesEvent = (fromThis) => {
          // Manually destroy events inside the component
          fromThis.$destroy()
          // Close the popup
          eventModel.remove()
        }

        this.taskId = id
        type = type || self.dagBarId

        eventModel = this.$drawer({
          closable: false,
          direction: 'right',
          escClose: true,
          className: 'dagMask',
          render: h => h(mFormModel, {
            on: {
              addTaskInfo ({ item, fromThis }) {
                self.addTasks(item)
                setTimeout(() => {
                  removeNodesEvent(fromThis)
                }, 100)
              },
              /**
               * Cache the item
               * @param item
               * @param fromThis
               */
              cacheTaskInfo({item, fromThis}) {
                self.cacheTasks(item)
              },
              close ({ item,flag, fromThis }) {
                self.addTasks(item)
                // Edit status does not allow deletion of nodes
                if (flag) {
                  jsPlumb.remove(id)
                }

                removeNodesEvent(fromThis)
              },
              onSubProcess ({ subProcessId, fromThis }) {
                removeNodesEvent(fromThis)
                self._subProcessHandle(subProcessId)
              }
            },
            props: {
              id: id,
              taskType: type,
              self: self,
              preNode: preNode,
              rearList: rearList,
              instanceId: this.$route.params.id
            }
          })
        })
      },
      removeEventModelById ($id) {
        if(eventModel && this.taskId == $id){
          eventModel.remove()
        }
      }
    },
    watch: {
      'tasks': {
        handler (o) {
          // Edit state does not allow deletion of node a...
          this.setIsEditDag(true)
        }
      }
    },
    created () {
      // Edit state does not allow deletion of node a...
      this.setIsEditDag(false)

      if (this.$route.query.subProcessIds) {
        this.isRtTasks = true
      }

      Dag.init({
        dag: this,
        instance: jsPlumb.getInstance({
          Endpoint: [
            'Dot', { radius: 1, cssClass: 'dot-style' }
          ],
          Connector: 'Bezier',
          PaintStyle: { lineWidth: 2, stroke: '#456' }, // Connection style
          ConnectionOverlays: [
            [
              'Arrow',
              {
                location: 1,
                id: 'arrow',
                length: 12,
                foldback: 0.8
              }
            ]
          ],
          Container: 'canvas'
        })
      })
    },
    mounted () {
      this.init(this.arg)
    },
    beforeDestroy () {
      this.resetParams()

      // Destroy round robin
      clearInterval(this.setIntervalP)
    },
    destroyed () {
      if (eventModel) {
        eventModel.remove()
      }
    },
    computed: {
      ...mapState('dag', ['tasks', 'locations', 'connects', 'isEditDag', 'name'])
    },
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import "./dag";
</style>
