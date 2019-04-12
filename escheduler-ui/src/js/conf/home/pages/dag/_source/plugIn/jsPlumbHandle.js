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

import Vue from 'vue'
import $ from 'jquery'
import _ from 'lodash'
import i18n from '@/module/i18n'
import { jsPlumb } from 'jsplumb'
import DragZoom from './dragZoom'
import store from '@/conf/home/store'
import router from '@/conf/home/router'
import Permissions from '@/module/permissions'
import { uuid, findComponentDownward } from '@/module/util/'
import {
  tasksAll,
  rtTasksTpl,
  setSvgColor,
  saveTargetarr,
  rtTargetarrArr } from './util'
import mStart from '@/conf/home/pages/projects/pages/definition/pages/list/_source/start'

let JSP = function () {
  this.dag = {}
  this.selectedElement = {}

  this.config = {
    // Whether to drag
    isDrag: true,
    // Whether to allow connection
    isAttachment: false,
    // Whether to drag a new node
    isNewNodes: true,
    // Whether to support double-click node events
    isDblclick: true,
    // Whether to support right-click menu events
    isContextmenu: true,
    // Whether to allow click events
    isClick: false
  }
}

/**
 * dag init
 */
JSP.prototype.init = function ({ dag, instance }) {
  // Get the dag component instance
  this.dag = dag
  // Get jsplumb instance
  this.JspInstance = instance
  // Register jsplumb connection type and configuration
  this.JspInstance.registerConnectionType('basic', {
    anchor: 'Continuous',
    connector: 'Straight' // Line type
  })

  // Initial configuration
  this.setConfig({
    isDrag: !store.state.dag.isDetails,
    isAttachment: false,
    isNewNodes: Permissions.getAuth() === false ? false : !store.state.dag.isDetails,
    isDblclick: true,
    isContextmenu: true,
    isClick: false
  })

  // Monitor line click
  this.JspInstance.bind('click', e => {
    if (this.config.isClick) {
      this.connectClick(e)
    }
  })

  // Drag and drop
  if (this.config.isNewNodes) {
    DragZoom.init()
  }
}

/**
 * set config attribute
 */
JSP.prototype.setConfig = function (o) {
  this.config = Object.assign(this.config, {}, o)
}

/**
 * Node binding event
 */
JSP.prototype.tasksEvent = function (selfId) {
  let tasks = $(`#${selfId}`)
  // Bind right event
  tasks.on('contextmenu', e => {
    this.tasksContextmenu(e)
    return false
  })

  // Binding double click event
  tasks.find('.icos').bind('dblclick', e => {
    this.tasksDblclick(e)
  })

  // Binding click event
  tasks.on('click', e => {
    this.tasksClick(e)
  })
}

/**
 * Dag node drag and drop processing
 */
JSP.prototype.draggable = function () {
  if (this.config.isNewNodes) {
    let selfId
    let self = this
    $('.toolbar-btn .roundedRect').draggable({
      scope: 'plant',
      helper: 'clone',
      containment: $('.dag-model'),
      stop: function (e, ui) {
        self.tasksEvent(selfId)

        // Dom structure is not generated without pop-up form form
        if ($(`#${selfId}`).html()) {
          // dag event
          findComponentDownward(self.dag.$root, 'dag-chart')._createNodes({
            id: selfId
          })
        }
      },
      drag: function () {
        $('body').find('.tooltip.fade.top.in').remove()
      }
    })

    $('#canvas').droppable({
      scope: 'plant',
      drop: function (ev, ui) {
        let id = 'tasks-' + Math.ceil(Math.random() * 100000) // eslint-disable-line
        // Get mouse coordinates
        let left = parseInt(ui.offset.left - $(this).offset().left)
        let top = parseInt(ui.offset.top - $(this).offset().top) - 10
        if (top < 25) {
          top = 25
        }
        // Generate template node
        $('#canvas').append(rtTasksTpl({
          id: id,
          name: id,
          x: left,
          y: top,
          isAttachment: self.config.isAttachment,
          taskType: findComponentDownward(self.dag.$root, 'dag-chart').dagBarId
        }))

        // Get the generated node
        let thisDom = jsPlumb.getSelector('.statemachine-demo .w')

        // Generating a connection node
        self.JspInstance.batch(() => {
          self.initNode(thisDom[thisDom.length - 1])
        })
        selfId = id
      }
    })
  }
}

/**
 * Echo json processing and old data structure processing
 */
JSP.prototype.jsonHandle = function ({ largeJson, locations }) {
  _.map(largeJson, v => {
    // Generate template
    $('#canvas').append(rtTasksTpl({
      id: v.id,
      name: v.name,
      x: locations[v.id]['x'],
      y: locations[v.id]['y'],
      targetarr: locations[v.id]['targetarr'],
      isAttachment: this.config.isAttachment,
      taskType: v.type,
      runFlag:v.runFlag
    }))

    // contextmenu event
    $(`#${v.id}`).on('contextmenu', e => {
      this.tasksContextmenu(e)
      return false
    })

    // dblclick event
    $(`#${v.id}`).find('.icos').bind('dblclick', e => {
      this.tasksDblclick(e)
    })

    // click event
    $(`#${v.id}`).bind('click', e => {
      this.tasksClick(e)
    })
  })
}

/**
 * Initialize a single node
 */
JSP.prototype.initNode = function (el) {
  // Whether to drag
  if (this.config.isDrag) {
    this.JspInstance.draggable(el, {
      containment: 'dag-container'
    })
  }

  // Node attribute configuration
  this.JspInstance.makeSource(el, {
    filter: '.ep',
    anchor: 'Continuous',
    connectorStyle: {
      stroke: '#555',
      strokeWidth: 2,
      outlineStroke: 'transparent',
      outlineWidth: 4
    },
    // This place is leaking
    // connectionType: "basic",
    extract: {
      action: 'the-action'
    },
    maxConnections: -1
  })

  // Node connection property configuration
  this.JspInstance.makeTarget(el, {
    dropOptions: { hoverClass: 'dragHover' },
    anchor: 'Continuous',
    allowLoopback: false // Forbid yourself to connect yourself
  })
  this.JspInstance.fire('jsPlumbDemoNodeAdded', el)
}

/**
 * Node right click menu
 */
JSP.prototype.tasksContextmenu = function (event) {
  if (this.config.isContextmenu) {
    let routerName = router.history.current.name
    // state
    let isOne = routerName === 'projects-definition-details' && this.dag.releaseState !== 'NOT_RELEASE'
    // hide
    let isTwo = store.state.dag.isDetails

    let html = [
      `<a href="javascript:" id="startRunning" class="${isOne ? '' : 'disbled'}"><i class="iconfont">&#xe60b;</i><span>${i18n.$t('开始运行')}</span></a>`,
      `<a href="javascript:" id="editNodes" class="${isTwo ? 'disbled' : ''}"><i class="iconfont">&#xe601;</i><span>${i18n.$t('编辑节点')}</span></a>`,
      `<a href="javascript:" id="copyNodes" class="${isTwo ? 'disbled' : ''}"><i class="iconfont">&#xe61e;</i><span>${i18n.$t('复制节点')}</span></a>`,
      `<a href="javascript:" id="removeNodes" class="${isTwo ? 'disbled' : ''}"><i class="iconfont">&#xe611;</i><span>${i18n.$t('删除节点')}</span></a>`
    ]

    let operationHtml = () => {
      return html.splice(',')
    }

    let e = event
    let $id = e.currentTarget.id
    let $contextmenu = $('#contextmenu')
    let $name = $(`#${$id}`).find('.name-p').text()
    let $left = e.pageX + document.body.scrollLeft - 5
    let $top = e.pageY + document.body.scrollTop - 5
    $contextmenu.css({
      left: $left,
      top: $top,
      visibility: 'visible'
    })
    // Action bar
    $contextmenu.html('').append(operationHtml)

    if (isOne) {
      // start run
      $('#startRunning').on('click', () => {
        let id = router.history.current.params.id
        store.dispatch('dag/getStartCheck', { processDefinitionId: id }).then(res => {
          let modal = Vue.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mStart, {
                on: {
                  onUpdate () {
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  item: {
                    id: id
                  },
                  startNodeList: $name,
                  sourceType: 'contextmenu'
                }
              })
            }
          })
        }).catch(e => {
          Vue.$message.error(e.msg || '')
        })
      })
    }
    if (!isTwo) {
      // edit node
      $(`#editNodes`).click(ev => {
        findComponentDownward(this.dag.$root, 'dag-chart')._createNodes({
          id: $id,
          type: $(`#${$id}`).attr('data-tasks-type')
        })
      })
      // delete node
      $('#removeNodes').click(ev => {
        this.removeNodes($id)
      })

      // copy node
      $('#copyNodes').click(res => {
        this.copyNodes($id)
      })
    }
  }
}

/**
 * Node double click event
 */
JSP.prototype.tasksDblclick = function (e) {
  // Untie event
  if (this.config.isDblclick) {
    let id = $(e.currentTarget.offsetParent).attr('id')

    findComponentDownward(this.dag.$root, 'dag-chart')._createNodes({
      id: id,
      type: $(`#${id}`).attr('data-tasks-type')
    })
  }
}

/**
 * Node click event
 */
JSP.prototype.tasksClick = function (e) {
  let $id
  let self = this
  let $body = $(`body`)
  if (this.config.isClick) {
    let $connect = this.selectedElement.connect
    $('.w').removeClass('jtk-tasks-active')
    $(e.currentTarget).addClass('jtk-tasks-active')
    if ($connect) {
      setSvgColor($connect, '#555')
      this.selectedElement.connect = null
    }
    this.selectedElement.id = $(e.currentTarget).attr('id')

    // Unbind copy and paste events
    $body.unbind('copy').unbind('paste')
    // Copy binding id
    $id = self.selectedElement.id

    $body.bind({
      copy: function () {
        $id = self.selectedElement.id
      },
      paste: function () {
        $id && self.copyNodes($id)
      }
    })
  }
}

/**
 * Remove binding events
 * paste
 */
JSP.prototype.removePaste = function () {
  let $body = $(`body`)
  // Unbind copy and paste events
  $body.unbind('copy').unbind('paste')
  // Remove selected node parameters
  this.selectedElement.id = null
  // Remove node selection effect
  $('.w').removeClass('jtk-tasks-active')
}

/**
 * Line click event
 */
JSP.prototype.connectClick = function (e) {
  // Set svg color
  setSvgColor(e, '#0097e0')
  let $id = this.selectedElement.id
  if ($id) {
    $(`#${$id}`).removeClass('jtk-tasks-active')
    this.selectedElement.id = null
  }
  this.selectedElement.connect = e
}

/**
 * toolbarEvent
 * @param {Pointer}
 */
JSP.prototype.handleEventPointer = function (is) {
  let wDom = $('.w')
  this.setConfig({
    isClick: is,
    isAttachment: false
  })
  wDom.removeClass('jtk-ep')
  if (!is) {
    wDom.removeClass('jtk-tasks-active')
    this.selectedElement = {}
    _.map($('#canvas svg'), v => {
      if ($(v).attr('class')) {
        _.map($(v).find('path'), v1 => {
          $(v1).attr('fill', '#555')
          $(v1).attr('stroke', '#555')
        })
      }
    })
  }
}

/**
 * toolbarEvent
 * @param {Line}
 */
JSP.prototype.handleEventLine = function (is) {
  let wDom = $('.w')
  this.setConfig({
    isAttachment: is
  })
  is ? wDom.addClass('jtk-ep') : wDom.removeClass('jtk-ep')
}

/**
 * toolbarEvent
 * @param {Remove}
 */
JSP.prototype.handleEventRemove = function () {
  let $id = this.selectedElement.id || null
  let $connect = this.selectedElement.connect || null
  if ($id) {
    this.removeNodes(this.selectedElement.id)
  } else {
    this.removeConnect($connect)
  }

  // Monitor whether to edit DAG
  store.commit('dag/setIsEditDag', true)
}

/**
 * Delete node
 */
JSP.prototype.removeNodes = function ($id) {
  // Delete node processing(data-targetarr)
  _.map(tasksAll(), v => {
    let targetarr = v.targetarr.split(',')
    if (targetarr.length) {
      let newArr = _.filter(targetarr, v1 => v1 !== $id)
      $(`#${v.id}`).attr('data-targetarr', newArr.toString())
    }
  })
  // delete node
  this.JspInstance.remove($id)
}

/**
 * Delete connection
 */
JSP.prototype.removeConnect = function ($connect) {
  if (!$connect) {
    return
  }
  // Remove connections and remove node and node dependencies
  let targetId = $connect.targetId
  let sourceId = $connect.sourceId
  let targetarr = rtTargetarrArr(targetId)
  if (targetarr.length) {
    targetarr = _.filter(targetarr, v => v !== sourceId)
    $(`#${targetId}`).attr('data-targetarr', targetarr.toString())
  }
  this.JspInstance.deleteConnection($connect)

  this.selectedElement = {}
}

/**
 * Copy node
 */
JSP.prototype.copyNodes = function ($id) {
  let newNodeInfo = _.cloneDeep(_.find(store.state.dag.tasks, v => v.id === $id))
  let newNodePors = store.state.dag.locations[$id]
  // Unstored nodes do not allow replication
  if (!newNodePors) {
    return
  }
  // Generate random id
  let newUuId = `${uuid() + uuid()}`
  let id = newNodeInfo.id.length > 8 ? newNodeInfo.id.substr(0, 7) : newNodeInfo.id
  let name = newNodeInfo.name.length > 8 ? newNodeInfo.name.substr(0, 7) : newNodeInfo.name

  // new id
  let newId = `${id || ''}-${newUuId}`
  // new name
  let newName = `${name || ''}-${newUuId}`
  // coordinate x
  let newX = newNodePors.x + 100
  // coordinate y
  let newY = newNodePors.y + 40

  // Generate template node
  $('#canvas').append(rtTasksTpl({
    id: newId,
    name: newName,
    x: newX,
    y: newY,
    isAttachment: this.config.isAttachment,
    taskType: newNodeInfo.type
  }))

  // Get the generated node
  let thisDom = jsPlumb.getSelector('.statemachine-demo .w')

  // Copy node information
  newNodeInfo = Object.assign(newNodeInfo, {
    id: newId,
    name: newName
  })

  // Add new node
  store.commit('dag/addTasks', newNodeInfo)
  // Add node location information
  store.commit('dag/setLocations', {
    [newId]: {
      name: newName,
      targetarr: '',
      x: newX,
      y: newY
    }
  })

  // Generating a connection node
  this.JspInstance.batch(() => {
    this.initNode(thisDom[thisDom.length - 1])
    // Add events to nodes
    this.tasksEvent(newId)
  })
}
/**
 * toolbarEvent
 * @param {Screen}
 */
JSP.prototype.handleEventScreen = function ({ item, is }) {
  let screenOpen = true
  if (is) {
    item.icon = '&#xe660;'
    screenOpen = true
  } else {
    item.icon = '&#xe6e0;'
    screenOpen = false
  }
  let $mainLayoutModel = $('.main-layout-model')
  if (screenOpen) {
    $mainLayoutModel.addClass('dag-screen')
  } else {
    $mainLayoutModel.removeClass('dag-screen')
  }
}
/**
 * save task
 * @param tasks
 * @param locations
 * @param connects
 */
JSP.prototype.saveStore = function () {
  return new Promise((resolve, reject) => {
    let connects = []
    let locations = {}
    let tasks = []

    let is = (id) => {
      return !!_.filter(tasksAll(), v => v.id === id).length
    }

    // task
    _.map(_.cloneDeep(store.state.dag.tasks), v => {
      if (is(v.id)) {
        let preTasks = []
        let id = $(`#${v.id}`)
        let tar = id.attr('data-targetarr')
        let idDep = tar ? id.attr('data-targetarr').split(',') : []
        if (idDep.length) {
          _.map(idDep, v1 => {
            preTasks.push($(`#${v1}`).find('.name-p').text())
          })
        }

        let tasksParam = _.assign(v, {
          preTasks: preTasks
        })

        // Sub-workflow has no retries and interval
        if (v.type === 'SUB_PROCESS') {
          tasksParam = _.omit(tasksParam, ['maxRetryTimes', 'retryInterval'])
        }

        tasks.push(tasksParam)
      }
    })

    _.map(this.JspInstance.getConnections(), v => {
      connects.push({
        'endPointSourceId': v.sourceId,
        'endPointTargetId': v.targetId
      })
    })

    _.map(tasksAll(), v => {
      locations[v.id] = {
        name: v.name,
        targetarr: v.targetarr,
        x: v.x,
        y: v.y
      }
    })

    // Storage node
    store.commit('dag/setTasks', tasks)
    // Store coordinate information
    store.commit('dag/setLocations', locations)
    // Storage line dependence
    store.commit('dag/setConnects', connects)

    resolve({
      connects: connects,
      tasks: tasks,
      locations: locations
    })
  })
}
/**
 * Event processing
 */
JSP.prototype.handleEvent = function () {
  this.JspInstance.bind('beforeDrop', function (info) {
    let sourceId = info['sourceId']// 出
    let targetId = info['targetId']// 入

    /**
     * Recursive search for nodes
     */
    let recursiveVal
    const recursiveTargetarr = (arr, targetId) => {
      for (var i in arr) {
        if (arr[i] === targetId) {
          recursiveVal = targetId
        } else {
          let recTargetarrArr = rtTargetarrArr(arr[i])
          if (recTargetarrArr.length) {
            recursiveTargetarr(recTargetarrArr, targetId)
          } else {
            return recursiveTargetarr(targetId)
          }
        }
      }
      return recursiveVal
    }

    // Connection to connected nodes is not allowed
    if (_.findIndex(rtTargetarrArr(targetId), v => v === sourceId) !== -1) {
      return false
    }

    // Recursive form to find if the target Targetarr has a sourceId
    if (recursiveTargetarr(rtTargetarrArr(sourceId), targetId)) {
      // setRecursiveVal(null)
      return false
    }

    // Storage node dependency information
    saveTargetarr(sourceId, targetId)

    // Monitor whether to edit DAG
    store.commit('dag/setIsEditDag', true)

    return true
  })
}
/**
 * Backfill data processing
 */
JSP.prototype.jspBackfill = function ({ connects, locations, largeJson }) {
  // Backfill nodes
  this.jsonHandle({
    largeJson: largeJson,
    locations: locations
  })

  let wNodes = jsPlumb.getSelector('.statemachine-demo .w')

  // Backfill line
  this.JspInstance.batch(() => {
    for (let i = 0; i < wNodes.length; i++) {
      this.initNode(wNodes[i])
    }
    _.map(connects, v => {
      let sourceId = v.endPointSourceId.split('-')
      let targetId = v.endPointTargetId.split('-')
      if (sourceId.length === 4 && targetId.length === 4) {
        sourceId = `${sourceId[0]}-${sourceId[1]}-${sourceId[2]}`
        targetId = `${targetId[0]}-${targetId[1]}-${targetId[2]}`
      } else {
        sourceId = v.endPointSourceId
        targetId = v.endPointTargetId
      }

      this.JspInstance.connect({
        source: sourceId,
        target: targetId,
        type: 'basic',
        paintStyle: { strokeWidth: 2, stroke: '#555' }
      })
    })
  })

  jsPlumb.fire('jsPlumbDemoLoaded', this.JspInstance)

  // Connection monitoring
  this.handleEvent()

  // Drag and drop new nodes
  this.draggable()
}

export default new JSP()
