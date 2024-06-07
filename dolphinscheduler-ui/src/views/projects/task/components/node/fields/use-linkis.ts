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
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'

export function useLinkis(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const configEditorSpan = computed(() => (model.useCustom ? 24 : 0))
  const paramEditorSpan = computed(() => (model.useCustom ? 0 : 24))
  computed(() => (model.useCustom ? 0 : 24))
  return [
    {
      type: 'switch',
      field: 'useCustom',
      name: t('project.node.custom_config')
    },
    {
      type: 'custom-parameters',
      field: 'paramScript',
      name: t('project.node.option_parameters'),
      span: paramEditorSpan,
      children: [
        {
          type: 'input',
          field: 'prop',
          span: 10,
          props: {
            placeholder: t('project.node.prop_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.prop_tips'))
              }

              const sameItems = model.localParams.filter(
                (item: { prop: string }) => item.prop === value
              )

              if (sameItems.length > 1) {
                return new Error(t('project.node.prop_repeat'))
              }
            }
          }
        },
        {
          type: 'input',
          field: 'value',
          span: 10,
          props: {
            placeholder: t('project.node.value_tips'),
            maxLength: 256
          }
        }
      ]
    },
    {
      type: 'editor',
      field: 'rawScript',
      name: t('project.node.script'),
      span: configEditorSpan,
      validate: {
        trigger: ['input', 'trigger'],
        required: model.useCustom,
        validator(validate: any, value: string) {
          if (model.useCustom && !value) {
            return new Error(t('project.node.script_tips'))
          }
        }
      }
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
  ]
}
