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

import { defineComponent, h, unref, renderSlot } from 'vue'
import { useFormItem } from 'naive-ui/es/_mixins'
import { NFormItemGi, NSpace, NButton, NGrid, NGridItem } from 'naive-ui'
import { PlusOutlined, DeleteOutlined } from '@vicons/antd'
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
              circle: true,
              size: 'small',
              type: 'info',
              disabled,
              onClick: onAdd
            },
            {
              icon: () => h(PlusOutlined)
            }
          )
        ]
      }
    })
  }
})

const getDefaultValue = (children: IJsonItem[]) => {
  const defaultValue: { [field: string]: any } = {}
  const ruleItem: { [key: string]: FormItemRule[] | FormItemRule } = {}
  const loop = (
    children: IJsonItem[],
    parent: { [field: string]: any },
    ruleParent: { [key: string]: FormItemRule[] | FormItemRule }
  ) => {
    children.forEach((child) => {
      if (Array.isArray(child.children)) {
        const childDefaultValue = {}
        const childRuleItem = {}
        loop(child.children, childDefaultValue, childRuleItem)
        parent[child.field] = [childDefaultValue]
        ruleParent[child.field] = {
          type: 'array',
          fields: childRuleItem
        }
        return
      } else {
        parent[child.field] = child.value || null
        if (child.validate)
          ruleParent[child.field] = formatValidate(child.validate)
      }
    })
  }

  loop(children, defaultValue, ruleItem)
  return {
    defaultValue,
    ruleItem
  }
}

export function renderCustomParameters(
  item: IJsonItem,
  fields: { [field: string]: any },
  rules: { [key: string]: FormItemRule | FormItemRule[] }[]
) {
  const { field, children = [] } = item
  const { defaultValue, ruleItem } = getDefaultValue(children)
  rules.push(ruleItem)
  const getChild = (item: object, i: number, disabled: boolean) =>
    children.map((child: IJsonItem) => {
      return h(
        NFormItemGi,
        {
          showLabel: false,
          path: `${field}[${i}].${child.field}`,
          span: unref(child.span),
          class: child.class
        },
        () => getField(child, item)
      )
    })
  const getChildren = ({ disabled }: { disabled: boolean }) =>
    fields[field].map((item: object, i: number) => {
      return h(NGrid, { xGap: 10 }, () => [
        ...getChild(item, i, disabled),
        h(
          NGridItem,
          {
            span: 2
          },
          () =>
            h(
              NButton,
              {
                circle: true,
                type: 'error',
                size: 'small',
                disabled,
                onClick: () => {
                  fields[field].splice(i, 1)
                  rules.splice(i, 1)
                }
              },
              {
                icon: () => h(DeleteOutlined)
              }
            )
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
