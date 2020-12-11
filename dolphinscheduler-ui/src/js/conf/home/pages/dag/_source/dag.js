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
import _ from 'lodash'
import i18n from '@/module/i18n'
import { jsPlumb } from 'jsplumb'
import JSP from './plugIn/jsPlumbHandle'
import DownChart from './plugIn/downChart'
import store from '@/conf/home/store'
import dagre from 'dagre'

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
  const plumbIns = jsPlumb.getInstance()
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
  const self = this
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
      Vue.prototype.$confirm(`${i18n.$t('Please confirm whether the workflow has been saved before downloading')}`, `${i18n.$t('Download')}`, {
        confirmButtonText: `${i18n.$t('Confirm')}`,
        cancelButtonText: `${i18n.$t('Cancel')}`,
        type: 'warning'
      }).then(() => {
        DownChart.download({
          dagThis: self.dag
        })
      }).catch(() => {
      })
      break
  }
}

/**
 * Echo data display
 */
Dag.prototype.backfill = function (arg) {
  if (arg) {
    const marginX = 100
    const g = new dagre.graphlib.Graph()
    g.setGraph({})
    g.setDefaultEdgeLabel(function () { return {} })

    for (const i in store.state.dag.locations) {
      const location = store.state.dag.locations[i]
      g.setNode(i, { label: i, width: Math.min(location.name.length * 7, 170), height: 150 })
    }

    for (const i in store.state.dag.connects) {
      const connect = store.state.dag.connects[i]
      g.setEdge(connect.endPointSourceId, connect.endPointTargetId)
    }
    dagre.layout(g)

    const dataObject = {}
    g.nodes().forEach(function (v) {
      const node = g.node(v)
      const location = store.state.dag.locations[node.label]
      const obj = {}
      obj.name = location.name
      obj.x = node.x + marginX
      obj.y = node.y
      obj.targetarr = location.targetarr
      dataObject[node.label] = obj
    })
    jsPlumb.ready(() => {
      JSP.init({
        dag: this.dag,
        instance: this.instance,
        options: {
          onRemoveNodes ($id) {
            this.dag.removeEventModelById($id)
          }
        }
      })
      // Backfill
      JSP.jspBackfill({
        // connects
        connects: _.cloneDeep(store.state.dag.connects),
        // Node location information
        locations: _.cloneDeep(dataObject),
        // Node data
        largeJson: _.cloneDeep(store.state.dag.tasks)
      })
    })
  } else {
    const plumbIns = jsPlumb.getInstance()
    plumbIns.reset()
    plumbIns.ready(() => {
      JSP.init({
        dag: this.dag,
        instance: this.instance,
        options: {
          onRemoveNodes ($id) {
            this.dag.removeEventModelById($id)
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
