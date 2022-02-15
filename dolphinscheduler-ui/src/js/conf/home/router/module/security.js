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

const security = [
  {
    path: '/security',
    name: 'security',
    component: resolve => require(['../../pages/security'], resolve),
    meta: {
      title: `${i18n.$t('Security')}`
    },
    redirect: {
      name: 'tenement-manage'
    },
    children: [
      {
        path: '/security/tenant',
        name: 'tenement-manage',
        component: resolve => require(['../../pages/security/pages/tenement'], resolve),
        meta: {
          title: `${i18n.$t('Tenant Manage')}`
        }
      },
      {
        path: '/security/users',
        name: 'users-manage',
        component: resolve => require(['../../pages/security/pages/users'], resolve),
        meta: {
          title: `${i18n.$t('User Manage')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/security/warning-groups',
        name: 'warning-groups-manage',
        component: resolve => require(['../../pages/security/pages/warningGroups'], resolve),
        meta: {
          title: `${i18n.$t('Warning group manage')}`
        }
      },
      {
        path: '/security/warning-instance',
        name: 'warning-instance-manage',
        component: resolve => require(['../../pages/security/pages/warningInstance'], resolve),
        meta: {
          title: `${i18n.$t('Warning instance manage')}`
        }
      },
      {
        path: '/security/queue',
        name: 'queue-manage',
        component: resolve => require(['../../pages/security/pages/queue'], resolve),
        meta: {
          title: `${i18n.$t('Queue manage')}`
        }
      },
      {
        path: '/security/worker-groups',
        name: 'worker-groups-manage',
        component: resolve => require(['../../pages/security/pages/workerGroups'], resolve),
        meta: {
          title: `${i18n.$t('Worker group manage')}`
        }
      },
      {
        path: '/security/environments',
        name: 'environment-manage',
        component: resolve => require(['../../pages/security/pages/environment'], resolve),
        meta: {
          title: `${i18n.$t('Environment manage')}`
        }
      },
      {
        path: '/security/token',
        name: 'token-manage',
        component: resolve => require(['../../pages/security/pages/token'], resolve),
        meta: {
          title: `${i18n.$t('Token manage')}`
        }
      },
      {
        path: '/security/namespace',
        name: 'namespace',
        component: resolve => require(['../../pages/security/pages/namespace'], resolve),
        meta: {
          title: `${i18n.$t('K8s Namespace')}`
        }
      }
    ]
  }
]

export default security
