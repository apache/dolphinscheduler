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

import { ref } from 'vue'

export function useFormField(forms: any) {
  const model: any = {}

  forms.forEach((f: any) => {
    if (f.field.indexOf('.') >= 0) {
      const hierarchy = f.field.split('.')
      for (let l = 0; l < hierarchy.length; l++) {
        model[hierarchy[0]] = {
          [hierarchy[1]]: ref(f.defaultValue ? f.defaultValue : '')
        }
      }
    } else {
      model[f.field] = ref(f.defaultValue ?
        f.defaultValue :
        f.type === 'select' ?
          null :
          ''
      )
    }
  })

  return {
    model
  }
}