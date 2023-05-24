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

export function useSeaTunnel({
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
    taskType: 'SEATUNNEL',
    flag: 'YES',
    description: '',
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
    startupScript: 'seatunnel.sh',
    runMode: 'RUN',
    useCustom: true,
    deployMode: 'client',
    master: 'YARN',
    masterUrl: '',
    resourceFiles: [],
    timeoutNotifyStrategy: ['WARN'],
    rawScript:
      'env {\n' +
      '  execution.parallelism = 2\n' +
      '  job.mode = "BATCH"\n' +
      '  checkpoint.interval = 10000\n' +
      '}\n' +
      '\n' +
      'source {\n' +
      '  FakeSource {\n' +
      '    parallelism = 2\n' +
      '    result_table_name = "fake"\n' +
      '    row.num = 16\n' +
      '    schema = {\n' +
      '      fields {\n' +
      '        name = "string"\n' +
      '        age = "int"\n' +
      '      }\n' +
      '    }\n' +
      '  }\n' +
      '}\n' +
      '\n' +
      'sink {\n' +
      '  Console {\n' +
      '  }\n' +
      '}'
  } as INodeData)

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
      ...Fields.useTaskGroup(model, projectCode),
      ...Fields.useFailed(),
      ...Fields.useResourceLimit(),
      Fields.useDelayTime(model),
      ...Fields.useTimeoutAlarm(model),
      ...Fields.useSeaTunnel(model),
      Fields.usePreTasks()
    ] as IJsonItem[],
    model
  }
}
