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
  <div>
    <div class="conditions-box">
      <!--<m-conditions @on-conditions="_onConditions"></m-conditions>-->
    </div>
    <div class="list-model" v-if="!isLoading">
      <template v-if="list.length">
        <div class="table-box">
          <table>
            <tr>
              <th>
                <span>{{$t('#')}}</span>
              </th>
              <th>
                <span>{{$t('Process Name')}}</span>
              </th>
              <th>
                <span>{{$t('Start Time')}}</span>
              </th>
              <th>
                <span>{{$t('End Time')}}</span>
              </th>
              <th>
                <span>{{$t('crontab')}}</span>
              </th>
              <th>
                <span>{{$t('Failure Strategy')}}</span>
              </th>
              <th>
                <span>{{$t('State')}}</span>
              </th>
              <th>
                <span>{{$t('Create Time')}}</span>
              </th>
              <th>
                <span>{{$t('Update Time')}}</span>
              </th>
              <th width="120">
                <span>{{$t('Operation')}}</span>
              </th>
            </tr>
            <tr v-for="(item, $index) in list" :key="item.id">
              <td>
                <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
              </td>
              <td>
                <span><a href="javascript:">{{item.processDefinitionName}}</a></span>
              </td>
              <td>
                <span>{{item.startTime | formatDate}}</span>
              </td>
              <td>
                <span>{{item.endTime | formatDate}}</span>
              </td>
              <td>
                <span>{{item.crontab}}</span>
              </td>
              <td>
                <span>{{item.failureStrategy}}</span>
              </td>
              <td>
                <span>{{_rtReleaseState(item.releaseState)}}</span>
              </td>
              <td>
                <span>{{item.createTime | formatDate}}</span>
              </td>
              <td>
                <span>{{item.updateTime | formatDate}}</span>
              </td>
              <td>
                <x-button
                        type="info"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Edit')"
                        @click="_editTiming(item)"
                        icon="ans-icon-edit"
                        :disabled="item.releaseState === 'ONLINE'" >
                </x-button>
                <x-button
                        type="warning"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('online')"
                        @click="_online(item)"
                        icon="ans-icon-upward"
                        v-if="item.releaseState === 'OFFLINE'">
                </x-button>
                <x-button
                        type="error"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('offline')"
                        icon="ans-icon-downward"
                        @click="_offline(item)"
                        v-if="item.releaseState === 'ONLINE'">
                </x-button>
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
              </td>
            </tr>
          </table>
        </div>
        <div class="page-box">
          <x-page :current="pageNo" :total="total" show-elevator @on-change="_page" show-sizer :page-size-options="[10,30,50]" @on-size-change="_pageSize"></x-page>
        </div>
      </template>
      <template v-if="!list.length">
        <m-no-data></m-no-data>
      </template>
    </div>
    <m-spin :is-spin="isLoading"></m-spin>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mSpin from '@/module/components/spin/spin'
  import mTiming from '../../pages/list/_source/timing'
  import mNoData from '@/module/components/noData/noData'
  import { publishStatus } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'list',
    data () {
      return {
        isLoading: false,
        total: null,
        pageNo: 1,
        pageSize: 10,
        list: []
      }
    },
    props: {
    },
    methods: {
      ...mapActions('dag', ['getScheduleList', 'scheduleOffline', 'scheduleOnline', 'getReceiver','deleteTiming']),
      /**
       * delete
       */
      _delete (item, i) {
        this.deleteTiming({
          scheduleId: item.id
        }).then(res => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.success(res.msg)
          this.$router.push({ name: 'projects-definition-list' })
        }).catch(e => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      /**
       * Close the delete layer
       */
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
      /**
       * return state
       */
      _rtReleaseState (code) {
        return _.filter(publishStatus, v => v.code === code)[0].desc
      },
      /**
       * page
       */
      _page (val) {
        this.pageNo = val
        this._getScheduleList()
      },
      _pageSize (val) {
        this.pageSize = val
        this._getScheduleList()
      },
      /**
       * Inquire list
       */
      _getScheduleList (flag) {
        this.isLoading = !flag
        this.getScheduleList({
          processDefinitionId: this.$route.params.id,
          searchVal: '',
          pageNo: this.pageNo,
          pageSize: this.pageSize
        }).then(res => {
          this.list = []
          setTimeout(() => {
            this.list = res.data.totalList
          })
          this.total = res.data.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      },
      /**
       * search
       */
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getScheduleList('false')
      },
      /**
       * online
       */
      _online (item) {
        this.pageNo = 1
        this.scheduleOnline({
          id: item.id
        }).then(res => {
          this.$message.success(res.msg)
          this._getScheduleList('false')
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * offline
       */
      _offline (item) {
        this.pageNo = 1
        this.scheduleOffline({
          id: item.id
        }).then(res => {
          this.$message.success(res.msg)
          this._getScheduleList('false')
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * get email
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
      _editTiming (item) {
        let self = this
        this._getReceiver(item.processDefinitionId).then(res => {
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
                    self.pageNo = 1
                    self._getScheduleList('false')
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  item: item,
                  receiversD: res.receivers,
                  receiversCcD: res.receiversCc
                }
              })
            }
          })
        })
      }
    },
    watch: {},
    created () {
      this._getScheduleList()
    },
    mounted () {},
    components: { mSpin, mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
</style>
