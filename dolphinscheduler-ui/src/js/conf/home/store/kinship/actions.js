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

export default {
  /**
   * Get workFlow DAG
   */
  getWorkFlowList ({ state }, payload) {
    const projectId = localStore.getItem('projectId')
    return new Promise((resolve, reject) => {
      const url = `lineages/${projectId}/list-name`
      io.get(url, {
        searchVal: payload
      }, res => {
        const workList = []
        if (res.data) {
          _.map(res.data, (item) => {
            workList.push({
              id: `${item.workFlowId}`,
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
  getWorkFlowDAG ({ state }, payload) {
    const projectId = localStore.getItem('projectId')
    return new Promise((resolve, reject) => {
      const url = `lineages/${projectId}/list-ids`
      io.get(url, {
        ids: payload
      }, res => {
        let locations = []
        let connects = []
        if (res.data.workFlowList) {
          locations = _.uniqBy(res.data.workFlowList, 'workFlowId').map((item) => ({
            id: `${item.workFlowId}`,
            name: item.workFlowName,
            workFlowPublishStatus: item.workFlowPublishStatus,
            scheduleStartTime: item.scheduleStartTime,
            scheduleEndTime: item.scheduleEndTime,
            crontab: item.crontab,
            schedulePublishStatus: item.schedulePublishStatus
          }))
        }
        if (res.data.workFlowRelationList) {
          connects = _.map(res.data.workFlowRelationList, (item) => ({
            source: `${item.sourceWorkFlowId}`, // should be string, or connects will not show by echarts
            target: `${item.targetWorkFlowId}` // should be string, or connects will not show by echarts
          }))
        }
        state.sourceWorkFlowId = payload || ''
        // locations
        state.locations = locations /* JSON.parse(locations) */
        // connects
        state.connects = connects /* JSON.parse(connects) */
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  }
}
