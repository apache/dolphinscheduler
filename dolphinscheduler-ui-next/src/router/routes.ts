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

import type { RouteRecordRaw } from 'vue-router'
import type { Component } from 'vue'
import utils from '@/utils'
import { useI18n } from 'vue-i18n'

// All TSX files under the views folder automatically generate mapping relationship
const modules = import.meta.glob('/src/views/**/**.tsx')
const components: { [key: string]: Component } = utils.mapping(modules)

/**
 * Basic page
 */
const basePage: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: { name: 'home' },
    meta: { title: '首页' },
    component: () => import('@/layouts/basic'),
    children: [
      {
        path: '/home',
        name: 'home',
        component: components['home'],
        meta: {
          title: '首页'
        },
      },
    ],
  },
]

const projectsPage = [
  {
    path: '/projects',
    name: 'projects',
    redirect: { name: 'projects-list'},
    meta: { title: '项目管理' },
    component: () => import('@/layouts/basic'),
    children: [
      {
        path: '/projects/list',
        name: 'projects-list',
        component: components['home'],
        meta: {
          title: '项目',
        }
      },
      {
        path: '/projects/:projectCode/index',
        name: 'projects-index',
        component: components['home'],
        meta: {
          title: '工作流监控',
        }
      }
    ]
  }
]

const resourcesPage = [
  {
    path: '/resource',
    name: 'resource',
    redirect: { name: 'file' },
    meta: { title: '资源中心' },
    component: () => import('@/layouts/basic'),
    children: [
      {
        path: '/resource/file',
        name: 'file',
        component: components['home'],
        meta: {
          title: '文件管理',
        }
      },
      {
        path: '/resource/file/create',
        name: 'resource-file-create',
        component: components['home'],
        meta: {
          title: '创建资源',
        }
      },
    ]
  }
]

const datasourcePage = [
  {
    path: '/datasource',
    name: 'datasource',
    redirect: { name: 'datasource-list' },
    meta: { title: '数据源中心' },
    component: () => import('@/layouts/basic'),
    children: [
      {
        path: '/datasource/list',
        name: 'datasource-list',
        component: components['home'],
        meta: {
          title: '数据源中心'
        }
      }
    ]
  }
]

const monitorPage = [
  {
    path: '/monitor',
    name: 'monitor',
    meta: { title: 'monitor' },
    redirect: { name: 'servers-master' },
    component: () => import('@/layouts/basic'),
    children: [
      {
        path: '/monitor/servers/master',
        name: 'servers-master',
        component: components['home'],
        meta: {
          title: '服务管理-Master'
        }
      },
      {
        path: '/monitor/servers/worker',
        name: 'servers-worker',
        component: components['home'],
        meta: {
          title: '服务管理-Worker'
        }
      },
    ]
  }
]

const securityPage: RouteRecordRaw[] = [
  {
    path: '/security',
    name: 'security',
    meta: { title: '安全中心' },
    redirect: { name: 'tenement-manage' },
    component: () => import('@/layouts/basicLayout'),
    children: [
      {
        path: '/security/tenant',
        name: 'tenement-manage',
        component: components['home'],
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
    ],
  },
]

/**
 * Login page
 */
const loginPage: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: components['login'],
  },
]

const routes: RouteRecordRaw[] = [...basePage, ...loginPage, ...projectsPage,...resourcesPage, ...datasourcePage, ...monitorPage, ...securityPage]

// 重新组织后导出
export default routes
