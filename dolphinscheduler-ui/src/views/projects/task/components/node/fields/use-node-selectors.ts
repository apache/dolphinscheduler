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
import { Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'

export function useNodeSelectors({
  model,
  field,
  name,
  span = 24
}: {
  model: { [field: string]: any }
  field: string
  name?: string
  span?: Ref | number
}): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'custom-parameters',
      field: field,
      name: t(`project.node.${name}`),
      class: 'btn-custom-parameters',
      span,
      children: [
        {
          type: 'input',
          field: 'key',
          span: 8,
          class: 'node-selector-label-name',
          props: {
            placeholder: t('project.node.expression_name_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.expression_name_tips'))
              }

              const sameItems = model[field].filter(
                (item: { label: string }) => item.label === value
              )

              if (sameItems.length > 1) {
                return new Error(t('project.node.label_repeat'))
              }
            }
          }
        },
        {
          type: 'select',
          field: 'operator',
          span: 4,
          options: OPERATOR_LIST,
          value: 'In'
        },
        {
          type: 'input',
          field: 'values',
          span: 10,
          class: 'node-selector-label-value',
          props: {
            placeholder: t('project.node.expression_value_tips'),
            maxLength: 256,
            disabled: false
          }
        }
      ]
    }
  ]
}

export const OPERATOR_LIST = [
  {
    value: 'In',
    label: 'In'
  },
  {
    value: 'NotIn',
    label: 'NotIn'
  },
  {
    value: 'Exists',
    label: 'Exists'
  },
  {
    value: 'DoesNotExist',
    label: 'DoesNotExist'
  },
  {
    value: 'Gt',
    label: 'Gt'
  },
  {
    value: 'Lt',
    label: 'Lt'
  }
]
