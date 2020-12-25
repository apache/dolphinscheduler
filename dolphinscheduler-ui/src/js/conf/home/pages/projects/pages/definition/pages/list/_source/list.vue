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
          <th scope="col" style="min-width: 40px">
            <span>{{$t('#')}}</span>
          </th>
          <th scope="col" style="min-width: 200px;max-width: 300px;">
            <span>{{$t('Process Name')}}</span>
          </th>
          <th scope="col" style="min-width: 50px">
            <span>{{$t('State')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('Create Time')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('Update Time')}}</span>
          </th>
          <th scope="col" style="min-width: 150px">
            <span>{{$t('Description')}}</span>
          </th>
          <th scope="col" style="min-width: 70px">
            <span>{{$t('Modify User')}}</span>
          </th>
          <th scope="col" style="min-width: 70px">
            <div style="width: 80px">
              <span>{{$t('Timing state')}}</span>
            </div>
          </th>
          <th scope="col" style="min-width: 300px">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td width="50"><x-checkbox v-model="item.isCheck" :disabled="item.releaseState === 'ONLINE'" @on-change="_arrDelChange"></x-checkbox></td>
          <td width="50">
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td style="min-width: 200px;max-width: 300px;padding-right: 10px;">
            <span class="ellipsis">
              <router-link :to="{ path: '/projects/definition/list/' + item.id}" tag="a" class="links" :title="item.name">
                {{item.name}}
              </router-link>
            </span>
          </td>
          <td><span>{{_rtPublishStatus(item.releaseState)}}</span></td>
          <td>
            <span v-if="item.createTime">{{item.createTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.updateTime">{{item.updateTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.description" class="ellipsis" v-tooltip.large.top.start.light="{text: item.description, maxWidth: '500px'}">{{item.description}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.modifyBy">{{item.modifyBy}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.scheduleReleaseState === 'OFFLINE'">{{$t('offline')}}</span>
            <span v-if="item.scheduleReleaseState === 'ONLINE'">{{$t('online')}}</span>
            <span v-if="!item.scheduleReleaseState">-</span>
          </td>
          <td style="z-index: inherit;">
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Edit')" @click="_edit(item)" :disabled="item.releaseState === 'ONLINE'"  icon="ans-icon-edit"><!--{{$t('编辑')}}--></x-button>
            <x-button type="success" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Start')" @click="_start(item)" :disabled="item.releaseState !== 'ONLINE'"  icon="ans-icon-play"><!--{{$t('启动')}}--></x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Timing')" @click="_timing(item)" :disabled="item.releaseState !== 'ONLINE' || item.scheduleReleaseState !== null"  icon="ans-icon-timer"><!--{{$t('定时')}}--></x-button>
            <x-button type="warning" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('online')" @click="_poponline(item)" v-if="item.releaseState === 'OFFLINE'"  icon="ans-icon-upward"><!--{{$t('下线')}}--></x-button>
            <x-button type="error" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('offline')" @click="_downline(item)" v-if="item.releaseState === 'ONLINE'"  icon="ans-icon-downward"><!--{{$t('上线')}}--></x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Copy Workflow')" @click="_copyProcess(item)" :disabled="item.releaseState === 'ONLINE'"  icon="ans-icon-copy"><!--{{$t('复制')}}--></x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Cron Manage')" @click="_timingManage(item)" :disabled="item.releaseState !== 'ONLINE'"  icon="ans-icon-datetime"><!--{{$t('定时管理')}}--></x-button>
            <x-poptip
              :ref="'poptip-delete-' + $index"
              placement="bottom-end"
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
                  :disabled="item.releaseState === 'ONLINE'"
                  data-toggle="tooltip"
                  :title="$t('delete')">
                </x-button>
              </template>
            </x-poptip>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('TreeView')" @click="_treeView(item)"  icon="ans-icon-node"><!--{{$t('树形图')}}--></x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Export')" @click="_export(item)"  icon="ans-icon-download"><!--{{$t('导出')}}--></x-button>

          </td>
        </tr>
      </table>
    </div>
    <x-poptip
            v-show="strSelectIds !== ''"
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
    <template v-if="strSelectIds !== ''">
      <x-button size="xsmall" style="position: absolute; bottom: -48px; left: 80px;" @click="_batchExport(item)" >{{$t('Export')}}</x-button>
    </template>

  </div>
</template>
<script>
  import _ from 'lodash'
  import mStart from './start'
  import mTiming from './timing'
  import { mapActions } from 'vuex'
  import { publishStatus } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'definition-list',
    data () {
      return {
        list: [],
        strSelectIds: '',
        checkAll: false
      }
    },
    props: {
      processList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('dag', ['editProcessState', 'getStartCheck', 'getReceiver', 'deleteDefinition', 'batchDeleteDefinition','exportDefinition','copyProcess']),
      ...mapActions('security', ['getWorkerGroupsAll']),
      _rtPublishStatus (code) {
        return _.filter(publishStatus, v => v.code === code)[0].desc
      },
      _treeView (item) {
        this.$router.push({ path: `/projects/definition/tree/${item.id}` })
      },
      /**
       * Start
       */
      _start (item) {
        this.getWorkerGroupsAll()
        this.getStartCheck({ processDefinitionId: item.id }).then(res => {
          let self = this
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mStart, {
                on: {
                  onUpdate () {
                    self._onUpdate()
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  item: item
                }
              })
            }
          })
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * get emial
       */
      _getReceiver (id) {
        return new Promise((resolve, reject) => {
          this.getReceiver({ processDefinitionId: id }).then(res => {
            resolve({
              receivers: res.receivers && res.receivers.split(',') || [],
              receiversCc: res.receiversCc && res.receiversCc.split(',') || []
            })
          })
        })
      },
      /**
       * timing
       */
      _timing (item) {
        let self = this
        this._getReceiver(item.id).then(res => {
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mTiming, {
                on: {
                  onUpdate () {
                    self._onUpdate()
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  item: item,
                  receiversD: res.receivers,
                  receiversCcD: res.receiversCc,
                  type: 'timing'
                }
              })
            }
          })
        })
      },
      /**
       * Timing manage
       */
      _timingManage (item) {
        this.$router.push({ path: `/projects/definition/list/timing/${item.id}` })
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
        this.deleteDefinition({
          processDefinitionId: item.id
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
      _edit (item) {
        this.$router.push({ path: `/projects/definition/list/${item.id}` })
      },
      /**
       * Offline
       */
      _downline (item) {
        this._upProcessState({
          processId: item.id,
          releaseState: 0
        })
      },
      /**
       * online
       */
      _poponline (item) {
        this._upProcessState({
          processId: item.id,
          releaseState: 1
        })
      },
      /**
       * copy
       */
      _copyProcess (item) {
        this.copyProcess({
          processId: item.id
        }).then(res => {
          this.$message.success(res.msg)
          $('body').find('.tooltip.fade.top.in').remove()
          this._onUpdate()
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },

      _export (item) {
        this.exportDefinition({
          processDefinitionIds: item.id,
          fileName: item.name
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },

      _batchExport () {
        this.exportDefinition({
          processDefinitionIds: this.strSelectIds,
          fileName: "process_"+new Date().getTime()
        }).then(res => {
          this._onUpdate()
          this.checkAll = false
          this.strSelectIds = ''
        }).catch(e => {
          this.strSelectIds = ''
          this.checkAll = false
          this.$message.error(e.msg)
        })
      },

      /**
       * Edit state
       */
      _upProcessState (o) {
        this.editProcessState(o).then(res => {
          this.$message.success(res.msg)
          $('body').find('.tooltip.fade.top.in').remove()
          this._onUpdate()
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      _onUpdate () {
        this.$emit('on-update')
      },
      /**
       * click the select-all checkbox
       */
      _topCheckBoxClick (is) {
        _.map(this.list , v => v.isCheck = v.releaseState === 'ONLINE' ? false : is)
        this._arrDelChange()
      },
      /**
       * the array that to be delete
       */
      _arrDelChange (v) {
        let arr = []
        this.list.forEach((item)=>{
          if (item.isCheck) {
            arr.push(item.id)
          }
        })
        this.strSelectIds = _.join(arr, ',')
        if (v === false) {
          this.checkAll = false
        }
      },
      /**
       * batch delete
       */
      _batchDelete () {
        this.$refs['poptipDeleteAll'].doClose()
        this.batchDeleteDefinition({
          processDefinitionIds: this.strSelectIds
        }).then(res => {
          this._onUpdate()
          this.checkAll = false
          this.strSelectIds = ''
          this.$message.success(res.msg)
        }).catch(e => {
          this.strSelectIds = ''
          this.checkAll = false
          this.$message.error(e.msg || '')
        })
      }
    },
    watch: {
      processList: {
        handler (a) {
          this.checkAll = false
          this.list = []
          setTimeout(() => {
            this.list = _.cloneDeep(a)
          })
        },
        immediate: true,
        deep: true
      },
      pageNo () {
        this.strSelectIds = ''
      }
    },
    created () {
    },
    mounted () {
    },
    components: { }
  }
</script>
