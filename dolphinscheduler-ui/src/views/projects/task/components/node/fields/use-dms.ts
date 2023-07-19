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

import type { IJsonItem } from '../types'
import { watch, ref } from 'vue'
import { useCustomParams, useResources } from '.'

export function useDms(model: { [field: string]: any }): IJsonItem[] {
  const jsonDataSpan = ref(0)
  const replicationTaskArnSpan = ref(0)
  const replicationTaskIdentifierSpan = ref(0)
  const sourceEndpointArnSpan = ref(0)
  const targetEndpointArnSpan = ref(0)
  const replicationInstanceArnSpan = ref(0)
  const migrationTypeSpan = ref(0)
  const tableMappingsSpan = ref(0)

  const setFlag = () => {
    model.isCreateAndNotJson =
      !model.isRestartTask && !model.isJsonFormat ? true : false
    model.isRestartAndNotJson =
      model.isRestartTask && !model.isJsonFormat ? true : false
  }

  const resetSpan = () => {
    jsonDataSpan.value = model.isJsonFormat ? 24 : 0
    replicationTaskArnSpan.value = model.isRestartAndNotJson ? 24 : 0
    migrationTypeSpan.value = model.isCreateAndNotJson ? 24 : 0
    sourceEndpointArnSpan.value = model.isCreateAndNotJson ? 24 : 0
    replicationTaskIdentifierSpan.value = model.isCreateAndNotJson ? 24 : 0
    targetEndpointArnSpan.value = model.isCreateAndNotJson ? 24 : 0
    replicationInstanceArnSpan.value = model.isCreateAndNotJson ? 24 : 0
    tableMappingsSpan.value = model.isCreateAndNotJson ? 24 : 0
  }

  watch(
    () => [model.isRestartTask, model.isJsonFormat],
    () => {
      setFlag()
      resetSpan()
    }
  )

  setFlag()
  resetSpan()

  return [
    {
      type: 'switch',
      field: 'isRestartTask',
      name: 'isRestartTask',
      span: 12
    },
    {
      type: 'switch',
      field: 'isJsonFormat',
      name: 'isJsonFormat',
      span: 12
    },
    {
      type: 'editor',
      field: 'jsonData',
      name: 'jsonData',
      span: jsonDataSpan
    },
    {
      type: 'select',
      field: 'migrationType',
      name: 'migrationType',
      span: migrationTypeSpan,
      options: MIGRATION_TYPE
    },
    {
      type: 'input',
      field: 'replicationTaskIdentifier',
      name: 'replicationTaskIdentifier',
      span: replicationTaskIdentifierSpan
    },
    {
      type: 'input',
      field: 'replicationInstanceArn',
      name: 'replicationInstanceArn',
      span: replicationInstanceArnSpan
    },
    {
      type: 'input',
      field: 'sourceEndpointArn',
      name: 'sourceEndpointArn',
      span: sourceEndpointArnSpan
    },
    {
      type: 'input',
      field: 'targetEndpointArn',
      name: 'targetEndpointArn',
      span: targetEndpointArnSpan
    },
    {
      type: 'editor',
      field: 'tableMappings',
      name: 'tableMappings',
      span: tableMappingsSpan
    },
    {
      type: 'input',
      field: 'replicationTaskArn',
      name: 'replicationTaskArn',
      span: replicationTaskArnSpan
    },
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}

export const MIGRATION_TYPE = [
  {
    label: 'full-load',
    value: 'full-load'
  },
  {
    label: 'cdc',
    value: 'cdc'
  },
  {
    label: 'full-load-and-cdc',
    value: 'full-load-and-cdc'
  }
]
