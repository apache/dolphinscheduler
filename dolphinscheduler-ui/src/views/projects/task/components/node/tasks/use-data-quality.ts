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

import { Ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import * as Fields from '../fields/index'
import type { IJsonItem, INodeData, ITaskData } from '../types'

export function useDataQuality({
  projectCode,
  from = 0,
  readonly,
  data,
  jsonRef,
  updateElements
}: {
  projectCode: number
  from?: number
  readonly?: boolean
  data?: ITaskData
  jsonRef: Ref<IJsonItem[]>
  updateElements: () => void
}) {
  const { t } = useI18n()
  const model = reactive({
    taskType: 'DATA_QUALITY',
    name: '',
    flag: 'YES',
    description: '',
    timeoutFlag: false,
    timeoutNotifyStrategy: ['WARN'],
    timeout: 30,
    localParams: [],
    environmentCode: null,
    failRetryInterval: 1,
    failRetryTimes: 0,
    workerGroup: 'default',
    delayTime: 0,
    ruleId: 1,
    deployMode: 'cluster',
    driverCores: 1,
    driverMemory: '512M',
    numExecutors: 2,
    executorMemory: '2G',
    executorCores: 2,
    others: '--conf spark.yarn.maxAppAttempts=1',
    yarnQueue: ''
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
      Fields.useDelayTime(model),
      ...Fields.useTimeoutAlarm(model),
      ...Fields.useRules(model, (items: IJsonItem[], len: number) => {
        jsonRef.value.splice(15, len, ...items)
        updateElements()
      }),
      Fields.useDeployMode(),
      Fields.useDriverCores(),
      Fields.useDriverMemory(),
      Fields.useExecutorNumber(),
      Fields.useExecutorMemory(),
      Fields.useExecutorCores(),
      Fields.useYarnQueue(),
      {
        type: 'input',
        field: 'others',
        name: t('project.node.option_parameters'),
        props: {
          type: 'textarea',
          placeholder: t('project.node.option_parameters_tips')
        }
      },
      ...Fields.useCustomParams({
        model,
        field: 'localParams',
        isSimple: true
      }),
      Fields.usePreTasks()
    ] as IJsonItem[],
    model
  }
}
