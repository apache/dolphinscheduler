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
      component: components['security-tenant-manage'],
      meta: {
        title: '租户管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    },
    {
      path: '/security/user-manage',
      name: 'user-manage',
      component: components['security-user-manage'],
      meta: {
        title: '用户管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    },
    {
      path: '/security/alarm-group-manage',
      name: 'alarm-group-manage',
      component: components['security-alarm-group-manage'],
      meta: {
        title: '告警组管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    },
    {
      path: '/security/worker-group-manage',
      name: 'worker-group-manage',
      component: components['security-worker-group-manage'],
      meta: {
        title: 'Worker分组管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    },
    {
      path: '/security/yarn-queue-manage',
      name: 'yarn-queue-manage',
      component: components['security-yarn-queue-manage'],
      meta: {
        title: 'Yarn队列管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    },
    {
      path: '/security/environment-manage',
      name: 'environment-manage',
      component: components['security-environment-manage'],
      meta: {
        title: '环境管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    },
    {
      path: '/security/cluster-manage',
      name: 'cluster-manage',
      component: components['security-cluster-manage'],
      meta: {
        title: '集群管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    },
    {
      path: '/security/token-manage',
      name: 'token-manage',
      component: components['security-token-manage'],
      meta: {
        title: '令牌管理管理',
        activeMenu: 'security',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/security/alarm-instance-manage',
      name: 'alarm-instance-manage',
      component: components['security-alarm-instance-manage'],
      meta: {
        title: '告警实例管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    },
    {
      path: '/security/k8s-namespace-manage',
      name: 'k8s-namespace-manage',
      component: components['security-k8s-namespace-manage'],
      meta: {
        title: 'K8S命名空间管理',
        activeMenu: 'security',
        showSide: true,
        auth: ['ADMIN_USER']
      }
    }
  ]
}
