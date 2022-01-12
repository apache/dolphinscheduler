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
  path: '/resource',
  name: 'resource',
  redirect: { name: 'file' },
  meta: { title: '资源中心' },
  component: () => import('@/layouts/content'),
  children: [
    {
      path: '/resource/file',
      name: 'file',
      component: components['home'],
      meta: {
        title: '文件管理',
      },
    },
    {
      path: '/resource/file/create',
      name: 'resource-file-create',
      component: components['home'],
      meta: {
        title: '创建资源',
      },
    },
    {
      path: '/resource/task-group',
      name: 'task-group-manage',
      component: components['taskGroupOption'],
      children: [
        {
          path: '/resource/task-group-option',
          name: 'task-group-option',
          component: components['taskGroupOption']
        },
        {
          path: '/resource/task-group-queue',
          name: 'task-group-queue',
          component: components['taskGroupQueue']
        }
      ]
    }
  ],
}
