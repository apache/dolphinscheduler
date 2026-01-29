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
import type { IJsonItem } from '../types'

export function useTimeoutAlarm(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const span = computed(() => (model.timeoutFlag ? 12 : 0))

  const strategyOptions = [
    {
      label: t('project.node.timeout_alarm'),
      value: 'WARN'
    },
    {
      label: t('project.node.timeout_failure'),
      value: 'FAILED'
    }
  ]

  return [
    {
      type: 'switch',
      field: 'timeoutFlag',
      name: t('project.node.timeout_alarm'),
      props: {
        'on-update:value': (value: boolean) => {
          if (value) {
            if (!model.timeoutNotifyStrategy.length)
              model.timeoutNotifyStrategy = ['WARN']
            if (!model.timeout) model.timeout = 30
          }
        }
      }
    },
    {
      type: 'checkbox',
      field: 'timeoutNotifyStrategy',
      name: t('project.node.timeout_strategy'),
      options: strategyOptions,
      span: span,
      validate: {
        trigger: ['input'],
        validator(validate: any, value: []) {
          if (model.timeoutFlag && !value.length) {
            return new Error(t('project.node.timeout_strategy_tips'))
          }
        }
      }
    },
    {
      type: 'input-number',
      field: 'timeout',
      name: t('project.node.timeout_period'),
      span,
      props: {
        max: Math.pow(10, 10) - 1
      },
      slots: {
        suffix: () => t('project.node.minute')
      },
      validate: {
        trigger: ['input'],
        validator(validate: any, value: number) {
          if (model.timeoutFlag && !/^[1-9]\d*$/.test(String(value))) {
            return new Error(t('project.node.timeout_period_tips'))
          }
        }
      }
    }
  ]
}
