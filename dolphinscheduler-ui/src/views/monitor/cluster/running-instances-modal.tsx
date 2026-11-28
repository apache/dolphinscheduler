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
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { NAlert, NButton, NDataTable, NModal, NSpin } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import type {
  TaskExecutor,
  WorkflowExecutor
} from '@/service/modules/monitor/types'
import type { ClusterNodeInfo } from './cluster-overview'

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
    const { t } = useI18n()
    const router = useRouter()
    const instanceLink = (projectCode: number, id: number, name: string) => {
      const path = `/projects/${projectCode}/workflow/instances/${id}`
      return (
        <a
          href={router.resolve(path).href}
          onClick={(event: MouseEvent) => {
            if (
              !event.ctrlKey &&
              !event.metaKey &&
              !event.shiftKey &&
              !event.altKey &&
              event.button === 0
            ) {
              event.preventDefault()
              router.push(path)
            }
          }}
        >
          {name}
        </a>
      )
    }
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
              title: t('monitor.cluster.workflow_name'),
              key: 'name',
              render: (row) => instanceLink(row.projectCode, row.id, row.name)
            },
            { title: t('monitor.cluster.workflow_state'), key: 'state' },
            { title: t('monitor.cluster.start_time'), key: 'startTime' },
            {
              title: t('monitor.cluster.run_times'),
              key: 'runTimes',
              width: 90
            }
          ]
        : [
            { title: t('monitor.cluster.task_name'), key: 'name' },
            {
              title: t('monitor.cluster.task_type'),
              key: 'taskType',
              width: 90
            },
            {
              title: t('monitor.cluster.workflow_name'),
              key: 'workflowInstanceName',
              render: (row) => {
                const task = row as TaskExecutor
                return instanceLink(
                  task.projectCode,
                  task.workflowInstanceId,
                  task.workflowInstanceName || String(task.workflowInstanceId)
                )
              }
            },
            {
              title: t('monitor.cluster.start_time'),
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
          title={`${target?.node.name || ''} · ${t(
            workflows
              ? 'monitor.cluster.running_workflows'
              : 'monitor.cluster.running_tasks'
          )}`}
          style={{ width: 'min(960px, calc(100vw - 32px))' }}
          onUpdateShow={(show) => {
            if (!show) emit('close')
          }}
        >
          {failed.value ? (
            <NAlert type='error' title={t('monitor.cluster.query_failed')}>
              {t('monitor.cluster.query_failed_description')}
              <NButton text type='primary' onClick={load}>
                {t('monitor.cluster.retry')}
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
