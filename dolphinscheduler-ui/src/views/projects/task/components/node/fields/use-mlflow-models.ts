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
import { watch, ref } from 'vue'

export function useMlflowModels(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const deployTypeSpan = ref(0)
  const deployModelKeySpan = ref(0)
  const deployPortSpan = ref(0)

  const setFlag = () => {
    model.isModels = model.mlflowTaskType === 'MLflow Models' ? true : false
  }

  const resetSpan = () => {
    deployTypeSpan.value = model.isModels ? 12 : 0
    deployModelKeySpan.value = model.isModels ? 24 : 0
    deployPortSpan.value = model.isModels ? 12 : 0
  }

  watch(
    () => [model.mlflowTaskType],
    () => {
      setFlag()
      resetSpan()
    }
  )

  setFlag()
  resetSpan()

  return [
    {
      type: 'select',
      field: 'deployType',
      name: t('project.node.mlflow_deployType'),
      span: deployTypeSpan,
      options: DEPLOY_TYPE
    },
    {
      type: 'input',
      field: 'deployModelKey',
      name: t('project.node.mlflow_deployModelKey'),
      span: deployModelKeySpan
    },
    {
      type: 'input',
      field: 'deployPort',
      name: t('project.node.mlflow_deployPort'),
      span: deployPortSpan
    }
  ]
}

const DEPLOY_TYPE = [
  {
    label: 'MLFLOW',
    value: 'MLFLOW'
  },
  {
    label: 'DOCKER',
    value: 'DOCKER'
  }
]
