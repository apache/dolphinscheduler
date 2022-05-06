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

import { h, unref } from 'vue'
import { NCheckbox, NCheckboxGroup, NSpace } from 'naive-ui'
import { isFunction } from 'lodash'
import type { IJsonItem } from '../types'

export function renderCheckbox(
  item: IJsonItem,
  fields: { [field: string]: any }
) {
  const { props, field, options } = isFunction(item) ? item() : item
  if (!options) {
    return h(NCheckbox, {
      ...props,
      value: fields[field],
      onUpdateChecked: (checked: boolean) => void (fields[field] = checked)
    })
  }
  return h(
    NCheckboxGroup,
    {
      value: fields[field],
      onUpdateValue: (value) => void (fields[field] = value)
    },
    () =>
      h(NSpace, null, () =>
        unref(options).map((option: object) => h(NCheckbox, { ...option }))
      )
  )
}
