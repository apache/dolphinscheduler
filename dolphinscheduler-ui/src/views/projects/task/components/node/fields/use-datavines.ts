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
import type { IJsonItem } from '../types'
import { useCustomParams } from '.'

export function useDatavines(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'input',
      field: 'datavinesAddress',
      name: t('project.node.datavines_address'),
      span: 24,
      props: {
        placeholder: t('project.node.datavines_address_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.datavines_address_tips'))
          }
          if (!/^https?:\/\//.test(value)) {
            return new Error(t('project.node.datavines_address_error_tips'))
          }
        }
      }
    },
    {
      type: 'input-number',
      field: 'datavinesJobId',
      name: t('project.node.datavines_job_id'),
      span: 12,
      props: {
        min: 1,
        precision: 0,
        placeholder: t('project.node.datavines_job_id_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: number) {
          if (!value || value <= 0) {
            return new Error(t('project.node.datavines_job_id_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'datavinesToken',
      name: t('project.node.datavines_token'),
      span: 12,
      props: {
        type: 'password',
        showPasswordOn: 'click',
        placeholder: t('project.node.datavines_token_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.datavines_token_tips'))
          }
        }
      }
    },
    {
      type: 'input-number',
      field: 'datavinesInterval',
      name: t('project.node.datavines_interval'),
      span: 8,
      props: {
        min: 1,
        precision: 0
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: number) {
          if (value === null || value === undefined || value < 1) {
            return new Error(t('project.node.datavines_interval_tips'))
          }
        }
      }
    },
    {
      type: 'input-number',
      field: 'datavinesTimeout',
      name: t('project.node.datavines_timeout'),
      span: 8,
      props: {
        min: 1,
        precision: 0
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: number) {
          if (value === null || value === undefined || value <= 0) {
            return new Error(t('project.node.datavines_timeout_tips'))
          }
        }
      }
    },
    {
      type: 'input-number',
      field: 'datavinesRetryTimes',
      name: t('project.node.datavines_retry_times'),
      span: 8,
      props: {
        min: 0,
        precision: 0
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: number) {
          if (value === null || value === undefined || value < 0) {
            return new Error(t('project.node.datavines_retry_times_tips'))
          }
        }
      }
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}
