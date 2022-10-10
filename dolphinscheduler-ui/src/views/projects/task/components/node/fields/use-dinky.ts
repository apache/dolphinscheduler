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

export function useDinky(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'input',
      field: 'address',
      name: t('project.node.dinky_address'),
      props: {
        placeholder: t('project.node.dinky_address_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(_validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.dinky_address_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'taskId',
      name: t('project.node.dinky_task_id'),
      props: {
        placeholder: t('project.node.dinky_task_id_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(_validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.dinky_task_id_tips'))
          }
        }
      }
    },
    {
      type: 'switch',
      field: 'online',
      name: t('project.node.dinky_online')
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}
