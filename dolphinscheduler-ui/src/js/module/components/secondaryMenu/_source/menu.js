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

import i18n from '@/module/i18n'
import config from '~/external/config'
import Permissions from '@/module/permissions'

const menu = {
  projects: [
    {
      name: `${i18n.$t('Project Home')}`,
      id: 0,
      path: 'projects-index',
      isOpen: true,
      enabled: true,
      icon: 'ri-home-4-line',
      children: []
    },
    {
      name: `${i18n.$t('Kinship')}`,
      id: 1,
      path: 'projects-kinship',
      isOpen: true,
      enabled: true,
      icon: 'ri-node-tree',
      children: []
    },
    {
      name: `${i18n.$t('Process')}`,
      id: 2,
      path: '',
      isOpen: true,
      enabled: true,
      icon: 'el-icon-s-tools',
      children: [
        {
          name: `${i18n.$t('Process definition')}`,
          path: 'definition',
          id: 0,
          enabled: true
        },
        {
          name: `${i18n.$t('Process Instance')}`,
          path: 'instance',
          id: 1,
          enabled: true
        },
        {
          name: `${i18n.$t('Task Instance')}`,
          path: 'task-instance',
          id: 2,
          enabled: true
        },
        {
          name: `${i18n.$t('Task record')}`,
          path: 'task-record',
          id: 3,
          enabled: config.recordSwitch
        },
        {
          name: `${i18n.$t('History task record')}`,
          path: 'history-task-record',
          id: 4,
          enabled: config.recordSwitch
        }
      ]
    }
  ],

  security: [
    {
      name: `${i18n.$t('Tenant Manage')}`,
      id: 0,
      path: 'tenement-manage',
      isOpen: true,
      enabled: true,
      icon: 'el-icon-user-solid',
      children: []
    },
    {
      name: `${i18n.$t('User Manage')}`,
      id: 1,
      path: 'users-manage',
      isOpen: true,
      enabled: true,
      icon: 'el-icon-user-solid',
      children: []
    },
    {
      name: `${i18n.$t('Warning group manage')}`,
      id: 2,
      path: 'warning-groups-manage',
      isOpen: true,
      enabled: true,
      icon: 'el-icon-warning',
      children: []
    },
    {
      name: `${i18n.$t('Warning instance manage')}`,
      id: 2,
      path: 'warning-instance-manage',
      isOpen: true,
      enabled: true,
      icon: 'ri-spam-fill',
      children: []
    },
    {
      name: `${i18n.$t('Worker group manage')}`,
      id: 4,
      path: 'worker-groups-manage',
      isOpen: true,
      enabled: true,
      icon: 'el-icon-s-custom',
      children: []
    },
    {
      name: `${i18n.$t('Queue manage')}`,
      id: 3,
      path: 'queue-manage',
      isOpen: true,
      enabled: true,
      icon: 'ri-group-line',
      children: []
    },
    {
      name: `${i18n.$t('Token manage')}`,
      id: 2,
      path: 'token-manage',
      isOpen: true,
      icon: 'el-icon-document',
      children: [],
      enabled: true
    }
  ],
  resource: [
    {
      name: `${i18n.$t('File Manage')}`,
      id: 0,
      path: 'file',
      isOpen: true,
      icon: 'el-icon-document-copy',
      children: [],
      enabled: true
    },
    {
      name: `${i18n.$t('UDF manage')}`,
      id: 1,
      path: '',
      isOpen: true,
      icon: 'el-icon-document',
      enabled: true,
      children: [
        {
          name: `${i18n.$t('Resource manage')}`,
          path: 'resource-udf',
          id: 0,
          enabled: true
        },
        {
          name: `${i18n.$t('Function manage')}`,
          path: 'resource-func',
          id: 1,
          enabled: true
        }
      ]
    }
  ],
  user: [
    {
      name: `${i18n.$t('User Information')}`,
      id: 0,
      path: 'account',
      isOpen: true,
      icon: 'el-icon-user-solid',
      children: [],
      enabled: true
    },
    {
      name: `${i18n.$t('Edit password')}`,
      id: 1,
      path: 'password',
      isOpen: true,
      icon: 'el-icon-key',
      children: [],
      enabled: true
    },
    {
      name: `${i18n.$t('Token manage')}`,
      id: 2,
      path: 'token',
      isOpen: true,
      icon: 'el-icon-s-custom',
      children: [],
      enabled: Permissions.getAuth()
    }
  ],
  monitor: [
    {
      name: `${i18n.$t('Servers manage')}`,
      id: 1,
      path: '',
      isOpen: true,
      enabled: true,
      icon: 'el-icon-menu',
      children: [
        {
          name: 'Master',
          path: 'servers-master',
          id: 0,
          enabled: true
        },
        {
          name: 'Worker',
          path: 'servers-worker',
          id: 1,
          enabled: true
        },
        {
          name: 'Zookeeper',
          path: 'servers-zookeeper',
          id: 4,
          enabled: true
        },
        {
          name: 'DB',
          path: 'servers-db',
          id: 6,
          enabled: true
        }
      ]
    },
    {
      name: `${i18n.$t('Statistics manage')}`,
      id: 0,
      path: '',
      isOpen: true,
      enabled: true,
      icon: 'el-icon-menu',
      children: [
        {
          name: 'Statistics',
          path: 'statistics',
          id: 0,
          enabled: true
        }
      ]
    }
  ]
}

export default type => menu[type]
