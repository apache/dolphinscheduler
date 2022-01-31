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
import * as Field from './index'
import type { FormRules } from 'naive-ui'
import type { IJsonItem } from '../types'

const getField = (
  item: IJsonItem,
  fields: { [field: string]: any },
  rules?: FormRules
) => {
  const { type, props = {}, field, options, children } = item
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

  if (type === 'custom-parameters') {
    const params = {
      field,
      fields,
      children,
      props,
      rules: []
    }
    if (rules) {
      params.rules = rules[field] = []
    }
    return Field.renderCustomParameters(params)
  }

  return Field.renderInput({ field, fields, props })
}

export default getField
