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
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'
import { useChunjunDeployMode } from './'

export function useChunjun(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const jsonEditorSpan = ref(0)
  const customParameterSpan = ref(0)

  const initConstants = () => {
    jsonEditorSpan.value = 24
    customParameterSpan.value = 24
  }

  onMounted(() => {
    initConstants()
  })

  return [
    {
      type: 'editor',
      field: 'json',
      name: t('project.node.chunjun_json_template'),
      span: jsonEditorSpan,
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.sql_empty_tips')
      }
    },
    {
      type: 'custom-parameters',
      field: 'localParams',
      name: t('project.node.custom_parameters'),
      span: customParameterSpan,
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
    useChunjunDeployMode(24),
    {
      type: 'input',
      field: 'others',
      name: t('project.node.option_parameters'),
      props: {
        type: 'textarea',
        placeholder: t('project.node.option_parameters_tips')
      }
    }
  ]
}
