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

import { reactive, ref } from 'vue'
import { useDynamicLocales } from './use-dynamic-locales'
import { useFormField } from './use-form-field'
import { useFormValidate } from './use-form-validate'
import { useFormStructure } from './use-form-structure'
import { useFormRequest } from './use-form-request'

export function useTaskForm(data: any) {
  const variables = reactive({
    taskForm: ref(),
    formStructure: {},
    model: {},
    rules: {}
  })

  useDynamicLocales(data.locales)
  variables.model = useFormField(data.forms)
  variables.rules = useFormValidate(data.forms, variables.model)
  variables.formStructure = useFormStructure(
    useFormRequest(data.apis, data.forms)
  )

  const handleValidate = () => {
    variables.taskForm.validate((err: any) => {
      if (err) return
    })
  }

  return {
    variables,
    handleValidate
  }
}
