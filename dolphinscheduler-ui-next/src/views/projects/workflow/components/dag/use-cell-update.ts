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
import type { Graph } from '@antv/x6'
import type { TaskType } from '@/views/projects/task/constants/task-type'
import type { Coordinate } from './types'
import { TASK_TYPES_MAP } from '@/views/projects/task/constants/task-type'
import { useCustomCellBuilder } from './dag-hooks'
import utils from '@/utils'

interface Options {
  graph: Ref<Graph | undefined>
}

/**
 * Expose some cell query
 * @param {Options} options
 */
export function useCellUpdate(options: Options) {
  const { graph } = options

  const { buildNode } = useCustomCellBuilder()

  /**
   * Set node name by id
   * @param {string} id
   * @param {string} name
   */
  function setNodeName(id: string, newName: string) {
    const node = graph.value?.getCellById(id)
    if (node) {
      const truncation = utils.truncateText(newName, 18)
      node.attr('title/text', truncation)
      node.setData({ taskName: newName })
    }
  }

  /**
   * Add a node to the graph
   * @param {string} id
   * @param {string} taskType
   * @param {Coordinate} coordinate Default is { x: 100, y: 100 }
   */
  function addNode(
    id: string,
    type: string,
    name: string,
    coordinate: Coordinate = { x: 100, y: 100 }
  ) {
    if (!TASK_TYPES_MAP[type as TaskType]) {
      console.warn(`taskType:${type} is invalid!`)
      return
    }
    const node = buildNode(id, type, name, coordinate)
    graph.value?.addNode(node)
  }

  return {
    setNodeName,
    addNode
  }
}
