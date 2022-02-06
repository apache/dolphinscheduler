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

import i18n from '@/module/i18n/index.js'
import config from '~/external/config'

const resource = [
  {
    path: '/resource',
    name: 'resource',
    component: resolve => require(['../../pages/resource'], resolve),
    redirect: {
      name: 'file'
    },
    meta: {
      title: `${i18n.$t('Resources')}`,
      refreshInSwitchedTab: config.refreshInSwitchedTab
    },
    children: [
      {
        path: '/resource/file',
        name: 'file',
        component: resolve => require(['../../pages/resource/pages/file/pages/list'], resolve),
        meta: {
          title: `${i18n.$t('File Manage')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/resource/file/create',
        name: 'resource-file-create',
        component: resolve => require(['../../pages/resource/pages/file/pages/create'], resolve),
        meta: {
          title: `${i18n.$t('Create Resource')}`
        }
      },
      {
        path: '/resource/file/createFolder',
        name: 'resource-file-createFolder',
        component: resolve => require(['../../pages/resource/pages/file/pages/createFolder'], resolve),
        meta: {
          title: `${i18n.$t('Create Resource')}`
        }
      },
      {
        path: '/resource/file/subFileFolder/:id',
        name: 'resource-file-subFileFolder',
        component: resolve => require(['../../pages/resource/pages/file/pages/subFileFolder'], resolve),
        meta: {
          title: `${i18n.$t('Create Resource')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/resource/file/subFile/:id',
        name: 'resource-file-subFile',
        component: resolve => require(['../../pages/resource/pages/file/pages/subFile'], resolve),
        meta: {
          title: `${i18n.$t('Create Resource')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/resource/file/list/:id',
        name: 'resource-file-details',
        component: resolve => require(['../../pages/resource/pages/file/pages/details'], resolve),
        meta: {
          title: `${i18n.$t('File Details')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/resource/file/subdirectory/:id',
        name: 'resource-file-subdirectory',
        component: resolve => require(['../../pages/resource/pages/file/pages/subdirectory'], resolve),
        meta: {
          title: `${i18n.$t('File Manage')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/resource/file/edit/:id',
        name: 'resource-file-edit',
        component: resolve => require(['../../pages/resource/pages/file/pages/edit'], resolve),
        meta: {
          title: `${i18n.$t('File Details')}`
        }
      },
      {
        path: '/resource/udf',
        name: 'udf',
        component: resolve => require(['../../pages/resource/pages/udf'], resolve),
        meta: {
          title: `${i18n.$t('UDF manage')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        },
        children: [
          {
            path: '/resource/udf',
            name: 'resource-udf',
            component: resolve => require(['../../pages/resource/pages/udf/pages/resource'], resolve),
            meta: {
              title: `${i18n.$t('UDF Resources')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          },
          {
            path: '/resource/udf/subUdfDirectory/:id',
            name: 'resource-udf-subUdfDirectory',
            component: resolve => require(['../../pages/resource/pages/udf/pages/subUdfDirectory'], resolve),
            meta: {
              title: `${i18n.$t('UDF Resources')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          },
          {
            path: '/resource/udf/createUdfFolder',
            name: 'resource-udf-createUdfFolder',
            component: resolve => require(['../../pages/resource/pages/udf/pages/createUdfFolder'], resolve),
            meta: {
              title: `${i18n.$t('Create Resource')}`
            }
          },
          {
            path: '/resource/udf/subCreateUdfFolder/:id',
            name: 'resource-udf-subCreateUdfFolder',
            component: resolve => require(['../../pages/resource/pages/udf/pages/subUdfFolder'], resolve),
            meta: {
              title: `${i18n.$t('Create Resource')}`
            }
          },
          {
            path: '/resource/func',
            name: 'resource-func',
            component: resolve => require(['../../pages/resource/pages/udf/pages/function'], resolve),
            meta: {
              title: `${i18n.$t('UDF Function')}`
            }
          }
        ]
      },
      {
        path: '/resource/task-group',
        name: 'task-group-manage',
        component: resolve => require(['../../pages/resource/pages/taskGroups'], resolve),
        meta: {
          title: `${i18n.$t('Task group manage')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        },
        children: [
          {
            path: '/resource/task-group',
            name: 'task-group-option',
            component: resolve => require(['../../pages/resource/pages/taskGroups/taskGroupOption'], resolve),
            meta: {
              title: `${i18n.$t('Task group option')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          },
          {
            path: '/resource/task-group-queue',
            name: 'task-group-queue',
            component: resolve => require(['../../pages/resource/pages/taskGroups/taskGroupQueue'], resolve),
            meta: {
              title: `${i18n.$t('Task group queue')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          }
        ]
      }
    ]
  }
]

export default resource
