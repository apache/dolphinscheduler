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
import { TASK_TYPES_MAP } from '@/store/project/task-type'
import type { IJsonItem } from '../types'

export function useTaskType(
  model: { [field: string]: any },
  readonly?: boolean
): IJsonItem {
  const { t } = useI18n()
  const disabledTaskType = ['CONDITIONS', 'SWITCH']

  const options = Object.keys(TASK_TYPES_MAP).map((option: string) => ({
    label: option,
    value: option,
    disabled: disabledTaskType.includes(option)
  }))
  return {
    type: 'select',
    field: 'taskType',
    span: 24,
    name: t('project.node.task_type'),
    props: {
      disabled: readonly || ['CONDITIONS', 'SWITCH'].includes(model.taskType),
      filterable: true
    },
    options: options,
    validate: {
      trigger: ['input', 'blur'],
      required: true,
      message: t('project.node.task_type_tips')
    },
    value: model.taskType ? model.taskType : 'SHELL'
  }
}
