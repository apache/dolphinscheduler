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
import { camelCase, upperFirst } from 'lodash'
import type { FormRules, FormItemRule } from 'naive-ui'
import type { IJsonItem } from '../types'

const getField = (
  item: IJsonItem,
  fields: { [field: string]: any },
  rules?: FormRules
) => {
  const { type = 'input' } = item
  const renderTypeName = `render${upperFirst(camelCase(type))}`
  // TODO Support other widgets later
  if (type === 'custom-parameters') {
    let fieldRules: { [key: string]: FormItemRule }[] = []
    if (rules && !rules[item.field]) fieldRules = rules[item.field] = []
    // @ts-ignore
    return Field[renderTypeName](item, fields, fieldRules)
  }
  // @ts-ignore
  return Field[renderTypeName](item, fields)
}

export default getField
