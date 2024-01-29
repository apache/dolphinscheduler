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

export function useFlinkStream({
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
  const model = reactive<INodeData>({
    taskType: 'FLINK_STREAM',
    name: '',
    flag: 'YES',
    description: '',
    timeoutFlag: false,
    localParams: [],
    environmentCode: null,
    failRetryInterval: 1,
    failRetryTimes: 0,
    workerGroup: 'default',
    delayTime: 0,
    timeout: 30,
    programType: 'SCALA',
    deployMode: 'cluster',
    initScript: '',
    rawScript: '',
    flinkVersion: '<1.10',
    jobManagerMemory: '1G',
    taskManagerMemory: '2G',
    slot: 1,
    taskManager: 2,
    parallelism: 1,
    timeoutNotifyStrategy: ['WARN'],
    yarnQueue: ''
  })

  return {
    json: [
      Fields.useName(from),
      ...Fields.useTaskDefinition({ projectCode, from, readonly, data, model }),
      Fields.useRunFlag(),
      Fields.useCache(),
      Fields.useDescription(),
      Fields.useTaskPriority(),
      Fields.useWorkerGroup(),
      Fields.useEnvironmentName(model, !data?.id),
      Fields.useDelayTime(model),
      ...Fields.useFlink(model),
      Fields.usePreTasks()
    ] as IJsonItem[],
    model
  }
}
