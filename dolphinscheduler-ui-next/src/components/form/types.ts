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
import { Ref } from 'vue'
import type {
  GridProps,
  FormProps,
  FormItemRule,
  FormRules,
  SelectOption,
  TreeSelectOption
} from 'naive-ui'

type IType =
  | 'input'
  | 'radio'
  | 'editor'
  | 'custom-parameters'
  | 'switch'
  | 'input-number'
  | 'select'
  | 'checkbox'
  | 'tree-select'

interface IOption extends SelectOption, TreeSelectOption {
  label: string
}

interface IFormItem {
  showLabel?: boolean
  path: string
  label?: string
  widget: any
  span?: number | Ref<number>
}

interface IMeta extends Omit<FormProps, 'model'> {
  elements?: IFormItem[]
  model: object
}

interface IJsonItem {
  field: string
  name?: string
  props?: object
  title?: string
  type?: IType
  validate?: FormItemRule
  value?: any
  options?: IOption[]
  children?: IJsonItem[]
  slots?: object
  span?: number | Ref<number>
}

export {
  IMeta,
  IType,
  IJsonItem,
  IOption,
  FormItemRule,
  FormRules,
  IFormItem,
  GridProps
}
