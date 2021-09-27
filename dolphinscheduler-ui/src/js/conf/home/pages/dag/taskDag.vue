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
  <div :class="['dag-chart', fullScreen ? 'full-screen' : '']">
    <dag-toolbar :source="source" />
    <task-table ref="canvas" />
    <el-drawer
      :visible.sync="taskDrawer"
      size=""
      :with-header="false"
      :wrapperClosable="false"
    >
      <!-- fix the bug that Element-ui(2.13.2) auto focus on the first input -->
      <div style="width:0px;height:0px;overflow:hidden;">
        <el-input type="text" />
      </div>
      <m-form-model
        v-if="taskDrawer"
        :nodeData="nodeData"
        @seeHistory="seeHistory"
        @addTaskInfo="addTaskInfo"
        @close="closeTaskDrawer"
        @onSubProcess="toSubProcess"
        modelType="createTask"
      ></m-form-model>
    </el-drawer>
    <el-dialog
      :title="$t('Please set the parameters before starting')"
      :visible.sync="startDialog"
      width="auto"
    >
      <m-start
        :startData="{ code: definitionCode, name: name }"
        :startNodeList="startTaskName"
        :sourceType="'contextmenu'"
        @onUpdateStart="onUpdateStart"
        @closeStart="closeStart"
      ></m-start>
    </el-dialog>
    <edge-edit-model ref="edgeEditModel" />
    <el-drawer :visible.sync="versionDrawer" size="" :with-header="false">
      <m-versions
        :versionData="versionData"
        :isInstance="type === 'instance'"
        @mVersionSwitchProcessDefinitionVersion="switchProcessVersion"
        @mVersionGetProcessDefinitionVersionsPage="getProcessVersions"
        @mVersionDeleteProcessDefinitionVersion="deleteProcessVersion"
        @closeVersion="closeVersion"
      ></m-versions>
    </el-drawer>
  </div>
</template>

<script>
  import dagToolbar from './_source/canvas/toolbar.vue'
  import taskTable from './_source/canvas/taskTable.vue'
  import mFormModel from './_source/formModel/formModel.vue'
  import { mapActions, mapState, mapMutations } from 'vuex'
  import mStart from '../projects/pages/definition/pages/list/_source/start.vue'
  import edgeEditModel from './_source/canvas/edgeEditModel.vue'
  import mVersions from '../projects/pages/definition/pages/list/_source/versions.vue'

  const DEFAULT_NODE_DATA = {
    id: null,
    taskType: '',
    self: {},
    instanceId: null
  }

  export default {
    name: 'dag-chart',
    components: {
      dagToolbar,
      mFormModel,
      mStart,
      edgeEditModel,
      mVersions,
      taskTable
    },
    provide () {
      return {
        dagChart: this
      }
    },
    inject: ['definitionDetails'],
    props: {
      releaseState: String,
      source: {
        default: 'process',
        type: String
      }
    },
    data () {
      return {
        definitionCode: 0,
        // full screen mode
        fullScreen: false,
        // whether the task config drawer is visible
        taskDrawer: false,
        nodeData: { ...DEFAULT_NODE_DATA },
        // whether the save dialog is visible
        saveDialog: false,
        // whether the start dialog is visible
        startDialog: false,
        startTaskName: '',
        // whether the version drawer is visible
        versionDrawer: false,
        versionData: {
          processDefinition: {
            id: null,
            version: '',
            releaseState: ''
          },
          processDefinitionVersions: [],
          total: null,
          pageNo: null,
          pageSize: null
        },
        // the task status refresh timer
        statusTimer: null,
        // the process instance id
        instanceId: -1
      }
    },
    mounted () {
      this.setIsEditDag(false)

      if (this.type === 'instance') {
        this.instanceId = this.$route.params.id
        this.definitionCode = this.$route.query.code
      } else if (this.type === 'definition') {
        this.definitionCode = this.$route.params.code
      }

      window.addEventListener('resize', this.resizeDebounceFunc)

      // refresh task status
      if (this.type === 'instance') {
        this.refreshTaskStatus()
        // status polling
        this.statusTimer = setInterval(() => {
          this.refreshTaskStatus()
        }, 90000)
      }
    },
    beforeDestroy () {
      this.resetParams()

      clearInterval(this.statusTimer)
      window.removeEventListener('resize', this.resizeDebounceFunc)
    },
    computed: {
      ...mapState('dag', [
        'tasks',
        'locations',
        'connects',
        'name',
        'isDetails',
        'projectCode',
        'version'
      ])
    },
    methods: {
      ...mapActions('dag', [
        'saveTask',
        'saveDAGchart',
        'updateInstance',
        'updateDefinition',
        'getTaskState',
        'getStartCheck',
        'genTaskCodeList',
        'switchProcessDefinitionVersion',
        'getProcessDefinitionVersionsPage',
        'deleteProcessDefinitionVersion'
      ]),
      ...mapMutations('dag', [
        'addTask',
        'setConnects',
        'resetParams',
        'setIsEditDag',
        'setName',
        'setLocations',
        'resetLocalParam'
      ]),
      /**
       * Task Drawer
       * @param {boolean} visible
       */
      toggleTaskDrawer (visible) {
        this.taskDrawer = visible
      },
      /**
       * Set the current node data
       */
      setNodeData (nodeData) {
        this.nodeData = Object.assign(DEFAULT_NODE_DATA, nodeData)
      },
      /**
       * open form model
       * @desc Edit task config
       * @param {number} taskCode
       * @param {string} taskType
       */
      openFormModel (taskCode, taskType) {
        this.setNodeData({
          id: taskCode,
          taskType: taskType
        })
        this.toggleTaskDrawer(true)
      },
      addTaskInfo ({ item }) {
        this.addTask(item)
        this.$refs.canvas.setNodeName(item.code, item.name)
        this.taskDrawer = false
      },
      closeTaskDrawer ({ flag }) {
        if (flag) {
          const canvas = this.$refs.canvas
          canvas.removeNode(this.nodeData.id)
        }
        this.taskDrawer = false
      },
      /**
       * Save dialog
       */
      toggleSaveDialog (value) {
        // create task instance
        const tasks = this.tasks.map(item => {
          const task = {
            ...item
          }
          delete task.code
          return task
        })
        this.saveTask({
          projectCode: this.projectCode,
          taskDefinitionJson: JSON.stringify(tasks)
        }).then(() => {
          this.$router.push({ name: 'task-definition' })
        }).catch(() => {
          this.$message.warning(`${$t('Failed')}`)
        })
      },
      toSubProcess ({ subProcessCode, subInstanceId }) {
        const tarIdentifier = this.type === 'instance' ? subInstanceId : subProcessCode
        const curIdentifier = this.type === 'instance' ? this.instanceId : this.definitionCode
        let subs = []
        let olds = this.$route.query.subs
        if (olds) {
          subs = olds.split(',')
          subs.push(curIdentifier)
        } else {
          subs.push(curIdentifier)
        }
        let $name = this.$route.name.split('-')
        this.$router.push({
          path: `/${$name[0]}/${this.projectCode}/${$name[1]}/list/${tarIdentifier}`,
          query: { subs: subs.join(',') }
        })
      },
      seeHistory (taskName) {
        this.$router.push({
          name: 'task-instance',
          query: {
            processInstanceId: this.$route.params.code,
            taskName: taskName
          }
        })
      },
      /**
       * Start dialog
       */
      startRunning (taskName) {
        this.startTaskName = taskName
        this.getStartCheck({ processDefinitionCode: this.definitionCode }).then(
          (res) => {
            this.startDialog = true
          }
        )
      },
      onUpdateStart () {
        this.startDialog = false
      },
      closeStart () {
        this.startDialog = false
      },
      /**
       * Task status
       */
      refreshTaskStatus () {
        const instanceId = this.$route.params.id
        this.loading(true)
        this.getTaskState(instanceId)
          .then((res) => {
            this.$message(this.$t('Refresh status succeeded'))
            const { taskList } = res.data
            if (taskList) {
              taskList.forEach((taskInstance) => {
                this.$refs.canvas.setNodeStatus({
                  code: taskInstance.taskCode,
                  state: taskInstance.state,
                  taskInstance
                })
              })
            }
          })
          .finally(() => {
            this.loading(false)
          })
      },
      /**
       * Loading
       * @param {boolean} visible
       */
      loading (visible) {
        if (visible) {
          this.spinner = this.$loading({
            lock: true,
            text: this.$t('Loading...'),
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.4)',
            customClass: 'dag-fullscreen-loading'
          })
        } else {
          this.spinner && this.spinner.close()
        }
      },
      /**
       * change process definition version
       */
      showVersions () {
        this.getProcessDefinitionVersionsPage({
          pageNo: 1,
          pageSize: 10,
          code: this.definitionCode
        })
          .then((res) => {
            let processDefinitionVersions = res.data.totalList
            let total = res.data.total
            let pageSize = res.data.pageSize
            let pageNo = res.data.currentPage
            // this.versionData.processDefinition.id = this.urlParam.id
            this.versionData.processDefinition.code = this.definitionCode
            this.versionData.processDefinition.version = this.version
            this.versionData.processDefinition.releaseState = this.releaseState
            this.versionData.processDefinitionVersions =
              processDefinitionVersions
            this.versionData.total = total
            this.versionData.pageNo = pageNo
            this.versionData.pageSize = pageSize
            this.versionDrawer = true
          })
          .catch((e) => {
            this.$message.error(e.msg || '')
          })
      },
      closeVersion () {
        this.versionDrawer = false
      },
      switchProcessVersion ({ version, processDefinitionCode }) {
        this.switchProcessDefinitionVersion({
          version: version,
          code: processDefinitionCode
        }).then(res => {
          this.$message.success($t('Switch Version Successfully'))
          this.closeVersion()
          this.definitionDetails.init()
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      getProcessVersions ({ pageNo, pageSize, processDefinitionCode }) {
        this.getProcessDefinitionVersionsPage({
          pageNo: pageNo,
          pageSize: pageSize,
          code: processDefinitionCode
        }).then(res => {
          this.versionData.processDefinitionVersions = res.data.totalList
          this.versionData.total = res.data.total
          this.versionData.pageSize = res.data.pageSize
          this.versionData.pageNo = res.data.currentPage
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      deleteProcessVersion ({ version, processDefinitionCode }) {
        this.deleteProcessDefinitionVersion({
          version: version,
          code: processDefinitionCode
        }).then(res => {
          this.$message.success(res.msg || '')
          this.getProcessVersions({
            pageNo: 1,
            pageSize: 10,
            processDefinitionCode: processDefinitionCode
          })
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./_source/dag";
</style>

<style lang="scss">
@import "./_source/loading";
</style>
