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

import io from '@/module/io'

export default {
  /**
   * Get a list of udf files
   */
  getResourcesListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources', payload, res => {
        resolve(res.data)
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
  getResourcesList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources/list', payload, res => {
        resolve(res)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Delete resource
   */
  deleteResource ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`resources/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Verify that the upload name exists
   */
  resourceVerifyName ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources/verify-name', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Resource file online view
   */
  getViewResources ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`resources/${payload.id}/view`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * UDF function creation
   */
  createUdfFunc ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`resources/${payload.resourceId}/udf-func`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * UDF function editing
   */
  updateUdfFunc ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.put(`resources/${payload.resourceId}/udf-func/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Verify that the UDF function name exists
   */
  verifyUdfFuncName ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources/udf-func/verify-name', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Delete udf function
   * @param id int
   */
  deleteUdf ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`resources/udf-func/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query UDF function list - paging
   */
  getUdfFuncListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources/udf-func', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Update document
   */
  updateContent ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.put(`resources/${payload.id}/update-content`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Resource online creation
   */
  createResourceFile ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('resources/online-create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Resource online create folder
   */
  createResourceFolder ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('resources/directory', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Resource rename
   */
  resourceRename ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.put(`resources/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get task group list pages
   */
  getTaskGroupListPaging ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('task-group/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get task group list pages
   */
  getTaskGroupListPagingByProjectCode ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('task-group/query-list-by-projectCode', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * create task group
   */
  createTaskGroup ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('task-group/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * update task group
   */
  updateTaskGroup ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('task-group/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * close task group
   */
  closeTaskGroup ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('task-group/close-task-group', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * start task group
   */
  startTaskGroup ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('task-group/start-task-group', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * query the details of task group through group id
   */
  getTaskListInTaskGroupQueueById ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('task-group/query-list-by-group-id', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * modify the priority of the task in the queue
   */
  modifyPriority ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('task-group/modifyPriority', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * force to start the task in the queue
   */
  forceStartTaskInQueue ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('task-group/forceStart', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  }

}
