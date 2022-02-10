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
  <m-list-construction :title="$t('Task group queue')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="search-group">
          <div class="list">
            <el-button size="mini" @click="_ckQuery" icon="el-icon-search"></el-button>
          </div>
          <div class="list">
            <el-input v-model="instanceName" style="width: 140px;" size="mini" :placeholder="$t('Process Instance')" clearable></el-input>
          </div>
          <div class="list">
            <el-input v-model="processName" style="width: 160px;" size="mini" :placeholder="$t('Process Name')" clearable></el-input>
          </div>
          <div class="list">
            <el-select style="width: 140px;" v-model="groupId" :placeholder="$t('Task group name')" size="mini" clearable>
              <el-option
                v-for="taskGroup in taskGroupList"
                :key="taskGroup.id"
                :value="taskGroup.id"
                :label="taskGroup.name">
              </el-option>
            </el-select>
          </div>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="taskGroupQueue.length || total>0">
        <m-list @on-edit="_onEdit"
                @on-force-start="_onForceStart"
                @on-edit-priority="_onEditPriority"
                :task-group-queue="taskGroupQueue"
                :page-no="searchParams.pageNo"
                :page-size="searchParams.pageSize">

        </m-list>
        <div class="page-box">
          <el-pagination
            background
            @current-change="_page"
            @size-change="_pageSize"
            :page-size="searchParams.pageSize"
            :current-page.sync="searchParams.pageNo"
            :page-sizes="[10, 30, 50]"
            layout="sizes, prev, pager, next, jumper"
            :total="total">
          </el-pagination>
        </div>
      </template>
      <template v-if="!taskGroupList.length && total<=0">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" :is-left="isLeft"></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import store from '@/conf/home/store'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'task-group-queue-index',
    data () {
      return {
        total: null,
        isLoading: true,
        modalType: 'create',
        taskGroupList: [],
        taskGroupQueue: [],
        groupId: '',
        instanceName: '',
        processName: '',
        searchParams: {
          pageSize: 10,
          pageNo: 1
        },
        isLeft: true,
        isADMIN: store.state.user.userInfo.userType === 'ADMIN_USER',
        item: {},
        createTaskGroupDialog: false
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('resource', ['getTaskListInTaskGroupQueueById']),
      ...mapActions('resource', ['getTaskGroupListPaging']),
      /**
       * Query
       */
      _onConditions (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.pageNo = 1
      },
      _ckQuery () {
        this.searchParams.groupId = this.groupId
        this.searchParams.instanceName = this.instanceName
        this.searchParams.processName = this.processName
        this._getList(false)
      },
      _page (val) {
        this.searchParams.pageNo = val
      },
      _pageSize (val) {
        this.searchParams.pageSize = val
      },
      _onEdit (item) {
        this.item = item
        this.item.modalType = 'edit'
        this.createTaskGroupDialog = true
      },
      _onForceStart (item) {
        this._getList(false)
      },
      _onEditPriority () {
        this._getList(false)
      },
      _create () {
        this.item = { projectOptions: this.projectList, modalType: 'create' }
        this.createTaskGroupDialog = true
      },
      onUpdate () {
        this._debounceGET('false')
        this.createTaskGroupDialog = false
      },
      close () {
        this.createTaskGroupDialog = false
      },
      _getList (flag) {
        const taskGroupSearchParams = {
          pageNo: 1,
          pageSize: 2147483647
        }
        if (sessionStorage.getItem('isLeft') === 0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        this.getTaskGroupListPaging(taskGroupSearchParams).then((values) => {
          this.taskGroupList = []
          this.taskGroupList = values.totalList
          if (this.taskGroupList) {
            if (this.groupId) {
              this.searchParams.groupId = _.parseInt(this.groupId)
            } else {
              this.searchParams.groupId = _.parseInt(this.searchParams.id)
              this.groupId = this.searchParams.groupId
            }
            this.getTaskListInTaskGroupQueueById(this.searchParams).then((res) => {
              if (this.searchParams.pageNo > 1 && values.totalList.length === 0) {
                this.searchParams.pageNo = this.searchParams.pageNo - 1
              } else {
                this.taskGroupQueue = []
                if (res.data.totalList) {
                  this.taskGroupQueue = res.data.totalList
                }
                this.taskGroupQueue.forEach(item => {
                  const taskGroup = _.find(this.taskGroupList, { id: item.groupId })
                  if (taskGroup) {
                    item.taskGroupName = taskGroup.name
                  }
                })
                this.total = res.data.total
                this.isLoading = false
              }
            }).catch(e => {
              this.isLoading = false
            })
          }
        }).catch(e => {
          this.isLoading = false
        })
      }
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
        this.searchParams.pageNo = _.isEmpty(a.query) ? 1 : a.query.pageNo
      }
    },
    beforeDestroy () {
      sessionStorage.setItem('isLeft', 1)
    },
    components: { mList, mListConstruction, mConditions, mSpin, mNoData }
  }
</script>
