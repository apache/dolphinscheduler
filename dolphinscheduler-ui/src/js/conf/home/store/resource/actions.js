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
      io.get('resources/list-paging', payload, res => {
        resolve(res.data)
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
      io.get('resources/delete', payload, res => {
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
      io.get('resources/view', payload, res => {
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
      io.post('resources/udf-func/create', payload, res => {
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
      io.post('resources/udf-func/update', payload, res => {
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
      io.get('resources/udf-func/delete', payload, res => {
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
      io.get('resources/udf-func/list-paging', payload, res => {
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
      io.post('resources/update-content', payload, res => {
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
      io.post('resources/directory/create', payload, res => {
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
      io.post('resources/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  }

}
