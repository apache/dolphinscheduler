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
import { useCustomParams, useResources } from '.'
import type { IJsonItem } from '../types'

export function useHiveCli(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const hiveSqlScriptSpan = computed(() => (model.hiveCliTaskExecutionType === 'SCRIPT' ? 24 : 0))

  return [
    {
      type: 'select',
      field: 'hiveCliTaskExecutionType',
      span: 12,
      name: t('project.node.hive_cli_task_execution_type'),
      options: HIVE_CLI_TASK_EXECUTION_TYPES,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'editor',
      field: 'hiveSqlScript',
      name: t('project.node.hive_sql_script'),
      props: {
        language: 'sql'
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true
      },
      span: hiveSqlScriptSpan
    },
    {
      type: 'input',
      field: 'hiveCliOptions',
      name: t('project.node.hive_cli_options'),
      props: {
        placeholder: t('project.node.hive_cli_options_tips')
      }
    },
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}

export const HIVE_CLI_TASK_EXECUTION_TYPES = [
  {
    label: 'FROM_SCRIPT',
    value: 'SCRIPT'
  },
  {
    label: 'FROM_FILE',
    value: 'FILE'
  }
]
