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

import { reactive } from 'vue'
import * as Fields from '../fields/index'
import type { IJsonItem, INodeData, ITaskData } from '../types'

export function useMlflow({
  projectCode,
  from = 0,
  readonly,
  data
}: {
  projectCode: number
  from?: number
  readonly?: boolean
  data?: ITaskData
}) {
  const model = reactive({
    name: '',
    taskType: 'MLFLOW',
    flag: 'YES',
    description: '',
    timeoutFlag: false,
    localParams: [],
    environmentCode: null,
    failRetryInterval: 1,
    failRetryTimes: 0,
    workerGroup: 'default',
    algorithm: 'svm',
    mlflowTrackingUri: 'http://127.0.0.1:5000',
    mlflowTaskType: 'MLflow Projects',
    deployType: 'MLFLOW',
    deployPort: '7000',
    mlflowJobType: 'CustomProject',
    automlTool: 'flaml',
    mlflowCustomProjectParameters: [],
    delayTime: 0,
    timeout: 30,
    timeoutNotifyStrategy: ['WARN']
  } as INodeData)

  return {
    json: [
      Fields.useName(from),
      ...Fields.useTaskDefinition({ projectCode, from, readonly, data, model }),
      Fields.useRunFlag(),
      Fields.useDescription(),
      Fields.useTaskPriority(),
      Fields.useWorkerGroup(),
      Fields.useEnvironmentName(model, !data?.id),
      ...Fields.useTaskGroup(model, projectCode),
      ...Fields.useFailed(),
      Fields.useDelayTime(model),
      ...Fields.useTimeoutAlarm(model),
      ...Fields.useMlflow(model),
      Fields.usePreTasks()
    ] as IJsonItem[],
    model
  }
}
