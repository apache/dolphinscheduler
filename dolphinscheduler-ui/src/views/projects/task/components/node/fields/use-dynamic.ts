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

import type { IJsonItem } from '../types'
import { useI18n } from 'vue-i18n'

export function useDynamic(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'input-number',
      field: 'maxNumOfSubWorkflowInstances',
      span: 12,
      name: t('project.node.max_num_of_sub_workflow_instances'),
      validate: {
        required: true
      }
    },
    {
      type: 'input-number',
      field: 'degreeOfParallelism',
      span: 12,
      name: t('project.node.parallelism'),
      validate: {
        required: true
      }
    },
    {
      type: 'custom-parameters',
      field: 'listParameters',
      name: t('project.node.params_value'),
      span: 24,
      children: [
        {
          type: 'input',
          field: 'name',
          span: 8,
          props: {
            placeholder: t('project.node.dynamic_name_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.dynamic_name_tips'))
              }

              const sameItems = model['listParameters'].filter(
                (item: { name: string }) => item.name === value
              )

              if (sameItems.length > 1) {
                return new Error(t('project.node.prop_repeat'))
              }
            }
          }
        },
        {
          type: 'input',
          field: 'value',
          span: 8,
          props: {
            placeholder: t('project.node.dynamic_value_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.dynamic_value_tips'))
              }
            }
          }
        },
        {
          type: 'input',
          field: 'separator',
          span: 4,
          props: {
            placeholder: t('project.node.dynamic_separator_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.dynamic_separator_tips'))
              }
            }
          }
        }
      ]
    },
    {
      type: 'input',
      field: 'filterCondition',
      span: 24,
      name: t('project.node.filter_condition')
    }
  ]
}
