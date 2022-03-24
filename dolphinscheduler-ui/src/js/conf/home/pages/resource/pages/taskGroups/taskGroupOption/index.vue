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
  <m-list-construction :title="$t('Task group option')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group" v-if="isADMIN">
          <el-button size="mini" @click="_create()">{{$t('Create task group')}}</el-button>
          <el-dialog
            :title="item && item.name ? $t('Edit task group') : $t('Create task group')"
            :v-if="createTaskGroupDialog"
            :visible.sync="createTaskGroupDialog"
            width="auto">
            <m-create-task-group :item="item" @onUpdate="onUpdate" @close="close"></m-create-task-group>
          </el-dialog>
        </template>
      </m-conditions>
    </template>

    <template slot="content">
      <template v-if="taskGroupList.length || total>0">
        <m-list @on-edit="_onEdit"
                :task-group-list="taskGroupList"
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
  import mCreateTaskGroup from './_source/createTaskGroup'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'task-group-index',
    data () {
      return {
        total: null,
        isLoading: true,
        modalType: 'create',
        taskGroupList: [],
        projectList: [],
        environmentWorkerGroupRelationList: [],
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          searchVal: ''
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
      ...mapActions('projects', ['getProjectsList']),
      ...mapActions('resource', ['getTaskGroupListPaging']),
      /**
       * Query
       */
      _onConditions (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.pageNo = 1
        this.searchParams.name = this.searchParams.searchVal
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
        const projectSearchParams = {
          pageNo: 1,
          pageSize: 2147483647
        }
        if (sessionStorage.getItem('isLeft') === 0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        Promise.all([this.getTaskGroupListPaging(this.searchParams), this.getProjectsList(projectSearchParams)]).then((values) => {
          if (this.searchParams.pageNo > 1 && values[0].totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.taskGroupList = []
            this.taskGroupList = values[0].totalList
            this.total = values[0].total
            this.isLoading = false
          }
          if (values[1] && values[1].totalList) {
            this.projectList = values[1].totalList
          }
          this.taskGroupList.forEach(item => {
            item.status = item.status === 1
            item.projectOptions = this.projectList
            item.projectName = _.find(this.projectList, { code: item.projectCode }).name
          })
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
    components: { mList, mListConstruction, mConditions, mSpin, mNoData, mCreateTaskGroup }
  }
</script>
