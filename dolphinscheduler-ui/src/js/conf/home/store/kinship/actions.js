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
import io from '@/module/io'
import localStore from '@/module/util/localStorage'

/**
 * build locations by workFlowList
 */
const buildLocations = (workFlowList) => {
  return _.uniqBy(workFlowList, 'workFlowCode').map((item) => ({
    code: `${item.workFlowCode}`,
    name: item.workFlowName,
    workFlowPublishStatus: item.workFlowPublishStatus,
    scheduleStartTime: item.scheduleStartTime,
    scheduleEndTime: item.scheduleEndTime,
    crontab: item.crontab,
    schedulePublishStatus: item.schedulePublishStatus
  }))
}

/**
 * build connects by workFlowRelationList
 */
const buildConnects = (workFlowRelationList) => {
  return _.map(workFlowRelationList, (item) => ({
    source: `${item.sourceWorkFlowCode}`, // should be string, or connects will not show by echarts
    target: `${item.targetWorkFlowCode}` // should be string, or connects will not show by echarts
  }))
}

export default {
  /**
   * Get workFlow DAG
   */
  getWorkFlowList ({ state }, payload) {
    const projectCode = localStore.getItem('projectCode')
    return new Promise((resolve, reject) => {
      const url = `projects/${projectCode}/lineages/query-by-name`
      io.get(url, {
        searchVal: payload
      }, res => {
        const workList = []
        if (res.data) {
          _.map(res.data, (item) => {
            workList.push({
              code: `${item.workFlowCode}`,
              name: item.workFlowName
            })
          })
        }
        state.workList = workList /* JSON.parse(connects) */
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get workFlow DAG
   */
  getWorkFlowDAG ({ state }, code) {
    const projectCode = localStore.getItem('projectCode')
    return new Promise((resolve, reject) => {
      const url = `projects/${projectCode}/lineages/${code}`
      io.get(url, res => {
        let locations = []
        let connects = []
        if (res.data.workFlowList) {
          locations = buildLocations(res.data.workFlowList)
        }
        if (res.data.workFlowRelationList) {
          connects = buildConnects(res.data.workFlowRelationList)
        }
        state.sourceWorkFlowCode = code || ''
        // locations
        state.locations = locations /* JSON.parse(locations) */
        // connects
        state.connects = connects /* JSON.parse(connects) */
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get all workFlow DAG
   */
  getWorkFlowDAGAll ({ state }, payload) {
    const projectCode = localStore.getItem('projectCode')
    return new Promise((resolve, reject) => {
      const url = `projects/${projectCode}/lineages/list`
      io.get(url, res => {
        let locations = []
        let connects = []
        if (res.data.workFlowList) {
          locations = buildLocations(res.data.workFlowList)
        }
        if (res.data.workFlowRelationList) {
          connects = buildConnects(res.data.workFlowRelationList)
        }
        state.sourceWorkFlowCode = ''
        state.locations = locations
        state.connects = connects
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  }
}
