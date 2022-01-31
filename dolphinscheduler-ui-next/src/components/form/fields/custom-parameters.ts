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

import { h } from 'vue'
import { NFormItem, NSpace, NButton, NIcon } from 'naive-ui'
import { PlusCircleOutlined, DeleteOutlined } from '@vicons/antd'
import getField from './get-field'
import { formatValidate } from '../utils'
import type { IFieldParams, IJsonItem, FormItemRule } from '../types'

interface ICustomParameters extends Omit<IFieldParams, 'rules'> {
  rules?: { [key: string]: FormItemRule }[]
}

export function renderCustomParameters(params: ICustomParameters) {
  const { fields, field, children = [], rules = [] } = params
  let defaultValue: { [field: string]: any } = {}
  let ruleItem: { [key: string]: FormItemRule } = {}
  children.forEach((child) => {
    defaultValue[child.field] = child.value || null
    if (child.validate) ruleItem[child.field] = formatValidate(child.validate)
  })
  const getChild = (item: object, i: number) =>
    children.map((child: IJsonItem) => {
      return h(
        NFormItem,
        {
          showLabel: false,
          path: `${field}[${i}].${child.field}`
        },
        () => getField(child, item)
      )
    })

  const getChildren = () =>
    fields[field].map((item: object, i: number) => {
      return h(NSpace, { ':key': i }, () => [
        ...getChild(item, i),
        h(
          NButton,
          {
            tertiary: true,
            circle: true,
            type: 'error',
            onClick: () => {
              fields[field].splice(i, 1)
              rules.splice(i, 1)
            }
          },
          () => h(NIcon, { size: 24 }, () => h(DeleteOutlined))
        )
      ])
    })

  return h(NSpace, null, () => [
    ...getChildren(),
    h(
      NButton,
      {
        tertiary: true,
        circle: true,
        type: 'info',
        onClick: () => {
          rules.push(ruleItem)
          fields[field].push({ ...defaultValue })
          console.log(rules)
        }
      },
      () => h(NIcon, { size: 24 }, () => h(PlusCircleOutlined))
    )
  ])
}
