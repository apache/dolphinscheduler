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
    <dag-toolbar />
    <dag-canvas ref="canvas" />
    <el-drawer
      :visible.sync="taskDrawer"
      size=""
      :with-header="false"
      :wrapperClosable="false"
    >
      <!-- fix the bug that Element-ui(2.13.2) auto focus on the first input -->
      <div style="width: 0px; height: 0px; overflow: hidden">
        <el-input type="text" />
      </div>
      <m-form-model
        v-if="taskDrawer"
        :nodeData="nodeData"
        @seeHistory="seeHistory"
        @addTaskInfo="addTaskInfo"
        @close="closeTaskDrawer"
        @onSubProcess="toSubProcess"
        :type="type"
      ></m-form-model>
    </el-drawer>
    <el-dialog
      :title="$t('Set the DAG diagram name')"
      :visible.sync="saveDialog"
      width="auto"
    >
      <m-udp ref="mUdp" @onUdp="onSave" @close="cancelSave"></m-udp>
    </el-dialog>
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
    <m-log
        v-if="type === 'instance' && logDialog"
        :item="logTaskInstance"
        source='dag'
        :task-instance-id="logTaskInstance.id"
        @close="closeLogDialog"
    ></m-log>
  </div>
</template>

<script>
  import { debounce } from 'lodash'
  import dagToolbar from './canvas/toolbar.vue'
  import dagCanvas from './canvas/canvas.vue'
  import mFormModel from '../_source/formModel/formModel.vue'
  import { mapActions, mapState, mapMutations } from 'vuex'
  import mUdp from '../_source/udp/udp.vue'
  import mStart from '../../projects/pages/definition/pages/list/_source/start.vue'
  import edgeEditModel from './canvas/edgeEditModel.vue'
  import mVersions from '../../projects/pages/definition/pages/list/_source/versions.vue'
  import mLog from './formModel/log.vue'

  const DEFAULT_NODE_DATA = {
    id: null,
    taskType: '',
    self: {},
    instanceId: null
  }

  export default {
    name: 'dag-chart',
    components: {
      dagCanvas,
      dagToolbar,
      mFormModel,
      mUdp,
      mStart,
      edgeEditModel,
      mVersions,
      mLog
    },
    provide () {
      return {
        dagChart: this
      }
    },
    inject: ['definitionDetails'],
    props: {
      type: String,
      releaseState: String
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
        instanceId: -1,
        // log dialog
        logDialog: false,
        logTaskInstance: null,
        taskInstances: []
      }
    },
    mounted () {
      this.setIsEditDag(false)

      if (this.type === 'instance') {
        this.instanceId = this.$route.params.id
        this.definitionCode = this.$route.query.code || this.code
      } else if (this.type === 'definition') {
        this.definitionCode = this.$route.params.code
      }

      // auto resize canvas
      this.resizeDebounceFunc = debounce(this.canvasResize, 200)
      window.addEventListener('resize', this.resizeDebounceFunc)

      // init graph
      this.$refs.canvas.graphInit(!this.isDetails)

      // backfill graph with tasks, locations and connects
      this.backfill()

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
        'version',
        'code'
      ])
    },
    methods: {
      ...mapActions('dag', [
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
       * Toggle full screen
       */
      canvasResize () {
        const canvas = this.$refs.canvas
        canvas && canvas.paperResize()
      },
      toggleFullScreen () {
        this.fullScreen = !this.fullScreen
        this.$nextTick(this.canvasResize)
      },
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
        this.saveDialog = value
        if (value) {
          this.$nextTick(() => {
            this.$refs.mUdp.reloadParam()
          })
        }
      },
      onSave (sourceType) {
        this.toggleSaveDialog(false)
        return new Promise((resolve, reject) => {
          let tasks = this.tasks || []
          const edges = this.$refs.canvas.getEdges()
          const nodes = this.$refs.canvas.getNodes()
          if (!nodes.length) {
            reject(this.$t('Failed to create node to save'))
          }
          const connects = this.buildConnects(edges, tasks)
          this.setConnects(connects)
          const locations = nodes.map((node) => {
            return {
              taskCode: node.id,
              x: node.position.x,
              y: node.position.y
            }
          })
          this.setLocations(locations)
          resolve({
            connects: connects,
            tasks: tasks,
            locations: locations
          })
        })
          .then((res) => {
            if (this.verifyConditions(res.tasks)) {
              this.loading(true)
              const isEdit = !!this.definitionCode
              if (isEdit) {
                const methodName = this.type === 'instance' ? 'updateInstance' : 'updateDefinition'
                const methodParam = this.type === 'instance' ? this.instanceId : this.definitionCode
                // Edit
                return this[methodName](methodParam)
                  .then((res) => {
                    this.$message({
                      message: res.msg,
                      type: 'success',
                      offset: 80
                    })
                    if (this.type === 'instance') {
                      this.$router.push({
                        path: `/projects/${this.projectCode}/instance/list`
                      })
                    } else {
                      this.$router.push({
                        path: `/projects/${this.projectCode}/definition/list`
                      })
                    }
                  })
                  .catch((e) => {
                    this.$message.error(e.msg || '')
                  })
                  .finally((e) => {
                    this.loading(false)
                  })
              } else {
                // Create
                return this.saveDAGchart()
                  .then((res) => {
                    this.$message.success(res.msg)
                    // source @/conf/home/pages/dag/_source/editAffirmModel/index.js
                    if (sourceType !== 'affirm') {
                      // Jump process definition
                      this.$router.push({ name: 'projects-definition-list' })
                    }
                  })
                  .catch((e) => {
                    this.setName('')
                    this.$message.error(e.msg || '')
                  })
                  .finally((e) => {
                    this.loading(false)
                  })
              }
            }
          })
          .catch((err) => {
            let msg = typeof err === 'string' ? err : err.msg || ''
            this.$message.error(msg)
          })
      },
      verifyConditions (value) {
        let tasks = value
        let bool = true
        tasks.map((v) => {
          if (
            v.taskType === 'CONDITIONS' &&
            (v.taskParams.conditionResult.successNode[0] === '' ||
              v.taskParams.conditionResult.successNode[0] === null ||
              v.taskParams.conditionResult.failedNode[0] === '' ||
              v.taskParams.conditionResult.failedNode[0] === null)
          ) {
            bool = false
            return false
          }
        })
        if (!bool) {
          this.$message.warning(
            `${this.$t(
              'Successful branch flow and failed branch flow are required'
            )}`
          )
          return false
        }
        return true
      },
      cancelSave () {
        this.toggleSaveDialog(false)
      },
      /**
       * build graph json
       */
      buildGraphJSON (tasks, locations, connects) {
        const nodes = []
        const edges = []
        tasks.forEach((task) => {
          const location = locations.find((l) => l.taskCode === task.code) || {}
          const node = this.$refs.canvas.genNodeJSON(
            task.code,
            task.taskType,
            task.name,
            {
              x: location.x,
              y: location.y
            }
          )
          nodes.push(node)
        })
        connects
          .filter((r) => !!r.preTaskCode)
          .forEach((c) => {
            const edge = this.$refs.canvas.genEdgeJSON(
              c.preTaskCode,
              c.postTaskCode,
              c.name
            )
            edges.push(edge)
          })
        return {
          nodes,
          edges
        }
      },
      /**
       * Build connects by edges and tasks
       * @param {Edge[]} edges
       * @param {Task[]} tasks
       * @returns
       */
      buildConnects (edges, tasks) {
        const preTaskMap = {}
        const tasksMap = {}

        edges.forEach((edge) => {
          preTaskMap[edge.targetId] = {
            sourceId: edge.sourceId,
            edgeLabel: edge.label || ''
          }
        })
        tasks.forEach((task) => {
          tasksMap[task.code] = task
        })

        const headEdges = tasks
          .filter((task) => !preTaskMap[task.code])
          .map((task) => {
            return {
              name: '',
              preTaskCode: 0,
              preTaskVersion: 0,
              postTaskCode: task.code,
              postTaskVersion: task.version || 0,
              // conditionType and conditionParams are reserved
              conditionType: 0,
              conditionParams: {}
            }
          })

        return edges
          .map((edge) => {
            return {
              name: edge.label,
              preTaskCode: edge.sourceId,
              preTaskVersion: tasksMap[edge.sourceId].version || 0,
              postTaskCode: edge.targetId,
              postTaskVersion: tasksMap[edge.targetId].version || 0,
              // conditionType and conditionParams are reserved
              conditionType: 0,
              conditionParams: {}
            }
          })
          .concat(headEdges)
      },
      backfill () {
        const tasks = this.tasks
        const locations = this.locations
        const connects = this.connects
        const json = this.buildGraphJSON(tasks, locations, connects)
        this.$refs.canvas.fromJSON(json)
      },
      /**
       * Return to the previous process
       */
      returnToPrevProcess () {
        let $name = this.$route.name.split('-')
        let subs = this.$route.query.subs
        let ids = subs.split(',')
        const last = ids.pop()
        this.$router.push({
          path: `/${$name[0]}/${this.projectCode}/${$name[1]}/list/${last}`,
          query: ids.length > 0 ? { subs: ids.join(',') } : null
        })
      },
      toSubProcess ({ subProcessCode, subInstanceId }) {
        const tarIdentifier =
          this.type === 'instance' ? subInstanceId : subProcessCode
        const curIdentifier =
          this.type === 'instance' ? this.instanceId : this.definitionCode
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
              this.taskInstances = taskList
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
        })
          .then((res) => {
            this.$message.success($t('Switch Version Successfully'))
            this.closeVersion()
            this.definitionDetails.init()
          })
          .catch((e) => {
            this.$message.error(e.msg || '')
          })
      },
      getProcessVersions ({ pageNo, pageSize, processDefinitionCode }) {
        this.getProcessDefinitionVersionsPage({
          pageNo: pageNo,
          pageSize: pageSize,
          code: processDefinitionCode
        })
          .then((res) => {
            this.versionData.processDefinitionVersions = res.data.totalList
            this.versionData.total = res.data.total
            this.versionData.pageSize = res.data.pageSize
            this.versionData.pageNo = res.data.currentPage
          })
          .catch((e) => {
            this.$message.error(e.msg || '')
          })
      },
      deleteProcessVersion ({ version, processDefinitionCode }) {
        this.deleteProcessDefinitionVersion({
          version: version,
          code: processDefinitionCode
        })
          .then((res) => {
            this.$message.success(res.msg || '')
            this.getProcessVersions({
              pageNo: 1,
              pageSize: 10,
              processDefinitionCode: processDefinitionCode
            })
          })
          .catch((e) => {
            this.$message.error(e.msg || '')
          })
      },
      /**
       * Log dialog
       */
      closeLogDialog () {
        this.logDialog = false
        this.logTaskInstance = null
      },
      showLogDialog (taskDefinitionCode) {
        const taskInstance = this.taskInstances.find(taskInstance => {
          return taskInstance.taskCode === taskDefinitionCode
        })
        if (taskInstance) {
          this.logTaskInstance = {
            id: taskInstance.id,
            type: taskInstance.taskType
          }
          this.logDialog = true
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./dag";
</style>

<style lang="scss">
@import "./loading";
</style>
