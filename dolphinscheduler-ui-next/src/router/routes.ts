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

// All TSX files under the views folder automatically generate mapping relationship
const modules = import.meta.glob('/src/views/**/**.tsx')
const components: { [key: string]: Component } = utils.classification(modules)

/**
 * Basic page
 */
const basePage: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: { name: 'home' },
    component: () => import('@/layouts/content/Content'),
    children: [
      {
        path: '/home',
        name: 'home',
        component: components['home'],
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

const routes: RouteRecordRaw[] = [...basePage, ...loginPage]

// 重新组织后导出
export default routes
