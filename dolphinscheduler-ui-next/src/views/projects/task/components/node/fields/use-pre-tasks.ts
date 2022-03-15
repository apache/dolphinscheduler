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

import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { uniqBy } from 'lodash'
import type { IJsonItem } from '../types'

export function usePreTasks(
  model: { [field: string]: any },
  code?: number
): IJsonItem {
  const { t } = useI18n()

  const options = ref([] as { value: number; label: string }[])

  const getOptions = (
    options: { code: number; name: string }[]
  ): { value: number; label: string }[] => {
    if (!options?.length) return []
    return options.map((task: { code: number; name: string }) => ({
      value: task.code,
      label: task.name
    }))
  }

  watch(
    () => model.definition,
    (value) => {
      if (!value) return
      const {
        preTaskOptions,
        preTasks = [],
        postTaskOptions = []
      } = getTaskOptions(value, code)
      model.preTasks = preTasks
      model.postTaskOptions = postTaskOptions
      options.value = getOptions(preTaskOptions)
    }
  )

  return {
    type: 'select',
    field: 'preTasks',
    span: 24,
    class: 'pre-tasks-model',
    name: t('project.node.pre_tasks'),
    props: {
      multiple: true,
      filterable: true
    },
    options
  }
}

function getTaskOptions(
  processDefinition: {
    processTaskRelationList: []
    taskDefinitionList: []
  },
  code?: number
): {
  preTaskOptions: { code: number; name: string }[]
  preTasks?: number[]
  postTaskOptions?: { code: number; name: string }[]
} {
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
  if (!code)
    return {
      preTaskOptions: uniqBy(preTaskOptions, 'code')
    }
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
  return {
    preTaskOptions: uniqBy(preTaskOptions, 'code'),
    preTasks,
    postTaskOptions
  }
}
