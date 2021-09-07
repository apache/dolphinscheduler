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
        api: 'tenants/verify-code'
      },
      alertgroup: {
        param: {
          groupName: payload.groupName
        },
        api: 'alert-groups/verify-name'
      },
      alarmInstance: {
        param: {
          alertInstanceName: payload.instanceName
        },
        api: 'alert-plugin-instances/verify-name'
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
      io.get('tenants', payload, res => {
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
      io.get('tenants/list', payload, res => {
        const list = res.data
        list.unshift({
          id: -1,
          tenantCode: 'default'
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
      io.get('queues/list', payload, res => {
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
      io.post('tenants', payload, res => {
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
      io.put(`tenants/${payload.id}`, payload, res => {
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
      io.delete(`tenants/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * queryAlertGroupListPaging
   */
  queryAlertGroupListPaging ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('alert-groups', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * queryAlertPluginInstanceListPaging
   */
  queryAlertPluginInstanceListPaging ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('alert-plugin-instances', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * queryUiPlugins
   */
  getPlugins ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('ui-plugins/query-by-type', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * queryUiPluginById
   */
  getUiPluginById ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`ui-plugins/${payload.pluginId}`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * queryAll alert-plugin-instance
   */
  queryAllAlertPluginInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('alert-plugin-instance/list', payload, res => {
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
      io.get('alert-groups/list', payload, res => {
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
      io.post('alert-groups', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * create alert plugin instance operation
   */
  createAlertPluginInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('alert-plugin-instances', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * update alert plugin instance operation
   */
  updateAlertPluginInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.put(`alert-plugin-instances/${payload.alertPluginInstanceId}`, payload, res => {
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
      io.put(`alert-groups/${payload.id}`, payload, res => {
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
      io.delete(`alert-groups/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * delete alert plugin instance operation
   */
  deletAelertPluginInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`alert-plugin-instance/${payload.id}`, payload, res => {
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
      io.get('queues', payload, res => {
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
      io.post('queues', payload, res => {
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
      io.put(`queues/${payload.id}`, payload, res => {
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
      io.post('queues/verify', payload, res => {
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
      io.get('worker-groups', payload, res => {
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
      io.get('worker-groups/all', payload, res => {
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
  /**
   * get alarm groups all
   */
  getAlarmGroupsAll ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('alert-groups/list', payload, res => {
        state.alarmGroupsListAll = res.data
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  saveWorkerGroups ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('worker-groups', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  deleteWorkerGroups ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`worker-groups/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getWorkerAddresses ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('worker-groups/worker-address-list', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get environment list pages
   */
  getEnvironmentListPaging ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('environment/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * create environment
   */
  createEnvironment ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('environment/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * update environment
   */
  updateEnvironment ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('environment/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * delete environment
   */
  deleteEnvironment ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('environment/delete', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  verifyEnvironment ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('environment/verify-environment', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * get all environment
   */
  getEnvironmentAll ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('environment/query-environment-list', payload, res => {
        let list = res.data
        state.environmentListAll = list
        resolve(list)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
