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

import { createApp, defineComponent, h, ref } from 'vue'
import {
  NConfigProvider,
  NGlobalStyle,
  NIcon,
  NMenu,
  NSwitch,
  darkTheme
} from 'naive-ui'
import {
  AppstoreOutlined,
  BellOutlined,
  DesktopOutlined,
  FileSearchOutlined,
  LineChartOutlined
} from '@vicons/antd'
import ClusterOverview from './views/monitor/cluster'
import type { ClusterNodeInfo } from './views/monitor/cluster'
import type { QueryRuntimeInstances } from './views/monitor/cluster/running-instances-modal'
import light from './themes/modules/light'
import dark from './themes/modules/dark'
import lightLogo from './assets/images/logo-light.svg'
import darkLogo from './assets/images/logo-dark.svg'
import styles from './cluster-preview.module.scss'

const nodes: ClusterNodeInfo[] = [
  {
    id: 'api-01',
    role: 'API',
    address: '10.20.1.11:12345',
    status: 'normal',
    cpu: 18,
    memory: 34
  },
  {
    id: 'api-02',
    role: 'API',
    address: '10.20.1.12:12345',
    status: 'normal',
    cpu: 21,
    memory: 38
  },
  {
    id: 'master-01',
    role: 'MASTER',
    address: '10.20.2.11:5679',
    status: 'normal',
    cpu: 32,
    memory: 48,
    coordinator: true,
    running: 12
  },
  {
    id: 'master-02',
    role: 'MASTER',
    address: '10.20.2.12:5679',
    status: 'normal',
    cpu: 28,
    memory: 42,
    running: 9
  },
  {
    id: 'master-03',
    role: 'MASTER',
    address: '10.20.2.13:5679',
    status: 'unknown',
    cpu: null,
    memory: null
  },
  {
    id: 'worker-01',
    role: 'WORKER',
    group: 'default',
    address: '10.20.3.11:1235',
    status: 'normal',
    cpu: 38,
    memory: 46,
    threads: 42,
    running: 8
  },
  {
    id: 'worker-02',
    role: 'WORKER',
    group: 'default',
    address: '10.20.3.12:1235',
    status: 'normal',
    cpu: 45,
    memory: 52,
    threads: 50,
    running: 10
  },
  {
    id: 'worker-03',
    role: 'WORKER',
    group: 'default',
    address: '10.20.3.13:1235',
    status: 'normal',
    cpu: 26,
    memory: 38,
    threads: 30,
    running: 6
  },
  {
    id: 'worker-04',
    role: 'WORKER',
    group: 'etl',
    address: '10.20.3.21:1235',
    status: 'normal',
    cpu: 52,
    memory: 63,
    threads: 65,
    running: 13
  },
  {
    id: 'worker-05',
    role: 'WORKER',
    group: 'etl',
    address: '10.20.3.22:1235',
    status: 'busy',
    cpu: 86,
    memory: 78,
    threads: 92,
    running: 23
  },
  {
    id: 'worker-06',
    role: 'WORKER',
    group: 'etl',
    address: '10.20.3.23:1235',
    status: 'normal',
    cpu: 41,
    memory: 49,
    threads: 45,
    running: 9
  },
  {
    id: 'alert-01',
    role: 'ALERT',
    address: '10.20.4.11:50052',
    status: 'normal',
    haRole: 'ACTIVE',
    cpu: 8,
    memory: 25
  },
  {
    id: 'alert-02',
    role: 'ALERT',
    address: '10.20.4.12:50052',
    status: 'normal',
    haRole: 'STANDBY',
    cpu: 6,
    memory: 22
  }
]

const Preview = defineComponent({
  setup() {
    const isDark = ref(false)
    const time = ref('示例快照 14:32:08')
    const queryInstances: QueryRuntimeInstances = async ({ node, kind }) => {
      if (node.status === 'unknown') throw new Error('模拟节点不可达')
      const count =
        kind === 'workflows'
          ? node.running || 0
          : node.role === 'MASTER'
          ? 4
          : node.running || 0
      return Array.from({ length: count }, (_, index) =>
        kind === 'workflows'
          ? {
              id: 8000 + index,
              name: `${node.id}_workflow_${String(index + 1).padStart(2, '0')}`,
              projectCode: 10001,
              workflowDefinitionCode: 20001,
              state: 'RUNNING_EXECUTION',
              startTime: '2026-11-27 14:20:00',
              runTimes: 1
            }
          : {
              id: 9000 + index,
              name: `${node.id}_task_${String(index + 1).padStart(2, '0')}`,
              taskType: index % 2 ? 'SQL' : 'SHELL',
              projectCode: 10001,
              workflowInstanceId: 8000 + index,
              workflowInstanceName: `warehouse_workflow_${index + 1}`,
              startTime: new Date('2026-11-27T14:20:00+08:00').getTime()
            }
      )
    }
    const icon = (component: typeof DesktopOutlined) => () =>
      h(NIcon, null, { default: () => h(component) })
    const menu = [
      {
        label: '服务管理',
        key: 'services',
        children: [
          { label: '集群总览', key: 'cluster', icon: icon(AppstoreOutlined) },
          {
            label: 'Alert Server',
            key: 'alert',
            icon: icon(BellOutlined),
            disabled: true
          }
        ]
      },
      {
        label: '统计管理',
        key: 'statistics',
        children: [
          {
            label: '统计',
            key: 'stats',
            icon: icon(LineChartOutlined),
            disabled: true
          },
          {
            label: '审计日志',
            key: 'audit',
            icon: icon(FileSearchOutlined),
            disabled: true
          }
        ]
      }
    ]
    return () => (
      <NConfigProvider
        theme={isDark.value ? darkTheme : null}
        themeOverrides={isDark.value ? dark : light}
      >
        <NGlobalStyle />
        <div class={[styles.shell, isDark.value && styles.dark]}>
          <header class={styles.header}>
            <img
              src={isDark.value ? darkLogo : lightLogo}
              alt='DolphinScheduler'
            />
            <nav aria-label='主导航'>
              <span>首页</span>
              <span>项目管理</span>
              <span>资源中心</span>
              <span>数据源中心</span>
              <span class={styles.active}>监控中心</span>
              <span>安全中心</span>
            </nav>
            <div class={styles.settings}>
              <span>深色</span>
              <NSwitch
                size='small'
                value={isDark.value}
                onUpdateValue={(value) => (isDark.value = value)}
              />
              <span>admin</span>
            </div>
          </header>
          <div class={styles.layout}>
            <aside class={styles.sidebar}>
              <div class={styles.sidebarTitle}>
                <NIcon component={DesktopOutlined} />
                监控中心
              </div>
              <NMenu
                value='cluster'
                options={menu}
                defaultExpandedKeys={['services', 'statistics']}
                indent={18}
              />
            </aside>
            <main class={styles.main}>
              <div class={styles.breadcrumb}>
                监控中心 <span>/</span> 服务管理 <span>/</span> 集群总览
              </div>
              <ClusterOverview
                nodes={nodes}
                snapshotTime={time.value}
                preview
                canQueryExecutors
                queryInstances={queryInstances}
                onRefresh={() =>
                  (time.value = `模拟刷新 ${new Date().toLocaleTimeString(
                    'zh-CN',
                    { hour12: false }
                  )}`)
                }
              />
            </main>
          </div>
        </div>
      </NConfigProvider>
    )
  }
})

createApp(Preview).mount('#app')
