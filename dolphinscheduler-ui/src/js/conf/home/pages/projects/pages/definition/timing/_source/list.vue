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
          <el-table :data="list" size="mini" style="width: 100%">
            <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
            <el-table-column prop="processDefinitionName" :label="$t('Process Name')"></el-table-column>
            <el-table-column :label="$t('Start Time')" min-width="120">
              <template slot-scope="scope">
                <span>{{scope.row.startTime | formatDate}}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('End Time')" min-width="120">
              <template slot-scope="scope">
                <span>{{scope.row.endTime | formatDate}}</span>
              </template>
            </el-table-column>
            <el-table-column prop="crontab" :label="$t('crontab')"></el-table-column>
            <el-table-column prop="failureStrategy" :label="$t('Failure Strategy')"></el-table-column>
            <el-table-column :label="$t('State')">
              <template slot-scope="scope">
                <span>{{_rtReleaseState(scope.row.releaseState)}}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('Create Time')" min-width="120">
              <template slot-scope="scope">
                <span>{{scope.row.createTime | formatDate}}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('Update Time')" min-width="120">
              <template slot-scope="scope">
                <span>{{scope.row.updateTime | formatDate}}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('Operation')" width="120">
              <template slot-scope="scope">
                <el-tooltip :content="$t('Edit')" placement="top">
                  <span><el-button type="primary" size="mini" icon="el-icon-edit-outline" :disabled="scope.row.releaseState === 'ONLINE'" @click="_editTiming(scope.row)" circle></el-button></span>
                </el-tooltip>
                <el-tooltip :content="$t('online')" placement="top" v-if="scope.row.releaseState === 'OFFLINE'">
                  <span><el-button type="warning" size="mini" icon="el-icon-upload2" @click="_online(scope.row)" circle></el-button></span>
                </el-tooltip>
                <el-tooltip :content="$t('offline')" placement="top" v-if="scope.row.releaseState === 'ONLINE'">
                  <span><el-button type="danger" size="mini" icon="el-icon-download" @click="_offline(scope.row)" circle></el-button></span>
                </el-tooltip>
                <el-tooltip :content="$t('delete')" placement="top">
                  <el-popconfirm
                    :confirmButtonText="$t('Confirm')"
                    :cancelButtonText="$t('Cancel')"
                    icon="el-icon-info"
                    iconColor="red"
                    :title="$t('Delete?')"
                    @onConfirm="_delete(scope.row,scope.row.id)"
                  >
                    <el-button type="danger" size="mini" icon="el-icon-delete" circle slot="reference"></el-button>
                  </el-popconfirm>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="page-box">
          <el-pagination
            background
            @current-change="_page"
            @size-change="_pageSize"
            :page-size="pageSize"
            :current-page.sync="pageNo"
            :page-sizes="[10, 30, 50]"
            layout="sizes, prev, pager, next, jumper"
            :total="total">
          </el-pagination>
        </div>
      </template>
      <template v-if="!list.length">
        <m-no-data></m-no-data>
      </template>
    </div>
    <m-spin :is-spin="isLoading"></m-spin>
    <el-dialog
      :title="$t('Set parameters before timing')"
      :visible.sync="timingDialog"
      width="auto">
      <m-timing :timingData="timingData" @onUpdateTiming="onUpdateTiming" @closeTiming="closeTiming"></m-timing>
    </el-dialog>
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
        list: [],
        timingDialog: false,
        timingData: {
          item: {}
        }
      }
    },
    props: {
    },
    methods: {
      ...mapActions('dag', ['getScheduleList', 'scheduleOffline', 'scheduleOnline', 'deleteTiming']),
      /**
       * delete
       */
      _delete (item, i) {
        this.deleteTiming({
          scheduleId: item.id
        }).then(res => {
          this.pageNo = 1
          this._getScheduleList('false')
          this.$message.success(res.msg)
        }).catch(e => {
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
       * timing
       */
      _editTiming (item) {
        this.timingData.item = item
        this.timingDialog = true
      },
      onUpdateTiming () {
        this.pageNo = 1
        this._getScheduleList('false')
        this.timingDialog = false
      },
      closeTiming () {
        this.timingDialog = false
      }
    },
    watch: {},
    created () {
      this._getScheduleList()
    },
    mounted () {},
    components: { mSpin, mNoData, mTiming }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
</style>
