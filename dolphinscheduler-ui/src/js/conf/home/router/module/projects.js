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
import store from '@/conf/home/store'
import localStore from '@/module/util/localStorage'

const projects = [
  {
    path: '/projects',
    name: 'projects',
    component: resolve => require(['../../pages/projects'], resolve),
    meta: {
      title: `${i18n.$t('Project')}`
    },
    redirect: {
      name: 'projects-list'
    },
    beforeEnter: (to, from, next) => {
      const blacklist = ['projects', 'projects-list']
      const { projectCode } = to.params || {}
      if (!blacklist.includes(to.name) && projectCode && projectCode !== localStore.getItem('projectCode')) {
        store.dispatch('projects/getProjectByCode', projectCode).then(res => {
          store.commit('dag/setProjectId', res.id)
          store.commit('dag/setProjectCode', res.code)
          store.commit('dag/setProjectName', res.name)
          localStore.setItem('projectId', res.id)
          localStore.setItem('projectCode', res.code)
          localStore.setItem('projectName', res.name)
          next()
        }).catch(e => {
          next({ name: 'projects-list' })
        })
      } else {
        next()
      }
    },
    children: [
      {
        path: '/projects/list',
        name: 'projects-list',
        component: resolve => require(['../../pages/projects/pages/list'], resolve),
        meta: {
          title: `${i18n.$t('Project')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/projects/:projectCode/index',
        name: 'projects-index',
        component: resolve => require(['../../pages/projects/pages/index'], resolve),
        meta: {
          title: `${i18n.$t('Project Home')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/projects/:projectCode/kinship',
        name: 'projects-kinship',
        component: resolve => require(['../../pages/projects/pages/kinship'], resolve),
        meta: {
          title: `${i18n.$t('Kinship')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/projects/:projectCode/definition',
        name: 'definition',
        component: resolve => require(['../../pages/projects/pages/definition'], resolve),
        meta: {
          title: `${i18n.$t('Process definition')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        },
        redirect: {
          name: 'projects-definition-list'
        },
        children: [
          {
            path: '/projects/:projectCode/definition/list',
            name: 'projects-definition-list',
            component: resolve => require(['../../pages/projects/pages/definition/pages/list'], resolve),
            meta: {
              title: `${i18n.$t('Process definition')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          },
          {
            path: '/projects/:projectCode/definition/list/:code',
            name: 'projects-definition-details',
            component: resolve => require(['../../pages/projects/pages/definition/pages/details'], resolve),
            meta: {
              title: `${i18n.$t('Process definition details')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          },
          {
            path: '/projects/:projectCode/definition/create',
            name: 'definition-create',
            component: resolve => require(['../../pages/projects/pages/definition/pages/create'], resolve),
            meta: {
              title: `${i18n.$t('Create process definition')}`
            }
          },
          {
            path: '/projects/:projectCode/definition/tree/:code',
            name: 'definition-tree-view-index',
            component: resolve => require(['../../pages/projects/pages/definition/pages/tree'], resolve),
            meta: {
              title: `${i18n.$t('TreeView')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          },
          {
            path: '/projects/:projectCode/definition/list/timing/:code',
            name: 'definition-timing-details',
            component: resolve => require(['../../pages/projects/pages/definition/timing'], resolve),
            meta: {
              title: `${i18n.$t('Scheduled task list')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          }
        ]
      },
      {
        path: '/projects/:projectCode/instance',
        name: 'instance',
        component: resolve => require(['../../pages/projects/pages/instance'], resolve),
        meta: {
          title: `${i18n.$t('Process Instance')}`
        },
        redirect: {
          name: 'projects-instance-list'
        },
        children: [
          {
            path: '/projects/:projectCode/instance/list',
            name: 'projects-instance-list',
            component: resolve => require(['../../pages/projects/pages/instance/pages/list'], resolve),
            meta: {
              title: `${i18n.$t('Process Instance')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          },
          {
            path: '/projects/:projectCode/instance/list/:id',
            name: 'projects-instance-details',
            component: resolve => require(['../../pages/projects/pages/instance/pages/details'], resolve),
            meta: {
              title: `${i18n.$t('Process instance details')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          },
          {
            path: '/projects/:projectCode/instance/gantt/:id',
            name: 'instance-gantt-index',
            component: resolve => require(['../../pages/projects/pages/instance/pages/gantt'], resolve),
            meta: {
              title: `${i18n.$t('Gantt')}`,
              refreshInSwitchedTab: config.refreshInSwitchedTab
            }
          }
        ]
      },
      {
        path: '/projects/:projectCode/task-instance',
        name: 'task-instance',
        component: resolve => require(['../../pages/projects/pages/taskInstance'], resolve),
        meta: {
          title: `${i18n.$t('Task Instance')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }

      },
      {
        path: '/projects/:projectCode/task-definition',
        name: 'task-definition',
        component: resolve => require(['../../pages/projects/pages/taskDefinition'], resolve),
        meta: {
          title: `${i18n.$t('Task Definition')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/projects/:projectCode/task-record',
        name: 'task-record',
        component: resolve => require(['../../pages/projects/pages/taskRecord'], resolve),
        meta: {
          title: `${i18n.$t('Task record')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }
      },
      {
        path: '/projects/:projectCode/history-task-record',
        name: 'history-task-record',
        component: resolve => require(['../../pages/projects/pages/historyTaskRecord'], resolve),
        meta: {
          title: `${i18n.$t('History task record')}`,
          refreshInSwitchedTab: config.refreshInSwitchedTab
        }

      }
    ]
  }
]

export default projects
