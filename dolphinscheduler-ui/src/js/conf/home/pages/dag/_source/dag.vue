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
      <m-form-model
        v-if="taskDrawer"
        :nodeData="nodeData"
        @seeHistory="seeHistory"
        @addTaskInfo="addTaskInfo"
        @close="closeTaskDrawer"
        @onSubProcess="toSubProcess"
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

  const DEFAULT_NODE_DATA = {
    id: null,
    taskType: '',
    self: {},
    preNode: [],
    rearList: [],
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
      edgeEditModel
    },
    provide () {
      return {
        dagChart: this
      }
    },
    props: {
      type: String,
      releaseState: String
    },
    data () {
      return {
        // full screen mode
        fullScreen: false,
        // task config drawer
        taskDrawer: false,
        nodeData: { ...DEFAULT_NODE_DATA },
        saveDialog: false,
        isLoading: false,
        definitionCode: 0,
        startDialog: false,
        startTaskName: ''
      }
    },
    mounted () {
      // TODO
      window._debug = this

      if (this.type === 'instance') {
        this.definitionCode = this.$route.query.id
      } else if (this.type === 'definition') {
        this.definitionCode = this.$route.params.code
      }

      this.resizeDebounceFunc = debounce(this.canvasResize, 200)
      window.addEventListener('resize', this.resizeDebounceFunc)
      this.$refs.canvas.graphInit(!this.isDetails)

      this.backfill()
    },
    unmounted () {
      window.removeEventListener('resize', this.resizeDebounceFunc)
    },
    computed: {
      ...mapState('dag', [
        'tasks',
        'locations',
        'connects',
        'isEditDag',
        'name',
        'isDetails',
        'projectCode'
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
        'setTasks',
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
      addTaskInfo ({ item, fromThis }) {
        this.addTask(item)
        this.$refs.canvas.setNodeName(item.code, item.name)
        this.taskDrawer = false
      },
      closeTaskDrawer ({ item, flag, fromThis }) {
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
      },
      onSave (sourceType) {
        this.toggleSaveDialog(false)
        return new Promise((resolve, reject) => {
          this.isLoading = true

          let tasks = this.tasks || []
          const edges = this.$refs.canvas.getEdges()
          const nodes = this.$refs.canvas.getNodes()

          const connects = this.buildConnects(edges, tasks)

          this.setConnects(connects)

          const locations = nodes.map(node => {
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
        }).then((res) => {
          if (this._verifConditions(res.tasks)) {
            const definitionCode = this.definitionCode
            if (definitionCode) {
              /**
               * Edit
               * @param saveInstanceEditDAGChart => Process instance editing
               * @param saveEditDAGChart => Process definition editing
               */
              return this[
                this.type === 'instance' ? 'updateInstance' : 'updateDefinition'
              ](definitionCode)
                .then((res) => {
                  // this.$message.success(res.msg)
                  this.$message({
                    message: res.msg,
                    type: 'success',
                    offset: 80
                  })
                  this.isLoading = false
                  // Jump process definition
                  if (this.type === 'instance') {
                    this.$router.push({
                      path: `/projects/${this.projectCode}/instance/list/${definitionCode}`
                    })
                  } else {
                    this.$router.push({
                      path: `/projects/${this.projectCode}/definition/list/${definitionCode}`
                    })
                  }
                })
                .catch((e) => {
                  this.$message.error(e.msg || '')
                  this.isLoading = false
                })
            } else {
              // New
              return this.saveDAGchart()
                .then((res) => {
                  this.$message.success(res.msg)
                  this.isLoading = false
                  // source @/conf/home/pages/dag/_source/editAffirmModel/index.js
                  if (sourceType !== 'affirm') {
                    // Jump process definition
                    this.$router.push({ name: 'projects-definition-list' })
                  }
                })
                .catch((e) => {
                  this.$message.error(e.msg || '')
                  this.setName('')
                  this.isLoading = false
                })
            }
          }
        })
      },
      _verifConditions (value) {
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
          this.isLoading = false
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
          const location = locations.find(l => l.taskCode === task.code) || {}
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
        connects.filter(r => !!r.preTaskCode).forEach((c) => {
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
        tasks.forEach(task => {
          tasksMap[task.code] = task
        })

        return tasks.map(task => {
          const preTask = preTaskMap[task.code]
          return {
            name: preTask ? preTask.edgeLabel : '',
            preTaskCode: preTask ? preTask.sourceId : 0,
            preTaskVersion: preTask ? tasksMap[preTask.sourceId].version : 0,
            postTaskCode: task.code,
            postTaskVersion: tasksMap[task.code].version || 0,
            conditionType: 0,
            conditionParams: {}
          }
        })
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
        let subProcessCodes = this.$route.query.subProcessCodes
        let codes = subProcessCodes.split(',')
        const last = codes.pop()
        this.$router.push({
          path: `/${$name[0]}/${this.projectId}/${$name[1]}/list/${last}`,
          query: codes.length > 0 ? { subProcessCodes: codes.join(',') } : null
        })
      },
      toSubProcess ({ subProcessCode, fromThis }) {
        let subProcessCodes = []
        let getIds = this.$route.query.subProcessCodes
        if (getIds) {
          let newId = getIds.split(',')
          newId.push(this.definitionCode)
          subProcessCodes = newId
        } else {
          subProcessCodes.push(this.definitionCode)
        }
        let $name = this.$route.name.split('-')
        this.$router.push({
          path: `/${$name[0]}/${this.projectCode}/${$name[1]}/list/${subProcessCode}`,
          query: { subProcessCodes: subProcessCodes.join(',') }
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
       * Verify whether edge is valid
       * The number of edges start with CONDITIONS task cannot be greater than 2
       */
      edgeIsValid (edge) {
        const { sourceId } = edge
        const sourceTask = this.tasks.find(task => task.code === sourceId)
        if (sourceTask.taskType === 'CONDITIONS') {
          const edges = this.$refs.canvas.getEdges()
          return edges.filter(e => e.sourceId === sourceTask.code).length <= 2
        }
        return true
      }
    }
  }
</script>

<style lang="scss" scoped>
.dag-chart {
  width: 100%;
  height: calc(100vh - 100px);
  padding: 10px;
  background: #f2f3f7;

  &.full-screen {
    position: fixed;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    z-index: 9999;
  }
}
</style>
