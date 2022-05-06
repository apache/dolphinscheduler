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

import type { Node, Edge } from '@antv/x6'
import { X6_NODE_NAME, X6_EDGE_NAME } from './dag-config'
import utils from '@/utils'
import { WorkflowDefinition, Coordinate } from './types'

export function useCustomCellBuilder() {
  /**
   * Convert locationStr to JSON
   * @param {string} locationStr
   * @returns
   */
  function parseLocationStr(locationStr: string) {
    let locations = null
    if (!locationStr) return locations
    locations = JSON.parse(locationStr)
    return Array.isArray(locations) ? locations : null
  }

  /**
   * Build edge metadata
   * @param {string} sourceId
   * @param {string} targetId
   * @param {string} label
   */
  function buildEdge(
    sourceId: string,
    targetId: string,
    label = ''
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
  function buildNode(
    id: string,
    type: string,
    taskName: string,
    flag: string,
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
        taskName: taskName || id,
        flag: flag
      },
      attrs: {
        image: {
          // Use href instead of xlink:href, you may lose the icon when downloadPNG
          'xlink:href': `${
            import.meta.env.BASE_URL
          }images/task-icons/${type.toLocaleLowerCase()}.png`
        },
        title: {
          text: truncation
        },
        rect: {
          fill: flag === 'NO' ? '#f3f3f5' : '#ffffff'
        }
      }
    }
  }

  /**
   * Build graph JSON
   * @param {WorkflowDefinition} definition
   * @returns
   */
  function buildGraph(definition: WorkflowDefinition) {
    const nodes: Node.Metadata[] = []
    const edges: Edge.Metadata[] = []

    const locations =
      parseLocationStr(definition.processDefinition.locations) || []
    const tasks = definition.taskDefinitionList
    const connects = definition.processTaskRelationList

    tasks.forEach((task) => {
      const location = locations.find((l) => l.taskCode === task.code) || {}
      const node = buildNode(
        task.code + '',
        task.taskType,
        task.name,
        task.flag,
        {
          x: location.x,
          y: location.y
        }
      )
      nodes.push(node)
    })

    connects
      .filter((r) => !!r.preTaskCode)
      .forEach((c) => {
        const edge = buildEdge(c.preTaskCode + '', c.postTaskCode + '', c.name)
        edges.push(edge)
      })
    return {
      nodes,
      edges
    }
  }

  return {
    buildNode,
    buildEdge,
    buildGraph
  }
}
