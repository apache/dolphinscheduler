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
  NInput, NSelect
} from 'naive-ui'
import { PlusOutlined, DeleteOutlined } from '@vicons/antd'
import type { IJsonItem, FormItemRule } from '../types'
import getField from "@/components/form/fields/get-field";

const MultiCondition = defineComponent({
  name: 'MultiCondition',
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
                size: 'tiny',
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

export function renderMultiCondition(
  item: IJsonItem,
  fields: { [field: string]: any },
  rules: { [key: string]: FormItemRule }[]
) {
  let ruleItem: { [key: string]: FormItemRule } = {}

  // the fields is the data of the task definition.
  // the item is the options of this component in the form.
  const { field, children = [] } = item

  const getChild = (item: object, i: number) =>
      children.map((child: IJsonItem) => {
        return h(
            NFormItemGi,
            {
              showLabel: child.name? true : false,
              label: child.name? child.name : '',
              path: `${fields[field]}[${i}].${child.field}`,
              span: unref(child.span)
            },
            () => getField(child, item)
        )
      })

  //initialize the component by using data
  const getChildren = ({ disabled }: { disabled: boolean }) =>
    fields[item.field].map((item: object, i: number) => {
      return h(NGrid, { xGap: 10 }, () => [
        ...getChild(item, i),
        h(
          NGridItem,
          {
            span: 2,
            style: { alignSelf: 'center' }
          },
          () =>
            h(
              NButton,
              {
                circle: true,
                type: 'error',
                size: 'tiny',
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

  return h(
     MultiCondition,
    {
      name: item.field,
      onAdd: () => {
        fields[item.field].push('')
      }
    },
    {
      default: getChildren
    }
  )
}
