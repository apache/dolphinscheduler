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

import { h } from 'vue'
import { NInput, NRadio, NRadioGroup, NSpace } from 'naive-ui'
import Editor from '@/components/monaco-editor'
import type { IFieldParams } from './types'

// TODO Support other widgets later
// Input
export function renderInput(params: IFieldParams) {
  const { props, fields, field } = params
  return h(NInput, {
    ...props,
    value: fields[field],
    onUpdateValue: (value) => void (fields[field] = value)
  })
}

// Radio && RadioGroup
export function renderRadio(params: IFieldParams) {
  const { props, fields, field, options } = params
  if (!options || options.length === 0) {
    return h(NRadio, {
      ...props,
      value: fields[field],
      onUpdateChecked: (checked) => void (fields[field] = checked)
    })
  }
  return h(
    NRadioGroup,
    {
      value: fields[field],
      onUpdateValue: (value) => void (fields[field] = value)
    },
    () =>
      h(NSpace, null, () =>
        options.map((option) => h(NRadio, option, () => option.label))
      )
  )
}

// Editor
export function renderEditor(params: IFieldParams) {
  const { props, fields, field } = params
  return h(Editor, {
    ...props,
    value: fields[field],
    onUpdateValue: (value) => void (fields[field] = value)
  })
}
