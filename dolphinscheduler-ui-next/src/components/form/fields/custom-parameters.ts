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

import { defineComponent, h, renderSlot } from 'vue'
import { useFormItem } from 'naive-ui/es/_mixins'
import { NFormItemGi, NSpace, NButton, NIcon, NGrid } from 'naive-ui'
import { PlusCircleOutlined, DeleteOutlined } from '@vicons/antd'
import { omit } from 'lodash'
import getField from './get-field'
import { formatValidate } from '../utils'
import type { IJsonItem, FormItemRule } from '../types'

const CustomParameters = defineComponent({
  name: 'CustomParameters',
  emits: ['add'],
  setup(props, ctx) {
    const formItem = useFormItem({})

    const onAdd = () => void ctx.emit('add')

    return { onAdd, disabled: formItem.mergedDisabledRef }
  },
  render() {
    const { disabled, $slots, onAdd } = this
    return h(NSpace, null, {
      default: () => {
        return [
          renderSlot($slots, 'default', { disabled }),
          h(
            NButton,
            {
              tertiary: true,
              circle: true,
              type: 'info',
              disabled,
              onClick: onAdd
            },
            () => h(NIcon, { size: 24 }, () => h(PlusCircleOutlined))
          )
        ]
      }
    })
  }
})

export function renderCustomParameters(
  item: IJsonItem,
  fields: { [field: string]: any },
  rules: { [key: string]: FormItemRule }[]
) {
  const { field, children = [] } = item
  let defaultValue: { [field: string]: any } = {}
  let ruleItem: { [key: string]: FormItemRule } = {}

  children.forEach((child) => {
    defaultValue[child.field] = child.value || null
    if (child.validate) ruleItem[child.field] = formatValidate(child.validate)
  })
  const getChild = (item: object, i: number) =>
    children.map((child: IJsonItem) => {
      return h(
        NFormItemGi,
        {
          showLabel: false,
          ...omit(item, ['field', 'type', 'props', 'options']),
          path: `${field}[${i}].${child.field}`,
          span: 6
        },
        () => getField(child, item)
      )
    })
  const getChildren = ({ disabled }: { disabled: boolean }) =>
    fields[field].map((item: object, i: number) => {
      return h(NGrid, { xGap: 10 }, () => [
        ...getChild(item, i),
        h(
          NButton,
          {
            tertiary: true,
            circle: true,
            type: 'error',
            disabled,
            onClick: () => {
              fields[field].splice(i, 1)
              rules.splice(i, 1)
            }
          },
          () => h(NIcon, { size: 24 }, () => h(DeleteOutlined))
        )
      ])
    })

  return h(
    CustomParameters,
    {
      onAdd: () => {
        rules.push(ruleItem)
        fields[field].push({ ...defaultValue })
      }
    },
    {
      default: getChildren
    }
  )
}
