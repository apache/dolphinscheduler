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
import { useMlflowProjects, useMlflowModels } from '.'
import { useCustomParams, useResources } from '.'

export const MLFLOW_TASK_TYPE = [
  {
    label: 'MLflow Models',
    value: 'MLflow Models'
  },
  {
    label: 'MLflow Projects',
    value: 'MLflow Projects'
  }
]

export function useMlflow(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'input',
      field: 'mlflowTrackingUri',
      name: t('project.node.mlflow_mlflowTrackingUri'),
      span: 12,
      props: {
        placeholder: t('project.node.mlflow_mlflowTrackingUri_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: false,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(
              t('project.node.mlflow_mlflowTrackingUri_error_tips')
            )
          }
        }
      }
    },
    {
      type: 'select',
      field: 'mlflowTaskType',
      name: t('project.node.mlflow_taskType'),
      span: 12,
      options: MLFLOW_TASK_TYPE
    },
    ...useMlflowProjects(model),
    ...useMlflowModels(model),
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
  ]
}
