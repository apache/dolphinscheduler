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
   * Data source creation
   * @param "type": string,//MYSQL, POSTGRESQL, HIVE, SPARK, CLICKHOUSE, ORACLE, SQLSERVER
   * @param "name": string,
   * @param "desc": string,
   * @param "parameter":string //{"address":"jdbc:hive2://192.168.220.189:10000","autoReconnect":"true","characterEncoding":"utf8","database":"default","initialTimeout":3000,"jdbcUrl":"jdbc:hive2://192.168.220.189:10000/default","maxReconnect":10,"password":"","useUnicode":true,"user":"hive"}
   */
  createDatasources ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('datasources/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Test connection
   * @param "id": int
   */
  connectDatasources ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('datasources/connect', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query data source list - no paging
   * @param "type": string//MYSQL, POSTGRESQL, HIVE, SPARK, CLICKHOUSE, ORACLE, SQLSERVER
   */
  getDatasourcesList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('datasources/list', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query data source list - paging
   * @param "searchVal": string,
   * @param "pageNo": int,
   * @param "pageSize": int
   */
  getDatasourcesListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('datasources/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Delete data source
   */
  deleteDatasource ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('datasources/delete', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Data source editing
   */
  updateDatasource ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('datasources/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getEditDatasource ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('datasources/update-ui', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  verifyName ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('datasources/verify-name', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getKerberosStartupState ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('datasources/kerberos-startup-state', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
