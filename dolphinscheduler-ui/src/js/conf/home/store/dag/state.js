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

import localStore from '@/module/util/localStorage'

// Get the project currently clicked
const projectId = localStore.getItem('projectId')
const projectCode = localStore.getItem('projectCode')
const projectName = localStore.getItem('projectName')

export default {
  // process definition code
  code: '',
  // process definition version
  version: '',
  // name
  name: '',
  // description
  description: '',
  // Node global parameter
  globalParams: [],
  // Node information
  tasks: [],
  // Timeout alarm
  timeout: 0,
  // process execute type
  executionType: 'PARALLEL',
  // tenant code
  tenantCode: 'default',
  // Node location information
  locations: {},
  // Node relations
  connects: [],
  // Running sign
  runFlag: '',
  // Whether to edit
  isEditDag: false,
  // Current project id
  projectId: projectId,
  // Current project code
  projectCode: projectCode,
  // Current project name
  projectName: projectName || '',
  // Whether to go online the process definition
  releaseState: 'ONLINE',
  // Whether to update the process definition
  syncDefine: true,
  // tasks processList
  processListS: [],
  // projectList
  projectListS: [],
  // tasks resourcesList
  resourcesListS: [],
  // tasks resourcesListJar
  resourcesListJar: [],
  // tasks datasource Type
  dsTypeListS: [
    {
      id: 0,
      code: 'MYSQL',
      disabled: false
    },
    {
      id: 1,
      code: 'POSTGRESQL',
      disabled: false
    },
    {
      id: 2,
      code: 'HIVE',
      disabled: false
    },
    {
      id: 3,
      code: 'SPARK',
      disabled: false
    },
    {
      id: 4,
      code: 'CLICKHOUSE',
      disabled: false
    },
    {
      id: 5,
      code: 'ORACLE',
      disabled: false
    },
    {
      id: 6,
      code: 'SQLSERVER',
      disabled: false
    },
    {
      id: 7,
      code: 'DB2',
      disabled: false
    },
    {
      id: 8,
      code: 'PRESTO',
      disabled: false
    },
    {
      id: 9,
      code: 'REDSHIFT',
      disabled: false
    }
  ],
  // Alarm interface
  notifyGroupListS: [],
  // Process instance list{ view a single record }
  instanceListS: [],
  // Operating state
  isDetails: false,
  startup: {},
  taskInstances: [],
  dependResult: {}
}
