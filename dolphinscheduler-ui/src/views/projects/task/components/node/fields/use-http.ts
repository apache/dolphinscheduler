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
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'

export function useHttp(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const HTTP_CHECK_CONDITIONS = [
    {
      label: t('project.node.status_code_default'),
      value: 'STATUS_CODE_DEFAULT'
    },
    {
      label: t('project.node.status_code_custom'),
      value: 'STATUS_CODE_CUSTOM'
    },
    {
      label: t('project.node.body_contains'),
      value: 'BODY_CONTAINS'
    },
    {
      label: t('project.node.body_not_contains'),
      value: 'BODY_NOT_CONTAINS'
    }
  ]

  return [
    {
      type: 'input',
      field: 'url',
      name: t('project.node.http_url'),
      props: {
        placeholder: t('project.node.http_url_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.http_url_tips'))
          }
          if (
            value.search(new RegExp(/http[s]{0,1}:\/\/\S*/, 'i'))
          ) {
            return new Error(t('project.node.http_url_validator'))
          }
        }
      }
    },
    {
      type: 'select',
      field: 'httpMethod',
      span: 12,
      name: t('project.node.http_method'),
      options: HTTP_METHODS
    },
    {
      type: 'custom-parameters',
      field: 'httpParams',
      name: t('project.node.http_parameters'),
      children: [
        {
          type: 'input',
          field: 'prop',
          span: 6,
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

              const sameItems = model.httpParams.filter(
                (item: { prop: string }) => item.prop === value
              )

              if (sameItems.length > 1) {
                return new Error(t('project.node.prop_repeat'))
              }
            }
          }
        },
        {
          type: 'select',
          field: 'httpParametersType',
          span: 6,
          options: POSITIONS,
          value: 'PARAMETER'
        },
        {
          type: 'input',
          field: 'value',
          span: 6,
          props: {
            placeholder: t('project.node.value_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.value_required_tips'))
              }
            }
          }
        }
      ]
    },
    {
      type: 'select',
      field: 'httpCheckCondition',
      name: t('project.node.http_check_condition'),
      options: HTTP_CHECK_CONDITIONS
    },
    {
      type: 'input',
      field: 'condition',
      name: t('project.node.http_condition'),
      props: {
        type: 'textarea',
        placeholder: t('project.node.http_condition_tips')
      }
    },
    {
      type: 'input-number',
      field: 'connectTimeout',
      name: t('project.node.connect_timeout'),
      span: 12,
      props: {
        max: Math.pow(7, 10) - 1
      },
      slots: {
        suffix: () => t('project.node.ms')
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: string) {
          if (!Number.isInteger(parseInt(value))) {
            return new Error(
              t('project.node.connect_timeout') +
                t('project.node.positive_integer_tips')
            )
          }
        }
      }
    },
    {
      type: 'input-number',
      field: 'socketTimeout',
      name: t('project.node.socket_timeout'),
      span: 12,
      props: {
        max: Math.pow(7, 10) - 1
      },
      slots: {
        suffix: () => t('project.node.ms')
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: string) {
          if (!Number.isInteger(parseInt(value))) {
            return new Error(
              t('project.node.socket_timeout') +
                t('project.node.positive_integer_tips')
            )
          }
        }
      }
    },
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: true
    })
  ]
}

const HTTP_METHODS = [
  {
    value: 'GET',
    label: 'GET'
  },
  {
    value: 'POST',
    label: 'POST'
  },
  {
    value: 'HEAD',
    label: 'HEAD'
  },
  {
    value: 'PUT',
    label: 'PUT'
  },
  {
    value: 'DELETE',
    label: 'DELETE'
  }
]

const POSITIONS = [
  {
    value: 'PARAMETER',
    label: 'Parameter'
  },
  {
    value: 'BODY',
    label: 'Body'
  },
  {
    value: 'HEADERS',
    label: 'Headers'
  }
]
