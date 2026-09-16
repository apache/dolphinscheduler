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

import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  queryWorkflowInstanceById,
  queryTaskListByWorkflowId,
  viewGanttTree
} from '@/service/modules/workflow-instances'
import { buildGanttModel, isWorkflowActive } from './model'
import type { IGanttRes, TaskInstance, WorkflowInstance } from './type'

export function useGantt(identity: () => [number, number]) {
  const workflow = ref<WorkflowInstance>()
  const tasks = ref<TaskInstance[]>([])
  const gantt = ref<IGanttRes>()
  const now = ref(Date.now())
  const loading = ref(false)
  const error = ref(false)
  const autoRefresh = ref(true)
  const updatedAt = ref<number>()
  let generation = 0
  let disposed = false
  let timer: ReturnType<typeof setInterval>
  let clock: ReturnType<typeof setInterval>

  const refresh = async () => {
    if (loading.value || disposed) return
    const requestGeneration = generation
    const [id, projectCode] = identity()
    loading.value = true
    try {
      const [instance, taskResponse, ganttResponse] = await Promise.all([
        queryWorkflowInstanceById(id, projectCode),
        queryTaskListByWorkflowId(id, projectCode),
        viewGanttTree(id, projectCode)
      ])
      if (disposed || requestGeneration !== generation) return
      workflow.value = {
        ...instance,
        state: taskResponse.workflowInstanceState || instance.state
      }
      tasks.value = taskResponse.taskList || []
      gantt.value = ganttResponse
      updatedAt.value = Date.now()
      now.value = updatedAt.value
      error.value = false
    } catch {
      if (!disposed && requestGeneration === generation) error.value = true
    } finally {
      if (!disposed && requestGeneration === generation) loading.value = false
    }
  }
  watch(
    identity,
    () => {
      generation++
      workflow.value = undefined
      tasks.value = []
      gantt.value = undefined
      updatedAt.value = undefined
      loading.value = false
      error.value = false
      void refresh()
    },
    { immediate: true }
  )
  onMounted(() => {
    timer = setInterval(() => {
      if (
        autoRefresh.value &&
        !document.hidden &&
        (!workflow.value || isWorkflowActive(workflow.value.state))
      )
        void refresh()
    }, 5000)
    clock = setInterval(() => {
      if (
        autoRefresh.value &&
        !error.value &&
        workflow.value &&
        isWorkflowActive(workflow.value.state)
      )
        now.value = Date.now()
    }, 1000)
  })
  onBeforeUnmount(() => {
    disposed = true
    generation++
    clearInterval(timer)
    clearInterval(clock)
  })
  const model = computed(() =>
    buildGanttModel(
      workflow.value || { state: 'SUBMITTED_SUCCESS' },
      tasks.value,
      gantt.value,
      now.value
    )
  )
  return { workflow, model, loading, error, autoRefresh, updatedAt, refresh }
}
