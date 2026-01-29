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

export function useCustomParams({
  model,
  field,
  isSimple,
  name = 'custom_parameters',
  span = 24
}: {
  model: { [field: string]: any }
  field: string
  isSimple: boolean
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
          field: 'prop',
          span: 6,
          class: 'input-param-key',
          props: {
            placeholder: t('project.node.prop_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.prop_tips'))
              }

              const sameItems = model[field].filter(
                (item: { prop: string }) => item.prop === value
              )

              if (sameItems.length > 1) {
                return new Error(t('project.node.prop_repeat'))
              }
            }
          }
        },
        {
          type: 'select',
          field: 'direct',
          span: 4,
          options: DIRECT_LIST,
          value: 'IN',
          props: {
            disabled: isSimple
          }
        },
        {
          type: 'select',
          field: 'type',
          span: 6,
          options: TYPE_LIST,
          value: 'VARCHAR',
          props: {
            disabled: isSimple
          }
        },
        {
          type: 'input',
          field: 'value',
          span: 6,
          class: 'input-param-value',
          props: {
            placeholder: t('project.node.value_tips'),
            maxLength: 256
          }
        }
      ]
    }
  ]
}

export const TYPE_LIST = [
  {
    value: 'VARCHAR',
    label: 'VARCHAR'
  },
  {
    value: 'INTEGER',
    label: 'INTEGER'
  },
  {
    value: 'LONG',
    label: 'LONG'
  },
  {
    value: 'FLOAT',
    label: 'FLOAT'
  },
  {
    value: 'DOUBLE',
    label: 'DOUBLE'
  },
  {
    value: 'DATE',
    label: 'DATE'
  },
  {
    value: 'TIME',
    label: 'TIME'
  },
  {
    value: 'TIMESTAMP',
    label: 'TIMESTAMP'
  },
  {
    value: 'BOOLEAN',
    label: 'BOOLEAN'
  },
  {
    value: 'LIST',
    label: 'LIST'
  },
  {
    value: 'FILE',
    label: 'FILE'
  }
]

export const DIRECT_LIST = [
  {
    value: 'IN',
    label: 'IN'
  },
  {
    value: 'OUT',
    label: 'OUT'
  }
]
