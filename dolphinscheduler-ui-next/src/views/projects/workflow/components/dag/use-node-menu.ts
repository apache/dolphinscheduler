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

import type { Ref } from 'vue'
import { onMounted, ref } from 'vue'
import type { Graph, Cell } from '@antv/x6'

interface Options {
  graph: Ref<Graph | undefined>
}

/**
 * Get position of the right-clicked Cell.
 */
export function useNodeMenu(options: Options) {
  const { graph } = options
  const startModalShow = ref(false)
  const logModalShow = ref(false)
  const menuVisible = ref(false)
  const pageX = ref()
  const pageY = ref()
  const menuCell = ref<Cell>()

  const menuHide = () => {
    menuVisible.value = false

    // unlock scroller
    graph.value?.unlockScroller()
  }

  const menuStart = () => {
    startModalShow.value = true
  }

  const viewLog = () => {
    logModalShow.value = true
  }

  const hideLog = () => {
    logModalShow.value = false
  }

  onMounted(() => {
    if (graph.value) {
      // contextmenu
      graph.value.on('node:contextmenu', ({ cell, x, y }) => {
        menuCell.value = cell
        const data = graph.value?.localToPage(x, y)
        pageX.value = data?.x
        pageY.value = data?.y

        // show menu
        menuVisible.value = true

        // lock scroller
        graph.value?.lockScroller()
      })
    }
  })

  return {
    pageX,
    pageY,
    startModalShow,
    logModalShow,
    menuVisible,
    menuCell,
    menuHide,
    menuStart,
    viewLog,
    hideLog
  }
}
