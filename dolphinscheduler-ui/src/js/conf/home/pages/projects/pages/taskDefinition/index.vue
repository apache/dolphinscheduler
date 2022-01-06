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
        <m-conditions>
          <template v-slot:button-group>
            <el-button size="mini" @click="createTask">
              {{ $t("Create task") }}
            </el-button>
          </template>
          <template v-slot:search-group>
            <div class="list">
              <el-button
                size="mini"
                @click="_ckQuery"
                icon="el-icon-search"
              ></el-button>
            </div>
            <div class="list">
              <el-select
                size="mini"
                style="width: 140px"
                :placeholder="$t('type')"
                :value="tempParams.taskType"
                @change="_onChangeTaskType"
                clearable
              >
                <el-option
                  v-for="taskType in tasksTypeList"
                  :key="taskType"
                  :value="taskType"
                  :label="taskType"
                >
                </el-option>
              </el-select>
            </div>
            <div class="list">
              <el-input
                v-model="tempParams.processName"
                @keyup.enter.native="_ckQuery"
                size="mini"
                :placeholder="$t('Process Name')"
                type="text"
                style="width: 180px"
                clearable
              />
            </div>
            <div class="list">
              <el-input
                v-model="tempParams.taskName"
                @keyup.enter.native="_ckQuery"
                size="mini"
                :placeholder="$t('Task Name')"
                type="text"
                style="width: 180px"
                clearable
              />
            </div>
          </template>
        </m-conditions>
      </template>
      <template v-slot:content>
        <template v-if="tasksList.length || total > 0">
          <m-list
            :tasksList="tasksList"
            @on-update="_onUpdate"
            @editTask="editTask"
            @showDeleteModal="showDeleteModal"
            @showMoveModal="showMoveModal"
            @viewTaskDetail="viewTaskDetail"
            @viewTaskVersions="viewTaskVersions"
          ></m-list>
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
    <el-drawer
      :visible.sync="taskDrawer"
      size=""
      :with-header="false"
      @close="closeTaskDrawer"
    >
      <!-- fix the bug that Element-ui(2.13.2) auto focus on the first input -->
      <div style="width: 0px; height: 0px; overflow: hidden">
        <el-input type="text" />
      </div>
      <m-form-model
        v-if="taskDrawer"
        :nodeData="nodeData"
        type="task-definition"
        @changeTaskType="changeTaskType"
        @close="closeTaskDrawer"
        @addTaskInfo="saveTask"
        :taskDefinition="editingTask"
      >
      </m-form-model>
    </el-drawer>
    <task-delete-modal
      ref="taskDeleteModal"
      :taskRow="deletingTaskRow"
      @deleteTask="deleteTask"
    />
    <task-move-modal
      ref="taskMoveModal"
      :taskRow="movingTaskRow"
      @moveTask="moveTask"
    />
    <version-drawer
      ref="versionDrawer"
      :taskRow="versionTaskRow"
      @reloadList="_onUpdate"
    />
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
  import { tasksType } from '@/conf/home/pages/dag/_source/config.js'
  import TaskDeleteModal from './_source/taskDeleteModal.vue'
  import TaskMoveModal from './_source/taskMoveModel.vue'
  import VersionDrawer from './_source/versions.vue'
  import _ from 'lodash'

  const DEFAULT_NODE_DATA = {
    id: -1,
    taskType: 'SHELL',
    instanceId: -1
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
          processName: '',
          taskName: '',
          taskType: ''
        },
        tempParams: {
          processName: '',
          taskName: '',
          taskType: ''
        },
        // whether the task config drawer is visible
        taskDrawer: false,
        // nodeData
        nodeData: { ...DEFAULT_NODE_DATA },
        // tasksType
        tasksTypeList,
        // editing task definition
        editingTask: null,
        editingProcess: null,
        // task to be deleted
        deletingTaskRow: null,
        // task ready to move
        movingTaskRow: null,
        // the current browse task
        versionTaskRow: null
      }
    },
    mixins: [listUrlParamHandle],
    methods: {
      ...mapActions('dag', [
        'getTaskDefinitionsList',
        'genTaskCodeList',
        'saveTaskDefinitionWithUpstreams',
        'updateTaskDefinition',
        'deleteTaskDefinition',
        'getTaskDefinition',
        'moveTaskToProcess',
        'deleteRelation'
      ]),
      ...mapActions('dag', [
        'getProcessList',
        'getProjectList',
        'getResourcesList'
      ]),
      ...mapMutations('dag', ['resetParams', 'setIsDetails']),
      ...mapActions('security', [
        'getTenantList',
        'getWorkerGroupsAll',
        'getAlarmGroupsAll'
      ]),
      /**
       * Toggle task form-model drawer
       */
      showTaskDrawer () {
        this.taskDrawer = true
      },
      closeTaskDrawer () {
        this.setIsDetails(false)
        this.taskDrawer = false
      },
      /**
       * Save task
       */
      saveTask ({ item: taskDefinition, prevTasks, processCode }) {
        const isEditing = !!this.editingTask
        if (isEditing) {
          this.updateTaskDefinition({
            prevTasks: prevTasks,
            taskDefinition: taskDefinition
          })
            .then((res) => {
              this.$message.success(res.msg)
              this._onUpdate()
              this.closeTaskDrawer()
            })
            .catch((e) => {
              this.$message.error(e.msg || '')
            })
        } else {
          this.genTaskCodeList({
            genNum: 1
          })
            .then((res) => {
              const [code] = res
              return code
            })
            .then((code) => {
              return this.saveTaskDefinitionWithUpstreams({
                taskDefinition: {
                  ...taskDefinition,
                  code
                },
                prevTasks: prevTasks,
                processDefinitionCode: processCode
              })
            })
            .then((res) => {
              this.$message.success(res.msg)
              this._onUpdate()
              this.closeTaskDrawer()
            })
            .catch((e) => {
              this.$message.error(e.msg || '')
            })
        }
      },
      /**
       * Show task creation modal
       */
      createTask () {
        this.editingTask = null
        this.nodeData.taskType = DEFAULT_NODE_DATA.taskType
        this.showTaskDrawer()
      },
      /**
       * Show task edit modal
       */
      editTask (taskRow) {
        this.getTaskDefinition(taskRow.taskCode).then((taskDefinition) => {
          this.editingTask = {
            ...taskDefinition,
            processCode: taskRow.processDefinitionCode
          }
          this.nodeData.id = taskDefinition.code
          this.nodeData.taskType = taskDefinition.taskType
          this.showTaskDrawer()
        })
      },
      /**
       * Show task detail modal
       */
      viewTaskDetail (taskRow) {
        this.setIsDetails(true)
        this.editTask(taskRow)
      },
      /**
       * Show delete task modal
       */
      showDeleteModal (taskRow) {
        this.deletingTaskRow = taskRow
        if (this.$refs.taskDeleteModal) {
          this.$refs.taskDeleteModal.show()
        }
      },
      /**
       * Show Move Modal
       */
      showMoveModal (taskRow) {
        this.movingTaskRow = taskRow
        if (this.$refs.taskMoveModal) {
          this.$refs.taskMoveModal.show()
        }
      },
      /**
       * Delete task
       * @param {Boolean} completely Whether to delete the task completely
       */
      deleteTask ({ completely, taskCode, processDefinitionCode }) {
        const completelyDelete = this.deleteTaskDefinition
        const deleteRelation = this.deleteRelation
        const delRequest = completely ? completelyDelete : deleteRelation
        const params = completely
          ? { taskCode }
          : {
            taskCode,
            processDefinitionCode
          }
        delRequest(params)
          .then((res) => {
            this.$message.success(res.msg)
            this.$refs.taskDeleteModal.close()
            this.deletingTaskRow = null
            this._onUpdate()
          })
          .catch((err) => {
            this.$message.error(err.msg || '')
          })
      },
      /**
       * Move task to another workflow
       */
      moveTask (params) {
        this.moveTaskToProcess(params)
          .then((res) => {
            this.$message.success(res.msg)
            this.$refs.taskMoveModal.close()
            this.movingTaskRow = null
            this._onUpdate()
          })
          .catch((err) => {
            this.$message.error(err.msg || '')
          })
      },
      /**
       * ViewTaskVersion
       */
      viewTaskVersions (taskRow) {
        if (this.$refs.versionDrawer) {
          this.versionTaskRow = taskRow
          this.$refs.versionDrawer.show()
        }
      },
      /**
       * pageNo
       */
      _page (val) {
        this.searchParams.pageNo = val
      },
      _pageSize (val) {
        this.searchParams.pageSize = val
      },
      /**
       * query tasks
       */
      _ckQuery (o) {
        this.searchParams.processName = this.tempParams.processName
        this.searchParams.taskType = this.tempParams.taskType
        this.searchParams.taskName = this.tempParams.taskName
        this.searchParams.pageNo = 1
      },
      /**
       * filter tasks by taskType
       */
      _onChangeTaskType (val) {
        this.tempParams.taskType = val
      },
      /**
       * get task definition list
       */
      _getList (flag) {
        this.isLoading = !flag
        this.getTaskDefinitionsList({
          pageNo: this.searchParams.pageNo,
          pageSize: this.searchParams.pageSize,
          taskType: this.searchParams.taskType,
          searchTaskName: this.searchParams.taskName,
          searchWorkflowName: this.searchParams.processName
        })
          .then((res) => {
            if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
              this.searchParams.pageNo = this.searchParams.pageNo - 1
            } else {
              this.tasksList = res.totalList.map((task) => {
                const upstreamTaskMap = task.upstreamTaskMap || {}
                const upstreamTasks = Object.keys(upstreamTaskMap).map((code) => {
                  return {
                    taskCode: code,
                    taskName: upstreamTaskMap[code]
                  }
                })
                return {
                  ...task,
                  upstreamTasks,
                  upstreamTaskNames: upstreamTasks
                    .map((u) => u.taskName)
                    .join(',')
                }
              })
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
      },
      /**
       * change form modal task type
       */
      changeTaskType (value) {
        this.nodeData.taskType = value
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
        // get resource
        this.getResourcesList(),
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
      // Routing parameter merging
      if (!_.isEmpty(this.$route.query)) {
        this.tempParams.processName = this.$route.query.processName || ''
        this.tempParams.taskType = this.$route.query.taskType || ''
        this.tempParams.taskName = this.$route.query.taskName || ''
      }
    },
    components: {
      mListConstruction,
      mConditions,
      mList,
      mNoData,
      mSpin,
      mFormModel,
      TaskMoveModal,
      TaskDeleteModal,
      VersionDrawer
    }
  }
</script>
<style  lang="scss" scoped>
.task-definition {
  .taskGroupBtn {
    width: 300px;
  }

  ::v-deep .table-box {
    table {
      tr {
        th:first-child,
        td:first-child {
          text-align: left;
          padding-left: 20px;
        }
        td:first-child span {
          text-align: left;
        }
      }
      td,
      th.is-leaf {
        padding-left: 10px;
      }
    }
    .pre-task-tag {
      margin-right: 10px;
      max-width: 100px;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .upstream-tasks {
      display: flex;
      flex-wrap: wrap;
    }
  }

  ::v-deep .el-dialog__header {
    .el-dialog__headerbtn {
      right: 20px;
    }
  }
}
</style>
