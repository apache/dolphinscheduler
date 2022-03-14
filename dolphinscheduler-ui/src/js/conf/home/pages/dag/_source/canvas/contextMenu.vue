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
  <div
    class="dag-context-menu"
    v-show="visible"
    :style="{
      left: `${left}px`,
      top: `${top}px`,
    }"
  >
    <menu-item :disabled="!startAvailable" @on-click="onStart">
      {{ $t("Start") }}
    </menu-item>
    <menu-item :disabled="readOnly" @on-click="onEdit">
      {{ $t("Edit") }}
    </menu-item>
    <menu-item :disabled="readOnly" @on-click="onCopy">
      {{ $t("Copy") }}
    </menu-item>
    <menu-item :disabled="readOnly" @on-click="onDelete">
      {{ $t("Delete") }}
    </menu-item>
    <menu-item v-if="dagChart.type === 'instance'" :disabled="!logMenuVisible" @on-click="showLog">
      {{ $t('View log') }}
    </menu-item>
  </div>
</template>

<script>
  import { mapState, mapActions, mapMutations } from 'vuex'
  import { findComponentDownward, uuid } from '@/module/util/'
  import MenuItem from './menuItem.vue'
  import _ from 'lodash'

  export default {
    name: 'dag-context-menu',
    inject: ['dagChart', 'dagCanvas'],
    components: {
      MenuItem
    },
    data () {
      return {
        visible: false,
        left: 0,
        top: 0,
        canvasRef: null,
        currentTask: {
          code: 0,
          name: '',
          type: ''
        }
      }
    },
    computed: {
      ...mapState('dag', ['isDetails', 'releaseState', 'tasks']),
      startAvailable () {
        return (
          this.$route.name === 'projects-definition-details' &&
          this.releaseState !== 'NOT_RELEASE'
        )
      },
      readOnly () {
        return this.isDetails
      },
      logMenuVisible () {
        if (this.dagChart.taskInstances.length > 0) {
          return !!this.dagChart.taskInstances.find(taskInstance => taskInstance.taskCode === this.currentTask.code)
        }
        return true
      }
    },
    mounted () {
      document.addEventListener('click', (e) => {
        this.hide()
      })
    },
    methods: {
      ...mapActions('dag', ['genTaskCodeList']),
      ...mapMutations('dag', ['addTask']),
      getDagCanvasRef () {
        if (this.canvasRef) {
          return this.canvasRef
        } else {
          const canvas = findComponentDownward(this.dagChart, 'dag-canvas')
          this.canvasRef = canvas
          return canvas
        }
      },
      setCurrentTask (task) {
        this.currentTask = { ...this.currentTask, ...task }
      },
      onStart () {
        this.dagChart.startRunning(this.currentTask.code)
      },
      onEdit () {
        this.dagChart.openFormModel(this.currentTask.code, this.currentTask.type)
      },
      onCopy () {
        const nodes = this.dagCanvas.getNodes()
        const targetNode = nodes.find(
          (node) => node.id === this.currentTask.code
        )
        const targetTask = this.tasks.find(
          (task) => task.code === this.currentTask.code
        )

        if (!targetNode || !targetTask) return

        this.genTaskCodeList({
          genNum: 1
        })
          .then((res) => {
            const [code] = res
            const taskName = uuid(targetTask.name + '_')
            const task = {
              ...targetTask,
              code,
              name: taskName
            }
            if (targetTask.taskParams) {
              task.taskParams = _.cloneDeep(targetTask.taskParams)
            }

            this.dagCanvas.addNode(code, this.currentTask.type, {
              x: targetNode.position.x + 100,
              y: targetNode.position.y + 100
            })
            this.addTask(task)
            this.dagCanvas.setNodeName(code, taskName)
          })
          .catch((err) => {
            console.error(err)
          })
      },
      onDelete () {
        this.dagCanvas.removeNode(this.currentTask.code)
      },
      showLog () {
        this.dagChart.showLogDialog(this.currentTask.code)
      },
      show (x = 0, y = 0) {
        this.dagCanvas.lockScroller()
        this.visible = true
        this.left = x + 10
        this.top = y + 10
      },
      hide () {
        this.dagCanvas.unlockScroller()
        this.visible = false
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./contextMenu";
</style>
