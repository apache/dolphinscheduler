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
  path: '/data-quality',
  name: 'data-quality',
  meta: { title: 'data-quality' },
  redirect: { name: 'task-result' },
  component: () => import('@/layouts/content'),
  children: [
    {
      path: '/data-quality/task-result',
      name: 'task-result',
      component: components['data-quality-task-result'],
      meta: {
        title: '数据质量-task-result',
        activeMenu: 'data-quality',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/data-quality/rule',
      name: 'data-quality-rule',
      component: components['data-quality-rule'],
      meta: {
        title: '数据质量-rule',
        activeMenu: 'data-quality',
        showSide: true,
        auth: []
      }
    }
  ]
}
