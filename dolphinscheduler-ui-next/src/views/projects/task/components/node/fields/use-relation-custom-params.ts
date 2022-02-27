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
import { computed, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'
import styles from '../index.module.scss'
import type { IJsonItem } from '../types'

export function useRelationCustomParams({
  model,
  children,
  childrenField,
  name
}: {
  model: {
    [field: string]: any
  }
  children: IJsonItem
  childrenField: string
  name: string
}): IJsonItem[] {
  const { t } = useI18n()
  const firstLevelRelationSpan = computed(() =>
    model.dependTaskList.length ? 3 : 0
  )

  watchEffect(() => {
    model.dependTaskList.forEach(
      (item: { [childrenField: string]: [] }, i: number) => {
        if (item[childrenField].length === 0) {
          model.dependTaskList.splice(i, 1)
        }
      }
    )
  })
  return [
    {
      type: 'custom',
      name: t(`project.node.${name}`),
      field: 'relationLabel',
      span: 24,
      class: styles['relaction-label']
    },
    {
      type: 'switch',
      field: 'relation',
      props: {
        round: false,
        'checked-value': 'AND',
        'unchecked-value': 'OR',
        size: 'small'
      },
      slots: {
        checked: () => t('project.node.and'),
        unchecked: () => t('project.node.or')
      },
      span: firstLevelRelationSpan,
      class: styles['relaction-switch']
    },
    {
      type: 'custom-parameters',
      field: 'dependTaskList',
      span: 20,
      children: [
        {
          type: 'switch',
          field: 'relation',
          props: {
            round: false,
            'checked-value': 'AND',
            'unchecked-value': 'OR',
            size: 'small'
          },
          slots: {
            checked: () => t('project.node.and'),
            unchecked: () => t('project.node.or')
          },
          span: 4,
          value: 'AND',
          class: styles['relaction-switch']
        },
        children
      ]
    }
  ]
}
