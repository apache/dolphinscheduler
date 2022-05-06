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

import { ref, watch, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryTaskGroupListPagingByProjectCode } from '@/service/modules/task-group'
import type { IJsonItem } from '../types'

export function useTaskGroup(
  model: { [field: string]: any },
  projectCode: number
): IJsonItem[] {
  const { t } = useI18n()

  const options = ref([])
  const loading = ref(false)
  const priorityDisabled = computed(() => !model.taskGroupId)

  const getTaskGroupList = async () => {
    if (loading.value) return
    loading.value = true
    const { totalList = [] } = await queryTaskGroupListPagingByProjectCode({
      pageNo: 1,
      pageSize: 2147483647,
      projectCode
    })
    options.value = totalList.map((item: { id: string; name: string }) => ({
      label: item.name,
      value: item.id
    }))
    loading.value = false
  }

  onMounted(() => {
    getTaskGroupList()
  })

  watch(
    () => model.taskGroupId,
    (taskGroupId) => {
      if (!taskGroupId) {
        model.taskGroupId = null
        model.taskGroupPriority = null
      }
    }
  )

  return [
    {
      type: 'select',
      field: 'taskGroupId',
      span: 12,
      name: t('project.node.task_group_name'),
      props: {
        loading,
        clearable: true
      },
      options
    },
    {
      type: 'input-number',
      field: 'taskGroupPriority',
      name: t('project.node.task_group_queue_priority'),
      props: {
        max: Math.pow(10, 60) - 1,
        disabled: priorityDisabled
      },
      span: 12
    }
  ]
}
