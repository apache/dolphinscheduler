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

import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { uniqBy } from 'lodash'
import {
  querySimpleList,
  queryProcessDefinitionByCode
} from '@/service/modules/process-definition'
import type { IJsonItem } from '../types'

export function useChildNode({
  model,
  projectCode,
  from,
  processName,
  code
}: {
  model: { [field: string]: any }
  projectCode: number
  from?: number
  processName?: number
  code?: number
}): IJsonItem {
  const { t } = useI18n()

  const options = ref([] as { label: string; value: string }[])
  const loading = ref(false)

  const getProcessList = async () => {
    if (loading.value) return
    loading.value = true
    const res = await querySimpleList(projectCode)
    options.value = res.map((option: { name: string; code: number }) => ({
      label: option.name,
      value: option.code
    }))
    loading.value = false
  }
  const getProcessListByCode = async (processCode: number) => {
    if (!processCode) return
    const res = await queryProcessDefinitionByCode(processCode, projectCode)
    getTaskOptions(res)
  }
  const getTaskOptions = (processDefinition: {
    processTaskRelationList: []
    taskDefinitionList: []
  }) => {
    const { processTaskRelationList = [], taskDefinitionList = [] } =
      processDefinition

    const preTaskOptions: { code: number; name: string }[] = []
    const tasks: { [field: number]: string } = {}
    taskDefinitionList.forEach(
      (task: { code: number; taskType: string; name: string }) => {
        tasks[task.code] = task.name
        if (task.code === code) return
        if (
          task.taskType === 'CONDITIONS' &&
          processTaskRelationList.filter(
            (relation: { preTaskCode: number }) =>
              relation.preTaskCode === task.code
          ).length >= 2
        ) {
          return
        }
        preTaskOptions.push({
          code: task.code,
          name: task.name
        })
      }
    )
    model.preTaskOptions = uniqBy(preTaskOptions, 'code')

    if (!code) return
    const preTasks: number[] = []
    const postTaskOptions: { code: number; name: string }[] = []
    processTaskRelationList.forEach(
      (relation: { preTaskCode: number; postTaskCode: number }) => {
        if (relation.preTaskCode === code) {
          postTaskOptions.push({
            code: relation.postTaskCode,
            name: tasks[relation.postTaskCode]
          })
        }
        if (relation.postTaskCode === code && relation.preTaskCode !== 0) {
          preTasks.push(relation.preTaskCode)
        }
      }
    )
    model.preTasks = preTasks
    model.postTaskOptions = postTaskOptions
  }

  const onChange = (code: number) => {
    getProcessListByCode(code)
  }

  onMounted(() => {
    if (from === 1 && processName) {
      getProcessListByCode(processName)
    }
    getProcessList()
  })

  return {
    type: 'select',
    field: 'processDefinitionCode',
    span: 24,
    name: t('project.node.child_node'),
    props: {
      loading: loading,
      'on-update:value': onChange
    },
    options: options
  }
}
