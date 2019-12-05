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
import _ from 'lodash'
import $ from 'jquery'
import * as d3 from 'd3'
import { formatDate } from '@/module/filter/filter'
import { tasksState } from '@/conf/home/pages/dag/_source/config'

let Gantt = function () {
  this.el = ''
  this.tasks = []
  this.width = null
  this.height = null
  this.tasksName = []
  this.tickFormat = `%H:%M:%S`
  this.margin = {
    top: 10,
    right: 40,
    bottom: 10,
    left: 300
  }
  this.startTimeXAxis = d3.time.day.offset(new Date(), -3)
  this.endTimeXAxis = d3.time.hour.offset(new Date(), +3)
}

Gantt.prototype.init = function ({ el, tasks }) {
  this.el = el
  this.tasks = tasks
  this.tasksName = _.map(_.cloneDeep(tasks), v => v.taskName)
  this.height = parseInt(this.tasksName.length * 30)
  this.width = $(this.el).width() - this.margin.right - this.margin.left - 5

  this.x = d3.time.scale()
    .domain([ this.startTimeXAxis, this.endTimeXAxis ])
    .range([ 0, this.width ])
    .clamp(true)

  this.y = d3.scale.ordinal()
    .domain(this.tasksName)
    .rangeRoundBands([ 0, this.height - this.margin.top - this.margin.bottom ], 0.1)

  this.xAxis = d3.svg.axis()
    .scale(this.x)
    .orient('bottom')
    .tickFormat(d3.time.format(this.tickFormat))
    .tickSubdivide(true)
    .tickSize(8)
    .tickPadding(8)

  this.yAxis = d3.svg.axis()
    .scale(this.x)
    .orient('left')
    .tickSize(0)

  // 时间纬度
  this.compXAxisTimes()
  // 时间刻度计算
  this.initializeXAxis()
  // 绘制图表
  this.drawChart()
}

/**
 * 计算时间纬度
 */
Gantt.prototype.compXAxisTimes = function () {
  if (this.tasks === undefined || this.tasks.length < 1) {
    this.startTimeXAxis = d3.time.day.offset(new Date(), -3)
    this.endTimeXAxis = d3.time.hour.offset(new Date(), +3)
    return
  }
  this.tasks.sort((a, b) => a.endDate - b.endDate)
  this.endTimeXAxis = this.tasks[this.tasks.length - 1].endDate
  this.tasks.sort((a, b) => a.startDate - b.startDate)
  this.startTimeXAxis = this.tasks[0].startDate
}

/**
 * 时间刻度处理
 */
Gantt.prototype.initializeXAxis = function () {
  this.x = d3.time.scale()
    .domain([ this.startTimeXAxis, this.endTimeXAxis ])
    .range([ 0, this.width ])
    .clamp(true)

  this.y = d3.scale.ordinal()
    .domain(this.tasksName)
    .rangeRoundBands([ 0, this.height - this.margin.top - this.margin.bottom ], 0.1)

  this.xAxis = d3.svg.axis()
    .scale(this.x)
    .orient('bottom')
    .tickFormat(d3.time.format(this.tickFormat))
    .tickSubdivide(true)
    .tickSize(8).tickPadding(8)

  this.yAxis = d3.svg.axis()
    .scale(this.y)
    .orient('left')
    .tickSize(0)
}

/**
 * 绘制图表
 */
Gantt.prototype.drawChart = function () {
  const svg = d3.select(this.el)
    .append('svg')
    .attr('class', 'chart')
    .attr('width', this.width + this.margin.left + this.margin.right)
    .attr('height', this.height + this.margin.top + this.margin.bottom + 150)
    .append('g')
    .attr('class', 'gantt-chart')
    .attr('width', this.width + this.margin.left + this.margin.right)
    .attr('height', this.height + this.margin.top + this.margin.bottom + 150)
    .attr('transform', 'translate(' + this.margin.left + ', ' + this.margin.top + ')')

  svg.selectAll('.chart')
    .data(this.tasks, d => d.startDate + d.taskName + d.endDate).enter()
    .append('rect')
    .attr('fill', d => d.status ? tasksState[d.status].color : '#000')
    .attr('data-toggle', 'tooltip')
    .attr('class', 'nodesp')
    .attr('data-container', 'body')
    .attr('title', this.tip)
    .attr('y', 0)
    .attr('transform', d => 'translate(' + this.x(d.startDate) + ',' + this.y(d.taskName) + ')')
    .attr('height', () => this.y.rangeBand())
    .attr('width', d => d3.max([this.x(d.endDate) - this.x(d.startDate), 1]))

  svg.append('g')
    .attr('class', 'x axis')
    .attr('transform', 'translate(0, ' + (this.height - this.margin.top - this.margin.bottom) + ')')
    .transition()
    .call(this.xAxis)
    .selectAll("text")
      .attr("transform", `rotate(-${this.width / ($('.tick').length - 1) > 50 ? 0 : Math.acos(this.width / ($('.tick').length - 1) / 50) * 57 })`)
      .style("text-anchor", `${this.width / ($('.tick').length - 1) > 50 ? 'middle' : 'end'}`)

  svg.append('g')
    .attr('class', 'y axis')
    .transition()
    .call(this.yAxis)

  $('rect.nodesp').tooltip({
    html: true,
    container: 'body'
  })
}
/**
 * tip提示
 */
Gantt.prototype.tip = function (d) {
  let str = ''
  str += `<div class="d3-toottip">`
  str += `<ul>`
  str += `<li><span class="sp1">taskName :</span><span>${d.taskName}</span></li>`
  str += `<li><span class="sp1">status :</span><span>${tasksState[d.status].desc} (${d.status})</span></li>`
  str += `<li><span class="sp1">startTime :</span><span>${formatDate(d.isoStart)}</span></li>`
  str += `<li><span class="sp1">endTime :</span><span>${formatDate(d.isoEnd)}</span></li>`
  str += `<li><span class="sp1">duration :</span><span>${d.duration}</span></li>`
  str += `</ul>`
  str += `</div>`
  return str
}


export default new Gantt()
