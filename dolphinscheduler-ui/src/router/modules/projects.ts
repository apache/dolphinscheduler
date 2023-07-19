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
  path: '/projects',
  name: 'projects',
  meta: {
    title: '项目管理'
  },
  redirect: { name: 'projects-list' },
  component: () => import('@/layouts/content'),
  children: [
    {
      path: '/projects/list',
      name: 'projects-list',
      component: components['projects-list'],
      meta: {
        title: '项目',
        activeMenu: 'projects',
        showSide: false,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode',
      name: 'projects-overview',
      component: components['projects-overview'],
      meta: {
        title: '项目概览',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/parameter',
      name: 'projects-parameter',
      component: components['projects-parameter'],
      meta: {
        title: '项目级参数',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/preferences',
      name: 'projects-preference',
      component: components['projects-preference'],
      meta: {
        title: '项目偏好设置',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow/relation',
      name: 'workflow-relation',
      component: components['projects-workflow-relation'],
      meta: {
        title: '工作流关系',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow-definition',
      name: 'workflow-definition-list',
      component: components['projects-workflow-definition'],
      meta: {
        title: '工作流定义',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow-definition/timing/:definitionCode',
      name: 'workflow-definition-timing',
      component: components['projects-workflow-definition-timing'],
      meta: {
        title: '定时管理',
        activeMenu: 'projects',
        activeSide: '/projects/:projectCode/workflow-definition',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow/definitions/create',
      name: 'workflow-definition-create',
      component: components['projects-workflow-definition-create'],
      meta: {
        title: '创建工作流定义',
        activeMenu: 'projects',
        activeSide: '/projects/:projectCode/workflow-definition',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow/definitions/:code',
      name: 'workflow-definition-detail',
      component: components['projects-workflow-definition-detail'],
      meta: {
        title: '工作流定义详情',
        activeMenu: 'projects',
        activeSide: '/projects/:projectCode/workflow-definition',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow/timings',
      name: 'workflow-timing-list',
      component: components['projects-workflow-timing'],
      meta: {
        title: '工作流定时管理',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow/instances',
      name: 'workflow-instance-list',
      component: components['projects-workflow-instance'],
      meta: {
        title: '工作流实例',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow/instances/:id',
      name: 'workflow-instance-detail',
      component: components['projects-workflow-instance-detail'],
      meta: {
        title: '工作流实例详情',
        activeMenu: 'projects',
        activeSide: '/projects/:projectCode/workflow/instances',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow/instances/:id/gantt',
      name: 'workflow-instance-gantt',
      component: components['projects-workflow-instance-gantt'],
      meta: {
        title: '工作流实例甘特图',
        activeMenu: 'projects',
        activeSide: '/projects/:projectCode/workflow/instances',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/task/definitions',
      name: 'task-definition',
      component: components['projects-task-definition'],
      meta: {
        title: '任务定义',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/task/instances',
      name: 'task-instance',
      component: components['projects-task-instance'],
      meta: {
        title: '任务实例',
        activeMenu: 'projects',
        showSide: true,
        auth: []
      }
    },
    {
      path: '/projects/:projectCode/workflow-definition/tree/:definitionCode',
      name: 'workflow-definition-tree',
      component: components['projects-workflow-definition-tree'],
      meta: {
        title: '工作流定义树形图',
        activeMenu: 'projects',
        activeSide: '/projects/:projectCode/workflow-definition',
        showSide: true,
        auth: []
      }
    }
  ]
}
