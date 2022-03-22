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
import {
  NFormItemGi,
  NSpace,
  NButton,
  NGrid,
  NGridItem,
  NInput
} from 'naive-ui'
import { isFunction } from 'lodash'
import { PlusOutlined, DeleteOutlined } from '@vicons/antd'
import type { IJsonItem, FormItemRule } from '../types'

const MultiInput = defineComponent({
  name: 'MultiInput',
  emits: ['add'],
  setup(props, ctx) {
    const formItem = useFormItem({})
    const onAdd = () => void ctx.emit('add')

    return { onAdd, disabled: formItem.mergedDisabledRef }
  },
  render() {
    const { disabled, $slots, onAdd } = this

    return h(
      NSpace,
      { vertical: true, style: { width: '100%' } },
      {
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
      }
    )
  }
})

export function renderMultiInput(
  item: IJsonItem,
  fields: { [field: string]: any },
  unused: { [key: string]: FormItemRule }[]
) {
  const { field } = isFunction(item) ? item() : item
  // the fields is the data of the task definition.
  // the item is the options of this component in the form.

  const getChild = (value: string, i: number) => {
    const mergedItem = isFunction(item) ? item() : item
    return h(
      NFormItemGi,
      {
        showLabel: false,
        path: `${mergedItem.field}[${i}]`,
        span: unref(mergedItem.span)
      },
      () =>
        h(NInput, {
          ...mergedItem.props,
          value: value,
          onUpdateValue: (value: string) =>
            void (fields[mergedItem.field][i] = value)
        })
    )
  }

  //initialize the component by using data
  const getChildren = ({ disabled }: { disabled: boolean }) => {
    return fields[field].map((value: string, i: number) => {
      return h(NGrid, { xGap: 10 }, () => [
        getChild(value, i),
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
                }
              },
              {
                icon: () => h(DeleteOutlined)
              }
            )
        )
      ])
    })
  }

  return h(
    MultiInput,
    {
      name: field,
      onAdd: () => {
        fields[field].push('')
      }
    },
    {
      default: getChildren
    }
  )
}
