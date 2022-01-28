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

import * as Field from './fields'
import { formatValidate } from './utils'
import type { FormRules } from 'naive-ui'
import type { IJsonItem } from './types'

export default function getElementByJson(
  json: IJsonItem[],
  fields: { [field: string]: any }
) {
  const rules: FormRules = {}
  const initialValues: { [field: string]: any } = {}
  const elements = []

  const getElement = (item: IJsonItem) => {
    const { type, props = {}, field, options } = item
    // TODO Support other widgets later
    if (type === 'radio') {
      return Field.renderRadio({
        field,
        fields,
        props,
        options
      })
    }
    if (type === 'editor') {
      return Field.renderEditor({
        field,
        fields,
        props
      })
    }

    return Field.renderInput({ field, fields, props })
  }

  for (let item of json) {
    fields[item.field] = item.value
    initialValues[item.field] = item.value
    if (item.validate) rules[item.field] = formatValidate(item.validate)
    elements.push({
      label: item.name,
      path: item.field,
      widget: () => getElement(item)
    })
  }

  return { rules, elements, initialValues }
}
