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
import type { FormRules, FormItemRule } from './types'

export function formatLabel(label?: string): string {
  if (!label) return ''
  const match = label.match(/^\$t\('(\S*)'\)/)
  return match ? match[1] : label
}

export function formatValidate(
  validate?: FormItemRule | FormRules
): FormItemRule {
  if (!validate) return {}
  if (Array.isArray(validate)) {
    validate.forEach((item: FormItemRule) => {
      if (!item?.message) delete item.message
      return item
    })
  }
  if (!validate.message) delete validate.message
  return validate
}
