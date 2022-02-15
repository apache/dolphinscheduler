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

import { ref, Ref } from 'vue'
import type { Graph } from '@antv/x6'
import { DagreLayout, GridLayout } from '@antv/layout'
import _ from 'lodash'
import { X6_NODE_NAME, X6_EDGE_NAME } from './dag-config'

interface Options {
  graph: Ref<Graph | undefined>
}

interface LayoutConfig {
  cols: number
  nodesep: number
  padding: number
  ranksep: number
  rows: number
  type: LAYOUT_TYPE
}

export enum LAYOUT_TYPE {
  GRID = 'grid',
  DAGRE = 'dagre'
}

/**
 * Auto layout graph
 * 1. Manage the state of auto layout popups
 * 2. Implement graph automatic layout function
 */
export function useGraphAutoLayout(options: Options) {
  const DEFAULT_LAYOUT_CONFIG: LayoutConfig = {
    cols: 0,
    nodesep: 50,
    padding: 50,
    ranksep: 50,
    rows: 0,
    type: LAYOUT_TYPE.DAGRE
  }

  const { graph: graphRef } = options

  // Auto layout config form ref
  const formRef = ref()

  // Auto layout config form value
  const formValue = ref({
    ...DEFAULT_LAYOUT_CONFIG
  })

  // Dag format modal visible
  const visible = ref<boolean>(false)
  const toggle = (bool?: boolean) => {
    if (typeof bool === 'boolean') {
      visible.value = bool
    } else {
      visible.value = !visible.value
    }
  }

  /**
   * Auto layout graph
   * @param layoutConfig
   * @returns
   */
  function format(layoutConfig: LayoutConfig) {
    if (!layoutConfig) {
      layoutConfig = DEFAULT_LAYOUT_CONFIG
    }
    const graph = graphRef?.value
    if (!graph) {
      return
    }

    graph.cleanSelection()

    let layoutFunc = null
    if (layoutConfig.type === LAYOUT_TYPE.DAGRE) {
      layoutFunc = new DagreLayout({
        type: LAYOUT_TYPE.DAGRE,
        rankdir: 'LR',
        align: 'UL',
        // Calculate the node spacing based on the edge label length
        ranksepFunc: (d) => {
          const edges = graph.getOutgoingEdges(d.id)
          let max = 0
          if (edges && edges.length > 0) {
            edges.forEach((edge) => {
              const edgeView = graph.findViewByCell(edge)
              const labelView = edgeView?.findAttr(
                'width',
                _.get(edgeView, ['labelSelectors', '0', 'body'], null)
              )
              const labelWidth = labelView ? +labelView : 0
              max = Math.max(max, labelWidth)
            })
          }
          return layoutConfig.ranksep + max
        },
        nodesep: layoutConfig.nodesep,
        controlPoints: true
      })
    } else if (layoutConfig.type === LAYOUT_TYPE.GRID) {
      layoutFunc = new GridLayout({
        type: LAYOUT_TYPE.GRID,
        preventOverlap: true,
        preventOverlapPadding: layoutConfig.padding,
        sortBy: '_index',
        rows: layoutConfig.rows || undefined,
        cols: layoutConfig.cols || undefined,
        nodeSize: 220
      })
    }
    const json = graph.toJSON()
    const nodes = json.cells
      .filter((cell) => cell.shape === X6_NODE_NAME)
      .map((item) => {
        return {
          ...item,
          // sort by code aesc
          _index: -(item.id as string)
        }
      })
    const edges = json.cells.filter((cell) => cell.shape === X6_EDGE_NAME)
    const newModel: any = layoutFunc?.layout({
      nodes: nodes,
      edges: edges
    } as any)
    graph.fromJSON(newModel)
  }

  /**
   * Auto layout modal submit
   */
  function submit() {
    if (formRef.value) {
      formRef.value.validate((errors: unknown) => {
        if (errors) return
        format(formValue.value)
        toggle(false)
      })
    }
  }

  /**
   * Auto layout modal cancel
   */
  function cancel() {
    toggle(false)
  }

  return {
    format,
    toggle,
    visible,
    formRef,
    formValue,
    cancel,
    submit
  }
}
