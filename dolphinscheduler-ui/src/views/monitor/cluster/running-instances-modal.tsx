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

import {
  defineComponent,
  onBeforeUnmount,
  ref,
  watch,
  type PropType
} from 'vue'
import { NAlert, NButton, NDataTable, NModal, NSpin } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import type {
  TaskExecutor,
  WorkflowExecutor
} from '@/service/modules/monitor/types'
import type { ClusterNodeInfo } from './index'

export interface RuntimeTarget {
  node: ClusterNodeInfo
  kind: 'workflows' | 'tasks'
}

export type RuntimeInstance = WorkflowExecutor | TaskExecutor
export type QueryRuntimeInstances = (
  target: RuntimeTarget
) => Promise<RuntimeInstance[]>

export default defineComponent({
  name: 'ClusterRunningInstancesModal',
  props: {
    target: { type: Object as PropType<RuntimeTarget | null>, default: null },
    queryInstances: {
      type: Function as PropType<QueryRuntimeInstances>,
      required: true
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const rows = ref<RuntimeInstance[]>([])
    const loading = ref(false)
    const failed = ref(false)
    let requestId = 0

    const load = async () => {
      const target = props.target
      const request = ++requestId
      rows.value = []
      failed.value = false
      if (!target) {
        loading.value = false
        return
      }
      loading.value = true
      try {
        const result = await props.queryInstances(target)
        if (request === requestId) rows.value = result
      } catch {
        if (request === requestId) failed.value = true
      } finally {
        if (request === requestId) loading.value = false
      }
    }
    watch(() => props.target, load, { immediate: true })
    onBeforeUnmount(() => {
      requestId++
    })

    return () => {
      const target = props.target
      const workflows = target?.kind === 'workflows'
      const columns: DataTableColumns<RuntimeInstance> = workflows
        ? [
            {
              title: '工作流实例',
              key: 'name',
              render: (row) => (
                <a
                  href={`/projects/${row.projectCode}/workflow/instances/${row.id}`}
                >
                  {row.name}
                </a>
              )
            },
            { title: '状态', key: 'state' },
            { title: '开始时间', key: 'startTime' },
            { title: '运行次数', key: 'runTimes', width: 90 }
          ]
        : [
            { title: '任务名称', key: 'name' },
            { title: '任务类型', key: 'taskType', width: 90 },
            {
              title: '工作流实例',
              key: 'workflowInstanceName',
              render: (row) => {
                const task = row as TaskExecutor
                return (
                  <a
                    href={`/projects/${task.projectCode}/workflow/instances/${task.workflowInstanceId}`}
                  >
                    {task.workflowInstanceName || task.workflowInstanceId}
                  </a>
                )
              }
            },
            {
              title: '开始时间',
              key: 'startTime',
              render: (row) => {
                const startTime = (row as TaskExecutor).startTime
                return startTime ? new Date(startTime).toLocaleString() : '—'
              }
            }
          ]
      return (
        <NModal
          show={target !== null}
          preset='card'
          title={`${target?.node.id || ''} · ${
            workflows ? '运行中的工作流' : '运行中的任务'
          }`}
          style={{ width: 'min(960px, calc(100vw - 32px))' }}
          onUpdateShow={(show) => {
            if (!show) emit('close')
          }}
        >
          {failed.value ? (
            <NAlert type='error' title='运行实例查询失败'>
              无法获取该节点的运行实例，请重试。
              <NButton text type='primary' onClick={load}>
                重新加载
              </NButton>
            </NAlert>
          ) : (
            <NSpin show={loading.value}>
              <NDataTable
                columns={columns}
                data={rows.value}
                rowKey={(row: RuntimeInstance) => row.id}
                pagination={{ pageSize: 8 }}
                scrollX={640}
                bordered={false}
                size='small'
              />
            </NSpin>
          )}
        </NModal>
      )
    }
  }
})
