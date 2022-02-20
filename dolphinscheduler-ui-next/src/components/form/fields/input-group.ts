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

const InputGroup = defineComponent({
  name: 'InputGroup',
  emits: ['add'],
  setup(props, ctx) {
    const formItem = useFormItem({})

    const onAdd = () => void ctx.emit('add')

    return { onAdd, disabled: formItem.mergedDisabledRef }
  },
  render() {
    const { disabled, $slots, onAdd } = this

    console.log('reander')
    console.log($slots)

    return h(NSpace, {vertical: true, style:{width: '100%'}}, {
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

export function renderInputGroup(
  item: IJsonItem,
  fields: { [field: string]: any },
  rules: { [key: string]: FormItemRule }[]
) {
  const { field, children = [] } = item
  let defaultValue: { [field: string]: any } = {}
  let ruleItem: { [key: string]: FormItemRule } = {}

  console.log('input group...')
  console.log(children)
  console.log('item...')
  console.log(item)
  console.log('fields...')
  console.log(fields)
  console.log('field')
  console.log(field)

  children.forEach((child) => {
    console.log('child')
    console.log(child)
    console.log({...child})
    defaultValue[child.field] = {...child} || null
    console.log('default..')
    console.log(defaultValue)
    if (child.validate) ruleItem[child.field] = formatValidate(child.validate)
  })

  console.log('defaultValue')
  console.log(defaultValue)

  const getChild = (item: object, i: number) =>
    children.map((child: IJsonItem) => {
      return h(
        NFormItemGi,
        {
          showLabel: false,
          path: `${field}[${i}].${child.field}`,
          span: unref(child.span)
        },
        () => getField(child, item)
      )
    })
  const getChildren = ({ disabled }: { disabled: boolean }) =>
    fields[field].map((item: object, i: number) => {
      console.log('getChildren....')
      console.log(...getChild(item, i))
      return h(NGrid, { xGap: 10 }, () => [
        ...getChild(item, i),
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
    InputGroup,
    {
      onAdd: () => {
        console.log('add....')
        console.log(defaultValue)
        // rules.push(ruleItem)
        console.log('push...')
        console.log(fields[field])
        fields[field].push({ ...defaultValue })
      }
    },
    {
      default: getChildren
    }
  )
}
