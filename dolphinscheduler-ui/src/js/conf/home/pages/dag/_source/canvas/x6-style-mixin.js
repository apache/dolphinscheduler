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

import {
  NODE,
  EDGE,
  PORT,
  NODE_HOVER,
  PORT_HOVER,
  EDGE_HOVER,
  PORT_SELECTED,
  NODE_SELECTED,
  EDGE_SELECTED,
  X6_PORT_OUT_NAME
} from './x6-helper'
import _ from 'lodash'

export default {
  data () {
    return {
      hoverCell: null
    }
  },
  methods: {
    bindStyleEvent (graph) {
      // nodes and edges hover
      graph.on('cell:mouseenter', (data) => {
        const { cell, e } = data
        const isStatusIcon = (tagName) =>
          tagName &&
          (tagName.toLocaleLowerCase() === 'em' ||
            tagName.toLocaleLowerCase() === 'body')
        if (!isStatusIcon(e.target.tagName)) {
          this.hoverCell = cell
          this.updateCellStyle(cell, graph)
        }
      })
      graph.on('cell:mouseleave', ({ cell }) => {
        this.hoverCell = null
        this.updateCellStyle(cell, graph)
      })
      // select
      graph.on('cell:selected', ({ cell }) => {
        this.updateCellStyle(cell, graph)
      })
      graph.on('cell:unselected', ({ cell }) => {
        this.updateCellStyle(cell, graph)
      })
    },
    updateCellStyle (cell, graph) {
      if (cell.isEdge()) {
        this.setEdgeStyle(cell, graph)
      } else if (cell.isNode()) {
        this.setNodeStyle(cell, graph)
      }
    },
    /**
     * Set node style
     * @param {Node} node
     * @param {Graph} graph
     */
    setNodeStyle (node, graph) {
      const isHover = node === this.hoverCell
      const isSelected = graph.isSelected(node)
      const portHover = _.cloneDeep(PORT_HOVER.groups[X6_PORT_OUT_NAME].attrs)
      const portSelected = _.cloneDeep(PORT_SELECTED.groups[X6_PORT_OUT_NAME].attrs)
      const portDefault = _.cloneDeep(PORT.groups[X6_PORT_OUT_NAME].attrs)
      const nodeHover = _.merge(_.cloneDeep(NODE.attrs), NODE_HOVER.attrs)
      const nodeSelected = _.merge(_.cloneDeep(NODE.attrs), NODE_SELECTED.attrs)

      let img = null
      let nodeAttrs = null
      let portAttrs = null

      if (isHover || isSelected) {
        img = require(`../images/task-icos/${node.data.taskType.toLocaleLowerCase()}_hover.png`)
        if (isHover) {
          nodeAttrs = nodeHover
          portAttrs = _.merge(portDefault, portHover)
        } else {
          nodeAttrs = nodeSelected
          portAttrs = _.merge(portDefault, portSelected)
        }
      } else {
        img = require(`../images/task-icos/${node.data.taskType.toLocaleLowerCase()}.png`)
        nodeAttrs = NODE.attrs
        portAttrs = portDefault
      }
      node.setAttrByPath('image/xlink:href', img)
      node.setAttrs(nodeAttrs)
      node.setPortProp(
        X6_PORT_OUT_NAME,
        'attrs',
        portAttrs
      )
    },
    /**
     * Set edge style
     * @param {Edge} edge
     * @param {Graph} graph
     */
    setEdgeStyle (edge, graph) {
      const isHover = edge === this.hoverCell
      const isSelected = graph.isSelected(edge)
      const labelName = this.getEdgeLabelName ? this.getEdgeLabelName(edge) : ''
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
            },
            {
              attrs: { label: { text: labelName } }
            }
          )
        }
      ])
    }
  }
}
