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
  <div class="dag-canvas">
    <dag-taskbar @on-drag-start="onDragStart"  />
    <div
      class="dag-container"
      ref="container"
      @dragenter.prevent
      @dragover.prevent
      @dragleave.prevent
      @drop.stop.prevent="onDrop"
    >
      <div ref="paper" class="paper"></div>
    </div>
  </div>
</template>

<script>
  import dagTaskbar from './taskbar.vue'
  import {
    NODE_PROPS,
    NODE_STATUS_MARKUP
  } from './x6-helper'
  import { tasksType, tasksState } from '../config'
  import { mapActions, mapMutations, mapState } from 'vuex'
  import nodeStatus from './nodeStatus'

  export default {
    name: 'dag-canvas',
    data () {
      return {
        graph: null,
        // Used to calculate the context menu location
        originalScrollPosition: {
          left: 0,
          top: 0
        },
        editable: true,
        dragging: {
          // Distance from the mouse to the top-left corner of the dragging element
          x: 0,
          y: 0,
          type: ''
        }
      }
    },
    provide () {
      return {
        dagCanvas: this
      }
    },
    inject: ['dagChart'],
    components: {
      dagTaskbar
    },
    computed: {
      ...mapState('dag', [
        'tasks'
      ])
    },
    mounted () {
      this.$nextTick(() => {
        const div = document.createElement('div')
        div.className = 'task-table'
        div.style.cssText = 'position: absolute; top: 0; bottom: 0; left: 0; right: 0'
        const targetDiv = document.querySelector('.paper')
        targetDiv.appendChild(div)
      })
    },
    methods: {
      ...mapActions('dag', ['genTaskCodeList']),
      ...mapMutations('dag', ['removeTask']),
      /**
       * Set node name by id
       * @param {string|number} id
       * @param {string} name
       */
      setNodeName (id, name) {
        id += ''
        let div = document.querySelector(`[data-id="${id}"]`)
        div.innerHTML = `
          <div class="item-cell">${$t('App Name')}：${name}</div>
          <div class="item-cell">${$t('Type')}：${div.getAttribute('data-taskType')}</div>
          `
        const deleteIcon = document.createElement('i')
        deleteIcon.className = 'el-icon-delete'
        deleteIcon.addEventListener('click', e => {
          e.stopPropagation()
          this.removeNode(id)
        })
        div.appendChild(deleteIcon)
      },
      getNodes () {
        const nodes = Array.from(document.querySelectorAll('.table-item'))
        return nodes.map((node) => {
          return {
            id: Number(node.getAttribute('data-id')),
            data: {
              taskType: node.getAttribute('data-taskType')
            }
          }
        })
      },
      /**
       * add a node to the graph
       * @param {string|number} id
       * @param {string} taskType
       * @param {{x:number;y:number}} coordinate Default is { x: 100, y: 100 }
       */
      addNode (id, taskType) {
        id += ''
        if (!tasksType[taskType]) {
          console.warn(`taskType:${taskType} is invalid!`)
          return
        }
        const targetDiv = document.querySelector('.task-table')
        // 创建任务列表
        const div = document.createElement('div')
        div.innerHTML = `<div class="item-cell">${$t('App Name')}</div>`
        div.setAttribute('data-id', id)
        div.setAttribute('data-taskType', taskType)
        div.className = 'table-item'
        div.addEventListener('click', () => {
          this.dagChart.openFormModel(Number(id), taskType)
        })
        targetDiv.appendChild(div)
      },
      /**
       * remove a node
       * @param {string|number} id NodeId
       */
      removeNode (id) {
        id += ''
        this.removeTask(+id)
        const targetDiv = document.querySelector('.task-table')
        // 删除节点
        const div = document.querySelector(`[data-id="${id}"]`)
        targetDiv.removeChild(div)
      },
      /**
       * Gets the current selections
       * @return {Cell[]}
       */
      getSelections () {},
      /**
       * Lock scroller
       */
      lockScroller () {},
      /**
       * Unlock scroller
       */
      unlockScroller () {},
      /**
       * set node status icon
       * @param {number} code
       * @param {string} state
       */
      setNodeStatus ({ code, state, taskInstance }) {
        code += ''
        const stateProps = tasksState[state]
        const node = this.graph.getCellById(code)
        if (node) {
          // Destroy the previous dom
          node.removeMarkup()
          node.setMarkup(NODE_PROPS.markup.concat(NODE_STATUS_MARKUP))
          const nodeView = this.graph.findViewByCell(node)
          const el = nodeView.find('div')[0]
          nodeStatus({
            stateProps,
            taskInstance
          }).$mount(el)
        }
      },
      /**
       * Drag && Drop Event
       */
      onDragStart (e, taskType) {
        if (!this.editable) {
          e.preventDefault()
          return
        }
        this.dragging = {
          x: e.offsetX,
          y: e.offsetY,
          type: taskType.name
        }
      },
      onDrop (e) {
        const { type } = this.dragging
        this.genTaskCodeList({
          genNum: 1
        })
          .then((res) => {
            const [code] = res
            this.addNode(code, type)
            this.dagChart.openFormModel(code, type)
          })
          .catch((err) => {
            console.error(err)
          })
      }
    }
  }
</script>

<style lang="scss">
@import "./canvas";
.table-item {
  display: flex;
  border-radius: 4px;
  border: 1px solid #DCDFE6;
  padding: 8px 16px;
  &+.table-item {
    margin-top: 8px;
  }
  &:hover {
    cursor: pointer;
    background-color: #fff;
    border-color: #fff;
  }
  .item-cell {
    flex: 1;
    text-align: center;
    &+.item-cell {
      margin-left: 100px;
    }
  }
}
</style>
