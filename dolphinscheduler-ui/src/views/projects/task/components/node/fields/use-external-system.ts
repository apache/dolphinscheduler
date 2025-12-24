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
import { ref, onMounted, nextTick, Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryDataSourceList, queryExternalSystemTasks } from '@/service/modules/data-source'
import { find } from 'lodash'
import type { IJsonItem } from '../types'
import type { TypeReq } from '@/service/modules/data-source/types'

export function useExternalSystem(
  model: { [field: string]: any },
  params: {
    externalSystemField?: string
    taskField?: string
    span?: Ref | number
  } = {}
): IJsonItem[] {
  const { t } = useI18n()

  const datasourceOptions = ref([] as { label: string; value: string }[])
  const taskOptions = ref([] as { label: string; value: string }[])

  const getDataSources = async () => {
    try {
      const parameters = {
        type: 'THIRDPARTY_SYSTEM_CONNECTOR'
      } as TypeReq
      
      const res = await queryDataSourceList(parameters)
      datasourceOptions.value = res.map((item: any) => ({
        label: item.name,
        value: String(item.id)
      }))
    } catch (error) {
      // Error handling is done by the calling function
    }
  }

  const refreshTasks = async () => {
    const datasourceId = model[params.externalSystemField || 'datasource']
    if (!datasourceId) return

    try {
      const res = await queryExternalSystemTasks(datasourceId)
      taskOptions.value = res.map((item: any) => ({
        label: item.name,
        value: String(item.id)
      }))
    } catch (error) {
      // Error handling is done by the calling function
    }

    const taskField = params.taskField || 'task'
    if (!taskOptions.value.length && model[taskField]) model[taskField] = null
    if (taskOptions.value.length && model[taskField]) {
      const item = find(taskOptions.value, { value: model[taskField] })
      if (!item) {
        model[taskField] = null
      }
    }
  }

  const onChange = () => {
    taskOptions.value = []
    const taskField = params.taskField || 'externalTaskId'
    model[taskField] = null
    model.externalTaskName = ''
    refreshTasks()
  }

  const onTaskChange = (value: string) => {
    if (value) {
      const taskItem = taskOptions.value.find(item => item.value === value)
      if (taskItem) {
        model.externalTaskName = taskItem.label // Set the name based on the selected task
      }
    } else {
      model.externalTaskName = ''
    }
  }

  onMounted(async () => {
    await getDataSources()
    await nextTick()
    refreshTasks()
  })

  return [
    {
      type: 'select',
      field: params.externalSystemField || 'datasource',
      span: params.span || 24,
      name: t('project.node.datasource_instances'),
      props: { 'on-update:value': onChange },
      options: datasourceOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value) {
            return new Error(t('project.node.datasource_instances_required'))
          }
        }
      }
    },
    {
      type: 'select',
      field: params.taskField || 'externalTaskId',
      span: params.span || 24,
      name: t('project.node.external_system_tasks'),
      props: { 'on-update:value': onTaskChange },
      options: taskOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value) {
            return new Error(t('thirdparty_api_source.external_system_task_required'))
          }
        }
      }
    }
  ]
}