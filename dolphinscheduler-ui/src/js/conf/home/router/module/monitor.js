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

const monitor = [
  {
    path: '/monitor',
    name: 'monitor',
    component: resolve => require(['../../pages/monitor'], resolve),
    meta: {
      title: 'monitor'
    },
    redirect: {
      name: 'servers-master'
    },
    children: [
      {
        path: '/monitor/servers/master',
        name: 'servers-master',
        component: resolve => require(['../../pages/monitor/pages/servers/master'], resolve),
        meta: {
          title: `${i18n.$t('Service-Master')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/monitor/servers/worker',
        name: 'servers-worker',
        component: resolve => require(['../../pages/monitor/pages/servers/worker'], resolve),
        meta: {
          title: `${i18n.$t('Service-Worker')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/monitor/servers/alert',
        name: 'servers-alert',
        component: resolve => require(['../../pages/monitor/pages/servers/alert'], resolve),
        meta: {
          title: 'Alert',
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/monitor/servers/rpcserver',
        name: 'servers-rpcserver',
        component: resolve => require(['../../pages/monitor/pages/servers/rpcserver'], resolve),
        meta: {
          title: 'Rpcserver',
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/monitor/servers/apiserver',
        name: 'servers-apiserver',
        component: resolve => require(['../../pages/monitor/pages/servers/apiserver'], resolve),
        meta: {
          title: 'Apiserver',
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/monitor/servers/db',
        name: 'servers-db',
        component: resolve => require(['../../pages/monitor/pages/servers/db'], resolve),
        meta: {
          title: 'DB',
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/monitor/servers/statistics',
        name: 'statistics',
        component: resolve => require(['../../pages/monitor/pages/servers/statistics'], resolve),
        meta: {
          title: 'statistics',
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/monitor/audit/log',
        name: 'audit-log',
        component: resolve => require(['../../pages/monitor/pages/log/index'], resolve),
        meta: {
          title: 'audit-log',
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      }
    ]
  }
]

export default monitor
