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

export default {
  setProjectId (state, payload) {
    state.projectId = payload
  },
  setProjectCode (state, payload) {
    state.projectCode = payload
  },
  setProjectName (state, payload) {
    state.projectName = payload
  },
  /**
   * set tasks
   * */
  setTasks (state, payload) {
    state.tasks = payload
  },
  /**
   * set locations
   * */
  setLocations (state, payload) {
    state.locations = payload
  },
  /**
   * add locations
   * */
  addLocations (state, payload) {
    state.locations = Object.assign(state.locations, {}, payload)
  },
  /**
   * set connects
   * */
  setConnects (state, payload) {
    state.connects = payload
  },
  /**
   * set dag name
   */
  setName (state, payload) {
    state.name = payload
  },
  /**
   * set timeout
   */
  setTimeout (state, payload) {
    state.timeout = payload
  },
  /**
   * set executionType
   */
  setExecutionType (state, payload) {
    state.executionType = payload
  },
  /**
   * set tenantCode
   */
  setTenantCode (state, payload) {
    state.tenantCode = payload
  },
  /**
   * set global params
   */
  setGlobalParams (state, payload) {
    state.globalParams = payload
  },
  /**
   * set description
   */
  setDesc (state, payload) {
    state.description = payload
  },
  setReleaseState (state, payload) {
    state.releaseState = payload
  },
  /**
   * Whether to update the process definition
   */
  setSyncDefine (state, payload) {
    state.syncDefine = payload
  },
  /**
   * Whether to edit the parameters
   */
  setIsEditDag (state, payload) {
    state.isEditDag = payload
  },

  /**
   * edit state
   */
  setIsDetails (state, payload) {
    state.isDetails = payload
  },

  /**
   * set depend result
   */
  setDependResult (state, payload) {
    state.dependResult = Object.assign(state.dependResult, {}, payload)
  },

  /**
   * reset params
   */
  resetParams (state, payload) {
    state.globalParams = (payload && payload.globalParams) || []
    state.tasks = (payload && payload.tasks) || []
    state.name = (payload && payload.name) || ''
    state.description = (payload && payload.description) || ''
    state.timeout = (payload && payload.timeout) || 0
    state.executionType = (payload && payload.executionType) || 'PARALLEL'
    state.tenantCode = (payload && payload.tenantCode) || 'default'
    state.processListS = (payload && payload.processListS) || []
    state.resourcesListS = (payload && payload.resourcesListS) || []
    state.resourcesListJar = (payload && payload.resourcesListJar) || []
    state.projectListS = (payload && payload.projectListS) || []
    state.isDetails = (payload && payload.isDetails) || false
    state.runFlag = (payload && payload.runFlag) || ''
    state.locations = (payload && payload.locations) || {}
    state.connects = (payload && payload.connects) || []
    state.dependResult = (payload && payload.dependResult) || {}
  },
  /**
   * add task
   * @param {Task} task
   */
  addTask (state, task) {
    state.isEditDag = true
    const i = _.findIndex(state.tasks, v => v.code === task.code)
    if (i !== -1) {
      state.tasks[i] = Object.assign(state.tasks[i], {}, task)
    } else {
      state.tasks.push(task)
    }
  },
  /**
   * remove task
   * @param {object} state
   * @param {string} code
   */
  removeTask (state, code) {
    state.isEditDag = true
    state.tasks = state.tasks.filter(task => task.code !== code)
  },
  resetLocalParam (state, payload) {
    const tasks = state.tasks
    tasks.forEach((task, index) => {
      payload.forEach(p => {
        if (p.id === task.id) {
          tasks[index].params.localParams = p.localParam
        }
      })
    })
    state.tasks = tasks
  }
}
