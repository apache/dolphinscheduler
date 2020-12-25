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
   * Get item list
   */
  getProjectsList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Create project
   */
  createProjects ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('projects/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Delete Project
   */
  deleteProjects ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/delete', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * edit Project
   */
  updateProjects ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('projects/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Task status statistics
   */
  getTaskCtatusCount ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/analysis/task-state-count', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get command state count
   */
  getCommandStateCount ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/analysis/command-state-count', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get command state count
   */
  getQueueCount ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/analysis/queue-count', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Process status statistics
   */
  getProcessStateCount ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/analysis/process-state-count', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Process definition statistics
   */
  getDefineUserCount ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/analysis/define-user-count', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
