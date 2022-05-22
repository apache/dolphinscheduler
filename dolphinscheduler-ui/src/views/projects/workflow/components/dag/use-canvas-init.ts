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

import type { Markup, Node } from '@antv/x6'
import { ref, onMounted, Ref } from 'vue'
import { Graph } from '@antv/x6'
import { NODE, EDGE, X6_NODE_NAME, X6_EDGE_NAME } from './dag-config'
import { debounce } from 'lodash'
import { useResizeObserver } from '@vueuse/core'
import ContextMenuTool from './dag-context-menu'

interface Options {
  readonly: Ref<boolean>
  graph: Ref<Graph | undefined>
}

/**
 * Canvas Init
 * 1. Bind the graph to the dom
 * 2. Redraw when the page is resized
 * 3. Register custom graphics
 */
export function useCanvasInit(options: Options) {
  // Whether the graph can be operated
  const { readonly, graph } = options

  const paper = ref<HTMLElement>() // The graph mount HTMLElement
  const minimap = ref<HTMLElement>() // The minimap mount HTMLElement
  const container = ref<HTMLElement>() // The container of paper and minimap

  /**
   * Graph Init, bind graph to the dom
   */
  function graphInit() {
    Graph.registerNodeTool('contextmenu', ContextMenuTool, true)

    return new Graph({
      container: paper.value,
      selecting: {
        enabled: true,
        multiple: true,
        rubberband: true,
        rubberEdge: true,
        movable: true,
        showNodeSelectionBox: false
      },
      scaling: {
        min: 0.2,
        max: 2
      },
      mousewheel: {
        enabled: true,
        modifiers: ['ctrl', 'meta']
      },
      scroller: true,
      grid: {
        size: 10,
        visible: true
      },
      snapline: true,
      minimap: {
        enabled: true,
        container: minimap.value,
        scalable: false,
        width: 200,
        height: 120
      },
      interacting: {
        edgeLabelMovable: false,
        nodeMovable: !readonly.value,
        magnetConnectable: !readonly.value
      },
      connecting: {
        // Whether multiple edges can be created between the same start node and end
        allowMulti: false,
        // Whether a point is allowed to connect to a blank position on the canvas
        allowBlank: false,
        // The start node and the end node are the same node
        allowLoop: false,
        // Whether an edge is allowed to link to another edge
        allowEdge: false,
        // Whether edges are allowed to link to nodes
        allowNode: true,
        // Whether to allow edge links to ports
        allowPort: false,
        // Whether all available ports or nodes are highlighted when you drag the edge
        highlight: true,
        createEdge() {
          return graph.value?.createEdge({ shape: X6_EDGE_NAME })
        },
        validateConnection(data) {
          const { sourceCell, targetCell } = data

          if (
            sourceCell &&
            targetCell &&
            sourceCell.isNode() &&
            targetCell.isNode()
          ) {
            const sourceData = sourceCell.getData()
            if (!sourceData) return true
            if (sourceData.taskType !== 'CONDITIONS') return true
            const edges = graph.value?.getConnectedEdges(sourceCell)
            if (!edges || edges.length < 2) return true
            let len = 0
            return !edges.some((edge) => {
              if (edge.getSourceCellId() === sourceCell.id) {
                len++
              }
              return len > 2
            })
          }

          return true
        }
      },
      highlighting: {
        nodeAvailable: {
          name: 'className',
          args: {
            className: 'available'
          }
        },
        magnetAvailable: {
          name: 'className',
          args: {
            className: 'available'
          }
        },
        magnetAdsorbed: {
          name: 'className',
          args: {
            className: 'adsorbed'
          }
        }
      }
    })
  }

  onMounted(() => {
    graph.value = graphInit()
    // Make sure the edge starts with node, not port
    graph.value.on('edge:connected', ({ isNew, edge }) => {
      if (isNew) {
        const sourceNode = edge.getSourceNode() as Node
        edge.setSource(sourceNode)
      }
    })

    // Add a node tool when the mouse entering
    graph.value.on('node:mouseenter', ({ node }) => {
      const nodeName = node.getData().taskName
      const markup = node.getMarkup() as Markup.JSONMarkup[]
      const fo = markup.filter((m) => m.tagName === 'foreignObject')[0]

      node.addTools({
        name: 'button',
        args: {
          markup: [
            {
              tagName: 'text',
              textContent: nodeName,
              attrs: {
                fill: '#868686',
                'font-size': 16,
                'text-anchor': 'center'
              }
            }
          ],
          x: 0,
          y: 0,
          offset: { x: 0, y: fo ? -28 : -10 }
        }
      })
    })

    // Remove all tools when the mouse leaving
    graph.value.on('node:mouseleave', ({ node }) => {
      node.removeTool('button')
    })
  })

  /**
   * Redraw when the page is resized
   */
  const resize = debounce(() => {
    if (container.value && true) {
      const w = container.value.offsetWidth
      const h = container.value.offsetHeight
      graph.value?.resize(w, h)
    }
  }, 200)
  useResizeObserver(container, resize)

  /**
   * Register custom cells
   */
  function registerCustomCells() {
    Graph.unregisterNode(X6_NODE_NAME)
    Graph.unregisterEdge(X6_EDGE_NAME)
    Graph.registerNode(X6_NODE_NAME, { ...NODE })
    Graph.registerEdge(X6_EDGE_NAME, { ...EDGE })
  }
  onMounted(() => {
    registerCustomCells()
  })

  return {
    graph,
    paper,
    minimap,
    container
  }
}
