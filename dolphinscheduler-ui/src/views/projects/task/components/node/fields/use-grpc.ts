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
import { languages } from 'monaco-editor'
// import { ref } from 'vue'

export function useGrpc(model: { [field: string]: any }): IJsonItem[] {
  //TODO: Implement gRPC specific logic
  const { t } = useI18n()
  // const grpcItems = ref<IJsonItem[]>([])

  const GRPC_CHECK_CONDITIONS = [
    {
      label: t('project.node.status_code_default'),
      value: 'STATUS_CODE_DEFAULT'
    },
    {
      label: t('project.node.status_code_custom'),
      value: 'STATUS_CODE_CUSTOM'
    }
  ]

  model.grpcMethod = model.grpcMethod || 'UNARY'

  return [
    {
      type: 'input',
      class: 'input-url-name',
      field: 'url',
      name: t('project.node.grpc_url'),
      props: {
        placeholder: t('project.node.grpc_url_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.grpc_url_tips'))
          }
          if (value.search(new RegExp(/http[s]{0,1}:\/\/\S*/, 'i'))) {
            return new Error(t('project.node.http_url_validator'))
          }
        }
      }
    },
    {
      type: 'editor',
      class: 'input-url-name',
      field: 'serviceDefinition',
      name: t('project.node.grpc_service_definition'),
      props: {
        languages: 'protobuf',
        placeholder: t('project.node.grpc_service_definition_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.grpc_service_definition_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      class: 'input-url-name',
      field: 'methodName',
      name: t('project.node.grpc_method'),
      options: grpcMethods,
      props: {
        placeholder: t('project.node.grpc_method_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.grpc_method_tips'))
          }
        }
      }
    },
    {
      type: 'editor',
      class: 'input-url-name',
      field: 'requestMessage',
      name: t('project.node.grpc_message'),
      props: {
        languages: 'json',
        placeholder: t('project.node.grpc_message_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.grpc_message_tips'))
          }
          //check value is a valid json format
          try {
            JSON.parse(value)
          } catch (e) {
            return new Error(t('project.node.grpc_message_tips_invalid_json'))
          }
        }
      }
    },
    {
      type: 'select',
      field: 'grpcCheckCondition',
      name: t('project.node.grpc_check_condition'),
      options: GRPC_CHECK_CONDITIONS
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
      type: 'input',
      class: 'input-url-name',
      field: 'methodName',
      name: t('project.node.grpc_condition'),
      options: grpcMethods,
      props: {
        placeholder: t('project.node.grpc_condition_tips')
      },
      validate: {
        trigger: ['input', 'blur']
      }
    },
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: true
    })
  ]
}
