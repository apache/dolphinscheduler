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

import { useI18n } from 'vue-i18n'
import { useTaskNodeStore } from '@/store/project/task-node'
import { useRelationCustomParams, useTimeoutAlarm } from '.'
import type { IJsonItem } from '../types'

export function useConditions(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const taskStore = useTaskNodeStore()

  const preTaskOptions = taskStore.preTaskOptions.filter((option) =>
    taskStore.preTasks.includes(Number(option.value))
  )
  const stateOptions = [
    { label: t('project.node.success'), value: 'success' },
    { label: t('project.node.failed'), value: 'failed' }
  ]

  return [
    {
      type: 'select',
      field: 'successNode',
      name: t('project.node.state'),
      span: 12,
      props: {
        disabled: true
      },
      options: stateOptions
    },
    {
      type: 'select',
      field: 'successBranch',
      name: t('project.node.branch_flow'),
      span: 12,
      props: {
        clearable: true
      },
      validate: {
        trigger: ['input', 'blur'],
        validator: (unuse, value) => {
          if (value && value === model.failedBranch) {
            return new Error(t('project.node.branch_tips'))
          }
        }
      },
      options: taskStore.getPostTaskOptions
    },
    {
      type: 'select',
      field: 'failedNode',
      name: t('project.node.state'),
      span: 12,
      props: {
        disabled: true
      },
      options: stateOptions
    },
    {
      type: 'select',
      field: 'failedBranch',
      name: t('project.node.branch_flow'),
      span: 12,
      props: {
        clearable: true
      },
      validate: {
        trigger: ['input', 'blur'],
        validator: (unuse, value) => {
          if (value && value === model.successBranch) {
            return new Error(t('project.node.branch_tips'))
          }
        }
      },
      options: taskStore.getPostTaskOptions
    },
    ...useTimeoutAlarm(model),
    ...useRelationCustomParams({
      model,
      children: {
        type: 'custom-parameters',
        field: 'dependItemList',
        span: 18,
        children: [
          {
            type: 'select',
            field: 'depTaskCode',
            span: 10,
            options: preTaskOptions
          },
          {
            type: 'select',
            field: 'status',
            span: 10,
            options: [
              {
                value: 'SUCCESS',
                label: t('project.node.success')
              },
              {
                value: 'FAILURE',
                label: t('project.node.failed')
              }
            ]
          }
        ]
      },
      childrenField: 'dependItemList',
      name: 'custom_parameters'
    })
  ]
}
