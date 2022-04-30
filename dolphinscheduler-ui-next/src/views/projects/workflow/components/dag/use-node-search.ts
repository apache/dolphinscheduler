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

import type { Graph } from '@antv/x6'
import { ref, Ref } from 'vue'

interface Options {
  graph: Ref<Graph | undefined>
}

/**
 * Node search and navigate
 */
export function useNodeSearch(options: Options) {
  const searchSelectValue = ref('')

  const { graph } = options

  /**
   * Search input visible control
   */
  const searchInputVisible = ref(false)
  const toggleSearchInput = () => {
    searchInputVisible.value = !searchInputVisible.value
  }

  /**
   * Search dropdown control
   */
  const nodesDropdown = ref<{ label: string; value: string }[]>([])
  const reQueryNodes = () => {
    const nodes = graph.value?.getNodes() || []
    nodesDropdown.value = nodes.map((node) => ({
      label: node.getData().taskName,
      value: node.id
    }))
    const filterSelect = nodesDropdown.value.findIndex(
      (item) => item.value === searchSelectValue.value
    )
    filterSelect === -1 && (searchSelectValue.value = '')
  }

  /**
   * Navigate to cell
   * @param {string} code
   */
  function navigateTo(code: string) {
    searchSelectValue.value = code
    if (!graph.value) return
    const cell = graph.value.getCellById(code)
    graph.value.scrollToCell(cell, { animation: { duration: 600 } })
    graph.value.cleanSelection()
    graph.value.select(cell)
  }

  return {
    searchSelectValue,
    navigateTo,
    toggleSearchInput,
    searchInputVisible,
    reQueryNodes,
    nodesDropdown
  }
}
