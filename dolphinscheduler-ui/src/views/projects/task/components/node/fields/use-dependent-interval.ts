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
import { IDependpendItem, IDependTask } from "../types";

export function useDependentInterval(
  {
    model,
    field,
    name = 'custom_parameters',
    span = 24
  }: {
    model: { [field: string]: any }
    field: string
    name?: string
    span?: Ref | number
  }): IJsonItem[] {
  const { t } = useI18n()

  const isLastQuoteKey = (i: number) => {
    const usedKeys = model.dependTaskList.reduce(
      (acc: Set<string>, item: IDependTask) => {
        item.dependItemList?.forEach(async (dependItem: IDependpendItem) => {
          if (dependItem.cycle === 'custom' && dependItem.dateValue) {
            acc.add(dependItem.dateValue)
          }
        })
        return acc
      },
      new Set<string>()
    )
    const key: string | null | undefined = model.localParams?.[i]?.prop
    const firstKeyIndex = model.localParams?.findIndex(
      ({ prop }: any) => prop === key)
    return firstKeyIndex === i && usedKeys.has(key)
  }


  return [
    {
      type: 'custom-parameters',
      field: field,
      name: t(`project.node.${name}`),
      class: 'btn-custom-parameters',
      span,
      children: [
        (i = 0) => ({
          type: 'input',
          field: 'prop',
          span: 6,
          class: 'input-param-key',
          props: {
            placeholder: t('project.node.prop_tips'),
            maxLength: 256,
            disabled: isLastQuoteKey(i)
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
        }),
        {
          type: 'input',
          field: 'startDate',
          span: 8,
          class: 'input-param-start-date',
          props: {
            placeholder: t('project.node.start_date_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.prop_tips'))
              }
            }
          }
        },
        {
          type: 'input',
          field: 'endDate',
          span: 8,
          class: 'input-param-end-date',
          props: {
            placeholder: t('project.node.end_date_tips'),
            maxLength: 256
          }
        }
      ]
    }
  ]
}
