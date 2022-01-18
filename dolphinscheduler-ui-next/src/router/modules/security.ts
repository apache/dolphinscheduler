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

import type { Component } from 'vue'
import utils from '@/utils'

// All TSX files under the views folder automatically generate mapping relationship
const modules = import.meta.glob('/src/views/**/**.tsx')
const components: { [key: string]: Component } = utils.mapping(modules)

export default {
  path: '/security',
  name: 'security',
  meta: { title: '安全中心' },
  redirect: { name: 'tenant-manage' },
  component: () => import('@/layouts/content'),
  children: [
    {
      path: '/security/tenant-manage',
      name: 'tenant-manage',
      component: components['tenant-manage'],
      meta: {
        title: '租户管理'
      }
    },
    {
      path: '/security/users',
      name: 'users-manage',
      component: components['home'],
      meta: {
        title: '用户管理'
      }
    },
    {
      path: '/security/alarm-group-manage',
      name: 'alarm-group-manage',
      component: components['alarm-group-manage'],
      meta: {
        title: '告警组管理'
      }
    },
    {
      path: '/security/worker-group-manage',
      name: 'worker-group-manage',
      component: components['worker-group-manage'],
      meta: {
        title: 'Worker分组管理'
      }
    },
    {
      path: '/security/yarn-queue-manage',
      name: 'yarn-queue-manage',
      component: components['yarn-queue-manage'],
      meta: {
        title: 'Yarn队列管理'
      }
    },
    {
      path: '/security/environment-manage',
      name: 'environment-manage',
      component: components['environment-manage'],
      meta: {
        title: '环境管理'
      }
    },
    {
      path: '/security/token-manage',
      name: 'token-manage',
      component: components['token-manage'],
      meta: {
        title: '令牌管理管理'
      }
    }
  ]
}
