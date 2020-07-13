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
import canvg from 'canvg'
import { tasksAll } from './util'
import html2canvas from 'html2canvas'
import { findComponentDownward } from '@/module/util/'

const DownChart = function () {
  this.dag = {}
}

/**
 * Get interception location information
 */
DownChart.prototype.maxVal = function () {
  return new Promise((resolve, reject) => {
    // All nodes
    const tasksAllList = tasksAll()
    const dom = $('.dag-container')
    const y = parseInt(_.maxBy(tasksAllList, 'y').y + 60)
    const x = parseInt(_.maxBy(tasksAllList, 'x').x + 100)

    resolve({
      width: (x > 600 ? x : dom.width()) + 100,
      height: (y > 500 ? y : dom.height()) + 100
    })
  })
}

/**
 * Download to image
 */
DownChart.prototype.download = function ({ dagThis }) {
  this.dag = dagThis

  this.maxVal().then(({ width, height }) => {
    // Dom to save
    const copyDom = $('#canvas')
    // gain
    const scale = 1
    // divReport is the id of the dom that needs to be intercepted into a picture
    const svgElem = copyDom.find('svg')
    svgElem.each((index, node) => {
      // svg handle
      const nodesToRecover = []
      const nodesToRemove = []
      const parentNode = node.parentNode
      const svg = node.outerHTML.trim()
      const canvas = document.createElement('canvas')
      canvg(canvas, svg)
      if (node.style.position) {
        canvas.style.position += node.style.position
        canvas.style.left += node.style.left
        canvas.style.top += node.style.top
      }
      nodesToRecover.push({
        parent: parentNode,
        child: node
      })
      parentNode.removeChild(node)
      nodesToRemove.push({
        parent: parentNode,
        child: canvas
      })
      parentNode.appendChild(canvas)
    })

    const canvas = document.createElement('canvas')
    // canvas width
    canvas.width = width * scale
    // canvas height
    canvas.height = height * scale

    const content = canvas.getContext('2d')
    content.scale(scale, scale)
    // Get the offset of the element relative to the inspection
    const rect = copyDom.get(0).getBoundingClientRect()
    // Set the context position, the value is a negative value relative to the window offset, let the picture reset
    content.translate(-rect.left, -rect.top)

    html2canvas(copyDom[0], {
      dpi: window.devicePixelRatio * 2,
      scale: scale,
      width: width,
      canvas: canvas,
      heigth: height,
      useCORS: true // Enable cross-domain configuration
    }).then((canvas) => {
      const name = `${this.dag.name}.png`
      const url = canvas.toDataURL('image/png', 1)
      setTimeout(() => {
        const triggerDownload = $('<a>').attr('href', url).attr('download', name).appendTo('body')
        triggerDownload[0].click()
        triggerDownload.remove()
      }, 100)

      // To refresh the dag instance, otherwise you can't re-plot
      setTimeout(() => {
        // Refresh current dag
        findComponentDownward(this.dag.$root, `${this.dag.type}-details`).init()
      }, 500)
    })
  })
}

export default new DownChart()
