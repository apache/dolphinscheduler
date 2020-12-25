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
  <div class="list-model" style="position: relative;">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th scope="col" style="min-width: 50px">
            <x-checkbox @on-change="_topCheckBoxClick" v-model="checkAll"></x-checkbox>
          </th>
          <th scope="col" style="min-width: 30px">
            <span>{{$t('#')}}</span>
          </th>
          <th scope="col" style="min-width: 200px;max-width: 300px;">
            <span>{{$t('Process Name')}}</span>
          </th>
          <th scope="col" style="min-width: 30px">
            <span>{{$t('State')}}</span>
          </th>
          <th scope="col" style="min-width: 70px">
            <span>{{$t('Run Type')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('Scheduling Time')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('Start Time')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('End Time')}}</span>
          </th>
          <th scope="col" style="min-width: 60px">
            <span>{{$t('Duration')}}s</span>
          </th>
          <th scope="col" style="min-width: 60px">
            <span>{{$t('Run Times')}}</span>
          </th>
          <th scope="col" style="min-width: 55px">
            <span>{{$t('fault-tolerant sign')}}</span>
          </th>
          <th scope="col" style="min-width: 135px">
            <span>{{$t('Executor')}}</span>
          </th>
          <th scope="col" style="min-width: 100px">
            <div style="width: 100px">
              <span>{{$t('host')}}</span>
            </div>
          </th>
          <th scope="col" style="min-width: 210px">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td width="50"><x-checkbox v-model="item.isCheck" :disabled="item.state === 'RUNNING_EXEUTION' || item.state === 'READY_STOP' || item.state === 'READY_PAUSE'" @on-change="_arrDelChange"></x-checkbox></td>
          <td width="50">
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td style="min-width: 200px;max-width: 300px;padding-right: 10px;">
            <span class="ellipsis" style="padding-left: 4px;"><router-link :to="{ path: '/projects/instance/list/' + item.id , query:{id: item.processDefinitionId}}" tag="a" class="links" :title="item.name">{{item.name}}</router-link></span>
          </td>
          <td>
            <span v-html="_rtState(item.state)" style="cursor: pointer;"></span>
          </td>
          <td><span>{{_rtRunningType(item.commandType)}}</span></td>
          <td>
            <span v-if="item.scheduleTime">{{item.scheduleTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.startTime">{{item.startTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.endTime">{{item.endTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td width="70"><span>{{item.duration || '-'}}</span></td>
          <td width="70"><span>{{item.runTimes}}</span></td>
          <td><span>{{item.recovery}}</span></td>
          <td>
            <span v-if="item.executorName">{{item.executorName}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.host" style="word-break: break-all">{{item.host}}</span>
            <span v-else>-</span>
          </td>
          <td style="z-index: inherit;">
            <div v-show="item.disabled">
              <x-button type="info"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Edit')"
                        @click="_reEdit(item)"
                        icon="ans-icon-edit"
                        :disabled="item.state !== 'SUCCESS' && item.state !== 'PAUSE' && item.state !== 'FAILURE' && item.state !== 'STOP'"></x-button>
              <x-button type="info"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Rerun')"
                        @click="_reRun(item,$index)"
                        icon="ans-icon-refresh"
                        :disabled="item.state !== 'SUCCESS' && item.state !== 'PAUSE' && item.state !== 'FAILURE' && item.state !== 'STOP'"></x-button>
              <x-button type="success"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Recovery Failed')"
                        @click="_restore(item,$index)"
                        icon="ans-icon-fail-empty"
                        :disabled="item.state !== 'FAILURE'"></x-button>
              <x-button type="error"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="item.state === 'STOP' ? $t('Recovery Suspend') : $t('Stop')"
                        @click="_stop(item,$index)"
                        :icon="item.state === 'STOP' ? 'ans-icon-pause-solid' : 'ans-icon-stop'"
                        :disabled="item.state !== 'RUNNING_EXEUTION' && item.state != 'STOP'"></x-button>
              <x-button type="warning"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="item.state === 'PAUSE' ? $t('Recovery Suspend') : $t('Pause')"
                        @click="_suspend(item,$index)"
                        :icon="item.state === 'PAUSE' ? 'ans-icon-pause-solid' : 'ans-icon-pause'"
                        :disabled="item.state !== 'RUNNING_EXEUTION' && item.state !== 'PAUSE'"></x-button>
              <x-poptip
                      :ref="'poptip-delete-' + $index"
                      placement="top-end"
                      width="90">
                <p>{{$t('Delete?')}}</p>
                <div style="text-align: right; margin: 0;padding-top: 4px;">
                  <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete($index)">{{$t('Cancel')}}</x-button>
                  <x-button type="primary" size="xsmall" shape="circle" @click="_delete(item,$index)">{{$t('Confirm')}}</x-button>
                </div>
                <template slot="reference">
                  <x-button
                          icon="ans-icon-trash"
                          type="error"
                          shape="circle"
                          size="xsmall"
                          data-toggle="tooltip"
                          :disabled="item.state !== 'SUCCESS' && item.state !== 'FAILURE' && item.state !== 'STOP' && item.state !== 'PAUSE'"
                          :title="$t('delete')">
                  </x-button>
                </template>
              </x-poptip>

              <x-button type="info"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Gantt')"
                        @click="_gantt(item)"
                        icon="ans-icon-gantt">
              </x-button>

            </div>
            <div v-show="!item.disabled">
              <!--Edit-->
              <x-button
                      type="info"
                      shape="circle"
                      size="xsmall"
                      icon="ans-icon-edit"
                      disabled="true">
              </x-button>

              <!--Rerun-->
              <x-button
                      v-show="buttonType === 'run'"
                      type="info"
                      shape="circle"
                      size="xsmall"
                      disabled="true">
                {{item.count}}
              </x-button>
              <x-button
                      v-show="buttonType !== 'run'"
                      type="info"
                      shape="circle"
                      size="xsmall"
                      icon="ans-icon-refresh"
                      disabled="true">
              </x-button>

              <!--Recovery Failed-->
              <x-button
                      v-show="buttonType === 'store'"
                      type="success"
                      shape="circle"
                      size="xsmall"
                      disabled="true">
                {{item.count}}
              </x-button>
              <x-button
                      v-show="buttonType !== 'store'"
                      type="success"
                      shape="circle"
                      size="xsmall"
                      icon="ans-icon-fail-empty"
                      disabled="true">
              </x-button>

              <!--Stop-->
              <!--<x-button-->
                      <!--type="error"-->
                      <!--shape="circle"-->
                      <!--size="xsmall"-->
                      <!--icon="ans-icon-pause"-->
                      <!--disabled="true">-->
              <!--</x-button>-->

              <!--倒计时 => Recovery Suspend/Pause-->
              <x-button
                      v-show="(item.state === 'PAUSE' || item.state == 'STOP') && buttonType === 'suspend'"
                      type="warning"
                      shape="circle"
                      size="xsmall"
                      disabled="true">
                {{item.count}}
              </x-button>
              <!--Recovery Suspend-->
              <x-button
                      v-show="(item.state === 'PAUSE' || item.state == 'STOP') && buttonType !== 'suspend'"
                      type="warning"
                      shape="circle"
                      size="xsmall"
                      icon="ans-icon-pause-solid"
                      disabled="true">
              </x-button>
              <!--Pause-->
              <x-button
                      v-show="item.state !== 'PAUSE'"
                      type="warning"
                      shape="circle"
                      size="xsmall"
                      icon="ans-icon-stop"
                      disabled="true">
              </x-button>
            <!--Stop-->
              <x-button
                      v-show="item.state !== 'STOP'"
                      type="warning"
                      shape="circle"
                      size="xsmall"
                      icon="ans-icon-pause"
                      disabled="true">
              </x-button>

              <!--delete-->
              <x-button
                      type="error"
                      shape="circle"
                      size="xsmall"
                      icon="ans-icon-trash"
                      :disabled="true">
              </x-button>

              <!--Gantt-->
              <x-button
                      type="info"
                      shape="circle"
                      size="xsmall"
                      icon="ans-icon-gantt"
                      disabled="true">
              </x-button>
            </div>
          </td>
        </tr>
      </table>
    </div>
    <x-poptip
            v-show="strDelete !== ''"
            ref="poptipDeleteAll"
            placement="bottom-start"
            width="90">
      <p>{{$t('Delete?')}}</p>
      <div style="text-align: right; margin: 0;padding-top: 4px;">
        <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete(-1)">{{$t('Cancel')}}</x-button>
        <x-button type="primary" size="xsmall" shape="circle" @click="_delete({},-1)">{{$t('Confirm')}}</x-button>
      </div>
      <template slot="reference">
        <x-button size="xsmall" style="position: absolute; bottom: -48px; left: 22px;" >{{$t('Delete')}}</x-button>
      </template>
    </x-poptip>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import { tasksState, runningType } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'list',
    data () {
      return {
        // data
        list: [],
        // btn type
        buttonType: '',
        strDelete: '',
        checkAll: false
      }
    },
    props: {
      processInstanceList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('dag', ['editExecutorsState', 'deleteInstance', 'batchDeleteInstance']),
      /**
       * Return run type
       */
      _rtRunningType (code) {
        return _.filter(runningType, v => v.code === code)[0].desc
      },
      /**
       * Return status
       */
      _rtState (code) {
        let o = tasksState[code]
        return `<em class="ansfont ${o.icoUnicode} ${o.isSpin ? 'as as-spin' : ''}" style="color:${o.color}" data-toggle="tooltip" data-container="body" title="${o.desc}"></em>`
      },
      /**
       * Close the delete layer
       */
      _closeDelete (i) {
        // close batch
        if (i < 0) {
          this.$refs['poptipDeleteAll'].doClose()
          return
        }
        // close one
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
      /**
       * delete
       */
      _delete (item, i) {
        // remove tow++
        if (i < 0) {
          this._batchDelete()
          return
        }
        // remove one
        this.deleteInstance({
          processInstanceId: item.id
        }).then(res => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this._onUpdate()
          this.$message.success(res.msg)
        }).catch(e => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit
       */
      _reEdit (item) {
        this.$router.push({ path: `/projects/instance/list/${item.id}` })
      },
      /**
       * Rerun
       * @param REPEAT_RUNNING
       */
      _reRun (item, index) {
        this._countDownFn({
          id: item.id,
          executeType: 'REPEAT_RUNNING',
          index: index,
          buttonType: 'run'
        })
      },
      /**
       * Resume running
       * @param PAUSE => RECOVER_SUSPENDED_PROCESS
       * @param FAILURE => START_FAILURE_TASK_PROCESS
       */
      _restore (item, index) {
        this._countDownFn({
          id: item.id,
          executeType: 'START_FAILURE_TASK_PROCESS',
          index: index,
          buttonType: 'store'
        })
      },
      /**
       * stop
       * @param STOP
       */
      _stop (item, index) {
        if(item.state == 'STOP') {
          this._countDownFn({
            id: item.id,
            executeType: 'RECOVER_SUSPENDED_PROCESS',
            index: index,
            buttonType: 'suspend'
          })
        } else {
          this._upExecutorsState({
            processInstanceId: item.id,
            executeType: 'STOP'
          })
        }
      },
      /**
       * pause
       * @param PAUSE
       */
      _suspend (item, index) {
        if (item.state === 'PAUSE') {
          this._countDownFn({
            id: item.id,
            executeType: 'RECOVER_SUSPENDED_PROCESS',
            index: index,
            buttonType: 'suspend'
          })
        } else {
          this._upExecutorsState({
            processInstanceId: item.id,
            executeType: 'PAUSE'
          })
        }
      },
      /**
       * operating
       */
      _upExecutorsState (o) {
        this.editExecutorsState(o).then(res => {
          this.$message.success(res.msg)
          $('body').find('.tooltip.fade.top.in').remove()
          this._onUpdate()
        }).catch(e => {
          this.$message.error(e.msg || '')
          this._onUpdate()
        })
      },
      /**
       * Countdown method refresh
       */
      _countDownFn (param) {
        this.buttonType = param.buttonType
        this.editExecutorsState({
          processInstanceId: param.id,
          executeType: param.executeType
        }).then(res => {
          this.list[param.index].disabled = false
          $('body').find('.tooltip.fade.top.in').remove()
          this.$forceUpdate()
          this.$message.success(res.msg)
          // Countdown
          this._countDown(() => {
            this._onUpdate()
          }, param.index)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this._onUpdate()
        })
      },
      /**
       * update
       */
      _onUpdate () {
        this.$emit('on-update')
      },
      /**
       * list data handle
       */
      _listDataHandle (data) {
        if (data.length) {
          _.map(data, v => {
            v.disabled = true
            v.count = 9
          })
        }
        return data
      },
      /**
       * Countdown
       */
      _countDown (fn, index) {
        const TIME_COUNT = 10
        let timer
        let $count
        if (!timer) {
          $count = TIME_COUNT
          timer = setInterval(() => {
            if ($count > 0 && $count <= TIME_COUNT) {
              $count--
              this.list[index].count = $count
              this.$forceUpdate()
            } else {
              fn()
              clearInterval(timer)
              timer = null
            }
          }, 1000)
        }
      },
      _gantt (item) {
        this.$router.push({ path: `/projects/instance/gantt/${item.id}` })
      },

      _topCheckBoxClick (is) {
        _.map(this.list , v => v.isCheck = v.state === ('RUNNING_EXEUTION') || v.state === ('READY_STOP') || v.state === ('READY_PAUSE')? false : is)
        this._arrDelChange()
      },
      _arrDelChange (v) {
        let arr = []
        this.list.forEach((item)=>{
          if (item.isCheck) {
            arr.push(item.id)
          }
        })
        this.strDelete = _.join(arr, ',')
        if (v === false) {
          this.checkAll = false
        }
      },
      _batchDelete () {
        this.$refs['poptipDeleteAll'].doClose()
        this.batchDeleteInstance({
          processInstanceIds: this.strDelete
        }).then(res => {
          this._onUpdate()
          this.checkAll = false
          this.strDelete = ''
          this.$message.success(res.msg)
        }).catch(e => {
          this.checkAll = false
          this.strDelete = ''
          this.$message.error(e.msg || '')
        })
      }
    },
    watch: {
      processInstanceList: {
        handler (a) {
          this.checkAll = false
          this.list = []
          setTimeout(() => {
            this.list = _.cloneDeep(this._listDataHandle(a))
          })
        },
        immediate: true,
        deep: true
      },
      pageNo () {
        this.strDelete = ''
      }
    },
    created () {
    },
    mounted () {
    },
    components: { }
  }
</script>
