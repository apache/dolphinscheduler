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

import { defineComponent, ref, watch, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NDataTable, NSpin } from 'naive-ui'
import { useRouter } from 'vue-router'
import Modal from '@/components/modal'
import { queryTaskExecutors } from '@/service/modules/monitor'
import type { PropType } from 'vue'
import type { TaskExecutor } from '@/service/modules/monitor/types'

const props = {
  showModal: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  serverAddress: {
    type: String as PropType<string>,
    default: ''
  }
}

const RunningTasksModal = defineComponent({
  props,
  emits: ['confirmModal'],
  setup(props, ctx) {
    const { t } = useI18n()
    const router = useRouter()
    const loading = ref(false)
    const data = ref<TaskExecutor[]>([])

    const columns = [
      {
        title: '#',
        key: 'index',
        render: (_row: any, index: number) => index + 1,
        width: 60
      },
      { title: t('monitor.worker.task_name'), key: 'name' },
      { title: t('monitor.worker.task_type'), key: 'taskType' },
      {
        title: t('monitor.worker.task_workflow_instance'),
        key: 'workflowInstanceName',
        render: (row: TaskExecutor) => {
          return h(
            'a',
            {
              href: `/projects/${row.projectCode}/workflow/instances/${row.workflowInstanceId}`,
              onClick: (e: MouseEvent) => {
                e.preventDefault()
                router.push({
                  path: `/projects/${row.projectCode}/workflow/instances/${row.workflowInstanceId}`
                })
              },
              style: { color: '#2080f0', cursor: 'pointer' }
            },
            row.workflowInstanceName || row.workflowInstanceId
          )
        }
      },
      { title: t('monitor.worker.task_start_time'), key: 'startTime' }
    ]

    const fetchData = async () => {
      if (!props.serverAddress) return
      loading.value = true
      try {
        const res = await queryTaskExecutors(props.serverAddress)
        data.value = res || []
      } catch (e) {
        data.value = []
      } finally {
        loading.value = false
      }
    }

    watch(
      () => props.showModal,
      (val) => {
        if (val) {
          fetchData()
        }
      }
    )

    const onConfirm = () => {
      ctx.emit('confirmModal')
    }

    return { t, columns, data, loading, onConfirm }
  },
  render() {
    const { t, columns, data, loading, onConfirm } = this

    return (
      <Modal
        title={t('monitor.worker.running_tasks')}
        show={this.showModal}
        cancelShow={false}
        onConfirm={onConfirm}
        style={{ width: '800px' }}
      >
        {{
          default: () => (
            <NSpin show={loading}>
              <NDataTable
                columns={columns}
                data={data}
                striped
                size={'small'}
              />
            </NSpin>
          )
        }}
      </Modal>
    )
  }
})

export default RunningTasksModal
