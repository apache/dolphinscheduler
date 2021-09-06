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
import { tasksState } from '@/conf/home/pages/dag/_source/config'

export default {
  /**
   *  Task status acquisition
   */
  getTaskState ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/instance/task-list-by-process-id`, {
        processInstanceId: payload
      }, res => {
        const arr = _.map(res.data.taskList, v => {
          return _.cloneDeep(_.assign(tasksState[v.state], {
            name: v.name,
            stateId: v.id,
            dependentResult: v.dependentResult
          }))
        })
        resolve({
          list: arr,
          processInstanceState: res.data.processInstanceState,
          taskList: res.data.taskList
        })
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
      io.post(`projects/${state.projectCode}/process/release`, {
        code: payload.code,
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
      io.get(`projects/${state.projectCode}/process/versions`, payload, res => {
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
      io.get(`projects/${state.projectCode}/process/version/switch`, payload, res => {
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
      io.get(`projects/${state.projectCode}/process/version/delete`, payload, res => {
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
      io.get(`projects/${state.projectCode}/process/verify-name`, {
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
      io.get(`projects/${state.projectCode}/process/select-by-code`, {
        code: payload
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
        state.tenantCode = res.data.processDefinition.tenantCode
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
      io.post(`projects/${state.projectCode}/process/copy`, {
        processDefinitionIds: payload.processDefinitionIds,
        targetProjectId: payload.targetProjectId
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
      io.post(`projects/${state.project}/process/move`, {
        processDefinitionIds: payload.processDefinitionIds,
        targetProjectId: payload.targetProjectId
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
      io.get('projects/created-and-authorized-project', {}, res => {
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
      io.get(`projects/${state.projectCode}/instance/select-by-id`, {
        processInstanceId: payload
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
        state.tenantCode = res.data.tenantCode
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
      io.post(`projects/${state.projectCode}/process/save`, {
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
      io.post(`projects/${state.projectCode}/process/update`, {
        locations: JSON.stringify(state.locations),
        name: _.trim(state.name),
        taskDefinitionJson: JSON.stringify(state.tasks),
        taskRelationJson: JSON.stringify(state.connects),
        tenantCode: state.tenantCode,
        description: _.trim(state.description),
        globalParams: JSON.stringify(state.globalParams),
        timeout: state.timeout,
        releaseState: state.releaseState,
        code: payload
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
      io.post(`projects/${state.projectCode}/instance/update`, {
        processInstanceJson: JSON.stringify(data),
        locations: JSON.stringify(state.locations),
        connects: JSON.stringify(state.connects),
        processInstanceId: payload,
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
      io.get(`projects/${state.projectCode}/process/list`, payload, res => {
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
      io.get(`projects/${state.projectCode}/process/list-paging`, payload, res => {
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
      io.get('projects/query-project-list', payload, res => {
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
  getProcessByProjectId ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process/queryProcessDefinitionAllByProjectId`, payload, res => {
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
      io.get('resources/list/jar', {
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
      io.get(`projects/${state.projectCode}/instance/list-paging`, payload, res => {
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
      io.get(`projects/${state.projectCode}/instance/select-sub-process`, payload, res => {
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
      io.post(`projects/${state.projectCode}/schedule/create`, payload, res => {
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
      io.post(`projects/${state.projectCode}/schedule/preview`, payload, res => {
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
      io.get(`projects/${state.projectCode}/schedule/list-paging`, payload, res => {
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
      io.post(`projects/${state.projectCode}/schedule/offline`, payload, res => {
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
      io.post(`projects/${state.projectCode}/schedule/online`, payload, res => {
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
      io.post(`projects/${state.projectCode}/schedule/update`, payload, res => {
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
      io.get(`projects/${state.projectCode}/instance/delete`, payload, res => {
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
      io.get(`projects/${state.projectCode}/instance/batch-delete`, payload, res => {
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
      io.get(`projects/${state.projectCode}/process/delete`, payload, res => {
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
      io.get(`projects/${state.projectCode}/process/batch-delete`, payload, res => {
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

    io.get(`projects/${state.projectCode}/process/export`, { processDefinitionIds: payload.processDefinitionIds }, res => {
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
      io.get(`projects/${state.projectCode}/instance/view-variables`, payload, res => {
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
      io.get(`projects/${state.projectCode}/task-instance/list-paging`, payload, res => {
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
      io.post(`projects/${state.projectCode}/task-instance/force-success`, payload, res => {
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
      io.get(`projects/${state.projectCode}/process/view-tree`, payload, res => {
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
      io.get(`projects/${state.projectCode}/instance/view-gantt`, payload, res => {
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
      io.get(`projects/${state.projectCode}/process/gen-task-list`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getTaskListDefIdAll ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/process/get-task-list`, payload, res => {
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
      io.get(`projects/${state.projectCode}/schedule/delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getResourceId ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources/queryResource', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  genTaskCodeList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectCode}/task/gen-task-code-list`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
