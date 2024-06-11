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
import { ref, watch, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTaskNodeStore } from '@/store/project/task-node'
import { queryProcessDefinitionByCode } from '@/service/modules/process-definition'
import { findIndex } from 'lodash'
import type { IJsonItem } from '../types'

export function useSwitch(
  model: { [field: string]: any },
  projectCode: number
): IJsonItem[] {
  const { t } = useI18n()
  const taskStore = useTaskNodeStore()
  const branchFlowOptions = ref(taskStore.postTaskOptions as any)
  const loading = ref(false)
  const getOtherTaskDefinitionList = async () => {
    if (loading.value) return
    loading.value = true
    branchFlowOptions.value = []
    const res = await queryProcessDefinitionByCode(
      model.processName,
      projectCode
    )
    res?.taskDefinitionList.forEach((item: any) => {
      if (item.code != model.code) {
        branchFlowOptions.value.push({ label: item.name, value: item.code })
      }
    })
    loading.value = false
    clearUselessNode(branchFlowOptions.value)
  }

  const clearUselessNode = (options: { value: number }[]) => {
    if (!options || !options.length) {
      model.nextNode = null
      model.dependTaskList?.forEach((task: { nextNode: number | null }) => {
        task.nextNode = null
      })
      return
    }
    if (
      findIndex(
        branchFlowOptions.value,
        (option: { value: number }) => option.value == model.nextNode
      ) === -1
    ) {
      model.nextNode = null
    }
    model.dependTaskList?.forEach((task: { nextNode: number | null }) => {
      if (
        findIndex(
          branchFlowOptions.value,
          (option: { value: number }) => option.value == task.nextNode
        ) === -1
      ) {
        task.nextNode = null
      }
    })
  }

  watch(
    () => [model.processName, model.nextCode],
    () => {
      if (model.processName) {
        getOtherTaskDefinitionList()
      }
    }
  )

  onMounted(async () => {
    await nextTick()
    clearUselessNode(branchFlowOptions.value)
  })

  return [
    {
      type: 'custom-parameters',
      field: 'dependTaskList',
      name: t('project.node.switch_condition'),
      children: [
        {
          type: 'input',
          field: 'condition',
          span: 24,
          props: {
            loading: loading,
            type: 'textarea',
            autosize: { minRows: 2 }
          }
        },
        {
          type: 'select',
          field: 'nextNode',
          span: 22,
          name: t('project.node.switch_branch_flow'),
          options: branchFlowOptions
        }
      ]
    },
    {
      type: 'select',
      field: 'nextNode',
      span: 24,
      name: t('project.node.switch_branch_flow_default'),
      props: {
        loading: loading
      },
      options: branchFlowOptions
    }
  ]
}
