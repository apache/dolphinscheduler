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
import i18n from '@/module/i18n'
import store from '@/conf/home/store'

/**
 * 节点,转数组
 */
const rtTargetarrArr = (id) => {
  let a = $(`#${id}`).attr('data-targetarr')
  return a ? a.split(',') : []
}

/**
 * 存储节点id到targetarr
 */
const saveTargetarr = (valId, domId) => {
  let $target = $(`#${domId}`)
  let targetStr = $target.attr('data-targetarr') ? $target.attr('data-targetarr') + `,${valId}` : `${valId}`
  $target.attr('data-targetarr', targetStr)
}

/**
 * 返回节点html
 */
const rtTasksTpl = ({ id, name, x, y, targetarr, isAttachment, taskType,runFlag }) => {
  let tpl = ``
  tpl += `<div class="w jtk-draggable jtk-droppable jtk-endpoint-anchor jtk-connected ${isAttachment ? 'jtk-ep' : ''}" data-targetarr="${targetarr || ''}" data-tasks-type="${taskType}" id="${id}" style="left: ${x}px; top: ${y}px;">`
  tpl += `<div>`
    tpl += `<div class="state-p"></div>`
    tpl += `<div class="icos icos-${taskType}"></div>`
    tpl += `<span class="name-p">${name}</span>`
  tpl += `</div>`
  tpl += `<div class="ep"></div>`
    tpl += `<div class="ban-p">`
    if (runFlag === 'FORBIDDEN') {
      tpl += `<i class="iconfont" data-toggle="tooltip" data-html="true" data-container="body" data-placement="left" title="${i18n.$t('禁止执行')}">&#xe63e;</i>`
    }
    tpl += `</div>`
  tpl += `</div>`

  return tpl
}

/**
 * 获取所有tasks节点
 */
const tasksAll = () => {
  let a = []
  $('#canvas .w').each(function (idx, elem) {
    let e = $(elem)
    a.push({
      id: e.attr('id'),
      name: e.find('.name-p').text(),
      targetarr: e.attr('data-targetarr') || '',
      x: parseInt(e.css('left'), 10),
      y: parseInt(e.css('top'), 10)
    })
  })
  return a
}

/**
 * 判断 name 是否在当前的dag图中
 * rely dom / backfill dom元素 回填
 */
const isNameExDag = (name, rely) => {
  if (rely === 'dom') {
    return _.findIndex(tasksAll(), v => v.name === name) !== -1
  } else {
    return _.findIndex(store.state.dag.tasks, v => v.name === name) !== -1
  }
}

/**
 * 更改svg线条颜色
 */
const setSvgColor = (e, color) => {
  // 遍历 清除所有颜色
  $('.jtk-connector').each((i, o) => {
    _.map($(o)[0].childNodes, v => {
      $(v).attr('fill', '#555').attr('stroke', '#555').attr('stroke-width', 2)
    })
  })

  // 给选择的添加颜色
  _.map($(e.canvas)[0].childNodes, (v, i) => {
    $(v).attr('fill', color).attr('stroke', color)
    if ($(v).attr('class')) {
      $(v).attr('stroke-width', 2)
    }
  })
}

/**
 * 获取所有节点id
 */
const allNodesId = () => {
  let idArr = []
  $('.w').each((i, o) => {
    let $obj = $(o)
    let $span = $obj.find('.name-p').text()
    if ($span) {
      idArr.push({
        id: $obj.attr('id'),
        name: $span
      })
    }
  })
  return idArr
}

export {
  rtTargetarrArr,
  saveTargetarr,
  rtTasksTpl,
  tasksAll,
  isNameExDag,
  setSvgColor,
  allNodesId
}
