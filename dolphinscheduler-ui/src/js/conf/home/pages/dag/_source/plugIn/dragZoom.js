import d3 from 'd3'
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

const DragZoom = function () {
  this.element = {}
  this.zoom = {}
  this.scale = 1
}

DragZoom.prototype.init = function () {
  const $canvas = $('#canvas')
  this.element = d3.select('#canvas')
  this.zoom = d3.behavior.zoom()
    .scaleExtent([0.5, 2])
    .on('zoom', () => {
      this.scale = d3.event.scale
      $canvas.css('transform', 'scale(' + this.scale + ')')
      $canvas.css('transform-origin', '0 0')
    })
  this.element.call(this.zoom).on('dblclick.zoom', null)
}

export default new DragZoom()
