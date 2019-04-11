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

import Vue from 'vue'
import i18n from '@/module/i18n/index.js'
import Router from 'vue-router'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/',
      name: 'index',
      redirect: {
        name: 'home'
      }
    },
    {
      path: '/home',
      name: 'home',
      component: resolve => require(['../pages/home/index'], resolve),
      meta: {
        title: `${i18n.$t('首页')} - EasyScheduler`
      }
    },
    {
      path: '/projects',
      name: 'projects',
      component: resolve => require(['../pages/projects/index'], resolve),
      meta: {
        title: `${i18n.$t('项目管理')}`
      },
      redirect: {
        name: 'projects-list'
      },
      children: [
        {
          path: '/projects/index',
          name: 'projects-index',
          component: resolve => require(['../pages/projects/pages/index/index'], resolve),
          meta: {
            title: `${i18n.$t('项目首页')}`
          }
        },
        {
          path: '/projects/list',
          name: 'projects-list',
          component: resolve => require(['../pages/projects/pages/list/index'], resolve),
          meta: {
            title: `${i18n.$t('项目')}`
          }
        },
        {
          path: '/projects/definition',
          name: 'definition',
          component: resolve => require(['../pages/projects/pages/definition/index'], resolve),
          meta: {
            title: `${i18n.$t('工作流定义')}`
          },
          redirect: {
            name: 'projects-definition-list'
          },
          children: [
            {
              path: '/projects/definition/list',
              name: 'projects-definition-list',
              component: resolve => require(['../pages/projects/pages/definition/pages/list/index'], resolve),
              meta: {
                title: `${i18n.$t('工作流定义')}`
              }
            },
            {
              path: '/projects/definition/list/:id',
              name: 'projects-definition-details',
              component: resolve => require(['../pages/projects/pages/definition/pages/details/index'], resolve),
              meta: {
                title: `${i18n.$t('流程定义详情')}`
              }
            },
            {
              path: '/projects/definition/create',
              name: 'definition-create',
              component: resolve => require(['../pages/projects/pages/definition/pages/create/index'], resolve),
              meta: {
                title: `${i18n.$t('创建流程定义')}`
              }
            },
            {
              path: '/projects/definition/tree/:id',
              name: 'definition-tree-view-index',
              component: resolve => require(['../pages/projects/pages/definition/pages/tree/index'], resolve),
              meta: {
                title: `${i18n.$t('树形图')}`
              }
            },
            {
              path: '/projects/definition/list/timing/:id',
              name: 'definition-timing-details',
              component: resolve => require(['../pages/projects/pages/definition/timing/index'], resolve),
              meta: {
                title: `${i18n.$t('定时任务列表')}`
              }
            }
          ]
        },
        {
          path: '/projects/instance',
          name: 'instance',
          component: resolve => require(['../pages/projects/pages/instance/index'], resolve),
          meta: {
            title: `${i18n.$t('工作流实例')}`
          },
          redirect: {
            name: 'projects-instance-list'
          },
          children: [
            {
              path: '/projects/instance/list',
              name: 'projects-instance-list',
              component: resolve => require(['../pages/projects/pages/instance/pages/list/index'], resolve),
              meta: {
                title: `${i18n.$t('工作流实例')}`
              }
            },
            {
              path: '/projects/instance/list/:id',
              name: 'projects-instance-details',
              component: resolve => require(['../pages/projects/pages/instance/pages/details/index'], resolve),
              meta: {
                title: `${i18n.$t('流程实例详情')}`
              }
            },
            {
              path: '/projects/instance/gantt/:id',
              name: 'instance-gantt-index',
              component: resolve => require(['../pages/projects/pages/instance/pages/gantt/index'], resolve),
              meta: {
                title: `${i18n.$t('甘特图')}`
              }
            }
          ]
        },
        {
          path: '/projects/task-instance',
          name: 'task-instance-index',
          component: resolve => require(['../pages/projects/pages/taskInstance'], resolve),
          meta: {
            title: `${i18n.$t('任务实例')}`
          },
          redirect: {
            name: 'task-instance-list'
          },
          children: [
            {
              path: '/projects/task-instance/list',
              name: 'task-instance-list',
              component: resolve => require(['../pages/projects/pages/taskInstance/pages/list/index'], resolve),
              meta: {
                title: `${i18n.$t('任务实例')}`
              }
            }
          ]
        },
        {
          path: '/projects/task-record',
          name: 'task-record-index',
          component: resolve => require(['../pages/projects/pages/taskRecord'], resolve),
          meta: {
            title: `${i18n.$t('任务记录')}`
          },
          redirect: {
            name: 'task-record-list'
          },
          children: [
            {
              path: '/projects/task-record/list',
              name: 'task-record-list',
              component: resolve => require(['../pages/projects/pages/taskRecord/pages/list/index'], resolve),
              meta: {
                title: `${i18n.$t('任务记录')}`
              }
            }
          ]
        }
      ]
    },
    {
      path: '/resource',
      name: 'resource',
      component: resolve => require(['../pages/resource/index'], resolve),
      redirect: {
        name: 'file'
      },
      meta: {
        title: `${i18n.$t('资源中心')}`
      },
      children: [
        {
          path: '/resource/file',
          name: 'file',
          component: resolve => require(['../pages/resource/pages/file/pages/list/index'], resolve),
          meta: {
            title: `${i18n.$t('文件管理')}`
          }
        },
        {
          path: '/resource/file/create',
          name: 'resource-file-create',
          component: resolve => require(['../pages/resource/pages/file/pages/create/index'], resolve),
          meta: {
            title: `${i18n.$t('创建资源')}`
          }
        },
        {
          path: '/resource/file/list/:id',
          name: 'resource-file-details',
          component: resolve => require(['../pages/resource/pages/file/pages/details/index'], resolve),
          meta: {
            title: `${i18n.$t('文件详情')}`
          }
        },
        {
          path: '/resource/file/edit/:id',
          name: 'resource-file-edit',
          component: resolve => require(['../pages/resource/pages/file/pages/edit/index'], resolve),
          meta: {
            title: `${i18n.$t('文件详情')}`
          }
        },
        {
          path: '/resource/udf',
          name: 'udf',
          component: resolve => require(['../pages/resource/pages/udf/index'], resolve),
          meta: {
            title: `${i18n.$t('UDF管理')}`
          },
          children: [
            {
              path: '/resource/udf/resource',
              name: 'resource-udf-resource',
              component: resolve => require(['../pages/resource/pages/udf/pages/resource/index'], resolve),
              meta: {
                title: `${i18n.$t('UDF资源管理')}`
              }
            },
            {
              path: '/resource/udf/function',
              name: 'resource-udf-function',
              component: resolve => require(['../pages/resource/pages/udf/pages/function/index'], resolve),
              meta: {
                title: `${i18n.$t('UDF函数管理')}`
              }
            }
          ]
        }
      ]
    },
    {
      path: '/datasource',
      name: 'datasource',
      component: resolve => require(['../pages/datasource/index'], resolve),
      meta: {
        title: `${i18n.$t('数据源中心')}`
      },
      redirect: {
        name: 'datasource-list'
      },
      children: [
        {
          path: '/datasource/list',
          name: 'datasource-list',
          component: resolve => require(['../pages/datasource/pages/list/index'], resolve),
          meta: {
            title: `${i18n.$t('数据源中心')}`
          }
        }
      ]
    },
    {
      path: '/security',
      name: 'security',
      component: resolve => require(['../pages/security/index'], resolve),
      meta: {
        title: `${i18n.$t('安全中心')}`
      },
      redirect: {
        name: 'tenement-manage'
      },
      children: [
        {
          path: '/security/tenant',
          name: 'tenement-manage',
          component: resolve => require(['../pages/security/pages/tenement/index'], resolve),
          meta: {
            title: `${i18n.$t('租户管理')}`
          }
        },
        {
          path: '/security/users',
          name: 'users-manage',
          component: resolve => require(['../pages/security/pages/users/index'], resolve),
          meta: {
            title: `${i18n.$t('用户管理')}`
          }
        },
        {
          path: '/security/warning-groups',
          name: 'warning-groups-manage',
          component: resolve => require(['../pages/security/pages/warningGroups/index'], resolve),
          meta: {
            title: `${i18n.$t('告警组管理')}`
          }
        },
        {
          path: '/security/queue',
          name: 'queue-manage',
          component: resolve => require(['../pages/security/pages/queue/index'], resolve),
          meta: {
            title: `${i18n.$t('队列管理')}`
          }
        },
        {
          path: '/security/servers',
          name: 'servers-manage',
          component: resolve => require(['../pages/security/pages/servers/index'], resolve),
          meta: {
            title: `${i18n.$t('服务管理')}`
          },
          redirect: {
            name: 'servers-master'
          },
          children: [
            {
              path: '/security/servers/master',
              name: 'servers-master',
              component: resolve => require(['../pages/security/pages/servers/pages/master/index'], resolve),
              meta: {
                title: `${i18n.$t('服务管理-Master')}`
              }
            },
            {
              path: '/security/servers/worker',
              name: 'servers-worker',
              component: resolve => require(['../pages/security/pages/servers/pages/worker/index'], resolve),
              meta: {
                title: `${i18n.$t('服务管理-Worker')}`
              }
            }
          ]
        }
      ]
    },
    {
      path: '/user',
      name: 'user',
      component: resolve => require(['../pages/user/index'], resolve),
      meta: {
        title: `${i18n.$t('用户中心')}`
      },
      redirect: {
        name: 'account'
      },
      children: [
        {
          path: '/user/account',
          name: 'account',
          component: resolve => require(['../pages/user/pages/account/index'], resolve),
          meta: {
            title: `${i18n.$t('用户信息')}`
          }
        },
        {
          path: '/user/password',
          name: 'password',
          component: resolve => require(['../pages/user/pages/password/index'], resolve),
          meta: {
            title: `${i18n.$t('修改密码')}`
          }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  let $body = $('body')
  $body.find('.tooltip.fade.top.in').remove()
  if (to.meta.title) {
    document.title = `${to.meta.title} - EasyScheduler`
  }
  next()
})

export default router
