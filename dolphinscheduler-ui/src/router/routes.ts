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
import projectsPage from './modules/projects'
import resourcesPage from './modules/resources'
import datasourcePage from './modules/datasource'
import monitorPage from './modules/monitor'
import securityPage from './modules/security'
import dataQualityPage from './modules/data-quality'
// todo: why is it throwing cannot find module and its corresponding type, but the render is working?
import uiSettingPage from './modules/ui-setting'

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
    component: () => import('@/layouts/content'),
    children: [
      {
        path: '/home',
        name: 'home',
        component: components['home'],
        meta: {
          title: '首页',
          activeMenu: 'home',
          auth: []
        }
      },
      {
        path: '/password',
        name: 'password',
        component: components['password'],
        meta: {
          title: '修改密码',
          auth: []
        }
      },
      {
        path: '/profile',
        name: 'profile',
        component: components['profile'],
        meta: {
          title: '用户信息',
          auth: []
        }
      }
    ]
  },
  projectsPage,
  resourcesPage,
  datasourcePage,
  monitorPage,
  securityPage,
  dataQualityPage,
  uiSettingPage
]

/**
 * Login page
 */
const loginPage: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: components['login'],
    meta: {
      auth: []
    }
  }
]

const routes: RouteRecordRaw[] = [...basePage, ...loginPage]

// 重新组织后导出
export default routes
