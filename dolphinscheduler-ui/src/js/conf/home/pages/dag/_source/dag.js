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
let v = new Vue()
import _ from 'lodash'
import i18n from '@/module/i18n'
import { jsPlumb } from 'jsplumb'
import JSP from './plugIn/jsPlumbHandle'
import DownChart from './plugIn/downChart'
import store from '@/conf/home/store'

/**
 * Prototype method
 */
const Dag = function () {
  this.dag = {}
  this.instance = {}
}

/**
 * init
 * @dag dag vue instance
 */
Dag.prototype.init = function ({ dag, instance }) {
  this.dag = dag
  this.instance = instance
}

/**
 * set init config
 */
Dag.prototype.setConfig = function (o) {
  JSP.setConfig(o)
}

/**
 * create dag
 */
Dag.prototype.create = function () {
  const self = this
  let plumbIns = jsPlumb.getInstance()
  plumbIns.reset()
  plumbIns.ready(() => {
    JSP.init({
      dag: this.dag,
      instance: this.instance,
      options: {
        onRemoveNodes ($id) {
          self.dag.removeEventModelById($id)
        }
      }
    })

    // init event
    JSP.handleEvent()

    // init draggable
    JSP.draggable()
  })
}

/**
 * Action event on the right side of the toolbar
 */
Dag.prototype.toolbarEvent = function ({ item, code, is }) {
  let self = this
  switch (code) {
    case 'pointer':
      JSP.handleEventPointer(is)
      break
    case 'line':
      JSP.handleEventLine(is)
      break
    case 'remove':
      JSP.handleEventRemove()
      break
    case 'screen':
      JSP.handleEventScreen({ item, is })
      break
    case 'download':
      v.$modal.dialog({
        width: 350,
        closable: false,
        showMask: true,
        maskClosable: true,
        title: i18n.$t('Download'),
        content: i18n.$t('Please confirm whether the workflow has been saved before downloading'),
        ok: {
          handle (e) {
            DownChart.download({
              dagThis: self.dag
            })
          }
        },
        cancel: {}
      })
      break
  }
}

/**
 * Echo data display
 */
Dag.prototype.backfill = function (arg) {
  if (arg) {
    let locationsValue = store.state.dag.locations
    const locationsValue1 = _.cloneDeep(store.state.dag.locations)
    const locationsValue2 = _.cloneDeep(store.state.dag.locations)
    const arr = []
    for (const i in locationsValue1) {
      const objs = {}
      objs.id = i
      arr.push(Object.assign(objs, locationsValue1[i])) // Attributes
    }
    const tmp = []
    for (const i in locationsValue2) {
      if (locationsValue2[i].targetarr !== '' && locationsValue2[i].targetarr.split(',').length > 1) {
        tmp.push(locationsValue2[i])
      }
    }

    const copy = function (array) {
      const newArray = []
      for (const item of array) {
        newArray.push(item)
      }
      return newArray
    }

    const newArr = copy(arr)
    const getNewArr = function () {
      for (let i = 0; i < newArr.length; i++) {
        if (newArr[i].targetarr !== '' && newArr[i].targetarr.split(',').length > 1) {
          newArr[i].targetarr = newArr[i].targetarr.split(',').shift()
        }
      }
      return newArr
    }
    getNewArr()
    /**
    * @description Transform flat data into a tree structure
    * @param {Array} arr Flat data
    * @param {String} pidStr targetarr key name
    * @param {String} idStr id key name
    * @param {String} childrenStr children key name
    */
    const fommat = function ({ arrayList, pidStr = 'targetarr', idStr = 'id', childrenStr = 'children' }) {
      const listOjb = {} // Used to store objects of the form {key: obj}
      const treeList = [] // An array to store the final tree structure data
      // Transform the data into {key: obj} format, which is convenient for the following data processing
      for (let i = 0; i < arrayList.length; i++) {
        listOjb[arrayList[i][idStr]] = arrayList[i]
      }
      // Format data based on pid
      for (let j = 0; j < arrayList.length; j++) {
        // Determine if the parent exists
        // let haveParent = arrayList[j].targetarr.split(',').length>1?listOjb[arrayList[j].targetarr.split(',')[0]]:listOjb[arrayList[j][pidStr]]
        const haveParent = listOjb[arrayList[j][pidStr]]
        if (haveParent) {
          // If there is no parent children field, create a children field
          !haveParent[childrenStr] && (haveParent[childrenStr] = [])
          // Insert child in parent
          haveParent[childrenStr].push(arrayList[j])
        } else {
          // If there is no parent, insert directly into the outermost layer
          treeList.push(arrayList[j])
        }
      }
      return treeList
    }
    const datas = fommat({ arrayList: newArr, pidStr: 'targetarr' })
    // Count the number of leaf nodes
    const getLeafCountTree = function (json) {
      if (!json.children) {
        json.colspan = 1
        return 1
      } else {
        let leafCount = 0
        for (let i = 0; i < json.children.length; i++) {
          leafCount = leafCount + getLeafCountTree(json.children[i])
        }
        json.colspan = leafCount
        return leafCount
      }
    }
    // Number of tree node levels
    const countTree = getLeafCountTree(datas[0])
    const getMaxFloor = function (treeData) {
      let max = 0
      function each (data, floor) {
        data.forEach(e => {
          e.floor = floor
          e.x = floor * 170
          if (floor > max) {
            max = floor
          }
          if (e.children) {
            each(e.children, floor + 1)
          }
        })
      }
      each(treeData, 1)
      return max
    }
    getMaxFloor(datas)
    // The last child of each node
    let lastchildren = []
    const forxh = function (list) {
      for (let i = 0; i < list.length; i++) {
        const chlist = list[i]
        if (chlist.children) {
          forxh(chlist.children)
        } else {
          lastchildren.push(chlist)
        }
      }
    }
    forxh(datas)
    // Get all parent nodes above the leaf node
    const treeFindPath = function (tree, func, path, n) {
      if (!tree) return []
      for (const data of tree) {
        path.push(data.name)
        if (func(data)) return path
        if (data.children) {
          const findChildren = treeFindPath(data.children, func, path, n)
          if (findChildren.length) return findChildren
        }
        path.pop()
      }
      return []
    }
    const toLine = function (data) {
      return data.reduce((arrData, { id, name, targetarr, x, y, children = [] }) =>
        arrData.concat([{ id, name, targetarr, x, y }], toLine(children)), [])
    }
    const listarr = toLine(datas)
    const listarrs = toLine(datas)
    const dataObject = {}
    for (let i = 0; i < listarrs.length; i++) {
      delete (listarrs[i].id)
    }

    for (let a = 0; a < listarr.length; a++) {
      dataObject[listarr[a].id] = listarrs[a]
    }
    // Comparison function
    const createComparisonFunction = function (propertyName) {
      return function (object1, object2) {
        const value1 = object1[propertyName]
        const value2 = object2[propertyName]

        if (value1 < value2) {
          return -1
        } else if (value1 > value2) {
          return 1
        } else {
          return 0
        }
      }
    }

    lastchildren = lastchildren.sort(createComparisonFunction('x'))

    // Coordinate value of each leaf node
    for (let a = 0; a < lastchildren.length; a++) {
      dataObject[lastchildren[a].id].y = (a + 1) * 120
    }
    for (let i = 0; i < lastchildren.length; i++) {
      const node = treeFindPath(datas, data => data.targetarr === lastchildren[i].targetarr, [], i + 1)
      for (let j = 0; j < node.length; j++) {
        for (let k = 0; k < listarrs.length; k++) {
          if (node[j] === listarrs[k].name) {
            listarrs[k].y = (i + 1) * 120
          }
        }
      }
    }
    for (let i = 0; i < tmp.length; i++) {
      for (const objs in dataObject) {
        if (tmp[i].name === dataObject[objs].name) {
          dataObject[objs].targetarr = tmp[i].targetarr
        }
      }
    }
    for (let a = 0; a < lastchildren.length; a++) {
      dataObject[lastchildren[a].id].y = (a + 1) * 120
    }
    if (countTree > 1) {
      dataObject[Object.keys(locationsValue1)[0]].y = (countTree / 2) * 120 + 50
    }

    locationsValue = dataObject
    const self = this
    let plumbIns = jsPlumb.getInstance()
    plumbIns.reset()
    plumbIns.ready(() => {
      JSP.init({
        dag: this.dag,
        instance: this.instance,
        options: {
          onRemoveNodes ($id) {
            self.dag.removeEventModelById($id)
          }
        }
      })
      // Backfill
      JSP.jspBackfill({
        // connects
        connects: _.cloneDeep(store.state.dag.connects),
        // Node location information
        locations: _.cloneDeep(locationsValue),
        // Node data
        largeJson: _.cloneDeep(store.state.dag.tasks)
      })
    })
  } else {
    const self = this
    let plumbIns = jsPlumb.getInstance()
    plumbIns.reset()
    plumbIns.ready(() => {
      JSP.init({
        dag: this.dag,
        instance: this.instance,
        options: {
          onRemoveNodes ($id) {
            self.dag.removeEventModelById($id)
          }
        }
      })
      // Backfill
      JSP.jspBackfill({
        // connects
        connects: _.cloneDeep(store.state.dag.connects),
        // Node location information
        locations: _.cloneDeep(store.state.dag.locations),
        // Node data
        largeJson: _.cloneDeep(store.state.dag.tasks)
      })
    })
  }
}

/**
 * Get dag storage format data
 */
Dag.prototype.saveStore = function () {
  return JSP.saveStore()
}

export default new Dag()
