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

import { useI18n } from 'vue-i18n'
import type { FormItemRule } from 'naive-ui'

export function useFormValidate(forms: Array<any>) {
  const { t } = useI18n()
  const validate: any = {}

  const setValidate = (v: any): object => {
    const data: any = {
      required: v.required,
      trigger: v.trigger
    }

    if (v.type) {
      if (v.type === 'non-empty') {
        data['validator'] = (rule: FormItemRule, value: string) => {
          if (!value) {
            return Error(t(v.message))
          }
        }
      }
    }

    return data
  }

  forms.forEach((f: any) => {
    if (!f.validate && Object.keys(f.validate).length <= 0) return

    if (f.field.indexOf('.') >= 0) {
      const hierarchy = f.field.split('.')
      validate[hierarchy[1]] = setValidate(f.validate)
    } else {
      validate[f.field] = setValidate(f.validate)
    }
  })

  return validate
}