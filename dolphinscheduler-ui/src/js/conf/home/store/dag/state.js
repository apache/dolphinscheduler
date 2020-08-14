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

// Get the name of the item currently clicked
const projectName = localStore.getItem('projectName')

export default {
  // name
  name: '',
  // description
  description: '',
  // Node global parameter
  globalParams: [],
  // Node information
  tasks: [],
  // Node cache information, cache the previous input
  cacheTasks: {},
  // Timeout alarm
  timeout: 0,
  // tenant id
  tenantId: -1,
  // Node location information
  locations: {},
  // Node-to-node connection
  connects: [],
  // Running sign
  runFlag: '',
  // Whether to edit
  isEditDag: false,
  // Current project
  projectName: projectName || '',
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
  // tasks resourcesListPy
  resourcesListPy: [],
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
    }
  ],
  // Alarm interface
  notifyGroupListS: [],
  // Process instance list{ view a single record }
  instanceListS: [],
  // Operating state
  isDetails: false,
  startup: {

  }
}
