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

import { toRef, Ref } from 'vue'
import { formatValidate } from './utils'
import getField from './fields/get-field'
import { omit, isFunction } from 'lodash'
import type { FormRules } from 'naive-ui'
import type { IFormItem, IJsonItem } from './types'

export default function getElementByJson(
  json: IJsonItem[],
  fields: { [field: string]: any }
) {
  const rules: FormRules = {}
  const initialValues: { [field: string]: any } = {}
  const elements: IFormItem[] = []
  for (const item of json) {
    const mergedItem = isFunction(item) ? item() : item
    const { name, value, field, children, validate, ...rest } = mergedItem
    if (value || value === 0) {
      fields[field] = value
      initialValues[field] = value
    }
    if (validate) rules[field] = formatValidate(validate)
    const element: IFormItem = {
      showLabel: !!name,
      ...omit(rest, ['type', 'props', 'options']),
      label: name,
      path: !children ? field : '',
      widget: () => getField(item, fields, rules),
      span: toRef(mergedItem, 'span') as Ref<number>
    }
    elements.push(element)
  }
  return { rules, elements, initialValues }
}
