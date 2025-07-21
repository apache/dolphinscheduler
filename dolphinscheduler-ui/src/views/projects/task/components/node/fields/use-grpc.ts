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

export function useGrpc(model: { [field: string]: any }): IJsonItem[] {
  //TODO: Implement gRPC specific logic
  const { t } = useI18n()

  const GRPC_CHECK_CONDITIONS = [
    {
      label: t('project.node.grpc_status_code_default'),
      value: 'STATUS_CODE_DEFAULT'
    },
    {
      label: t('project.node.grpc_status_code_custom'),
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
      // 输入直接绑定到 field，如何处理用户输入的 protobuf 定义和parse后的json结构，
      // 目前方案：两个都存，仅允许protobuf的控件输入，json只做显示，然后准备一个变量存储
      field: 'grpcServiceDefinition',
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
      field: 'methodName',
      name: t('project.node.grpc_method'),
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
      field: 'message',
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
      type: 'input',
      field: 'condition',
      name: t('project.node.grpc_condition'),
      props: {
        placeholder: t('project.node.grpc_condition_tips')
      },
      validate: {
        trigger: ['input', 'blur']
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
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: true
    })
  ]
}
