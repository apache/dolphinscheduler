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
  <div class="task-definition" v-if="!isLoading">
    <m-list-construction :title="$t('Task Definition')">
      <template slot="conditions">
        <m-conditions @on-conditions="_onConditions">
          <template v-slot:button-group>
            <el-button size="mini" @click="_onCreate">
              {{ $t("Create task") }}
            </el-button>
          </template>
        </m-conditions>
      </template>
      <template v-slot:content>
        <template v-if="tasksList.length || total > 0">
          <m-list :tasksList="tasksList" @on-update="_onUpdate"></m-list>
          <div class="page-box">
            <el-pagination
              background
              @current-change="_page"
              @size-change="_pageSize"
              :page-size="searchParams.pageSize"
              :current-page.sync="searchParams.pageNo"
              :page-sizes="[10, 30, 50]"
              :total="total"
              layout="sizes, prev, pager, next, jumper"
            >
            </el-pagination>
          </div>
        </template>
        <template v-if="!tasksList.length">
          <m-no-data></m-no-data>
        </template>
        <m-spin :is-spin="isLoading"></m-spin>
      </template>
    </m-list-construction>
    <el-drawer :visible.sync="taskDrawer" size="">
      <m-form-model v-if="taskDrawer" :nodeData="nodeData" class="form-model">
      </m-form-model>
    </el-drawer>
  </div>
</template>
<script>
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mConditions from '@/module/components/conditions/conditions'
  import mList from './_source/list'
  import mNoData from '@/module/components/noData/noData'
  import mSpin from '@/module/components/spin/spin'
  import { mapActions, mapMutations } from 'vuex'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mFormModel from '@/conf/home/pages/dag/_source/formModel/formModel.vue'
  /**
   * tasksType
   */
  import { tasksType } from '@/conf/home/pages/dag/_source/config.js'

  const DEFAULT_NODE_DATA = {
    id: null,
    taskType: '',
    self: {},
    instanceId: null
  }
  export default {
    name: 'task-definition-index',
    data () {
      // tasksType
      const tasksTypeList = Object.keys(tasksType)
      return {
        total: null,
        tasksList: [],
        isLoading: true,
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          searchVal: '',
          userId: ''
        },
        // whether the task config drawer is visible
        taskDrawer: false,
        // nodeData
        nodeData: { ...DEFAULT_NODE_DATA },
        // tasksType
        tasksTypeList
      }
    },
    mixins: [listUrlParamHandle],
    methods: {
      ...mapActions('dag', ['getTaskDefinitionsList', 'genTaskCodeList']),
      ...mapActions('dag', [
        'getProcessList',
        'getProjectList',
        'getResourcesList',
        'getResourcesListJar',
        'getResourcesListJar'
      ]),
      ...mapMutations('dag', ['resetParams']),
      ...mapActions('security', [
        'getTenantList',
        'getWorkerGroupsAll',
        'getAlarmGroupsAll'
      ]),
      /**
       * taskCreate
       */
      _onCreate () {},
      /**
       * pageNo
       */
      _page (val) {
        this.searchParams.pageNo = val
      },
      /**
       * pageSize
       */
      _pageSize (val) {
        this.searchParams.pageSize = val
      },
      /**
       * conditions
       */
      _onConditions (o) {
        this.searchParams.searchVal = o.searchVal
        this.searchParams.pageNo = 1
      },
      /**
       * get data list
       */
      _getList (flag) {
        this.isLoading = !flag
        this.getTaskDefinitionsList(this.searchParams)
          .then((res) => {
            if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
              this.searchParams.pageNo = this.searchParams.pageNo - 1
            } else {
              this.tasksList = []
              this.tasksList = res.totalList
              this.total = res.total
              this.isLoading = false
            }
          })
          .catch((e) => {
            this.isLoading = false
          })
      },
      /**
       * update task dataList
       */
      _onUpdate () {
        this._debounceGET('false')
      }
    },
    created () {
      this.isLoading = true
      // Initialization parameters
      this.resetParams()
      // Promise Get node needs data
      Promise.all([
        // get process definition
        this.getProcessList(),
        // get project
        this.getProjectList(),
        // get jar
        this.getResourcesListJar(),
        // get resource
        this.getResourcesList(),
        // get jar
        this.getResourcesListJar(),
        // get worker group list
        this.getWorkerGroupsAll(),
        // get alarm group list
        this.getAlarmGroupsAll(),
        this.getTenantList()
      ])
        .then((data) => {
          this.isLoading = false
        })
        .catch(() => {
          this.isLoading = false
        })
    },
    mounted () {},
    components: {
      mListConstruction,
      mConditions,
      mList,
      mNoData,
      mSpin,
      mFormModel
    }
  }
</script>
<style  lang="scss" scoped>
.task-definition {
  .taskGroupBtn {
    width: 300px;
  }
  .form-model {
    border: 1px solid blue;
  }
}
</style>
