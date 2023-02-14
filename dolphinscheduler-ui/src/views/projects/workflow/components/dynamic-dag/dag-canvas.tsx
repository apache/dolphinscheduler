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

import { defineComponent, ref, onMounted, watch } from 'vue'
import { Graph } from '@antv/x6'
import { useDagResize } from './use-dag-resize'
import { useDagGraph } from './use-dag-graph'
import { useDagNode } from './use-dag-node'
import { useAddDagShape } from './use-add-dag-shape'
import { useDagStore } from '@/store/project/dynamic/dag'
import { DagNodeName } from './dag-setting'
import styles from './dag-canvas.module.scss'

const DagCanvas = defineComponent({
  name: 'DagCanvas',
  emits: ['drop'],
  setup(props, context) {
    const container = ref()
    const dagContainer = ref()
    const minimapContainer = ref()
    const graph = ref<Graph>()
    const dagTasks = useDagStore().getDagTasks

    if (graph.value) {
      useDagResize(dagContainer.value, graph.value)
    }

    const initGraph = () => {
      graph.value = useDagGraph(container.value, minimapContainer.value)
    }

    const registerNode = () => {
      Graph.unregisterNode(DagNodeName)
      Graph.registerNode(DagNodeName, useDagNode())
    }

    const handlePreventDefault = (e: DragEvent) => {
      e.preventDefault()
    }

    const handleDrop = (e: DragEvent) => {
      context.emit('drop', e)
    }

    onMounted(() => {
      initGraph()
      registerNode()
    })

    watch(dagTasks, () => {
      useAddDagShape(graph.value as Graph)
    })

    return {
      container,
      'dag-container': dagContainer,
      minimapContainer,
      handlePreventDefault,
      handleDrop
    }
  },
  render() {
    return (
      <>
        <div
          ref='container'
          class={styles.container}
          onDrop={this.handleDrop}
          onDragenter={this.handlePreventDefault}
          onDragover={this.handlePreventDefault}
          onDragleave={this.handlePreventDefault}
        >
          <div ref='dag-container' class={styles['dag-container']} />
        </div>
        <div ref='minimapContainer' class={styles.minimap} />
      </>
    )
  }
})

export { DagCanvas }
