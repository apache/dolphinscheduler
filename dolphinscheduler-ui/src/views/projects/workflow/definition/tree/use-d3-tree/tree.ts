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

// @ts-nocheck
import * as d3 from 'd3'
import { rtInstancesTooltip, rtCountMethod } from './util'

// eslint-disable-next-line @typescript-eslint/no-this-alias
let self = this

const Tree = function () {
  // eslint-disable-next-line @typescript-eslint/no-this-alias
  self = this
  this.selfTree = {}
  this.tree = function () {}
  // basic configuration
  this.config = {
    barHeight: 26,
    axisHeight: 40,
    squareSize: 10,
    squarePading: 4,
    taskNum: 25,
    nodesMax: 0
  }
  // Margin configuration
  this.config.margin = {
    top: this.config.barHeight / 2 + this.config.axisHeight,
    right: 0,
    bottom: 0,
    left: this.config.barHeight / 2
  }
  // width
  this.config.margin.width =
    960 - this.config.margin.left - this.config.margin.right
  // bar width
  this.config.barWidth = parseInt(this.config.margin.width * 0.9)
}

/**
 * init
 */
Tree.prototype.init = function ({
  data,
  limit,
  selfTree,
  taskTypeNodeOptions,
  tasksStateObj
}) {
  return new Promise((resolve) => {
    this.selfTree = selfTree
    this.config.taskNum = limit
    this.duration = 400
    this.i = 0
    this.tree = d3.tree().size([0, 46])
    this.taskTypeNodeOptions = taskTypeNodeOptions
    this.tasksStateObj = tasksStateObj

    const root = d3.hierarchy(data)
    const treeData = this.tree(root)

    const tasks = treeData.descendants()
    const links = treeData.links()
    this.tasks = tasks
    this.links = links

    this.diagonal = d3
      .linkHorizontal()
      .x((d) => d.y)
      .y((d) => d.x)

    this.svg = d3
      .select('.tree-svg')
      .append('g')
      .attr('class', 'level')
      .attr(
        'transform',
        'translate(' +
          this.config.margin.left +
          ',' +
          this.config.margin.top +
          ')'
      )

    data.x0 = 0
    data.y0 = 0

    this.squareNum = tasks[tasks.length === 1 ? 0 : 1]?.data?.instances.length

    // Calculate the maximum node length
    this.config.nodesMax = rtCountMethod(data.children)

    this.treeUpdate((this.root = data)).then(() => {
      this.treeTooltip()
      resolve()
    })
  })
}

/**
 * tasks
 */
Tree.prototype.nodesClass = function (d) {
  let sclass = 'node'
  if (d.children === undefined && d._children === undefined) {
    sclass += ' leaf'
  } else {
    sclass += ' parent'
    if (d.children === undefined) {
      sclass += ' collapsed'
    } else {
      sclass += ' expanded'
    }
  }
  return sclass
}

/**
 * tree Expand hidden
 */
Tree.prototype.treeToggles = function (e,clicked_d) { // eslint-disable-line

  self.removeTooltip()

  // eslint-disable-next-line quotes
  d3.selectAll("[task_id='" + clicked_d.data.uuid + "']").each((d) => {
    if (clicked_d !== d && d.children) {
      // eslint-disable-line
      d._children = d.children
      d.children = null
      self.treeUpdate(d)
    }
  })
  if (clicked_d._children) {
    clicked_d.children = clicked_d._children
    clicked_d._children = null
  } else {
    clicked_d._children = clicked_d.children
    clicked_d.children = null
  }
  self.treeUpdate(clicked_d)
}

/**
 * update tree
 */
Tree.prototype.treeUpdate = function (source) {
  const tasksStateObj = this.tasksStateObj

  const tasksType = {}

  this.taskTypeNodeOptions.map((v) => {
    tasksType[v.taskType] = {
      color: v.color
    }
  })

  return new Promise((resolve) => {
    const tasks = this.tasks
    const height = Math.max(
      500,
      tasks.length * this.config.barHeight +
        this.config.margin.top +
        this.config.margin.bottom
    )

    d3.select('.tree-svg')
      .transition()
      .duration(this.duration)
      .attr('height', height)

    tasks.forEach((n, i) => {
      n.x = i * this.config.barHeight
    })

    const task = this.svg.selectAll('g.node').data(tasks, (d) => {
      return d.id || (d.id = ++this.i)
    })

    const nodeEnter = task
      .enter()
      .append('g')
      .attr('class', this.nodesClass)
      .attr('transform', () => 'translate(' + source.y0 + ',' + source.x0 + ')')
      .style('opacity', 1e-6)

    // Node circle
    nodeEnter
      .append('circle')
      .attr('r', this.config.barHeight / 3)
      .attr('class', 'task')
      .attr('title', (d) => {
        return d.data.type ? d.data.type : ''
      })
      .attr('height', this.config.barHeight)
      .attr('width', (d) => this.config.barWidth - d.y)
      .style('fill', (d) =>
        d.data.type ? tasksType[d.data.type]?.color : '#fff'
      )
      .attr('task_id', (d) => {
        return d.data.uuid
      })
      .on('click', this.treeToggles)
      .on('mouseover', (e, d) => {
        self.treeTooltip(d.data.type, e)
      })
      .on('mouseout', () => {
        self.removeTooltip()
      })

    // Node text
    nodeEnter
      .append('text')
      .attr('dy', 3.5)
      .attr('dx', this.config.barHeight / 2)
      .text((d) => {
        return d.data.name
      })
      .style('fill', 'var(--n-title-text-color)')

    const translateRatio =
      this.config.nodesMax > 10 ? (this.config.nodesMax > 20 ? 10 : 30) : 60

    // Right node information
    nodeEnter
      .append('g')
      .attr('class', 'stateboxes')
      .attr(
        'transform',
        (d) =>
          'translate(' + (this.config.nodesMax * translateRatio - d.y) + ',0)'
      )
      .selectAll('rect')
      .data((d) => d.data.instances)
      .enter()
      .append('rect')
      .on('click', () => {
        this.removeTooltip()
      })
      .attr('class', 'state')
      .style(
        'fill',
        (d) => (d.state && tasksStateObj[d.state].color) || '#ffffff'
      )
      .attr('rx', (d) => (d.type ? 0 : 12))
      .attr('ry', (d) => (d.type ? 0 : 12))
      .style('shape-rendering', (d) => (d.type ? 'crispEdges' : 'auto'))
      .attr(
        'x',
        (d, i) => i * (this.config.squareSize + this.config.squarePading)
      )
      .attr('y', -(this.config.squareSize / 2))
      .attr('width', 10)
      .attr('height', 10)
      .on('mouseover', (e, d) => {
        self.treeTooltip(rtInstancesTooltip(d, tasksStateObj), e)
      })
      .on('mouseout', () => {
        self.removeTooltip()
      })

    // Convert nodes to their new location。
    nodeEnter
      .transition()
      .duration(this.duration)
      .attr('transform', (d) => 'translate(' + d.y + ',' + d.x + ')')
      .style('opacity', 1)

    // Node line
    task
      .transition()
      .duration(this.duration)
      .attr('class', this.nodesClass)
      .attr('transform', (d) => 'translate(' + d.y + ',' + d.x + ')')
      .style('opacity', 1)

    // Convert the exit node to the new location of the parent node。
    task
      .exit()
      .transition()
      .duration(this.duration)
      .attr('transform', () => 'translate(' + source.y + ',' + source.x + ')')
      .style('opacity', 1e-6)
      .remove()

    // Update link
    const link = this.svg
      .selectAll('path.link')
      .data(this.links, (d) => d.target.id)

    // Enter any new links in the previous location of the parent node。
    link
      .enter()
      .insert('path', 'g')
      .attr('class', 'link')
      .attr('d', () => {
        const o = { x: source.x0, y: source.y0 }
        return this.diagonal({ source: o, target: o })
      })
      .transition()
      .duration(this.duration)
      .attr('d', this.diagonal)

    // Transition link
    link.transition().duration(this.duration).attr('d', this.diagonal)

    // Convert the exit node to the new location of the parent node
    link
      .exit()
      .transition()
      .duration(this.duration)
      .attr('d', () => {
        const o = { x: source.x, y: source.y }
        return this.diagonal({ source: o, target: o })
      })
      .remove()

    // Hide the old position for a transition.
    tasks.forEach((d) => {
      d.x0 = d.x
      d.y0 = d.y
    })
    resolve()
  })
}

/**
 * reset
 */
Tree.prototype.reset = function () {
  // $('.d3-tree .tree').html('')
  d3.select('.d3-tree .tree-svg').html('')
}

/**
 * toottip handle
 */
Tree.prototype.treeTooltip = function (str, e) {
  if (!str) return

  this.selfTree.proxy.showTooltip = true
  this.selfTree.proxy.tooltipText = str
  this.selfTree.proxy.changeTooltip(e)
}

/**
 * Manually clear tooltip
 */
Tree.prototype.removeTooltip = function () {
  this.selfTree.proxy.showTooltip = false
}

export default new Tree()
