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

import { reactive, ref, Ref } from 'vue'
import { onMounted } from 'vue'
import type { Graph, Cell } from '@antv/x6'

interface Options {
  graph: Ref<Graph | undefined>
}

/**
 * Get position of the right-clicked Cell.
 */
export function useNodeMenu(options: Options) {
  const { graph } = options

  const nodeVariables = reactive({
    menuVisible: false,
    startModalShow: false,
    logTaskId: -1,
    logTaskType: '',
    pageX: 0,
    pageY: 0,
    menuCell: {} as Cell,
    showModalRef: ref(false),
    row: {},
    logRef: '',
    logLoadingRef: ref(true),
    skipLineNum: ref(0),
    limit: ref(1000),
    taskCode: ''
  })

  const menuHide = () => {
    nodeVariables.menuVisible = false

    // unlock scroller
    graph.value?.unlockScroller()
  }

  const menuStart = (code: number) => {
    nodeVariables.startModalShow = true
    nodeVariables.taskCode = String(code)
  }

  const viewLog = (taskId: number, taskType: string) => {
    nodeVariables.logTaskId = taskId
    nodeVariables.logTaskType = taskType
    nodeVariables.showModalRef = true
  }

  onMounted(() => {
    if (graph.value) {
      // contextmenu
      graph.value.on('node:contextmenu', ({ cell, x, y }) => {
        nodeVariables.menuCell = cell
        const data = graph.value!.localToPage(x, y)
        nodeVariables.pageX = data.x
        nodeVariables.pageY = data.y

        // show menu
        nodeVariables.menuVisible = true

        // lock scroller
        graph.value!.lockScroller()
      })
    }
  })

  return {
    nodeVariables,
    menuHide,
    menuStart,
    viewLog
  }
}
