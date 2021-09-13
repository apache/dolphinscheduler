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
<template>
  <div class="dag-canvas">
    <dag-taskbar @on-drag-start="onDragStart" />
    <div
      class="dag-container"
      ref="container"
      @dragenter.prevent
      @dragover.prevent
      @dragleave.prevent
      @drop.stop.prevent="onDrop"
    >
      <div ref="paper" class="paper"></div>
      <div ref="minimap" class="minimap"></div>
      <context-menu ref="contextMenu" />
    </div>
  </div>
</template>

<script>
  import _ from 'lodash'
  import { Graph, DataUri } from '@antv/x6'
  import dagTaskbar from './taskbar.vue'
  import contextMenu from './contextMenu.vue'
  import {
    NODE_PROPS,
    EDGE_PROPS,
    PORT_PROPS,
    X6_NODE_NAME,
    X6_PORT_OUT_NAME,
    X6_PORT_IN_NAME,
    X6_EDGE_NAME,
    NODE_HIGHLIGHT_PROPS,
    PORT_HIGHLIGHT_PROPS,
    EDGE_HIGHLIGHT_PROPS,
    NODE_STATUS_MARKUP
  } from './x6-helper'
  import { DagreLayout } from '@antv/layout'
  import { tasksType, tasksState } from '../config'
  import { mapActions, mapMutations, mapState } from 'vuex'
  import nodeStatus from './nodeStatus'

  export default {
    name: 'dag-canvas',
    data () {
      return {
        graph: null,
        // Used to calculate the context menu location
        originalScrollPosition: {
          left: 0,
          top: 0
        },
        editable: true,
        dragging: {
          // Distance from the mouse to the top-left corner of the dragging element
          x: 0,
          y: 0,
          type: ''
        }
      }
    },
    provide () {
      return {
        dagCanvas: this
      }
    },
    inject: ['dagChart'],
    components: {
      dagTaskbar,
      contextMenu
    },
    computed: {
      ...mapState('dag', ['tasks'])
    },
    methods: {
      ...mapActions('dag', ['genTaskCodeList']),
      ...mapMutations('dag', ['removeTask']),
      /**
       * Recalculate the paper width and height
       */
      paperResize () {
        const w = this.$el.offsetWidth
        const h = this.$el.offsetHeight
        this.graph.resize(w, h)
      },
      /**
       * Init graph
       * This will be called in the dag-chart mounted event
       * @param {boolean} uneditable
       */
      graphInit (editable) {
        const self = this
        this.editable = !!editable
        const paper = this.$refs.paper
        const minimap = this.$refs.minimap
        const graph = (this.graph = new Graph({
          container: paper,
          selecting: {
            enabled: true,
            multiple: true,
            rubberband: true,
            rubberEdge: true,
            movable: true,
            showNodeSelectionBox: false
          },
          scroller: true,
          grid: {
            size: 10,
            visible: true
          },
          snapline: true,
          minimap: {
            enabled: true,
            container: minimap
          },
          interacting: {
            edgeLabelMovable: false,
            nodeMovable: !!editable,
            magnetConnectable: !!editable
          },
          connecting: {
            snap: {
              radius: 30
            },
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
            allowPort: true,
            // Whether all available ports or nodes are highlighted when you drag the edge
            highlight: true,
            createEdge () {
              return graph.createEdge({ shape: X6_EDGE_NAME })
            },
            validateMagnet ({ magnet }) {
              return magnet.getAttribute('port-group') !== X6_PORT_IN_NAME
            },
            validateConnection (data) {
              const { sourceCell, targetCell, sourceMagnet, targetMagnet } = data
              // Connections can only be created from the output link post
              if (
                !sourceMagnet ||
                sourceMagnet.getAttribute('port-group') !== X6_PORT_OUT_NAME
              ) {
                return false
              }

              // Can only be connected to the input link post
              if (
                !targetMagnet ||
                targetMagnet.getAttribute('port-group') !== X6_PORT_IN_NAME
              ) {
                return false
              }

              if (
                sourceCell &&
                targetCell &&
                sourceCell.isNode() &&
                targetCell.isNode()
              ) {
                const edgeData = {
                  sourceId: Number(sourceCell.id),
                  targetId: Number(targetCell.id)
                }
                if (!self.edgeIsValid(edgeData)) {
                  return false
                }
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
        }))
        this.registerX6Shape()
        this.bindGraphEvent()
        this.originalScrollPosition = graph.getScrollbarPosition()
      },
      /**
       * Register custom shapes
       */
      registerX6Shape () {
        Graph.unregisterNode(X6_NODE_NAME)
        Graph.unregisterEdge(X6_EDGE_NAME)
        Graph.registerNode(X6_NODE_NAME, { ...NODE_PROPS })
        Graph.registerEdge(X6_EDGE_NAME, { ...EDGE_PROPS })
      },
      /**
       * Bind grap event
       */
      bindGraphEvent () {
        // nodes and edges hover
        this.graph.on('cell:mouseenter', (data) => {
          const { cell, e } = data
          const isStatusIcon = (tagName) =>
            tagName &&
            (tagName.toLocaleLowerCase() === 'em' ||
              tagName.toLocaleLowerCase() === 'body')
          if (!isStatusIcon(e.target.tagName)) {
            this.setHighlight(cell)
          }
        })
        this.graph.on('cell:mouseleave', ({ cell }) => {
          if (!this.graph.isSelected(cell)) {
            this.resetHighlight(cell)
          }
        })
        // select
        this.graph.on('cell:selected', ({ cell }) => {
          this.setHighlight(cell)
        })
        this.graph.on('cell:unselected', ({ cell }) => {
          if (!this.graph.isSelected(cell)) {
            this.resetHighlight(cell)
          }
        })
        // right click
        this.graph.on('node:contextmenu', ({ x, y, cell }) => {
          const { left, top } = this.graph.getScrollbarPosition()
          const o = this.originalScrollPosition
          this.$refs.contextMenu.show(x + (o.left - left), y + (o.top - top))
          this.$refs.contextMenu.setCurrentTask({
            name: cell.data.taskName,
            type: cell.data.taskType,
            code: Number(cell.id)
          })
        })
        // node double click
        this.graph.on('node:dblclick', ({ cell }) => {
          this.dagChart.openFormModel(Number(cell.id), cell.data.taskType)
        })
        // create edge label
        this.graph.on('edge:dblclick', ({ cell }) => {
          const labelName = this.getEdgeLabelName(cell)
          this.dagChart.$refs.edgeEditModel.show({
            id: cell.id,
            label: labelName
          })
        })
      },
      /**
       * @param {Edge|string} edge
       */
      getEdgeLabelName (edge) {
        if (typeof edge === 'string') edge = this.graph.getCellById(edge)
        const labels = edge.getLabels()
        const labelName = _.get(labels, ['0', 'attrs', 'label', 'text'], '')
        return labelName
      },
      /**
       * Set edge label by id
       * @param {string} id
       * @param {string} label
       */
      setEdgeLabel (id, label) {
        const edge = this.graph.getCellById(id)
        edge.setLabels(label)
        if (this.graph.isSelected(edge)) {
          this.setEdgeHighlight(edge)
        }
      },
      /**
       * @param {number} limit
       * @param {string} text
       * Each Chinese character is equal to two chars
       */
      truncateText (text, n) {
        const exp = /[\u4E00-\u9FA5]/
        let res = ''
        let len = text.length
        let chinese = text.match(new RegExp(exp, 'g'))
        if (chinese) {
          len += chinese.length
        }
        if (len > n) {
          let i = 0
          let acc = 0
          while (true) {
            let char = text[i]
            if (exp.test(char)) {
              acc += 2
            } else {
              acc++
            }
            if (acc > n) break
            res += char
            i++
          }
          res += '...'
        } else {
          res = text
        }
        return res
      },
      /**
       * Set node name by id
       * @param {string|number} id
       * @param {string} name
       */
      setNodeName (id, name) {
        id += ''
        const node = this.graph.getCellById(id)
        if (node) {
          const truncation = this.truncateText(name, 18)
          node.attr('title/text', truncation)
          node.setData({ taskName: name })
        }
      },
      /**
       * Set node highlight
       * @param {Node} node
       */
      setNodeHighlight (node) {
        const url = require(`../images/task-icos/${node.data.taskType.toLocaleLowerCase()}_hover.png`)
        node.setAttrs(NODE_HIGHLIGHT_PROPS.attrs)
        node.setAttrByPath('image/xlink:href', url)
        node.setPortProp(
          X6_PORT_OUT_NAME,
          'attrs',
          PORT_HIGHLIGHT_PROPS[X6_PORT_OUT_NAME].attrs
        )
      },
      /**
       * Reset node style
       * @param {Node} node
       */
      resetNodeStyle (node) {
        const url = require(`../images/task-icos/${node.data.taskType.toLocaleLowerCase()}.png`)
        node.setAttrs(NODE_PROPS.attrs)
        node.setAttrByPath('image/xlink:href', url)
        node.setPortProp(
          X6_PORT_OUT_NAME,
          'attrs',
          PORT_PROPS.groups[X6_PORT_OUT_NAME].attrs
        )
      },
      /**
       * Set edge highlight
       * @param {Edge} edge
       */
      setEdgeHighlight (edge) {
        const labelName = this.getEdgeLabelName(edge)
        edge.setAttrs(EDGE_HIGHLIGHT_PROPS.attrs)
        edge.setLabels([
          _.merge(
            {
              attrs: _.cloneDeep(EDGE_HIGHLIGHT_PROPS.defaultLabel.attrs)
            },
            {
              attrs: { label: { text: labelName } }
            }
          )
        ])
      },
      /**
       * Reset edge style
       * @param {Edge} edge
       */
      resetEdgeStyle (edge) {
        const labelName = this.getEdgeLabelName(edge)
        edge.setAttrs(EDGE_PROPS.attrs)
        edge.setLabels([
          {
            ..._.merge(
              {
                attrs: _.cloneDeep(EDGE_PROPS.defaultLabel.attrs)
              },
              {
                attrs: { label: { text: labelName } }
              }
            )
          }
        ])
      },
      /**
       * Set cell highlight
       * @param {Cell} cell
       */
      setHighlight (cell) {
        if (cell.isEdge()) {
          this.setEdgeHighlight(cell)
        } else if (cell.isNode()) {
          this.setNodeHighlight(cell)
        }
      },
      /**
       * Reset cell highlight
       * @param {Cell} cell
       */
      resetHighlight (cell) {
        if (cell.isEdge()) {
          this.resetEdgeStyle(cell)
        } else if (cell.isNode()) {
          this.resetNodeStyle(cell)
        }
      },
      /**
       * Convert the graph to JSON
       * @return {{cells:Cell[]}}
       */
      toJSON () {
        return this.graph.toJSON()
      },
      /**
       * Generate graph with JSON
       */
      fromJSON (json) {
        this.graph.fromJSON(json)
      },
      /**
       * getNodes
       * @return {Node[]}
       */
      // interface Node {
      //   id: number;
      //   position: {x:number;y:number};
      //   data: {taskType:string;taskName:string;}
      // }
      getNodes () {
        const nodes = this.graph.getNodes()
        return nodes.map((node) => {
          const position = node.getPosition()
          const data = node.getData()
          return {
            id: Number(node.id),
            position: position,
            data: data
          }
        })
      },
      /**
       * getEdges
       * @return {Edge[]} Edge is inherited from the Cell
       */
      // interface Edge {
      //   id: string;
      //   label: string;
      //   sourceId: number;
      //   targetId: number;
      // }
      getEdges () {
        const edges = this.graph.getEdges()
        return edges.map((edge) => {
          const labelData = edge.getLabelAt(0)
          return {
            id: edge.id,
            label: _.get(labelData, ['attrs', 'label', 'text'], ''),
            sourceId: Number(edge.getSourceCellId()),
            targetId: Number(edge.getTargetCellId())
          }
        })
      },
      /**
       * downloadPNG
       * @param {string} filename
       */
      downloadPNG (fileName = 'chart') {
        this.graph.toPNG(
          (dataUri) => {
            DataUri.downloadDataUri(dataUri, `${fileName}.png`)
          },
          {
            padding: {
              top: 50,
              right: 50,
              bottom: 50,
              left: 50
            },
            backgroundColor: '#f2f3f7'
          }
        )
      },
      /**
       * format
       * @desc Auto layout use @antv/layout
       */
      format () {
        const dagreLayout = new DagreLayout({
          type: 'dagre',
          rankdir: 'LR',
          align: 'UL',
          // Calculate the node spacing based on the edge label length
          ranksepFunc: (d) => {
            const edges = this.graph.getOutgoingEdges(d.id)
            let max = 0
            if (edges && edges.length > 0) {
              edges.forEach((edge) => {
                const edgeView = this.graph.findViewByCell(edge)
                const labelWidth = +edgeView.findAttr(
                  'width',
                  _.get(edgeView, ['labelSelectors', '0', 'body'], null)
                )
                max = Math.max(max, labelWidth)
              })
            }
            return 50 + max
          },
          nodesep: 50,
          controlPoints: true
        })
        const json = this.toJSON()
        const nodes = json.cells.filter((cell) => cell.shape === X6_NODE_NAME)
        const edges = json.cells.filter((cell) => cell.shape === X6_EDGE_NAME)
        const newModel = dagreLayout.layout({
          nodes: nodes,
          edges: edges
        })
        this.fromJSON(newModel)
      },
      /**
       * add a node to the graph
       * @param {string|number} id
       * @param {string} taskType
       * @param {{x:number;y:number}} coordinate Default is { x: 100, y: 100 }
       */
      addNode (id, taskType, coordinate = { x: 100, y: 100 }) {
        id += ''
        if (!tasksType[taskType]) {
          console.warn(`taskType:${taskType} is invalid!`)
          return
        }
        const node = this.genNodeJSON(id, taskType, '', coordinate)
        this.graph.addNode(node)
      },
      /**
       * generate node json
       * @param {number|string} id
       * @param {string} taskType
       * @param {{x:number;y:number}} coordinate Default is { x: 100, y: 100 }
       */
      genNodeJSON (id, taskType, taskName, coordinate = { x: 100, y: 100 }) {
        id += ''
        const url = require(`../images/task-icos/${taskType.toLocaleLowerCase()}.png`)
        const truncation = taskName ? this.truncateText(taskName, 18) : id
        return {
          id: id,
          shape: X6_NODE_NAME,
          x: coordinate.x,
          y: coordinate.y,
          data: {
            taskType: taskType,
            taskName: taskName
          },
          attrs: {
            image: {
              // Use href instead of xlink:href, you may lose the icon when downloadPNG
              'xlink:href': url
            },
            title: {
              text: truncation
            }
          }
        }
      },
      /**
       * generate edge json
       * @param {number|string} sourceId
       * @param {number|string} targetId
       * @param {string} label
       */
      genEdgeJSON (sourceId, targetId, label = '') {
        sourceId += ''
        targetId += ''
        return {
          shape: X6_EDGE_NAME,
          source: {
            cell: sourceId,
            port: X6_PORT_OUT_NAME
          },
          target: {
            cell: targetId,
            port: X6_PORT_IN_NAME
          },
          labels: label ? [label] : undefined
        }
      },
      /**
       * remove a node
       * @param {string|number} id NodeId
       */
      removeNode (id) {
        id += ''
        this.graph.removeNode(id)
        this.removeTask(id)
      },
      /**
       * remove an edge
       * @param {string} id EdgeId
       */
      removeEdge (id) {
        this.graph.removeEdge(id)
      },
      /**
       * remove multiple cells
       * @param {Cell[]} cells
       */
      removeCells (cells) {
        this.graph.removeCells(cells)
        cells.forEach((cell) => {
          if (cell.isNode()) {
            this.removeTask(+cell.id)
          }
        })
      },
      /**
       * Verify whether edge is valid
       * The number of edges start with CONDITIONS task cannot be greater than 2
       */
      edgeIsValid (edge) {
        const { sourceId } = edge
        const sourceTask = this.tasks.find((task) => task.code === sourceId)
        if (sourceTask.taskType === 'CONDITIONS') {
          const edges = this.getEdges()
          return edges.filter((e) => e.sourceId === sourceTask.code).length <= 2
        }
        return true
      },
      /**
       * Gets the current selections
       * @return {Cell[]}
       */
      getSelections () {
        return this.graph.getSelectedCells()
      },
      /**
       * Lock scroller
       */
      lockScroller () {
        this.graph.lockScroller()
      },
      /**
       * Unlock scroller
       */
      unlockScroller () {
        this.graph.unlockScroller()
      },
      /**
       * set node status icon
       * @param {number} code
       * @param {string} state
       */
      setNodeStatus ({ code, state, taskInstance }) {
        code += ''
        const stateProps = tasksState[state]
        const node = this.graph.getCellById(code)
        if (node) {
          // Destroy the previous dom
          node.removeMarkup()
          node.setMarkup(NODE_PROPS.markup.concat(NODE_STATUS_MARKUP))
          const nodeView = this.graph.findViewByCell(node)
          const el = nodeView.find('div')[0]
          nodeStatus({
            stateProps,
            taskInstance
          }).$mount(el)
        }
      },
      /**
       * Drag && Drop Event
       */
      onDragStart (e, taskType) {
        if (!this.editable) {
          e.preventDefault()
          return
        }
        this.dragging = {
          x: e.offsetX,
          y: e.offsetY,
          type: taskType.name
        }
      },
      onDrop (e) {
        const { type } = this.dragging
        const { x, y } = this.calcGraphCoordinate(e.clientX, e.clientY)
        this.genTaskCodeList({
          genNum: 1
        })
          .then((res) => {
            const [code] = res
            this.addNode(code, type, { x, y })
            this.dagChart.openFormModel(code, type)
          })
          .catch((err) => {
            console.error(err)
          })
      },
      calcGraphCoordinate (mClientX, mClientY) {
        // Distance from the mouse to the top-left corner of the container;
        const { left: cX, top: cY } =
          this.$refs.container.getBoundingClientRect()
        const mouseX = mClientX - cX
        const mouseY = mClientY - cY

        // The distance that paper has been scrolled
        const { left: sLeft, top: sTop } = this.graph.getScrollbarPosition()
        const { left: oLeft, top: oTop } = this.originalScrollPosition
        const scrollX = sLeft - oLeft
        const scrollY = sTop - oTop

        // Distance from the mouse to the top-left corner of the dragging element;
        const { x: eX, y: eY } = this.dragging

        return {
          x: mouseX + scrollX - eX,
          y: mouseY + scrollY - eY
        }
      },
      /**
       * Get prev nodes by code
       * @param {number} code
       * node1 -> node2 -> node3
       * getPrevNodes(node2.code) => [node1]
       */
      getPrevNodes (code) {
        const nodes = this.getNodes()
        const edges = this.getEdges()
        const nodesMap = {}
        nodes.forEach((node) => {
          nodesMap[node.id] = node
        })
        return edges
          .filter((edge) => edge.targetId === code)
          .map((edge) => nodesMap[edge.sourceId])
      },
      /**
       * set prev nodes
       * @param {number} code
       * @param {number[]} preNodeCodes
       * @param {boolean} override If set to true, setPreNodes will delete all edges that end with the node and rebuild
       */
      setPreNodes (code, preNodeCodes, override) {
        const edges = this.getEdges()
        const currPreCodes = []
        edges.forEach((edge) => {
          if (edge.targetId === code) {
            if (override) {
              this.removeEdge(edge.id)
            } else {
              currPreCodes.push(edge.sourceId)
            }
          }
        })
        preNodeCodes.forEach((preCode) => {
          if (currPreCodes.includes(preCode) || preCode === code) return
          const edge = this.genEdgeJSON(preCode, code)
          this.graph.addEdge(edge)
        })
      },
      /**
       * Get post nodes by code
       * @param {number} code
       * node1 -> node2 -> node3
       * getPostNodes(node2.code) => [node3]
       */
      getPostNodes (code) {
        const nodes = this.getNodes()
        const edges = this.getEdges()
        const nodesMap = {}
        nodes.forEach((node) => {
          nodesMap[node.id] = node
        })
        return edges
          .filter((edge) => edge.sourceId === code)
          .map((edge) => nodesMap[edge.targetId])
      },
      /**
       * set post nodes
       * @param {number} code
       * @param {number[]} postNodeCodes
       * @param {boolean} override If set to true, setPreNodes will delete all edges that end with the node and rebuild
       */
      setPostNodes (code, postNodeCodes, override) {
        const edges = this.getEdges()
        const currPostCodes = []
        edges.forEach((edge) => {
          if (edge.sourceId === code) {
            if (override) {
              this.removeEdge(edge.id)
            } else {
              currPostCodes.push(edge.targetId)
            }
          }
        })
        postNodeCodes.forEach((postCode) => {
          if (currPostCodes.includes(postCode) || postCode === code) return
          const edge = this.genEdgeJSON(code, postCode)
          this.graph.addEdge(edge)
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./canvas";
</style>

<style lang="scss">
@import "./x6-style";
</style>
