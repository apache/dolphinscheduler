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
import type { Node, Graph, Edge, Cell } from '@antv/x6'
import _ from 'lodash'
import {
  X6_PORT_OUT_NAME,
  PORT_HOVER,
  PORT_SELECTED,
  PORT,
  NODE,
  NODE_HOVER,
  NODE_SELECTED,
  EDGE,
  EDGE_SELECTED,
  EDGE_HOVER
} from './dag-config'

interface Options {
  graph: Ref<Graph | undefined>
}

/**
 * Change the style on cell hover and select
 */
export function useCellActive(options: Options) {
  const { graph } = options
  const hoverCell = ref()

  const isStatusIcon = (tagName: string) => {
    if (!tagName) return false
    return (
      tagName.toLocaleLowerCase() === 'em' ||
      tagName.toLocaleLowerCase() === 'body'
    )
  }

  function setEdgeStyle(edge: Edge) {
    const isHover = edge === hoverCell.value
    const isSelected = graph.value?.isSelected(edge)
    // TODO
    // const labelName = this.getEdgeLabelName ? this.getEdgeLabelName(edge) : ''
    let edgeProps = null

    if (isHover) {
      edgeProps = _.merge(_.cloneDeep(EDGE), EDGE_HOVER)
    } else if (isSelected) {
      edgeProps = _.merge(_.cloneDeep(EDGE), EDGE_SELECTED)
    } else {
      edgeProps = _.cloneDeep(EDGE)
    }

    edge.setAttrs(edgeProps.attrs)
    edge.setLabels([
      {
        ..._.merge(
          {
            attrs: _.cloneDeep(edgeProps.defaultLabel.attrs)
          }
          // {
          //   attrs: { label: { text: labelName } }
          // }
        )
      }
    ])
  }

  function setNodeStyle(node: Node) {
    const isHover = node === hoverCell.value
    const isSelected = graph.value?.isSelected(node)
    const portHover = _.cloneDeep(PORT_HOVER.groups[X6_PORT_OUT_NAME].attrs)
    const portSelected = _.cloneDeep(
      PORT_SELECTED.groups[X6_PORT_OUT_NAME].attrs
    )
    const portDefault = _.cloneDeep(PORT.groups[X6_PORT_OUT_NAME].attrs)
    const nodeHover = _.merge(_.cloneDeep(NODE.attrs), NODE_HOVER.attrs)
    const nodeSelected = _.merge(_.cloneDeep(NODE.attrs), NODE_SELECTED.attrs)

    let img = null
    let nodeAttrs = null
    let portAttrs = null

    if (isHover || isSelected) {
      img = `${import.meta.env.BASE_URL}images/task-icons/${(node.data
        .taskType !== 'FLINK_STREAM'
        ? node.data.taskType
        : 'FLINK'
      ).toLocaleLowerCase()}_hover.png`
      if (isHover) {
        nodeAttrs = nodeHover
        portAttrs = _.merge(portDefault, portHover)
      } else {
        nodeAttrs = nodeSelected
        portAttrs = _.merge(portDefault, portSelected)
      }
    } else {
      img = `${import.meta.env.BASE_URL}images/task-icons/${(node.data
        .taskType !== 'FLINK_STREAM'
        ? node.data.taskType
        : 'FLINK'
      ).toLocaleLowerCase()}.png`
      nodeAttrs = NODE.attrs
      portAttrs = portDefault
    }
    node.setAttrByPath('image/xlink:href', img)
    node.setAttrs(nodeAttrs)
    node.setPortProp(X6_PORT_OUT_NAME, 'attrs', portAttrs)
  }

  function updateCellStyle(cell: Cell) {
    if (cell.isEdge()) {
      setEdgeStyle(cell)
    } else if (cell.isNode()) {
      setNodeStyle(cell)
    }
  }

  onMounted(() => {
    if (graph.value) {
      // hover
      graph.value.on('cell:mouseenter', (data) => {
        const { cell, e } = data
        if (!isStatusIcon(e.target.tagName)) {
          hoverCell.value = cell
          updateCellStyle(cell)
        }
      })
      graph.value.on('cell:mouseleave', ({ cell }) => {
        hoverCell.value = undefined
        updateCellStyle(cell)
      })

      // select
      graph.value.on('cell:selected', ({ cell }) => {
        updateCellStyle(cell)
      })
      graph.value.on('cell:unselected', ({ cell }) => {
        updateCellStyle(cell)
      })
    }
  })

  return {
    hoverCell
  }
}
