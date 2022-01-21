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
import type { Node, Graph, Edge } from '@antv/x6'
import { X6_NODE_NAME, X6_EDGE_NAME } from './dag-config'
import { ALL_TASK_TYPES } from '../../../task/constants/task-type'
import utils from '@/utils'

interface Options {
  graph: Ref<Graph | undefined>
}

type Coordinate = { x: number; y: number }

/**
 * Expose some graph operation methods
 * @param {Options} options
 */
export function useGraphOperations(options: Options) {
  const { graph } = options

  /**
   * Build edge metadata
   * @param {string} sourceId
   * @param {string} targetId
   * @param {string} label
   */
  function buildEdgeMetadata(
    sourceId: string,
    targetId: string,
    label: string = ''
  ): Edge.Metadata {
    return {
      shape: X6_EDGE_NAME,
      source: {
        cell: sourceId
      },
      target: {
        cell: targetId
      },
      labels: label ? [label] : undefined
    }
  }

  /**
   * Build node metadata
   * @param {string} id
   * @param {string} taskType
   * @param {Coordinate} coordinate Default is { x: 100, y: 100 }
   */
  function buildNodeMetadata(
    id: string,
    type: string,
    taskName: string,
    coordinate: Coordinate = { x: 100, y: 100 }
  ): Node.Metadata {
    const truncation = taskName ? utils.truncateText(taskName, 18) : id
    return {
      id: id,
      shape: X6_NODE_NAME,
      x: coordinate.x,
      y: coordinate.y,
      data: {
        taskType: type,
        taskName: taskName || id
      },
      attrs: {
        image: {
          // Use href instead of xlink:href, you may lose the icon when downloadPNG
          'xlink:href': `/src/assets/images/task-icons/${type.toLocaleLowerCase()}.png`
        },
        title: {
          text: truncation
        }
      }
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
    coordinate: Coordinate = { x: 100, y: 100 }
  ) {
    if (!ALL_TASK_TYPES[type]) {
      console.warn(`taskType:${type} is invalid!`)
      return
    }
    const node = buildNodeMetadata(id, type, '', coordinate)
    graph.value?.addNode(node)
  }

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
   * Get nodes
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
        name: data.taskName,
        type: data.taskType
      }
    })
  }

  /**
   * Navigate to cell
   * @param {string} code
   */
  function navigateTo(code: string) {
    if (!graph.value) return
    const cell = graph.value.getCellById(code)
    graph.value.scrollToCell(cell, { animation: { duration: 600 } })
    graph.value.cleanSelection()
    graph.value.select(cell)
  }

  return {
    buildEdgeMetadata,
    buildNodeMetadata,
    addNode,
    setNodeName,
    getNodes,
    navigateTo
  }
}
