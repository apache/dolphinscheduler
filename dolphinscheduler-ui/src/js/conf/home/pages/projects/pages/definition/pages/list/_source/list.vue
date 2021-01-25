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
      <el-table :data="list" size="mini" style="width: 100%" @selection-change="_arrDelChange">
        <el-table-column type="selection" width="50" :selectable="selectable"></el-table-column>
        <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
        <el-table-column :label="$t('Process Name')" min-width="200">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.name }}</p>
              <div slot="reference" class="name-wrapper">
                <router-link :to="{ path: '/projects/definition/list/' + scope.row.id}" tag="a" class="links" :title="scope.row.name">
                  {{scope.row.name}}
                </router-link>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column :label="$t('State')">
          <template slot-scope="scope">
            {{_rtPublishStatus(scope.row.releaseState)}}
          </template>
        </el-table-column>
        <el-table-column :label="$t('Create Time')" width="135">
          <template slot-scope="scope">
            <span>{{scope.row.createTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Update Time')" width="135">
          <template slot-scope="scope">
            <span>{{scope.row.updateTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Description')">
          <template slot-scope="scope">
            <span>{{scope.row.description | filterNull}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="modifyBy" :label="$t('Modify User')"></el-table-column>
        <el-table-column :label="$t('Timing state')">
          <template slot-scope="scope">
            <span v-if="scope.row.scheduleReleaseState === 'OFFLINE'">{{$t('offline')}}</span>
            <span v-if="scope.row.scheduleReleaseState === 'ONLINE'">{{$t('online')}}</span>
            <span v-if="!scope.row.scheduleReleaseState">-</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="335" fixed="right">
          <template slot-scope="scope">
            <el-tooltip :content="$t('Edit')" placement="top" :enterable="false">
              <el-button type="primary" size="mini" icon="el-icon-edit-outline" :disabled="scope.row.releaseState === 'ONLINE'" @click="_edit(scope.row)" circle></el-button>
            </el-tooltip>
            <el-tooltip :content="$t('Start')" placement="top" :enterable="false">
              <span><el-button type="success" size="mini" :disabled="scope.row.releaseState !== 'ONLINE'"  icon="el-icon-video-play" @click="_start(scope.row)" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('Timing')" placement="top" :enterable="false">
              <el-button type="primary" size="mini" icon="el-icon-time" :disabled="scope.row.releaseState !== 'ONLINE' || scope.row.scheduleReleaseState !== null" @click="_timing(scope.row)" circle></el-button>
            </el-tooltip>
            <el-tooltip :content="$t('online')" placement="top" :enterable="false">
              <span><el-button type="warning" size="mini" v-if="scope.row.releaseState === 'OFFLINE'"  icon="el-icon-upload2" @click="_poponline(scope.row)" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('offline')" placement="top" :enterable="false">
              <el-button type="danger" size="mini" icon="el-icon-download" v-if="scope.row.releaseState === 'ONLINE'" @click="_downline(scope.row)" circle></el-button>
            </el-tooltip>
            <el-tooltip :content="$t('Copy Workflow')" placement="top" :enterable="false">
              <span><el-button type="primary" size="mini" :disabled="scope.row.releaseState === 'ONLINE'"  icon="el-icon-document-copy" @click="_copyProcess(scope.row)" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('Cron Manage')" placement="top" :enterable="false">
              <el-button type="primary" size="mini" icon="el-icon-date" :disabled="scope.row.releaseState !== 'ONLINE'" @click="_timingManage(scope.row)" circle></el-button>
            </el-tooltip>
            <el-tooltip :content="$t('delete')" placement="top" :enterable="false">
              <el-popconfirm
                :confirmButtonText="$t('Confirm')"
                :cancelButtonText="$t('Cancel')"
                icon="el-icon-info"
                iconColor="red"
                :title="$t('Delete?')"
                @onConfirm="_delete(scope.row,scope.row.id)"
              >
                <el-button type="danger" size="mini" icon="el-icon-delete" :disabled="scope.row.releaseState === 'ONLINE'" circle slot="reference"></el-button>
              </el-popconfirm>
            </el-tooltip>
            <el-tooltip :content="$t('TreeView')" placement="top" :enterable="false">
              <el-button type="primary" size="mini" icon="el-icon-s-data" @click="_treeView(scope.row)" circle></el-button>
            </el-tooltip>
            <el-tooltip :content="$t('Export')" placement="top" :enterable="false">
              <span><el-button type="primary" size="mini" icon="el-icon-s-unfold" @click="_export(scope.row)" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('Version Info')" placement="top" :enterable="false">
              <el-button type="primary" size="mini" icon="el-icon-info" :disabled="scope.row.releaseState === 'ONLINE'" @click="_version(scope.row)" circle></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-tooltip :content="$t('delete')" placement="top">
      <el-popconfirm
        :confirmButtonText="$t('Confirm')"
        :cancelButtonText="$t('Cancel')"
        :title="$t('Delete?')"
        @onConfirm="_delete({},-1)"
      >
        <el-button style="position: absolute; bottom: -48px; left: 19px;"  type="primary" size="mini" :disabled="!strSelectIds" slot="reference">{{$t('Delete')}}</el-button>
      </el-popconfirm>
    </el-tooltip>
    <el-button type="primary" size="mini" :disabled="!strSelectIds" style="position: absolute; bottom: -48px; left: 80px;" @click="_batchExport(item)" >{{$t('Export')}}</el-button>
    <span><el-button type="primary" size="mini" :disabled="!strSelectIds" style="position: absolute; bottom: -48px; left: 140px;" @click="_batchCopy(item)" >{{$t('Batch copy')}}</el-button></span>
    <el-button type="primary" size="mini" :disabled="!strSelectIds" style="position: absolute; bottom: -48px; left: 225px;" @click="_batchMove(item)" >{{$t('Batch move')}}</el-button>
    <el-drawer
      :visible.sync="drawer"
      size=""
      :with-header="false">
      <m-versions :versionData = versionData @mVersionSwitchProcessDefinitionVersion="mVersionSwitchProcessDefinitionVersion" @mVersionGetProcessDefinitionVersionsPage="mVersionGetProcessDefinitionVersionsPage" @mVersionDeleteProcessDefinitionVersion="mVersionDeleteProcessDefinitionVersion" @closeVersion="closeVersion"></m-versions>
    </el-drawer>
    <el-dialog
      :title="$t('Please set the parameters before starting')"
      :visible.sync="startDialog"
      width="auto">
      <m-start :startData= "startData" @onUpdateStart="onUpdateStart" @closeStart="closeStart"></m-start>
    </el-dialog>
    <el-dialog
      :title="$t('Set parameters before timing')"
      :visible.sync="timingDialog"
      width="auto">
      <m-timing :timingData="timingData" @onUpdateTiming="onUpdateTiming" @closeTiming="closeTiming"></m-timing>
    </el-dialog>
    <el-dialog
      :title="$t('Info')"
      :visible.sync="relatedItemsDialog"
      width="auto">
      <m-related-items :tmp="tmp" @onBatchCopy="onBatchCopy" @onBatchMove="onBatchMove" @closeRelatedItems="closeRelatedItems"></m-related-items>
    </el-dialog>
  </div>
</template>
<script>
  import _ from 'lodash'
  import mStart from './start'
  import mTiming from './timing'
  import mRelatedItems from './relatedItems'
  import { mapActions } from 'vuex'
  import { publishStatus } from '@/conf/home/pages/dag/_source/config'
  import mVersions from './versions'

  export default {
    name: 'definition-list',
    data () {
      return {
        list: [],
        strSelectIds: '',
        checkAll: false,
        drawer: false,
        versionData: {
          processDefinition: {},
          processDefinitionVersions: [],
          total: null,
          pageNo: null,
          pageSize: null
        },
        startDialog: false,
        startData: {},
        timingDialog: false,
        timingData: {
          item: {},
          type: ''
        },
        relatedItemsDialog: false,
        tmp: false
      }
    },
    props: {
      processList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('dag', ['editProcessState', 'getStartCheck', 'deleteDefinition', 'batchDeleteDefinition', 'exportDefinition', 'getProcessDefinitionVersionsPage', 'copyProcess', 'switchProcessDefinitionVersion', 'deleteProcessDefinitionVersion', 'moveProcess']),
      ...mapActions('security', ['getWorkerGroupsAll']),

      selectable (row, index) {
        if (row.releaseState === 'ONLINE') {
          return false
        } else {
          return true
        }
      },
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
          this.startData = item
          this.startDialog = true
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      onUpdateStart () {
        this._onUpdate()
        this.startDialog = false
      },
      closeStart () {
        this.startDialog = false
      },
      /**
       * timing
       */
      _timing (item) {
        this.timingData.item = item
        this.timingData.type = 'timing'
        this.timingDialog = true
      },
      onUpdateTiming () {
        this._onUpdate()
        this.timingDialog = false
      },
      closeTiming () {
        this.timingDialog = false
      },
      /**
       * Timing manage
       */
      _timingManage (item) {
        this.$router.push({ path: `/projects/definition/list/timing/${item.id}` })
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
          this._onUpdate()
          this.$message.success(res.msg)
        }).catch(e => {
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
          releaseState: 'OFFLINE'
        })
      },
      /**
       * online
       */
      _poponline (item) {
        this._upProcessState({
          processId: item.id,
          releaseState: 'ONLINE'
        })
      },
      /**
       * copy
       */
      _copyProcess (item) {
        this.copyProcess({
          processDefinitionIds: item.id,
          targetProjectId: item.projectId
        }).then(res => {
          this.strSelectIds = ''
          this.$message.success(res.msg)
          // $('body').find('.tooltip.fade.top.in').remove()
          this._onUpdate()
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },

      /**
       * move
       */
      _moveProcess (item) {
        this.moveProcess({
          processDefinitionIds: item.id,
          targetProjectId: item.projectId
        }).then(res => {
          this.strSelectIds = ''
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
      /**
        * switch version in process definition version list
        *
        * @param version the version user want to change
        * @param processDefinitionId the process definition id
        * @param fromThis fromThis
      */
      mVersionSwitchProcessDefinitionVersion ({ version, processDefinitionId, fromThis }) {
        this.switchProcessDefinitionVersion({
          version: version,
          processDefinitionId: processDefinitionId
        }).then(res => {
          this.$message.success($t('Switch Version Successfully'))
          this.$router.push({ path: `/projects/definition/list/${processDefinitionId}` })
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
        * Paging event of process definition versions
        *
        * @param pageNo page number
        * @param pageSize page size
        * @param processDefinitionId the process definition id of page version
        * @param fromThis fromThis
      */
      mVersionGetProcessDefinitionVersionsPage ({ pageNo, pageSize, processDefinitionId, fromThis }) {
        this.getProcessDefinitionVersionsPage({
          pageNo: pageNo,
          pageSize: pageSize,
          processDefinitionId: processDefinitionId
        }).then(res => {
          this.versionData.processDefinitionVersions = res.data.lists
          this.versionData.total = res.data.totalCount
          this.versionData.pageSize = res.data.pageSize
          this.versionData.pageNo = res.data.currentPage
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
        * delete one version of process definition
        *
        * @param version the version need to delete
        * @param processDefinitionId the process definition id user want to delete
        * @param fromThis fromThis
      */
      mVersionDeleteProcessDefinitionVersion ({ version, processDefinitionId, fromThis }) {
        this.deleteProcessDefinitionVersion({
          version: version,
          processDefinitionId: processDefinitionId
        }).then(res => {
          this.$message.success(res.msg || '')
          this.mVersionGetProcessDefinitionVersionsPage({
            pageNo: 1,
            pageSize: 10,
            processDefinitionId: processDefinitionId,
            fromThis: fromThis
          })
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      _version (item) {
        this.getProcessDefinitionVersionsPage({
          pageNo: 1,
          pageSize: 10,
          processDefinitionId: item.id
        }).then(res => {
          let processDefinitionVersions = res.data.lists
          let total = res.data.totalCount
          let pageSize = res.data.pageSize
          let pageNo = res.data.currentPage

          this.versionData.processDefinition = item
          this.versionData.processDefinitionVersions = processDefinitionVersions
          this.versionData.total = total
          this.versionData.pageNo = pageNo
          this.versionData.pageSize = pageSize
          this.drawer = true
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },

      closeVersion () {
        this.drawer = false
      },

      _batchExport () {
        this.exportDefinition({
          processDefinitionIds: this.strSelectIds,
          fileName: 'process_' + new Date().getTime()
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
       * Batch Copy
       */
      _batchCopy () {
        this.relatedItemsDialog = true
        this.tmp = false
      },
      onBatchCopy (item) {
        this._copyProcess({ id: this.strSelectIds, projectId: item })
        this.relatedItemsDialog = false
      },
      closeRelatedItems () {
        this.relatedItemsDialog = false
      },
      /**
       * _batchMove
       */
      _batchMove () {
        this.tmp = true
        this.relatedItemsDialog = true
      },
      onBatchMove (item) {
        this._moveProcess({ id: this.strSelectIds, projectId: item })
        this.relatedItemsDialog = false
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
       * the array that to be delete
       */
      _arrDelChange (v) {
        let arr = []
        arr = _.map(v, 'id')
        this.strSelectIds = _.join(arr, ',')
      },
      /**
       * batch delete
       */
      _batchDelete () {
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
    components: { mVersions, mStart, mTiming, mRelatedItems }
  }
</script>
