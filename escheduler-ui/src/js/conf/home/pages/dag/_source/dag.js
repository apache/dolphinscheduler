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
import { jsPlumb } from 'jsplumb'
import JSP from './plugIn/jsPlumbHandle'
import DownChart from './plugIn/downChart'
import store from '@/conf/home/store'

/**
 * Prototype method
 */
let Dag = function () {
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
  jsPlumb.ready(() => {
    JSP.init({
      dag: this.dag,
      instance: this.instance
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
      DownChart.download({
        dagThis: this.dag
      })
      break
  }
}

/**
 * Echo data display
 */
Dag.prototype.backfill = function () {
  jsPlumb.ready(() => {
    JSP.init({
      dag: this.dag,
      instance: this.instance
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

/**
 * Get dag storage format data
 */
Dag.prototype.saveStore = function () {
  return JSP.saveStore()
}

export default new Dag()
