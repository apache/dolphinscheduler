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
    <div class="list-model" style="position: relative;">
      <div class="table-box">
        <el-table class="fixed" :data="list" size="mini" style="width: 100%">
          <el-table-column prop="id" :label="$t('#')" width="50"></el-table-column>
          <el-table-column :label="$t('Process Name')" min-width="120">
            <template slot-scope="scope">
              <el-popover trigger="hover" placement="top">
                <p>{{ scope.row.processDefinitionName }}</p>
                <div slot="reference" class="name-wrapper">
                  <router-link :to="{ path: `/projects/${projectCode}/definition/list/${scope.row.code}` }" tag="a" class="links">
                    <span class="ellipsis">{{scope.row.processDefinitionName}}</span>
                  </router-link>
                </div>
              </el-popover>
            </template>
          </el-table-column>
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
              <el-tooltip :content="$t('Delete')" placement="top">
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
  import { mapActions, mapState } from 'vuex'
  import mSpin from '@/module/components/spin/spin'
  import mTiming from '../../pages/list/_source/timing'
  import { publishStatus } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'timing-list',
    data () {
      return {
        isLoading: false,
        // data
        list: [],
        // btn type
        buttonType: '',
        strDelete: '',
        timingDialog: false,
        timingData: {
          item: {}
        }
      }
    },
    props: {
      scheduleList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('dag', ['scheduleOffline', 'scheduleOnline', 'getReceiver', 'deleteTiming']),
      /**
       * delete
       */
      _delete (item, i) {
        this.deleteTiming({
          scheduleId: item.id
        }).then(res => {
          this._onUpdate()
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.success(res.msg)
          // this.$router.push({ name: 'projects-definition-list' })
        }).catch(e => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      /**
       * Update
       */
      _onUpdate () {
        this.$emit('on-update')
      },
      /**
       * return state
       */
      _rtReleaseState (code) {
        return _.filter(publishStatus, v => v.code === code)[0].desc
      },
      /**
       * online
       */
      _online (item) {
        this.scheduleOnline({
          id: item.id
        }).then(res => {
          this.$message.success(res.msg)
          this._onUpdate()
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * offline
       */
      _offline (item) {
        this.scheduleOffline({
          id: item.id
        }).then(res => {
          this.$message.success(res.msg)
          this._onUpdate()
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
        this.timingData.item = item
        this.timingDialog = true
      },
      onUpdateTiming () {
        this.pageNo = 1
        // this._getScheduleList('false')
        this.timingDialog = false
        this._onUpdate()
      },
      closeTiming () {
        this.timingDialog = false
      }
    },
    watch: {
      scheduleList: {
        handler (a) {
          this.list = []
          setTimeout(() => {
            this.list = _.cloneDeep(a)
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
    computed: {
      ...mapState('dag', ['projectCode'])
    },
    components: { mSpin, mTiming }
  }
</script>
