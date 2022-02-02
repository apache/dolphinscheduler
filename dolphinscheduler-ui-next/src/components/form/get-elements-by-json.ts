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

import { formatValidate } from './utils'
import getField from './fields/get-field'
import type { FormRules } from 'naive-ui'
import type { IJsonItem } from './types'

export default function getElementByJson(
  json: IJsonItem[],
  fields: { [field: string]: any }
) {
  const rules: FormRules = {}
  const initialValues: { [field: string]: any } = {}
  const elements = []

  for (let item of json) {
    if (item.value) {
      fields[item.field] = item.value
      initialValues[item.field] = item.value
    }
    if (item.validate) rules[item.field] = formatValidate(item.validate)
    elements.push({
      label: item.name,
      path: !item.children ? item.field : '',
      showLabel: !!item.name,
      widget: () => getField(item, fields, rules)
    })
  }

  return { rules, elements, initialValues }
}
