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
import type { IJsonItem, INodeData } from '../types'
import { ITaskData } from '../types'

export function useChunjun({
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
    taskType: 'CHUNJUN',
    flag: 'YES',
    description: '',
    deployMode: 'local',
    timeoutFlag: false,
    localParams: [],
    environmentCode: null,
    failRetryInterval: 1,
    failRetryTimes: 0,
    workerGroup: 'default',
    cpuQuota: -1,
    memoryMax: -1,
    delayTime: 0,
    timeout: 30,
    customConfig: false,
    dsType: 'MYSQL',
    dtType: 'MYSQL',
    preStatements: [],
    postStatements: [],
    timeoutNotifyStrategy: ['WARN']
  } as INodeData)

  let extra: IJsonItem[] = []
  if (from === 1) {
    extra = [
      Fields.useTaskType(model, readonly),
      Fields.useProcessName({
        model,
        projectCode,
        isCreate: !data?.id,
        from,
        processName: data?.processName
      })
    ]
  }

  return {
    json: [
      Fields.useName(from),
      ...extra,
      Fields.useRunFlag(),
      Fields.useDescription(),
      Fields.useTaskPriority(),
      Fields.useWorkerGroup(),
      Fields.useEnvironmentName(model, !model.id),
      ...Fields.useTaskGroup(model, projectCode),
      ...Fields.useFailed(),
      Fields.useDelayTime(model),
      ...Fields.useTimeoutAlarm(model),
      ...Fields.useChunjun(model),
      Fields.usePreTasks()
    ] as IJsonItem[],
    model
  }
}
