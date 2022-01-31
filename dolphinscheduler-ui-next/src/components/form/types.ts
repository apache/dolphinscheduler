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

import type {
  GridProps,
  FormProps,
  FormItemGiProps,
  FormItemRule,
  FormRules,
  SelectOption
} from 'naive-ui'

type IType = 'input' | 'radio' | 'editor' | 'custom-parameters'

type IOption = SelectOption

interface IFormItem extends FormItemGiProps {
  widget: any
}

interface IMeta extends Omit<FormProps, 'model'> {
  elements?: IFormItem[]
  model: object
}

interface IFieldParams {
  field: string
  props: object
  fields: { [field: string]: any }
  options?: IOption[]
  rules?: FormRules | { [key: string]: FormRules }
  children?: IJsonItem[]
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
}

export {
  IMeta,
  IType,
  IJsonItem,
  IOption,
  FormItemRule,
  FormRules,
  IFormItem,
  GridProps,
  IFieldParams
}
