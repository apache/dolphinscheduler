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

export default {
  /**
   *  Task status acquisition
   */
  getTaskState ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-instances/${payload}/tasks`, {
        processInstanceId: payload
      }, res => {
        state.taskInstances = res.data.taskList
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Update process definition status
   */
  editProcessState ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/process-definition/${payload.code}/release`, {
        name: payload.name,
        releaseState: payload.releaseState
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * get process definition versions pagination info
   */
  getProcessDefinitionVersionsPage ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition/${payload.code}/versions`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * switch process definition version
   */
  switchProcessDefinitionVersion ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition/${payload.code}/versions/${payload.version}`, {}, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * delete process definition version
   */
  deleteProcessDefinitionVersion ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`projects/${state.projectCode}/process-definition/${payload.code}/versiond/${payload.version}`, {}, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Update process instance status
   */
  editExecutorsState ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/executors/execute`, {
        processInstanceId: payload.processInstanceId,
        executeType: payload.executeType
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Verify that the DGA map name exists
   */
  verifDAGName ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition/verify-name`, {
        name: payload
      }, res => {
        state.name = payload
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Get process definition DAG diagram details
   */
  getProcessDetails ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition/${payload}`, {
      }, res => {
        // process definition code
        state.code = res.data.processDefinition.code
        // version
        state.version = res.data.processDefinition.version
        // name
        state.name = res.data.processDefinition.name
        // description
        state.description = res.data.processDefinition.description
        // taskRelationJson
        state.connects = res.data.processTaskRelationList
        // locations
        state.locations = JSON.parse(res.data.processDefinition.locations)
        // global params
        state.globalParams = res.data.processDefinition.globalParamList
        // timeout
        state.timeout = res.data.processDefinition.timeout
        // tenantId
        state.tenantCode = res.data.processDefinition.tenantCode || 'default'
        // tasks info
        state.tasks = res.data.taskDefinitionList.map(task => _.pick(task, [
          'code',
          'name',
          'version',
          'description',
          'delayTime',
          'taskType',
          'taskParams',
          'flag',
          'taskPriority',
          'workerGroup',
          'failRetryTimes',
          'failRetryInterval',
          'timeoutFlag',
          'timeoutNotifyStrategy',
          'timeout'
        ]))
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },

  /**
   * Get process definition DAG diagram details
   */
  copyProcess ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/process-definition/batch-copy`, {
        codes: payload.codes,
        targetProjectCode: payload.targetProjectCode
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Get process definition DAG diagram details
   */
  moveProcess ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/process-definition/batch-move`, {
        codes: payload.codes,
        targetProjectCode: payload.targetProjectCode
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Get all the items created by the logged in user
   */
  getAllItems ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/created-and-authed', {}, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Get the process instance DAG diagram details
   */
  getInstancedetail ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-instances/${payload}`, {
      }, res => {
        const { processDefinition, processTaskRelationList, taskDefinitionList } = res.data.dagData
        // code
        state.code = processDefinition.code
        // version
        state.version = processDefinition.version
        // name
        state.name = res.data.name
        // desc
        state.description = processDefinition.description
        // connects
        state.connects = processTaskRelationList
        // locations
        state.locations = JSON.parse(processDefinition.locations)
        // global params
        state.globalParams = processDefinition.globalParamList
        // timeout
        state.timeout = processDefinition.timeout
        // tenantCode
        state.tenantCode = res.data.tenantCode || 'default'
        // tasks info
        state.tasks = taskDefinitionList.map(task => _.pick(task, [
          'code',
          'name',
          'version',
          'description',
          'delayTime',
          'taskType',
          'taskParams',
          'flag',
          'taskPriority',
          'workerGroup',
          'failRetryTimes',
          'failRetryInterval',
          'timeoutFlag',
          'timeoutNotifyStrategy',
          'timeout'
        ]))
        // startup parameters
        state.startup = _.assign(state.startup, _.pick(res.data, ['commandType', 'failureStrategy', 'processInstancePriority', 'workerGroup', 'warningType', 'warningGroupId', 'receivers', 'receiversCc']))
        state.startup.commandParam = JSON.parse(res.data.commandParam)

        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Create process definition
   */
  saveDAGchart ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/process-definition`, {
        locations: JSON.stringify(state.locations),
        name: _.trim(state.name),
        taskDefinitionJson: JSON.stringify(state.tasks),
        taskRelationJson: JSON.stringify(state.connects),
        tenantCode: state.tenantCode,
        description: _.trim(state.description),
        globalParams: JSON.stringify(state.globalParams),
        timeout: state.timeout
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Process definition update
   */
  updateDefinition ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.put(`projects/${state.projectCode}/process-definition/${payload}`, {
        locations: JSON.stringify(state.locations),
        name: _.trim(state.name),
        taskDefinitionJson: JSON.stringify(state.tasks),
        taskRelationJson: JSON.stringify(state.connects),
        tenantCode: state.tenantCode,
        description: _.trim(state.description),
        globalParams: JSON.stringify(state.globalParams),
        timeout: state.timeout,
        releaseState: state.releaseState
      }, res => {
        resolve(res)
        state.isEditDag = false
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Process instance update
   */
  updateInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      const data = {
        globalParams: state.globalParams,
        tasks: state.tasks,
        tenantId: state.tenantId,
        timeout: state.timeout
      }
      io.put(`projects/${state.projectCode}/process-instances/${payload}`, {
        processInstanceJson: JSON.stringify(data),
        locations: JSON.stringify(state.locations),
        connects: JSON.stringify(state.connects),
        syncDefine: state.syncDefine
      }, res => {
        resolve(res)
        state.isEditDag = false
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Get a list of process definitions (sub-workflow usage is not paged)
   */
  getProcessList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      if (state.processListS.length) {
        resolve()
        return
      }
      io.get(`projects/${state.projectCode}/process-definition/list`, payload, res => {
        state.processListS = res.data
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get a list of process definitions (list page usage with pagination)
   */
  getProcessListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition`, payload, res => {
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get a list of project
   */
  getProjectList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      if (state.projectListS.length) {
        resolve()
        return
      }
      io.get('projects/list', payload, res => {
        state.projectListS = res.data
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get a list of process definitions by project id
   */
  getProcessByProjectCode ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition/all`, payload, res => {
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * get datasource
   */
  getDatasourceList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('datasources/list', {
        type: payload
      }, res => {
        resolve(res)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * get resources
   */
  getResourcesList ({ state }) {
    return new Promise((resolve, reject) => {
      if (state.resourcesListS.length) {
        resolve()
        return
      }
      io.get('resources/list', {
        type: 'FILE'
      }, res => {
        state.resourcesListS = res.data
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * get jar
   */
  getResourcesListJar ({ state }) {
    return new Promise((resolve, reject) => {
      if (state.resourcesListJar.length) {
        resolve()
        return
      }
      io.get('resources/query-by-type', {
        type: 'FILE'
      }, res => {
        state.resourcesListJar = res.data
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get process instance
   */
  getProcessInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-instances`, payload, res => {
        state.instanceListS = res.data.totalList
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get alarm list
   */
  getNotifyGroupList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('alert-groups/list', res => {
        state.notifyGroupListS = _.map(res.data, v => {
          return {
            id: v.id,
            code: v.groupName,
            disabled: false
          }
        })
        resolve(_.cloneDeep(state.notifyGroupListS))
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Process definition startup interface
   */
  processStart ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/executors/start-process-instance`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * View log
   */
  getLog ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('log/detail', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Get the process instance id according to the process definition id
   * @param taskId
   */
  getSubProcessId ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-instances/query-sub-by-parent`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Called before the process definition starts
   */
  getStartCheck ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/executors/start-check`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Create timing
   */
  createSchedule ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/schedules`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Preview timing
   */
  previewSchedule ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/schedules/preview`, payload, res => {
        resolve(res.data)
        // alert(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Timing list paging
   */
  getScheduleList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/schedules`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Timing online
   */
  scheduleOffline ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/schedules/${payload.id}/offline`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Timed offline
   */
  scheduleOnline ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/schedules/${payload.id}/online`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Edit timing
   */
  updateSchedule ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.put(`projects/${state.projectCode}/schedules/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Delete process instance
   */
  deleteInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`projects/${state.projectCode}/process-instances/${payload.code}`, {}, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Batch delete process instance
   */
  batchDeleteInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/process-instances/batch-delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Delete definition
   */
  deleteDefinition ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`projects/${state.projectCode}/process-definition/${payload.code}`, {}, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Batch delete definition
   */
  batchDeleteDefinition ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/process-definition/batch-delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * export definition
   */
  exportDefinition ({ state }, payload) {
    const downloadBlob = (data, fileNameS = 'json') => {
      if (!data) {
        return
      }
      const blob = new Blob([data])
      const fileName = `${fileNameS}.json`
      if ('download' in document.createElement('a')) { // 不是IE浏览器
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link) // 下载完成移除元素
        window.URL.revokeObjectURL(url) // 释放掉blob对象
      } else { // IE 10+
        window.navigator.msSaveBlob(blob, fileName)
      }
    }

    io.post(`projects/${state.projectCode}/process-definition/batch-export`, { codes: payload.codes }, res => {
      downloadBlob(res, payload.fileName)
    }, e => {

    }, {
      responseType: 'blob'
    })
  },

  /**
   * Process instance get variable
   */
  getViewvariables ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-instances/${payload.code}/view-variables`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Get udfs function based on data source
   */
  getUdfList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources/udf-func/list', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query task instance list
   */
  getTaskInstanceList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/task-instances`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Force fail/kill/need_fault_tolerance task success
   */
  forceTaskSuccess ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectCode}/task-instances/${payload.code}/force-success`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query task record list
   */
  getTaskRecordList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/task-record/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query history task record list
   */
  getHistoryTaskRecordList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/task-record/history-list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * tree chart
   */
  getViewTree ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition/${payload.code}/view-tree`, { limit: payload.limit }, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * gantt chart
   */
  getViewGantt ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-instances/${payload.processInstanceId}/view-gantt`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query task node list
   */
  getProcessTasksList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition/${payload.code}/tasks`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getTaskListDefIdAll ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process-definition/batch-query-tasks`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * remove timing
   */
  deleteTiming ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`projects/${state.projectCode}/schedules/${payload.scheduleId}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getResourceId ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`resources/${payload.id}`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  genTaskCodeList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/task-definition/gen-task-codes`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getTaskDefinitions ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/task-definition`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
