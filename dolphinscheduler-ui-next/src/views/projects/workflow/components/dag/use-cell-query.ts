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
import { TaskType } from '../../../task/constants/task-type'

interface Options {
  graph: Ref<Graph | undefined>
}

/**
 * Expose some cell-related query methods and refs
 * @param {Options} options
 */
export function useCellQuery(options: Options) {
  const { graph } = options

  /**
   * Get all nodes
   */
  function getNodes() {
    const nodes = graph.value?.getNodes()
    if (!nodes) return []
    return nodes.map((node) => {
      const position = node.getPosition()
      const data = node.getData()
      return {
        code: node.id,
        position: position,
        name: data.taskName as string,
        type: data.taskType as TaskType
      }
    })
  }

  return {
    getNodes
  }
}
