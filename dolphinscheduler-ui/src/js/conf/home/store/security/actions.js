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
   * verify Name
   * @param alertgroup/verifyGroupName
   * @param users/verifyUserName
   * @param tenant/verifyTenantCode
   */
  verifyName ({ state }, payload) {
    const o = {
      user: {
        param: {
          userName: payload.userName
        },
        api: 'users/verify-user-name'
      },
      tenant: {
        param: {
          tenantCode: payload.tenantCode
        },
        api: 'tenant/verify-tenant-code'
      },
      alertgroup: {
        param: {
          groupName: payload.groupName
        },
        api: 'alert-group/verify-group-name'
      }
    }

    return new Promise((resolve, reject) => {
      io.get(o[payload.type].api, o[payload.type].param, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Create user
   * @param "userName":string
   * @param "userPassword": string
   * @param "tenantId":int
   * @param "email":string
   * @param "phone":string
   */
  createUser ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('users/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Verify that the username exists
   * @param userName
   */
  verifyUserName ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('users/verify-user-name', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Paging query user list
   * @param "pageNo":int,
   * @param "searchVal":string,
   * @param "pageSize":int
   */
  getUsersListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('users/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * user list expect admin
   */
  getUsersList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('users/list', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * user all  list
   */
  getUsersAll ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('users/list-all', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Update user
   * @param "id":int,
   * @param "userName":string,
   * @param "userPassword": string,
   * @param "tenantId":int,
   * @param "email":string,
   * @param "phone":string
   */
  updateUser ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('users/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * delete users
   * @param "id":int
   */
  deleteUser ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('users/delete', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Obtain authorized and unauthorized items
   */
  getAuthList ({ state }, payload) {
    const o = {
      type: payload.type,
      category: payload.category
    }

    const param = {}
    // Manage user
    if (o.type === 'user') {
      param.alertgroupId = payload.id
    } else {
      param.userId = payload.id
    }

    // Authorized project
    const p1 = new Promise((resolve, reject) => {
      io.get(`${o.category}/unauth-${o.type}`, param, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
    // Unauthorized project
    const p2 = new Promise((resolve, reject) => {
      io.get(`${o.category}/authed-${o.type}`, param, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
    return new Promise((resolve, reject) => {
      Promise.all([p1, p2]).then(a => {
        resolve(a)
      }).catch(e => {
        reject(e)
      })
    })
  },

  getResourceList ({ state }, payload) {
    const o = {
      type: payload.type,
      category: payload.category
    }

    const param = {}
    // Manage user
    if (o.type === 'user') {
      param.alertgroupId = payload.id
    } else {
      param.userId = payload.id
    }

    // Authorized project
    const p1 = new Promise((resolve, reject) => {
      io.get(`${o.category}/authorize-resource-tree`, param, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
    // Unauthorized project
    const p2 = new Promise((resolve, reject) => {
      io.get(`${o.category}/authed-${o.type}`, param, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
    return new Promise((resolve, reject) => {
      Promise.all([p1, p2]).then(a => {
        resolve(a)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Authorization [project, resource, data source]
   * @param Project,Resources,Datasource
   */
  grantAuthorization ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(payload.api, payload.param, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Query user details
   * @param "userId":int
   */
  getUsersDetails ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('users/select-by-id', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Tenant list - pagination
   */
  getTenantListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('tenant/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Tenant list - no paging
   */
  getTenantList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('tenant/list', payload, res => {
        const list = res.data
        list.unshift({
          id: -1,
          tenantName: 'default'
        })
        state.tenantAllList = list
        resolve(list)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Queue interface
   */
  getQueueList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('queue/list', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Create Queue
   */
  createQueue ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('tenant/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * update Queue
   */
  updateQueue ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('tenant/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * delete Queue
   */
  deleteQueue ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('tenant/delete', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Paging query alarm group list
   */
  getAlertgroupP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('alert-group/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Alarm group list
   */
  getAlertgroup ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('alert-group/list', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Create an alarm group.
   */
  createAlertgrou ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('alert-group/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * update an alarm group.
   */
  updateAlertgrou ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('alert-group/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * delete alarm group.
   */
  deleteAlertgrou ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('alert-group/delete', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Master list
   */
  getProcessMasterList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('process/master/list', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Worker list
   */
  getProcessWorkerList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('process/worker/list', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get queue list pages
   */
  getQueueListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('queue/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * create queue
   */
  createQueueQ ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('queue/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * update queue
   */
  updateQueueQ ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('queue/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * update queue
   */
  verifyQueueQ ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('queue/verify-queue', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get worker groups
   */
  getWorkerGroups ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('worker-group/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get worker groups all
   */
  getWorkerGroupsAll ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('worker-group/all-groups', payload, res => {
        let list = res.data
        if (list.length > 0) {
          list = list.map(item => {
            return {
              id: item,
              name: item
            }
          })
        } else {
          list.unshift({
            id: 'default',
            name: 'default'
          })
        }
        state.workerGroupsListAll = list
        resolve(list)
      }).catch(e => {
        reject(e)
      })
    })
  },
  saveWorkerGroups ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('worker-group/save', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  deleteWorkerGroups ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('worker-group/delete-by-id', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
